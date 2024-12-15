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

package io.github.doddle_owl.actions;

import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLEProjectFileFilter;
import io.github.doddle_owl.models.common.DODDLEProjectFolderFilter;
import io.github.doddle_owl.models.common.ProjectFileNames;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.concept_definition.ConceptDefinitionPanel;
import io.github.doddle_owl.views.concept_selection.ConceptSelectionPanel;
import io.github.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;
import io.github.doddle_owl.views.document_selection.DocumentSelectionPanel;
import io.github.doddle_owl.views.reference_ontology_selection.ReferenceOntologySelectionPanel;
import io.github.doddle_owl.views.term_selection.TermSelectionPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Takeshi Morita
 */
public class OpenProjectAction extends AbstractAction {

    String title;
    DODDLE_OWL doddle;
    File openFile;
    private FileFilter doddleProjectFileFilter;
    private FileFilter doddleProjectFolderFilter;

    OpenProjectAction() {
    }

    public OpenProjectAction(String title, DODDLE_OWL ddl) {
        super(title, Utils.getImageIcon("baseline_open_in_new_black_18dp.png"));
        this.title = title;
        doddle = ddl;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        doddleProjectFileFilter = new DODDLEProjectFileFilter();
        doddleProjectFolderFilter = new DODDLEProjectFolderFilter();
    }

    public String getTitle() {
        return title;
    }

    private static final int EOF = -1;

