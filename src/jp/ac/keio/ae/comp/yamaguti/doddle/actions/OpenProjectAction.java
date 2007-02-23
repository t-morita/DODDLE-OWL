/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OpenProjectAction extends AbstractAction {

    private String title;
    private File openFile;
    private DODDLE doddle;
    private DODDLEProject newProject;

    public OpenProjectAction(String title, DODDLE ddl) {
        super(title, Utils.getImageIcon("open.gif"));
        this.title = title;
        doddle = ddl;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    }

    public String getTitle() {
        return title;
    }

    protected static final int EOF = -1;

    public static void getEntry(ZipFile zipFile, ZipEntry target) {
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
        } catch (ZipException ze) {
            ze.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }catch(Exception e) {
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
            for (Enumeration enumeration = projectFile.entries(); enumeration.hasMoreElements();) {
                entry = (ZipEntry) enumeration.nextElement();
                getEntry(projectFile, entry);
            }
            openDir = new File(entry.getName()).getParentFile();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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

    public void openProject(DODDLEProject newProject) {
        try {            
            DODDLEProject currentProject = DODDLE.getCurrentProject();
            OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
            DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
            DocumentSelectionPanel docSelectionPanelI = currentProject.getDocumentSelectionPanel();
            InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
            ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
            File openDir = null;
            if (openFile.isDirectory()) {
                openDir = openFile;
            } else {
                openDir = unzipProjectDir(openFile);
            }
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
            //doddle.loadConceptTypicalWord(currentProject, new File(openDir,
              //      ProjectFileNames.CONCEPT_TYPICAL_WORD_MAP_FILE));
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
            newProject.setVisible(true);
            newProject.setMaximum(true);  // setVisibleより前にしてしまうと，初期サイズ(800x600)で最大化されてしまう
            disambiguationPanel.selectTopList();            
            constructClassPanel.expandTree();
            constructPropertyPanel.expandTree();
            
            if (!openFile.isDirectory()) {
                List<File> allFile = new ArrayList<File>();
                getAllProjectFile(openDir, allFile);
                for (File file: allFile) {
                    file.delete();
                }
                File[] dirs = openDir.listFiles();
                for (int i = 0; i < dirs.length; i++) {
                    dirs[i].delete();
                }
                openDir.delete();
            }
            DODDLE.STATUS_BAR.addProjectValue();                                   
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE.STATUS_BAR.unLock();
            DODDLE.STATUS_BAR.hideProgressBar();
        }
    }
    
    private void getAllProjectFile(File dir, List<File> allFile) {
        File[] files = dir.listFiles();
        for( int i=0; i< files.length; i++ ) {
            if( files[i].isDirectory() ) {
                getAllProjectFile(files[i], allFile);
            } else {
                allFile.add(files[i]);
            }
        }
    }
        
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_HOME);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        openFile = chooser.getSelectedFile();
        DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.OpenProjectDone"));
        DODDLE.STATUS_BAR.startTime();
        DODDLE.STATUS_BAR.initNormal(23);
        DODDLE.STATUS_BAR.lock();
        newProject = new DODDLEProject(openFile.getAbsolutePath(), DODDLE.projectMenu);
        DODDLE.desktop.add(newProject);
        newProject.toFront();            
        DODDLE.desktop.setSelectedFrame(newProject);
        DODDLE.STATUS_BAR.addProjectValue();        
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            public String doInBackground() {
                openProject(newProject);
                return "done";
            }
        };
        DODDLE.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
