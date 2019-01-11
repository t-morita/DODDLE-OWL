/*
 * Project Name: DODDLE-OWL (a Domain Ontology rapiD DeveLopment Environment - OWL extension)

 * Project Website: http://doddle-owl.org/
 *
 * Copyright (C) 2004-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.doddle_owl.views;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.ViewMap;
import org.doddle_owl.DODDLE_OWL;
import org.doddle_owl.models.concept_selection.Concept;
import org.doddle_owl.models.term_selection.TermModel;
import org.doddle_owl.utils.Translator;
import org.doddle_owl.utils.UndoManager;
import org.doddle_owl.utils.Utils;
import org.doddle_owl.views.concept_definition.ConceptDefinitionPanel;
import org.doddle_owl.views.concept_selection.ConceptSelectionPanel;
import org.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import org.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;
import org.doddle_owl.views.document_selection.DocumentSelectionPanel;
import org.doddle_owl.views.reference_ontology_selection.ReferenceOntologySelectionPanel;
import org.doddle_owl.views.term_selection.TermSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * @author Takeshi Morita
 */
public class DODDLEProjectPanel extends JPanel {

    private boolean isInitialized;

    private View[] views;
    private RootWindow rootWindow;
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

    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;

    public void initProject() {
        ontologySelectionPanel.initialize();
        documentSelectionPanel.initialize();
        termSelectionPanel.initialize();
        conceptSelectionPanel.initialize();
        constructClassPanel.initialize();
        constructPropertyPanel.initialize();
        conceptDefinitionPanel.initialize();
    }

    class NewProjectWorker extends SwingWorker<String, String> implements PropertyChangeListener {

        private int taskCnt;
        private int currentTaskCnt;
        private DODDLEProjectPanel project;

        public NewProjectWorker(int taskCnt, DODDLEProjectPanel project) {
            this.taskCnt = taskCnt;
            currentTaskCnt = 1;
            this.project = project;
            addPropertyChangeListener(this);
            DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("NewProjectAction"));
            DODDLE_OWL.STATUS_BAR.startTime();
            DODDLE_OWL.STATUS_BAR.initNormal(taskCnt);
            DODDLE_OWL.STATUS_BAR.lock();
        }

