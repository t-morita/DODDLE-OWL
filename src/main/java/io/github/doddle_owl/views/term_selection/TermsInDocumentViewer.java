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

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.document_selection.Document;
import io.github.doddle_owl.models.ontology_api.WordNet;
import io.github.doddle_owl.models.term_selection.TermInfo;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.document_selection.DocumentSelectionPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class TermsInDocumentViewer extends JPanel implements MouseListener, ActionListener, HyperlinkListener, KeyListener {

    private Document selectedDoc = null;
    private DODDLE_POS selectedPOS = null;

    private final JList<Document> documentList;
    private DefaultListModel<Document> documentListModel;
    private final JEditorPane documentArea;

    private final TitledBorder documentAreaBorder;

    private final JTextField searchField;
    private final JButton addTermButton;
    private final JRadioButton compoundWordRadioButton;
    private final JRadioButton nounRadioButton;
    private final JRadioButton verbRadioButton;
    private final JRadioButton otherRadioButton;

    private final JTable wordInfoTable;
    private DefaultTableModel wordInfoTableModel;

    private static final String HTML_START = "<html><body style='font-size: 12px;'>";
    private static final String HTML_END = "</body></html>";
    private static final String CORRECT_WORD_LINK_COLOR = "#1111cc";
    private static final String REMOVED_WORD_LINK_COLOR = "gray";

    public void initialize() {
        documentListModel.clear();
        documentArea.setText("");
        searchField.setText("");
    }

    public TermsInDocumentViewer() {
        documentListModel = new DefaultListModel<>();
        documentList = new JList(documentListModel);
        documentList.addListSelectionListener(e -> {
            if (!documentListModel.isEmpty()) {
                selectedDoc = documentList.getSelectedValue();
                setDocumentArea();
            }
        });
        JScrollPane documentListScroll = new JScrollPane(documentList);
        documentListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("InputDocumentList")));
        documentArea = new JEditorPane("text/html", "");
        documentArea.addMouseListener(this);
        documentArea.addHyperlinkListener(this);
        documentArea.setEditable(false);
        JScrollPane documentAreaScroll = new JScrollPane(documentArea);
        documentAreaBorder = BorderFactory.createTitledBorder(Translator.getTerm("InputDocumentArea"));
        documentAreaScroll.setBorder(documentAreaBorder);

        documentList.addMouseListener(this);

        JPanel documentPanel = new JPanel();
        documentPanel.setLayout(new BorderLayout());
        documentPanel.add(documentAreaScroll, BorderLayout.CENTER);

        searchField = new JTextField(15);
        searchField.addActionListener(this);
        searchField.addKeyListener(this);

        addTermButton = new JButton(Translator.getTerm("AddButton"));
        addTermButton.addActionListener(this);

        compoundWordRadioButton = new JRadioButton(Translator.getTerm("GensenCheckBox"));
        compoundWordRadioButton.addActionListener(this);
        nounRadioButton = new JRadioButton(Translator.getTerm("NounCheckBox"));
        nounRadioButton.addActionListener(this);
        verbRadioButton = new JRadioButton(Translator.getTerm("VerbCheckBox"));
        verbRadioButton.addActionListener(this);
        otherRadioButton = new JRadioButton(Translator.getTerm("OtherPOSCheckBox"));
        otherRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(compoundWordRadioButton);
        group.add(nounRadioButton);
        group.add(verbRadioButton);
        group.add(otherRadioButton);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(searchField);
        buttonPanel.add(addTermButton);
        buttonPanel.add(compoundWordRadioButton);
        buttonPanel.add(nounRadioButton);
        buttonPanel.add(verbRadioButton);
        buttonPanel.add(otherRadioButton);

        wordInfoTable = new JTable();
        JScrollPane wordInfoTableScroll = new JScrollPane(wordInfoTable);
        wordInfoTableScroll.setPreferredSize(new Dimension(60, 60));
        wordInfoTableScroll.setMinimumSize(new Dimension(60, 60));
        setWordInfoTableModel();

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        southPanel.add(wordInfoTableScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentListScroll, documentPanel);
        splitPane.setDividerSize(DODDLEConstants.DIVIDER_SIZE);
        splitPane.setOneTouchExpandable(true);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void setWordInfoTableModel() {
        String WORD = Translator.getTerm("TermLabel");
        String POS = Translator.getTerm("POSLabel");
        String TF = Translator.getTerm("TFLabel");
        String IDF = Translator.getTerm("IDFLabel");
        String TFIDF = Translator.getTerm("TFIDFLabel");
        String UPPER_CONCEPT = Translator.getTerm("UpperConceptLabel");
        Object[] titles = new Object[]{WORD, POS, TF, IDF, TFIDF, UPPER_CONCEPT};

        wordInfoTableModel = new DefaultTableModel(null, titles);
        wordInfoTableModel.getColumnClass(0);

        wordInfoTable.setModel(wordInfoTableModel);
        wordInfoTable.getTableHeader().setToolTipText("sorted by column");
    }

    private final Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

    private void removeHighlights(JTextComponent jtc) {
        Highlighter highlight = jtc.getHighlighter();
        Highlighter.Highlight[] highlights = highlight.getHighlights();
        for (Highlighter.Highlight highlight1 : highlights) {
            if (highlight1.getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter) {
                highlight.removeHighlight(highlight1);
            }
        }
    }

    private void setHighlight(JTextComponent jtc, String pattern) {
        removeHighlights(jtc);
        try {
            Highlighter highlight = jtc.getHighlighter();
            javax.swing.text.Document doc = jtc.getDocument();
            String text = doc.getText(0, doc.getLength());
            int pos = 0;
            while ((pos = text.indexOf(pattern, pos)) >= 0) {
                highlight.addHighlight(pos, pos + pattern.length(), highlightPainter);
                pos += pattern.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    enum DODDLE_POS {
        COMPOUND_WORD, NOUN, VERB, OTHER
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == compoundWordRadioButton) {
            selectedPOS = DODDLE_POS.COMPOUND_WORD;
            setDocumentArea();
        } else if (e.getSource() == nounRadioButton) {
            selectedPOS = DODDLE_POS.NOUN;
            setDocumentArea();
        } else if (e.getSource() == verbRadioButton) {
            selectedPOS = DODDLE_POS.VERB;
            setDocumentArea();
        } else if (e.getSource() == otherRadioButton) {
            selectedPOS = DODDLE_POS.OTHER;
            setDocumentArea();
        } else if (e.getSource() == searchField) {
            if (searchField.getText().length() == 0) {
                removeHighlights(documentArea);
            } else {
                setHighlight(documentArea, searchField.getText());
            }
        } else if (e.getSource() == addTermButton) {
            addUserDefinedWord(searchField.getText());
        }
    }

    /**
     * 抽出できなかった入力単語をユーザ定義単語として追加する
     */
    private void addUserDefinedWord(String word) {
        if (word == null || word.length() == 0) {
            return;
        }
        word = word.replaceAll("<", "");
        word = word.replaceAll(">", "");
        StringBuilder basicWord = new StringBuilder();
        if (selectedDoc.getLang().equals("ja")) {
            Tokenizer tokenizer = new Tokenizer();
            List<Token> tokenList = tokenizer.tokenize(word);
            for (Token token : tokenList) {
                String bw = token.getBaseForm();
                if (bw.equals("*")) {
                    bw = token.getSurface();
                }
                basicWord.append(bw);
            }
        } else if (selectedDoc.getLang().equals("en")) {
            String[] words = word.split("\\s+");
            for (String word1 : words) {
                String basic = "";
                IndexWord indexWord = WordNet.getIndexWord(POS.NOUN, word1.toLowerCase());
                if (indexWord != null && indexWord.getLemma() != null) {
                    basic = indexWord.getLemma().toLowerCase();
                }
                if (basic.equals("")) {
                    indexWord = WordNet.getIndexWord(POS.VERB, word1.toLowerCase());
                    if (indexWord != null && indexWord.getLemma() != null) {
                        basic = indexWord.getLemma().toLowerCase();
                    }
                }
                if (basic.equals("")) {
                    basic = word1;
                }
                basicWord.append(basic).append(" ");
            }
            word = basicWord.toString();
        }
        String pos = Translator.getTerm("UserDefinedInputTermCheckBox");
        TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject().getInputTermSelectionPanel();
        TermInfo info = new TermInfo(word, 1);
        info.addPos(pos);
        info.putInputDoc(selectedDoc.getFile());
        termSelectionPanel.addInputTermInfo(info);
        setDocumentArea();
    }

    public void mouseClicked(MouseEvent e) {
        addUserDefinedWord(documentArea.getSelectedText());
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void setDocumentList(ListModel<Document> docListModel) {
        documentListModel.clear();
        for (int i = 0; i < docListModel.getSize(); i++) {
            documentListModel.add(i, docListModel.getElementAt(i));
        }
    }

    private boolean isNoun(String pos) {
        return pos.contains("名詞") || pos.toLowerCase().contains("noun");
    }

    private boolean isVerb(String pos) {
        return pos.contains("動詞") || pos.toLowerCase().contains("verb");
    }

    private boolean isCompoundWord(String pos) {
        return pos.contains(DocumentSelectionPanel.COMPOUND_WORD_JA)
                || pos.toLowerCase().contains(DocumentSelectionPanel.COMPOUND_WORD_EN);
    }

    private boolean isUserDefinedWord(String pos) {
        return pos.equals(Translator.getTerm("UserDefinedInputTermCheckBox"));
    }

    private boolean isOther(String pos) {
        return !(isNoun(pos) || isVerb(pos) || isCompoundWord(pos)) || isUserDefinedWord(pos);
    }

    private String getAddedSpaceText(String text) {
        text = text.replaceAll(",", " , ");
        text = text.replaceAll("．", " ． ");
        text = text.replaceAll("\"", " \" ");
        text = text.replaceAll("'", " ' ");
        return text;
    }

    private List<String> getEnBasicWordList(String text) {
        List<String> basicWordList = new ArrayList<>();
        String[] words = getAddedSpaceText(text).split("\\s+");
        for (String word : words) {
            String basic = "";

            IndexWord indexWord = WordNet.getIndexWord(POS.NOUN, word.toLowerCase());
            if (indexWord != null && indexWord.getLemma() != null) {
                basic = indexWord.getLemma().toLowerCase();
                basicWordList.add(basic);
            }

            if (basic.equals("")) {
                indexWord = WordNet.getIndexWord(POS.VERB, word.toLowerCase());
                if (indexWord != null && indexWord.getLemma() != null) {
                    basic = indexWord.getLemma().toLowerCase();
                    basicWordList.add(basic);
                }
            }
            if (basic.equals("")) {
                basicWordList.add(word.toLowerCase());
            }
        }
        return basicWordList;
    }

    private List<String> getJaBasicWordList(String text) {
        List<String> basicWordList = new ArrayList<>();
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokenList = tokenizer.tokenize(text);
        for (Token token : tokenList) {
            String basicForm = token.getBaseForm();
            if (basicForm.equals("*")) {
                basicForm = token.getSurface();
            }
            basicWordList.add(basicForm);
        }
        return basicWordList;
    }

    private List<String> getJaSurfaceWordList(String text) {
        List<String> surfaceList = new ArrayList<>();
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokenList = tokenizer.tokenize(text);
        for (Token token : tokenList) {
            surfaceList.add(token.getSurface());
        }
        return surfaceList;
    }

    private String highlightEnText(String text) {
        StringBuilder builder = new StringBuilder();
        String[] words = getAddedSpaceText(text).split("\\s+");
        for (String word : words) {
            String pos = "";
            String basic = "";
            if (selectedPOS == DODDLE_POS.NOUN) {
                IndexWord indexWord = WordNet.getIndexWord(POS.NOUN, word.toLowerCase());
                if (indexWord != null && indexWord.getLemma() != null) {
                    basic = indexWord.getLemma();
                    pos = "noun";
                }
            } else if (selectedPOS == DODDLE_POS.VERB) {
                IndexWord indexWord = WordNet.getIndexWord(POS.VERB, word.toLowerCase());
                if (indexWord != null && indexWord.getLemma() != null) {
                    basic = indexWord.getLemma();
                    pos = "verb";
                }
            }
            if (basic.equals("")) {
                basic = word;
            }
            TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject()
                    .getInputTermSelectionPanel();
            DocumentSelectionPanel docSelectionPanel = DODDLE_OWL.getCurrentProject()
                    .getDocumentSelectionPanel();
            if (!docSelectionPanel.isOneWordChecked() && basic.length() == 1) {
                builder.append(word);
                builder.append(" ");
                continue;
            }
            if (docSelectionPanel.isStopWord(basic)) {
                builder.append(word);
                builder.append(" ");
                continue;
            }

            TermInfo wordInfo = termSelectionPanel.getInputTermInfo(basic);
            TermInfo removedWordInfo = termSelectionPanel.getRemovedTermInfo(basic);
            String type = "";
            if (wordInfo != null) {
                type = "inputTerm";
            } else if (removedWordInfo != null) {
                type = "removedTerm";
            }

            if (type.equals("")) {
                builder.append(word);
            } else if (selectedPOS == DODDLE_POS.NOUN && isNoun(pos)) {
                builder.append(getHighlightWord(basic, word, type));
            } else if (selectedPOS == DODDLE_POS.VERB && isVerb(pos)) {
                builder.append(getHighlightWord(basic, word, type));
            } else {
                builder.append(word);
            }
            builder.append(" ");
        }
        return builder.toString();
    }

    private String highlightJaText(String text) {
        StringBuilder builder = new StringBuilder();
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokenList = tokenizer.tokenize(text);
        for (Token token : tokenList) {
            String basic = token.getBaseForm();
            if (basic.equals("*")) {
                basic = token.getSurface();
            }
            String pos = token.getPartOfSpeechLevel1();
            String word = token.getSurface();
            TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject()
                    .getInputTermSelectionPanel();
            DocumentSelectionPanel docSelectionPanel = DODDLE_OWL.getCurrentProject()
                    .getDocumentSelectionPanel();
            if (!docSelectionPanel.isOneWordChecked() && basic.length() == 1) {
                builder.append(word);
                continue;
            }
            if (docSelectionPanel.isStopWord(basic)) {
                builder.append(word);
                continue;
            }

            TermInfo termInfo = termSelectionPanel.getInputTermInfo(basic);
            TermInfo removedTermInfo = termSelectionPanel.getRemovedTermInfo(basic);
            String type = "";
            if (termInfo != null) {
                type = "inputTerm";
            } else if (removedTermInfo != null) {
                type = "removedTerm";
            }
            if (type.equals("")) {
                builder.append(word);
            } else if (selectedPOS == DODDLE_POS.NOUN && isNoun(pos)) {
                builder.append(getHighlightWord(basic, word, type));
            } else if (selectedPOS == DODDLE_POS.VERB && isVerb(pos)) {
                builder.append(getHighlightWord(basic, word, type));
            } else if (selectedPOS == DODDLE_POS.OTHER && isOther(pos)) {
                builder.append(getHighlightWord(basic, word, type));
            } else {
                builder.append(word);
            }
        }
        return builder.toString();
    }

    private String getHighlightWord(String basic, String word, String type) {
        String color;
        if (type.equals("inputTerm")) {
            color = CORRECT_WORD_LINK_COLOR;
        } else {
            color = REMOVED_WORD_LINK_COLOR;
        }
        return String.format("<a style='color: %s' href='%s:%s'>%s</a>", color, type, basic, word);
    }

    private Set<List<String>> getTermInfoEnCompoundWordSet(Collection<TermInfo> termInfoSet) {
        Set<List<String>> termInfoCompoundWordSet = new HashSet<>();
        for (TermInfo info : termInfoSet) {
            String word = info.getTerm();
            Set<String> posSet = info.getPosSet();
            for (String pos : posSet) {
                if (isCompoundWord(pos) || isUserDefinedWord(pos)) {
                    List<String> compoundWordList = new ArrayList<>();
                    String[] words = word.split("\\s+");
                    for (String word1 : words) {
                        String basic = "";
                        IndexWord indexWord = WordNet.getIndexWord(POS.NOUN, word1);
                        if (indexWord != null && indexWord.getLemma() != null) {
                            basic = indexWord.getLemma();
                        }
                        if (basic.equals("")) {
                            indexWord = WordNet.getIndexWord(POS.VERB, word1);
                            if (indexWord != null && indexWord.getLemma() != null) {
                                basic = indexWord.getLemma();
                            }
                        }
                        if (basic.equals("")) {
                            compoundWordList.add(word1);
                        } else {
                            compoundWordList.add(basic);
                        }
                    }
                    termInfoCompoundWordSet.add(compoundWordList);
                    break;
                }
            }
        }
        return termInfoCompoundWordSet;
    }

    private Set<List<String>> getTermInfoJaCompoundWordSet(Collection<TermInfo> termInfoSet) {
        Set<List<String>> termInfoCompoundWordSet = new HashSet<>();
        for (TermInfo info : termInfoSet) {
            String word = info.getTerm();
            Set<String> posSet = info.getPosSet();
            for (String pos : posSet) {
                if (isCompoundWord(pos) || isUserDefinedWord(pos)) {
                    List<String> compoundWordList = new ArrayList<>();
                    Tokenizer tokenizer = new Tokenizer();
                    List<Token> tokenList = tokenizer.tokenize(word);
                    for (Token token : tokenList) {
                        String bf = token.getBaseForm();
                        if (bf.equals("*")) {
                            bf = token.getSurface();
                        }
                        compoundWordList.add(bf);
                    }
                    termInfoCompoundWordSet.add(compoundWordList);
                    break;
                }
            }
        }
        return termInfoCompoundWordSet;
    }

    private String highlightEnCompoundWord(String text) {
        StringBuilder builder = new StringBuilder();
        List<String> basicStringList = getEnBasicWordList(text);

        TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject()
                .getInputTermSelectionPanel();
        Collection<TermInfo> wordInfoSet = termSelectionPanel.getTermInfoSet();
        Collection<TermInfo> removedWordInfoSet = termSelectionPanel.getRemovedTermInfoSet();

        Set<List<String>> termInfoCompoundWordSet = getTermInfoEnCompoundWordSet(wordInfoSet);
        Set<List<String>> removedTermInfoCompoundWordSet = getTermInfoEnCompoundWordSet(removedWordInfoSet);

        String[] texts = getAddedSpaceText(text).split("\\s+");
        for (int i = 0; i < basicStringList.size(); i++) {
            int compoundWordSize = getTermSize(basicStringList, termInfoCompoundWordSet, i);
            int removedCompoundWordSize = getTermSize(basicStringList,
                    removedTermInfoCompoundWordSet, i);
            StringBuilder word = new StringBuilder();
            StringBuilder basicWord = new StringBuilder();
            if (0 < compoundWordSize) {
                for (int j = i; j < i + compoundWordSize; j++) {
                    word.append(texts[j]);
                    basicWord.append(basicStringList.get(j));
                    if (j != i + compoundWordSize - 1) {
                        word.append(" ");
                        basicWord.append(" ");
                    }
                }
                builder.append(String.format("<a style='color: %s' href='inputTerm:%s'>%s</a>", CORRECT_WORD_LINK_COLOR, basicWord, word));
                i += compoundWordSize - 1;
            } else if (0 < removedCompoundWordSize) {
                for (int j = i; j < i + removedCompoundWordSize; j++) {
                    word.append(texts[j]);
                    basicWord.append(basicStringList.get(j));
                    if (j != i + removedCompoundWordSize - 1) {
                        word.append(" ");
                        basicWord.append(" ");
                    }
                }
                basicWord = new StringBuilder(basicWord.toString().replace("\\s+$", ""));
                builder.append(String.format("<a style='color: %s' href='removedTerm:%s'>%s</a>", REMOVED_WORD_LINK_COLOR, basicWord, word));
                i += removedCompoundWordSize - 1;
            } else {
                word = new StringBuilder(texts[i] + " ");
                builder.append(word);
            }
        }
        return builder.toString();
    }

    private String highlightJaCompoundWord(String text) {
        StringBuilder builder = new StringBuilder();
        List<String> basicStringList = getJaBasicWordList(text);

        TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject().getInputTermSelectionPanel();
        Collection<TermInfo> wordInfoSet = termSelectionPanel.getTermInfoSet();
        Collection<TermInfo> removedWordInfoSet = termSelectionPanel.getRemovedTermInfoSet();

        Set<List<String>> termInfoCompoundWordSet = getTermInfoJaCompoundWordSet(wordInfoSet);
        Set<List<String>> removedTermInfoCompoundWordSet = getTermInfoJaCompoundWordSet(removedWordInfoSet);

        List<String> surfaceList = getJaSurfaceWordList(text);
        for (int i = 0; i < basicStringList.size(); i++) {
            int compoundWordSize = getTermSize(basicStringList, termInfoCompoundWordSet, i);
            int removedCompoundWordSize = getTermSize(basicStringList, removedTermInfoCompoundWordSet, i);
            StringBuilder word = new StringBuilder();
            StringBuilder basicWord = new StringBuilder();
            if (0 < compoundWordSize) {
                for (int j = i; j < i + compoundWordSize; j++) {
                    word.append(surfaceList.get(j));
                    basicWord.append(basicStringList.get(j));
                }
                if (termSelectionPanel.getInputTermInfo(basicWord.toString()) != null) {
                    builder.append(String.format("<a style='color: %s' href='inputTerm:%s'>%s</a>", CORRECT_WORD_LINK_COLOR, basicWord, word));
                    i += compoundWordSize - 1;
                } else {
                    word = new StringBuilder(surfaceList.get(i));
                    builder.append(word);
                }
            } else if (0 < removedCompoundWordSize) {
                for (int j = i; j < i + removedCompoundWordSize; j++) {
                    word.append(surfaceList.get(j));
                    basicWord.append(basicStringList.get(j));
                }
                if (termSelectionPanel.getRemovedTermInfo(basicWord.toString()) != null) {
                    builder.append(String.format("<a style='color: %s' href='removedTerm:%s'>%s</a>", REMOVED_WORD_LINK_COLOR, basicWord, word));
                    i += removedCompoundWordSize - 1;
                } else {
                    word = new StringBuilder(surfaceList.get(i));
                    builder.append(word);
                }
            } else {
                word = new StringBuilder(surfaceList.get(i));
                builder.append(word);
            }
        }
        return builder.toString();
    }

    /**
     * @param basicStringList
     * @param termInfoCompoundWordSet
     * @param i
     * @return
     */
    private int getTermSize(List<String> basicStringList,
                            Set<List<String>> termInfoCompoundWordSet, int i) {
        int compoundWordSize = 0;
        for (List<String> compoundWordList : termInfoCompoundWordSet) {
            boolean isCompoundWord = true;
            for (int j = 0; j < compoundWordList.size(); j++) {
                if (!compoundWordList.get(j).equals(basicStringList.get(i + j))) {
                    isCompoundWord = false;
                    break;
                }
            }
            if (isCompoundWord) {
                if (compoundWordSize < compoundWordList.size()) {
                    compoundWordSize = compoundWordList.size();
                }
            }
        }
        return compoundWordSize;
    }

    private String getHighlightedString() {
        if (selectedDoc == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        String[] texts = selectedDoc.getTexts();
        for (int i = 0; i < selectedDoc.getSize(); i++) {
            text.append(texts[i]).append("<br>");
        }

        if (selectedPOS == DODDLE_POS.COMPOUND_WORD) {
            if (selectedDoc.getLang().equals("en")) {
                text = new StringBuilder(highlightEnCompoundWord(text.toString()));
            } else if (selectedDoc.getLang().equals("ja")) {
                text = new StringBuilder(highlightJaCompoundWord(text.toString()));
            }
        } else {
            if (selectedDoc.getLang().equals("en")) {
                text = new StringBuilder(highlightEnText(text.toString()));
            } else if (selectedDoc.getLang().equals("ja")) {
                text = new StringBuilder(highlightJaText(text.toString()));
            }
        }
        return text.toString();
    }

    public void setDocumentArea() {
        if (documentListModel == null) {
            return;
        }
        StringBuilder docBuilder = new StringBuilder();
        docBuilder.append(HTML_START);
        String text = getHighlightedString();
        docBuilder.append(text);
        docBuilder.append("<br><br>");
        docBuilder.append(HTML_END);
        setDocumentArea(docBuilder.toString());
    }

    private void setDocumentArea(String text) {
        int p = documentArea.getCaretPosition();
        documentArea.setText(text);
        try {
            documentArea.setCaretPosition(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isRegisteredWord(String word, String type) {
        TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject()
                .getInputTermSelectionPanel();
        if (type.equals("inputTerm")) {
            return termSelectionPanel.getInputTermInfo(word) != null;
        }
        return termSelectionPanel.getRemovedTermInfo(word) != null;
    }

    private String getSelectedLinkText(String type, String word) {
        if (!isRegisteredWord(word, type)) {
            if (selectedDoc.getLang().equals("ja")) {
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < selectedDoc.getSize(); i++) {
                    text.append(selectedDoc.getTexts()[i]).append("<br>");
                }
                Tokenizer tokenizer = new Tokenizer();
                List<Token> tokenList = tokenizer.tokenize(text.toString());
                for (Token token : tokenList) {
                    if (token.getSurface().equals(word)) {
                        String basic = token.getBaseForm();
                        if (basic.equals("*")) {
                            basic = token.getSurface();
                        }
                        if (isRegisteredWord(basic, type)) {
                            word = basic;
                            break;
                        }
                    }
                }
            } else if (selectedDoc.getLang().equals("en")) {
                String[] words = word.split("\\s+");
                StringBuilder basicWord = new StringBuilder();
                for (String word1 : words) {
                    String basic = "";
                    IndexWord indexWord = WordNet.getIndexWord(POS.NOUN, word1.toLowerCase());
                    if (indexWord != null && indexWord.getLemma() != null) {
                        basic = indexWord.getLemma();
                    }
                    if (basic.equals("")) {
                        indexWord = WordNet.getIndexWord(POS.VERB, word1.toLowerCase());
                        if (indexWord != null && indexWord.getLemma() != null) {
                            basic = indexWord.getLemma();
                        }
                    }
                    if (basic.equals("")) {
                        basic = word1;
                    }
                    basicWord.append(basic).append(" ");
                }
                word = basicWord.toString();
            }
        }
        return word;
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String[] descriptions = e.getDescription().split(":");
            StringBuilder builder = new StringBuilder();
            builder.append(HTML_START);
            if (descriptions.length == 1) {
                builder.append(getHighlightedString());
            } else {
                String type = descriptions[0];
                String word = descriptions[1];
                word = getSelectedLinkText(type, word);
                TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject()
                        .getInputTermSelectionPanel();
                if (type.equals("inputTerm")) {
                    termSelectionPanel.removeTerm(word);
                } else {
                    termSelectionPanel.addTerm(word);
                }
                builder.append(getHighlightedString());
            }
            builder.append(HTML_END);
            setDocumentArea(builder.toString());
            if (descriptions.length == 1) {
                documentArea.setCaretPosition(0);
            }
            repaint();
        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            String[] descriptions = e.getDescription().split(":");
            if (descriptions.length == 2) {
                String type = descriptions[0];
                String word = descriptions[1];
                word = getSelectedLinkText(type, word);
                TermSelectionPanel termSelectionPanel = DODDLE_OWL.getCurrentProject()
                        .getInputTermSelectionPanel();
                TermInfo info;
                if (type.equals("inputTerm")) {
                    info = termSelectionPanel.getInputTermInfo(word);
                } else {
                    info = termSelectionPanel.getRemovedTermInfo(word);
                }
                if (0 < wordInfoTableModel.getRowCount()) {
                    wordInfoTableModel.removeRow(0);
                }
                if (info != null) {
                    wordInfoTableModel.addRow(info.getRowData());
                }
            }
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
            if (0 < wordInfoTableModel.getRowCount()) {
                wordInfoTableModel.removeRow(0);
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (searchField.getText().length() == 0) {
            removeHighlights(documentArea);
        } else {
            setHighlight(documentArea, searchField.getText());
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
