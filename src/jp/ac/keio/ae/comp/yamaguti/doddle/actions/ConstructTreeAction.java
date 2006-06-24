/*
 * @(#)  2006/04/07
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 */
public class ConstructTreeAction {

    private Map<String, Set<Concept>> wordCorrespondConceptSetMap; // 入力単語と適切に対応するIDのマッピング
    private Map<DefaultMutableTreeNode, String> abstractNodeLabelMap;
    private Map<InputWordModel, ConstructTreeOption> complexConstructTreeOptionMap;
    private DisambiguationPanel disambiguationPanel;
    private ConstructClassPanel constructClassPanel;
    private ConstructPropertyPanel constructPropertyPanel;

    private DefaultListModel undefinedWordListModel;
    private UndefinedWordListPanel undefinedWordListPanel;

    private DODDLEProject project;

    public ConstructTreeAction(boolean isNounAndVerbTree, DODDLEProject p) {
        project = p;
        disambiguationPanel = p.getDisambiguationPanel();
        constructClassPanel = p.getConstructClassPanel();
        constructPropertyPanel = p.getConstructPropertyPanel();
        wordCorrespondConceptSetMap = disambiguationPanel.getWordCorrespondConceptSetMap();
        undefinedWordListPanel = disambiguationPanel.getUndefinedWordListPanel();
        complexConstructTreeOptionMap = disambiguationPanel.getComplexConstructTreeOptionMap();
        if (isNounAndVerbTree) {
            OptionDialog.setNounAndVerbConceptHiearchy();
        } else {
            OptionDialog.setNounConceptHiearchy();
        }
    }

