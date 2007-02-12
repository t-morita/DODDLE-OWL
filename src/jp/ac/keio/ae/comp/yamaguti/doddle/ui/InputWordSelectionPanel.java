package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class InputWordSelectionPanel extends JPanel implements ActionListener, ChangeListener {

    private JTabbedPane tableTab;
    private JTextArea inputWordArea;
    private WordInfoTablePanel wordInfoTablePanel;
    private WordInfoTablePanel removedWordInfoTablePanel;
    private InputWordDocumentViewer documentViewer;

    private JButton addInputWordButton;
    private JButton deleteTableItemButton;
    private JButton returnTableItemButton;
    private JButton reloadDocumentAreaButton;
    private JButton setInputWordSetButton;
    private JButton addInputWordSetButton;

    private DisambiguationPanel disambiguationPanel;

    public InputWordSelectionPanel(DisambiguationPanel ui) {
        wordInfoTablePanel = new WordInfoTablePanel();
        removedWordInfoTablePanel = new WordInfoTablePanel();
        documentViewer = new InputWordDocumentViewer();

        System.setProperty("sen.home", DODDLE.EDR_HOME + "sen-1.2.1");
        disambiguationPanel = ui;

        inputWordArea = new JTextArea(10, 20);
        JScrollPane inputWordsAreaScroll = new JScrollPane(inputWordArea);
        inputWordsAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("InputWordSelectionPanel.InputWordList")));

        addInputWordButton = new JButton(Translator.getString("InputWordSelectionPanel.AddInputWords"));
        addInputWordButton.addActionListener(this);
        deleteTableItemButton = new JButton(Translator.getString("InputWordSelectionPanel.Remove"));
        deleteTableItemButton.addActionListener(this);
        returnTableItemButton = new JButton(Translator.getString("InputWordSelectionPanel.Return"));
        returnTableItemButton.addActionListener(this);
        reloadDocumentAreaButton = new JButton(Translator.getString("Reload"));
        reloadDocumentAreaButton.addActionListener(this);
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.add(addInputWordButton);
        tableButtonPanel.add(deleteTableItemButton);
        tableButtonPanel.add(returnTableItemButton);
        tableButtonPanel.add(reloadDocumentAreaButton);

        setInputWordSetButton = new JButton(Translator.getString("InputWordSelectionPanel.SetInputWordSet"));
        setInputWordSetButton.addActionListener(this);
        addInputWordSetButton = new JButton(Translator.getString("InputWordSelectionPanel.AddInputWordSet"));
        addInputWordSetButton.addActionListener(this);
        JPanel inputWordsButtonPanel = new JPanel();
        inputWordsButtonPanel.add(setInputWordSetButton);
        inputWordsButtonPanel.add(addInputWordSetButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(tableButtonPanel, BorderLayout.WEST);
        buttonPanel.add(inputWordsButtonPanel, BorderLayout.EAST);

        tableTab = new JTabbedPane();
        tableTab.addChangeListener(this);
        tableTab.add(wordInfoTablePanel, Translator.getString("InputWordSelectionPanel.ExtractedWordTable"));
        tableTab.add(removedWordInfoTablePanel, Translator.getString("InputWordSelectionPanel.RemovedWordTable"));
        tableTab.add(documentViewer, Translator.getString("InputWordSelectionPanel.DocumentViewer"));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(tableTab, BorderLayout.CENTER);
        centerPanel.add(inputWordsAreaScroll, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setWordInfoTableModel(Map<String, WordInfo> wordInfoMap, int docNum) {
        wordInfoTablePanel.setWordInfoTableModel(wordInfoMap, docNum);
        removedWordInfoTablePanel.setWordInfoTableModel(new HashMap<String, WordInfo>(), docNum);
        documentViewer.setTableModel(wordInfoTablePanel.getTableModel(), removedWordInfoTablePanel.getTableModel());
    }

    public void setInputDocumentListModel(ListModel listModel) {
        documentViewer.setDocumentList(listModel);
    }

    public void loadWordInfoTable() {
        wordInfoTablePanel.loadWordInfoTable();
        removedWordInfoTablePanel.loadWordInfoTable();
    }

    public void loadWordInfoTable(File file, File removedFile) {
        wordInfoTablePanel.loadWordInfoTable(file);
        removedWordInfoTablePanel.loadWordInfoTable(removedFile);
    }

    public void saveWordInfoTable() {
        wordInfoTablePanel.saveWordInfoTable();
        removedWordInfoTablePanel.saveWordInfoTable();
    }

    public void saveWordInfoTable(File file, File removedFile) {
        wordInfoTablePanel.saveWordInfoTable(file);
        removedWordInfoTablePanel.saveWordInfoTable(removedFile);
    }

    private void setInputWordSet() {
        DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.SetInputWordListDone"));
        String[] inputWords = inputWordArea.getText().split("\n");
        Set<String> inputWordSet = new HashSet<String>(Arrays.asList(inputWords));
        disambiguationPanel.loadInputWordSet(inputWordSet);
        DODDLE.setSelectedIndex(DODDLE.INPUT_MODULE);
    }

    private void addInputWordSet() {
        DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.SetInputWordListDone"));
        String[] inputWords = inputWordArea.getText().split("\n");
        Set<String> inputWordSet = new HashSet<String>(Arrays.asList(inputWords));
        disambiguationPanel.addInputWordSet(inputWordSet);
        DODDLE.setSelectedIndex(DODDLE.INPUT_MODULE);
    }

    private void addInputWords() {
        JTable wordInfoTable = wordInfoTablePanel.getWordInfoTable();
        int[] rows = wordInfoTable.getSelectedRows();
        StringBuilder inputWords = new StringBuilder("");
        for (int i = 0; i < rows.length; i++) {
            String word = (String) wordInfoTable.getValueAt(rows[i], 0);
            inputWords.append(word + "\n");
        }
        inputWordArea.setText(inputWordArea.getText() + inputWords.toString());
    }

    private void deleteTableItems() {
        JTable wordInfoTable = wordInfoTablePanel.getWordInfoTable();
        JTable removedWordInfoTable = removedWordInfoTablePanel.getWordInfoTable();
        DefaultTableModel wordInfoTableModel = (DefaultTableModel) wordInfoTable.getModel();
        DefaultTableModel removedWordInfoTableModel = (DefaultTableModel) removedWordInfoTable.getModel();

        int[] rows = wordInfoTable.getSelectedRows();
        Set<String> deleteWordSet = new HashSet<String>();
        for (int i = 0; i < rows.length; i++) {
            String deleteWord = (String) wordInfoTable.getValueAt(rows[i], 0);
            deleteWordSet.add(deleteWord);
            WordInfo info = wordInfoTablePanel.getWordInfo(deleteWord);
            removedWordInfoTablePanel.addWordInfoMapKey(deleteWord, info);
            wordInfoTablePanel.removeWordInfoMapKey(deleteWord);
        }
        for (int i = 0; i < wordInfoTableModel.getRowCount(); i++) {
            String word = (String) wordInfoTableModel.getValueAt(i, 0);
            if (deleteWordSet.contains(word)) {
                Vector rowData = new Vector();
                for (int j = 0; j < wordInfoTableModel.getColumnCount(); j++) {
                    rowData.add(wordInfoTableModel.getValueAt(i, j));
                }
                removedWordInfoTableModel.insertRow(0, rowData);
                wordInfoTableModel.removeRow(i);
                i = 0;
                continue;
            }
        }
        tableTab.setTitleAt(0, Translator.getString("InputWordSelectionPanel.ExtractedWordTable") + "（"
                + wordInfoTablePanel.getTableSize() + "）");
        tableTab.setTitleAt(1, Translator.getString("InputWordSelectionPanel.ExtractedWordTable") + "（"
                + removedWordInfoTablePanel.getTableSize() + "）");
        tableTab.repaint();
    }

    private void returnTableItems() {
        JTable wordInfoTable = wordInfoTablePanel.getWordInfoTable();
        JTable removedWordInfoTable = removedWordInfoTablePanel.getWordInfoTable();
        DefaultTableModel wordInfoTableModel = (DefaultTableModel) wordInfoTable.getModel();
        DefaultTableModel removedWordInfoTableModel = (DefaultTableModel) removedWordInfoTable.getModel();

        int[] rows = removedWordInfoTable.getSelectedRows();
        Set<String> returnWordSet = new HashSet<String>();
        for (int i = 0; i < rows.length; i++) {
            String returnWord = (String) removedWordInfoTable.getValueAt(rows[i], 0);
            returnWordSet.add(returnWord);
            WordInfo info = removedWordInfoTablePanel.getWordInfo(returnWord);
            wordInfoTablePanel.addWordInfoMapKey(returnWord, info);
            removedWordInfoTablePanel.removeWordInfoMapKey(returnWord);
        }
        for (int i = 0; i < removedWordInfoTableModel.getRowCount(); i++) {
            String word = (String) removedWordInfoTableModel.getValueAt(i, 0);
            if (returnWordSet.contains(word)) {
                Vector rowData = new Vector();
                for (int j = 0; j < wordInfoTableModel.getColumnCount(); j++) {
                    rowData.add(removedWordInfoTableModel.getValueAt(i, j));
                }
                wordInfoTableModel.insertRow(0, rowData);
                removedWordInfoTableModel.removeRow(i);
                i = 0;
                continue;
            }
        }
        tableTab.setTitleAt(0, Translator.getString("InputWordSelectionPanel.ExtractedWordTable") + "（"
                + wordInfoTablePanel.getTableSize() + "）");
        tableTab.setTitleAt(1, Translator.getString("InputWordSelectionPanel.ExtractedWordTable") + "（"
                + removedWordInfoTablePanel.getTableSize() + "）");
        tableTab.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setInputWordSetButton) {
            setInputWordSet();
        } else if (e.getSource() == addInputWordSetButton) {
            addInputWordSet();
        } else if (e.getSource() == addInputWordButton) {
            addInputWords();
        } else if (e.getSource() == deleteTableItemButton) {
            deleteTableItems();
        } else if (e.getSource() == returnTableItemButton) {
            returnTableItems();
        } else if (e.getSource() == reloadDocumentAreaButton) {
            documentViewer.setDocumentArea();
        }
    }

    public void stateChanged(ChangeEvent e) {
        if (tableTab.getSelectedIndex() == 0) {
            addInputWordButton.setVisible(true);
            deleteTableItemButton.setVisible(true);
            returnTableItemButton.setVisible(false);
            reloadDocumentAreaButton.setVisible(false);
        } else if (tableTab.getSelectedIndex() == 1) {
            addInputWordButton.setVisible(false);
            deleteTableItemButton.setVisible(false);
            returnTableItemButton.setVisible(true);
            reloadDocumentAreaButton.setVisible(false);
        } else if (tableTab.getSelectedIndex() == 2) {
            addInputWordButton.setVisible(false);
            deleteTableItemButton.setVisible(false);
            returnTableItemButton.setVisible(false);
            reloadDocumentAreaButton.setVisible(true);
        }
    }
}
