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

package io.github.doddle_owl.views.document_selection;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import org.apache.commons.io.FileUtils;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.ProjectFileNames;
import io.github.doddle_owl.models.document_selection.Document;
import io.github.doddle_owl.models.ontology_api.WordNet;
import io.github.doddle_owl.models.term_selection.TermInfo;
import io.github.doddle_owl.task_analyzer.CabochaDocument;
import io.github.doddle_owl.task_analyzer.TaskAnalyzer;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.UpperConceptManager;
import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.views.term_selection.TermSelectionPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * @author Takeshi Morita
 */
public class DocumentSelectionPanel extends JPanel implements ListSelectionListener, ActionListener {

    private Set<String> stopWordSet;

    private final JList<Document> documentList;
    private final DefaultListModel<Document> documentListModel;

    private final JComboBox<String> documentLangComboBox;

    private final JCheckBox genSenCheckBox;
    private final JCheckBox cabochaCheckBox;
    private final JCheckBox nounCheckBox;
    private final JCheckBox verbCheckBox;
    private final JCheckBox otherCheckBox;
    private final JCheckBox oneWordCheckBox;

    private final JTextField punctuationField;
    private final JButton setPunctuationButton;
    private final JButton termExtractionButton;

    public static String PUNCTUATION_CHARS = "．|。|\\.";
    public static final String COMPOUND_WORD_JA = "複合語";
    public static final String COMPOUND_WORD_EN = "Compound Word";

    private final ImageIcon addDocIcon = Utils.getImageIcon("baseline_add_circle_black_18dp.png");
    private final ImageIcon removeDocIcon = Utils.getImageIcon("baseline_remove_circle_black_18dp.png");

    private TaskAnalyzer taskAnalyzer;

    private final JTextArea documentTextArea;
    private Map<String, TermInfo> termInfoMap;

    private final TermSelectionPanel termSelectionPanel;
    private final DODDLEProjectPanel project;

    private Process jaMorphologicalAnalyzerProcess;
    private Process termExtractProcess;
    public static String Japanese_Morphological_Analyzer = "C:/Program Files/Chasen/chasen.exe";
    public static String Japanese_Dependency_Structure_Analyzer = "C:/Program Files/CaboCha/bin/cabocha.exe";
    public static String PERL_EXE = "C:/Perl/bin/perl.exe";
    public static String TERM_EXTRACT_SCRIPTS_DIR = "TermExtractScripts" + File.separator;
    public static String STOP_WORD_LIST_FILE = "C:/DODDLE-OWL/stop_word_list.txt";

    public void initialize() {
        if (termInfoMap == null) {
            termInfoMap = new HashMap<>();
        } else {
            termInfoMap.clear();
        }
        if (stopWordSet == null) {
            stopWordSet = new HashSet<>();
        } else {
            stopWordSet.clear();
        }
        documentListModel.clear();
        genSenCheckBox.setSelected(false);
        cabochaCheckBox.setSelected(false);
        nounCheckBox.setSelected(true);
        verbCheckBox.setSelected(false);
        otherCheckBox.setSelected(false);
        oneWordCheckBox.setSelected(false);
        punctuationField.setText(PUNCTUATION_CHARS);
        documentTextArea.setText("");
    }

