/*
 * @(#)  2006/06/04
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class VisualizationPanel extends JPanel implements ActionListener {

    private JButton toMR3Button;
    private JButton toDoddleButton;
    private DODDLEProject currentProject;

    public VisualizationPanel(DODDLEProject project) {
        currentProject = project;
        toMR3Button = new JButton("DODDLE → MR3");
        toMR3Button.addActionListener(this);
        toMR3Button.setFont(new Font("Dialog", Font.BOLD, 16));
        toDoddleButton = new JButton("MR3 → DODDLE");
        toDoddleButton.setFont(new Font("Dialog", Font.BOLD, 16));
        toDoddleButton.addActionListener(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));
        mainPanel.add(toMR3Button);
        mainPanel.add(toDoddleButton);
        
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
    }

    public void toMR3() {
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
        ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
        Model ontology = JenaModelMaker.makeClassModel(constructClassPanel.getTreeModelRoot(), ModelFactory
                .createDefaultModel());
        JenaModelMaker.makePropertyModel(constructPropertyPanel.getTreeModelRoot(), ontology);
        conceptDefinitionPanel.addConceptDefinition(ontology);
        DODDLE.getDODDLEPlugin().replaceRDFSModel(ontology);
    }

    public void toDODDLE() {
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        constructClassPanel.init();
        constructPropertyPanel.init();
        currentProject.resetIDConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(disambiguationPanel.getInputConceptSet());

        Model model = DODDLE.getDODDLEPlugin().getModel();
        currentProject.initUserIDCount();
        TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                ResourceFactory.createResource(DODDLE.BASE_URI + ConceptTreeMaker.DODDLE_CLASS_ROOT_ID));
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        constructClassPanel.setTreeModel(treeModel);
        constructClassPanel.setVisibleConceptTree(true);
        constructClassPanel.checkMultipleInheritance(treeModel);
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructClassPanel.setConceptDriftManagementResult();
        treeModel.reload();

        currentProject.setUserIDCount(currentProject.getUserIDCount());
        rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                ResourceFactory.createResource(DODDLE.BASE_URI + ConceptTreeMaker.DODDLE_PROPERTY_ROOT_ID));
        treeModel = new DefaultTreeModel(rootNode);
        constructPropertyPanel.setTreeModel(treeModel);
        constructPropertyPanel.setVisibleConceptTree(true);
        constructPropertyPanel.checkMultipleInheritance(treeModel);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructPropertyPanel.setConceptDriftManagementResult();
        treeModel.reload();
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == toMR3Button) {
            toMR3();
        } else if (e.getSource() == toDoddleButton) {
            toDODDLE();
        }
    }
}
