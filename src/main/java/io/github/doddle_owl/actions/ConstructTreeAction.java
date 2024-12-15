/*
 * Project Name: DODDLE-OWL (a Domain Ontology rapiD DeveLopment Environment - OWL extension)
 * Project Website: https://doddle-owl.github.io/
 *
 * Copyright (C) 2004-2024 Takeshi Morita. All rights reserved.
 *
 * This file is part of DODDLE-OWL.
 *
 * DODDLE-OWL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DODDLE-OWL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DODDLE-OWL.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.github.doddle_owl.actions;

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.concept_selection.TreeConstructionOption;
import io.github.doddle_owl.models.concept_tree.CompoundConceptTreeInterface;
import io.github.doddle_owl.models.concept_tree.ConceptTreeNode;
import io.github.doddle_owl.models.ontology_api.EDR;
import io.github.doddle_owl.models.term_selection.TermModel;
import io.github.doddle_owl.task_analyzer.Morpheme;
import io.github.doddle_owl.utils.ConceptTreeMaker;
import io.github.doddle_owl.utils.OWLOntologyManager;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.views.concept_selection.ConceptSelectionPanel;
import io.github.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.ConceptTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Takeshi Morita
 */
public class ConstructTreeAction {

    private final Map<String, Set<Concept>> wordCorrespondConceptSetMap; // 入力単語と適切に対応するIDのマッピング
    private Map<DefaultMutableTreeNode, String> abstractNodeLabelMap;
    private final Map<TermModel, TreeConstructionOption> compoundConstructTreeOptionMap;
    private final ConceptSelectionPanel conceptSelectionPanel;
    private final ClassTreeConstructionPanel constructClassPanel;
    private final PropertyTreeConstructionPanel constructPropertyPanel;

    private final DefaultListModel undefinedTermListModel;

    private final DODDLEProjectPanel project;

    public ConstructTreeAction(boolean isNounAndVerbTree, DODDLEProjectPanel p) {
        project = p;
        conceptSelectionPanel = p.getConceptSelectionPanel();
        constructClassPanel = p.getConstructClassPanel();
        constructPropertyPanel = p.getConstructPropertyPanel();
        wordCorrespondConceptSetMap = conceptSelectionPanel.getTermCorrespondConceptSetMap();
        undefinedTermListModel = conceptSelectionPanel.getUndefinedTermListPanel().getModel();
        compoundConstructTreeOptionMap = conceptSelectionPanel
                .getCompoundConstructTreeOptionMap();
        constructClassPanel.clearPanel();
        constructPropertyPanel.clearPanel();
        conceptSelectionPanel.setConstructNounAndVerbTree(isNounAndVerbTree);
    }

    private boolean isExistNode(TreeNode node, TreeNode childNode, String word, Concept ic) {
        return childNode.toString().equals(word)
                || (node.getParent() == null && childNode.toString().equals(ic.getURI()));
    }

    private boolean isEnglish(String iw) {
        return iw.matches("(\\w|\\s)*");
    }

    private void addCompoundWordNode(int len, TermModel iwModel, TreeNode node) {
        if (len == iwModel.getCompoundWordLength()) {
            return;
        }
        List<Morpheme> morphemeList = iwModel.getMorphemeList();
        StringBuilder buf = new StringBuilder();
        boolean isEnglish = isEnglish(iwModel.getTerm());
        for (int i = morphemeList.size() - len - 1; i < morphemeList.size(); i++) {
            // 食べ放題が「食べる放題」のように活用が原形に変換されるのを防ぐために，getSurface()を使用
            buf.append(morphemeList.get(i).getSurface());
            if (isEnglish) {
                buf.append(" ");
            }
        }
        String word = buf.toString();
        // System.out.println(word);
        if (isEnglish) { // スペースを除去
            word = word.substring(0, word.length() - 1);
        }
        // wordの長さが照合単語以上の長さのときに複合語の階層化を行う
        if (iwModel.getMatchedTerm().length() <= word.length()) {
            Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(iwModel.getTerm());
            for (Concept ic : correspondConceptSet) {
                for (int i = 0; i < node.getChildCount(); i++) {
                    TreeNode childNode = node.getChildAt(i);
                    if (isExistNode(node, childNode, word, ic)) {
                        addCompoundWordNode(len + 1, iwModel, childNode);
                        return;
                    }
                }
                DefaultMutableTreeNode childNode;
                if (word.equals(iwModel.getMatchedTerm())) {
                    childNode = new DefaultMutableTreeNode(ic.getURI());
                } else {
                    childNode = new DefaultMutableTreeNode(word);
                }

                ((DefaultMutableTreeNode) node).add(childNode);
                addCompoundWordNode(len + 1, iwModel, childNode);
            }
        } else {
            // System.out.println("照合単語: " +
            // iwModel.getMatchedWord() + " => 短すぎる単語: " + word);
            addCompoundWordNode(len + 1, iwModel, node);
        }
    }

