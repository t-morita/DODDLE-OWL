package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author Yoshihiro Shigeta
 * 
 * last modified: 2004-12-06 modified by takeshi morita
 * 
 */
public class ConceptDefinitionAlgorithmPanel extends JPanel implements ChangeListener, ActionListener {

    private Set<WordSpace> wordSpaceSet;
    private Set<Apriori> aprioriSet;
    private Map<Document, Map<String, List<ConceptPair>>> docWSResultMap;
    private Map<Document, Map<String, List<ConceptPair>>> docAprioriResultMap;

    private JLabel minSupport;
    private JTextField minSupportField;
    private JSlider minConfidenceSlider;
    private JLabel confidenceValue;

    private JLabel wordSpaceValue;
    private JSlider wordSpaceValueSlider;
    private JLabel gramNumber;
    private JLabel gramCount;
    private JLabel frontscope;
    private JLabel behindscope;
    private JTextField gramNumberField;
    private JTextField gramCountField;
    private JTextField frontScopeField;
    private JTextField behindScopeField;

    private JButton exeWordSpaceButton;
    private JButton exeAprioriButton;

    private JList inputConceptJList;

    private JComponent wordSpaceParamPanel;
    private JComponent aprioriParamPanel;

    private DODDLEProject doddleProject;

    public ConceptDefinitionAlgorithmPanel(JList list, DODDLEProject project) {
        inputConceptJList = list;
        doddleProject = project;

        wordSpaceSet = new HashSet<WordSpace>();
        aprioriSet = new HashSet<Apriori>();
        docWSResultMap = new HashMap<Document, Map<String, List<ConceptPair>>>();
        docAprioriResultMap = new HashMap<Document, Map<String, List<ConceptPair>>>();

        wordSpaceValueSlider = new JSlider();
        wordSpaceValueSlider.addChangeListener(this);
        minConfidenceSlider = new JSlider();
        minConfidenceSlider.addChangeListener(this);

        gramNumber = new JLabel("N-Gram    ");
        gramCount = new JLabel("Gram Count    ");
        frontscope = new JLabel("Front Scope    ");
        behindscope = new JLabel("Behind Scope    ");

        minSupport = new JLabel("Minimum Support     ");

        gramNumberField = new JTextField();
        gramNumberField.setHorizontalAlignment(JTextField.RIGHT);
        gramNumberField.setText("4");
        gramCountField = new JTextField();
        gramCountField.setHorizontalAlignment(JTextField.RIGHT);
        gramCountField.setText("7");
        frontScopeField = new JTextField();
        frontScopeField.setHorizontalAlignment(JTextField.RIGHT);
        frontScopeField.setText("60");
        behindScopeField = new JTextField();
        behindScopeField.setHorizontalAlignment(JTextField.RIGHT);
        behindScopeField.setText("10");

        minSupportField = new JTextField();
        minSupportField.setHorizontalAlignment(JTextField.RIGHT);
        minSupportField.setText("0");

        exeWordSpaceButton = new JButton(Translator.getTerm("ExecuteWordSpaceButton"));
        exeWordSpaceButton.addActionListener(this);
        exeAprioriButton = new JButton(Translator.getTerm("ExecuteAprioriButton"));
        exeAprioriButton.addActionListener(this);

        wordSpaceParamPanel = getNorthWestComponent(getWordSpacePanel());
        aprioriParamPanel = getNorthWestComponent(getAprioriPanel());
        /*
         * View[] mainViews = new View[2]; ViewMap viewMap = new ViewMap();
         * 
         * mainViews[0] = new
         * View(Translator.getString("ConceptDefinitionPanel.WordSpaceParameters"),
         * null, getWestComponent(wordSpaceParamPanel)); mainViews[1] = new
         * View(Translator.getString("ConceptDefinitionPanel.AprioriParameters"),
         * null, getWestComponent(aprioriParamPanel));
         * 
         * for (int i = 0; i < mainViews.length; i++) { viewMap.addView(i,
         * mainViews[i]); } RootWindow rootWindow =
         * Utils.createDODDLERootWindow(viewMap); //SplitWindow sw1 = new
         * SplitWindow(false, 0.3f, mainViews[0], mainViews[1]);
         * rootWindow.setWindow(new TabWindow(new DockingWindow[]{mainViews[0],
         * mainViews[1]}));
         * 
         * setLayout(new BorderLayout()); add(rootWindow, BorderLayout.CENTER); /*
         * JTabbedPane parameterTab = new JTabbedPane();
         * parameterTab.add(getWestComponent(wordSpaceParamPanel), Translator
         * .getString("ConceptDefinitionPanel.WordSpaceParameters"));
         * parameterTab.add(getWestComponent(aprioriParamPanel), Translator
         * .getString("ConceptDefinitionPanel.AprioriParameters"));
         * 
         * setLayout(new BorderLayout()); add(parameterTab,
         * BorderLayout.CENTER);
         */
    }