    private static void getEntry(ZipFile zipFile, ZipEntry target) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            File file = new File(target.getName());
            if (target.isDirectory()) {
                file.mkdirs();
            } else {
                bis = new BufferedInputStream(zipFile.getInputStream(target));
                String parentName;
                if ((parentName = file.getParent()) != null) {
                    File dir = new File(parentName);
                    dir.mkdirs();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
                int c;
                while ((c = bis.read()) != EOF) {
                    bos.write((byte) c);
                }
                bis.close();
                bos.close();
            }
        } catch (IOException ze) {
            ze.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File unzipProjectDir(File openFile) {
        BufferedInputStream bis = null;
        File openDir = null;
        try {
            ZipFile projectFile = new ZipFile(openFile);
            ZipEntry entry = null;
            for (Enumeration enumeration = projectFile.entries(); enumeration.hasMoreElements(); ) {
                entry = (ZipEntry) enumeration.nextElement();
                getEntry(projectFile, entry);
            }
            openDir = new File(entry.getName()).getParentFile();
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return openDir;
    }

    private void getAllProjectFile(File dir, List<File> allFile) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                getAllProjectFile(file, allFile);
            } else {
                allFile.add(file);
            }
        }
    }

    class OpenProjectWorker extends SwingWorker<String, String> implements PropertyChangeListener {

        private int currentTaskCnt;

        OpenProjectWorker(int taskCnt) {
            currentTaskCnt = taskCnt;
            addPropertyChangeListener(this);
        }

        public String doInBackground() {
            while (!DODDLE_OWL.doddleProjectPanel.isInitialized()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            DODDLE_OWL.STATUS_BAR.setLastMessage(title);
            setProgress(currentTaskCnt++);
            try {
                DODDLEProjectPanel currentProject = DODDLE_OWL.doddleProjectPanel;

                ReferenceOntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
                ConceptSelectionPanel conceptSelectionPanel = currentProject.getConceptSelectionPanel();
                DocumentSelectionPanel docSelectionPanelI = currentProject.getDocumentSelectionPanel();
                TermSelectionPanel termSelectionPanel = currentProject.getInputTermSelectionPanel();
                ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
                File openDir;
                List<String> recentProjects = doddle.loadRecentProject();
                recentProjects.remove(openFile.getAbsolutePath());
                recentProjects.add(0, openFile.getAbsolutePath());
                doddle.saveRecentProject(recentProjects);
                if (openFile.isDirectory()) {
                    openDir = openFile;
                } else {
                    openDir = unzipProjectDir(openFile);
                }
                DODDLE_OWL.STATUS_BAR.printMessage(Translator.getTerm("OpenProjectAction") + ": "
                        + openFile.toString());

                currentProject.loadLog(new File(openDir, ProjectFileNames.LOG_FILE));
                currentProject.addLog("OpenProjectAction");
                setProgress(currentTaskCnt++);
                doddle.loadBaseURI(new File(openDir, ProjectFileNames.PROJECT_INFO_FILE));
                setProgress(currentTaskCnt++);
                docSelectionPanelI.loadDocuments(openDir);
                setProgress(currentTaskCnt++);
                ontSelectionPanel.loadGeneralOntologyInfo(new File(openDir, ProjectFileNames.GENERAL_ONTOLOGY_INFO_FILE));
                setProgress(currentTaskCnt++);
                ontSelectionPanel.loadOWLMetaDataSet(new File(openDir, ProjectFileNames.OWL_META_DATA_SET_DIR));
                setProgress(currentTaskCnt++);
                termSelectionPanel.loadInputTermInfoTable(new File(openDir,
                        ProjectFileNames.TERM_INFO_TABLE_FILE), new File(openDir,
                        ProjectFileNames.REMOVED_TERM_INFO_TABLE_FILE));
                setProgress(currentTaskCnt++);
                File inputTermSetFile = new File(openDir, ProjectFileNames.INPUT_TERM_SET_FILE);
                conceptSelectionPanel.loadInputTermSet(inputTermSetFile, currentTaskCnt);
                setProgress(currentTaskCnt++);
                while (!conceptSelectionPanel.isLoadInputTermSet()) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                DODDLE_OWL.STATUS_BAR.printMessage(Translator
                        .getTerm("OpenInputConceptSelectionResultAction"));
                conceptSelectionPanel.loadTermEvalConceptSet(new File(openDir,
                        ProjectFileNames.TERM_EVAL_CONCEPT_SET_FILE));
                setProgress(currentTaskCnt++);
                if (inputTermSetFile.exists()) {
                    DODDLE_OWL.STATUS_BAR.printMessage(Translator
                            .getTerm("OpenInputTermConceptMapAction"));
                    conceptSelectionPanel.loadTermCorrespondConceptSetMap(new File(openDir,
                            ProjectFileNames.INPUT_TERM_CONCEPT_MAP_FILE));
                }
                setProgress(currentTaskCnt++);
                conceptSelectionPanel.loadConstructTreeOption(new File(openDir,
                        ProjectFileNames.CONSTRUCT_TREE_OPTION_FILE));
                setProgress(currentTaskCnt++);
                conceptSelectionPanel.loadInputTermConstructTreeOptionSet(new File(openDir,
                        ProjectFileNames.INPUT_TERM_CONSTRUCT_TREE_OPTION_SET_FILE));
                setProgress(currentTaskCnt++);
                DODDLE_OWL.STATUS_BAR.printMessage(Translator.getTerm("OpenInputConceptSetAction"));
                conceptSelectionPanel.loadInputConceptSet(new File(openDir,
                        ProjectFileNames.INPUT_CONCEPT_SET_FILE));
                setProgress(currentTaskCnt++);
                conceptSelectionPanel.loadUndefinedTermSet(new File(openDir,
                        ProjectFileNames.UNDEFINED_TERM_SET_FILE));
                setProgress(currentTaskCnt++);
                DODDLE_OWL.STATUS_BAR.printMessage(Translator.getTerm("OpenOWLOntologyAction"));
                doddle.loadOntology(currentProject, new File(openDir,
                        ProjectFileNames.ONTOLOGY_FILE));
                setProgress(currentTaskCnt++);

                ClassTreeConstructionPanel constructClassPanel = currentProject.getConstructClassPanel();
                PropertyTreeConstructionPanel constructPropertyPanel = currentProject
                        .getConstructPropertyPanel();
                constructClassPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(
                        new File(openDir, ProjectFileNames.CLASS_TRIMMED_RESULT_ANALYSIS_FILE));
                constructPropertyPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(
                        new File(openDir, ProjectFileNames.PROPERTY_TRIMMED_RESULT_ANALYSIS_FILE));

                conceptDefinitionPanel.setInputDocList();
                conceptDefinitionPanel.loadConceptDefinitionParameters(new File(openDir,
                        ProjectFileNames.CONCEPT_DEFINITION_PARAMETERS_FILE));
                setProgress(currentTaskCnt++);
                File conceptDefinitionResultDir = new File(openDir,
                        ProjectFileNames.WORDSPACE_RESULTS_DIR);
                conceptDefinitionResultDir.mkdir();
                conceptDefinitionPanel.loadWordSpaceResult(conceptDefinitionResultDir);
                setProgress(currentTaskCnt++);
                conceptDefinitionResultDir = new File(openDir, ProjectFileNames.APRIORI_RESULTS_DIR);
                conceptDefinitionResultDir.mkdir();
                conceptDefinitionPanel.loadAprioriResult(conceptDefinitionResultDir);
                setProgress(currentTaskCnt++);

                conceptDefinitionPanel.loadConceptDefinition(new File(openDir,
                        ProjectFileNames.CONCEPT_DEFINITION_FILE));
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.loadWrongPairSet(new File(openDir,
                        ProjectFileNames.WRONG_PAIR_SET_FILE));
                setProgress(currentTaskCnt++);

                conceptSelectionPanel.selectTopList();
                constructClassPanel.expandIsaTree();
                constructClassPanel.expandHasaTree();
                constructPropertyPanel.expandIsaTree();

                if (!openFile.isDirectory()) {
                    List<File> allFile = new ArrayList<>();
                    getAllProjectFile(openDir, allFile);
                    for (File file : allFile) {
                        file.delete();
                    }
                    for (File dir : openDir.listFiles()) {
                        dir.delete();
                    }
                    openDir.delete();
                }
                setProgress(currentTaskCnt++);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DODDLE_OWL.doddleProjectPanel.setVisible(true);
                setProgress(currentTaskCnt++);
                DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("OpenProjectDoneMessage"));
                DODDLE_OWL.STATUS_BAR.setCurrentTime();
                DODDLE_OWL.STATUS_BAR.unLock();
                DODDLE_OWL.STATUS_BAR.hideProgressBar();
            }
            return "done";
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE_OWL.STATUS_BAR.setValue(currentTaskCnt);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.addChoosableFileFilter(doddleProjectFileFilter);
        chooser.addChoosableFileFilter(doddleProjectFolderFilter);
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) {
            return;
        }
        openFile = chooser.getSelectedFile();
        DODDLE_OWL.doddleProjectPanel.initProject();
        OpenProjectWorker worker = new OpenProjectWorker(11);
        DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
