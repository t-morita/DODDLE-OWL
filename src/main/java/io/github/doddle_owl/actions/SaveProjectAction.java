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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Takeshi Morita
 */
public class SaveProjectAction extends AbstractAction {

    private final String title;
    private final DODDLE_OWL doddle;
    private final FileFilter doddleProjectFileFilter;
    private final FileFilter doddleProjectFolderFilter;

    public String getTitle() {
        return title;
    }

    public SaveProjectAction(String title, DODDLE_OWL ddl) {
        super(title, Utils.getImageIcon("baseline_save_black_18dp.png"));
        this.title = title;
        doddle = ddl;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        doddleProjectFileFilter = new DODDLEProjectFileFilter();
        doddleProjectFolderFilter = new DODDLEProjectFolderFilter();
    }

    SaveProjectAction(String title, ImageIcon icon, DODDLE_OWL ddl) {
        super(title, icon);
        this.title = title;
        doddle = ddl;
        doddleProjectFileFilter = new DODDLEProjectFileFilter();
        doddleProjectFolderFilter = new DODDLEProjectFolderFilter();
    }

    private void removeFiles(File dir) {
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }

    void saveProject(File saveFile, DODDLEProjectPanel currentProject) {
        File saveDir;
        if (saveFile.isDirectory()) {
            saveDir = saveFile;
        } else {
            saveDir = new File(saveFile.getAbsolutePath() + ".dir");
            saveDir.mkdir();
        }
        ReferenceOntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
        ConceptSelectionPanel conceptSelectionPanel = currentProject.getConceptSelectionPanel();
        DocumentSelectionPanel docSelectionPanel = currentProject.getDocumentSelectionPanel();
        TermSelectionPanel termSelectionPanel = currentProject.getInputTermSelectionPanel();
        ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
        DODDLE_OWL.rootFrame.setTitle(saveFile.getAbsolutePath());
        ontSelectionPanel.saveGeneralOntologyInfo(new File(saveDir, ProjectFileNames.GENERAL_ONTOLOGY_INFO_FILE));
        File owlMetaDataSetDir = new File(saveDir, ProjectFileNames.OWL_META_DATA_SET_DIR);
        owlMetaDataSetDir.mkdir();
        removeFiles(owlMetaDataSetDir);
        ontSelectionPanel.saveOWLMetaDataSet(owlMetaDataSetDir);
        // docSelectionPanel.saveDocuments(saveDir); // ファイルの内容のコピーはしないようにした
        docSelectionPanel.saveDocumentInfo(saveDir);
        termSelectionPanel.saveInputTermInfoTable(new File(saveDir, ProjectFileNames.TERM_INFO_TABLE_FILE),
                new File(saveDir, ProjectFileNames.REMOVED_TERM_INFO_TABLE_FILE));
        conceptSelectionPanel.saveInputTermSet(new File(saveDir, ProjectFileNames.INPUT_TERM_SET_FILE));
        conceptSelectionPanel
                .saveTermEvalConceptSet(new File(saveDir, ProjectFileNames.TERM_EVAL_CONCEPT_SET_FILE));
        conceptSelectionPanel.saveTermCorrespondConceptSetMap(new File(saveDir,
                ProjectFileNames.INPUT_TERM_CONCEPT_MAP_FILE));
        conceptSelectionPanel.saveConstructTreeOption(new File(saveDir,
                ProjectFileNames.CONSTRUCT_TREE_OPTION_FILE));
        conceptSelectionPanel.saveInputTermConstructTreeOptionSet(new File(saveDir,
                ProjectFileNames.INPUT_TERM_CONSTRUCT_TREE_OPTION_SET_FILE));
        conceptSelectionPanel.saveInputConceptSet(new File(saveDir, ProjectFileNames.INPUT_CONCEPT_SET_FILE));
        conceptSelectionPanel.saveUndefinedTermSet(new File(saveDir, ProjectFileNames.UNDEFINED_TERM_SET_FILE));
        doddle.saveOntology(currentProject, new File(saveDir, ProjectFileNames.ONTOLOGY_FILE));

        currentProject.getConstructClassPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(
                new File(saveDir, ProjectFileNames.CLASS_TRIMMED_RESULT_ANALYSIS_FILE));
        currentProject.getConstructPropertyPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(
                new File(saveDir, ProjectFileNames.PROPERTY_TRIMMED_RESULT_ANALYSIS_FILE));
        saveProjectInfo(currentProject, new File(saveDir, ProjectFileNames.PROJECT_INFO_FILE));
        conceptDefinitionPanel.saveConeptDefinitionParameters(new File(saveDir,
                ProjectFileNames.CONCEPT_DEFINITION_PARAMETERS_FILE));
        conceptDefinitionPanel.saveConceptDefinition(new File(saveDir, ProjectFileNames.CONCEPT_DEFINITION_FILE));
        conceptDefinitionPanel.saveWrongPairSet(new File(saveDir, ProjectFileNames.WRONG_PAIR_SET_FILE));
        File conceptDefinitionResultDir = new File(saveDir, ProjectFileNames.WORDSPACE_RESULTS_DIR);
        conceptDefinitionResultDir.mkdir();
        removeFiles(conceptDefinitionResultDir);
        conceptDefinitionPanel.saveWordSpaceResult(conceptDefinitionResultDir);
        conceptDefinitionResultDir = new File(saveDir, ProjectFileNames.APRIORI_RESULTS_DIR);
        conceptDefinitionResultDir.mkdir();
        removeFiles(conceptDefinitionResultDir);
        conceptDefinitionPanel.saveAprioriResult(conceptDefinitionResultDir);
        currentProject.addLog("SaveProjectAction");
        currentProject.saveLog(new File(saveDir, ProjectFileNames.LOG_FILE));
        if (!saveFile.isDirectory()) {
            zipProjectDir(saveFile, saveDir);
        }
        DODDLE_OWL.STATUS_BAR.setText(Translator.getTerm("SaveProjectDoneMessage") + " ----- "
                + java.util.Calendar.getInstance().getTime() + ": " + DODDLE_OWL.rootFrame.getTitle());
    }