    public JComponent getWordSpaceParamPanel() {
        return wordSpaceParamPanel;
    }

    public JComponent getAprioriParamPanel() {
        return aprioriParamPanel;
    }

    public Map<Document, Map<String, List<ConceptPair>>> getDocWordSpaceResult() {
        return docWSResultMap;
    }

    public Map<Document, Map<String, List<ConceptPair>>> getDocAprioriResult() {
        return docAprioriResultMap;
    }

    public int getGramNumber() {
        int gramNum = 0;
        if (gramNumberField.getText() != null) {
            gramNum = new Integer(gramNumberField.getText()).intValue();
        }
        return gramNum;
    }

    public int getGramCount() {
        int gramCount = 0;
        if (gramCountField.getText() != null) {
            gramCount = new Integer(gramCountField.getText()).intValue();
        }
        return gramCount;
    }

    public int getFrontScope() {
        int frontScope = 0;
        if (frontScopeField.getText() != null) {
            frontScope = new Integer(frontScopeField.getText()).intValue();
        }
        return frontScope;
    }

    public int getBehindScope() {
        int behindScope = 0;
        if (behindScopeField.getText() != null) {
            behindScope = new Integer(behindScopeField.getText()).intValue();
        }
        return behindScope;
    }

    public double getMinSupport() {
        double minSupport = 0;
        if (minSupportField.getText() != null) {
            minSupport = new Double(minSupportField.getText()).doubleValue();
        }
        return minSupport;
    }

    private JComponent getEastComponent(JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(comp, BorderLayout.EAST);
        return p;
    }

    private JComponent getNorthWestComponent(JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(comp, BorderLayout.WEST);
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(p, BorderLayout.NORTH);
        return p2;
    }

    private JPanel getAprioriPanel() {
        confidenceValue = new JLabel("0.50");
        confidenceValue.setFont(new Font("Dialog", Font.PLAIN, 14));
        JPanel barPanel = new JPanel();
        barPanel.setPreferredSize(new Dimension(150, 20));
        barPanel.setLayout(new BorderLayout());
        barPanel.add(confidenceValue, BorderLayout.WEST);
        barPanel.add(minConfidenceSlider, BorderLayout.CENTER);

        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new GridLayout(2, 2, 0, 0));
        paramPanel.add(minSupport);
        paramPanel.add(minSupportField);
        paramPanel.add(new JLabel("Minimum Confidence"));
        paramPanel.add(barPanel);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(paramPanel, BorderLayout.NORTH);
        panel.add(getEastComponent(exeAprioriButton), BorderLayout.SOUTH);

