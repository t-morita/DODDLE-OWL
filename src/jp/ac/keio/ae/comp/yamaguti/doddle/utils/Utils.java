package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.tree.*;

import net.java.sen.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

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
    
    public static void addJaComplexWord(List tokenList, Set<String> complexWordSet) {
        for (String complexWord : complexWordSet) {
            try {
                StringTagger tagger = StringTagger.getInstance();
                Token[] complexWordTokenList = tagger.analyze(complexWord);
                List complexWordElementList = new ArrayList();
                for (int i = 0; i < complexWordTokenList.length; i++) {
                    complexWordElementList.add(complexWordTokenList[i].getBasicString());
                }            
                // System.out.println(complexWord);
                // System.out.println(complexWordList);
                Utils.addComplexWord(complexWord, complexWordElementList, tokenList, complexWordSet);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void addEnComplexWord(List<String> tokenList, Set<String> complexWordSet) {
        for (String complexWord : complexWordSet) {
            List<String> complexWordElementList = Arrays.asList(complexWord.split("\\s+"));
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
}