    private void saveProjectInfo(DODDLEProjectPanel currentProject, File file) {
        ReferenceOntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
        ConceptSelectionPanel conceptSelectionPanel = currentProject.getConceptSelectionPanel();
        ClassTreeConstructionPanel constructClassPanel = currentProject.getConstructClassPanel();
        PropertyTreeConstructionPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
        try {
            StringBuilder buf = new StringBuilder();
            buf.append(DODDLEConstants.BASE_URI).append(System.lineSeparator());

            buf.append(Translator.getTerm("AvailableGeneralOntologiesMessage")).append(": ").append(ontSelectionPanel.getEnableDicList()).append(System.lineSeparator());
            if (conceptSelectionPanel.getTermModelSet() != null) {
                buf.append(Translator.getTerm("InputTermCountMessage")).append(": ").append(conceptSelectionPanel.getInputTermCnt()).append(System.lineSeparator());
            }
            buf.append(Translator.getTerm("ExactMatchTermCountMessage")).append(": ").append(conceptSelectionPanel.getPerfectlyMatchedTermCnt()).append(System.lineSeparator());
            buf.append(Translator.getTerm("SystemAddedExactMatchTermCountMessage")).append(": ").append(conceptSelectionPanel.getSystemAddedPerfectlyMatchedTermCnt()).append(System.lineSeparator());
            buf.append(Translator.getTerm("PartialMatchTermCountMessage")).append(": ").append(conceptSelectionPanel.getPartiallyMatchedTermCnt()).append(System.lineSeparator());
            buf.append(Translator.getTerm("MatchedTermCountMessage")).append(": ").append(conceptSelectionPanel.getMatchedTermCnt()).append(System.lineSeparator());
            buf.append(Translator.getTerm("UndefinedTermCountMessage")).append(": ").append(conceptSelectionPanel.getUndefinedTermCnt()).append(System.lineSeparator());

            if (conceptSelectionPanel.getInputConceptSet() != null) {
                buf.append(Translator.getTerm("InputConceptCountMessage")).append(": ").append(conceptSelectionPanel.getInputConceptSet().size()).append(System.lineSeparator());
            }
            if (conceptSelectionPanel.getInputNounConceptSet() != null) {
                buf.append(Translator.getTerm("InputNounConceptCountMessage")).append(": ").append(conceptSelectionPanel.getInputNounConceptSet().size()).append(System.lineSeparator());
            }
            if (conceptSelectionPanel.getInputVerbConceptSet() != null) {
                buf.append(Translator.getTerm("InputVerbConceptCountMessage")).append(": ").append(conceptSelectionPanel.getInputVerbConceptSet().size()).append(System.lineSeparator());
            }

            buf.append(Translator.getTerm("ClassSINCountMessage")).append(": ").append(constructClassPanel.getAddedSINNum()).append(System.lineSeparator());
            buf.append(Translator.getTerm("BeforeTrimmingClassCountMessage")).append(": ").append(constructClassPanel.getBeforeTrimmingConceptNum()).append(System.lineSeparator());
            buf.append(Translator.getTerm("TrimmedClassCountMessage")).append(": ").append(constructClassPanel.getTrimmedConceptNum()).append(System.lineSeparator());
            int afterTrimmingConceptNum = constructClassPanel.getAfterTrimmingConceptNum();
            buf.append(Translator.getTerm("AfterTrimmingClassCountMessage")).append(": ").append(afterTrimmingConceptNum).append(System.lineSeparator());

            buf.append(Translator.getTerm("PropertySINCountMessage")).append(": ").append(constructPropertyPanel.getAddedSINNum()).append(System.lineSeparator());
            buf.append(Translator.getTerm("BeforeTrimmingPropertyCountMessage")).append(": ").append(constructPropertyPanel.getBeforeTrimmingConceptNum()).append(System.lineSeparator());
            buf.append(Translator.getTerm("TrimmedPropertyCountMessage")).append(": ").append(constructPropertyPanel.getTrimmedConceptNum()).append(System.lineSeparator());
            int afterTrimmingPropertyNum = constructPropertyPanel.getAfterTrimmingConceptNum();
            buf.append(Translator.getTerm("AfterTrimmingPropertyCountMessage")).append(": ").append(afterTrimmingPropertyNum).append(System.lineSeparator());

            buf.append(Translator.getTerm("AbstractInternalClassCountMessage")).append(": ").append(constructClassPanel.getAddedAbstractCompoundConceptCnt()).append(System.lineSeparator());
            buf.append(Translator.getTerm("AverageAbstractSiblingConceptCountInClassesMessage")).append(": ").append(constructClassPanel.getAverageAbstracCompoundConceptGroupSiblingConceptCnt()).append(System.lineSeparator());

            buf.append(Translator.getTerm("AbstractInternalPropertyCountMessage")).append(": ").append(constructPropertyPanel.getAddedAbstractCompoundConceptCnt()).append(System.lineSeparator());
            buf.append(Translator.getTerm("AverageAbstractSiblingConceptCountInPropertiesMessage")).append(": ").append(constructPropertyPanel.getAverageAbstracCompoundConceptGroupSiblingConceptCnt()).append(System.lineSeparator());

            int lastClassNum = constructClassPanel.getAllConceptCnt();
            int lastPropertyNum = constructPropertyPanel.getAllConceptCnt();

            buf.append(Translator.getTerm("ClassFromCompoundWordCountMessage")).append(": ").append(lastClassNum - afterTrimmingConceptNum).append(System.lineSeparator());
            buf.append(Translator.getTerm("PropertyFromCompoundWordCountMessage")).append(": ").append(lastPropertyNum - afterTrimmingPropertyNum).append(System.lineSeparator());

            buf.append(Translator.getTerm("TotalClassCountMessage")).append(": ").append(lastClassNum).append(System.lineSeparator());
            buf.append(Translator.getTerm("TotalPropertyCountMessage")).append(": ").append(lastPropertyNum).append(System.lineSeparator());

            buf.append(Translator.getTerm("AverageSiblingClassesMessage")).append(": ").append(constructClassPanel.getChildCntAverage()).append(System.lineSeparator());
            buf.append(Translator.getTerm("AverageSiblingPropertiesMessage")).append(": ").append(constructPropertyPanel.getChildCntAverage()).append(System.lineSeparator());

            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int EOF = -1;

    private void zipProjectDir(File saveFile, File saveDir) {
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(saveFile));
            List<File> allFile = new ArrayList<>();
            getAllProjectFile(saveDir, allFile);
            for (File file : allFile) {
                ZipEntry entry = new ZipEntry(file.getPath());
                zos.putNextEntry(entry);
                bis = new BufferedInputStream(new FileInputStream(file));
                int count;
                byte[] buf = new byte[1024];
                while ((count = bis.read(buf, 0, 104)) != EOF) {
                    zos.write(buf, 0, count);
                }
                bis.close();
                zos.closeEntry();
                file.delete();
            }
            for (File dir : saveDir.listFiles()) {
                dir.delete();
            }
            saveDir.delete();
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (zos != null) {
                    zos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getAllProjectFile(File dir, List<File> allFile) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                getAllProjectFile(f, allFile);
            } else {
                allFile.add(f);
            }
        }
    }