    private boolean hasXCompoundWordChild(TreeNode node) {
        int compoundWordChildNum = 0;
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            if (childNode.getUserObject() instanceof String) {
                compoundWordChildNum += 1;
                if (getAbstractConceptChildNum() < compoundWordChildNum) {
                    return false;
                }
            }
        }
        return compoundWordChildNum <= getAbstractConceptChildNum();
    }

    private void trimCompoundWordNode(DefaultMutableTreeNode node) {
        Set<DefaultMutableTreeNode> addNodeSet = new HashSet<>();
        Set<DefaultMutableTreeNode> removeNodeSet = new HashSet<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            trimCompoundWord(node, childNode, addNodeSet, removeNodeSet);
        }
        for (DefaultMutableTreeNode anode : addNodeSet) {
            node.add(anode);
        }
        for (DefaultMutableTreeNode rnode : removeNodeSet) {
            node.remove(rnode);
        }
        if (0 < addNodeSet.size()) {
            trimCompoundWordNode(node);
        }
        // 兄弟概念をすべて処理した後に，子ノードの処理に移る
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            trimCompoundWordNode(childNode);
        }
    }

    private void trimAbstractNode(DefaultMutableTreeNode node) {
        Set<String> sameNodeSet = new HashSet<>();
        Set<DefaultMutableTreeNode> addNodeSet = new HashSet<>();
        Set<DefaultMutableTreeNode> removeNodeSet = new HashSet<>();
        Set<String> reconstructNodeSet = new HashSet<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            extractMoveCompoundWordNodeSet(sameNodeSet, addNodeSet, removeNodeSet, childNode);
            extractReconstructedNodeSet(reconstructNodeSet, childNode);
        }
        moveCompoundWordNodeSet(node, addNodeSet, removeNodeSet, reconstructNodeSet);
        // 兄弟概念をすべて処理した後に，子ノードの処理に移る
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            trimAbstractNode(childNode);
        }
    }

    /**
     * @param node
     * @param addNodeSet
     * @param removeNodeSet
     * @param reconstructNodeSet
     */
    private void moveCompoundWordNodeSet(DefaultMutableTreeNode node,
                                         Set<DefaultMutableTreeNode> addNodeSet, Set<DefaultMutableTreeNode> removeNodeSet,
                                         Set<String> reconstructNodeSet) {
        // 子ノードを一つしかもたない抽象ノードの子ノードをnodeに追加
        for (DefaultMutableTreeNode addNode : addNodeSet) {
            node.add(addNode);
        }
        // 子ノードを一つしかもたない抽象ノードを削除
        for (DefaultMutableTreeNode removeNode : removeNodeSet) {
            node.remove(removeNode);
            abstractNodeLabelMap.remove(removeNode);
        }
        Set<DefaultMutableTreeNode> duplicatedNodeSet = new HashSet<>();
        // 同一レベルに抽象ノードに追加されたノードが含まれている場合には削除
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            if (reconstructNodeSet.contains(childNode.toString())) {
                duplicatedNodeSet.add(childNode);
            }
        }
        // System.out.println("dnode set: " + duplicatedNodeSet);
        for (DefaultMutableTreeNode dnode : duplicatedNodeSet) {
            node.remove(dnode);
        }
    }

    /**
     * @param reconstructNodeSet
     * @param childNode
     */
    private void extractReconstructedNodeSet(Set<String> reconstructNodeSet,
                                             DefaultMutableTreeNode childNode) {
        // X個以上子ノード(複合語)を持つ抽象中間ノードに追加されたノードをreconstructNodeSetに保存
        if (childNode.getUserObject() instanceof Concept && !hasXCompoundWordChild(childNode)) {
            for (int i = 0; i < childNode.getChildCount(); i++) {
                DefaultMutableTreeNode reconstructNode = (DefaultMutableTreeNode) childNode
                        .getChildAt(i);
                reconstructNodeSet.add(reconstructNode.toString());
            }
        }
    }

    /**
     * @param addNodeSet
     * @param removeNodeSet
     * @param childNode
     */
    private void extractMoveCompoundWordNodeSet(Set<String> sameNodeSet,
                                                Set<DefaultMutableTreeNode> addNodeSet, Set<DefaultMutableTreeNode> removeNodeSet,
                                                DefaultMutableTreeNode childNode) {
        if (childNode.getUserObject() instanceof Concept && hasXCompoundWordChild(childNode)) {
            DefaultMutableTreeNode grandChildNode = (DefaultMutableTreeNode) childNode
                    .getChildAt(0);
            if (grandChildNode.getUserObject() instanceof String) {
                // System.out.println("rm: " + childNode);
                removeNodeSet.add(childNode);
                abstractNodeLabelMap.remove(childNode);
                if (!sameNodeSet.contains(grandChildNode.toString())) {
                    sameNodeSet.add(grandChildNode.toString());
                    DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(
                            grandChildNode.toString());
                    deepCloneTreeNode(grandChildNode, addNode);
                    addNodeSet.add(addNode);
                }
            }
        }
    }

    private void trimCompoundWord(DefaultMutableTreeNode node, DefaultMutableTreeNode childNode,
                                  Set addNodeSet, Set removeNodeSet) {
        if (childNode.getUserObject() instanceof String
                && !compoundWordSet.contains(childNode.toString())) {
            for (int i = 0; i < childNode.getChildCount(); i++) {
                addNodeSet.add(childNode.getChildAt(i));
            }
            removeNodeSet.add(childNode);
        }
    }

    private Set<String> compoundWordSet; // 入力語彙に含まれない複合語を削除するさいに参照

    private boolean isInputConcept(Concept c, Set<Concept> conceptSet) {
        for (Concept ic : conceptSet) {
            if (ic.getURI().equals(c.getURI())) {
                return true;
            }
        }
        return false;
    }

    private void setCompoundConcept(CompoundConceptTreeInterface ccTreeInterface,
                                    Set<Concept> conceptSet) {
        compoundWordSet = new HashSet<>();
        Map<String, String> matchedWordURIMap = new HashMap<>();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        for (TermModel iwModel : compoundConstructTreeOptionMap.keySet()) {
            if (iwModel == null) {
                continue;
            }
            compoundWordSet.add(iwModel.getTerm());
            compoundWordSet.add(iwModel.getMatchedTerm());
            TreeConstructionOption ctOption = compoundConstructTreeOptionMap.get(iwModel);
            if (ctOption.getConcept() == null) {
                // オプション復元時に参照できないURIをConceptとした場合にnullとなる．
                continue;
            }
            matchedWordURIMap.put(iwModel.getMatchedTerm(), ctOption.getConcept().getURI());
            if (!isInputConcept(ctOption.getConcept(), conceptSet)) {
                continue;
            }
            if (ctOption.getOption().equals("SAME")) {
                ccTreeInterface.addJPWord(ctOption.getConcept().getURI(), iwModel.getTerm());
            } else if (ctOption.getOption().equals("SUB")) {
                addCompoundWordNode(0, iwModel, rootNode);
            }
        }
        DODDLE_OWL.getLogger().info(Translator.getTerm("ConstructConceptTreeFromCompoundWordsMessage"));

        // printDebugTree(rootNode, "before trimming");
        if (project.getConceptSelectionPanel().getPartiallyMatchedOptionPanel().isTrimming()) {
            // childNodeは汎用オントロジー中の概念と対応しているため処理しない
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                trimCompoundWordNode(childNode);
            }
            DODDLE_OWL.getLogger().info(Translator.getTerm("TrimmingCompoundWordsMessage"));
        }

        if (project.getConceptSelectionPanel().getPartiallyMatchedOptionPanel()
                .isAddAbstractConcept()) {
            addAbstractTreeNode(rootNode);
            trimAbstractNode(rootNode);
            trimLeafAbstractNode();
            DODDLE_OWL.getLogger().info(Translator.getTerm("AddAbstractInternalConceptsMessage"));
        }

        // printDebugTree(rootNode, "add abstract node");
        compoundWordSet.clear();
        ccTreeInterface.addCompoundWordConcept(matchedWordURIMap, abstractNodeLabelMap, rootNode);
    }

    private int getAbstractConceptChildNum() {
        return project.getConceptSelectionPanel().getPartiallyMatchedOptionPanel()
                .getAbstractConceptChildNodeNum();
    }

    /**
     * childCount以下の子ノードしかもたない抽象ノードラベルを削除
     */
    private void trimLeafAbstractNode() {
        Set<DefaultMutableTreeNode> leafAbstractNodeSet = new HashSet<>();
        for (DefaultMutableTreeNode anode : abstractNodeLabelMap.keySet()) {
            if (anode.getChildCount() < getAbstractConceptChildNum()) {
                leafAbstractNodeSet.add(anode);
            }
        }
        for (DefaultMutableTreeNode leafAbstractNode : leafAbstractNodeSet) {
            abstractNodeLabelMap.remove(leafAbstractNode);
        }
    }

    private Set conceptStrSet;

    private void countNode(TreeNode node) {
        conceptStrSet.add(node.toString());
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode childNode = node.getChildAt(i);
            countNode(childNode);
        }
    }

    private void countRootChildNode(TreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode childNode = node.getChildAt(i);
            conceptStrSet.add(childNode.toString());
        }
    }

    /**
     * @param rootNode
     */
    private void addAbstractTreeNode(DefaultMutableTreeNode rootNode) {
        nodeRemoveNodeSetMap = new HashMap();
        abstractNodeLabelMap = new HashMap<>();
        tmpcnt = 0;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DODDLE_OWL.getLogger().info(rootNode.getChildAt(i) + ": " + (i + 1) + "/" + rootNode.getChildCount());
            reconstructCompoundTree(1, (DefaultMutableTreeNode) rootNode.getChildAt(i));
            // 多重継承している場合もあるので，一度クローンを抽象ノードに挿入した後に，
            // 親ノードから削除する．
            for (Object o1 : nodeRemoveNodeSetMap.entrySet()) {
                Entry entry = (Entry) o1;
                DefaultMutableTreeNode supNode = (DefaultMutableTreeNode) entry.getKey();
                Set removeNodeSet = (Set) entry.getValue();
                for (Object o : removeNodeSet) {
                    supNode.remove((DefaultMutableTreeNode) o);
                }
            }
            nodeRemoveNodeSetMap.clear();
        }
    }

    /**
     * @param rootNode
     */
    private void printDebugTree(DefaultMutableTreeNode rootNode, String title) {
        JFrame frame = new JFrame();
        frame.setTitle(title);
        JTree debugTree = new JTree(new DefaultTreeModel(rootNode));
        frame.getContentPane().add(new JScrollPane(debugTree));
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private int tmpcnt;
    private Map nodeRemoveNodeSetMap;

    /**
     * 接頭語で複合語階層を再構成する
     * <p>
     * d: デバッグ用．再帰の深さをはかるため．
     */
    private void reconstructCompoundTree(int d, DefaultMutableTreeNode node) {
        if (node.getChildCount() == 0) {
            return;
        }
        // System.out.println(node + ": " + d);
        if (!(node.getUserObject() instanceof Concept)) { // 抽象ノードを上位に持つ複合語は処理しない
            Map abstractConceptTreeNodeMap = new HashMap();
            Set<DefaultMutableTreeNode> abstractNodeSet = new HashSet<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                if (childNode.getUserObject() instanceof String) {
                    TermModel iwModel = conceptSelectionPanel
                            .makeInputTermModel(childNode.toString());
                    Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(iwModel
                            .getTopBasicWord());
                    if (correspondConceptSet != null) {
                        for (Concept headConcept : correspondConceptSet) {
                            tmpcnt++;
                            if (headConcept != null && 1 < iwModel.getMorphemeList().size()) {
                                Set<Concept> supConceptSet = getSupConceptSet(headConcept
                                        .getLocalName());
                                for (Concept supConcept : supConceptSet) {
                                    DefaultMutableTreeNode abstractNode = getAbstractNode(node,
                                            abstractConceptTreeNodeMap, childNode, supConcept,
                                            iwModel);
                                    abstractNodeSet.add(abstractNode);
                                }
                            }
                        }
                    }
                }
            }
            for (DefaultMutableTreeNode anode : abstractNodeSet) {
                node.add(anode);
            }
        }
        // 兄弟ノードをすべて処理した後に，子ノードの処理に移る
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            reconstructCompoundTree(++d, childNode);
        }
    }

    /**
     * @param iwModel
     * @param headConcept
     * @param j
     * @param headWordBuf
     */
    private void printHeadWordDebug(TermModel iwModel, Concept headConcept, int j,
                                    StringBuilder headWordBuf) {
        /*
         * for (int j = 0; j < iwModel.getMatchedPoint(); j++) { List<String>
         * headWordList = iwModel.getWordList().subList(j,
         * iwModel.getMatchedPoint()); StringBuilder headWordBuf = new
         * StringBuilder(); for (String w : headWordList) {
         * headWordBuf.append(w); } headConcept =
         * wordConceptMap.get(headWordBuf.toString()); if (headConcept != null)
         * { printHeadWordDebug(iwModel, headConcept, j, headWordBuf); break; }
         * }
         */
        if (j < iwModel.getMatchedPoint() - 1) {
            System.out.println("********************************");
            System.out.println("word: " + iwModel.getTerm());
            System.out.println("matchedword: " + iwModel.getMatchedTerm());
            System.out.println("head word: " + headWordBuf);
            System.out.println("head concept :" + headConcept);
        }
    }

    private Set getSupConceptSet(String id) {
        Set supConceptSet = constructClassPanel.getSupConceptSet(id);
        supConceptSet.addAll(constructPropertyPanel.getSupConceptSet(id));
        return supConceptSet;
    }

    /**
     * @param node
     * @param abstractConceptTreeNodeMap
     * @param childNode
     * @param supConcept
     */
    private DefaultMutableTreeNode getAbstractNode(DefaultMutableTreeNode node,
                                                   Map abstractConceptTreeNodeMap, DefaultMutableTreeNode childNode, Concept supConcept,
                                                   TermModel iwModel) {
        DefaultMutableTreeNode abstractNode = getAbstractNode(abstractConceptTreeNodeMap,
                supConcept, iwModel);
        // System.out.println("語頭の上位概念: " + supConcept.getWord());
        // System.out.println("複合語の上位概念: " + node.toString());
        insertNode(childNode, abstractNode);
        setRemoveNode(node, childNode);
        return abstractNode;
    }

    /**
     * @param node
     * @param childNode
     */
    private void setRemoveNode(DefaultMutableTreeNode node, DefaultMutableTreeNode childNode) {
        Set removeNodeSet;
        if (nodeRemoveNodeSetMap.get(node) != null) {
            removeNodeSet = (Set) nodeRemoveNodeSetMap.get(node);
        } else {
            removeNodeSet = new HashSet();
        }
        removeNodeSet.add(childNode);
        nodeRemoveNodeSetMap.put(node, removeNodeSet);
    }

    /**
     *
     */
    private void insertNode(DefaultMutableTreeNode childNode, DefaultMutableTreeNode abstractNode) {
        DefaultMutableTreeNode insertNode = new DefaultMutableTreeNode(childNode.toString());
        deepCloneTreeNode(childNode, insertNode); // 多重継承している場合があるので，クローンを挿入する
        abstractNode.add(insertNode);
    }

    /*
     * TreeNodeの深いコピーを行う． orgNodeをinsertNodeにコピーする
     */
    private void deepCloneTreeNode(DefaultMutableTreeNode orgNode, DefaultMutableTreeNode insertNode) {
        for (int i = 0; i < orgNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) orgNode.getChildAt(i);
            DefaultMutableTreeNode childNodeClone = new DefaultMutableTreeNode(
                    childNode.getUserObject());
            insertNode.add(childNodeClone);
            deepCloneTreeNode(childNode, childNodeClone);
        }
    }

    /**
     * @param abstractConceptTreeNodeMap
     * @param supConcept
     * @return
     */
    private DefaultMutableTreeNode getAbstractNode(Map abstractConceptTreeNodeMap,
                                                   Concept supConcept, TermModel iwModel) {
        DefaultMutableTreeNode abstractNode;
        if (abstractConceptTreeNodeMap.get(supConcept) != null) {
            abstractNode = (DefaultMutableTreeNode) abstractConceptTreeNodeMap.get(supConcept);
        } else {
            abstractNode = new DefaultMutableTreeNode(supConcept);
            String abstractNodeLabel = supConcept.getWord() + iwModel.getBasicWordWithoutTopWord();
            abstractNodeLabel = abstractNodeLabel.replaceAll("\\s*", "");
            abstractNodeLabelMap.put(abstractNode, abstractNodeLabel);
            abstractConceptTreeNodeMap.put(supConcept, abstractNode);
        }
        return abstractNode;
    }

    private TreeModel makeClassTreeModel(Set<Concept> nounConceptSet) {
        TreeModel classTreeModel;
        if (project.getConceptSelectionPanel().getConstructionTypePanel().isNewConstruction()) {
            constructClassPanel.initialize();
            if (project.getConceptSelectionPanel().getPerfectlyMatchedOptionPanel()
                    .isConstruction()) {
                classTreeModel = constructClassPanel.getTreeModel(nounConceptSet);
            } else {
                classTreeModel = ConceptTreeMaker.getInstance().getDefaultConceptTreeModel(
                        new HashSet<>(), project,
                        ConceptTreeMaker.DODDLE_CLASS_ROOT_URI);
            }
            classTreeModel = constructClassPanel.setConceptTreeModel(classTreeModel);
        } else { // add concepts action
            classTreeModel = constructClassPanel.getConceptTreeModel();
            nounConceptSet = constructClassPanel.getConceptSet();
        }
        constructClassPanel.setUndefinedTermListModel(undefinedTermListModel);
        DODDLE_OWL.STATUS_BAR.addValue();
        if (project.getConceptSelectionPanel().getPartiallyMatchedOptionPanel()
                .isConstruction()) {
            setCompoundConcept(constructClassPanel, nounConceptSet);
            trimUnnecessaryAbstractNode(constructClassPanel);
        }
        ConceptTreeNode rootNode = (ConceptTreeNode) classTreeModel.getRoot();
        Set<ConceptTreeNode> replaceNodeSet = new HashSet<>();
        Set<String> conceptIdSetForReplaceSubConcepts = conceptSelectionPanel
                .getURISetForReplaceSubConcepts();
        // System.out.println(conceptIdSetForReplaceSubConcepts);
        checkReplaceSubConcepts(rootNode, conceptIdSetForReplaceSubConcepts, replaceNodeSet);
        replaceSubConcepts(replaceNodeSet);
        DODDLE_OWL.STATUS_BAR.addValue();
        return classTreeModel;
    }

    private void replaceSubConcepts(Set<ConceptTreeNode> replaceNodeSet) {
        Object[] replaceNodes = replaceNodeSet.toArray();
        for (Object replaceNode1 : replaceNodes) {
            ConceptTreeNode replaceNode = (ConceptTreeNode) replaceNode1;
            // System.out.println("replace: " + replaceNode);
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) replaceNode.getParent();
            // System.out.println("parent: " + parentNode);
            // System.out.println(replaceNode.getChildCount());
            while (replaceNode.getChildCount() != 0) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) replaceNode
                        .getChildAt(0);
                parentNode.add(childNode);
                // System.out.println("childNode: " + childNode);
            }
            parentNode.remove(replaceNode);
            // System.out.println("");
        }
    }

    private void checkReplaceSubConcepts(ConceptTreeNode node,
                                         Set<String> conceptIdSetForReplaceSubConcepts, Set<ConceptTreeNode> replaceNodeSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (conceptIdSetForReplaceSubConcepts.contains(childNode.getConcept().getURI())) {
                replaceNodeSet.add(childNode);
            }
            checkReplaceSubConcepts(childNode, conceptIdSetForReplaceSubConcepts, replaceNodeSet);
        }
    }

    private TreeModel makePropertyTreeModel(Set<Concept> verbConceptSet) {
        TreeModel propertyTreeModel;
        if (project.getConceptSelectionPanel().getConstructionTypePanel().isNewConstruction()) {
            constructPropertyPanel.initialize();
            if (project.getConceptSelectionPanel().getPerfectlyMatchedOptionPanel()
                    .isConstruction()) {
                propertyTreeModel = constructPropertyPanel.getTreeModel(
                        constructClassPanel.getAllConceptURI(), verbConceptSet,
                        ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI);
            } else {
                propertyTreeModel = ConceptTreeMaker.getInstance().getDefaultConceptTreeModel(new HashSet<>(), project,
                        ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI);
            }
            propertyTreeModel = constructPropertyPanel.setConceptTreeModel(propertyTreeModel);
        } else {
            propertyTreeModel = constructPropertyPanel.getConceptTreeModel();
            verbConceptSet = constructClassPanel.getConceptSet();
        }
        constructPropertyPanel.setUndefinedTermListModel(undefinedTermListModel);

        DODDLE_OWL.STATUS_BAR.addValue();
        if (project.getConceptSelectionPanel().getPartiallyMatchedOptionPanel()
                .isConstruction()) {
            setCompoundConcept(constructPropertyPanel, verbConceptSet);
            trimUnnecessaryAbstractNode(constructPropertyPanel);
        }
        ConceptTreeNode rootNode = (ConceptTreeNode) propertyTreeModel.getRoot();
        Set<ConceptTreeNode> replaceNodeSet = new HashSet<>();
        Set<String> uriSetForReplaceSubConcepts = conceptSelectionPanel
                .getURISetForReplaceSubConcepts();
        checkReplaceSubConcepts(rootNode, uriSetForReplaceSubConcepts, replaceNodeSet);
        replaceSubConcepts(replaceNodeSet);
        DODDLE_OWL.STATUS_BAR.addValue();
        return propertyTreeModel;
    }

    /**
     * 不要な抽象ノードを削除（抽象ノードの親ノードの子ノードが対象の抽象ノードのみの場合は，抽象ノードを追加する意味がないため削除））
     */
    private void trimUnnecessaryAbstractNode(ConceptTreeConstructionPanel conceptTreePanel) {
        DefaultTreeModel treeModel = (DefaultTreeModel) conceptTreePanel.getConceptTreeModel();
        Set<ConceptTreeNode> unnecessaryNodeSet = new HashSet<>();
        getUnnecessaryAbstractNode((ConceptTreeNode) treeModel.getRoot(), unnecessaryNodeSet);
        for (ConceptTreeNode unnecessaryNode : unnecessaryNodeSet) {
            ConceptTreeNode parentNode = (ConceptTreeNode) unnecessaryNode.getParent();
            while (0 < unnecessaryNode.getChildCount()) {
                ConceptTreeNode childNode = (ConceptTreeNode) unnecessaryNode.getChildAt(0);
                treeModel.insertNodeInto(childNode, parentNode, 0);
            }
            treeModel.removeNodeFromParent(unnecessaryNode);
        }
    }

    /**
     * 不要な抽象ノードをunnecessaryNodeSetに格納
     */
    private void getUnnecessaryAbstractNode(ConceptTreeNode node,
                                            Set<ConceptTreeNode> unnecessaryNodeSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getConcept().getWord().contains("[A]") && node.getChildCount() == 1) {
                unnecessaryNodeSet.add(childNode);
            }
            getUnnecessaryAbstractNode(childNode, unnecessaryNodeSet);
        }
    }

    /**
     * 「該当なし」とされた概念を辞書載っていない単語リストに追加
     */
    private void setUndefinedTermSet() {
        for (Entry<String, Set<Concept>> entry : wordCorrespondConceptSetMap.entrySet()) {
            String word = entry.getKey();
            Set<Concept> cset = entry.getValue();
            if (cset.size() == 1 && cset.contains(ConceptSelectionPanel.nullConcept)) {
                undefinedTermListModel.addElement(word);
            }
        }
    }

    public void constructTree() {
        if (project.getConceptSelectionPanel().getConstructionTypePanel().isNewConstruction()) {
            project.resetURIConceptMap();
        } else if (project.getConceptSelectionPanel().getPerfectlyMatchedOptionPanel()
                .isConstruction()) {
            JOptionPane
                    .showMessageDialog(DODDLE_OWL.getCurrentProject(),
                            Translator.getTerm("NotImplementedMessage"), "Error",
                            JOptionPane.ERROR_MESSAGE);
            DODDLE_OWL.STATUS_BAR.hideProgressBar();
            return;
        }
        project.initUndoManager();
        constructClassPanel.initUndo();
        constructPropertyPanel.initUndo();
        constructClassPanel.setVisibleIsaTree(false);
        constructClassPanel.setVisibleHasaTree(false);
        constructPropertyPanel.setVisibleIsaTree(false);
        constructPropertyPanel.setVisibleHasaTree(false);

        setUndefinedTermSet();
        conceptSelectionPanel.setInputConceptSet(); // 入力概念のセット
        Set<Concept> inputConceptSet = conceptSelectionPanel.getInputConceptSet();
        if (inputConceptSet.size() == 0) {
            DODDLE_OWL.STATUS_BAR.hideProgressBar();
            return;
        }

        DODDLE_OWL.getLogger().info(Translator.getTerm("ExactMatchTermCountMessage") + ": "
                + conceptSelectionPanel.getPerfectlyMatchedTermCnt());
        DODDLE_OWL.getLogger().info(Translator.getTerm("SystemAddedExactMatchTermCountMessage") + ": "
                + conceptSelectionPanel.getSystemAddedPerfectlyMatchedTermCnt());
        DODDLE_OWL.getLogger().info(Translator.getTerm("PartialMatchTermCountMessage") + ": "
                + conceptSelectionPanel.getPartiallyMatchedTermCnt());
        DODDLE_OWL.getLogger().info(Translator.getTerm("InputTermCountMessage") + ": "
                + (conceptSelectionPanel.getMatchedTermCnt()));
        DODDLE_OWL.getLogger().info(Translator.getTerm("InputConceptCountMessage") + ": " + inputConceptSet.size());
        DODDLE_OWL.STATUS_BAR.addValue();
        project.initUserIDCount();

        DODDLE_OWL.STATUS_BAR.addValue();
        if (project.getConceptSelectionPanel().isConstructNounAndVerbTree()) {
            Set<Concept> inputVerbConceptSet = new HashSet<>();
            if (project.getOntologySelectionPanel().isEDREnable()) {
                inputVerbConceptSet.addAll(EDR.getVerbConceptSet(inputConceptSet)); // EDRにおける動詞的概念の抽出
            }
            inputVerbConceptSet.addAll(OWLOntologyManager.getVerbConceptSet(inputConceptSet)); // OWLオントロジー中のプロパティセット
            Set<Concept> inputNounConceptSet = new HashSet<>(inputConceptSet);
            inputNounConceptSet.removeAll(inputVerbConceptSet);

            DODDLE_OWL.getLogger().info(Translator.getTerm("InputNounConceptCountMessage") + ": "
                    + inputNounConceptSet.size());
            DODDLE_OWL.STATUS_BAR.addValue();
            constructClassPanel.setConceptTreeModel(makeClassTreeModel(inputNounConceptSet));
            TreeModel hasaTreeModel = ConceptTreeMaker.getInstance().getDefaultConceptTreeModel(
                    new HashSet<>(), project,
                    ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI);
            constructClassPanel.setHasaTreeModel(hasaTreeModel);
            DODDLE_OWL.STATUS_BAR.addValue();
            DODDLE_OWL.getLogger().info(Translator.getTerm("InputVerbConceptCountMessage") + ": "
                    + inputVerbConceptSet.size());
            constructPropertyPanel.setConceptTreeModel(makePropertyTreeModel(inputVerbConceptSet));
            hasaTreeModel = ConceptTreeMaker.getInstance().getDefaultConceptTreeModel(
                    new HashSet<>(), project,
                    ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI);
            constructPropertyPanel.setHasaTreeModel(hasaTreeModel);
            DODDLE_OWL.STATUS_BAR.addValue();
        } else {
            Set<Concept> inputNounConceptSet = new HashSet<>(inputConceptSet);
            DODDLE_OWL.getLogger().info(Translator.getTerm("InputNounConceptCountMessage") + ": "
                    + inputNounConceptSet.size());
            constructClassPanel.setConceptTreeModel(makeClassTreeModel(inputNounConceptSet));
            TreeModel hasaTreeModel = ConceptTreeMaker.getInstance().getDefaultConceptTreeModel(
                    new HashSet<>(), project,
                    ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI);
            constructClassPanel.setHasaTreeModel(hasaTreeModel);
            DODDLE_OWL.STATUS_BAR.addValue();
            constructPropertyPanel
                    .setConceptTreeModel(makePropertyTreeModel(new HashSet<>()));
            hasaTreeModel = ConceptTreeMaker.getInstance().getDefaultConceptTreeModel(
                    new HashSet<>(), project,
                    ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI);
            constructPropertyPanel.setHasaTreeModel(hasaTreeModel);
            DODDLE_OWL.STATUS_BAR.addValue();
        }

        constructClassPanel.expandIsaTree();
        constructClassPanel.expandHasaTree();
        DODDLE_OWL.STATUS_BAR.addValue();
        constructPropertyPanel.expandIsaTree();
        constructPropertyPanel.expandHasaTree();
        DODDLE_OWL.STATUS_BAR.addValue();
        constructClassPanel.setVisibleIsaTree(true);
        constructClassPanel.setVisibleHasaTree(true);
        constructPropertyPanel.setVisibleIsaTree(true);
        constructPropertyPanel.setVisibleHasaTree(true);
        DODDLE_OWL.setSelectedIndex(DODDLEConstants.TAXONOMIC_PANEL);
        DODDLE_OWL.STATUS_BAR.printMessage(Translator.getTerm("TreeConstructionDoneMessage"));
        DODDLE_OWL.STATUS_BAR.addValue();
        DODDLE_OWL.STATUS_BAR.hideProgressBar();
    }
}
