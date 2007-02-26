package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import net.infonode.docking.*;
import net.infonode.docking.properties.*;
import net.infonode.docking.theme.*;
import net.infonode.docking.util.*;
import net.infonode.util.*;
import net.java.sen.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class Utils {
    public static final String RESOURCE_DIR = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";

    public static ImageIcon getImageIcon(String icon) {
        return new ImageIcon(DODDLE.class.getClassLoader().getResource(RESOURCE_DIR + icon));
    }

    public static URL getURL(String icon) {
        return DODDLE.class.getClassLoader().getResource(RESOURCE_DIR + icon);
    }

    public static RootWindow createDODDLERootWindow(ViewMap viewMap) {
        RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, true);
        rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
        RootWindowProperties properties = new RootWindowProperties();
        DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();
        properties.addSuperObject(currentTheme.getRootWindowProperties());
        RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();
        properties.addSuperObject(titleBarStyleProperties);
        rootWindow.getRootWindowProperties().addSuperObject(properties);
        return rootWindow;
    }
    
    public static void addJaComplexWord(List tokenList, List<String> inputWordList) {
        Set<String> complexWordSet = new HashSet<String>();
        Map<String, List<String>> complexWordElementListMap = new HashMap<String, List<String>>();
        for (String complexWord : inputWordList) {
            try {
                StringTagger tagger = StringTagger.getInstance();
                Token[] complexWordTokenList = tagger.analyze(complexWord);
                if (complexWordTokenList.length == 1) {
                    continue; // 複合ではない
                }
                complexWordSet.add(complexWord);
                List complexWordElementList = new ArrayList();
                for (int i = 0; i < complexWordTokenList.length; i++) {
                    complexWordElementList.add(complexWordTokenList[i].getBasicString());
                }
                complexWordElementListMap.put(complexWord, complexWordElementList);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        for (String complexWord : complexWordSet) {
            List<String> complexWordElementList = complexWordElementListMap.get(complexWord);
            Utils.addComplexWord(complexWord, complexWordElementList, tokenList, complexWordSet);
        }
    }

    public static void addEnComplexWord(List<String> tokenList, List<String> inputWordList) {
        Set<String> complexWordSet = new HashSet<String>();
        Map<String, List<String>> complexWordElementListMap = new HashMap<String, List<String>>();
        for (String complexWord : inputWordList) {
            List<String> complexWordElementList = Arrays.asList(complexWord.split("\\s+"));
            if (complexWordElementList.size() == 1) {
                continue; // 複合語ではない
            }
            complexWordSet.add(complexWord);
            complexWordElementListMap.put(complexWord, complexWordElementList);
        }
        for (String complexWord : complexWordSet) {
            List<String> complexWordElementList = complexWordElementListMap.get(complexWord);
            Utils.addComplexWord(complexWord, complexWordElementList, tokenList, complexWordSet);
        }
    }

    private static void addComplexWord(String complexWord, List<String> complexWordElementList, List<String> tokenList,
            Set complexWordSet) {
        for (int i = 0; i < tokenList.size(); i++) {
            List<String> complexWordSizeList = new ArrayList<String>();
            for (int j = 0; complexWordSizeList.size() != complexWordElementList.size(); j++) {
                if ((i + j) == tokenList.size()) {
                    break;
                }
                String nw = tokenList.get(i + j);
                if (complexWordSet.contains(nw)) {
                    continue;
                }
                complexWordSizeList.add(nw);
            }
            if (complexWordElementList.size() == complexWordSizeList.size()) {
                boolean isComplexWordList = true;
                for (int j = 0; j < complexWordElementList.size(); j++) {
                    if (!complexWordElementList.get(j).equals(complexWordSizeList.get(j))) {
                        isComplexWordList = false;
                        break;
                    }
                }
                if (isComplexWordList) {
                    tokenList.add(i, complexWord);
                    i++;
                }
            }
        }
    }

    public static int getUserObjectNum(DefaultMutableTreeNode rootNode) {
        Set userObjectSet = new HashSet();
        userObjectSet.add(rootNode.getUserObject());
        getAllUserObject(rootNode, userObjectSet);
        return userObjectSet.size();
    }

    private static void getAllUserObject(TreeNode node, Set userObjectSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            userObjectSet.add(childNode.getUserObject());
            getAllUserObject(childNode, userObjectSet);
        }
    }

    public static Set getAllConcept(TreeModel treeModel) {
        Set<Concept> conceptSet = new HashSet<Concept>();
        if (!(treeModel.getRoot() instanceof ConceptTreeNode)) { return conceptSet; }
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        conceptSet.add(rootNode.getConcept());
        getAllConcept(rootNode, conceptSet);
        return conceptSet;
    }

    private static void getAllConcept(TreeNode node, Set<Concept> conceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            conceptSet.add(childNode.getConcept());
            getAllConcept(childNode, conceptSet);
        }
    }

    public static double getChildCntAverage(TreeModel treeModel) {
        List<Integer> childNodeCntList = new ArrayList<Integer>();
        if (!(treeModel.getRoot() instanceof ConceptTreeNode)) { return 0; }
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        getChildCntAverage(rootNode, childNodeCntList);
        double totalChildNum = 0;
        for (int childNum : childNodeCntList) {
            totalChildNum += childNum;
        }
        if (childNodeCntList.size() == 0) { return 0; }
        return totalChildNum / childNodeCntList.size();
    }

    private static void getChildCntAverage(TreeNode node, List<Integer> childNodeCntList) {
        if (0 < node.getChildCount()) {
            childNodeCntList.add(node.getChildCount());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            getChildCntAverage(childNode, childNodeCntList);
        }
    }

    /**
     * ResourceクラスのgetLocalNameメソッドは，ローカル名が数字からはじまる場合に名前空間の分割がうまくできないため，
     * 独自に実装している．（不完全）
     */
    public static String getLocalName(Resource res) {
        return res.getURI().replaceAll(getNameSpace(res), "");
    }

    /**
     * ResourceクラスのgetNameSpaceメソッドは，ローカル名が数字からはじまる場合に名前空間の分割がうまくできないため，
     * 独自に実装している．（不完全）
     */
    public static String getNameSpace(Resource res) {
        String ns = res.getNameSpace();
        if (ns == null) { return ""; }
        if (ns.matches(".*#$") || ns.matches(".*/$")) { return ns; }
        String ns2 = ns.split("#\\d*[^#/]*$")[0];
        if (ns2 != null && !ns2.equals(ns)) { return ns2 + "#"; }
        ns2 = ns.split("/\\d*[^#/]*$")[0];
        if (ns2 != null && !ns2.equals(ns)) { return ns2 + "/"; }
        return "";
    }
    
    public static JComponent createWestPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.WEST);
        return panel;
    }

    public static JComponent createEastPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.EAST);
        return panel;
    }

    public static JComponent createNorthPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.NORTH);
        return panel;
    }

    public static JComponent createSouthPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.SOUTH);
        return panel;
    }
    
    public static JComponent createTitledPanel(JComponent component, String title) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(component, BorderLayout.CENTER);
        return p;
    }
    
    public static JComponent createTitledPanel(JComponent component, String title, int width, int height) {
        component.setPreferredSize(new Dimension(width, height));
        component.setMinimumSize(new Dimension(width, height));
        return createTitledPanel(component, title);
    }
}
