/*
 * @(#)  2006/11/29
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
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
public class InputWordDocumentViewer extends JPanel implements MouseListener, ActionListener, HyperlinkListener {
    private JList documentList;
    private ListModel inputDocumentListModel;
    private JEditorPane documentArea;
    private JEditorPane linkArea;
    
    private JRadioButton complexWordRadioButton;
    private JRadioButton nounRadioButton;
    private JRadioButton verbRadioButton;
    private JRadioButton otherRadioButton;
    
    private TableModel wordInfoTableModel;
    private TableModel removedWordInfoTableModel;

    public InputWordDocumentViewer() {
        documentList = new JList();
        documentList.addMouseListener(this);
        documentList.setCellRenderer(new DocumentListCellRenderer());
        JScrollPane documentListScroll = new JScrollPane(documentList);
        documentListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("InputDocumentList")));
        documentArea = new JEditorPane("text/html", "");
        documentArea.addHyperlinkListener(this);
        documentArea.setEditable(false);                
        JScrollPane documentAreaScroll = new JScrollPane(documentArea);
        documentAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("InputDocumentArea")));
        
        linkArea = new JEditorPane("text/html", "");
        linkArea.addHyperlinkListener(this);
        linkArea.setEditable(false);                
        JScrollPane linkAreaScroll = new JScrollPane(linkArea);
        linkAreaScroll.setBorder(BorderFactory.createTitledBorder("links"));
        linkAreaScroll.setMinimumSize(new Dimension(100, 50));
        linkAreaScroll.setPreferredSize(new Dimension(100, 50));

        JPanel documentPanel = new JPanel();
        documentPanel.setLayout(new BorderLayout());
        documentPanel.add(documentAreaScroll, BorderLayout.CENTER);
        documentPanel.add(linkAreaScroll, BorderLayout.WEST);
        
        complexWordRadioButton = new JRadioButton("Complex Word");
        complexWordRadioButton.addActionListener(this);
        nounRadioButton = new JRadioButton("Noun");
        nounRadioButton.addActionListener(this);
        verbRadioButton = new JRadioButton("Verb");
        verbRadioButton.addActionListener(this);
        otherRadioButton = new JRadioButton("Other");
        otherRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(complexWordRadioButton);
        group.add(nounRadioButton);
        group.add(verbRadioButton);
        group.add(otherRadioButton);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(complexWordRadioButton);
        buttonPanel.add(nounRadioButton);
        buttonPanel.add(verbRadioButton);
        buttonPanel.add(otherRadioButton);
        
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentListScroll, documentPanel);
        splitPane.setDividerSize(DODDLEConstants.DIVIDER_SIZE);
        splitPane.setOneTouchExpandable(true);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel,BorderLayout.SOUTH);
    }
    
    enum POS {
        COMPLEX, NOUN, VERB, OTHER;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == complexWordRadioButton) {
            selectedPOS = POS.COMPLEX;
            setDocumentArea();
        } else if (e.getSource() == nounRadioButton) {
            selectedPOS = POS.NOUN;
            setDocumentArea();
        } else if (e.getSource() == verbRadioButton) {
            selectedPOS = POS.VERB;
            setDocumentArea();
        } else if (e.getSource() == otherRadioButton) {
            selectedPOS = POS.OTHER;
            setDocumentArea();
        }
    }

    public void setTableModel(TableModel wtm, TableModel rwtm) {
        wordInfoTableModel = wtm;
        removedWordInfoTableModel = rwtm;
    }

    //class DocumentListCellRenderer extends JCheckBox implements ListCellRenderer {
    class DocumentListCellRenderer extends JRadioButton implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            //JCheckBox checkBox = (JCheckBox) value;
            JRadioButton checkBox = (JRadioButton) value;
            setText(checkBox.getText());
            setSelected(checkBox.isSelected());
            return this;
        }
    }

    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        int index = documentList.locationToIndex(p);
        if (0 < documentList.getModel().getSize()) {
            //JCheckBox checkBox = (JCheckBox) documentList.getModel().getElementAt(index);
            JRadioButton checkBox = (JRadioButton) documentList.getModel().getElementAt(index);
//            if (checkBox.isSelected()) {
            checkBox.setSelected(true);
//            } else {
//                checkBox.setSelected(true);
//            }
        }
        selectedDoc = (Document) inputDocumentListModel.getElementAt(index);
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
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < inputDocumentListModel.getSize(); i++) {
            Document doc = (Document) inputDocListModel.getElementAt(i);
            //JCheckBox checkBox = new JCheckBox(doc.getFile().getAbsolutePath(), true);
            JRadioButton checkBox = new JRadioButton(doc.getFile().getAbsolutePath());
            listModel.addElement(checkBox);
            group.add(checkBox);
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

    private boolean isNoun(String pos) {
        return pos.indexOf("名詞") != -1 || pos.toLowerCase().indexOf("noun") != -1;
    }
    
    private boolean isVerb(String pos) {
        return pos.indexOf("動詞") != -1 || pos.toLowerCase().indexOf("verb") != -1;
    }
    
    private boolean isComplexword(String pos) {
        return pos.indexOf("複合語") != -1 || pos.toLowerCase().indexOf("complex word") != -1;
    }
    
    private boolean isOther(String pos) {
        return !(isNoun(pos) || isVerb(pos) || isComplexword(pos));
    }
    
    private String hilightWord(TableModel tblModel, String text) {
        for (int i = 0; i < tblModel.getRowCount(); i++) {
            String word = (String) tblModel.getValueAt(i, 0);
            String wpos = (String) tblModel.getValueAt(i, 1);
            int termFrequency = (Integer) tblModel.getValueAt(i, 2);
            if (word.length() == 1) {
                System.out.println(word);
                continue;
            }
            if (selectedPOS == POS.NOUN && isNoun(wpos)) {
                text = hilightWord(tblModel, text, word, termFrequency);                
            } else if (selectedPOS == POS.VERB && isVerb(wpos)) {
                text = hilightWord(tblModel, text, word, termFrequency);                
            } else if (selectedPOS == POS.COMPLEX && isComplexword(wpos)) {
                text = hilightWord(tblModel, text, word, termFrequency);
            } else if (selectedPOS == POS.OTHER && isOther(wpos)) {
                text = hilightWord(tblModel, text, word, termFrequency);                
            }            
        }
        return text;
    }

    /**
     * @param tblModel
     * @param text
     * @param word
     * @param termFrequency
     * @return
     */
    private String hilightWord(TableModel tblModel, String text, String word, int termFrequency) {
        if (text.indexOf(word) != -1) {
            if (tblModel == wordInfoTableModel) {
                // text = text.replace(word, "<b><font color=blue size=" +
                // getFontSize(termFrequency) + "><a href=>|" + word
                  // + "|</a></font></b>");
                text = text.replace(word, "<font color=\"blue\"><a href=\"inputword:"+word+"\">|" + word  + "|</a></font>");
            } else if (tblModel == removedWordInfoTableModel) {
                //text = text.replace(word, "|<b><font color=gray size=" + getFontSize(termFrequency) + "><a href=>|" + word
                  //      + "|</a></font></b>");
                text = text.replace(word, "<font color=\"gray\"><a href=\"removedword:"+word+"\">|" + word + "|</a></font>");
            }
        }
        return text;
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

    private int lineNum = 0;
    private Document selectedDoc = null;
    private POS selectedPOS = null;
    
    public String getHilightedString() {
        String text = "";
        String[] texts = selectedDoc.getTexts();
        int num = 0; 
        for (int i = lineNum; i < selectedDoc.getSize(); i++,num++) {
            if (num == 20) {
                break;
            }
            text += texts[i]+"<br>";
        }
        
        Map<String, Integer> correctTermFreqMap = getTermFrequencyMap(wordInfoTableModel);
        Map<String, Integer> removedTermFreqMap = getTermFrequencyMap(removedWordInfoTableModel);
//        if (doc.getLang().equals("en")) {
//            String[] words = text.split(" ");
//            for (int j = 0; j < words.length; j++) {
//                builder.append(hilightWord(words[j], correctTermFreqMap, removedTermFreqMap));
//                builder.append(" ");
//            }
//        } else {
        if (selectedDoc.getLang().equals("en")) {
            String[] words = text.split("\\s+");
            text = "";
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                word = hilightWord(wordInfoTableModel, word);
                if (word.indexOf("<") == -1) {
                    word= hilightWord(removedWordInfoTableModel, word);
                }
                text += word+" ";
            }
        } else {
            text = hilightWord(wordInfoTableModel, text);
            text = hilightWord(removedWordInfoTableModel, text);            
        }
        return text;
    }
    
    public void setDocumentArea() {
        if (inputDocumentListModel == null) { return; }
        StringBuilder docBuilder = new StringBuilder();
        StringBuilder linkBuilder = new StringBuilder();
        docBuilder.append("<html><body>");
        linkBuilder.append("<html><body>");
        String text = getHilightedString();
        docBuilder.append(text);
        docBuilder.append("<br><br>");
        docBuilder.append("<ul>");
        for (int j = 0; j < selectedDoc.getSize(); j+=20) {
            linkBuilder.append("<li><a href=\""+j+"\">"+(j+1)+"-"+(j+20)+"</a></li>");
        }
        linkBuilder.append("</ul>");
        docBuilder.append("</body></html>");
        linkBuilder.append("</body></html>");
        linkArea.setText(linkBuilder.toString());
        documentArea.setText(docBuilder.toString());
    }
    
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            InputWordSelectionPanel inputWordSelectionPanel = DODDLE.getCurrentProject().getInputWordSelectionPanel();
            String[] descriptions = e.getDescription().split(":");
            StringBuilder builder = new StringBuilder();
            builder.append("<html><body>");
            if (descriptions.length == 1) {                
                lineNum = Integer.parseInt(e.getDescription());
                System.out.println("linenum: "+lineNum);
                builder.append(getHilightedString());                            
            } else {
                String type = descriptions[0];
                String word = descriptions[1];                          
                if (type.equals("inputword")) {
                    inputWordSelectionPanel.removeWord(word);
                } else {
                    inputWordSelectionPanel.addWord(word);
                }
                builder.append(getHilightedString());  
            }
            builder.append("</body></html>");
            documentArea.setText(builder.toString());
        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            
        }
    }
}
