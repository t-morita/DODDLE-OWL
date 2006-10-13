package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

/*
 * Created on 2004/02/05
 *  
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author shigeta
 * 
 * 2004-07-15 modified by takeshi morita
 * 
 */
public class ConceptDriftManagementPanel extends JPanel implements ActionListener, ListSelectionListener {

    private String conceptTreeType;
    private JList mraJList;
    private List<List<ConceptTreeNode>> mraList;
    private JList traJList;
    private JTree trimmedNodeTree;
    private List<ConceptTreeNode> traList;
    private JTabbedPane conceptDriftManagementTab;
    private JButton traButton;
    private RemoveMultipleInheritancePanel rmMultipleInheritancePanel;

    private JTree conceptTree;

    private JTextField trimmedNumField;
    private ConceptTreeMaker maker = ConceptTreeMaker.getInstance();

    private DODDLEProject project;

    public ConceptDriftManagementPanel(String type, JTree tree, DODDLEProject p) {
        project = p;
        conceptTree = tree;
        conceptTreeType = type;
        mraList = new ArrayList<List<ConceptTreeNode>>();
        mraJList = new JList();
        mraJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mraJList.addListSelectionListener(this);
        traList = new ArrayList<ConceptTreeNode>();
        traJList = new JList();
        traJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        traJList.addListSelectionListener(this);
        trimmedNodeTree = new JTree();
        trimmedNodeTree.setCellRenderer(new ConceptTreeCellRenderer(type));
        trimmedNodeTree.setModel(new DefaultTreeModel(null));
        trimmedNodeTree.setEditable(false);
        traButton = new JButton(Translator.getString("ConceptTreePanel.TrimmedResultAnalysis"));
        traButton.addActionListener(this);
        trimmedNumField = new JTextField(5);
        trimmedNumField.setText("3");

        conceptDriftManagementTab = new JTabbedPane();
        conceptDriftManagementTab.add(setMRAPanel(), Translator
                .getString("ConceptTreePanel.MatchedResultAnalysisResult"));
        conceptDriftManagementTab.add(setTRAPanel(), Translator
                .getString("ConceptTreePanel.TrimmedResultAnalysisResult"));
        rmMultipleInheritancePanel = new RemoveMultipleInheritancePanel();
        conceptDriftManagementTab.add(rmMultipleInheritancePanel, "多重継承の除去");
        setLayout(new BorderLayout());
        add(conceptDriftManagementTab);

        this.setBorder(BorderFactory
                .createTitledBorder(Translator.getString("ConceptTreePanel.ConceptDriftManagement")));
    }
    public List<ConceptTreeNode> getTRAResult() {
        return traList;
    }

