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

package io.github.doddle_owl.views;

import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.term_selection.TermModel;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.UndoManager;
import io.github.doddle_owl.views.concept_definition.ConceptDefinitionPanel;
import io.github.doddle_owl.views.concept_selection.ConceptSelectionPanel;
import io.github.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;
import io.github.doddle_owl.views.document_selection.DocumentSelectionPanel;
import io.github.doddle_owl.views.reference_ontology_selection.ReferenceOntologySelectionPanel;
import io.github.doddle_owl.views.term_selection.TermSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class DODDLEProjectPanel extends JPanel {

    private final boolean isInitialized;

    private JTabbedPane rootTabbedPane;
    private ReferenceOntologySelectionPanel ontologySelectionPanel;
    private DocumentSelectionPanel documentSelectionPanel;
    private TermSelectionPanel termSelectionPanel;
    private ConceptSelectionPanel conceptSelectionPanel;
    private ClassTreeConstructionPanel constructClassPanel;
    private PropertyTreeConstructionPanel constructPropertyPanel;
    private ConceptDefinitionPanel conceptDefinitionPanel;

    private int userIDCount;
    private Map<String, Concept> uriConceptMap;
    private List<String> logList;
    private UndoManager undoManager;

    public void initProject() {
        ontologySelectionPanel.initialize();
        documentSelectionPanel.initialize();
        termSelectionPanel.initialize();
        conceptSelectionPanel.initialize();
        constructClassPanel.initialize();
        constructPropertyPanel.initialize();
        conceptDefinitionPanel.initialize();
    }

    public DODDLEProjectPanel() {
        try {
            ToolTipManager.sharedInstance().setEnabled(false);
            undoManager = new UndoManager(this);

            userIDCount = 0;
            uriConceptMap = new HashMap<>();
            logList = new ArrayList<>();

            addLog("NewProjectAction");
            constructClassPanel = new ClassTreeConstructionPanel(this);
            ontologySelectionPanel = new ReferenceOntologySelectionPanel();
            constructPropertyPanel = new PropertyTreeConstructionPanel(this);
            conceptSelectionPanel = new ConceptSelectionPanel(constructClassPanel, constructPropertyPanel, this);
            termSelectionPanel = new TermSelectionPanel(conceptSelectionPanel);
            documentSelectionPanel = new DocumentSelectionPanel(termSelectionPanel, this);
            conceptDefinitionPanel = new ConceptDefinitionPanel(this);
            conceptSelectionPanel.setDocumentSelectionPanel(documentSelectionPanel);

            rootTabbedPane = new JTabbedPane();
            rootTabbedPane.addTab(Translator.getTerm("OntologySelectionTab"), ontologySelectionPanel);
            rootTabbedPane.addTab(Translator.getTerm("DocumentSelectionTab"), documentSelectionPanel);
            rootTabbedPane.addTab(Translator.getTerm("TermSelectionTab"), termSelectionPanel);
            rootTabbedPane.addTab(Translator.getTerm("ConceptSelectionTab"), conceptSelectionPanel);
            rootTabbedPane.addTab(Translator.getTerm("ClassTreeConstructionTab"), constructClassPanel);
            rootTabbedPane.addTab(Translator.getTerm("PropertyTreeConstructionTab"), constructPropertyPanel);
            rootTabbedPane.addTab(Translator.getTerm("RelationConstructionTab"), conceptDefinitionPanel);
            setLayout(new BorderLayout());
            add(rootTabbedPane, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isInitialized = true;
            ToolTipManager.sharedInstance().setEnabled(true);
        }
    }


    private String getTerm(String msg) {
        String term = Translator.getTerm(msg);
        if (term == null) {
            term = msg;
        }
        return term;
    }

    public void addLog(String msg) {
        String log = Calendar.getInstance().getTime() + ": " + getTerm(msg);
        logList.add(log);
    }

    public void addLog(String msg, Object option) {
        String log = Calendar.getInstance().getTime() + ": " + getTerm(msg) + ": " + option;
        logList.add(log);
    }

    public void saveLog(File file) {
        if (logList == null) {
            return;
        }
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                for (String log : logList) {
                    writer.write(log);
                    writer.newLine();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadLog(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    logList.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void initUndoManager() {
        undoManager.initUndoManager();
    }

    public void addCommand(Concept parentConcept, Concept targetConcept, String treeType) {
        undoManager.addCommand(parentConcept, targetConcept, treeType);
    }

    public void undo() {
        undoManager.undo();
    }

    public void redo() {
        undoManager.redo();
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    public void resetURIConceptMap() {
        uriConceptMap.clear();
    }

    public void putConcept(String uri, Concept c) {
        uriConceptMap.put(uri, c);
    }

    public Concept getConcept(String uri) {
        return uriConceptMap.get(uri);
    }

    public void initUserIDCount() {
        userIDCount = 0;
    }

    public int getUserIDCount() {
        return userIDCount;
    }

    public String getUserIDStr() {
        return "UID" + (userIDCount++);
    }

    public void setUserIDCount(int id) {
        if (userIDCount < id) {
            userIDCount = id;
        }
    }

    public ReferenceOntologySelectionPanel getOntologySelectionPanel() {
        return ontologySelectionPanel;
    }

    public DocumentSelectionPanel getDocumentSelectionPanel() {
        return documentSelectionPanel;
    }

    public TermSelectionPanel getInputTermSelectionPanel() {
        return termSelectionPanel;
    }

    public ConceptSelectionPanel getConceptSelectionPanel() {
        return conceptSelectionPanel;
    }

    public TermModel makeInputTermModel(String iw) {
        return conceptSelectionPanel.makeInputTermModel(iw);
    }

    public PropertyTreeConstructionPanel getConstructPropertyPanel() {
        return constructPropertyPanel;
    }

    public ClassTreeConstructionPanel getConstructClassPanel() {
        return constructClassPanel;
    }

    public ConceptDefinitionPanel getConceptDefinitionPanel() {
        return conceptDefinitionPanel;
    }

    public boolean isPerfectlyMatchedAmbiguityCntCheckBox() {
        return conceptSelectionPanel.getExactMatchAmbiguityCntCheckBox();
    }

    public boolean isPerfectlyMatchedSystemAddedWordCheckBox() {
        return conceptSelectionPanel.isPerfectlyMatchedSystemAddedTermCheckBox();
    }

    public boolean isPartiallyMatchedAmbiguityCntCheckBox() {
        return conceptSelectionPanel.getPartialMatchAmbiguityCntCheckBox();
    }

    public boolean isPartiallyMatchedCompoundWordCheckBox() {
        return conceptSelectionPanel.getPartialMatchCompoundWordCheckBox();
    }

    public boolean isPartiallyMatchedMatchedWordBox() {
        return conceptSelectionPanel.getPartialMatchMatchedTermBox();
    }

    public void setSelectedIndex(int index) {
        rootTabbedPane.setSelectedIndex(index);
    }

}
