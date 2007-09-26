package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.io.*;
import java.sql.Statement;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.infonode.docking.*;
import net.infonode.docking.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author shigeta
 * 
 * 2004-12-06 modified by takeshi morita
 * 
 */
public class ConceptDefinitionPanel extends JPanel implements ListSelectionListener {

    private Map<String, Set<Concept>> wordCorrespondConceptSetMap;
    private Map<String, Concept> complexWordConceptMap;
    private Map<String, Set<Concept>> wordConceptSetMap;

    public String corpusString;
    public List<String> inputWordList;

    private JList inputConceptJList;
    private ConceptDefinitionResultPanel resultPanel;
    private ConceptDefinitionAlgorithmPanel algorithmPanel;
    private ConceptDefinitionResultPanel.ConceptDefinitionPanel conceptDefinitionPanel;

    private DODDLEProject doddleProject;
    private DocumentSelectionPanel docSelectionPanel;
    private DisambiguationPanel disambiguationPanel;

    private View[] mainViews;
    private RootWindow rootWindow;

    public void setInputConceptJList() {
        inputWordList = getInputWordList();
        inputConceptJList.removeAll();
        DefaultListModel listModel = new DefaultListModel();
        for (String iw : inputWordList) {
            listModel.addElement(iw);
        }
        inputConceptJList.setModel(listModel);
    }

    public Concept getConcept(String word) {
        Concept c = null;
        if (complexWordConceptMap.get(word) != null) {
            c = complexWordConceptMap.get(word);
            // System.out.println("cid: " + id);
        } else if (wordCorrespondConceptSetMap.get(word) != null) {
            Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(word);
            c = (Concept) correspondConceptSet.toArray()[0];
            // System.out.println("id: " + id);
        } else {
            Set<Concept> wordConceptSet = wordConceptSetMap.get(word);
            if (wordConceptSet != null) {
                c = (Concept) wordConceptSet.toArray()[0];
            }
        }
        if (c == null) { return null; }
        Concept concept = doddleProject.getConcept(c.getURI());
        if (concept != null) { return concept; }

        concept = OWLOntologyManager.getConcept(c.getURI());
        if (concept != null) { return concept; }
        if (c.getNameSpace().equals(DODDLEConstants.EDR_URI)) {
            concept = EDRDic.getEDRConcept(c.getLocalName());
        } else if (c.getNameSpace().equals(DODDLEConstants.EDRT_URI)) {
            concept = EDRDic.getEDRTConcept(c.getLocalName());
        } else if (c.getNameSpace().equals(DODDLEConstants.WN_URI)) {
            concept = WordNetDic.getWNConcept(c.getLocalName());
        }
        return concept;
    }

    private Resource getResource(Concept c, Model ontology) {
        return ontology.getResource(c.getURI());
    }

    public Property getProperty(Concept c, Model ontology) {
        return ontology.getProperty(c.getURI());
    }

    public Model addConceptDefinition(Model ontology) {
        for (int i = 0; i < resultPanel.getRelationCount(); i++) {
            Object[] relation = resultPanel.getRelation(i);
            boolean isMetaProperty = new Boolean((String) relation[0]);
            String domainWord = (String) relation[1];
            String rangeWord = (String) relation[3];
            Concept property = (Concept) relation[2];
            Concept domainConcept = getConcept(domainWord);
            Concept rangeConcept = getConcept(rangeWord);

            // System.out.println("r: "+property+"d: "+domainConcept + "r:
            // "+rangeConcept);

            if (property.getLocalName().equals("DID0")) { // agent
                ontology.add(getResource(domainConcept, ontology), RDF.type, OWL.ObjectProperty);
                ontology.add(getResource(domainConcept, ontology), RDFS.domain, getResource(rangeConcept, ontology));
            } else if (property.getLocalName().equals("DID1")) {// object
                ontology.add(getResource(domainConcept, ontology), RDF.type, OWL.ObjectProperty);
                ontology.add(getResource(domainConcept, ontology), RDFS.range, getResource(rangeConcept, ontology));
            } else {
                if (isMetaProperty) {
                    ontology.add(getResource(domainConcept, ontology), getProperty(property, ontology), getResource(
                            rangeConcept, ontology));
                    ontology.add(getResource(property, ontology), RDFS.domain, OWL.Class);
                    ontology.add(getResource(property, ontology), RDFS.range, OWL.Class);
                } else {
                    ontology.add(getResource(property, ontology), RDFS.domain, getResource(domainConcept, ontology));
                    ontology.add(getResource(property, ontology), RDFS.range, getResource(rangeConcept, ontology));
                }
            }
        }
        return ontology;
    }

