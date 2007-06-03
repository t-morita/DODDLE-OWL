/*
 * @(#)  2007/06/03
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class UndoManager {
    
    private int index;    
    private Command lastCommand;
    private List<Command> commandList;
    private LoadOntologyAction loadOntologyAction;
    private SaveOntologyAction saveOntologyAction;
    private DODDLEProject project;
    
    private static int MAX_COMMAND_CNT = 50;
    
    public UndoManager(DODDLEProject project) {
        index = -1;
        commandList = new ArrayList<Command>();
        loadOntologyAction = new LoadOntologyAction(Translator.getTerm("OpenOWLOntologyAction"),
                LoadOntologyAction.OWL_ONTOLOGY);
        saveOntologyAction = new SaveOntologyAction(Translator.getTerm("SaveOWLOntologyAction"),
                SaveOntologyAction.OWL_ONTOLOGY);
        this.project = project;        
    }
    
    public void initUndoManager() {
        index = -1;
        lastCommand = null;
        commandList.clear();
    }
        
    public void addCommand(Concept parentConcept, Concept targetConcept, String treeType) {
        ++index;
        commandList.add(index, new Command(parentConcept, targetConcept, treeType));
        if (commandList.size() == MAX_COMMAND_CNT+1) {
            commandList.remove(0);
            index = MAX_COMMAND_CNT-1;
        } else if (index+1 < commandList.size()){ // 新たなCommandを追加したら，redoはできなくなる
            for (int i = index+1; i < commandList.size(); i++) {
                commandList.remove(i);
            }
        }
    }

    public void loadFiles() {
        loadOntologyAction.loadOWLOntology(project, lastCommand.getOntFile());
        ConstructClassPanel classPanel = project.getConstructClassPanel();
        ConstructPropertyPanel propertyPanel = project.getConstructPropertyPanel();
        classPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(lastCommand.getClassTRAFile());
        propertyPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(lastCommand.getPropertyTRAFile());
        if (lastCommand.getTargetConcept() != null) {
            if (lastCommand.getTreeType().equals(ConceptTreePanel.CLASS_ISA_TREE)) {
                classPanel.selectIsaTreeNode(lastCommand.getTargetConcept(), lastCommand.getParentConcept());
            } else if (lastCommand.getTreeType().equals(ConceptTreePanel.CLASS_HASA_TREE)) {
                classPanel.selectHasaTreeNode(lastCommand.getTargetConcept(), lastCommand.getParentConcept());
            } else if (lastCommand.getTreeType().equals(ConceptTreePanel.PROPERTY_ISA_TREE)) {
                propertyPanel.selectIsaTreeNode(lastCommand.getTargetConcept(), lastCommand.getParentConcept());
            } else if (lastCommand.getTreeType().equals(ConceptTreePanel.PROPERTY_HASA_TREE)) {
                propertyPanel.selectHasaTreeNode(lastCommand.getTargetConcept(), lastCommand.getParentConcept());
            }
        }
    }
    
    public void undo() {
        if (canUndo()) {
            if (lastCommand != null && lastCommand.equals(commandList.get(index)) && -1 <= index - 2) {
                lastCommand = commandList.get(index-1);
                index -=2;
            } else {
                lastCommand = commandList.get(index--);
            }
            loadFiles();
        }
    }
    
    public void redo() {
        if (canRedo()) {
            if (lastCommand != null && lastCommand.equals(commandList.get(index+1)) && index+2 < commandList.size()) {
                index += 2;
                lastCommand = commandList.get(index);  
            } else {
                lastCommand = commandList.get(++index);  
            }            
            loadFiles();
        }
    }
    
    public boolean canUndo() {
        return 0 <= index;
    }
    
    public boolean canRedo() {
        return index+1 < commandList.size();
    }
    
    class Command {
        private String treeType;
        private Concept parentConcept;
        private Concept targetConcept;
        private File ontFile;
        private File classTRAFile;
        private File propertyTRAFile;
        
        public String getTreeType() {
            return treeType;
        }

        public File getClassTRAFile() {
            return classTRAFile;
        }
        
        public File getPropertyTRAFile() {
            return propertyTRAFile;
        }
        
        public File getOntFile() {
            return ontFile;
        }

        public Concept getParentConcept() {
            return parentConcept;
        }

        public Concept getTargetConcept() {
            return targetConcept;
        }

        public Command(Concept p, Concept t, String type) {
            treeType = type;
            parentConcept = p;
            targetConcept = t;
            try { 
                ontFile = File.createTempFile("doddle_history_ont", null);
                ontFile.deleteOnExit();
                classTRAFile = File.createTempFile("doddle_history_class_tra", null);
                classTRAFile.deleteOnExit();
                propertyTRAFile = File.createTempFile("doddle_history_property_tra", null);
                propertyTRAFile.deleteOnExit();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
            saveOntologyAction.saveOWLOntology(project, ontFile);
            project.getConstructClassPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(classTRAFile);
            project.getConstructPropertyPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(propertyTRAFile);
        }
    }
}
