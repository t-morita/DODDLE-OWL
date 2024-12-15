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

package io.github.doddle_owl.views.concept_tree;

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.concept_tree.ConceptTreeCellRenderer;
import io.github.doddle_owl.models.concept_tree.ConceptTreeNode;
import io.github.doddle_owl.models.ontology_api.ReferenceOntology;
import io.github.doddle_owl.utils.ConceptTreeMaker;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.DODDLEProjectPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author shigeta
 * @author Takeshi Morita
 */
public class ConceptDriftManagementPanel extends JPanel implements ActionListener, ListSelectionListener {

    private final String conceptTreeType;
    private final JList mraJList;
    private final JButton checkMRAButton;
    private List<List<ConceptTreeNode>> mraList;
    private final JList traJList;
    private final JTree trimmedNodeTree;
    private List<ConceptTreeNode> traList;
    private final JButton traButton;
    private final JButton checkTRAButton;
    private final RemoveMultipleInheritancePanel rmMultipleInheritancePanel;

    private final JTree conceptTree;

    private final JTextField trimmedNumField;
    private final ConceptTreeMaker maker = ConceptTreeMaker.getInstance();

    private final DODDLEProjectPanel project;

    public void initialize() {
        System.out.println("initialize concept drift");
        resetMatchedResultAnalysis();
        resetTrimmedResultAnalysis();
        rmMultipleInheritancePanel.initialize();
    }

    public ConceptDriftManagementPanel(String type, JTree tree, DODDLEProjectPanel p) {
        project = p;
        conceptTree = tree;
        conceptTreeType = type;
        mraList = new ArrayList<>();
        mraJList = new JList();
        mraJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mraJList.addListSelectionListener(this);
        checkMRAButton = new JButton(Translator.getTerm("CheckMRAButton"));
        checkMRAButton.addActionListener(this);
        traList = new ArrayList<>();
        traJList = new JList();
        traJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        traJList.addListSelectionListener(this);
        trimmedNodeTree = new JTree();
        trimmedNodeTree.setCellRenderer(new ConceptTreeCellRenderer(type));
        trimmedNodeTree.setModel(new DefaultTreeModel(null));
        trimmedNodeTree.setEditable(false);
        traButton = new JButton(Translator.getTerm("TrimmedResultAnalysisButton"));
        traButton.addActionListener(this);
        checkTRAButton = new JButton(Translator.getTerm("CheckTRAButton"));
        checkTRAButton.addActionListener(this);
        trimmedNumField = new JTextField(5);
        trimmedNumField.setText("3");

        rmMultipleInheritancePanel = new RemoveMultipleInheritancePanel();
        var mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab(Translator.getTerm("MatchedResultAnalysisResultBorder"), null, getMRAPanel());
        mainTabbedPane.addTab(Translator.getTerm("TrimmedResultAnalysisResultBorder"), null, getTRAPanel());
        mainTabbedPane.addTab(Translator.getTerm("RemoveMultipleInheritanceBorder"), null, rmMultipleInheritancePanel);

        setLayout(new BorderLayout());
        add(mainTabbedPane, BorderLayout.CENTER);
    }

    private List<ConceptTreeNode> getTRAResult() {
        return traList;
    }

