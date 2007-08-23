package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.taskanalyzer.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.didion.jwnl.data.*;
import net.infonode.docking.*;
import net.infonode.docking.util.*;
import net.java.sen.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 */
public class DocumentSelectionPanel extends JPanel implements ListSelectionListener, ActionListener {

    private Set<String> stopWordSet;

    private JList docList;
    private JList inputDocList;

    private JComboBox docLangBox;
    private JButton addDocButton;
    private JButton removeDocButton;
    private JComboBox inputDocLangBox;
    private JButton addInputDocButton;
    private JButton removeInputDocButton;
    private JButton editInputDocButton;

    private JButton analyzeMorphemeButton;
    private JCheckBox genSenCheckBox;
    private JCheckBox cabochaCheckBox;
    private JCheckBox showImportanceCheckBox;
    private JCheckBox nounCheckBox;
    private JCheckBox verbCheckBox;
    private JCheckBox otherCheckBox;
    private JCheckBox oneWordCheckBox;

    private JTextField punctuationField;
    private JButton setPunctuationButton;

    public static String PUNCTUATION_CHARS = "．|。|\\.";

    private TaskAnalyzer taskAnalyzer;

    private JTextArea inputDocArea;
    private Map<String, WordInfo> wordInfoMap;

    private InputWordSelectionPanel inputWordSelectionPanel;
    private DODDLEProject project;

    private View[] mainViews;
    private RootWindow rootWindow;

