/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OpenProjectAction extends AbstractAction {

    private File openDir;
    private String title;
    private DODDLE doddle;

    public OpenProjectAction(String title, DODDLE ddl) {
        super(title, Utils.getImageIcon("open.gif"));
        this.title = title;
        doddle = ddl;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    }

    public String getTitle() {
        return title;
    }

    public void openProject() {
        try {
            DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.OpenProjectDone"));
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(23);
            DODDLE.STATUS_BAR.lock();

            DODDLEProject project = new DODDLEProject(openDir.getAbsolutePath(), DODDLE.projectMenu);
            DODDLE.desktop.add(project);
            project.toFront();
            DODDLE.desktop.setSelectedFrame(project);
            DODDLE.STATUS_BAR.addProjectValue();
            DODDLEProject currentProject = (DODDLEProject) DODDLE.desktop.getSelectedFrame();
            OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
            DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
            DocumentSelectionPanel docSelectionPanelI = currentProject.getDocumentSelectionPanel();
            InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
            ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
            openDir.mkdir();
            DODDLE.STATUS_BAR.addProjectValue();
            doddle.loadBaseURI(new File(openDir, ProjectFileNames.PROJECT_INFO_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            docSelectionPanelI.loadDocuments(openDir);
            DODDLE.STATUS_BAR.addProjectValue();
            ontSelectionPanel.loadGeneralOntologyInfo(new File(openDir, ProjectFileNames.GENERAL_ONTOLOGY_INFO_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            ontSelectionPanel.loadOWLMetaDataSet(new File(openDir, ProjectFileNames.OWL_META_DATA_SET_DIR));
            DODDLE.STATUS_BAR.addProjectValue();
            inputWordSelectionPanel.loadWordInfoTable(new File(openDir, ProjectFileNames.WORD_INFO_TABLE_FILE),
                    new File(openDir, ProjectFileNames.REMOVED_WORD_INFO_TABLE_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            File inputWordSetFile = new File(openDir, ProjectFileNames.INPUT_WORD_SET_FILE);
            disambiguationPanel.loadInputWordSet(inputWordSetFile);
            DODDLE.STATUS_BAR.addProjectValue();
            disambiguationPanel.loadWordEvalConceptSet(new File(openDir, ProjectFileNames.WORD_EVAL_CONCEPT_SET_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            if (inputWordSetFile.exists()) {
                disambiguationPanel.loadWordCorrespondConceptSetMap(new File(openDir,
                        ProjectFileNames.INPUT_WORD_CONCEPT_MAP_FILE));
                DODDLE.STATUS_BAR.addProjectValue();
            }
            disambiguationPanel.loadConstructTreeOption(new File(openDir, ProjectFileNames.CONSTRUCT_TREE_OPTION_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            disambiguationPanel.loadInputWordConstructTreeOptionSet(new File(openDir,
                    ProjectFileNames.INPUT_WORD_CONSTRUCT_TREE_OPTION_SET_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            disambiguationPanel.loadInputConceptSet(new File(openDir, ProjectFileNames.INPUT_CONCEPT_SET_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            disambiguationPanel.loadUndefinedWordSet(new File(openDir, ProjectFileNames.UNDEFINED_WORD_SET_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            doddle.loadOntology(currentProject, new File(openDir, ProjectFileNames.ONTOLOGY_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            doddle.loadConceptTypicalWord(currentProject, new File(openDir, ProjectFileNames.CONCEPT_TYPICAL_WORD_MAP_FILE));
            DODDLE.STATUS_BAR.addProjectValue();

            ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
            ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
            constructClassPanel.loadTrimmedResultAnalysis(currentProject, new File(openDir,
                    ProjectFileNames.CLASS_TRIMMED_RESULT_ANALYSIS_FILE));
            constructPropertyPanel.loadTrimmedResultAnalysis(currentProject, new File(openDir,
                    ProjectFileNames.PROPERTY_TRIMMED_RESULT_ANALYSIS_FILE));

            conceptDefinitionPanel.setInputDocList();
            conceptDefinitionPanel.loadConceptDefinitionParameters(new File(openDir,
                    ProjectFileNames.CONCEPT_DEFINITION_PARAMETERS_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            File conceptDefinitionResultDir = new File(openDir, ProjectFileNames.WORDSPACE_RESULTS_DIR);
            conceptDefinitionResultDir.mkdir();
            conceptDefinitionPanel.loadWordSpaceResult(conceptDefinitionResultDir);
            DODDLE.STATUS_BAR.addProjectValue();
            conceptDefinitionResultDir = new File(openDir, ProjectFileNames.APRIORI_RESULTS_DIR);
            conceptDefinitionResultDir.mkdir();
            conceptDefinitionPanel.loadAprioriResult(conceptDefinitionResultDir);
            DODDLE.STATUS_BAR.addProjectValue();

            conceptDefinitionPanel.loadConceptDefinition(new File(openDir, ProjectFileNames.CONCEPT_DEFINITION_FILE));
            DODDLE.STATUS_BAR.addProjectValue();
            conceptDefinitionPanel.loadWrongPairSet(new File(openDir, ProjectFileNames.WRONG_PAIR_SET_FILE));
            DODDLE.STATUS_BAR.addProjectValue();

            disambiguationPanel.selectTopList();
            project.setVisible(true);
            project.setMaximum(true);
            constructClassPanel.expandTree();
            constructPropertyPanel.expandTree();
            DODDLE.STATUS_BAR.addProjectValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE.STATUS_BAR.unLock();
            DODDLE.STATUS_BAR.hideProgressBar();
        }
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_DIR);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        openDir = chooser.getSelectedFile();
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            public String doInBackground() {
                openProject();
                return "done";
            }
        };
        DODDLE.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