    public ConceptDefinitionPanel(DODDLEProject project) {
        doddleProject = project;
        docSelectionPanel = project.getDocumentSelectionPanel();
        disambiguationPanel = project.getDisambiguationPanel();

        inputConceptJList = new JList(new DefaultListModel());
        inputConceptJList.addListSelectionListener(this);

        algorithmPanel = new ConceptDefinitionAlgorithmPanel(inputConceptJList, doddleProject);
        resultPanel = new ConceptDefinitionResultPanel(inputConceptJList, algorithmPanel, doddleProject);
        conceptDefinitionPanel = resultPanel.getDefinePanel();

        mainViews = new View[10];
        ViewMap viewMap = new ViewMap();

        mainViews[0] = new View(Translator.getTerm("WordSpaceParameterPanel"), null,
                algorithmPanel.getWordSpaceParamPanel());
        mainViews[1] = new View(Translator.getTerm("AprioriParameterPanel"), null, algorithmPanel
                .getAprioriParamPanel());
        mainViews[2] = new View(Translator.getTerm("InputConceptList"), null, resultPanel
                .getInputConceptPanel());
        mainViews[3] = new View(Translator.getTerm("InputDocumentList"), null, resultPanel
                .getInputDocPanel());
        mainViews[4] = new View("WordSpace", null, new JScrollPane(resultPanel.getWordSpaceResultTable()));
        mainViews[5] = new View("Apriori", null, new JScrollPane(resultPanel.getAprioriResultTable()));
        mainViews[6] = new View("WordSpace & Apriori", null, new JScrollPane(resultPanel.getWAResultTable()));
        mainViews[7] = new View(Translator.getTerm("CorrectConceptPairTable"), null, resultPanel
                .getAcceptedPairPanel());
        mainViews[8] = new View(Translator.getTerm("WrongConceptPairTable"), null, resultPanel
                .getWrongPairPanel());
        mainViews[9] = new View(Translator.getTerm("DefineConceptPairPanel"), null, conceptDefinitionPanel);

        for (int i = 0; i < mainViews.length; i++) {
            viewMap.addView(i, mainViews[i]);
        }
        rootWindow = Utils.createDODDLERootWindow(viewMap);
        setLayout(new BorderLayout());
        add(rootWindow, BorderLayout.CENTER);

        setTableAction();
    }

    public void setXGALayout() {
        TabWindow algorithmTabWindow = new TabWindow(new DockingWindow[] { mainViews[0], mainViews[1]});
        TabWindow parameterTabWindow = new TabWindow(new DockingWindow[] { mainViews[4], mainViews[5], mainViews[6]});
        TabWindow resultTabWindow = new TabWindow(new DockingWindow[] { mainViews[7], mainViews[8]});
        SplitWindow sw1 = new SplitWindow(false, 0.4f, mainViews[3], parameterTabWindow);
        SplitWindow sw2 = new SplitWindow(true, 0.3f, mainViews[2], sw1);
        SplitWindow sw3 = new SplitWindow(false, 0.4f, algorithmTabWindow, sw2);
        SplitWindow sw4 = new SplitWindow(false, 0.25f, mainViews[9], resultTabWindow);
        SplitWindow sw5 = new SplitWindow(false, 0.6f, sw3, sw4);
        rootWindow.setWindow(sw5);
        mainViews[0].restoreFocus();
        mainViews[4].restoreFocus();
        mainViews[7].restoreFocus();
    }

    public void setUXGALayout() {
        SplitWindow algorithmSw = new SplitWindow(true, mainViews[0], mainViews[1]);
        SplitWindow parameterSw1 = new SplitWindow(true, mainViews[4], mainViews[5]);
        SplitWindow parameterSw2 = new SplitWindow(true, 0.66f, parameterSw1, mainViews[6]);
        SplitWindow resultSw = new SplitWindow(true, mainViews[7], mainViews[8]);

        SplitWindow sw1 = new SplitWindow(false, 0.4f, mainViews[3], parameterSw2);
        SplitWindow sw2 = new SplitWindow(true, 0.3f, mainViews[2], sw1);
        SplitWindow sw3 = new SplitWindow(false, 0.3f, algorithmSw, sw2);
        SplitWindow sw4 = new SplitWindow(false, 0.25f, mainViews[9], resultSw);
        SplitWindow sw5 = new SplitWindow(false, 0.6f, sw3, sw4);
        rootWindow.setWindow(sw5);
        mainViews[0].restoreFocus();
        mainViews[4].restoreFocus();
        mainViews[7].restoreFocus();
    }

