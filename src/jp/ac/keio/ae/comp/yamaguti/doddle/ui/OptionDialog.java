package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OptionDialog extends JDialog implements ActionListener {

    private static JCheckBox siblingDisambiguationCheckBox;
    private static JCheckBox supDisambiguationCheckBox;
    private static JCheckBox subDisambiguationCheckBox;

    private static JCheckBox isUsingSpreadActivatingAlgorithmForDisambiguationBox;
    private static JRadioButton shortestSpreadActivatingAlgorithmForDisambiguationButton;
    private static JRadioButton longestSpreadActivatingAlgorithmForDisambiguationButton;
    private static JRadioButton averageSpreadActivatingAlgorithmForDisambiguationButton;

    private static JRadioButton complexWordSetSameConceptButton;
    private static JRadioButton complexWordSetSubConceptButton;

    private static JCheckBox showQNameCheckBox;

    private JButton applyButton;
    private JButton saveOptionButton;
    private JButton cancelButton;

    private BasicOptionPanel basicOptionPanel;
    private DirectoryPanel directoryPanel;

    public OptionDialog(Frame owner) {
        super(owner);

        basicOptionPanel = new BasicOptionPanel();

        isUsingSpreadActivatingAlgorithmForDisambiguationBox = new JCheckBox(Translator
                .getTerm("UsingSpreadActivatingAlgorithmCheckBox"), true);
        isUsingSpreadActivatingAlgorithmForDisambiguationBox.addActionListener(this);
        shortestSpreadActivatingAlgorithmForDisambiguationButton = new JRadioButton(Translator
                .getTerm("ShortestRadioButton"), true);
        longestSpreadActivatingAlgorithmForDisambiguationButton = new JRadioButton(Translator
                .getTerm("LongestRadioButton"));
        averageSpreadActivatingAlgorithmForDisambiguationButton = new JRadioButton(Translator
                .getTerm("AverageRadioButton"));
        ButtonGroup spreadActivatingAlgorithmGroup = new ButtonGroup();
        spreadActivatingAlgorithmGroup.add(shortestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmGroup.add(longestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmGroup.add(averageSpreadActivatingAlgorithmForDisambiguationButton);
        JPanel spreadActivatingAlgorithmOptionPanel = new JPanel();
        spreadActivatingAlgorithmOptionPanel.add(isUsingSpreadActivatingAlgorithmForDisambiguationBox);
        spreadActivatingAlgorithmOptionPanel.add(shortestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmOptionPanel.add(longestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmOptionPanel.add(averageSpreadActivatingAlgorithmForDisambiguationButton);

        supDisambiguationCheckBox = new JCheckBox(Translator.getTerm("PathToRootConceptsCheckBox"));
        supDisambiguationCheckBox.setSelected(true);
        subDisambiguationCheckBox = new JCheckBox(Translator.getTerm("SubConceptCheckBox"));
        subDisambiguationCheckBox.setSelected(true);
        siblingDisambiguationCheckBox = new JCheckBox(Translator.getTerm("SiblingConceptCheckBox"));
        siblingDisambiguationCheckBox.setSelected(true);
        JPanel automaticDisambiguationCheckBoxOptionPanel = new JPanel();
        automaticDisambiguationCheckBoxOptionPanel.setLayout(new GridLayout(3, 1));
        automaticDisambiguationCheckBoxOptionPanel.add(supDisambiguationCheckBox);
        automaticDisambiguationCheckBoxOptionPanel.add(subDisambiguationCheckBox);
        automaticDisambiguationCheckBoxOptionPanel.add(siblingDisambiguationCheckBox);

        JPanel automaticDisambiguationOptionPanel = new JPanel();
        automaticDisambiguationOptionPanel.setLayout(new BorderLayout());
        automaticDisambiguationOptionPanel.add(spreadActivatingAlgorithmOptionPanel, BorderLayout.NORTH);
        automaticDisambiguationOptionPanel.add(Utils.createNorthPanel(automaticDisambiguationCheckBoxOptionPanel),
                BorderLayout.CENTER);

        complexWordSetSameConceptButton = new JRadioButton(Translator.getTerm("SameConceptRadioButton"));
        complexWordSetSubConceptButton = new JRadioButton(Translator.getTerm("SubConceptRadioButton"));
        complexWordSetSubConceptButton.setSelected(true);
        ButtonGroup complexWordButtonGroup = new ButtonGroup();
        complexWordButtonGroup.add(complexWordSetSameConceptButton);
        complexWordButtonGroup.add(complexWordSetSubConceptButton);
        JPanel complexWordOptionPanel = new JPanel();
        complexWordOptionPanel.setLayout(new GridLayout(1, 2));
        complexWordOptionPanel.add(complexWordSetSameConceptButton);
        complexWordOptionPanel.add(complexWordSetSubConceptButton);

        showQNameCheckBox = new JCheckBox(Translator.getTerm("DisplayQNameCheckBox"));
        showQNameCheckBox.addActionListener(this);
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(showQNameCheckBox, BorderLayout.NORTH);

        directoryPanel = new DirectoryPanel();

        JTabbedPane optionTab = new JTabbedPane();
        optionTab.add(basicOptionPanel, Translator.getTerm("BaseOptionPanel"));
        optionTab.add(directoryPanel, Translator.getTerm("FolderOptionPanel"));
        optionTab.add(automaticDisambiguationOptionPanel, Translator.getTerm("DisambiguationOptionPanel"));
        optionTab.add(Utils.createNorthPanel(complexWordOptionPanel), Translator.getTerm("ComplexWordOptionPanel"));
        optionTab.add(viewPanel, Translator.getTerm("DisplayOptionPanel"));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(optionTab, BorderLayout.CENTER);

        saveOptionButton = new JButton(Translator.getTerm("SaveButton"));
        saveOptionButton.addActionListener(this);
        applyButton = new JButton(Translator.getTerm("ApplyButton"));
        applyButton.addActionListener(this);
        cancelButton = new JButton(Translator.getTerm("CloseButton"));
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveOptionButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(buttonPanel, BorderLayout.EAST);
        Container contentPane = getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(eastPanel, BorderLayout.SOUTH);

        loadConfig(new File(ProjectFileNames.CONFIG_FILE));

        setLocationRelativeTo(owner);
        setTitle(Translator.getTerm("OptionDialog"));
        pack();
    }

    class BasicOptionPanel extends JPanel {

        private JLabel langLabel;
        private JLabel basePrefixLabel;
        private JLabel baseURILabel;

        private JTextField langField;
        private JTextField basePrefixField;
        private JTextField baseURIField;

        BasicOptionPanel() {
            langLabel = new JLabel(Translator.getTerm("LanguageLabel"));            
            basePrefixLabel = new JLabel(Translator.getTerm("BasePrefixLabel"));
            baseURILabel = new JLabel(Translator.getTerm("BaseURILabel"));

            langField = new JTextField();
            langField.setText(DODDLEConstants.LANG);
            basePrefixField = new JTextField();            
            basePrefixField.setText(DODDLEConstants.BASE_PREFIX);
            baseURIField = new JTextField();
            baseURIField.setText(DODDLEConstants.BASE_URI);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(3, 2));
            mainPanel.add(langLabel);
            mainPanel.add(langField);
            mainPanel.add(basePrefixLabel);
            mainPanel.add(basePrefixField);
            mainPanel.add(baseURILabel);
            mainPanel.add(baseURIField);

            setLayout(new BorderLayout());
            add(mainPanel, BorderLayout.NORTH);
        }

        public void setLang(String lang) {
            langField.setText(lang);
        }

        public String getLang() {
            return langField.getText();
        }

        public void setBasePrefix(String prefix) {
            basePrefixField.setText(prefix);
        }

        public String getBasePrefix() {
            return basePrefixField.getText();
        }

        public void setBaseURI(String uri) {
            baseURIField.setText(uri);
        }

        public String getBaseURI() {
            return baseURIField.getText();
        }
    }

    public static void setSiblingDisambiguation(boolean t) {
        siblingDisambiguationCheckBox.setSelected(t);
    }

    public static void setSupDisambiguation(boolean t) {
        supDisambiguationCheckBox.setSelected(t);
    }

    public static void setSubDisambiguation(boolean t) {
        subDisambiguationCheckBox.setSelected(t);
    }

    public static void setUsingSpreadActivationAlgorithmForDisambiguation(boolean t) {
        isUsingSpreadActivatingAlgorithmForDisambiguationBox.setSelected(t);
    }

    public static void setShortestSpreadActivatingAlgorithmforDisambiguation(boolean t) {
        shortestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
    }

    public static void setLongestSpreadActivatingAlgorithmforDisambiguation(boolean t) {
        longestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
    }

    public static void setAverageSpreadActivatingAlgorithmforDisambiguation(boolean t) {
        averageSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
    }

    public static boolean isComplexWordSetSameConcept() {
        return complexWordSetSameConceptButton.isSelected();
    }

    public static boolean isUsingSpreadActivatingAlgorithm() {
        return isUsingSpreadActivatingAlgorithmForDisambiguationBox.isSelected();
    }

    public static boolean isCheckShortestSpreadActivation() {
        return shortestSpreadActivatingAlgorithmForDisambiguationButton.isSelected();
    }

    public static boolean isCheckLongestSpreadActivation() {
        return longestSpreadActivatingAlgorithmForDisambiguationButton.isSelected();
    }

    public static boolean isCheckAverageSpreadActivation() {
        return averageSpreadActivatingAlgorithmForDisambiguationButton.isSelected();
    }

    public static boolean isCheckSupConcepts() {
        return supDisambiguationCheckBox.isSelected();
    }

    public static boolean isCheckSubConcepts() {
        return subDisambiguationCheckBox.isSelected();
    }

    public static boolean isCheckSiblingConcepts() {
        return siblingDisambiguationCheckBox.isSelected();
    }

    public static boolean isShowQName() {
        return showQNameCheckBox.isSelected();
    }

    public void saveConfig(File file) {
        applyConfig();
        BufferedWriter writer = null;
        try {
            Properties properties = new Properties();

            properties.setProperty("LANG", basicOptionPanel.getLang());
            properties.setProperty("BASE_PREFIX", basicOptionPanel.getBasePrefix());
            properties.setProperty("BASE_URI", basicOptionPanel.getBaseURI());

            properties.setProperty("SEN_HOME", directoryPanel.getSenDicDir());
            properties.setProperty("EDR_HOME", directoryPanel.getEDRDicDir());
            properties.setProperty("EDRT_HOME", directoryPanel.getEDRTDicDir());
            properties.setProperty("PERL_EXE", directoryPanel.getPerlDir());
            properties.setProperty("CHASEN_EXE", directoryPanel.getJapaneseMorphologicalAnalyzer());
            properties.setProperty("PROJECT_DIR", directoryPanel.getProjectDir());
            properties.setProperty("UPPER_CONCEPT_LIST", directoryPanel.getUpperConceptList());
            properties.setProperty("STOP_WORD_LIST", directoryPanel.getStopWordList());
            properties.setProperty("TERM_EXTRACT_SCRIPTS_DIR", directoryPanel.getTermExtractScriptsDir());
            properties.setProperty("SWOOGLE_QUERY_RESULTS_DIR", directoryPanel.getSwoogleQueryResultsDir());
            properties.setProperty("OWL_ONTOLOGIES_DIR", directoryPanel.getOWLOntologiesDir());

            if (DocumentSelectionPanel.Japanese_Morphological_Analyzer != null) {
                properties.setProperty("Japanese_Morphological_Analyzer",
                        DocumentSelectionPanel.Japanese_Morphological_Analyzer);
            } else {
                // properties.setProperty("Japanese_Morphological_Analyzer","C:/Program
                // Files/Mecab/bin/mecab.exe -Ochasen");
                properties.setProperty("Japanese_Morphological_Analyzer", "C:/Program Files/Chasen/bin/chasen.exe");
            }
            properties.setProperty("SSTAGGER_HOME", directoryPanel.getSSTaggerDir());
            properties.setProperty("XDOC2TXT_EXE", directoryPanel.getXdoc2txtDir());
            properties.setProperty("WORDNET_HOME", directoryPanel.getWNDicDir());

            properties.setProperty("AutomaticDisambiguation.useSiblingNodeCount", String
                    .valueOf(siblingDisambiguationCheckBox.isSelected()));
            properties.setProperty("AutomaticDisambiguation.useChildNodeCount", String
                    .valueOf(subDisambiguationCheckBox.isSelected()));
            properties.setProperty("AutomaticDisambiguation.usePathToRootNodeCount", String
                    .valueOf(supDisambiguationCheckBox.isSelected()));

            properties.setProperty("AutomaticDisambiguation.isUsingSpreadActivationAlgorithm", String
                    .valueOf(isUsingSpreadActivatingAlgorithm()));
            properties.setProperty("AutomaticDisambiguation.isCheckShortestSpreadActivation", String
                    .valueOf(isCheckShortestSpreadActivation()));
            properties.setProperty("AutomaticDisambiguation.isCheckLongestSpreadActivation", String
                    .valueOf(isCheckLongestSpreadActivation()));
            properties.setProperty("AutomaticDisambiguation.isCheckAverageSpreadActivation", String
                    .valueOf(isCheckAverageSpreadActivation()));

            String isSameConceptOrSubConcept = "";
            if (complexWordSetSameConceptButton.isSelected()) {
                isSameConceptOrSubConcept = "SAME";
            } else {
                isSameConceptOrSubConcept = "SUB";
            }
            properties.setProperty("MakeConceptTreeWithComplexWord.isSameConceptOrSubConcept",
                    isSameConceptOrSubConcept);

            properties.setProperty("DisplayQName", String.valueOf(showQNameCheckBox.isSelected()));

            // 以下はオプションダイアログでの設定は未実装
            properties.setProperty("USING_DB", String.valueOf(DODDLEConstants.IS_USING_DB));

            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            properties.store(writer, "DODDLE-OWL Option");
        } catch (IOException ioe) {
            ioe.printStackTrace();
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

    public void applyConfig() {
        DODDLEConstants.LANG = basicOptionPanel.getLang();
        DODDLEConstants.BASE_PREFIX = basicOptionPanel.getBasePrefix();
        DODDLEConstants.BASE_URI = basicOptionPanel.getBaseURI();

        DODDLEConstants.SEN_HOME = directoryPanel.getSenDicDir();

        DODDLEConstants.EDR_HOME = directoryPanel.getEDRDicDir();
        EDRDic.ID_DEFINITION_MAP = DODDLEConstants.EDR_HOME + "idDefinitionMapforEDR.txt";
        EDRDic.WORD_IDSET_MAP = DODDLEConstants.EDR_HOME + "wordIDSetMapforEDR.txt";
        EDRTree.ID_SUBIDSET_MAP = DODDLEConstants.EDR_HOME + "idSubIDSetMapforEDR.txt";
        ConceptDefinition.CONCEPT_DEFINITION = DODDLEConstants.EDR_HOME + "conceptDefinitionforEDR.txt";

        DODDLEConstants.EDRT_HOME = directoryPanel.getEDRTDicDir();
        EDRDic.EDRT_ID_DEFINITION_MAP = DODDLEConstants.EDRT_HOME + "idDefinitionMapforEDR.txt";
        EDRDic.EDRT_WORD_IDSET_MAP = DODDLEConstants.EDRT_HOME + "wordIDSetMapforEDR.txt";
        EDRTree.EDRT_ID_SUBIDSET_MAP = DODDLEConstants.EDRT_HOME + "idSubIDSetMapforEDR.txt";
        DODDLEConstants.WORDNET_HOME = directoryPanel.getWNDicDir();

        DocumentSelectionPanel.PERL_EXE = directoryPanel.getPerlDir();
        DocumentSelectionPanel.Japanese_Morphological_Analyzer = directoryPanel.getJapaneseMorphologicalAnalyzer();
        DODDLEConstants.PROJECT_HOME = directoryPanel.getProjectDir();
        UpperConceptManager.UPPER_CONCEPT_LIST = directoryPanel.getUpperConceptList();
        DocumentSelectionPanel.STOP_WORD_LIST_FILE = directoryPanel.getStopWordList();
        DocumentSelectionPanel.SS_TAGGER_HOME = directoryPanel.getSSTaggerDir();
        DocumentSelectionPanel.XDOC2TXT_EXE = directoryPanel.getXdoc2txtDir();
        DocumentSelectionPanel.TERM_EXTRACT_SCRIPTS_DIR = directoryPanel.getTermExtractScriptsDir();
        SwoogleWebServiceWrapper.SWOOGLE_QUERY_RESULTS_DIR = directoryPanel.getSwoogleQueryResultsDir();
        SwoogleWebServiceWrapper.OWL_ONTOLOGIES_DIR = directoryPanel.getOWLOntologiesDir();
    }

    public void loadConfig(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            Properties properties = new Properties();
            properties.load(reader);

            DODDLEConstants.LANG = properties.getProperty("LANG");
            basicOptionPanel.setLang(DODDLEConstants.LANG);
            DODDLEConstants.BASE_PREFIX = properties.getProperty("BASE_PREFIX");
            basicOptionPanel.setBasePrefix(DODDLEConstants.BASE_PREFIX);
            DODDLEConstants.BASE_URI = properties.getProperty("BASE_URI");
            basicOptionPanel.setBaseURI(DODDLEConstants.BASE_URI);

            DODDLEConstants.SEN_HOME = properties.getProperty("SEN_HOME");
            directoryPanel.setSenDir(DODDLEConstants.SEN_HOME);
            DODDLEConstants.EDR_HOME = properties.getProperty("EDR_HOME");
            EDRDic.ID_DEFINITION_MAP = DODDLEConstants.EDR_HOME + "idDefinitionMapforEDR.txt";
            EDRDic.WORD_IDSET_MAP = DODDLEConstants.EDR_HOME + "wordIDSetMapforEDR.txt";
            EDRTree.ID_SUBIDSET_MAP = DODDLEConstants.EDR_HOME + "idSubIDSetMapforEDR.txt";
            ConceptDefinition.CONCEPT_DEFINITION = DODDLEConstants.EDR_HOME + "conceptDefinitionforEDR.txt";
            directoryPanel.setEDRDicDir(DODDLEConstants.EDR_HOME);
            DODDLEConstants.EDRT_HOME = properties.getProperty("EDRT_HOME");
            EDRDic.EDRT_ID_DEFINITION_MAP = DODDLEConstants.EDRT_HOME + "idDefinitionMapforEDR.txt";
            EDRDic.EDRT_WORD_IDSET_MAP = DODDLEConstants.EDRT_HOME + "wordIDSetMapforEDR.txt";
            EDRTree.EDRT_ID_SUBIDSET_MAP = DODDLEConstants.EDRT_HOME + "idSubIDSetMapforEDR.txt";
            directoryPanel.setEDRTDicDir(DODDLEConstants.EDRT_HOME);
            DODDLEConstants.WORDNET_HOME = properties.getProperty("WORDNET_HOME");
            directoryPanel.setWNDicDir(DODDLEConstants.WORDNET_HOME);

            DocumentSelectionPanel.PERL_EXE = properties.getProperty("PERL_EXE");
            directoryPanel.setPerlDir(DocumentSelectionPanel.PERL_EXE);
            DODDLEConstants.PROJECT_HOME = properties.getProperty("PROJECT_DIR");
            directoryPanel.setProjectDir(DODDLEConstants.PROJECT_HOME);
            UpperConceptManager.UPPER_CONCEPT_LIST = properties.getProperty("UPPER_CONCEPT_LIST");
            directoryPanel.setUpperCnceptList(UpperConceptManager.UPPER_CONCEPT_LIST);
            directoryPanel.setStopWordList(DocumentSelectionPanel.STOP_WORD_LIST_FILE);

            DocumentSelectionPanel.SS_TAGGER_HOME = properties.getProperty("SSTAGGER_HOME");
            directoryPanel.setSSTaggerDir(DocumentSelectionPanel.SS_TAGGER_HOME);
            DocumentSelectionPanel.XDOC2TXT_EXE = properties.getProperty("XDOC2TXT_EXE");
            directoryPanel.setXdoc2txtDir(DocumentSelectionPanel.XDOC2TXT_EXE);

            SwoogleWebServiceWrapper.SWOOGLE_QUERY_RESULTS_DIR = properties.getProperty("SWOOGLE_QUERY_RESULTS_DIR");
            directoryPanel.setSwoogleQueryResultsDir(SwoogleWebServiceWrapper.SWOOGLE_QUERY_RESULTS_DIR);
            SwoogleWebServiceWrapper.OWL_ONTOLOGIES_DIR = properties.getProperty("OWL_ONTOLOGIES_DIR");
            directoryPanel.setOWLOntologiesDir(SwoogleWebServiceWrapper.OWL_ONTOLOGIES_DIR);

            if (properties.getProperty("USING_DB").equals("true")) {
                DODDLEConstants.IS_USING_DB = true;
            }

            if (DocumentSelectionPanel.Japanese_Morphological_Analyzer != null) {
                properties.setProperty("Japanese_Morphological_Analyzer",
                        DocumentSelectionPanel.Japanese_Morphological_Analyzer);
            } else {
                // properties.setProperty("Japanese_Morphological_Analyzer","C:/Program
                // Files/Mecab/bin/mecab.exe -Ochasen");
                properties.setProperty("Japanese_Morphological_Analyzer", "C:/Program Files/Chasen/bin/chasen.exe");
            }
            DocumentSelectionPanel.Japanese_Morphological_Analyzer = properties
                    .getProperty("Japanese_Morphological_Analyzer");

            boolean t = new Boolean(properties.getProperty("AutomaticDisambiguation.useSiblingNodeCount"));
            siblingDisambiguationCheckBox.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.useChildNodeCount"));
            subDisambiguationCheckBox.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.usePathToRootNodeCount"));
            supDisambiguationCheckBox.setSelected(t);

            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isUsingSpreadActivationAlgorithm"));
            isUsingSpreadActivatingAlgorithmForDisambiguationBox.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isCheckShortestSpreadActivation"));
            shortestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isCheckLongestSpreadActivation"));
            longestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isCheckAverageSpreadActivation"));
            averageSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);

            String isSameConceptOrSubConcept = properties
                    .getProperty("MakeConceptTreeWithComplexWord.isSameConceptOrSubConcept");
            if (isSameConceptOrSubConcept != null) {
                complexWordSetSameConceptButton.setSelected(isSameConceptOrSubConcept.equals("SAME"));
                complexWordSetSubConceptButton.setSelected(isSameConceptOrSubConcept.equals("SUB"));
            }

            t = new Boolean(properties.getProperty("DisplayQName"));
            showQNameCheckBox.setSelected(t);
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
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveOptionButton) {
            JFileChooser chooser = new JFileChooser();
            int retval = chooser.showSaveDialog(DODDLE.rootPane);
            if (retval != JFileChooser.APPROVE_OPTION) { return; }
            saveConfig(chooser.getSelectedFile());
        } else if (e.getSource() == applyButton) {
            applyConfig();
        } else if (e.getSource() == cancelButton) {
            setVisible(false);
        } else if (e.getSource() == isUsingSpreadActivatingAlgorithmForDisambiguationBox) {
            boolean t = isUsingSpreadActivatingAlgorithmForDisambiguationBox.isSelected();
            shortestSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
            longestSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
            averageSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
        } else if (e.getSource() == showQNameCheckBox) {
            DODDLE.getCurrentProject().getConstructClassPanel().getIsaTree().updateUI();
            DODDLE.getCurrentProject().getConstructClassPanel().getHasaTree().updateUI();
            DODDLE.getCurrentProject().getConstructPropertyPanel().getIsaTree().updateUI();
        }
    }

    class DirectoryPanel extends JPanel {
        private JTextField japaneseMorphologicalAnalyzerField;
        private JTextField ssTaggerDirField;
        private JTextField perlDirField;
        private JTextField xdoc2txtDirField;
        private JTextField senDicDirField;
        private JTextField edrDicDirField;
        private JTextField edrtDicDirField;
        private JTextField wnDicDirField;
        private JTextField projectDirField;
        private JTextField upperConceptListField;
        private JTextField stopWordListField;
        private JTextField termExtractScriptsField;
        private JTextField swoogleQueryResultsDirField;
        private JTextField owlOntologiesDirField;

        private JButton browseJapaneseMorphologicalAnalyzerButton;
        private JButton browseSSTaggerDirButton;
        private JButton browsePerlDirButton;
        private JButton browseXdoc2txtDirButton;
        private JButton browseSenDicDirButton;
        private JButton browseEDRDicDirButton;
        private JButton browseEDRTDicDirButton;
        private JButton browseWNDicDirButton;
        private JButton browseProjectDirButton;
        private JButton browseUpperConceptListButton;
        private JButton browseStopWordListButton;
        private JButton browseTermExtractScriptsDirButton;
        private JButton browseSwoogleQueryResultsDirButton;
        private JButton browseOWLOntologiesDirButton;

        public DirectoryPanel() {
            japaneseMorphologicalAnalyzerField = new JTextField(FIELD_SIZE);
            browseJapaneseMorphologicalAnalyzerButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(japaneseMorphologicalAnalyzerField, browseJapaneseMorphologicalAnalyzerButton,
                    DocumentSelectionPanel.Japanese_Morphological_Analyzer);
            ssTaggerDirField = new JTextField(FIELD_SIZE);
            browseSSTaggerDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(ssTaggerDirField, browseSSTaggerDirButton, DocumentSelectionPanel.SS_TAGGER_HOME);
            perlDirField = new JTextField(FIELD_SIZE);
            browsePerlDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(perlDirField, browsePerlDirButton, DocumentSelectionPanel.PERL_EXE);
            xdoc2txtDirField = new JTextField(FIELD_SIZE);
            browseXdoc2txtDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(xdoc2txtDirField, browseXdoc2txtDirButton, DocumentSelectionPanel.XDOC2TXT_EXE);
            senDicDirField = new JTextField(FIELD_SIZE);
            browseSenDicDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(senDicDirField, browseSenDicDirButton, DODDLEConstants.SEN_HOME);
            edrDicDirField = new JTextField(FIELD_SIZE);
            browseEDRDicDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(edrDicDirField, browseEDRDicDirButton, DODDLEConstants.EDR_HOME);
            edrtDicDirField = new JTextField(FIELD_SIZE);
            browseEDRTDicDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(edrtDicDirField, browseEDRTDicDirButton, DODDLEConstants.EDRT_HOME);
            wnDicDirField = new JTextField(FIELD_SIZE);
            browseWNDicDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(wnDicDirField, browseWNDicDirButton, DODDLEConstants.WORDNET_HOME);
            projectDirField = new JTextField(FIELD_SIZE);
            browseProjectDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(projectDirField, browseProjectDirButton, DODDLEConstants.PROJECT_HOME);
            upperConceptListField = new JTextField(FIELD_SIZE);
            browseUpperConceptListButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(upperConceptListField, browseUpperConceptListButton, UpperConceptManager.UPPER_CONCEPT_LIST);
            stopWordListField = new JTextField(FIELD_SIZE);
            browseStopWordListButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(stopWordListField, browseStopWordListButton, DocumentSelectionPanel.STOP_WORD_LIST_FILE);
            termExtractScriptsField = new JTextField(FIELD_SIZE);
            browseTermExtractScriptsDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(termExtractScriptsField, browseTermExtractScriptsDirButton,
                    DocumentSelectionPanel.TERM_EXTRACT_SCRIPTS_DIR);
            swoogleQueryResultsDirField = new JTextField(FIELD_SIZE);
            browseSwoogleQueryResultsDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(swoogleQueryResultsDirField, browseSwoogleQueryResultsDirButton,
                    SwoogleWebServiceWrapper.SWOOGLE_QUERY_RESULTS_DIR);
            owlOntologiesDirField = new JTextField(FIELD_SIZE);
            browseOWLOntologiesDirButton = new JButton(Translator.getTerm("ReferenceButton"));
            initComponent(owlOntologiesDirField, browseOWLOntologiesDirButton,
                    SwoogleWebServiceWrapper.OWL_ONTOLOGIES_DIR);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(8, 2));
            panel.add(getPanel(japaneseMorphologicalAnalyzerField, browseJapaneseMorphologicalAnalyzerButton,
                    Translator.getTerm("JapaneseMorphologicalAnalyzerTextField")));
            panel
                    .add(getPanel(ssTaggerDirField, browseSSTaggerDirButton, Translator
                            .getTerm("SSTaggerFolderTextField")));
            panel.add(getPanel(perlDirField, browsePerlDirButton, Translator.getTerm("PerlTextField")));
            panel.add(getPanel(xdoc2txtDirField, browseXdoc2txtDirButton, Translator.getTerm("Xdoc2TxtTextField")));
            panel.add(getPanel(senDicDirField, browseSenDicDirButton, Translator.getTerm("SenDicFolderTextField")));
            panel.add(getPanel(edrDicDirField, browseEDRDicDirButton, Translator.getTerm("EDRDicFolderTextField")));
            panel.add(getPanel(edrtDicDirField, browseEDRTDicDirButton, Translator.getTerm("EDRTDicFolderTextField")));
            panel.add(getPanel(wnDicDirField, browseWNDicDirButton, Translator.getTerm("WordNetFolderTextField")));
            panel.add(getPanel(projectDirField, browseProjectDirButton, Translator.getTerm("ProjectFolderTextField")));
            panel.add(getPanel(upperConceptListField, browseUpperConceptListButton, Translator
                    .getTerm("UpperConceptListTextField")));
            panel.add(getPanel(stopWordListField, browseStopWordListButton, Translator.getTerm("StopWordsTextField")));
            panel.add(getPanel(termExtractScriptsField, browseTermExtractScriptsDirButton, Translator
                    .getTerm("ComplexWordExtractionScriptFolderTextField")));
            panel.add(getPanel(swoogleQueryResultsDirField, browseSwoogleQueryResultsDirButton, Translator
                    .getTerm("SwoogleQueryResultFolderTextField")));
            panel.add(getPanel(owlOntologiesDirField, browseOWLOntologiesDirButton, Translator
                    .getTerm("OWLOntologyFolderTextField")));

            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEtchedBorder());
            add(panel, BorderLayout.CENTER);
        }

        public void setJapaneseMorphologicalAnalyzer(String dir) {
            japaneseMorphologicalAnalyzerField.setText(dir);
        }

        public String getJapaneseMorphologicalAnalyzer() {
            return japaneseMorphologicalAnalyzerField.getText();
        }

        public void setSenDir(String dir) {
            senDicDirField.setText(dir);
        }

        public String getSenDicDir() {
            return senDicDirField.getText();
        }

        public void setPerlDir(String dir) {
            perlDirField.setText(dir);
        }

        public String getPerlDir() {
            return perlDirField.getText();
        }

        public void setXdoc2txtDir(String dir) {
            xdoc2txtDirField.setText(dir);
        }

        public String getXdoc2txtDir() {
            return xdoc2txtDirField.getText();
        }

        public void setSSTaggerDir(String dir) {
            ssTaggerDirField.setText(dir);
        }

        public String getSSTaggerDir() {
            return ssTaggerDirField.getText();
        }

        public void setEDRDicDir(String dir) {
            edrDicDirField.setText(dir);
        }

        public String getEDRDicDir() {
            return edrDicDirField.getText();
        }

        public void setEDRTDicDir(String dir) {
            edrtDicDirField.setText(dir);
        }

        public String getEDRTDicDir() {
            return edrtDicDirField.getText();
        }

        public void setWNDicDir(String dir) {
            wnDicDirField.setText(dir);
        }

        public String getWNDicDir() {
            return wnDicDirField.getText();
        }

        public void setProjectDir(String dir) {
            projectDirField.setText(dir);
        }

        public String getProjectDir() {
            return projectDirField.getText();
        }

        public void setUpperCnceptList(String file) {
            upperConceptListField.setText(file);
        }

        public String getUpperConceptList() {
            return upperConceptListField.getText();
        }

        public void setStopWordList(String file) {
            stopWordListField.setText(file);
        }

        public String getStopWordList() {
            return stopWordListField.getText();
        }

        public void setTermExtractScriptsDir(String file) {
            termExtractScriptsField.setText(file);
        }

        public String getTermExtractScriptsDir() {
            return termExtractScriptsField.getText();
        }

        public void setSwoogleQueryResultsDir(String file) {
            swoogleQueryResultsDirField.setText(file);
        }

        public String getSwoogleQueryResultsDir() {
            return swoogleQueryResultsDirField.getText();
        }

        public void setOWLOntologiesDir(String file) {
            owlOntologiesDirField.setText(file);
        }

        public String getOWLOntologiesDir() {
            return owlOntologiesDirField.getText();
        }

        private static final int FIELD_SIZE = 20;

        private void initComponent(JTextField textField, JButton button, String value) {
            textField.setText(value);
            textField.setEditable(false);
            button.addActionListener(new BrowseDirectory(textField));
        }

        private JPanel getPanel(JTextField textField, JButton button, String borderTitle) {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(borderTitle));
            workDirectoryPanel.add(textField);
            workDirectoryPanel.add(button);
            return workDirectoryPanel;
        }

        class BrowseDirectory extends AbstractAction {
            private JTextField directoryField;

            BrowseDirectory(JTextField field) {
                directoryField = field;
            }

            private String getDirectoryName() {
                File currentDirectory = new File(directoryField.getText());
                JFileChooser jfc = new JFileChooser(currentDirectory);
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                jfc.setDialogTitle("Select Directory");
                int fd = jfc.showOpenDialog(DODDLE.getCurrentProject().getRootPane());
                if (fd == JFileChooser.APPROVE_OPTION) { return jfc.getSelectedFile().toString(); }
                return null;
            }

            public void actionPerformed(ActionEvent e) {
                String directoryName = getDirectoryName();
                if (directoryName != null) {
                    directoryField.setText(directoryName);
                    directoryField.setToolTipText(directoryName);
                    if (directoryField == japaneseMorphologicalAnalyzerField) {
                        DocumentSelectionPanel.Japanese_Morphological_Analyzer = directoryName;
                    } else if (directoryField == ssTaggerDirField) {
                        DocumentSelectionPanel.SS_TAGGER_HOME = directoryName;
                    } else if (directoryField == perlDirField) {
                        DocumentSelectionPanel.PERL_EXE = directoryName;
                    } else if (directoryField == senDicDirField) {
                        DODDLEConstants.SEN_HOME = directoryName;
                    } else if (directoryField == edrDicDirField) {
                        DODDLEConstants.EDR_HOME = directoryName;
                    } else if (directoryField == edrtDicDirField) {
                        DODDLEConstants.EDRT_HOME = directoryName;
                    } else if (directoryField == wnDicDirField) {
                        DODDLEConstants.WORDNET_HOME = directoryName;
                    } else if (directoryField == projectDirField) {
                        DODDLEConstants.PROJECT_HOME = directoryName;
                    } else if (directoryField == upperConceptListField) {
                        UpperConceptManager.UPPER_CONCEPT_LIST = directoryName;
                    } else if (directoryField == termExtractScriptsField) {
                        DocumentSelectionPanel.TERM_EXTRACT_SCRIPTS_DIR = directoryName;
                    } else if (directoryField == swoogleQueryResultsDirField) {
                        SwoogleWebServiceWrapper.SWOOGLE_QUERY_RESULTS_DIR = directoryName;
                    } else if (directoryField == owlOntologiesDirField) {
                        SwoogleWebServiceWrapper.OWL_ONTOLOGIES_DIR = directoryName;
                    }
                }
            }
        }

    }
}