    File getSaveFile() {
        JFileChooser fileChooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.addChoosableFileFilter(doddleProjectFileFilter);
        fileChooser.addChoosableFileFilter(doddleProjectFolderFilter);
        int retval = fileChooser.showSaveDialog(DODDLE_OWL.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        String selectedFilterDescription = fileChooser.getFileFilter().getDescription();
        File saveFile = fileChooser.getSelectedFile();
        if (saveFile.isFile() && !saveFile.getName().endsWith(".ddl")) {
            saveFile = new File(saveFile.getAbsolutePath() + ".ddl");
        } else if (!saveFile.exists()) {
            if (selectedFilterDescription.equals(Translator.getTerm("DODDLEProjectFolderFilter"))) {
                saveFile.mkdir();
            } else if (selectedFilterDescription.equals(Translator.getTerm("DODDLEProjectFileFilter"))) {
                if (!saveFile.getName().endsWith(".ddl")) {
                    saveFile = new File(saveFile.getAbsolutePath() + ".ddl");
                }
            }
        }
        return saveFile;
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProjectPanel currentProject = DODDLE_OWL.getCurrentProject();
        if (currentProject == null) {
            return;
        }
        File saveFile;
        if (!DODDLE_OWL.rootFrame.getTitle().equals(Translator.getTerm("NewProjectAction"))) {
            saveFile = new File(DODDLE_OWL.rootFrame.getTitle());
        } else {
            saveFile = getSaveFile();
        }
        if (saveFile != null) {
            saveProject(saveFile, currentProject);
        }
    }
}