    public DocumentSelectionPanel(TermSelectionPanel iwsPanel, DODDLEProjectPanel p) {
        project = p;
        termSelectionPanel = iwsPanel;
        documentListModel = new DefaultListModel<>();
        documentList = new JList<>(documentListModel);
        documentList.addListSelectionListener(this);
        var documentListScroll = new JScrollPane(documentList);

        DefaultComboBoxModel<String> documentLangComboBoxModel = new DefaultComboBoxModel<>(new String[]{"en", "ja"});
        documentLangComboBox = new JComboBox<>(documentLangComboBoxModel);
        documentLangComboBox.addActionListener(this);
        var addDocumentButton = new JButton(new AddDocumentAction(Translator.getTerm("AddInputDocumentButton")));
        var removeDocumentButton = new JButton(new RemoveDocumentAction(Translator.getTerm("RemoveInputDocumentButton")));

        documentTextArea = new JTextArea();
        documentTextArea.setLineWrap(true);
        var documentTextAreaScroll = new JScrollPane(documentTextArea);
        documentTextAreaScroll.setBorder(new TitledBorder(Translator.getTerm("InputDocumentArea")));

        punctuationField = new JTextField(10);
        setPunctuationButton = new JButton(Translator.getTerm("SetPunctuationCharacterButton"));
        setPunctuationButton.addActionListener(this);

        var punctuationPanel = new JPanel();
        punctuationPanel.add(punctuationField);
        punctuationPanel.add(setPunctuationButton);

        var documentButtonPanel = new JPanel();
        documentButtonPanel.setLayout(new GridLayout(1, 3));
        documentButtonPanel.add(documentLangComboBox);
        documentButtonPanel.add(addDocumentButton);
        documentButtonPanel.add(removeDocumentButton);

        var southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        southPanel.add(documentButtonPanel, BorderLayout.WEST);
        southPanel.add(punctuationPanel, BorderLayout.EAST);

        var documentPanel = new JPanel();
        documentPanel.setLayout(new BorderLayout());
        documentPanel.add(documentListScroll, BorderLayout.CENTER);
        documentPanel.add(southPanel, BorderLayout.SOUTH);
        documentPanel.setBorder(new TitledBorder(Translator.getTerm("InputDocumentList")));

        termExtractionButton = new JButton(Translator.getTerm("InputTermExtractionButton"));
        termExtractionButton.addActionListener(this);

        genSenCheckBox = new JCheckBox(Translator.getTerm("GensenCheckBox"));
        cabochaCheckBox = new JCheckBox(Translator.getTerm("CabochaCheckBox"));
        nounCheckBox = new JCheckBox(Translator.getTerm("NounCheckBox"));
        verbCheckBox = new JCheckBox(Translator.getTerm("VerbCheckBox"));
        otherCheckBox = new JCheckBox(Translator.getTerm("OtherPOSCheckBox"));
        oneWordCheckBox = new JCheckBox(Translator.getTerm("OneCharacterCheckBox"));

        var morphemeAnalysisPanel = new JPanel();
        morphemeAnalysisPanel.add(genSenCheckBox);
        morphemeAnalysisPanel.add(cabochaCheckBox);
        morphemeAnalysisPanel.add(nounCheckBox);
        morphemeAnalysisPanel.add(verbCheckBox);
        morphemeAnalysisPanel.add(otherCheckBox);
        morphemeAnalysisPanel.add(oneWordCheckBox);

        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(morphemeAnalysisPanel, BorderLayout.WEST);
        buttonPanel.add(termExtractionButton, BorderLayout.EAST);

        var documentSelectionPanelSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        documentSelectionPanelSplitPane.add(documentPanel);
        documentSelectionPanelSplitPane.add(documentTextAreaScroll);

        initialize();
        setLayout(new BorderLayout());
        add(documentSelectionPanelSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setStopWordSet() {
        stopWordSet.clear();
        try {
            Path stopWordListFilePath = Paths.get(STOP_WORD_LIST_FILE);
            if (Files.notExists(stopWordListFilePath)) {
                return;
            }
            stopWordSet = Files.lines(stopWordListFilePath).collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isOneWordChecked() {
        return oneWordCheckBox.isSelected();
    }

    public boolean isStopWord(String w) {
        return stopWordSet.contains(w);
    }

    public void saveDocumentInfo(File saveDir) {
        File docInfo = new File(saveDir, ProjectFileNames.DOC_INFO_FILE);
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(docInfo.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                for (int i = 0; i < documentListModel.getSize(); i++) {
                    Document doc = documentListModel.getElementAt(i);
                    writer.write("inputDoc,");
                    writer.write(doc.getFile().getAbsolutePath());
                    writer.write(",");
                    writer.write(doc.getLang());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDocuments(File openDir) {
        File docInfo = new File(openDir, ProjectFileNames.DOC_INFO_FILE);
        if (!docInfo.exists()) {
            return;
        }
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(docInfo.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] info = line.split(",");
                    if (info.length != 3) {
                        continue;
                    }
                    String type = info[0];
                    String fileName = info[1];
                    String lang = info[2];
                    if (type.equals("inputDoc")) {
                        documentListModel.addElement(new Document(lang, new File(fileName)));
                    }
                }
            }
            termSelectionPanel.setInputDocumentListModel(documentListModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setTermInfoMap(String term, String pos, File doc) {
        TermInfo info;
        if (termInfoMap.get(term) != null) {
            info = termInfoMap.get(term);
        } else {
            int docNum = documentListModel.getSize();
            info = new TermInfo(term, docNum);
        }
        if (!(pos.equals(COMPOUND_WORD_EN) || pos.equals(COMPOUND_WORD_JA))) {
            info.addPos(pos);
        } else if (info.getPosSet().size() == 0) {
            info.addPos(pos);
        }
        info.putInputDoc(doc);
        termInfoMap.put(term, info);
    }

    private void setTermInfo(String term, String pos, String basicStr, File file) {
        if (nounCheckBox.isSelected() && isEnNoun(pos)) {
            IndexWord indexWord = WordNet.getIndexWord(POS.NOUN, term.toLowerCase());
            if (indexWord != null && indexWord.getLemma() != null) {
                basicStr = indexWord.getLemma();
            }
            setTermInfoMap(basicStr, pos, file);
        } else if (verbCheckBox.isSelected() && isEnVerb(pos)) {
            IndexWord indexWord = WordNet.getIndexWord(POS.VERB, term.toLowerCase());
            if (indexWord != null && indexWord.getLemma() != null) {
                basicStr = indexWord.getLemma();
            }
            setTermInfoMap(basicStr, pos, file);
        } else if (otherCheckBox.isSelected() && isEnOther(pos)) {
            setTermInfoMap(basicStr, pos, file);
        }
    }

    private void enTermExtraction(Document doc) {
        File file = doc.getFile();
        var taggerModel = DODDLE_OWL.class.getClassLoader().getResourceAsStream("pos_tagger_model/english-left3words-distsim.tagger");
        var tagger = new MaxentTagger(taggerModel);

        Path taggerOutPath = Paths.get(DODDLEConstants.PROJECT_HOME + File.separator + "tmpTagger.txt");
        try (var writer = Files.newBufferedWriter(taggerOutPath)) {
            try (var reader = Files.newBufferedReader(Paths.get(doc.getFile().getAbsolutePath()))) {
                List<List<HasWord>> sentenceList = MaxentTagger.tokenizeText(reader);
                for (List<HasWord> sentence : sentenceList) {
                    List<TaggedWord> taggedWordList = tagger.tagSentence(sentence);
                    for (TaggedWord tw : taggedWordList) {
                        String word = tw.word();
                        String pos = tw.tag();
                        String basicStr = word.toLowerCase();
                        if (!oneWordCheckBox.isSelected() && basicStr.length() == 1) {
                            continue;
                        }
                        if (isStopWord(basicStr)) {
                            continue;
                        }
                        setTermInfo(word, pos, basicStr, file);
                    }
                    writer.write(SentenceUtils.listToString(taggedWordList, false));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (genSenCheckBox.isSelected()) {
            Set<String> comoundWordSet = getGensenCompoundWordSet(doc);
            for (String compoundWord : comoundWordSet) {
                setTermInfoMap(compoundWord, COMPOUND_WORD_EN, file);
            }
        }
    }

    private void jaTermExtraction(Document doc) {
        File file = doc.getFile();
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokenList = tokenizer.tokenize(doc.getText());
        for (Token token : tokenList) {
            String pos = token.getPartOfSpeechLevel1();
            String basicStr = token.getBaseForm();
            if (basicStr.equals("*")) {
                basicStr = token.getSurface();
            }
            if (!oneWordCheckBox.isSelected() && basicStr.length() == 1) {
                continue;
            }
            if (isStopWord(basicStr)) {
                continue;
            }
            if (nounCheckBox.isSelected() && isJaNoun(pos)) {
                setTermInfoMap(basicStr, pos, file);
            } else if (verbCheckBox.isSelected() && isJaVerb(pos)) {
                setTermInfoMap(basicStr, pos, file);
            } else if (otherCheckBox.isSelected() && isJaOther(pos)) {
                setTermInfoMap(basicStr, pos, file);
            }
        }
        if (genSenCheckBox.isSelected()) {
            Set<String> compoundWordSet = getGensenCompoundWordSet(doc);
            for (String compoundWord : compoundWordSet) {
                setTermInfoMap(compoundWord, COMPOUND_WORD_JA, file);
            }
        }
        if (cabochaCheckBox.isSelected()) {
            CabochaDocument cabochaDoc = taskAnalyzer.loadUseCaseTask(doc.getFile());
            setWordInfoForCabochaDoc(cabochaDoc.getCompoundWordCountMap(), doc);
            setWordInfoForCabochaDoc(cabochaDoc.getCompoundWordWithNokakuCountMap(), doc);
        }
    }

    private void setWordInfoForCabochaDoc(Map<String, Integer> map, Document doc) {
        for (Entry<String, Integer> entry : map.entrySet()) {
            String compoundWord = entry.getKey();
            int count = entry.getValue();
            setTermInfoMap(compoundWord, COMPOUND_WORD_JA, doc.getFile());
            TermInfo wordInfo = termInfoMap.get(compoundWord);
            wordInfo.putInputDoc(doc.getFile(), count);
        }
    }

    private void termExtraction(Document doc) {
        switch (doc.getLang()) {
            case "ja":
                jaTermExtraction(doc);
                break;
            case "en":
                enTermExtraction(doc);
                break;
        }
    }


    public void destroyProcesses() {
        if (jaMorphologicalAnalyzerProcess != null) {
            jaMorphologicalAnalyzerProcess.destroy();
        }
        if (termExtractProcess != null) {
            termExtractProcess.destroy();
        }
        if (taskAnalyzer != null) {
            taskAnalyzer.destroyProcess();
        }
    }

    private Set<String> getGensenCompoundWordSet(Document doc) {
        String lang = doc.getLang();
        Set<String> wordSet = new HashSet<>();
        BufferedReader reader = null;
        try {
            if (lang.equals("ja")) {
                reader = getJaGenSenReader(doc.getText());
            } else if (lang.equals("en")) {
                reader = getEnGensenReader();
            }
            String line;
            String splitStr = "\\s+";
            if (lang.equals("en")) {
                splitStr = "\t";
            }
            while ((line = reader.readLine()) != null) { // reader.ready()は使えない
                String[] lines = line.split(splitStr);
                if (lines.length < 2) {
                    continue;
                }
                String word = lines[0].toLowerCase();
                if (lang.equals("en")) {
                    word = word.replaceAll("\\s+", " ");
                }
                if (lang.equals("en") && word.split("\\s+").length == 1) {
                    continue;
                }
                if (!oneWordCheckBox.isSelected() && word.length() == 1) {
                    continue;
                }
                wordSet.add(word);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
            deleteTempFiles();
        }
        return wordSet;
    }

    private BufferedReader getEnGensenReader() throws IOException {
        File dir = new File(TERM_EXTRACT_SCRIPTS_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String TERM_EXTRACT_TAGGER_PL = "ex_brillstagger.pl";
        String taggerPath = TERM_EXTRACT_SCRIPTS_DIR + File.separator + TERM_EXTRACT_TAGGER_PL;
        File scriptFile = new File(taggerPath);
        if (!scriptFile.exists()) {
            URL url = DODDLE_OWL.class.getClassLoader().getResource("TermExtractScripts/" + TERM_EXTRACT_TAGGER_PL);
            if (url != null) {
                FileUtils.copyURLToFile(url, scriptFile);
                // System.out.println("copy: " + scriptFile.getAbsolutePath());
            }
        }
        ProcessBuilder processBuilder = new ProcessBuilder(PERL_EXE, taggerPath,
                DODDLEConstants.PROJECT_HOME + File.separator + "tmpTagger.txt");
        termExtractProcess = processBuilder.start();
        return new BufferedReader(new InputStreamReader(termExtractProcess.getInputStream(), StandardCharsets.UTF_8));
    }

    private File tmpFile;
    private File tmpJapaneseMorphologicalAnalyzerFile;

    private void deleteTempFiles() {
        if (tmpFile != null) {
            tmpFile.deleteOnExit();
        }
        if (tmpJapaneseMorphologicalAnalyzerFile != null) {
            tmpJapaneseMorphologicalAnalyzerFile.deleteOnExit();
        }
    }

    /**
     * @param text
     * @return
     * @throws IOException
     */
    private BufferedReader getJaGenSenReader(String text) throws IOException {
        tmpFile = File.createTempFile("tmp", null);
        String japanese_Morphological_Analyzer_CharacterSet = "UTF-8";
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tmpFile), japanese_Morphological_Analyzer_CharacterSet));
        bw.write(text);
        bw.close();

        tmpJapaneseMorphologicalAnalyzerFile = File.createTempFile("tmpJpMorphologicalAnalyzer", null);
        ProcessBuilder processBuilder;
        if (Japanese_Morphological_Analyzer.matches(".*mecab.*")) {
            processBuilder = new ProcessBuilder(Japanese_Morphological_Analyzer, "-o",
                    tmpJapaneseMorphologicalAnalyzerFile.getAbsolutePath(),
                    tmpFile.getAbsolutePath());
        } else {
            processBuilder = new ProcessBuilder(Japanese_Morphological_Analyzer, "-i", "w", "-o",
                    tmpJapaneseMorphologicalAnalyzerFile.getAbsolutePath(),
                    tmpFile.getAbsolutePath());
        }

        jaMorphologicalAnalyzerProcess = processBuilder.start();
        String TERM_EXTRACT_CHASEN_PL = "ex_chasen.pl";
        String TERM_EXTRACT_EXE = TERM_EXTRACT_CHASEN_PL;
        if (Japanese_Morphological_Analyzer.matches(".*mecab.*")) {
            String TERM_EXTRACT_MECAB_PL = "ex_mecab.pl";
            TERM_EXTRACT_EXE = TERM_EXTRACT_MECAB_PL;
        }
        File dir = new File(TERM_EXTRACT_SCRIPTS_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String path = TERM_EXTRACT_SCRIPTS_DIR + File.separator + TERM_EXTRACT_EXE;
        File scriptFile = new File(path);
        if (!scriptFile.exists()) {
            URL url = DODDLE_OWL.class.getClassLoader().getResource("TermExtractScripts/" + TERM_EXTRACT_EXE);
            if (url != null) {
                FileUtils.copyURLToFile(url, scriptFile);
                // System.out.println("copy: " + scriptFile.getAbsolutePath());
            }
        }
        processBuilder = new ProcessBuilder(PERL_EXE, path,
                tmpJapaneseMorphologicalAnalyzerFile.getAbsolutePath());
        termExtractProcess = processBuilder.start();
        return new BufferedReader(new InputStreamReader(termExtractProcess.getInputStream(),
                japanese_Morphological_Analyzer_CharacterSet));
    }

    private boolean isJaNoun(String pos) {
        return pos.indexOf("名詞") == 0;
    }

    private boolean isJaVerb(String pos) {
        return pos.indexOf("動詞") == 0;
    }

    private boolean isJaOther(String pos) {
        return !(isJaNoun(pos) || isJaVerb(pos));
    }

    private boolean isEnNoun(String pos) {
        return pos.contains("NN");
    }

    private boolean isEnVerb(String pos) {
        return pos.contains("VB");
    }

    private boolean isEnOther(String pos) {
        return !(isEnNoun(pos) || isEnVerb(pos));
    }

    private void setUpperConcept() {
        if (!UpperConceptManager.hasUpperConceptLabelSet()) {
            return;
        }
        for (Entry<String, TermInfo> entry : termInfoMap.entrySet()) {
            String term = entry.getKey();
            TermInfo info = entry.getValue();
            Set<String> upperConceptLabelSet = UpperConceptManager.getUpperConceptLabelSet(term);
            if (info != null) {
                for (String ucLabel : upperConceptLabelSet) {
                    info.addUpperConcept(ucLabel);
                }
            }
        }
    }

    class TermExtractionWorker extends SwingWorker<String, String> implements PropertyChangeListener {

        private int currentTaskCnt;

        TermExtractionWorker(int taskCnt) {
            currentTaskCnt = 1;
            DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("InputTermExtractionButton"));
            DODDLE_OWL.STATUS_BAR.startTime();
            DODDLE_OWL.STATUS_BAR.initNormal(taskCnt);
            DODDLE_OWL.STATUS_BAR.lock();
            addPropertyChangeListener(this);
            setStopWordSet();
        }

        @Override
        protected String doInBackground() {
            try {
                taskAnalyzer = new TaskAnalyzer();
                UpperConceptManager.makeUpperOntologyList();
                setProgress(currentTaskCnt++);
                termInfoMap.clear();
                for (int i = 0; i < documentListModel.getSize(); i++) {
                    Document doc = documentListModel.getElementAt(i);
                    DODDLE_OWL.STATUS_BAR.setLastMessage(doc.getFile().getName());
                    termExtraction(doc);
                }

                setProgress(currentTaskCnt++);
                setUpperConcept();
                removeDocWordSet();
                int docNum = documentListModel.getSize();
                termSelectionPanel.setInputTermInfoTableModel(termInfoMap, docNum);
                termSelectionPanel.setInputDocumentListModel(documentListModel);
                setProgress(currentTaskCnt++);
                DODDLE_OWL.setSelectedIndex(DODDLEConstants.INPUT_WORD_SELECTION_PANEL);
                setProgress(currentTaskCnt++);
            } finally {
                DODDLE_OWL.STATUS_BAR.setLastMessage(Translator
                        .getTerm("InputTermExtractionDoneMessage"));
                DODDLE_OWL.STATUS_BAR.unLock();
                DODDLE_OWL.STATUS_BAR.hideProgressBar();
                project.addLog("TermExtractionButton");
            }
            return "done";
        }

        @Override
        public void done() {
            destroyProcesses();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE_OWL.STATUS_BAR.setValue(currentTaskCnt);
            }
        }

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == termExtractionButton) {
            destroyProcesses(); // 連続でボタンを押された場合の処理
            TermExtractionWorker worker = new TermExtractionWorker(4);
            DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        } else if (e.getSource() == documentLangComboBox) {
            if (documentList.getSelectedValuesList().size() == 1) {
                String lang = (String) documentLangComboBox.getSelectedItem();
                Document doc = documentList.getSelectedValue();
                doc.setLang(lang);
                updateUI();
            }
        } else if (e.getSource() == setPunctuationButton) {
            PUNCTUATION_CHARS = punctuationField.getText();
            for (int i = 0; i < documentListModel.getSize(); i++) {
                Document doc = documentListModel.getElementAt(i);
                doc.resetText();
            }
            Document doc = documentList.getSelectedValue();
            if (doc != null) {
                documentTextArea.setText(doc.getText());
                documentTextArea.setCaretPosition(0);
                documentLangComboBox.setSelectedItem(doc.getLang());
            }
            updateUI();
        }
    }

    private void removeDocWordSet() {
        Set<String> docWordSet = new HashSet<>();
        for (TermInfo info : termInfoMap.values()) {
            if (!info.isInputWord()) {
                docWordSet.add(info.getTerm());
            }
        }
        for (String dw : docWordSet) {
            termInfoMap.remove(dw);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == documentList && documentList.getSelectedValuesList().size() == 1) {
            Document doc = documentList.getSelectedValue();
            documentTextArea.setText(doc.getText());
            documentTextArea.setCaretPosition(0);
            documentLangComboBox.setSelectedItem(doc.getLang());
        }
    }

    private Set getFiles(File[] files, Set fileSet) {
        for (File file : files) {
            if (file.isFile()) {
                fileSet.add(file);
            } else if (file.isDirectory()) {
                getFiles(file.listFiles(), fileSet);
            }
        }
        return fileSet;
    }

    private Set getFiles() {
        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File[] files = chooser.getSelectedFiles();
        Set fileSet = new TreeSet();
        getFiles(files, fileSet);
        return fileSet;
    }

    public String getTargetHtmlLines(String word) {
        StringWriter writer = new StringWriter();
        writer.write("<html><body style='line-height: 1.2em; font-size: 12px;'>");
        ListModel listModel = documentListModel;
        for (int i = 0; i < listModel.getSize(); i++) {
            Document doc = (Document) listModel.getElementAt(i);
            String text = doc.getText();
            StringBuilder buf = new StringBuilder();
            if (text != null) {
                String[] lines = text.split("\\r\\n|\\n|\\r");
                for (int j = 0; j < lines.length; j++) {
                    String line = lines[j];
                    try {
                        if (line.contains(word)) {
                            line = line.replaceAll(word, String.format("<span style='color: #1111cc; font-weight: bold;'>%s</span>", word));
                            buf.append(String.format("<span style='font-weight: bold;'>%d: </span>", (j + 1)));
                            buf.append(line);
                            buf.append("<br>");
                        }
                    } catch (PatternSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (0 < buf.toString().length()) {
                writer.write(String.format("<div style='font-weight: bold;'>%s</div>", doc.getFile().getAbsolutePath()));
                writer.write(buf.toString());
            }
        }
        writer.write("</body></html>");
        return writer.toString();
    }

    private void addDocuments(JList<Document> list, Set fileSet) {
        DefaultListModel model = (DefaultListModel) list.getModel();
        for (Object o : fileSet) {
            File file = (File) o;
            Document doc = new Document(file);
            doc.setLang("en");
            try {
                List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
                LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                        .withProfiles(languageProfiles)
                        .build();
                TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
                TextObject textObject = textObjectFactory.forText(doc.getText());
                Optional<LdLocale> lang = languageDetector.detect(textObject);
                if (lang.isPresent()) {
                    doc.setLang(lang.get().getLanguage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            model.addElement(doc);
        }
    }

    class AddDocumentAction extends AbstractAction {
        AddDocumentAction(String title) {
            super(title, addDocIcon);
        }

        public void actionPerformed(ActionEvent e) {
            Set fileSet = getFiles();
            if (fileSet == null) {
                return;
            }
            addDocuments(documentList, fileSet);
            project.getConceptDefinitionPanel().setInputDocList();
            project.addLog("AddInputDocumentButton");
        }
    }

    class RemoveDocumentAction extends AbstractAction {
        RemoveDocumentAction(String title) {
            super(title, removeDocIcon);
        }

        public void actionPerformed(ActionEvent e) {
            List<Document> removeElements = documentList.getSelectedValuesList();
            for (Document removeElement : removeElements) {
                documentListModel.removeElement(removeElement);
            }
            documentTextArea.setText("");
            project.getConceptDefinitionPanel().setInputDocList();
            project.addLog("RemoveInputDocumentButton");
        }
    }

    public Set<Document> getDocSet() {
        TreeSet<Document> docSet = new TreeSet<>();
        for (int i = 0; i < documentListModel.getSize(); i++) {
            docSet.add(documentListModel.getElementAt(i));
        }
        return docSet;
    }
}