    public DocumentSelectionPanel(InputWordSelectionPanel iwsPanel, DODDLEProject p) {
        project = p;
        inputWordSelectionPanel = iwsPanel;
        System.setProperty("sen.home", DODDLEConstants.SEN_HOME);
        setStopWordSet();
        wordInfoMap = new HashMap<String, WordInfo>();
        docList = new JList(new DefaultListModel());
        docList.addListSelectionListener(this);
        JScrollPane docListScroll = new JScrollPane(docList);
        inputDocList = new JList(new DefaultListModel());
        inputDocList.addListSelectionListener(this);
        JScrollPane inputDocListScroll = new JScrollPane(inputDocList);

        DefaultComboBoxModel docLangBoxModel = new DefaultComboBoxModel(new Object[] { "en", "ja"});
        docLangBox = new JComboBox(docLangBoxModel);
        docLangBox.addActionListener(this);
        addDocButton = new JButton(new AddDocAction(Translator.getTerm("AddDocumentButton")));
        removeDocButton = new JButton(new RemoveDocAction(Translator.getTerm("RemoveDocumentButton")));
        DefaultComboBoxModel inputDocLangBoxModel = new DefaultComboBoxModel(new Object[] { "en", "ja"});
        inputDocLangBox = new JComboBox(inputDocLangBoxModel);
        inputDocLangBox.addActionListener(this);
        addInputDocButton = new JButton(new AddInputDocAction(Translator.getTerm("AddInputDocumentButton")));
        removeInputDocButton = new JButton(new RemoveInputDocAction(Translator.getTerm("RemoveInputDocumentButton")));
        editInputDocButton = new JButton(new EditInputDocAction(Translator.getTerm("EditInputDocumentButton")));
        inputDocArea = new JTextArea();
        inputDocArea.setLineWrap(true);
        JScrollPane inputDocAreaScroll = new JScrollPane(inputDocArea);

        JPanel docButtonPanel = new JPanel();
        docButtonPanel.setLayout(new BorderLayout());
        docButtonPanel.setLayout(new GridLayout(1, 3));
        docButtonPanel.add(docLangBox);
        docButtonPanel.add(addDocButton);
        docButtonPanel.add(removeDocButton);
        JPanel docPanel = new JPanel();
        docPanel.setLayout(new BorderLayout());
        docPanel.add(docListScroll, BorderLayout.CENTER);
        docPanel.add(docButtonPanel, BorderLayout.SOUTH);

        JPanel inputDocButtonPanel = new JPanel();
        inputDocButtonPanel.setLayout(new BorderLayout());
        inputDocButtonPanel.setLayout(new GridLayout(1, 3));
        inputDocButtonPanel.add(inputDocLangBox);
        inputDocButtonPanel.add(addInputDocButton);
        inputDocButtonPanel.add(removeInputDocButton);
        inputDocButtonPanel.add(editInputDocButton);
        JPanel inputDocPanel = new JPanel();
        inputDocPanel.setLayout(new BorderLayout());
        inputDocPanel.add(inputDocListScroll, BorderLayout.CENTER);
        inputDocPanel.add(inputDocButtonPanel, BorderLayout.SOUTH);

        analyzeMorphemeButton = new JButton(Translator.getTerm("WordExtractionButton"));
        analyzeMorphemeButton.addActionListener(this);

        genSenCheckBox = new JCheckBox(Translator.getTerm("GensenCheckBox"));
        genSenCheckBox.setSelected(true);
        cabochaCheckBox = new JCheckBox(Translator.getTerm("CabochaCheckBox"));
        cabochaCheckBox.setSelected(true);
        showImportanceCheckBox = new JCheckBox("重要度");
        nounCheckBox = new JCheckBox(Translator.getTerm("NounCheckBox"));
        nounCheckBox.setSelected(true);
        verbCheckBox = new JCheckBox(Translator.getTerm("VerbCheckBox"));
        verbCheckBox.setSelected(true);
        otherCheckBox = new JCheckBox(Translator.getTerm("OtherPOSCheckBox"));
        oneWordCheckBox = new JCheckBox(Translator.getTerm("OneCharacterCheckBox"));

        punctuationField = new JTextField(10);
        punctuationField.setText(PUNCTUATION_CHARS);
        setPunctuationButton = new JButton(Translator.getTerm("SetPunctuationCharacterButton"));
        setPunctuationButton.addActionListener(this);

        JPanel morphemeAnalysisPanel = new JPanel();
        morphemeAnalysisPanel.add(genSenCheckBox);
        morphemeAnalysisPanel.add(cabochaCheckBox);
        morphemeAnalysisPanel.add(nounCheckBox);
        morphemeAnalysisPanel.add(verbCheckBox);
        morphemeAnalysisPanel.add(otherCheckBox);
        morphemeAnalysisPanel.add(oneWordCheckBox);
        morphemeAnalysisPanel.add(punctuationField);
        morphemeAnalysisPanel.add(setPunctuationButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(morphemeAnalysisPanel, BorderLayout.WEST);
        buttonPanel.add(analyzeMorphemeButton, BorderLayout.EAST);

        mainViews = new View[2];
        ViewMap viewMap = new ViewMap();
        // mainViews[0] = new View(Translator.getTerm("DocumentList"), null,
        // docPanel);
        mainViews[0] = new View(Translator.getTerm("InputDocumentList"), null, inputDocPanel);
        mainViews[1] = new View(Translator.getTerm("InputDocumentArea"), null, inputDocAreaScroll);

        for (int i = 0; i < mainViews.length; i++) {
            viewMap.addView(i, mainViews[i]);
        }
        rootWindow = Utils.createDODDLERootWindow(viewMap);
        setLayout(new BorderLayout());
        add(rootWindow, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setXGALayout() {
        // SplitWindow sw1 = new SplitWindow(true, 0.5f, mainViews[0],
        // mainViews[1]);
        SplitWindow sw2 = new SplitWindow(false, 0.4f, mainViews[0], mainViews[1]);
        rootWindow.setWindow(sw2);
    }

    private void setStopWordSet() {
        stopWordSet = new HashSet<String>();
        BufferedReader reader = null;
        try {
            File file = new File(STOP_WORD_LIST_FILE);
            if (!file.exists()) { return; }
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            while (reader.ready()) {
                String line = reader.readLine();
                stopWordSet.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    public boolean isOneWordChecked() {
        return oneWordCheckBox.isSelected();
    }

    public boolean isStopWord(String w) {
        return stopWordSet.contains(w);
    }

    private void deleteFiles(File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    private String getTextFileName(String fileName) {
        if (!fileName.endsWith("txt")) {
            fileName += ".txt";
        }
        return fileName;
    }

    private void saveFiles(Map<File, String> fileTextStringMap, File saveDir) {
        BufferedWriter writer = null;
        try {
            for (Entry<File, String> entrySet : fileTextStringMap.entrySet()) {
                File file = entrySet.getKey();
                String text = entrySet.getValue();
                File saveFile = new File(saveDir, getTextFileName(file.getName()));
                FileOutputStream fos = new FileOutputStream(saveFile);
                writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                writer.write(text);
                writer.close();
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    private Map<File, String> getFileTextStringMap(ListModel listModel) {
        Map<File, String> fileTextStringMap = new HashMap<File, String>();
        for (int i = 0; i < listModel.getSize(); i++) {
            Document doc = (Document) listModel.getElementAt(i);
            fileTextStringMap.put(doc.getFile(), doc.getText());
        }
        return fileTextStringMap;
    }

    /**
     * 同名のファイルが複数ある場合には，上書きされる
     */
    public void saveDocuments(File saveDir) {
        // File docs = new File(saveDir, "docs");
        // Map<File, String> fileTextStringMap =
        // getFileTextStringMap(docList.getModel());
        // if (!docs.mkdir()) {
        // deleteFiles(docs);
        // }
        // saveFiles(fileTextStringMap, docs);
        File inputDocs = new File(saveDir, "inputDocs");
        Map<File, String> fileTextStringMap = getFileTextStringMap(inputDocList.getModel());
        if (!inputDocs.mkdir()) {
            deleteFiles(inputDocs);
        }
        saveFiles(fileTextStringMap, inputDocs);
        saveDocumentInfo(saveDir);
    }

    public void saveDocumentInfo(File saveDir) {
        File docInfo = new File(saveDir, ProjectFileNames.DOC_INFO_FILE);
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(docInfo);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            // for (int i = 0; i < docList.getModel().getSize(); i++) {
            // Document doc = (Document) docList.getModel().getElementAt(i);
            // writer.write("doc," +
            // getTextFileName(doc.getFile().getAbsolutePath()) + "," +
            // doc.getLang() + "\n");
            // }
            for (int i = 0; i < inputDocList.getModel().getSize(); i++) {
                Document doc = (Document) inputDocList.getModel().getElementAt(i);
                writer.write("inputDoc," + getTextFileName(doc.getFile().getAbsolutePath()) + "," + doc.getLang()
                        + "\n");
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void openDocuments(File openDir) {
        File docs = new File(openDir, ProjectFileNames.DOC_DIR);
        if (docs.listFiles() != null) {
            Set fileSet = new TreeSet();
            getFiles(docs.listFiles(), fileSet);
            if (fileSet == null) { return; }
            addDocuments(docList, fileSet);
        }
    }

    public void loadDocuments(File openDir) {
        File docInfo = new File(openDir, ProjectFileNames.DOC_INFO_FILE);
        if (!docInfo.exists()) { return; }
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(docInfo);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] info = line.split(",");
                if (info.length != 3) {
                    continue;
                }
                String type = info[0];
                String fileName = info[1];
                String lang = info[2];
                if (type.equals("doc")) {
                    DefaultListModel model = (DefaultListModel) docList.getModel();
                    model.addElement(new Document(lang, new File(fileName)));
                } else if (type.equals("inputDoc")) {
                    DefaultListModel model = (DefaultListModel) inputDocList.getModel();
                    model.addElement(new Document(lang, new File(fileName)));
                }
            }
            inputWordSelectionPanel.setInputDocumentListModel(inputDocList.getModel());
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    public void openInputDocuments(File openDir) {
        File inputDocs = new File(openDir, ProjectFileNames.INPUT_DOC_DIR);
        if (inputDocs.listFiles() != null) {
            Set fileSet = new TreeSet();
            getFiles(inputDocs.listFiles(), fileSet);
            if (fileSet == null) { return; }
            addDocuments(inputDocList, fileSet);
        }
    }

    private void setWordInfoMap(String word, String pos, File doc, boolean isInputDoc) {
        WordInfo info = null;
        if (wordInfoMap.get(word) != null) {
            info = wordInfoMap.get(word);
        } else {
            int docNum = docList.getModel().getSize() + inputDocList.getModel().getSize();
            info = new WordInfo(word, docNum);
        }
        if (!(pos.equals("Complex Word") || pos.equals("複合語"))) {
            info.addPos(pos);
        } else if (info.getPosSet().size() == 0) {
            info.addPos(pos);
        }
        if (isInputDoc) {
            info.putInputDoc(doc);
        } else {
            info.putDoc(doc);
        }
        wordInfoMap.put(word, info);
    }

    private String runSSTagger(String text) {
        StringBuffer buf = new StringBuffer("");
        BufferedReader reader = null;
        BufferedWriter bw = null;
        try {
            System.out.println(SS_TAGGER_HOME);
            bw = new BufferedWriter(new FileWriter(SS_TAGGER_HOME + "tmp.txt"));
            text = text.replaceAll("．|\\*", " ");
            bw.write(text);
            bw.close();
            ProcessBuilder processBuilder = new ProcessBuilder(SS_TAGGER_HOME + "tagger.exe", "-i", "tmp.txt");
            processBuilder.directory(new File(SS_TAGGER_HOME));
            Process process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = "";
            while ((line = reader.readLine()) != null) { // reader.ready()は使えない
                System.out.println(line);
                if (line.matches(".*15")) {
                    break;
                }
            }
            reader.close();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append("\n");
            }
            reader.close();
            bw = new BufferedWriter(new FileWriter(SS_TAGGER_HOME + "tmpTagger.txt"));
            bw.write(buf.toString());
            bw.close();
        } catch (IOException ioe) {
            DODDLE.getLogger().log(Level.DEBUG, "SS Tagger can not execute.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        return buf.toString();
    }

    private void setWordInfo(String word, String pos, String basicStr, File file, boolean isInputDoc) {
        WordNetDic wordNetAPI = WordNetDic.getInstance();
        if (nounCheckBox.isSelected() && isEnNoun(pos)) {
            IndexWord indexWord = wordNetAPI.getIndexWord(POS.NOUN, word.toLowerCase());
            if (indexWord != null && indexWord.getLemma() != null) {
                basicStr = indexWord.getLemma();
                // System.out.println("n: " + basicStr);
            }
            setWordInfoMap(basicStr, pos, file, isInputDoc);
        } else if (verbCheckBox.isSelected() && isEnVerb(pos)) {
            IndexWord indexWord = wordNetAPI.getIndexWord(POS.VERB, word.toLowerCase());
            if (indexWord != null && indexWord.getLemma() != null) {
                basicStr = indexWord.getLemma();
                // System.out.println("v: " + basicStr);
            }
            setWordInfoMap(basicStr, pos, file, isInputDoc);
        } else if (otherCheckBox.isSelected() && isEnOther(pos)) {
            setWordInfoMap(basicStr, pos, file, isInputDoc);
        }
    }

    private void setWordInfo(String word, String basicStr, File file, boolean isInputDoc) {
        WordNetDic wordNetAPI = WordNetDic.getInstance();
        if (nounCheckBox.isSelected()) {
            IndexWord indexWord = wordNetAPI.getIndexWord(POS.NOUN, word.toLowerCase());
            if (indexWord != null && indexWord.getLemma() != null) {
                basicStr = indexWord.getLemma();
                setWordInfoMap(basicStr, "noun", file, isInputDoc);
                // System.out.println("n: " + basicStr);
            }
        }
        if (verbCheckBox.isSelected()) {
            IndexWord indexWord = wordNetAPI.getIndexWord(POS.VERB, word.toLowerCase());
            if (indexWord != null && indexWord.getLemma() != null) {
                basicStr = indexWord.getLemma();
                setWordInfoMap(basicStr, "verb", file, isInputDoc);
                // System.out.println("v: " + basicStr);
            }
        }
        if (otherCheckBox.isSelected()) {
            setWordInfoMap(basicStr, "", file, isInputDoc);
        }
    }

    private void analyzeEnMorpheme(String text, Document doc, boolean isInputDoc) {
        File file = doc.getFile();
        String taggedText = runSSTagger(text);
        if (taggedText.length() != 0) {
            String[] token = taggedText.split("\\s");
            if (token == null) { return; }
            for (int i = 0; i < token.length; i++) {
                String[] info = token[i].split("/");
                if (info.length != 2) {
                    continue;
                }
                String word = info[0];
                String pos = info[1];
                String basicStr = word.toLowerCase();

                if (!oneWordCheckBox.isSelected() && basicStr.length() == 1) {
                    continue;
                }
                if (isStopWord(basicStr)) {
                    continue;
                }
                setWordInfo(word, pos, basicStr, file, isInputDoc);
            }
        } else {
            String[] words = text.split("\\s");
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                String basicStr = word.toLowerCase();

                if (!oneWordCheckBox.isSelected() && basicStr.length() == 1) {
                    continue;
                }

                if (isStopWord(basicStr)) {
                    continue;
                }
                setWordInfo(word, basicStr, file, isInputDoc);
            }
        }
        if (genSenCheckBox.isSelected()) {
            Set complexWordSet = getGensenComplexWordSet(text, doc.getLang());
            for (Iterator i = complexWordSet.iterator(); i.hasNext();) {
                String complexWord = (String) i.next();
                setWordInfoMap(complexWord, "Complex Word", file, isInputDoc);
            }
        }
    }

    private void analyzeJaMorpheme(String text, Document doc, boolean isInputDoc) {
        File file = doc.getFile();
        try {
            StringTagger tagger = StringTagger.getInstance();
            Token[] tokenList = tagger.analyze(text);
            for (int i = 0; i < tokenList.length; i++) {
                String pos = tokenList[i].getPos();
                String basicStr = tokenList[i].getBasicString();
                // System.out.println(token[i].getPos());
                if (!oneWordCheckBox.isSelected() && basicStr.length() == 1) {
                    continue;
                }
                if (isStopWord(basicStr)) {
                    continue;
                }
                if (nounCheckBox.isSelected() && isJaNoun(pos)) {
                    setWordInfoMap(basicStr, pos, file, isInputDoc);
                } else if (verbCheckBox.isSelected() && isJaVerb(pos)) {
                    setWordInfoMap(basicStr, pos, file, isInputDoc);
                } else if (otherCheckBox.isSelected() && isJaOther(pos)) {
                    setWordInfoMap(basicStr, pos, file, isInputDoc);
                }
            }
            if (genSenCheckBox.isSelected()) {
                Set<String> complexWordSet = getGensenComplexWordSet(text, doc.getLang());
                for (String complexWord : complexWordSet) {
                    setWordInfoMap(complexWord, "複合語", file, isInputDoc);
                }
            }
            if (cabochaCheckBox.isSelected()) {
                taskAnalyzer.loadUseCaseTask(doc.getFile());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void analyzeMorphem(Document doc, boolean isInputDoc) {
        if (doc.getLang().equals("ja")) {
            analyzeJaMorpheme(doc.getText(), doc, isInputDoc);
        } else if (doc.getLang().equals("en")) {
            analyzeEnMorpheme(doc.getText(), doc, isInputDoc);
        }
    }

    public static String Japanese_Morphological_Analyzer = "C:/Program Files/Chasen/chasen.exe";
    public static String Japanese_Dependency_Structure_Analyzer = "C:/Program Files/CaboCha/bin/cabocha.exe";
    public static String PERL_EXE = "C:/Perl/bin/perl.exe";
    public static String SS_TAGGER_HOME = "C:/DODDLE-OWL/postagger-1.0";
    public static final String RESOURCE_DIR = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";
    private static String TERM_EXTRACT_CHASEN_EXE = "ex_chasen.pl";
    private static String TERM_EXTRACT_MECAB_EXE = "ex_mecab.pl";
    private static String TERM_EXTRACT_TAGGER_EXE = "ex_brillstagger.pl";
    public static String TERM_EXTRACT_SCRIPTS_DIR = "C:/DODDLE-OWL/TermExtractScripts";
    public static String XDOC2TXT_EXE = "C:/DODDLE-OWL/d2txt123/xdoc2txt.exe";
    public static String STOP_WORD_LIST_FILE = "C:/DODDLE-OWL/stop_word_list.txt";

    public void setCabochaComplexWordSet() {
        Set<Segment> segmentSet = taskAnalyzer.getSegmentSet();

        Map<String, Integer> compoundWordCountMap = taskAnalyzer.getCompounWordCountMap();
        Map<String, Integer> compoundWordWithNokakuCountMap = taskAnalyzer.getCompoundWordWithNokakuCountMap();

        for (Entry<String, Integer> entry : compoundWordCountMap.entrySet()) {
            String compoundWord = entry.getKey();
            int count = entry.getValue();
            setWordInfoMap(compoundWord, "複合語", null, true);
            System.out.println(compoundWord);
        }

        for (Entry<String, Integer> entry : compoundWordWithNokakuCountMap.entrySet()) {
            String compoundWord = entry.getKey();
            int count = entry.getValue();
            setWordInfoMap(compoundWord, "複合語", null, true);
            System.out.println(compoundWord);
        }
    }

    public Set<String> getGensenComplexWordSet(String text, String lang) {
        Set<String> wordSet = new HashSet<String>();
        BufferedReader reader = null;
        try {
            if (lang.equals("ja")) {
                reader = getGenSenReader(text);
            } else if (lang.equals("en")) {
                reader = getSSTaggerReader();
            }
            String line = "";
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
                String importance = lines[1];
                if (lang.equals("en") && word.split("\\s+").length == 1) {
                    continue;
                }
                if (!oneWordCheckBox.isSelected() && word.length() == 1) {
                    continue;
                }
                if (showImportanceCheckBox.isSelected()) {
                    wordSet.add(word + "(" + importance + ")");
                } else {
                    wordSet.add(word);
                }
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

    private BufferedReader getSSTaggerReader() throws IOException {
        String taggerPath = "";
        taggerPath = TERM_EXTRACT_SCRIPTS_DIR + TERM_EXTRACT_TAGGER_EXE;
        ProcessBuilder processBuilder = new ProcessBuilder(PERL_EXE, taggerPath, SS_TAGGER_HOME + "tmpTagger.txt");
        Process process = processBuilder.start();
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
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
     * @param rt
     * @return
     * @throws IOException
     */
    private BufferedReader getGenSenReader(String text) throws IOException {
        tmpFile = File.createTempFile("tmp", null);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));
        bw.write(text);
        bw.close();
        tmpJapaneseMorphologicalAnalyzerFile = File.createTempFile("tmpJpMorphologicalAnalyzer", null);
        ProcessBuilder processBuilder = new ProcessBuilder(Japanese_Morphological_Analyzer, "-o",
                tmpJapaneseMorphologicalAnalyzerFile.getAbsolutePath(), tmpFile.getAbsolutePath());
        processBuilder.start();
        String path = "";
        String TERM_EXTRACT_EXE = TERM_EXTRACT_CHASEN_EXE;
        if (Japanese_Morphological_Analyzer.matches(".*mecab.*")) {
            TERM_EXTRACT_EXE = TERM_EXTRACT_MECAB_EXE;
        }
        path = TERM_EXTRACT_SCRIPTS_DIR + TERM_EXTRACT_EXE;
        processBuilder = new ProcessBuilder(PERL_EXE, path, tmpJapaneseMorphologicalAnalyzerFile.getAbsolutePath());
        Process process = processBuilder.start();
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
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
        return pos.indexOf("NN") != -1;
    }

    private boolean isEnVerb(String pos) {
        return pos.indexOf("VB") != -1;
    }

    private boolean isEnOther(String pos) {
        return !(isEnNoun(pos) || isEnVerb(pos));
    }

    private void setUpperConcept() {
        for (Iterator i = UpperConceptManager.getUpperConceptLabelSet().iterator(); i.hasNext();) {
            String ucLabel = (String) i.next();
            Set wordSet = UpperConceptManager.getWordSet(ucLabel);
            for (Iterator j = wordSet.iterator(); j.hasNext();) {
                String word = (String) j.next();
                WordInfo info = wordInfoMap.get(word);
                if (info != null) {
                    info.addUpperConcept(ucLabel);
                }
            }
        }
    }

    class AnalyzeMorphemeWorker extends SwingWorker implements PropertyChangeListener {

        private int currentTaskCnt;

        public AnalyzeMorphemeWorker(int taskCnt) {
            currentTaskCnt = 1;
            DODDLE.STATUS_BAR.setLastMessage("単語抽出");
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(taskCnt);
            DODDLE.STATUS_BAR.lock();
            addPropertyChangeListener(this);
        }

        @Override
        protected Object doInBackground() throws Exception {
            try {
                taskAnalyzer = new TaskAnalyzer();
                UpperConceptManager.makeUpperOntologyList();
                setProgress(currentTaskCnt++);
                wordInfoMap.clear();
                ListModel listModel = inputDocList.getModel();
                for (int i = 0; i < listModel.getSize(); i++) {
                    Document doc = (Document) listModel.getElementAt(i);
                    analyzeMorphem(doc, true);
                }
                listModel = docList.getModel();
                for (int i = 0; i < listModel.getSize(); i++) {
                    Document doc = (Document) listModel.getElementAt(i);
                    analyzeMorphem(doc, false);
                }
                if (cabochaCheckBox.isSelected()) {
                    setCabochaComplexWordSet();
                }

                setProgress(currentTaskCnt++);
                setUpperConcept();
                removeDocWordSet();
                int docNum = docList.getModel().getSize() + inputDocList.getModel().getSize();
                inputWordSelectionPanel.setWordInfoTableModel(wordInfoMap, docNum);
                inputWordSelectionPanel.setInputDocumentListModel(inputDocList.getModel());
                setProgress(currentTaskCnt++);
                DODDLE.setSelectedIndex(DODDLEConstants.INPUT_WORD_SELECTION_PANEL);
                setProgress(currentTaskCnt++);
            } finally {
                DODDLE.STATUS_BAR.unLock();
                DODDLE.STATUS_BAR.hideProgressBar();
                project.addLog("WordExtractionButton");
            }
            return "done";
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE.STATUS_BAR.setValue(currentTaskCnt);
            }
        }

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == analyzeMorphemeButton) {
            AnalyzeMorphemeWorker worker = new AnalyzeMorphemeWorker(4);
            DODDLE.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        } else if (e.getSource() == docLangBox) {
            if (docList.getSelectedValues().length == 1) {
                String lang = (String) docLangBox.getSelectedItem();
                Document doc = (Document) docList.getSelectedValue();
                doc.setLang(lang);
                updateUI();
            }
        } else if (e.getSource() == inputDocLangBox) {
            if (inputDocList.getSelectedValues().length == 1) {
                String lang = (String) inputDocLangBox.getSelectedItem();
                Document doc = (Document) inputDocList.getSelectedValue();
                doc.setLang(lang);
                updateUI();
            }
        } else if (e.getSource() == setPunctuationButton) {
            PUNCTUATION_CHARS = punctuationField.getText();
            ListModel inputDocModel = inputDocList.getModel();
            for (int i = 0; i < inputDocModel.getSize(); i++) {
                Document doc = (Document) inputDocModel.getElementAt(i);
                doc.resetText();
            }
            Document doc = (Document) inputDocList.getSelectedValue();
            inputDocArea.setText(doc.getText());
            inputDocArea.setCaretPosition(0);
            docLangBox.setSelectedItem(doc.getLang());
            updateUI();
        }
    }

    private void removeDocWordSet() {
        Set docWordSet = new HashSet();
        for (Iterator i = wordInfoMap.values().iterator(); i.hasNext();) {
            WordInfo info = (WordInfo) i.next();
            if (!info.isInputWord()) {
                docWordSet.add(info.getWord());
            }
        }
        for (Iterator i = docWordSet.iterator(); i.hasNext();) {
            String dw = (String) i.next();
            wordInfoMap.remove(dw);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == inputDocList && inputDocList.getSelectedValues().length == 1) {
            Document doc = (Document) inputDocList.getSelectedValue();
            inputDocArea.setText(doc.getText());
            inputDocArea.setCaretPosition(0);
            inputDocLangBox.setSelectedItem(doc.getLang());
        } else if (e.getSource() == docList && docList.getSelectedValues().length == 1) {
            Document doc = (Document) docList.getSelectedValue();
            inputDocArea.setText(doc.getText());
            inputDocArea.setCaretPosition(0);
            docLangBox.setSelectedItem(doc.getLang());
        }
    }

    private Set getFiles(File[] files, Set fileSet) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
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
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return null; }
        File[] files = chooser.getSelectedFiles();
        Set fileSet = new TreeSet();
        getFiles(files, fileSet);
        return fileSet;
    }

    public String getTargetTextLines(String word) {
        StringWriter writer = new StringWriter();
        ListModel listModel = inputDocList.getModel();
        for (int i = 0; i < listModel.getSize(); i++) {
            Document doc = (Document) listModel.getElementAt(i);
            String text = doc.getText();
            if (text != null) {
                writer.write("[ " + doc.getFile().getAbsolutePath() + " ]\n");
                String[] lines = text.split("\n");
                for (int j = 0; j < lines.length; j++) {
                    String line = lines[j];
                    if (line.indexOf(word) != -1) {
                        writer.write((j + 1) + ": " + line + "\n");
                    }
                }
                writer.write("\n");
            }
        }
        return writer.toString();
    }

    public String getTargetHtmlLines(String word) {
        StringWriter writer = new StringWriter();
        writer.write("<html><body>");
        ListModel listModel = inputDocList.getModel();
        for (int i = 0; i < listModel.getSize(); i++) {
            Document doc = (Document) listModel.getElementAt(i);
            String text = doc.getText();
            StringBuilder buf = new StringBuilder();
            if (text != null) {
                String[] lines = text.split("\n");
                for (int j = 0; j < lines.length; j++) {
                    String line = lines[j];
                    if (line.matches(".*" + word + ".*")) {
                        line = line.replaceAll(word, "<b><font color=red>" + word + "</font></b>");
                        buf.append("<b><font color=navy>");
                        if (DODDLEConstants.LANG.equals("en")) {
                            buf.append(Translator.getTerm("LineMessage"));
                            buf.append(" ");
                            buf.append((j + 1));
                        } else {
                            buf.append((j + 1));
                            buf.append(Translator.getTerm("LineMessage"));
                        }
                        buf.append(": </font></b>");
                        buf.append(line);
                        buf.append("<br>");
                    }
                }
            }
            if (0 < buf.toString().length()) {
                writer.write("<b>" + doc.getFile().getAbsolutePath() + "</b><br>");
                writer.write(buf.toString());
            }
        }
        writer.write("</body></html>");
        return writer.toString();
    }

    private void addDocuments(JList list, Set fileSet) {
        DefaultListModel model = (DefaultListModel) list.getModel();
        for (Iterator i = fileSet.iterator(); i.hasNext();) {
            File file = (File) i.next();
            Document doc = new Document(file);
            String text = doc.getText();
            if (30 < text.split(" ").length) { // 適当．スペース数が一定以上あれば英文とみなす
                doc.setLang("en");
            }
            model.addElement(doc);
        }
    }

    class AddDocAction extends AbstractAction {

        public AddDocAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent e) {
            Set fileSet = getFiles();
            if (fileSet == null) { return; }
            addDocuments(docList, fileSet);
        }
    }

    class AddInputDocAction extends AbstractAction {
        public AddInputDocAction(String title) {
            super(title);
        }
        public void actionPerformed(ActionEvent e) {
            Set fileSet = getFiles();
            if (fileSet == null) { return; }
            addDocuments(inputDocList, fileSet);
            project.getConceptDefinitionPanel().setInputDocList();
            project.addLog("AddInputDocumentButton");
        }
    }

    class RemoveDocAction extends AbstractAction {
        public RemoveDocAction(String title) {
            super(title);
        }
        public void actionPerformed(ActionEvent e) {
            Object[] removeElements = docList.getSelectedValues();
            DefaultListModel model = (DefaultListModel) docList.getModel();
            for (int i = 0; i < removeElements.length; i++) {
                model.removeElement(removeElements[i]);
            }
            inputDocArea.setText("");
        }
    }

    class RemoveInputDocAction extends AbstractAction {
        public RemoveInputDocAction(String title) {
            super(title);
        }
        public void actionPerformed(ActionEvent e) {
            Object[] removeElements = inputDocList.getSelectedValues();
            DefaultListModel model = (DefaultListModel) inputDocList.getModel();
            for (int i = 0; i < removeElements.length; i++) {
                model.removeElement(removeElements[i]);
            }
            inputDocArea.setText("");
            project.getConceptDefinitionPanel().setInputDocList();
            project.addLog("RemoveInputDocumentButton");
        }
    }

    class EditInputDocAction extends AbstractAction {
        public EditInputDocAction(String title) {
            super(title);
        }
        public void actionPerformed(ActionEvent e) {
            Document doc = (Document) inputDocList.getSelectedValue();
            doc.setText(inputDocArea.getText());
            project.addLog("Edit");
        }
    }

    public Set<Document> getDocSet() {
        Set<Document> docSet = new HashSet<Document>();
        ListModel listModel = inputDocList.getModel();
        for (int i = 0; i < listModel.getSize(); i++) {
            Document doc = (Document) listModel.getElementAt(i);
            docSet.add(doc);
        }
        return docSet;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        DODDLEConstants.EDR_HOME = "C:/usr/eclipse_workspace/DODDLE_DIC/";
        frame.getContentPane().add(new DocumentSelectionPanel(null, null), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