    public void addTRANode(ConceptTreeNode node) {
        traList.add(node);

    }
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == traJList) {
            traAction();
        } else if (e.getSource() == mraJList) {
            mraAction();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == traButton) {
            int trimmedNum = 3;
            try {
                trimmedNum = Integer.parseInt(trimmedNumField.getText());
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            maker.resetTRA();
            maker.trimmedResultAnalysis((ConceptTreeNode) conceptTree.getModel().getRoot(), trimmedNum);
            traList = new ArrayList<ConceptTreeNode>(maker.getTRAresult());
            setTRADefaultValue();
        }
    }

    /**
     * 各言語のルート概念にあたるラベルを比較する必要がある
     * 
     * @return
     */
    private boolean isClass() {
        String rootLabel = conceptTree.getModel().getRoot().toString();
        return rootLabel.equals("名詞的概念") || rootLabel.equals("Root Class");
    }

    /**
     * 
     * リストで選択されているTRAで分析された箇所の選択，グループ化を行う
     * 
     */
    private void traAction() {
        if (traList != null && !traList.isEmpty()) {
            int index = traJList.getSelectedIndex();
            if (index == -1) { return; }
            ConceptTreeNode traNode = traList.get(index);
            if (DODDLE.doddlePlugin != null) {
                List set = new ArrayList();
                set.add(traNode);
                if (traNode.getParent() != null) {
                    set.add(traNode.getParent());
                }
                if (isClass()) {
                    DODDLE.doddlePlugin.selectClasses(changeToURISet(set));
                } else {
                    DODDLE.doddlePlugin.selectProperties(changeToURISet(set));
                }
            }
            DefaultTreeModel treeModel = (DefaultTreeModel) conceptTree.getModel();
            TreeNode[] nodes = treeModel.getPathToRoot(traNode);
            TreePath path = new TreePath(nodes);
            conceptTree.scrollPathToVisible(path);
            conceptTree.setSelectionPath(path);
            traAction(traNode);
        }
    }

    public void traAction(ConceptTreeNode traNode) {
        if (traNode.getParent() == null) {
            trimmedNodeTree.setModel(new DefaultTreeModel(null));
            return;
        }
        Concept trimmedTreeRootConcept = ((ConceptTreeNode) traNode.getParent()).getConcept();
        TreeModel trimmedTreeModel = getTrimmedTreeModel(trimmedTreeRootConcept, traNode.getTrimmedConceptList());
        trimmedNodeTree.setModel(trimmedTreeModel);
        for (int i = 0; i < trimmedNodeTree.getRowCount(); i++) {
            trimmedNodeTree.expandPath(trimmedNodeTree.getPathForRow(i));
        }
    }

    private TreeModel getTrimmedTreeModel(Concept trimmedTreeRootConcept, List<List<Concept>> trimmedConceptList) {
        ConceptTreeNode rootNode = new ConceptTreeNode(trimmedTreeRootConcept, project);
        TreeModel trimmedTreeModel = new DefaultTreeModel(rootNode);
        for (List<Concept> list : trimmedConceptList) {
            DefaultMutableTreeNode parentNode = rootNode;
            for (Concept tc : list) {
                ConceptTreeNode childNode = new ConceptTreeNode(tc, project);
                parentNode.insert(childNode, 0);
                parentNode = childNode;
            }
        }
        if (rootNode.getChildCount() == 0) {
            trimmedTreeModel = new DefaultTreeModel(null);
        }
        return trimmedTreeModel;
    }

    /**
     * 
     * リストで選択されているMRAで分析された箇所の選択，グループ化を行う
     * 
     */
    private void mraAction() {
        if (mraList != null && !mraList.isEmpty()) {
            int index = mraJList.getSelectedIndex();

            if (index == -1) { return; }
            List<ConceptTreeNode> stm = mraList.get(index);
            if (DODDLE.doddlePlugin != null) {
                if (isClass()) {
                    DODDLE.doddlePlugin.selectClasses(changeToURISet(stm));
                } else {
                    DODDLE.doddlePlugin.selectProperties(changeToURISet(stm));
                }
            }
            TreePath[] paths = new TreePath[stm.size()];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = new TreePath(stm.get(i).getPath());
            }
            conceptTree.scrollPathToVisible(paths[0]);
            conceptTree.setSelectionPaths(paths);
        }
    }

    /**
     * 名前空間を付与する
     */
    private Set changeToURISet(List<ConceptTreeNode> stm) {
        Set uri = new HashSet();
        for (ConceptTreeNode node : stm) {
            Concept c = node.getConcept();
            if (c.getPrefix().equals("edr")) {
                uri.add(DODDLE.EDR_URI + node.getIdStr());
            } else if (c.getPrefix().equals("edrt")) {
                uri.add(DODDLE.EDRT_URI + node.getIdStr());
            } else if (c.getPrefix().equals("wn")) {
                uri.add(DODDLE.WN_URI + node.getIdStr());
            } else {
                uri.add(DODDLE.BASE_URI + node.getIdStr());
            }
        }
        return uri;
    }

    /**
     * matched result analysis を制御するパネルを作る
     */
    private JPanel setMRAPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(mraJList), BorderLayout.CENTER);
        return panel;
    }

    /**
     * trimmed result analysis を制御するパネルを作る
     */
    private JPanel setTRAPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel southPanel = new JPanel();
        southPanel.add(new JSeparator());
        southPanel.add(trimmedNumField);
        southPanel.add(traButton);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(2, 1));
        listPanel.add(new JScrollPane(traJList));
        JScrollPane trimmedNodeJListScroll = new JScrollPane(trimmedNodeTree);
        trimmedNodeJListScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ConceptDriftManagementPanel.TrimmedNodeList")));
        listPanel.add(trimmedNodeJListScroll);
        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    /**
     * 初期値をセットする
     */
    public void setConceptDriftManagementResult() {
        mraList = new ArrayList<List<ConceptTreeNode>>(maker.getMRAresult());
        traList = new ArrayList<ConceptTreeNode>(maker.getTRAresult());
        rmMultipleInheritancePanel.setMultipleInheritanceConceptSet(maker.getMulipleInheritanceConceptSet());
        setMRADefaultValue();
        setTRADefaultValue();
        conceptDriftManagementTab
                .setTitleAt(2, "多重継承の除去" + " (" + maker.getMulipleInheritanceConceptSet().size() + ")");
    }

    private void setMRADefaultValue() {
        List list = new ArrayList();
        for (int i = 0; i < mraList.size(); i++) {
            List<ConceptTreeNode> nodeList = mraList.get(i);
            ConceptTreeNode sinNode = nodeList.get(0); // 0番目にSINノードが格納されている
            list.add(i + 1 + ": " + sinNode.getConcept());
        }
        mraJList.setListData(list.toArray());
        conceptDriftManagementTab.setTitleAt(0, Translator.getString("ConceptTreePanel.MatchedResultAnalysisResult")
                + " (" + list.size() + ")");
    }

    public void setTRADefaultValue() {
        List list = new ArrayList();
        for (int i = 0; i < traList.size(); i++) {
            ConceptTreeNode traNode = traList.get(i);
            list.add(i + 1 + ": " + traNode.getConcept() + " (" + traNode.getTrimmedCountList() + ")");
        }
        traJList.setListData(list.toArray());
        conceptDriftManagementTab.setTitleAt(1, Translator.getString("ConceptTreePanel.TrimmedResultAnalysisResult")
                + " (" + traList.size() + ")");
    }

    class RemoveMultipleInheritancePanel extends JPanel implements ListSelectionListener, ActionListener {

        private JList multipleInheritanceConceptJList;
        private JList multipleInheritanceUpperConceptJList;
        private JButton removeUpperConceptLinkButton;

        public RemoveMultipleInheritancePanel() {
            multipleInheritanceConceptJList = new JList();
            multipleInheritanceConceptJList.addListSelectionListener(this);
            JScrollPane multipleInheritanceConceptJListScroll = new JScrollPane(multipleInheritanceConceptJList);
            multipleInheritanceConceptJListScroll.setBorder(BorderFactory.createTitledBorder("多重継承の概念リスト"));
            multipleInheritanceUpperConceptJList = new JList();
            multipleInheritanceUpperConceptJList.addListSelectionListener(this);
            JScrollPane multipleInheritanceUpperConceptJListScroll = new JScrollPane(
                    multipleInheritanceUpperConceptJList);
            multipleInheritanceUpperConceptJListScroll.setBorder(BorderFactory.createTitledBorder("上位概念リスト"));
            removeUpperConceptLinkButton = new JButton("上位概念へのリンクを削除");
            removeUpperConceptLinkButton.addActionListener(this);

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new GridLayout(2, 1));
            listPanel.add(multipleInheritanceConceptJListScroll);
            listPanel.add(multipleInheritanceUpperConceptJListScroll);

            JPanel eastPanel = new JPanel();
            eastPanel.setLayout(new BorderLayout());
            eastPanel.add(removeUpperConceptLinkButton, BorderLayout.EAST);

            setLayout(new BorderLayout());
            add(listPanel, BorderLayout.CENTER);
            add(eastPanel, BorderLayout.SOUTH);
        }

        public void setMultipleInheritanceConceptSet(Set<Concept> multipleInheritanceConceptSet) {
            DefaultListModel listModel = new DefaultListModel();
            for (Concept c : multipleInheritanceConceptSet) {
                listModel.addElement(c);
            }
            multipleInheritanceConceptJList.setModel(listModel);
        }

        public void valueChanged(ListSelectionEvent e) {
            ConstructConceptTreePanel conceptTreePanel = getConceptTreePanel();
            DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTreeModel();
            Concept c = (Concept) multipleInheritanceConceptJList.getSelectedValue();
            if (e.getSource() == multipleInheritanceConceptJList) {
                if (c != null) {
                    Set<ConceptTreeNode> sameConceptTreeNodeSet = getSameConceptTreeNodeSet(conceptTreePanel, model, c);
                    DefaultListModel listModel = new DefaultListModel();
                    Set<Concept> sameUpperConceptSet = new HashSet<Concept>();
                    for (ConceptTreeNode node : sameConceptTreeNodeSet) {
                        ConceptTreeNode upperConcept = (ConceptTreeNode) node.getParent();
                        if (!sameUpperConceptSet.contains(upperConcept.getConcept())) {
                            listModel.addElement(upperConcept);
                            sameUpperConceptSet.add(upperConcept.getConcept());
                        }
                    }
                    multipleInheritanceUpperConceptJList.setModel(listModel);
                }
            } else if (e.getSource() == multipleInheritanceUpperConceptJList) {
                ConceptTreeNode upperTreeNode = (ConceptTreeNode) multipleInheritanceUpperConceptJList
                        .getSelectedValue();
                if (upperTreeNode != null) {
                    Set<ConceptTreeNode> multipleInheritanceNodeSet = getSameConceptTreeNodeSet(conceptTreePanel,
                            model, c);
                    for (ConceptTreeNode multipleInheritanceNode : multipleInheritanceNodeSet) {
                        if (multipleInheritanceNode.getParent().equals(upperTreeNode)) {
                            TreeNode[] nodes = model.getPathToRoot(multipleInheritanceNode);
                            TreePath path = new TreePath(nodes);
                            conceptTree.scrollPathToVisible(path);
                            conceptTree.setSelectionPath(path);
                            if (DODDLE.doddlePlugin != null) {
                                List set = new ArrayList();
                                set.add(multipleInheritanceNode);
                                set.add(upperTreeNode);
                                if (isClass()) {
                                    DODDLE.doddlePlugin.selectClasses(changeToURISet(set));
                                } else {
                                    DODDLE.doddlePlugin.selectProperties(changeToURISet(set));
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        /**
         * @param conceptTreePanel
         * @param model
         * @param c
         * @return
         */
        private Set<ConceptTreeNode> getSameConceptTreeNodeSet(ConstructConceptTreePanel conceptTreePanel,
                DefaultTreeModel model, Concept c) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            Set<ConceptTreeNode> sameConceptSet = new HashSet<ConceptTreeNode>();
            conceptTreePanel.searchSameConceptTreeNode(c, rootNode, sameConceptSet);
            return sameConceptSet;
        }

        public void actionPerformed(ActionEvent e) {
            Concept c = (Concept) multipleInheritanceConceptJList.getSelectedValue();
            if (e.getSource() == removeUpperConceptLinkButton) {
                ConceptTreeNode upperConceptTreeNode = (ConceptTreeNode) multipleInheritanceUpperConceptJList
                        .getSelectedValue();
                ConstructConceptTreePanel conceptTreePanel = getConceptTreePanel();
                DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTreeModel();
                Set<ConceptTreeNode> sameConceptTreeNodeSet = getSameConceptTreeNodeSet(conceptTreePanel, model, c);
                for (ConceptTreeNode removeNode : sameConceptTreeNodeSet) {
                    if (removeNode.getParent().equals(upperConceptTreeNode)) {
                        conceptTreePanel.deleteLinkToUpperConcept(removeNode);
                        DefaultListModel listModel = (DefaultListModel) multipleInheritanceUpperConceptJList.getModel();
                        multipleInheritanceUpperConceptJList.clearSelection();
                        listModel.removeElement(upperConceptTreeNode);
                        break;
                    }
                }
            }
            if (multipleInheritanceUpperConceptJList.getModel().getSize() == 1) {
                DefaultListModel listModel = (DefaultListModel) multipleInheritanceConceptJList.getModel();
                multipleInheritanceConceptJList.clearSelection();
                listModel.removeElement(c);
                multipleInheritanceUpperConceptJList.setModel(new DefaultListModel());
                conceptDriftManagementTab.setTitleAt(2, "多重継承の除去" + " (" + listModel.size() + ")");
            }
        }

        /**
         * @return
         */
        private ConstructConceptTreePanel getConceptTreePanel() {
            ConstructConceptTreePanel conceptTreePanel = null;
            if (conceptTreeType.equals(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE)) {
                conceptTreePanel = project.getConstructClassPanel();
            } else if (conceptTreeType.equals(ConceptTreeCellRenderer.VERB_CONCEPT_TREE)) {
                conceptTreePanel = project.getConstructPropertyPanel();
            }
            return conceptTreePanel;
        }
    }

}