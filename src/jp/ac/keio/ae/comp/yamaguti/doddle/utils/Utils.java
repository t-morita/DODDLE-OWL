package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import net.java.sen.*;

/**
 * @author takeshi morita
 */
public class Utils {
    public static final String RESOURCE_DIR = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";

    public static ImageIcon getImageIcon(String icon) {
        return new ImageIcon(DODDLE.class.getClassLoader().getResource(RESOURCE_DIR + icon));
    }

    public static void addJaComplexWord(List tokenList, Set<String> complexWordSet) {
        try {
            for (String complexWord : complexWordSet) {
                StringTagger tagger = StringTagger.getInstance();
                Token[] complexWordToken = tagger.analyze(complexWord);
                List complexWordElementList = new ArrayList();
                for (int i = 0; i < complexWordToken.length; i++) {
                    complexWordElementList.add(complexWordToken[i].getBasicString());
                }
                // System.out.println(complexWord);
                // System.out.println(complexWordList);
                Utils.addComplexWord(complexWord, complexWordElementList, tokenList, complexWordSet);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
}
