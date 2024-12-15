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

package io.github.doddle_owl.views.concept_definition;

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.concept_definition.ConceptPair;
import io.github.doddle_owl.models.document_selection.Document;
import io.github.doddle_owl.models.concept_definition.WordSpaceData;
import io.github.doddle_owl.utils.Apriori;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.WordSpace;
import io.github.doddle_owl.views.DODDLEProjectPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Yoshihiro Shigeta
 * @author Takeshi Morita
 */
class ConceptDefinitionAlgorithmPanel extends JPanel implements ChangeListener,
        ActionListener {

    private final Set<WordSpace> wordSpaceSet;
    private final Set<Apriori> aprioriSet;
    private final TreeMap<Document, Map<String, List<ConceptPair>>> docWSResultMap;
    private final TreeMap<Document, Map<String, List<ConceptPair>>> docAprioriResultMap;

    private JLabel minSupportValueLabel;
    private JLabel minConfidenceValueLabel;
    private final JSlider minSupportSlider;
    private final JSlider minConfidenceSlider;

    private JLabel wordSpaceValue;
    private final JSlider wordSpaceValueSlider;
    private final JLabel gramNumber;
    private final JLabel gramCount;
    private final JLabel frontscope;
    private final JLabel behindscope;
    private final JTextField gramNumberField;
    private final JTextField gramCountField;
    private final JTextField frontScopeField;
    private final JTextField behindScopeField;

    private final JButton exeWordSpaceButton;
    private final JButton exeAprioriButton;

    private final JList inputConceptJList;

    private final JComponent wordSpaceParamPanel;
    private final JComponent aprioriParamPanel;

    private final DODDLEProjectPanel doddleProjectPanel;

    public ConceptDefinitionAlgorithmPanel(JList list, DODDLEProjectPanel project) {
        inputConceptJList = list;
        doddleProjectPanel = project;

        wordSpaceSet = new HashSet<>();
        aprioriSet = new HashSet<>();
        docWSResultMap = new TreeMap<>();
        docAprioriResultMap = new TreeMap<>();

        wordSpaceValueSlider = new JSlider();
        wordSpaceValueSlider.addChangeListener(this);
        minSupportSlider = new JSlider();
        minSupportSlider.addChangeListener(this);
        minConfidenceSlider = new JSlider();
        minConfidenceSlider.addChangeListener(this);

        gramNumber = new JLabel("N-Gram    ");
        gramCount = new JLabel("Gram Count    ");
        frontscope = new JLabel("Front Scope    ");
        behindscope = new JLabel("Behind Scope    ");

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

        exeWordSpaceButton = new JButton(Translator.getTerm("ExecuteWordSpaceButton"));
        exeWordSpaceButton.addActionListener(this);
        exeAprioriButton = new JButton(Translator.getTerm("ExecuteAprioriButton"));
        exeAprioriButton.addActionListener(this);

        wordSpaceParamPanel = getNorthWestComponent(getWordSpacePanel());
        aprioriParamPanel = getNorthWestComponent(getAprioriPanel());
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

    private int getGramNumber() {
        int gramNum = 0;
        if (gramNumberField.getText() != null) {
            gramNum = Integer.valueOf(gramNumberField.getText());
        }
        return gramNum;
    }

    private int getGramCount() {
        int gramCount = 0;
        if (gramCountField.getText() != null) {
            gramCount = Integer.valueOf(gramCountField.getText());
        }
        return gramCount;
    }

    private int getFrontScope() {
        int frontScope = 0;
        if (frontScopeField.getText() != null) {
            frontScope = Integer.valueOf(frontScopeField.getText());
        }
        return frontScope;
    }

    private int getBehindScope() {
        int behindScope = 0;
        if (behindScopeField.getText() != null) {
            behindScope = Integer.valueOf(behindScopeField.getText());
        }
        return behindScope;
    }

    private double getMinSupport() {
        return Double.parseDouble(minSupportValueLabel.getText());
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
        minConfidenceValueLabel = new JLabel("0.50");
        minConfidenceValueLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        JPanel minConfidencePanel = new JPanel();
        minConfidencePanel.setPreferredSize(new Dimension(150, 20));
        minConfidencePanel.setLayout(new BorderLayout());
        minConfidencePanel.add(minConfidenceValueLabel, BorderLayout.WEST);
        minConfidencePanel.add(minConfidenceSlider, BorderLayout.CENTER);

        minSupportValueLabel = new JLabel("0.50");
        minSupportValueLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        JPanel minSupportPanel = new JPanel();
        minSupportPanel.setPreferredSize(new Dimension(150, 20));
        minSupportPanel.setLayout(new BorderLayout());
        minSupportPanel.add(minSupportValueLabel, BorderLayout.WEST);
        minSupportPanel.add(minSupportSlider, BorderLayout.CENTER);


        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new GridLayout(2, 2, 0, 0));
        paramPanel.add(new JLabel(Translator.getTerm("MinimumSupportLabel")));
        paramPanel.add(minSupportPanel);
        paramPanel.add(new JLabel(Translator.getTerm("MinimumConfidenceLabel")));
        paramPanel.add(minConfidencePanel);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(paramPanel, BorderLayout.NORTH);
        panel.add(getEastComponent(exeAprioriButton), BorderLayout.SOUTH);

        return panel;
    }

    private double getMinConfidence() {
        return Double.parseDouble(minConfidenceValueLabel.getText());
    }

    private double getWordSpaceUnderValue() {
        return Double.parseDouble(wordSpaceValue.getText());
    }

    private JPanel getWordSpacePanel() {
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
        if (e.getSource() == minSupportSlider) {
            Integer inte = minSupportSlider.getValue();
            double value = inte.doubleValue() / 100;
            if (Double.toString(value).length() == 4) {
                minSupportValueLabel.setText(Double.toString(value));
            } else {
                minSupportValueLabel.setText(value + "0");
            }
        } else if (e.getSource() == minConfidenceSlider) {
            Integer inte = minConfidenceSlider.getValue();
            double value = inte.doubleValue() / 100;
            if (Double.toString(value).length() == 4) {
                minConfidenceValueLabel.setText(Double.toString(value));
            } else {
                minConfidenceValueLabel.setText(value + "0");
            }
        } else if (e.getSource() == wordSpaceValueSlider) {
            Integer inte = wordSpaceValueSlider.getValue();
            double value = inte.doubleValue() / 100;
            if (Double.toString(value).length() == 4) {
                wordSpaceValue.setText(Double.toString(value));
            } else {
                wordSpaceValue.setText(value + "0");
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
            DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        } else if (e.getSource() == exeAprioriButton) {
            SwingWorker worker = new SwingWorker<String, String>() {
                public String doInBackground() {
                    exeApriori();
                    return "done";
                }
            };
            DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        }
    }

    private WordSpaceData getWordSpaceData() {
        int gramNumber = getGramNumber();
        int gramCount = getGramCount();
        int frontScope = getFrontScope();
        int behindScope = getBehindScope();
        double underValue = getWordSpaceUnderValue();
        return new WordSpaceData(gramNumber, gramCount, frontScope, behindScope, underValue);
    }

    public void setInputConcept() {
        wordSpaceSet.clear();
        aprioriSet.clear();
        ConceptDefinitionPanel conceptDefinitionPanel = doddleProjectPanel.getConceptDefinitionPanel();
        conceptDefinitionPanel.setTermList();
        if (0 < conceptDefinitionPanel.getInputTermList().size()) {
            Set<Document> docSet = doddleProjectPanel.getDocumentSelectionPanel().getDocSet();
            for (Document doc : docSet) {
                wordSpaceSet.add(new WordSpace(conceptDefinitionPanel, doc));
                aprioriSet.add(new Apriori(conceptDefinitionPanel, doc));
            }
        }
    }

    private List<String> getTargetInputWordList(Document doc) {
        List<String> inputWordList = doddleProjectPanel.getConceptDefinitionPanel().getInputTermList();
        if (inputWordList == null) {
            return null;
        }
        List<String> targetInputWordList = new ArrayList<>();
        for (String iw : inputWordList) {
            String text = doc.getText();
            // '_'が入力単語に含まれている場合には，スペースに変換した場合もチェックする
            if (text.contains(iw.replaceAll("_", " ")) || text.contains(iw)) {
                targetInputWordList.add(iw);
            } else {
                // System.out.println("文書中に存在しない入力単語: " + iw);
            }
        }
        DODDLE_OWL.STATUS_BAR.printMessage(doc.getFile().getAbsolutePath() + ": "
                + targetInputWordList.size() + "/" + inputWordList.size());
        return targetInputWordList;
    }

    public static final int APRIORI = 0;
    public static final int WORDSPACE = 1;

    public void saveResult(File dir, int algorithm) {
        TreeMap<Document, Map<String, List<ConceptPair>>> docResultMap = null;
        if (algorithm == APRIORI) {
            docResultMap = docAprioriResultMap;
        } else if (algorithm == WORDSPACE) {
            docResultMap = docWSResultMap;
        }
        try {
            for (Entry<Document, Map<String, List<ConceptPair>>> entry : docResultMap.entrySet()) {
                Document doc = entry.getKey();
                Map<String, List<ConceptPair>> map = entry.getValue();
                if (map == null) {
                    continue;
                }
                Path path = Paths.get(dir.getPath() + File.separator + doc.getFile().getName());
                BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                try (writer) {
                    for (List<ConceptPair> pairList : map.values()) {
                        Collections.sort(pairList);
                        for (ConceptPair pair : pairList) {
                            writer.write(pair.getFromConceptLabel());
                            writer.write("\t");
                            writer.write(pair.getToConceptLabel());
                            writer.write("\t");
                            writer.write(pair.getRelatoinValue().toString());
                            writer.newLine();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveResult(int projectID, Statement stmt, int algorithm) {
        TreeMap<Document, Map<String, List<ConceptPair>>> docResultMap = null;
        String algorithmTable = "";
        if (algorithm == APRIORI) {
            docResultMap = docAprioriResultMap;
            algorithmTable = "apriori_result";
        } else if (algorithm == WORDSPACE) {
            docResultMap = docWSResultMap;
            algorithmTable = "wordspace_result";
        }
        int docID = 1;
        try {
            for (Entry<Document, Map<String, List<ConceptPair>>> entry : docResultMap.entrySet()) {
                Document doc = entry.getKey();
                Map<String, List<ConceptPair>> map = entry.getValue();
                if (map == null) {
                    continue;
                }
                for (List<ConceptPair> pairList : map.values()) {
                    Collections.sort(pairList);
                    for (ConceptPair pair : pairList) {
                        String fromConceptLabel = URLEncoder.encode(pair.getFromConceptLabel(),
                                StandardCharsets.UTF_8);
                        String toConceptLabel = URLEncoder.encode(pair.getToConceptLabel(), StandardCharsets.UTF_8);
                        String sql = "INSERT INTO " + algorithmTable
                                + " (Project_ID,Doc_ID,Term1,Term2,Value) " + "VALUES(" + projectID
                                + "," + docID + ",'" + fromConceptLabel + "','" + toConceptLabel
                                + "'," + pair.getRelatoinValue() + ")";
                        stmt.executeUpdate(sql);
                    }
                }
                docID++;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void loadResult(File dir, int algorithm) {
        TreeMap<Document, Map<String, List<ConceptPair>>> docResultMap = null;
        if (algorithm == APRIORI) {
            docResultMap = docAprioriResultMap;
        } else if (algorithm == WORDSPACE) {
            docResultMap = docWSResultMap;
        }
        try {
            Set<Document> docSet = doddleProjectPanel.getDocumentSelectionPanel().getDocSet();
            for (Document doc : docSet) {
                File file = new File(dir.getPath() + File.separator + doc.getFile().getName());
                if (!file.exists()) {
                    continue;
                }
                BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
                try (reader) {
                    Map<String, List<ConceptPair>> wordCPListMap = new HashMap<>();
                    while (reader.ready()) {
                        String line = reader.readLine();
                        String[] lines = line.split("\t");
                        if (lines.length != 3) { // ファイルが正常に保存できなかった場合の対応
                            // System.out.println(file.getAbsolutePath() + ": " +
                            // line);
                            continue;
                        }
                        String toC = lines[0];
                        String fromC = lines[1];
                        Double relVal = Double.parseDouble(lines[2]);
                        ConceptPair cp = new ConceptPair(toC, fromC, relVal);
                        if (wordCPListMap.get(toC) != null) {
                            List<ConceptPair> cpList = wordCPListMap.get(toC);
                            cpList.add(cp);
                        } else {
                            List<ConceptPair> cpList = new ArrayList<>();
                            cpList.add(cp);
                            wordCPListMap.put(toC, cpList);
                        }
                    }
                    docResultMap.put(doc, wordCPListMap);
                }
            }
        } catch (IOException uee) {
            uee.printStackTrace();
        }
    }

    public void loadResult(int projectID, Statement stmt, int algorithm) {
        TreeMap<Document, Map<String, List<ConceptPair>>> docResultMap = null;
        String resultTable = "";
        if (algorithm == APRIORI) {
            docResultMap = docAprioriResultMap;
            resultTable = "apriori_result";
        } else if (algorithm == WORDSPACE) {
            docResultMap = docWSResultMap;
            resultTable = "wordspace_result";
        }

        try {
            Map<String, Integer> docIDMap = new HashMap<>();
            String sql = "SELECT * from doc_info where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int docID = rs.getInt("Doc_ID");
                String path = URLDecoder.decode(rs.getString("Doc_Path"), StandardCharsets.UTF_8);
                docIDMap.put(path, docID);
            }

            Set<Document> docSet = doddleProjectPanel.getDocumentSelectionPanel().getDocSet();
            for (Document doc : docSet) {
                int docID = docIDMap.get(doc.getFile().getAbsolutePath());
                sql = "SELECT * from " + resultTable + " where Project_ID=" + projectID
                        + " and Doc_ID=" + docID;
                rs = stmt.executeQuery(sql);
                Map<String, List<ConceptPair>> wordCPListMap = new HashMap<>();
                while (rs.next()) {
                    String term1 = URLDecoder.decode(rs.getString("Term1"), StandardCharsets.UTF_8);
                    String term2 = URLDecoder.decode(rs.getString("Term2"), StandardCharsets.UTF_8);
                    double value = rs.getDouble("Value");
                    ConceptPair cp = new ConceptPair(term1, term2, value);
                    if (wordCPListMap.get(term1) != null) {
                        List<ConceptPair> cpList = wordCPListMap.get(term1);
                        cpList.add(cp);
                    } else {
                        List<ConceptPair> cpList = new ArrayList<>();
                        cpList.add(cp);
                        wordCPListMap.put(term1, cpList);
                    }
                }
                docResultMap.put(doc, wordCPListMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveConceptDefinitionParameters(File file) {
        try {
            Properties properties = new Properties();
            properties.setProperty("N-Gram", gramNumberField.getText());
            properties.setProperty("Gram_Count", gramCountField.getText());
            properties.setProperty("Front_Scope", frontScopeField.getText());
            properties.setProperty("Behind_Scope", behindScopeField.getText());
            properties.setProperty("WordSpace_Value", String.valueOf(wordSpaceValueSlider.getValue()));
            properties.setProperty("Minimum_Support", String.valueOf(minSupportSlider.getValue()));
            properties.setProperty("Minimum_Confidence", String.valueOf(minConfidenceSlider.getValue()));
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                properties.store(writer, "Concept Definition Parameters");
            }
        } catch (IOException uee) {
            uee.printStackTrace();
        }
    }

    public void saveConceptDefinitionParameters(int projectID, Statement stmt) {
        try {
            int minimumConfidence = minConfidenceSlider.getValue();
            double minimumSupport = minSupportSlider.getValue();
            int frontScope = Integer.parseInt(frontScopeField.getText());
            int behindScope = Integer.parseInt(behindScopeField.getText());
            int nGram = Integer.parseInt(gramNumberField.getText());
            int gramCount = Integer.parseInt(gramCountField.getText());
            int wordSpaceValue = wordSpaceValueSlider.getValue();

            String sql = "INSERT INTO concept_definition_parameter (Project_ID,Minimum_Confidence,Minimum_Support,Front_Scope,Behind_Scope,N_Gram,Gram_Count,Word_Space_Value) "
                    + "VALUES("
                    + projectID
                    + ","
                    + minimumConfidence
                    + ","
                    + minimumSupport
                    + ","
                    + frontScope
                    + ","
                    + behindScope
                    + ","
                    + nGram
                    + ","
                    + gramCount
                    + ","
                    + wordSpaceValue + ")";
            stmt.executeUpdate(sql);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void loadConceptDefinitionParameters(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            Properties properties = new Properties();
            try (reader) {
                properties.load(reader);
            }
            gramNumberField.setText(properties.getProperty("N-Gram"));
            gramCountField.setText(properties.getProperty("Gram_Count"));
            frontScopeField.setText(properties.getProperty("Front_Scope"));
            behindScopeField.setText(properties.getProperty("Behind_Scope"));
            wordSpaceValueSlider.setValue(Integer.parseInt(properties.getProperty("WordSpace_Value")));
            minSupportSlider.setValue(Integer.parseInt(properties.getProperty("Minimum_Support")));
            minConfidenceSlider.setValue(Integer.parseInt(properties.getProperty("Minimum_Confidence")));
        } catch (IOException uee) {
            uee.printStackTrace();
        }
    }

    public void loadConceptDefinitionParameters(int projectID, Statement stmt) {
        try {
            String sql = "SELECT * from  concept_definition_parameter where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                gramNumberField.setText(Integer.toString(rs.getInt("N_Gram")));
                gramCountField.setText(Integer.toString(rs.getInt("Gram_Count")));
                frontScopeField.setText(Integer.toString(rs.getInt("Front_Scope")));
                behindScopeField.setText(Integer.toString(rs.getInt("Behind_Scope")));
                wordSpaceValueSlider.setValue((int) rs.getDouble("Word_Space_Value"));
                minSupportSlider.setValue((int) rs.getDouble("Minimum_Support"));
                minConfidenceSlider.setValue((int) rs.getDouble("Minimum_Confidence"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exeWordSpace() {
        try {
            DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("ExecuteWordSpaceButton"));
            DODDLE_OWL.STATUS_BAR.startTime();
            DODDLE_OWL.STATUS_BAR.initNormal(4 * wordSpaceSet.size());
            DODDLE_OWL.STATUS_BAR.lock();
            docWSResultMap.clear();
            WordSpaceData wsData = getWordSpaceData();
            for (WordSpace ws : wordSpaceSet) {
                if (ws != null) {
                    ws.setWSData(wsData);
                    List<String> targetInputWordList = getTargetInputWordList(ws.getDocument());
                    docWSResultMap.put(ws.getDocument(),
                            ws.calcWordSpaceResult(targetInputWordList));
                }
            }
            if (0 < inputConceptJList.getModel().getSize()) {
                inputConceptJList.setSelectedIndex(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE_OWL.STATUS_BAR.printMessage("WordSpace Done");
            DODDLE_OWL.STATUS_BAR.unLock();
            DODDLE_OWL.STATUS_BAR.hideProgressBar();
        }
    }

    private void exeApriori() {
        try {
            DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("ExecuteAprioriButton"));
            DODDLE_OWL.STATUS_BAR.startTime();
            DODDLE_OWL.STATUS_BAR.initNormal(2 * aprioriSet.size());
            DODDLE_OWL.STATUS_BAR.lock();
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
            DODDLE_OWL.STATUS_BAR.printMessage("Apriori Done");
            DODDLE_OWL.STATUS_BAR.unLock();
            DODDLE_OWL.STATUS_BAR.hideProgressBar();
        }
    }
}