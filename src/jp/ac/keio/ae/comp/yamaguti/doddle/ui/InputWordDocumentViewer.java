/*
 * @(#)  2006/11/29
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class InputWordDocumentViewer extends JPanel implements MouseListener {
    private JList documentList;
    private ListModel inputDocumentListModel;
    private JEditorPane documentArea;

    private TableModel wordInfoTableModel;
    private TableModel removedWordInfoTableModel;

    public InputWordDocumentViewer() {
        documentList = new JList();
        documentList.addMouseListener(this);
        documentList.setCellRenderer(new DocumentListCellRenderer());
        JScrollPane documentListScroll = new JScrollPane(documentList);
        documentListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("InputDocumentList")));
        documentArea = new JEditorPane("text/html", "");
        documentArea.setEditable(false);
        JScrollPane documentAreaScroll = new JScrollPane(documentArea);
        documentAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("InputDocumentArea")));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentListScroll, documentAreaScroll);
        splitPane.setDividerSize(DODDLEConstants.DIVIDER_SIZE);
        splitPane.setOneTouchExpandable(true);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    public void setTableModel(TableModel wtm, TableModel rwtm) {
        wordInfoTableModel = wtm;
        removedWordInfoTableModel = rwtm;
    }

    class DocumentListCellRenderer extends JCheckBox implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JCheckBox checkBox = (JCheckBox) value;
            setText(checkBox.getText());
            setSelected(checkBox.isSelected());
            return this;
        }
    }

    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        int index = documentList.locationToIndex(p);
        if (0 < documentList.getModel().getSize()) {
            JCheckBox checkBox = (JCheckBox) documentList.getModel().getElementAt(index);
            if (checkBox.isSelected()) {
                checkBox.setSelected(false);
            } else {
                checkBox.setSelected(true);
            }
        }
        setDocumentArea();
        documentList.repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }

    public void setDocumentList(ListModel inputDocListModel) {
        inputDocumentListModel = inputDocListModel;
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < inputDocumentListModel.getSize(); i++) {
            Document doc = (Document) inputDocListModel.getElementAt(i);
            JCheckBox checkBox = new JCheckBox(doc.getFile().getAbsolutePath(), true);
            listModel.addElement(checkBox);
        }
        documentList.setModel(listModel);
        // setDocumentArea();
    }

    private int getFontSize(int frequency) {
        if (1 <= frequency && frequency < 5) {
            return 5;
        } else if (5 <= frequency && frequency < 10) {
            return 6;
        } else if (10 <= frequency && frequency < 15) {
            return 7;
        } else if (15 <= frequency && frequency < 20) {
            return 8;
        } else if (20 <= frequency && frequency < 25) {
            return 9;
        } else if (25 <= frequency) { return 10; }
        return 5;
    }

    private String hilightWord(TableModel tblModel, String line) {
        for (int i = 0; i < tblModel.getRowCount(); i++) {
            String word = (String) tblModel.getValueAt(i, 0);
            int termFrequency = (Integer) tblModel.getValueAt(i, 2);
            if (line.indexOf(word) != -1) {
                if (tblModel == wordInfoTableModel) {
                    line = line.replace(word, "<b><font color=blue size=" + getFontSize(termFrequency) + ">" + word
                            + "</font></b>");
                } else if (tblModel == removedWordInfoTableModel) {
                    line = line.replace(word, "<b><font color=gray size=" + getFontSize(termFrequency) + ">" + word
                            + "</font></b>");
                }
            }
        }
        return line;
    }

    private String hilightWord(String word, Map<String, Integer> correctTermFreqMap,
            Map<String, Integer> removedTermFreqMap) {
        if (correctTermFreqMap.get(word) != null) {
            int freq = correctTermFreqMap.get(word);
            return "<b><font color=blue size=" + getFontSize(freq) + ">" + word + "</font></b>";
        } else if (removedTermFreqMap.get(word) != null) {
            int freq = removedTermFreqMap.get(word);
            return "<b><font color=gray size=" + getFontSize(freq) + ">" + word + "</font></b>";
        } else {
            return word;
        }
    }

    private Map<String, Integer> getTermFrequencyMap(TableModel tblModel) {
        Map<String, Integer> termFreqMap = new HashMap<String, Integer>();
        for (int i = 0; i < tblModel.getRowCount(); i++) {
            String word = (String) tblModel.getValueAt(i, 0);
            int termFrequency = (Integer) tblModel.getValueAt(i, 2);
            termFreqMap.put(word, termFrequency);
        }
        return termFreqMap;
    }

    public void setDocumentArea() {
        if (inputDocumentListModel == null) { return; }
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>");
        for (int i = 0; i < inputDocumentListModel.getSize(); i++) {
            Document doc = (Document) inputDocumentListModel.getElementAt(i);
            JCheckBox checkBox = (JCheckBox) documentList.getModel().getElementAt(i);
            if (checkBox.isSelected()) {
                builder.append("<h3>" + doc.getFile().getAbsolutePath() + "</h3><br>");
                String text = DocumentSelectionPanel.getTextString(doc);
                Map<String, Integer> correctTermFreqMap = getTermFrequencyMap(wordInfoTableModel);
                Map<String, Integer> removedTermFreqMap = getTermFrequencyMap(removedWordInfoTableModel);
                if (doc.getLang().equals("en")) {
                    String[] words = text.split(" ");
                    for (int j = 0; j < words.length; j++) {
                        builder.append(hilightWord(words[j], correctTermFreqMap, removedTermFreqMap));
                        builder.append(" ");
                    }
                } else {
                    String[] lines = text.split("\n");
                    for (int j = 0; j < lines.length; j++) {
                        String line = lines[j];
                        line = hilightWord(wordInfoTableModel, line);
                        line = hilightWord(removedWordInfoTableModel, line);
                        builder.append(hilightWord(wordInfoTableModel, line) + "<br>");
                    }
                    builder.append("<br><br>");
                }
            }
        }
        builder.append("</body></html>");
        documentArea.setText(builder.toString());
    }
}