    private void addTRANode(ConceptTreeNode node) {
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
            resetTrimmedResultAnalysis();
        } else if (e.getSource() == checkMRAButton) {
            if (mraJList.getSelectedIndex() < 0) {
                return;
            }
            List<ConceptTreeNode> nodeList = mraList.get(mraJList.getSelectedIndex());
            for (ConceptTreeNode node : nodeList) {
                node.setIsUserConcept(true);
                DODDLE_OWL.getCurrentProject().getConceptSelectionPanel()
                        .addInputConcept(node.getConcept());
                DODDLE_OWL.getCurrentProject().getConceptSelectionPanel()
                        .deleteSystemAddedConcept(node.getConcept());
            }
            resetMatchedResultAnalysis();
        } else if (e.getSource() == checkTRAButton) {
            if (traJList.getSelectedIndex() < 0) {
                return;
            }
            ConceptTreeNode node = traList.get(traJList.getSelectedIndex());
            node.initTrimmedConceptList();
            resetTrimmedResultAnalysis();
        }
    }

    public void resetConceptDriftManagementResult(ConceptTreeNode rootNode) {
        resetMultipleInheritanceConceptSet(rootNode);
        resetTrimmedResultAnalysis();
        resetMatchedResultAnalysis();
    }

    public void resetMatchedResultAnalysis() {
        maker.resetMRA();
        maker.matchedResultAnalysis((ConceptTreeNode) conceptTree.getModel().getRoot());
        mraList = new ArrayList<>(maker.getMRAresult());
        setMRADefaultValue();
    }

    private void resetTrimmedResultAnalysis() {
        int trimmedNum;
        try {
            trimmedNum = Integer.parseInt(trimmedNumField.getText());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, nfe.getMessage(), "Number Format Exception",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        maker.resetTRA();
        maker.trimmedResultAnalysis((ConceptTreeNode) conceptTree.getModel().getRoot(), trimmedNum);
        traList = new ArrayList<>(maker.getTRAresult());
        setTRADefaultValue();
    }

    private void resetMultipleInheritanceConceptSet(ConceptTreeNode rootNode) {
        rmMultipleInheritancePanel.setMultipleInheritanceConceptSet(maker
                .getMulipleInheritanceConceptSet(rootNode));
        rmMultipleInheritancePanel.resetMutipleInheritanceConceptJListTitle();
    }

    /**
     * 各言語のルート概念にあたるラベルを比較する必要がある
     *
     * @return
     */
    private boolean isClass() {
        String rootLabel = conceptTree.getModel().getRoot().toString();
        return rootLabel.equals("名詞的概念") || rootLabel.equals("Root Class")
                || rootLabel.equals("名詞的概念 (Is-a)") || rootLabel.equals("Is-a Root Class");
    }

    /**
     * リストで選択されているTRAで分析された箇所の選択，グループ化を行う
     */
    private void traAction() {
        if (traList != null && !traList.isEmpty()) {
            int index = traJList.getSelectedIndex();
            if (index == -1) {
                return;
            }
            ConceptTreeNode traNode = traList.get(index);
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
        TreeModel trimmedTreeModel = getTrimmedTreeModel(trimmedTreeRootConcept,
                traNode.getTrimmedConceptList());
        trimmedNodeTree.setModel(trimmedTreeModel);
        for (int i = 0; i < trimmedNodeTree.getRowCount(); i++) {
            trimmedNodeTree.expandPath(trimmedNodeTree.getPathForRow(i));
        }
    }

    private TreeModel getTrimmedTreeModel(Concept trimmedTreeRootConcept,
                                          List<List<Concept>> trimmedConceptList) {
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
     * リストで選択されているMRAで分析された箇所の選択，グループ化を行う
     */
    private void mraAction() {
        if (mraList != null && !mraList.isEmpty()) {
            int index = mraJList.getSelectedIndex();

            if (index == -1) {
                return;
            }
            List<ConceptTreeNode> stm = mraList.get(index);
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
    private Set<String> changeToURISet(List<ConceptTreeNode> stm) {
        Set<String> uriSet = new HashSet<>();
        for (ConceptTreeNode node : stm) {
            Concept c = node.getConcept();
            uriSet.add(c.getURI());
        }
        return uriSet;
    }

    /**
     * matched result analysis を制御するパネルを作る
     */
    private JPanel getMRAPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(mraJList), BorderLayout.CENTER);
        panel.add(Utils.createEastPanel(checkMRAButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * trimmed result analysis を制御するパネルを作る
     */
    private JPanel getTRAPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel southPanel = new JPanel();
        southPanel.add(new JSeparator());
        southPanel.add(trimmedNumField);
        southPanel.add(traButton);
        southPanel.add(checkTRAButton);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(2, 1));
        listPanel.add(new JScrollPane(traJList));
        JScrollPane trimmedNodeJListScroll = new JScrollPane(trimmedNodeTree);
        trimmedNodeJListScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getTerm("TrimmedConceptList")));
        listPanel.add(trimmedNodeJListScroll);
        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(Utils.createEastPanel(southPanel), BorderLayout.SOUTH);
        return mainPanel;
    }

    /**
     * 初期値をセットする
     */
    public void setConceptDriftManagementResult() {
        mraList = new ArrayList<>(maker.getMRAresult());
        traList = new ArrayList<>(maker.getTRAresult());
        trimmedNodeTree.setModel(new DefaultTreeModel(null));
        rmMultipleInheritancePanel.setMultipleInheritanceConceptSet(maker
                .getMulipleInheritanceConceptSet());
        setMRADefaultValue();
        setTRADefaultValue();
        // TODO update
//        mainViews[2].getViewProperties().setTitle(
//                Translator.getTerm("RemoveMultipleInheritanceBorder") + " ("
//                        + maker.getMulipleInheritanceConceptSet().size() + ")");
    }

    private void setMRADefaultValue() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < mraList.size(); i++) {
            List<ConceptTreeNode> nodeList = mraList.get(i);
            ConceptTreeNode sinNode = nodeList.get(0); // 0番目にSINノードが格納されている
            list.add(i + 1 + ": " + sinNode.getConcept());
        }
        mraJList.setListData(list.toArray());
        // TODO update
//        mainViews[0].getViewProperties().setTitle(
//                Translator.getTerm("MatchedResultAnalysisResultBorder") + " (" + list.size() + ")");
    }

    private void setTRADefaultValue() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < traList.size(); i++) {
            ConceptTreeNode traNode = traList.get(i);
            list.add(i + 1 + ": " + traNode.getConcept() + " (" + traNode.getTrimmedCountList()
                    + ")");
        }
        traJList.setListData(list.toArray());
        // TODO update
