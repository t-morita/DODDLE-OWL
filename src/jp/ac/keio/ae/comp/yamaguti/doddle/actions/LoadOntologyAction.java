/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class LoadOntologyAction extends AbstractAction {

    private DODDLE doddle;

    public LoadOntologyAction(String title, DODDLE ddl) {
        super(title);
        doddle = ddl;
    }
    
    public void loadOntology(DODDLEProject currentProject, File file) {
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        if (!file.exists()) { return; }
        constructClassPanel.init();
        constructPropertyPanel.init();
        currentProject.resetIDConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(disambiguationPanel.getInputConceptSet());
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            Model model = ModelFactory.createDefaultModel();
            model.read(reader, DODDLE.BASE_URI, "RDF/XML");
            currentProject.initUserIDCount();
            TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                    ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI));
            DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
            constructClassPanel.setConceptTreeModel(treeModel);
            constructClassPanel.setVisibleConceptTree(true);
            constructClassPanel.checkMultipleInheritance(treeModel);
            currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
            ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
            constructClassPanel.setConceptDriftManagementResult();
            treeModel.reload();

            currentProject.setUserIDCount(currentProject.getUserIDCount());
            rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                    ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI));
            treeModel = new DefaultTreeModel(rootNode);
            constructPropertyPanel.setConceptTreeModel(treeModel);
            constructPropertyPanel.setVisibleConceptTree(true);
            constructPropertyPanel.checkMultipleInheritance(treeModel);
            ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
            constructPropertyPanel.setConceptDriftManagementResult();
            treeModel.reload();
            currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);

            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_DIR);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            DODDLEProject currentProject = DODDLE.getCurrentProject();
            loadOntology(currentProject, chooser.getSelectedFile());
            currentProject.getConstructClassPanel().expandTree();
            currentProject.getConstructPropertyPanel().expandTree();
        }
    }
}
