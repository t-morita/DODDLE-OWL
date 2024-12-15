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

package io.github.doddle_owl.views.term_selection;

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.term_selection.TermInfo;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.views.concept_selection.ConceptSelectionPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.sql.Statement;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class TermSelectionPanel extends JPanel implements ActionListener, KeyListener {

    private final JTextArea termTextArea;
    private final TermInfoTablePanel termInfoTablePanel;
    private final TermInfoTablePanel removedTermInfoTablePanel;
    private final TermsInDocumentViewer documentViewer;

    private final JButton addTermListButton;
    private final JButton deleteTableItemButton;
    private final JButton returnTableItemButton;
    private final JButton reloadDocumentAreaButton;
    private final JButton completelyDeleteTableItemButton;
    private final JButton setTermSetButton;
    private final JButton addTermSetButton;

    private final ConceptSelectionPanel conceptSelectionPanel;

    public void initialize() {
        termTextArea.setText("");
        termInfoTablePanel.initialize();
        removedTermInfoTablePanel.initialize();
        documentViewer.initialize();
    }

    public TermSelectionPanel(ConceptSelectionPanel ui) {
        termInfoTablePanel = new TermInfoTablePanel();
        termInfoTablePanel.getTable().addKeyListener(this);
        removedTermInfoTablePanel = new TermInfoTablePanel();
        removedTermInfoTablePanel.getTable().addKeyListener(this);
        documentViewer = new TermsInDocumentViewer();
        conceptSelectionPanel = ui;

        termTextArea = new JTextArea();
        var inputTermsAreaScroll = new JScrollPane(termTextArea);
        inputTermsAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("InputTermListArea")));

        addTermListButton = new JButton(Translator.getTerm("AddInputTermListButton"));
        addTermListButton.addActionListener(this);
        deleteTableItemButton = new JButton(Translator.getTerm("RemoveButton"));
        deleteTableItemButton.addActionListener(this);
        returnTableItemButton = new JButton(Translator.getTerm("ReturnButton"));
        returnTableItemButton.addActionListener(this);
        reloadDocumentAreaButton = new JButton(Translator.getTerm("ReloadButton"));
        reloadDocumentAreaButton.addActionListener(this);
        completelyDeleteTableItemButton = new JButton(Translator.getTerm("CompletelyDeleteTermButton"));
        completelyDeleteTableItemButton.addActionListener(this);

        var tableButtonPanel = new JPanel();
        tableButtonPanel.add(addTermListButton);
        tableButtonPanel.add(deleteTableItemButton);
        tableButtonPanel.add(returnTableItemButton);
        tableButtonPanel.add(reloadDocumentAreaButton);
        tableButtonPanel.add(completelyDeleteTableItemButton);

        setTermSetButton = new JButton(Translator.getTerm("SetInputTermSetButton"));
        setTermSetButton.addActionListener(this);
        addTermSetButton = new JButton(Translator.getTerm("AddInputTermSetButton"));
        addTermSetButton.addActionListener(this);
        var inputTermsButtonPanel = new JPanel();
        inputTermsButtonPanel.add(setTermSetButton);
        inputTermsButtonPanel.add(addTermSetButton);

        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(tableButtonPanel, BorderLayout.WEST);
        buttonPanel.add(inputTermsButtonPanel, BorderLayout.EAST);

        var mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addChangeListener(l -> {
            switch (mainTabbedPane.getSelectedIndex()) {
                case 0:
                    addTermListButton.setVisible(false);
                    deleteTableItemButton.setVisible(false);
                    returnTableItemButton.setVisible(false);
                    completelyDeleteTableItemButton.setVisible(false);
                    break;
                case 1:
                    addTermListButton.setVisible(true);
                    deleteTableItemButton.setVisible(true);
                    returnTableItemButton.setVisible(false);
                    completelyDeleteTableItemButton.setVisible(false);
                    reloadDocumentAreaButton.setVisible(false);
                    break;
                case 2:
                    addTermListButton.setVisible(false);
                    deleteTableItemButton.setVisible(false);
                    returnTableItemButton.setVisible(true);
                    completelyDeleteTableItemButton.setVisible(true);
                    reloadDocumentAreaButton.setVisible(false);
                    break;
            }
        });

        mainTabbedPane.addTab(Translator.getTerm("InputDocumentViewerPanel"), null, documentViewer);
        mainTabbedPane.addTab(Translator.getTerm("InputTermInfoTablePanel"), null, termInfoTablePanel);
        mainTabbedPane.addTab(Translator.getTerm("RemovedTermInfoTablePanel"), null, removedTermInfoTablePanel);

        var mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.add(mainTabbedPane);
        mainSplitPane.add(inputTermsAreaScroll);
        mainSplitPane.setDividerLocation(1000);

        setLayout(new BorderLayout());
        add(mainSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }


    // TODO update
    private void setWindowTitle() {
//        mainViews[1].getViewProperties().setTitle(
//                Translator.getTerm("InputTermInfoTablePanel") + "（" + termInfoTablePanel.getTableSize() + "）");
//        mainViews[2].getViewProperties().setTitle(
//                Translator.getTerm("RemovedTermInfoTablePanel") + "（" + removedTermInfoTablePanel.getTableSize() + "）");
//        rootWindow.repaint();
    }

    public void setInputTermInfoTableModel(Map<String, TermInfo> termInfoMap, int docNum) {
        termInfoTablePanel.setTermInfoTableModel(termInfoMap, docNum);
        removedTermInfoTablePanel.setTermInfoTableModel(new HashMap<>(), docNum);
        setWindowTitle();
    }

    public void setInputDocumentListModel(ListModel listModel) {
        documentViewer.setDocumentList(listModel);
    }

    public void loadInputTermInfoTable() {
        termInfoTablePanel.loadTermInfoTable();
        removedTermInfoTablePanel.loadTermInfoTable();
        setWindowTitle();
    }

    public void loadInputTermInfoTable(File file, File removedFile) {
        termInfoTablePanel.loadTermInfoTable(file);
        removedTermInfoTablePanel.loadTermInfoTable(removedFile);
        setWindowTitle();
    }

    public void loadInputTermInfoTable(int projectID, Statement stmt, int docNum) {
        termInfoTablePanel.loadTermInfoTable(projectID, stmt, "term_info", docNum);
        removedTermInfoTablePanel.loadTermInfoTable(projectID, stmt, "removed_term_info", docNum);
        setWindowTitle();
    }

    public void saveInputTermInfoTable() {
        termInfoTablePanel.saveTermInfoTable();
        removedTermInfoTablePanel.saveTermInfoTable();
    }

    public void saveInputTermInfoTable(File file, File removedFile) {
        termInfoTablePanel.saveTermInfoTable(file);
        removedTermInfoTablePanel.saveTermInfoTable(removedFile);
    }

    public void saveTermInfoTable(int projectID, Statement stmt) {
        termInfoTablePanel.saveTermInfoTable(projectID, stmt, "term_info");
        removedTermInfoTablePanel.saveTermInfoTable(projectID, stmt, "removed_term_info");
    }

    private void setInputTermSet(int taskCnt) {
        DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("SetInputTermListButton"));
        String[] inputTerms = termTextArea.getText().split("\\r\\n|\\n|\\r");
        Set<String> inputTermSet = new HashSet<>(Arrays.asList(inputTerms));
        conceptSelectionPanel.loadInputTermSet(inputTermSet, taskCnt);
    }

    private void addInputTermSet(int taskCnt) {
        DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("AddInputTermListButton"));
        String[] inputTerms = termTextArea.getText().split("\\r\\n|\\n|\\r");
        Set<String> inputTermSet = new HashSet<>(Arrays.asList(inputTerms));
        conceptSelectionPanel.addInputTermSet(inputTermSet, taskCnt);
    }

    private void addInputTerms() {
        JTable termInfoTable = termInfoTablePanel.getTermInfoTable();
        int[] rows = termInfoTable.getSelectedRows();
        StringBuilder inputTerms = new StringBuilder();
        for (int row : rows) {
            String term = (String) termInfoTable.getValueAt(row, getColumnNamePosition(termInfoTable, Translator
                    .getTerm("TermLabel"))); // 現在選択されている行のデータがほしいのでTableを使う
            inputTerms.append(term).append(System.lineSeparator());
        }
        termTextArea.setText(termTextArea.getText() + inputTerms.toString());
    }

    private void deleteTableItems() {
        termInfoTablePanel.setIsDeletingTableItems(true);
        removedTermInfoTablePanel.setIsDeletingTableItems(true);
        JTable inputTermInfoTable = termInfoTablePanel.getTermInfoTable();
        JTable removedTermInfoTable = removedTermInfoTablePanel.getTermInfoTable();
        DefaultTableModel inputTermInfoTableModel = (DefaultTableModel) inputTermInfoTable.getModel();
        DefaultTableModel removedTermInfoTableModel = (DefaultTableModel) removedTermInfoTable.getModel();

        int[] selectedRows = inputTermInfoTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length <= 0) return;

        Map<String, Integer> columnNamePositionMap = getColumnNamePositionMap(inputTermInfoTable);
        for (int i = selectedRows.length - 1; 0 <= i; i--) {
            String deleteTerm = (String) inputTermInfoTable.getValueAt(selectedRows[i], getColumnNamePosition(
                    inputTermInfoTable, Translator.getTerm("TermLabel"))); // 現在選択されている行のデータがほしいのでTableを使う

            TermInfo info = termInfoTablePanel.getTermInfo(deleteTerm);
            removedTermInfoTablePanel.addTermInfoMapKey(deleteTerm, info);
            termInfoTablePanel.removeTermInfoMapKey(deleteTerm);

            // Vector rowData = (Vector)
            // inputTermInfoTableModel.getDataVector().get(selectedRows[i]);
            // // ソートしたり列を入れ替えたりする場合は，このメソッドは利用できない
            // Vector rowData = getRowData(selectedRows[i], inputTermInfoTable);
            Vector rowData = getRowData(selectedRows[i], inputTermInfoTable, columnNamePositionMap);
            removedTermInfoTableModel.insertRow(0, rowData);
            inputTermInfoTableModel.removeRow(inputTermInfoTable.convertRowIndexToModel(selectedRows[i]));
        }
        setWindowTitle();
        documentViewer.setDocumentArea();
        termInfoTablePanel.setIsDeletingTableItems(false);
        removedTermInfoTablePanel.setIsDeletingTableItems(false);
    }

    private int getColumnNamePosition(JTable table, String columnName) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(columnName)) {
                return i;
            }
        }
        return 0;
    }

    private Map<String, Integer> getColumnNamePositionMap(JTable table) {
        Map<String, Integer> columnNamePositionMap = new HashMap<>();
        String columnName = Translator.getTerm("TermLabel");
        columnNamePositionMap.put(columnName, getColumnNamePosition(table, columnName));
        columnName = Translator.getTerm("POSLabel");
        columnNamePositionMap.put(columnName, getColumnNamePosition(table, columnName));
        columnName = Translator.getTerm("TFLabel");
        columnNamePositionMap.put(columnName, getColumnNamePosition(table, columnName));
        columnName = Translator.getTerm("IDFLabel");
        columnNamePositionMap.put(columnName, getColumnNamePosition(table, columnName));
        columnName = Translator.getTerm("TFIDFLabel");
        columnNamePositionMap.put(columnName, getColumnNamePosition(table, columnName));
        columnName = Translator.getTerm("UpperConceptLabel");
        columnNamePositionMap.put(columnName, getColumnNamePosition(table, columnName));
        return columnNamePositionMap;
    }

    private Vector getRowData(int row, JTable table, Map<String, Integer> columnNamePositionMap) {
        Vector rowData = new Vector();
        rowData.add(table.getValueAt(row, columnNamePositionMap.get(Translator.getTerm("TermLabel"))));
        rowData.add(table.getValueAt(row, columnNamePositionMap.get(Translator.getTerm("POSLabel"))));
        rowData.add(table.getValueAt(row, columnNamePositionMap.get(Translator.getTerm("TFLabel"))));
        rowData.add(table.getValueAt(row, columnNamePositionMap.get(Translator.getTerm("IDFLabel"))));
        rowData.add(table.getValueAt(row, columnNamePositionMap.get(Translator.getTerm("TFIDFLabel"))));
        rowData.add(table.getValueAt(row, columnNamePositionMap.get(Translator.getTerm("UpperConceptLabel"))));
        return rowData;
    }

    public void removeTerm(String rmTerm) {
        JTable inputTermInfoTable = termInfoTablePanel.getTermInfoTable();
        JTable removedTermInfoTable = removedTermInfoTablePanel.getTermInfoTable();
        DefaultTableModel inputTermInfoTableModel = (DefaultTableModel) inputTermInfoTable.getModel();
        DefaultTableModel removedTermInfoTableModel = (DefaultTableModel) removedTermInfoTable.getModel();

        for (int i = 0; i < inputTermInfoTableModel.getRowCount(); i++) {
            // 全体をパースするだけなのでTableModelでよい．また，Modelの場合は絶対位置となるので0でよい．
            String term = (String) inputTermInfoTableModel.getValueAt(i, 0);
            if (term.equals(rmTerm)) {
                TermInfo info = termInfoTablePanel.getTermInfo(rmTerm);
                removedTermInfoTableModel.insertRow(0, info.getRowData());
                removedTermInfoTablePanel.addTermInfoMapKey(rmTerm, info);
                termInfoTablePanel.removeTermInfoMapKey(rmTerm);
                inputTermInfoTableModel.removeRow(i);
                break;
            }
        }
        setWindowTitle();
    }

    public void addInputTermInfo(TermInfo info) {
        JTable inputTermInfoTable = termInfoTablePanel.getTermInfoTable();
        if (termInfoTablePanel.getTermInfo(info.getTerm()) == null) {
            DefaultTableModel inputTermInfoTableModel = (DefaultTableModel) inputTermInfoTable.getModel();
            termInfoTablePanel.addTermInfoMapKey(info.getTerm(), info);
            inputTermInfoTableModel.insertRow(0, info.getRowData());
            setWindowTitle();
        }
    }

    public void addTerm(String addTerm) {
        JTable inputTermInfoTable = termInfoTablePanel.getTermInfoTable();
        JTable removedTermInfoTable = removedTermInfoTablePanel.getTermInfoTable();
        DefaultTableModel inputTermInfoTableModel = (DefaultTableModel) inputTermInfoTable.getModel();
        DefaultTableModel removedTermInfoTableModel = (DefaultTableModel) removedTermInfoTable.getModel();

        for (int i = 0; i < removedTermInfoTableModel.getRowCount(); i++) {
            // 全体をパースするだけなのでTableModelでよい．また，Modelの場合は絶対位置となるので0でよい．
            String term = (String) removedTermInfoTableModel.getValueAt(i, 0);
            if (term.equals(addTerm)) {
                TermInfo info = removedTermInfoTablePanel.getTermInfo(addTerm);
                inputTermInfoTableModel.insertRow(0, info.getRowData());
                termInfoTablePanel.addTermInfoMapKey(addTerm, info);
                removedTermInfoTablePanel.removeTermInfoMapKey(addTerm);
                removedTermInfoTableModel.removeRow(i);
                break;
            }
        }
        setWindowTitle();
    }

    private void returnTableItems(boolean isCompleteDelete) {
        termInfoTablePanel.setIsDeletingTableItems(true);
        removedTermInfoTablePanel.setIsDeletingTableItems(true);
        JTable inputTermInfoTable = termInfoTablePanel.getTermInfoTable();
        JTable removedTermInfoTable = removedTermInfoTablePanel.getTermInfoTable();
        DefaultTableModel inputTermInfoTableModel = (DefaultTableModel) inputTermInfoTable.getModel();
        DefaultTableModel removedTermInfoTableModel = (DefaultTableModel) removedTermInfoTable.getModel();

        int[] selectedRows = removedTermInfoTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length <= 0) return;
        Map<String, Integer> columnNamePositionMap = getColumnNamePositionMap(removedTermInfoTable);
        for (int i = selectedRows.length - 1; 0 <= i; i--) {
            String returnTerm = (String) removedTermInfoTable.getValueAt(selectedRows[i], getColumnNamePosition(
                    removedTermInfoTable, Translator.getTerm("TermLabel"))); // 現在選択されている行のデータがほしいのでTableを使う
            TermInfo info = removedTermInfoTablePanel.getTermInfo(returnTerm);
            if (!isCompleteDelete) {
                termInfoTablePanel.addTermInfoMapKey(returnTerm, info);
            }
            removedTermInfoTablePanel.removeTermInfoMapKey(returnTerm);

            // Vector rowData = (Vector)
            // removedTermInfoTableModel.getDataVector().get(selectedRows[i]);
            // // ソートしたり列を入れ替えたりする場合は，このメソッドは利用できない
            Vector rowData = getRowData(selectedRows[i], removedTermInfoTable, columnNamePositionMap); // ソートしたり，列をいれかえたりしても対応できるようにするためJTableを用いる
            inputTermInfoTableModel.insertRow(0, rowData);
            removedTermInfoTableModel.removeRow(removedTermInfoTable.convertRowIndexToModel(selectedRows[i])); // viewのindexをmodelのindexに変換．insert後に削除する
            // System.out.println(Translator.getTerm("ReturnButton") + "(" +
            // (selectedRows.length - i) + "/"
            // + selectedRows.length + "): " + returnTerm);
        }
        setWindowTitle();
        documentViewer.setDocumentArea();
        termInfoTablePanel.setIsDeletingTableItems(false);
        removedTermInfoTablePanel.setIsDeletingTableItems(false);
    }

    public Collection<TermInfo> getTermInfoSet() {
        return termInfoTablePanel.getTermInfoSet();
    }

    public Collection<TermInfo> getRemovedTermInfoSet() {
        return removedTermInfoTablePanel.getTermInfoSet();
    }

    public TermInfo getInputTermInfo(String term) {
        return termInfoTablePanel.getTermInfo(term);
    }

    public TermInfo getRemovedTermInfo(String term) {
        return removedTermInfoTablePanel.getTermInfo(term);
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProjectPanel project = DODDLE_OWL.getCurrentProject();

        if (e.getSource() == setTermSetButton) {
            setInputTermSet(0);
            project.addLog("SetInputTermSetButton");
        } else if (e.getSource() == addTermSetButton) {
            addInputTermSet(0);
            project.addLog("AddInputTermSetButton");
        } else if (e.getSource() == addTermListButton) {
            addInputTerms();
            project.addLog("AddInputTermListButton");
        } else if (e.getSource() == deleteTableItemButton) {
            deleteTableItems();
            project.addLog("RemoveButton", "TermSelectionPanel");
        } else if (e.getSource() == returnTableItemButton) {
            returnTableItems(false);
            project.addLog("ReturnButton", "TermSelectionPanel");
        } else if (e.getSource() == completelyDeleteTableItemButton) {
            returnTableItems(true);
            project.addLog("CompletelyDeleteTermButtonButton", "TermSelectionPanel");
        } else if (e.getSource() == reloadDocumentAreaButton) {
            documentViewer.setDocumentArea();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (e.getSource() == termInfoTablePanel.getTable()) {
                deleteTableItems();
                DODDLE_OWL.getCurrentProject().addLog("RemoveButton", "TermSelectionPanel");
            } else if (e.getSource() == removedTermInfoTablePanel.getTable()) {
                returnTableItems(false);
                DODDLE_OWL.getCurrentProject().addLog("ReturnButton", "TermSelectionPanel");
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
