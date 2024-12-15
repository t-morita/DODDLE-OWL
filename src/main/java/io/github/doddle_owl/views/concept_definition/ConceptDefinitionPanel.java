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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.concept_definition.ConceptPair;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.ontology_api.EDR;
import io.github.doddle_owl.models.ontology_api.WordNet;
import io.github.doddle_owl.models.term_selection.TermModel;
import io.github.doddle_owl.utils.OWLOntologyManager;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.views.concept_selection.ConceptSelectionPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * @author shigeta
 * @author Takeshi Morita
 */
public class ConceptDefinitionPanel extends JPanel implements ListSelectionListener {

    private Map<String, Set<Concept>> wordCorrespondConceptSetMap;
    private Map<String, Concept> compoundWordConceptMap;
    private Map<String, Set<Concept>> termConceptSetMap;

    private final DefaultListModel<String> termListModel;
    private final JList termJList;
    private final ConceptDefinitionResultPanel resultPanel;
    private final ConceptDefinitionAlgorithmPanel algorithmPanel;
    private final ConceptDefinitionResultPanel.ConceptDefinitionPanel conceptDefinitionPanel;

    private final DODDLEProjectPanel doddleProjectPanel;
    private final ConceptSelectionPanel conceptSelectionPanel;

    public void initialize() {
        termListModel.clear();
        if (wordCorrespondConceptSetMap != null) {
            wordCorrespondConceptSetMap.clear();
        }
        if (compoundWordConceptMap != null) {
            compoundWordConceptMap.clear();
        }
        if (termConceptSetMap != null) {
            termConceptSetMap.clear();
        }
        resultPanel.initialize();
    }