        return panel;
    }

    public double getMinConfidence() {
        return (new Double(confidenceValue.getText())).doubleValue();
    }

    public double getWordSpaceUnder() {
        return (new Double(wordSpaceValue.getText())).doubleValue();
    }

    private JPanel getWordSpacePanel() {
        // Integer inte = new Integer(wordSpaceValueSlider.getValue());
        wordSpaceValue = new JLabel("0.50");
        wordSpaceValue.setFont(new Font("Dialog", Font.PLAIN, 14));

        JPanel barPanel = new JPanel();
        barPanel.setPreferredSize(new Dimension(150, 20));
        barPanel.setLayout(new BorderLayout());
        barPanel.add(wordSpaceValue, BorderLayout.WEST);
        barPanel.add(wordSpaceValueSlider, BorderLayout.CENTER);

        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new GridLayout(3, 4, 5, 5));
        paramPanel.add(gramNumber);
        paramPanel.add(gramNumberField);
        paramPanel.add(gramCount);
        paramPanel.add(gramCountField);
        paramPanel.add(frontscope);
        paramPanel.add(frontScopeField);
        paramPanel.add(behindscope);
        paramPanel.add(behindScopeField);
        paramPanel.add(new JLabel("WordSpace Value"));
        paramPanel.add(barPanel);
        paramPanel.add(new Label()); // dammy
        paramPanel.add(exeWordSpaceButton);

        return paramPanel;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == minConfidenceSlider) {
            Integer inte = new Integer(minConfidenceSlider.getValue());
            Double value = new Double(inte.doubleValue() / 100);
            if (value.toString().length() == 4) {
                confidenceValue.setText(value.toString());
            } else {
                confidenceValue.setText(value.toString() + "0");
            }
        } else if (e.getSource() == wordSpaceValueSlider) {
            Integer inte = new Integer(wordSpaceValueSlider.getValue());
            Double value = new Double(inte.doubleValue() / 100);
            if (value.toString().length() == 4) {
                wordSpaceValue.setText(value.toString());
            } else {
                wordSpaceValue.setText(value.toString() + "0");
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exeWordSpaceButton) {
            SwingWorker worker = new SwingWorker<String, String>() {
                public String doInBackground() {
                    exeWordSpace();
                    return "done";
                }
            };
            DODDLE.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        } else if (e.getSource() == exeAprioriButton) {
            SwingWorker worker = new SwingWorker<String, String>() {
                public String doInBackground() {
                    exeApriori();
                    return "done";
                }
            };
            DODDLE.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        }
    }

    public WordSpaceData getWordSpaceData() {
        WordSpaceData wsData = new WordSpaceData();
        wsData.setGramNumber(getGramNumber());
        wsData.setGramCount(getGramCount());
        wsData.setFrontScope(getFrontScope());
        wsData.setBehindScope(getBehindScope());
        wsData.setUnderValue(getWordSpaceUnder());
        return wsData;
    }

    public void setInputConcept() {
        wordSpaceSet.clear();
        aprioriSet.clear();
        ConceptDefinitionPanel conceptDefinitionPanel = doddleProject.getConceptDefinitionPanel();
        conceptDefinitionPanel.setInputConceptJList();
        if (0 < conceptDefinitionPanel.getInputWordList().size()) {
            Set<Document> docSet = doddleProject.getDocumentSelectionPanel().getDocSet();
            for (Document doc : docSet) {
                wordSpaceSet.add(new WordSpace(conceptDefinitionPanel, doc));
                aprioriSet.add(new Apriori(conceptDefinitionPanel, doc));
            }
        }
    }

    private List<String> getTargetInputWordList(Document doc) {
        List<String> inputWordList = doddleProject.getConceptDefinitionPanel().getInputWordList();
        if (inputWordList == null) { return null; }
        System.out.println("inputWordListSize: " + inputWordList.size());
        List<String> targetInputWordList = new ArrayList<String>();
        for (String iw : inputWordList) {
            //String text = DocumentSelectionPanel.getTextString(doc);
            String text = doc.getText();
            // '_'が入力単語に含まれている場合には，スペースに変換した場合もチェックする
            if (text.indexOf(iw.replaceAll("_", " ")) != -1 || text.indexOf(iw) != -1) {
                targetInputWordList.add(iw);
            } else {
                System.out.println("文書中に存在しない入力単語: " + iw);
            }
        }
        System.out.println("targetInputWordListSize: " + targetInputWordList.size());
        return targetInputWordList;
    }

    public static int APRIORI = 0;
    public static int WORDSPACE = 1;

    public void saveResult(File dir, int algorithm) {
        Map<Document, Map<String, List<ConceptPair>>> docResultMap = null;
        if (algorithm == APRIORI) {
            docResultMap = docAprioriResultMap;
        } else if (algorithm == WORDSPACE) {
            docResultMap = docWSResultMap;
        }
        BufferedWriter writer = null;
        try {
            for (Iterator i = docResultMap.entrySet().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                Document doc = (Document) entry.getKey();
                Map<String, List<ConceptPair>> map = (Map<String, List<ConceptPair>>) entry.getValue();
                if (map == null) {
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(dir.getPath() + "/" + doc.getFile().getName());
                writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                for (List<ConceptPair> pairList : map.values()) {
                    Collections.sort(pairList);
                    for (ConceptPair pair : pairList) {
                        writer.write(pair.getFromConceptLabel());
                        writer.write("\t");
                        writer.write(pair.getToConceptLabel());
                        writer.write("\t");
                        writer.write(pair.getRelatoinValue() + "\n");
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
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

    public void loadResult(File dir, int algorithm) {
        Map<Document, Map<String, List<ConceptPair>>> docResultMap = null;
        if (algorithm == APRIORI) {
            docResultMap = docAprioriResultMap;
        } else if (algorithm == WORDSPACE) {
            docResultMap = docWSResultMap;
        }
        BufferedReader reader = null;
        try {
            Set<Document> docSet = doddleProject.getDocumentSelectionPanel().getDocSet();
            for (Document doc : docSet) {
                File file = new File(dir.getPath() + "/" + doc.getFile().getName());
                if (!file.exists()) {
                    continue;
                }
                FileInputStream fis = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                Map<String, List<ConceptPair>> wordCPListMap = new HashMap<String, List<ConceptPair>>();
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] lines = line.split("\t");
                    String toC = lines[0];
                    String fromC = lines[1];
                    Double relVal = new Double(lines[2]);
                    ConceptPair cp = new ConceptPair(toC, fromC, relVal);
                    if (wordCPListMap.get(toC) != null) {
                        List<ConceptPair> cpList = wordCPListMap.get(toC);
                        cpList.add(cp);
                    } else {
                        List<ConceptPair> cpList = new ArrayList<ConceptPair>();
                        cpList.add(cp);
                        wordCPListMap.put(toC, cpList);
                    }
                }
                docResultMap.put(doc, wordCPListMap);
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
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

    public void saveConceptDefinitionParameters(File file) {
        BufferedWriter writer = null;
        try {
            Properties properties = new Properties();
            properties.setProperty("N-Gram", gramNumberField.getText());
            properties.setProperty("Gram_Count", gramCountField.getText());
            properties.setProperty("Front_Scope", frontScopeField.getText());
            properties.setProperty("Behind_Scope", behindScopeField.getText());
            properties.setProperty("WordSpace_Value", String.valueOf(wordSpaceValueSlider.getValue()));
            properties.setProperty("Minimum_Support", minSupportField.getText());
            properties.setProperty("Minimum_Confidence", String.valueOf(minConfidenceSlider.getValue()));
            FileOutputStream fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            properties.store(writer, "Concept Definition Parameters");
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
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

    public void loadConceptDefinitionParameters(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            Properties properties = new Properties();
            properties.load(reader);
            gramNumberField.setText(properties.getProperty("N-Gram"));
            gramCountField.setText(properties.getProperty("Gram_Count"));
            frontScopeField.setText(properties.getProperty("Front_Scope"));
            behindScopeField.setText(properties.getProperty("Behind_Scope"));
            wordSpaceValueSlider.setValue(Integer.parseInt(properties.getProperty("WordSpace_Value")));
            minSupportField.setText(properties.getProperty("Minimum_Support"));
            minConfidenceSlider.setValue(Integer.parseInt(properties.getProperty("Minimum_Confidence")));
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
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

    public void exeWordSpace() {
        try {
            DODDLE.STATUS_BAR.setLastMessage(Translator.getTerm("ExecuteWordSpaceButton"));
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(4 * wordSpaceSet.size());
            DODDLE.STATUS_BAR.lock();
            docWSResultMap.clear();
            WordSpaceData wsData = getWordSpaceData();
            for (WordSpace ws : wordSpaceSet) {
                if (ws != null) {
                    ws.setWSData(wsData);
                    List<String> targetInputWordList = getTargetInputWordList(ws.getDocument());
                    docWSResultMap.put(ws.getDocument(), ws.calcWordSpaceResult(targetInputWordList));
                }
            }
            if (0 < inputConceptJList.getModel().getSize()) {
                inputConceptJList.setSelectedIndex(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE.STATUS_BAR.unLock();
            DODDLE.STATUS_BAR.hideProgressBar();
        }
    }

    public void exeApriori() {
        try {
            DODDLE.STATUS_BAR.setLastMessage(Translator.getTerm("ExecuteAprioriButton"));
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(2 * aprioriSet.size());
            DODDLE.STATUS_BAR.lock();
            docAprioriResultMap.clear();
            double minSupport = getMinSupport();
            double minConfidence = getMinConfidence();
            for (Apriori apriori : aprioriSet) {
                if (apriori != null) {
                    apriori.setParameters(minSupport, minConfidence);
                    List<String> targetInputWordList = getTargetInputWordList(apriori.getDocument());
                    docAprioriResultMap.put(apriori.getDocument(), apriori.calcAprioriResult(targetInputWordList));
                }
            }
            if (0 < inputConceptJList.getModel().getSize()) {
                inputConceptJList.setSelectedIndex(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE.STATUS_BAR.unLock();
            DODDLE.STATUS_BAR.hideProgressBar();
        }
    }
}