        @Override
        protected String doInBackground() {
            try {
                ToolTipManager.sharedInstance().setEnabled(false);
                undoManager = new UndoManager(project);

                userIDCount = 0;
                uriConceptMap = new HashMap<>();
                logList = new ArrayList<>();

                addLog("NewProjectAction");
                constructClassPanel = new ClassTreeConstructionPanel(project);
                setProgress(currentTaskCnt++);
                ontologySelectionPanel = new ReferenceOntologySelectionPanel();
                setProgress(currentTaskCnt++);
                constructPropertyPanel = new PropertyTreeConstructionPanel(project);
                setProgress(currentTaskCnt++);
                conceptSelectionPanel = new ConceptSelectionPanel(constructClassPanel,
                        constructPropertyPanel, project);
                setProgress(currentTaskCnt++);

                termSelectionPanel = new TermSelectionPanel(conceptSelectionPanel);
                setProgress(currentTaskCnt++);
                documentSelectionPanel = new DocumentSelectionPanel(termSelectionPanel, project);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel = new ConceptDefinitionPanel(project);
                setProgress(currentTaskCnt++);
                conceptSelectionPanel.setDocumentSelectionPanel(documentSelectionPanel);
                /*
                rootTabbedPane = new JTabbedPane();
                rootTabbedPane.addTab(
                        Translator.getTerm("OntologySelectionPanel"),
                        Utils.getImageIcon("reference_ontology_selection.png"),
                        ontologySelectionPanel);
                rootTabbedPane.addTab(
                        Translator.getTerm("DocumentSelectionPanel"),
                        Utils.getImageIcon("input_document_selection.png"),
                        documentSelectionPanel);
                rootTabbedPane.addTab(
                        Translator.getTerm("TermSelectionPanel"),
                        Utils.getImageIcon("input_term_selection.png"),
                        termSelectionPanel);
                rootTabbedPane.addTab(
                        Translator.getTerm("ConceptSelectionPanel"),
                        Utils.getImageIcon("input_concept_selection.png"),
                        conceptSelectionPanel);
                rootTabbedPane.addTab(
                        Translator.getTerm("ClassTreeConstructionPanel"),
                        Utils.getImageIcon("constructing_class_hierarchy.png"),
                        constructClassPanel);
                rootTabbedPane.addTab(
                        Translator.getTerm("PropertyTreeConstructionPanel"),
                        Utils.getImageIcon("constructing_property_hierarchy.png"),
                        constructPropertyPanel);
                rootTabbedPane.addTab(
                        Translator.getTerm("ConceptDefinitionPanel"),
                        Utils.getImageIcon("concept_definition.png"),
                        conceptDefinitionPanel);
                        */
                views = new View[7];
                ViewMap viewMap = new ViewMap();

                views[0] = new View(Translator.getTerm("OntologySelectionPanel"),
                        Utils.getImageIcon("reference_ontology_selection.png"), ontologySelectionPanel);
                views[1] = new View(Translator.getTerm("DocumentSelectionPanel"),
                        Utils.getImageIcon("input_document_selection.png"), documentSelectionPanel);
                views[2] = new View(Translator.getTerm("TermSelectionPanel"),
                        Utils.getImageIcon("input_term_selection.png"), termSelectionPanel);
                views[3] = new View(Translator.getTerm("ConceptSelectionPanel"),
                        Utils.getImageIcon("input_concept_selection.png"), conceptSelectionPanel);
                views[4] = new View(Translator.getTerm("ClassTreeConstructionPanel"),
                        Utils.getImageIcon("constructing_class_hierarchy.png"), constructClassPanel);
                views[5] = new View(Translator.getTerm("PropertyTreeConstructionPanel"),
                        Utils.getImageIcon("constructing_property_hierarchy.png"), constructPropertyPanel);
                views[6] = new View(Translator.getTerm("ConceptDefinitionPanel"),
                        Utils.getImageIcon("concept_definition.png"), conceptDefinitionPanel);

                for (int i = 0; i < views.length; i++) {
                    viewMap.addView(i, views[i]);
                }

                rootWindow = Utils.createDODDLERootWindow(viewMap);
                setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                setProgress(currentTaskCnt++);
                setXGALayoutForAll();
                DODDLE_OWL.rootPane.getContentPane().add(rootWindow, BorderLayout.CENTER);
//                DODDLE_OWL.rootPane.getContentPane().add(rootTabbedPane, BorderLayout.CENTER);
            } catch (NullPointerException npe) {
                setXGALayoutForAll();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                setProgress(currentTaskCnt++);
                isInitialized = true;
                if (taskCnt == 11) {
                    project.setVisible(true); // かならず表示させるため
                    DODDLE_OWL.STATUS_BAR.unLock();
                    DODDLE_OWL.STATUS_BAR.hideProgressBar();
                }
                ToolTipManager.sharedInstance().setEnabled(true);
            }
            return "done";
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE_OWL.STATUS_BAR.setValue(currentTaskCnt);
            }
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

    public DODDLEProjectPanel(int taskCnt) {
        NewProjectWorker worker = new NewProjectWorker(taskCnt, this);
        worker.execute();
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

    public void setXGALayout() {
        rootWindow.setWindow(new TabWindow(new DockingWindow[]{views[0], views[1], views[2],
                views[3], views[4], views[5], views[6]}));
        views[0].restoreFocus();
    }

    public void setXGALayoutForAll() {
        setXGALayout();
        conceptSelectionPanel.setXGALayout();
        constructClassPanel.setXGALayout();
        constructPropertyPanel.setXGALayout();
//        conceptDefinitionPanel.setXGALayout();
    }

    public void setUXGALayoutForAll() {
        setXGALayout();
        conceptSelectionPanel.setUXGALayout();
        constructClassPanel.setUXGALayout();
        constructPropertyPanel.setUXGALayout();
//        conceptDefinitionPanel.setUXGALayout();
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

    public void setSelectedIndex(int i) {
        views[i].restoreFocus();
    }

    public boolean isPerfectlyMatchedAmbiguityCntCheckBox() {
        return conceptSelectionPanel.isPerfectlyMatchedAmbiguityCntCheckBox();
    }

    public boolean isPerfectlyMatchedSystemAddedWordCheckBox() {
        return conceptSelectionPanel.isPerfectlyMatchedSystemAddedTermCheckBox();
    }

    public boolean isPartiallyMatchedAmbiguityCntCheckBox() {
        return conceptSelectionPanel.isPartiallyMatchedAmbiguityCntCheckBox();
    }

    public boolean isPartiallyMatchedCompoundWordCheckBox() {
        return conceptSelectionPanel.isPartiallyMatchedCompoundWordCheckBox();
    }

    public boolean isPartiallyMatchedMatchedWordBox() {
        return conceptSelectionPanel.isPartiallyMatchedMatchedTermBox();
    }

}