    public void constructTree() {

        new Thread() {

            private boolean isExistNode(TreeNode node, TreeNode childNode, String word, Concept ic) {
                return childNode.toString().equals(word)
                        || (node.getParent() == null && childNode.toString().equals(ic.getIdentity()));
            }

            private boolean isEnglish(String iw) {
                return iw.matches("(\\w|\\s)*");
            }

            private void addComplexWordNode(int len, InputWordModel iwModel, TreeNode node) {
                if (len == iwModel.getComplexWordLength()) { return; }
                List wordList = iwModel.getWordList();
                StringBuilder buf = new StringBuilder();
                boolean isEnglish = isEnglish(iwModel.getWord());
                for (int i = wordList.size() - len - 1; i < wordList.size(); i++) {
                    buf.append(wordList.get(i));
                    if (isEnglish) {
                        buf.append(" ");
                    }
                }
                String word = buf.toString();
                if (isEnglish) { // スペースを除去
                    word = word.substring(0, word.length() - 1);
                }
                // wordの長さが照合単語以上の長さのときに複合語の階層化を行う
                if (iwModel.getMatchedWord().length() <= word.length()) {
                    Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(iwModel.getWord());
                    for (Concept ic : correspondConceptSet) {
                        for (int i = 0; i < node.getChildCount(); i++) {
                            TreeNode childNode = node.getChildAt(i);
                            if (isExistNode(node, childNode, word, ic)) {
                                addComplexWordNode(len + 1, iwModel, childNode);
                                return;
                            }
                        }
                        DefaultMutableTreeNode childNode = null;
                        if (word.equals(iwModel.getMatchedWord())) {
                            childNode = new DefaultMutableTreeNode(ic.getIdentity());
                        } else {
                            childNode = new DefaultMutableTreeNode(word);
                        }

                        ((DefaultMutableTreeNode) node).add(childNode);
                        addComplexWordNode(len + 1, iwModel, childNode);
                    }
                } else {
                    // System.out.println("照合単語: " +
                    // iwModel.getMatchedWord() + " => 短すぎる単語: " + word);
                    addComplexWordNode(len + 1, iwModel, node);
                }
            }

            private boolean hasONEComplexWordChild(TreeNode node) {
                int complexWordChildNum = 0;
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    if (childNode.getUserObject() instanceof String) {
                        complexWordChildNum += 1;
                        if (1 < complexWordChildNum) { return false; }
                    }
                }
                return complexWordChildNum <= 1;
            }

            private void trimComplexWordNode(DefaultMutableTreeNode node) {
                Set<DefaultMutableTreeNode> addNodeSet = new HashSet<DefaultMutableTreeNode>();
                Set<DefaultMutableTreeNode> removeNodeSet = new HashSet<DefaultMutableTreeNode>();
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    trimComplexWord(node, childNode, addNodeSet, removeNodeSet);
                }
                for (DefaultMutableTreeNode anode : addNodeSet) {
                    node.add(anode);
                }
                for (DefaultMutableTreeNode rnode : removeNodeSet) {
                    node.remove(rnode);
                }
                if (0 < addNodeSet.size()) {
                    trimComplexWordNode(node);
                }
                // 兄弟概念をすべて処理した後に，子ノードの処理に移る
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    trimComplexWordNode(childNode);
                }
            }

            private void trimAbstractNode(DefaultMutableTreeNode node) {
                Set<String> sameNodeSet = new HashSet<String>();
                Set<DefaultMutableTreeNode> addNodeSet = new HashSet<DefaultMutableTreeNode>();
                Set<DefaultMutableTreeNode> removeNodeSet = new HashSet<DefaultMutableTreeNode>();
                Set<String> reconstructNodeSet = new HashSet<String>();
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    extractMoveComplexWordNodeSet(sameNodeSet, addNodeSet, removeNodeSet, childNode);
                    extractReconstructedNodeSet(reconstructNodeSet, childNode);
                }
                moveComplexWordNodeSet(node, addNodeSet, removeNodeSet, reconstructNodeSet);
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
            private void moveComplexWordNodeSet(DefaultMutableTreeNode node, Set<DefaultMutableTreeNode> addNodeSet,
                    Set<DefaultMutableTreeNode> removeNodeSet, Set<String> reconstructNodeSet) {
                // 子ノードを一つしかもたない抽象ノードの子ノードをnodeに追加
                for (DefaultMutableTreeNode addNode : addNodeSet) {
                    node.add(addNode);
                }
                // 子ノードを一つしかもたない抽象ノードを削除
                for (DefaultMutableTreeNode removeNode : removeNodeSet) {
                    node.remove(removeNode);
                    abstractNodeLabelMap.remove(removeNode);
                }
                Set<DefaultMutableTreeNode> duplicatedNodeSet = new HashSet<DefaultMutableTreeNode>();
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
            private void extractReconstructedNodeSet(Set<String> reconstructNodeSet, DefaultMutableTreeNode childNode) {
                // ２つ以上子ノード(複合語)を持つ抽象中間ノードに追加されたノードをreconstructNodeSetに保存
                if (childNode.getUserObject() instanceof Concept && !hasONEComplexWordChild(childNode)) {
                    for (int i = 0; i < childNode.getChildCount(); i++) {
                        DefaultMutableTreeNode reconstructNode = (DefaultMutableTreeNode) childNode.getChildAt(i);
                        reconstructNodeSet.add(reconstructNode.toString());
                    }
                }
            }

            /**
             * @param addNodeSet
             * @param removeNodeSet
             * @param childNode
             */
            private void extractMoveComplexWordNodeSet(Set<String> sameNodeSet, Set<DefaultMutableTreeNode> addNodeSet,
                    Set<DefaultMutableTreeNode> removeNodeSet, DefaultMutableTreeNode childNode) {
                if (childNode.getUserObject() instanceof Concept && hasONEComplexWordChild(childNode)) {
                    DefaultMutableTreeNode grandChildNode = (DefaultMutableTreeNode) childNode.getChildAt(0);
                    if (grandChildNode.getUserObject() instanceof String) {
                        // System.out.println("rm: " + childNode);
                        removeNodeSet.add(childNode);
                        abstractNodeLabelMap.remove(childNode);
                        if (!sameNodeSet.contains(grandChildNode.toString())) {
                            sameNodeSet.add(grandChildNode.toString());
                            DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(grandChildNode.toString());
                            deepCloneTreeNode(grandChildNode, addNode);
                            addNodeSet.add(addNode);
                        }
                    }
                }
            }

            private void trimComplexWord(DefaultMutableTreeNode node, DefaultMutableTreeNode childNode, Set addNodeSet,
                    Set removeNodeSet) {
                if (childNode.getUserObject() instanceof String && !complexWordSet.contains(childNode.toString())) {
                    for (int i = 0; i < childNode.getChildCount(); i++) {
                        addNodeSet.add((DefaultMutableTreeNode) childNode.getChildAt(i));
                    }
                    removeNodeSet.add(childNode);
                }
            }

            private Set<String> complexWordSet; // 入力語彙に含まれない複合語を削除するさいに参照

            private boolean isInputConcept(Concept c, Set<Concept> conceptSet) {
                for (Concept ic : conceptSet) {
                    if (ic.getIdentity().equals(c.getIdentity())) { return true; }
                }
                return false;
            }

            private void setComplexConcept(ComplexConceptTreeInterface ccTreeInterface, Set<Concept> conceptSet) {
                complexWordSet = new HashSet<String>();
                Map<String, String> matchedWordIDMap = new HashMap<String, String>();
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
                for (InputWordModel iwModel : complexConstructTreeOptionMap.keySet()) {
                    if (iwModel == null) {
                        continue;
                    }
                    complexWordSet.add(iwModel.getWord());
                    complexWordSet.add(iwModel.getMatchedWord());
                    ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                    matchedWordIDMap.put(iwModel.getMatchedWord(), ctOption.getConcept().getId());
                    if (!isInputConcept(ctOption.getConcept(), conceptSet)) {
                        continue;
                    }
                    if (ctOption.getOption().equals("SAME")) {
                        ccTreeInterface.addJPWord(ctOption.getConcept().getIdentity(), iwModel.getWord());
                    } else if (ctOption.getOption().equals("SUB")) {
                        addComplexWordNode(0, iwModel, rootNode);
                    }
                }
                DODDLE.getLogger().log(Level.DEBUG, "複合語階層構築");

                // printDebugTree(rootNode, "before trimming");
                if (OptionDialog.isTrimNodeWithComplexWordConceptConstruction()) {
                    // childNodeは汎用オントロジー中の概念と対応しているため処理しない
                    for (int i = 0; i < rootNode.getChildCount(); i++) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                        trimComplexWordNode(childNode);
                    }
                    DODDLE.getLogger().log(Level.DEBUG, "複合語の剪定");
                }

                if (OptionDialog.isAddAbstractInternalComplexWordConcept()) {
                    addAbstractTreeNode(rootNode);
                    trimAbstractNode(rootNode);
                    trimLeafAbstractNode();
                    DODDLE.getLogger().log(Level.DEBUG, "抽象中間概念を追加");
                }

                // printDebugTree(rootNode, "add abstract node");
                complexWordSet.clear();
                ccTreeInterface.addComplexWordConcept(matchedWordIDMap, abstractNodeLabelMap, rootNode);
            }

            /**
             * 
             */
            private void trimLeafAbstractNode() {
                Set<DefaultMutableTreeNode> leafAbstractNodeSet = new HashSet<DefaultMutableTreeNode>();
                for (Iterator i = abstractNodeLabelMap.keySet().iterator(); i.hasNext();) {
                    DefaultMutableTreeNode anode = (DefaultMutableTreeNode) i.next();
                    if (anode.getChildCount() < 2) {
                        leafAbstractNodeSet.add(anode);
                    }
                }
                for (DefaultMutableTreeNode leafAbstractNode : leafAbstractNodeSet) {
                    abstractNodeLabelMap.remove(leafAbstractNode);
                }
            }

            Set conceptStrSet;

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
                abstractNodeLabelMap = new HashMap<DefaultMutableTreeNode, String>();
                tmpcnt = 0;
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    DODDLE.getLogger().log(Level.DEBUG,
                            rootNode.getChildAt(i) + ": " + (i + 1) + "/" + rootNode.getChildCount());
                    reconstructComplexTree(1, (DefaultMutableTreeNode) rootNode.getChildAt(i));
                    // 多重継承している場合もあるので，一度クローンを抽象ノードに挿入した後に，
                    // 親ノードから削除する．
                    for (Iterator j = nodeRemoveNodeSetMap.entrySet().iterator(); j.hasNext();) {
                        Entry entry = (Entry) j.next();
                        DefaultMutableTreeNode supNode = (DefaultMutableTreeNode) entry.getKey();
                        Set removeNodeSet = (Set) entry.getValue();
                        for (Iterator k = removeNodeSet.iterator(); k.hasNext();) {
                            supNode.remove((DefaultMutableTreeNode) k.next());
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
             * 
             * d: デバッグ用．再帰の深さをはかるため．
             */
            private void reconstructComplexTree(int d, DefaultMutableTreeNode node) {
                if (node.getChildCount() == 0) { return; }
                // System.out.println(node + ": " + d);
                if (!(node.getUserObject() instanceof Concept)) { // 抽象ノードを上位に持つ複合語は処理しない
                    Map abstractConceptTreeNodeMap = new HashMap();
                    Set<DefaultMutableTreeNode> abstractNodeSet = new HashSet<DefaultMutableTreeNode>();
                    for (int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                        if (childNode.getUserObject() instanceof String) {
                            InputWordModel iwModel = disambiguationPanel.makeInputWordModel(childNode.toString());
                            Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(iwModel.getTopWord());
                            if (correspondConceptSet != null) {
                                for (Concept headConcept : correspondConceptSet) {
                                    tmpcnt++;
                                    if (headConcept != null && 1 < iwModel.getWordList().size()) {
                                        Set<Concept> supConceptSet = getSupConceptSet(headConcept.getId());
                                        for (Concept supConcept : supConceptSet) {
                                            DefaultMutableTreeNode abstractNode = getAbstractNode(node,
                                                    abstractConceptTreeNodeMap, childNode, supConcept, iwModel);
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
                    reconstructComplexTree(++d, childNode);
                }
            }

            /**
             * @param iwModel
             * @param headConcept
             * @param j
             * @param headWordBuf
             */
            private void printHeadWordDebug(InputWordModel iwModel, Concept headConcept, int j,
                    StringBuilder headWordBuf) {
                /*
                 * for (int j = 0; j < iwModel.getMatchedPoint(); j++) { List<String>
                 * headWordList = iwModel.getWordList().subList(j,
                 * iwModel.getMatchedPoint()); StringBuilder headWordBuf = new
                 * StringBuilder(); for (String w : headWordList) {
                 * headWordBuf.append(w); } headConcept =
                 * wordConceptMap.get(headWordBuf.toString()); if (headConcept !=
                 * null) { printHeadWordDebug(iwModel, headConcept, j,
                 * headWordBuf); break; } }
                 */
                if (j < iwModel.getMatchedPoint() - 1) {
                    System.out.println("********************************");
                    System.out.println("word: " + iwModel.getWord());
                    System.out.println("matchedword: " + iwModel.getMatchedWord());
                    System.out.println("head word: " + headWordBuf);
                    System.out.println("head concept :" + headConcept);
                }
            }

            private Set getSupConceptSet(String id) {
                Set supConceptSet = null;
                supConceptSet = constructClassPanel.getSupConceptSet(id);
                supConceptSet.addAll(constructPropertyPanel.getSupConceptSet(id));
                return supConceptSet;
            }

            /**
             * @param node
             * @param abstractConceptTreeNodeMap
             * @param childNode
             * @param supConcept
             */
            private DefaultMutableTreeNode getAbstractNode(DefaultMutableTreeNode node, Map abstractConceptTreeNodeMap,
                    DefaultMutableTreeNode childNode, Concept supConcept, InputWordModel iwModel) {
                DefaultMutableTreeNode abstractNode = getAbstractNode(abstractConceptTreeNodeMap, supConcept, iwModel);
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
                Set removeNodeSet = null;
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
                    DefaultMutableTreeNode childNodeClone = new DefaultMutableTreeNode(childNode.getUserObject());
                    insertNode.add(childNodeClone);
                    deepCloneTreeNode(childNode, childNodeClone);
                }
            }

            /**
             * @param node
             * @param abstractConceptTreeNodeMap
             * @param supConcept
             * @return
             */
            private DefaultMutableTreeNode getAbstractNode(Map abstractConceptTreeNodeMap, Concept supConcept,
                    InputWordModel iwModel) {
                DefaultMutableTreeNode abstractNode = null;
                if (abstractConceptTreeNodeMap.get(supConcept) != null) {
                    abstractNode = (DefaultMutableTreeNode) abstractConceptTreeNodeMap.get(supConcept);
                } else {
                    abstractNode = new DefaultMutableTreeNode(supConcept);
                    String abstractNodeLabel = supConcept.getWord() + iwModel.getWordWithoutTopWord();
                    abstractNodeLabel = abstractNodeLabel.replaceAll("\\s*", "");
                    abstractNodeLabelMap.put(abstractNode, abstractNodeLabel);
                    abstractConceptTreeNodeMap.put(supConcept, abstractNode);
                }
                return abstractNode;
            }

            private TreeModel makeClassTreeModel(Set<Concept> nounConceptSet) {
                constructClassPanel.init();
                TreeModel classTreeModel = constructClassPanel.getTreeModel(nounConceptSet);
                constructClassPanel.setTreeModel(classTreeModel);
                constructClassPanel.setUndefinedWordListModel(undefinedWordListModel);
                DODDLE.STATUS_BAR.addValue();
                if (OptionDialog.isConstructComplexWordTree()) {
                    setComplexConcept(constructClassPanel, nounConceptSet);
                }
                DODDLE.STATUS_BAR.addValue();
                return classTreeModel;
            }

            private TreeModel makePropertyTreeModel(Set<Concept> verbConceptSet) {
                constructPropertyPanel.init();
                TreeModel propertyTreeModel = constructPropertyPanel.getTreeModel(
                        constructClassPanel.getAllConceptID(), verbConceptSet);
                constructPropertyPanel.setTreeModel(propertyTreeModel);
                constructPropertyPanel.setUndefinedWordListModel(undefinedWordListModel);

                DODDLE.STATUS_BAR.addValue();
                constructPropertyPanel.removeNounNode(); // 動詞的概念階層から名詞的概念を削除
                DODDLE.STATUS_BAR.addValue();
                if (OptionDialog.isConstructComplexWordTree()) {
                    setComplexConcept(constructPropertyPanel, verbConceptSet);
                }

                return propertyTreeModel;
            }

            /*
             * 「該当なし」とされた概念を辞書載っていない単語リストに追加
             */
            private void setUndefinedWordSet() {
                for (Iterator i = wordCorrespondConceptSetMap.entrySet().iterator(); i.hasNext();) {
                    Entry entry = (Entry) i.next();
                    String word = (String) entry.getKey();
                    Set<Concept> cset = (Set<Concept>) entry.getValue();
                    if (cset.size() == 1 && cset.contains(DisambiguationPanel.nullConcept)) {
                        undefinedWordListModel.addElement(word);
                    }
                }
            }

            public void run() {
                constructClassPanel.setVisibleConceptTree(false);
                constructPropertyPanel.setVisibleConceptTree(false);
                DODDLE.STATUS_BAR.setLastMessage("階層構築完了");
                DODDLE.STATUS_BAR.initNormal(9);
                DODDLE.STATUS_BAR.startTime();
                project.resetIDConceptMap();

                undefinedWordListModel = new DefaultListModel();
                setUndefinedWordSet();
                Set<Concept> inputConceptSet = disambiguationPanel.setInputConceptSet(); // 入力概念のセット

                DODDLE.getLogger().log(Level.INFO, "完全照合 単語数: " + disambiguationPanel.getPerfectMatchedWordCnt());
                DODDLE.getLogger().log(Level.INFO, "部分照合 単語数: " + disambiguationPanel.getPartialMatchedWordCnt());
                DODDLE.getLogger().log(Level.INFO, "入力語彙数: " + (disambiguationPanel.getMatchedWordCnt()));
                DODDLE.getLogger().log(Level.INFO, "入力概念数: " + inputConceptSet.size());
                DODDLE.STATUS_BAR.addValue();
                project.initUserIDCount();

                for (int i = 0; i < undefinedWordListPanel.getModel().getSize(); i++) {
                    undefinedWordListModel.addElement(undefinedWordListPanel.getModel().getElementAt(i));
                }

                DODDLE.STATUS_BAR.addValue();
                if (OptionDialog.isNounAndVerbConceptHierarchyConstructionMode()) {
                    ConceptDefinition conceptDefinition = ConceptDefinition.getInstance();
                    Set<Concept> inputVerbConceptSet = conceptDefinition.getVerbConceptSet(inputConceptSet);
                    Set<Concept> inputNounConceptSet = new HashSet<Concept>(inputConceptSet);
                    inputNounConceptSet.removeAll(inputVerbConceptSet);

                    DODDLE.getLogger().log(Level.INFO, "入力名詞的概念数: " + inputNounConceptSet.size());
                    DODDLE.STATUS_BAR.addValue();
                    constructClassPanel.setTreeModel(makeClassTreeModel(inputNounConceptSet));
                    DODDLE.STATUS_BAR.addValue();
                    DODDLE.getLogger().log(Level.INFO, "入力動詞的概念数: " + inputVerbConceptSet.size());
                    constructPropertyPanel.setTreeModel(makePropertyTreeModel(inputVerbConceptSet));
                    DODDLE.STATUS_BAR.addValue();
                } else {
                    Set<Concept> inputNounConceptSet = new HashSet<Concept>(inputConceptSet);
                    DODDLE.getLogger().log(Level.INFO, "入力名詞的概念数: " + inputNounConceptSet.size());
                    constructClassPanel.setTreeModel(makeClassTreeModel(inputNounConceptSet));
                    DODDLE.STATUS_BAR.addValue();
                    constructPropertyPanel.setTreeModel(makePropertyTreeModel(new HashSet<Concept>()));
                    DODDLE.STATUS_BAR.addValue();
                }

                constructClassPanel.expandTree();
                DODDLE.STATUS_BAR.addValue();
                constructPropertyPanel.expandTree();
                DODDLE.STATUS_BAR.addValue();
                constructClassPanel.setVisibleConceptTree(true);
                constructPropertyPanel.setVisibleConceptTree(true);
                DODDLE.setSelectedIndex(DODDLE.TAXONOMIC_PANEL);
                DODDLE.STATUS_BAR.addValue();
                DODDLE.STATUS_BAR.hideProgressBar();
            }
        }.start();
    }
}