    public void valueChanged(ListSelectionEvent e) {
        if (inputConceptJList.getSelectedValue() != null) {
            String selectedInputConcept = inputConceptJList.getSelectedValue().toString();
            resultPanel.calcWSandARValue(selectedInputConcept);
        }
    }

    private void setTableAction() {
        resultPanel.getWordSpaceSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    // System.out.println(lsm.getMinSelectionIndex());
                    String c1 = inputConceptJList.getSelectedValue().toString();
                    String c2 = resultPanel.getWSTableRowConceptName(selectedRow);
                    conceptDefinitionPanel.setCText(c1, c2);
                }
            }
        });

        resultPanel.getAprioriSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    String c1 = inputConceptJList.getSelectedValue().toString();
                    String c2 = resultPanel.getARTableRowConceptName(selectedRow);
                    conceptDefinitionPanel.setCText(c1, c2);
                    // System.out.println("-----" + selectedRow);
                }
            }
        });

        resultPanel.getWASelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    // String c1 = comboBox.getSelectedItem().toString();
                    String c1 = inputConceptJList.getSelectedValue().toString();
                    String c2 = resultPanel.getWATableRowConceptName(selectedRow);
                    conceptDefinitionPanel.setCText(c1, c2);
                    // System.out.println("-----" + selectedRow);
                }
            }
        });

    }

    public int getWSResultTableSelectedRow() {
        return resultPanel.getWSTableSelectedRow();
    }

    public int getAprioriResultTableSelectedRow() {
        return resultPanel.getARTableSelectedRow();
    }

    public int getWAResultTableSelectedRow() {
        return resultPanel.getWATableSelectedRow();
    }

    public DefaultTableModel getWSResultTableModel() {
        return resultPanel.getWResultTableModel();
    }

    public DefaultTableModel getAprioriResultTableModel() {
        return resultPanel.getAResultTableModel();
    }

    public DefaultTableModel getWAResultTableModel() {
        return resultPanel.getWAResultTableModel();
    }

    public void saveWordSpaceResult(File dir) {
        algorithmPanel.saveResult(dir, ConceptDefinitionAlgorithmPanel.WORDSPACE);
    }
    
    public void saveWordSpaceResult(int projectID, java.sql.Statement stmt) {
        algorithmPanel.saveResult(projectID, stmt, ConceptDefinitionAlgorithmPanel.WORDSPACE);
    }

    public void loadWordSpaceResult(File dir) {
        algorithmPanel.loadResult(dir, ConceptDefinitionAlgorithmPanel.WORDSPACE);
    }
    
    public void loadWordSpaceResult(int projectID, Statement stmt) {
        algorithmPanel.loadResult(projectID, stmt, ConceptDefinitionAlgorithmPanel.WORDSPACE);
    }

    public void saveAprioriResult(File dir) {
        algorithmPanel.saveResult(dir, ConceptDefinitionAlgorithmPanel.APRIORI);
    }
    
    public void saveAprioriResult(int projectID, java.sql.Statement stmt) {
        algorithmPanel.saveResult(projectID, stmt, ConceptDefinitionAlgorithmPanel.APRIORI);
    }

    public void loadAprioriResult(File dir) {
        algorithmPanel.loadResult(dir, ConceptDefinitionAlgorithmPanel.APRIORI);
    }
    
    public void loadAprioriResult(int projectID, Statement stmt) {
        algorithmPanel.loadResult(projectID, stmt, ConceptDefinitionAlgorithmPanel.APRIORI);
    }

    public void saveConceptDefinition(File file) {
        resultPanel.saveConceptDefinition(file);
    }
    
    public void saveConceptDefinition(int projectID, java.sql.Statement stmt) {
        resultPanel.saveConceptDefinition(projectID, stmt);
    }

    public void loadConceptDefinition(File file) {
        resultPanel.loadConceptDefinition(file);
    }
    
    public void loadConceptDefinition(int projectID, Statement stmt) {
        resultPanel.loadConceptDefinition(projectID, stmt);
    }

    public void saveWrongPairSet(File file) {
        resultPanel.saveWrongPairSet(file);
    }
    
    public void saveWrongPairSet(int projectID, Statement stmt) {
        resultPanel.saveWrongPairSet(projectID, stmt);
    }

    public void loadWrongPairSet(File file) {
        resultPanel.loadWrongPairSet(file);
    }
    
    public void loadWrongPairSet(int projectID, Statement stmt) {
        resultPanel.loadWrongPairSet(projectID, stmt);
    }

    public void saveConeptDefinitionParameters(File file) {
        algorithmPanel.saveConceptDefinitionParameters(file);
    }
    
    public void saveConeptDefinitionParameters(int projectID, Statement stmt) {
        algorithmPanel.saveConceptDefinitionParameters(projectID, stmt);
    }

    public void loadConceptDefinitionParameters(File file) {
        algorithmPanel.loadConceptDefinitionParameters(file);
    }
    
    public void loadConceptDefinitionParameters(int projectID, Statement stmt) {
        algorithmPanel.loadConceptDefinitionParameters(projectID, stmt);
    }

    public void setInputConceptSet() {
        algorithmPanel.setInputConcept();
    }

    public void setInputDocList() {
        resultPanel.setInputDocList();
        setInputConceptSet();
    }

    public ConceptPair getPair(String str, List list) {
        for (int i = 0; i < list.size(); i++) {
            if (((ConceptPair) list.get(i)).getCombinationToString().equals(str)) { return (ConceptPair) list.get(i); }
        }
        return null;
    }

    public boolean contains(List list, ConceptPair pair) {
        for (int i = 0; i < list.size(); i++) {
            if (pair.isSameCombination((ConceptPair) list.get(i))) { return true; }
        }
        return false;
    }

    public List makeValidList(List list) {
        List returnList = new ArrayList();
        List resultA = (ArrayList) list.get(0);
        boolean flag = false;
        for (int j = 0; j < resultA.size(); j++) {
            ConceptPair pair = (ConceptPair) resultA.get(j);
            for (int i = 1; i < list.size(); i++) {
                List resultB = (List) list.get(i);
                flag = contains(resultB, pair);
            }
            if (flag) {
                returnList.add(pair.getCombinationToString());
            }
        }
        return returnList;
    }

    public ConceptPair getSameCombination(ConceptPair pair, List list) {
        for (int i = 0; i < list.size(); i++) {
            ConceptPair item = (ConceptPair) list.get(i);
            if (item.isSameCombination(pair)) { return item; }
        }
        return null;
    }

    public Set<Document> getDocSet() {
        return docSelectionPanel.getDocSet();
    }

    private boolean isVerbConcept(Concept c) {
        if (c.getLocalName().indexOf("UID") != -1) {
            if (doddleProject.getConstructPropertyPanel().isConceptContains(c)) { return true; }
            return false;
        }
        Set<List<Concept>> pathSet = OWLOntologyManager.getPathToRootSet(c.getURI());
        if (pathSet == null) {
            if (c.getNameSpace().equals(DODDLEConstants.EDR_URI)) {
                pathSet = EDRTree.getEDRTree().getPathToRootSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLEConstants.EDRT_URI)) {
                pathSet = EDRTree.getEDRTTree().getPathToRootSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLEConstants.WN_URI)) {
                pathSet = WordNetDic.getPathToRootSet(new Long(c.getLocalName()));
            }
        }
        for (List<Concept> path : pathSet) {
            if (path.size() == 1) { return false; }
            Concept upperConcept = path.get(1); // 事象概念の下位に移動と行為があるため，１とする
            // 移動または行為の下位概念の場合は，動詞と見なす．
            if (upperConcept.getLocalName().equals("ID30f83e") || upperConcept.getLocalName().equals("ID30f801")) { return true; }
        }
        return false;
    }

    public List<String> getInputWordList() {
        List<String> inputWordList = new ArrayList<String>();
        wordCorrespondConceptSetMap = disambiguationPanel.getWordCorrespondConceptSetMap();
        if (wordCorrespondConceptSetMap != null) {
            wordConceptSetMap = disambiguationPanel.getWordConceptSetMap();
            complexWordConceptMap = doddleProject.getConstructClassPanel().getComplexWordConceptMap();
            Set<InputWordModel> inputWordModelSet = disambiguationPanel.getInputWordModelSet();
            for (InputWordModel iwModel : inputWordModelSet) {
                if (!iwModel.isSystemAdded()) {
                    inputWordList.add(iwModel.getWord());
                }
            }
            DefaultListModel undefinedWordListModel = disambiguationPanel.getUndefinedWordListPanel().getModel();
            for (int i = 0; i < undefinedWordListModel.size(); i++) {
                String undefWord = (String) undefinedWordListModel.getElementAt(i);
                inputWordList.add(undefWord);
            }
            Collections.sort(inputWordList);
        }
        return inputWordList;
    }

    public Set<String> getComplexWordSet() {
        Set<String> complexWordSet = new HashSet<String>();
        Set<String> wordSet = wordConceptSetMap.keySet();
        for (String w : wordSet) {
            if (w.indexOf(" ") != -1) {
                complexWordSet.add(w);
            }
        }
        Set<String> partialMatchedWordSet = complexWordConceptMap.keySet();
        if (partialMatchedWordSet != null) {
            complexWordSet.addAll(partialMatchedWordSet);
        }
        return complexWordSet;
    }
}