//        mainViews[1].getViewProperties().setTitle(
//                Translator.getTerm("TrimmedResultAnalysisResultBorder") + " (" + traList.size()
//                        + ")");
    }

    class RemoveMultipleInheritancePanel extends JPanel implements ListSelectionListener,
            ActionListener {

        private final JList multipleInheritanceConceptJList;
        private final JList multipleInheritanceUpperConceptJList;
        private final JButton removeUpperConceptLinkButton;

        void initialize() {
            multipleInheritanceConceptJList.setListData(new ArrayList<>().toArray());
            multipleInheritanceUpperConceptJList.setListData(new ArrayList<>().toArray());
        }

        RemoveMultipleInheritancePanel() {
            multipleInheritanceConceptJList = new JList();
            multipleInheritanceConceptJList.addListSelectionListener(this);
            JScrollPane multipleInheritanceConceptJListScroll = new JScrollPane(
                    multipleInheritanceConceptJList);
            multipleInheritanceConceptJListScroll.setBorder(BorderFactory
                    .createTitledBorder(Translator.getTerm("MultipleInheritanceConceptList")));
            multipleInheritanceUpperConceptJList = new JList();
            multipleInheritanceUpperConceptJList.addListSelectionListener(this);
            JScrollPane multipleInheritanceUpperConceptJListScroll = new JScrollPane(
                    multipleInheritanceUpperConceptJList);
            multipleInheritanceUpperConceptJListScroll.setBorder(BorderFactory
                    .createTitledBorder(Translator.getTerm("UpperConceptList")));
            removeUpperConceptLinkButton = new JButton(
                    Translator.getTerm("RemoveLinkToUpperConceptButton"));
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

        void setMultipleInheritanceConceptSet(Set<Concept> multipleInheritanceConceptSet) {
            DefaultListModel listModel = new DefaultListModel();
            for (Concept c : multipleInheritanceConceptSet) {
                listModel.addElement(c);
            }
            multipleInheritanceConceptJList.setModel(listModel);
            multipleInheritanceUpperConceptJList.setModel(new DefaultListModel());
        }

        public void valueChanged(ListSelectionEvent e) {
            ConceptTreeConstructionPanel conceptTreePanel = getConceptTreePanel();
            DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTreeModel();
            Concept c = (Concept) multipleInheritanceConceptJList.getSelectedValue();
            if (e.getSource() == multipleInheritanceConceptJList) {
                if (c != null) {
                    Set<ConceptTreeNode> sameConceptTreeNodeSet = getSameConceptTreeNodeSet(
                            conceptTreePanel, model, c);
                    DefaultListModel listModel = new DefaultListModel();
                    Set<Concept> sameUpperConceptSet = new HashSet<>();
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
                    Set<ConceptTreeNode> multipleInheritanceNodeSet = getSameConceptTreeNodeSet(
                            conceptTreePanel, model, c);
                    for (ConceptTreeNode multipleInheritanceNode : multipleInheritanceNodeSet) {
                        if (multipleInheritanceNode.getParent().equals(upperTreeNode)) {
                            TreeNode[] nodes = model.getPathToRoot(multipleInheritanceNode);
                            TreePath path = new TreePath(nodes);
                            conceptTree.scrollPathToVisible(path);
                            conceptTree.setSelectionPath(path);
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
        private Set<ConceptTreeNode> getSameConceptTreeNodeSet(
                ConceptTreeConstructionPanel conceptTreePanel, DefaultTreeModel model, Concept c) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            Set<ConceptTreeNode> sameConceptTreeNodeSet = new HashSet<>();
            conceptTreePanel.searchSameConceptTreeNode(c, rootNode, sameConceptTreeNodeSet);
            return sameConceptTreeNodeSet;
        }

        public void actionPerformed(ActionEvent e) {
            Concept c = (Concept) multipleInheritanceConceptJList.getSelectedValue();
            if (e.getSource() == removeUpperConceptLinkButton) {
                ConceptTreeNode upperConceptTreeNode = (ConceptTreeNode) multipleInheritanceUpperConceptJList
                        .getSelectedValue();
                ConceptTreeConstructionPanel conceptTreePanel = getConceptTreePanel();
                DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTreeModel();
                Set<ConceptTreeNode> sameConceptTreeNodeSet = getSameConceptTreeNodeSet(
                        conceptTreePanel, model, c);
                for (ConceptTreeNode removeNode : sameConceptTreeNodeSet) {
                    if (removeNode.getParent().equals(upperConceptTreeNode)) {
                        conceptTreePanel.deleteLinkToUpperConcept(removeNode);
                        selectTargetMultipleInheritanceConcept(c);
                        break;
                    }
                }
            }
        }

        void selectTargetMultipleInheritanceConcept(Concept targetConcept) {
            ListModel listModel = multipleInheritanceConceptJList.getModel();
            for (int i = 0; i < listModel.getSize(); i++) {
                Concept c = (Concept) listModel.getElementAt(i);
                if (targetConcept.equals(c)) {
                    multipleInheritanceConceptJList.setSelectedIndex(i);
                    break;
                }
            }
        }

        void resetMutipleInheritanceConceptJListTitle() {
            DefaultListModel listModel = (DefaultListModel) multipleInheritanceConceptJList.getModel();
            // TODO update title
//            mainViews[2].getViewProperties().setTitle(Translator.getTerm("RemoveMultipleInheritanceBorder") + " (" + listModel.size() + ")");
//            rootWindow.repaint();
        }

        /**
         * @return
         */
        private ConceptTreeConstructionPanel getConceptTreePanel() {
            ConceptTreeConstructionPanel conceptTreePanel = null;
            if (conceptTreeType.equals(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE)) {
                conceptTreePanel = project.getConstructClassPanel();
            } else if (conceptTreeType.equals(ConceptTreeCellRenderer.VERB_CONCEPT_TREE)) {
                conceptTreePanel = project.getConstructPropertyPanel();
            }
            return conceptTreePanel;
        }
    }

    public void saveTrimmedResultAnalysis(File file) {
        try {
            StringBuilder builder = new StringBuilder();

            List<ConceptTreeNode> conceptTreeNodeList = getTRAResult();
            for (ConceptTreeNode traNode : conceptTreeNodeList) {
                builder.append(traNode.getConcept().getURI());
                builder.append(",");
                ConceptTreeNode parentNode = (ConceptTreeNode) traNode.getParent();
                builder.append(parentNode.getConcept().getURI());
                builder.append(",");
                List<List<Concept>> trimmedConceptList = traNode.getTrimmedConceptList();
                for (List<Concept> list : trimmedConceptList) {
                    builder.append("|");
                    for (Concept tc : list) {
                        if (tc == null) {
                            continue;
                        }
                        builder.append(tc.getURI());
                        builder.append(",");
                    }
                }
                builder.append(System.lineSeparator());
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(builder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTrimmedResultAnalysis(int projectID, Statement stmt,
                                          String trimmedAnalysisTable, String trimmedConceptListTable) {
        try {
            List<ConceptTreeNode> conceptTreeNodeList = getTRAResult();
            int conceptListID = 1;
            for (ConceptTreeNode traNode : conceptTreeNodeList) {
                ConceptTreeNode parentNode = (ConceptTreeNode) traNode.getParent();
                String sql = "INSERT INTO " + trimmedAnalysisTable
                        + " (Project_ID,Concept_List_ID,Target_Concept,Target_Parent_Concept) "
                        + "VALUES(" + projectID + "," + conceptListID + ",'"
                        + traNode.getConcept().getURI() + "','" + parentNode.getConcept().getURI()
                        + "')";
                stmt.executeUpdate(sql);

                List<List<Concept>> trimmedConceptList = traNode.getTrimmedConceptList();
                for (List<Concept> list : trimmedConceptList) {
                    for (Concept tc : list) {
                        if (tc == null) {
                            continue;
                        }
                        sql = "INSERT INTO " + trimmedConceptListTable
                                + " (Project_ID,Concept_List_ID,Concept) " + "VALUES(" + projectID
                                + ",'" + conceptListID + "','" + tc.getURI() + "')";
                        stmt.executeUpdate(sql);
                    }
                }
                conceptListID++;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void loadTrimmedResultAnalysis(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            Map<String, List<List<Concept>>> idTrimmedConceptListMap = new HashMap<>();
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] lines = line.split("\\|");
                    String[] concepts = lines[0].split(",");
                    List<List<Concept>> trimmedConceptList = new ArrayList<>();
                    for (int i = 1; i < lines.length; i++) {
                        String[] conceptStrs = lines[i].split(",");
                        List<Concept> list = new ArrayList<>();
                        for (String conceptStr : conceptStrs) {
                            list.add(ReferenceOntology.getConcept(conceptStr));
                        }
                        trimmedConceptList.add(list);
                    }
                    idTrimmedConceptListMap.put(concepts[0] + concepts[1], trimmedConceptList);
                }
            }
            TreeModel treeModel = conceptTree.getModel();
            ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
            loadTrimmedResultAnalysis(rootNode, idTrimmedConceptListMap);
            setTRADefaultValue();
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
        }
    }

    public void loadTrimmedResultAnalysis(int projectID, Statement stmt,
                                          String trimmedAnalysisTable, String trimmedConceptListTable) {
        try {
            Map<Integer, String> conceptListIDConceptMap = new HashMap<>();
            String sql = "SELECT * from " + trimmedAnalysisTable + " where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int conceptListID = rs.getInt("Concept_List_ID");
                String targetConcept = rs.getString("Target_Concept");
                String targetParentConcept = rs.getString("Target_Parent_Concept");
                conceptListIDConceptMap.put(conceptListID, targetConcept + targetParentConcept);
            }
            Map<String, List<List<Concept>>> idTrimmedConceptListMap = new HashMap<>();
            for (Entry<Integer, String> entry : conceptListIDConceptMap.entrySet()) {
                int conceptListID = entry.getKey();
                String conceptID = entry.getValue();
                List<List<Concept>> trimmedConceptList;
                if (idTrimmedConceptListMap.get(conceptID) != null) {
                    trimmedConceptList = idTrimmedConceptListMap.get(conceptID);
                } else {
                    trimmedConceptList = new ArrayList<>();
                }
                sql = "SELECT * from " + trimmedConceptListTable + " where Project_ID=" + projectID
                        + " and Concept_List_ID=" + conceptListID;
                rs = stmt.executeQuery(sql);
                List<Concept> list = new ArrayList<>();
                while (rs.next()) {
                    String concept = rs.getString("Concept");
                    list.add(ReferenceOntology.getConcept(concept));
                }
                trimmedConceptList.add(list);
                idTrimmedConceptListMap.put(conceptID, trimmedConceptList);
            }

            TreeModel treeModel = conceptTree.getModel();
            ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
            loadTrimmedResultAnalysis(rootNode, idTrimmedConceptListMap);
            setTRADefaultValue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTrimmedResultAnalysis(ConceptTreeNode node,
                                           Map<String, List<List<Concept>>> idTrimmedConceptListMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            String id = childNode.getConcept().getURI() + node.getConcept().getURI();
            List<List<Concept>> trimmedConceptList = idTrimmedConceptListMap.get(id);
            if (trimmedConceptList != null && 0 < trimmedConceptList.size()) {
                childNode.setTrimmedConceptList(trimmedConceptList);
                addTRANode(childNode);
            }
            loadTrimmedResultAnalysis(childNode, idTrimmedConceptListMap);
        }
    }
}