    public ConceptDefinitionPanel(DODDLEProjectPanel project) {
        doddleProjectPanel = project;
        conceptSelectionPanel = project.getConceptSelectionPanel();

        termListModel = new DefaultListModel();
        termJList = new JList(termListModel);
        termJList.addListSelectionListener(this);

        algorithmPanel = new ConceptDefinitionAlgorithmPanel(termJList, doddleProjectPanel);
        resultPanel = new ConceptDefinitionResultPanel(termJList, algorithmPanel, doddleProjectPanel);
        conceptDefinitionPanel = resultPanel.getDefinePanel();

        var parameterTabbedPane = new JTabbedPane();
        parameterTabbedPane.addTab(Translator.getTerm("WordSpaceParameterPanel"), null,
                algorithmPanel.getWordSpaceParamPanel());
        parameterTabbedPane.addTab(Translator.getTerm("AprioriParameterPanel"), null,
                algorithmPanel.getAprioriParamPanel());

        var resultTabbedPane = new JTabbedPane();
        resultTabbedPane.addTab("WordSpace", null, new JScrollPane(
                resultPanel.getWordSpaceResultTable()));
        resultTabbedPane.addTab("Apriori", null, new JScrollPane(
                resultPanel.getAprioriResultTable()));
        resultTabbedPane.addTab("WordSpace & Apriori", null, new JScrollPane(
                resultPanel.getWAResultTable()));

        var conceptPairsTabbedPane = new JTabbedPane();
        conceptPairsTabbedPane.addTab(Translator.getTerm("CorrectConceptPairTable"), null,
                resultPanel.getAcceptedPairPanel());
        conceptPairsTabbedPane.addTab(Translator.getTerm("WrongConceptPairTable"), null,
                resultPanel.getWrongPairPanel());
        conceptPairsTabbedPane.setPreferredSize(new Dimension(800, 200));

        var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.add(resultPanel.getInputDocPanel());
        splitPane.add(resultTabbedPane);

        var centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(resultPanel.getInputConceptPanel(), BorderLayout.WEST);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        centerPanel.add(conceptDefinitionPanel, BorderLayout.SOUTH);

        var mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.add(centerPanel);
        mainSplitPane.add(conceptPairsTabbedPane);

        setLayout(new BorderLayout());
        add(parameterTabbedPane, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(mainSplitPane, BorderLayout.SOUTH);

        setTableAction();
    }

    public void setTermList() {
        termListModel.clear();
        termListModel.addAll(getInputTermList());
    }

    public Concept getConcept(String word) {
        Concept c = null;
        if (compoundWordConceptMap.get(word) != null) {
            c = compoundWordConceptMap.get(word);
            // System.out.println("cid: " + id);
        } else if (wordCorrespondConceptSetMap.get(word) != null) {
            Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(word);
            c = (Concept) correspondConceptSet.toArray()[0];
            // System.out.println("id: " + id);
        } else {
            Set<Concept> wordConceptSet = termConceptSetMap.get(word);
            if (wordConceptSet != null) {
                c = (Concept) wordConceptSet.toArray()[0];
            }
        }
        if (c == null) {
            return null;
        }
        Concept concept = doddleProjectPanel.getConcept(c.getURI());
        if (concept != null) {
            return concept;
        }

        concept = OWLOntologyManager.getConcept(c.getURI());
        if (concept != null) {
            return concept;
        }
        switch (c.getNameSpace()) {
            case DODDLEConstants.EDR_URI:
                concept = EDR.getEDRConcept(c.getLocalName());
                break;
            case DODDLEConstants.EDRT_URI:
                concept = EDR.getEDRTConcept(c.getLocalName());
                break;
            case DODDLEConstants.WN_URI:
                concept = WordNet.getWNConcept(c.getLocalName());
                break;
        }
        return concept;
    }

    private Resource getResource(Concept c, Model ontology) {
        return ontology.getResource(c.getURI());
    }

    private Property getProperty(Concept c, Model ontology) {
        return ontology.getProperty(c.getURI());
    }

    public Model addConceptDefinition(Model ontology) {
        for (int i = 0; i < resultPanel.getRelationCount(); i++) {
            Object[] relation = resultPanel.getRelation(i);
            boolean isMetaProperty = Boolean.valueOf((String) relation[0]);
            String domainWord = (String) relation[1];
            String rangeWord = (String) relation[3];
            Concept property = (Concept) relation[2];
            Concept domainConcept = getConcept(domainWord);
            Concept rangeConcept = getConcept(rangeWord);

            // System.out.println("r: "+property+"d: "+domainConcept + "r:
            // "+rangeConcept);

            if (property.getLocalName().equals("DID0")) { // agent
                ontology.add(getResource(domainConcept, ontology), RDF.type, OWL.ObjectProperty);
                ontology.add(getResource(domainConcept, ontology), RDFS.domain,
                        getResource(rangeConcept, ontology));
            } else if (property.getLocalName().equals("DID1")) {// object
                ontology.add(getResource(domainConcept, ontology), RDF.type, OWL.ObjectProperty);
                ontology.add(getResource(domainConcept, ontology), RDFS.range,
                        getResource(rangeConcept, ontology));
            } else {
                if (isMetaProperty) {
                    ontology.add(getResource(domainConcept, ontology),
                            getProperty(property, ontology), getResource(rangeConcept, ontology));
                    ontology.add(getResource(property, ontology), RDFS.domain, OWL.Class);
                    ontology.add(getResource(property, ontology), RDFS.range, OWL.Class);
                } else {
                    ontology.add(getResource(property, ontology), RDFS.domain,
                            getResource(domainConcept, ontology));
                    ontology.add(getResource(property, ontology), RDFS.range,
                            getResource(rangeConcept, ontology));
                }
            }
        }
        return ontology;
    }


    public void valueChanged(ListSelectionEvent e) {
        if (termJList.getSelectedValue() != null) {
            String selectedInputConcept = termJList.getSelectedValue().toString();
            resultPanel.calcWSandARValue(selectedInputConcept);
        }
    }

    private void setTableAction() {
        resultPanel.getWordSpaceSelectionModel().addListSelectionListener(
                e -> {
                    if (e.getValueIsAdjusting())
                        return;
                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                    if (lsm.isSelectionEmpty()) {
                    } else {
                        int selectedRow = lsm.getMinSelectionIndex();
                        String c1 = termJList.getSelectedValue().toString();
                        String c2 = resultPanel.getWSTableRowConceptName(selectedRow);
                        conceptDefinitionPanel.setCText(c1, c2);
                    }
                });

        resultPanel.getAprioriSelectionModel().addListSelectionListener(
                e -> {
                    if (e.getValueIsAdjusting())
                        return;
                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                    if (lsm.isSelectionEmpty()) {
                    } else {
                        int selectedRow = lsm.getMinSelectionIndex();
                        String c1 = termJList.getSelectedValue().toString();
                        String c2 = resultPanel.getARTableRowConceptName(selectedRow);
                        conceptDefinitionPanel.setCText(c1, c2);
                    }
                });

        resultPanel.getWASelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
            } else {
                int selectedRow = lsm.getMinSelectionIndex();
                String c1 = termJList.getSelectedValue().toString();
                String c2 = resultPanel.getWATableRowConceptName(selectedRow);
                conceptDefinitionPanel.setCText(c1, c2);
            }
        });

    }

    public void saveWordSpaceResult(File dir) {
        algorithmPanel.saveResult(dir, ConceptDefinitionAlgorithmPanel.WORDSPACE);
    }

    public void loadWordSpaceResult(File dir) {
        algorithmPanel.loadResult(dir, ConceptDefinitionAlgorithmPanel.WORDSPACE);
    }

    public void saveAprioriResult(File dir) {
        algorithmPanel.saveResult(dir, ConceptDefinitionAlgorithmPanel.APRIORI);
    }

    public void loadAprioriResult(File dir) {
        algorithmPanel.loadResult(dir, ConceptDefinitionAlgorithmPanel.APRIORI);
    }

    public void saveConceptDefinition(File file) {
        resultPanel.saveConceptDefinition(file);
    }

    public void loadConceptDefinition(File file) {
        resultPanel.loadConceptDefinition(file);
    }

    public void saveWrongPairSet(File file) {
        resultPanel.saveWrongPairSet(file);
    }

    public void loadWrongPairSet(File file) {
        resultPanel.loadWrongPairSet(file);
    }

    public void saveConeptDefinitionParameters(File file) {
        algorithmPanel.saveConceptDefinitionParameters(file);
    }

    public void loadConceptDefinitionParameters(File file) {
        algorithmPanel.loadConceptDefinitionParameters(file);
    }

    public void setInputConceptSet() {
        algorithmPanel.setInputConcept();
    }

    public void setInputDocList() {
        resultPanel.setInputDocList();
        setInputConceptSet();
    }

    public boolean contains(List list, ConceptPair pair) {
        for (Object o : list) {
            if (pair.isSameCombination((ConceptPair) o)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getInputTermList() {
        List<String> inputTermList = new ArrayList<>();
        wordCorrespondConceptSetMap = conceptSelectionPanel.getTermCorrespondConceptSetMap();
        if (wordCorrespondConceptSetMap != null) {
            termConceptSetMap = conceptSelectionPanel.getTermConceptSetMap();
            compoundWordConceptMap = doddleProjectPanel.getConstructClassPanel()
                    .getCompoundWordConceptMap();
            Set<TermModel> inputWordModelSet = conceptSelectionPanel
                    .getTermModelSet();
            for (TermModel iwModel : inputWordModelSet) {
                if (!iwModel.isSystemAdded()) {
                    inputTermList.add(iwModel.getTerm());
                }
            }
            DefaultListModel undefinedTermListModel = conceptSelectionPanel
                    .getUndefinedTermListPanel().getModel();
            for (int i = 0; i < undefinedTermListModel.size(); i++) {
                String undefTerm = (String) undefinedTermListModel.getElementAt(i);
                inputTermList.add(undefTerm);
            }
            Collections.sort(inputTermList);
        }
        return inputTermList;
    }

}