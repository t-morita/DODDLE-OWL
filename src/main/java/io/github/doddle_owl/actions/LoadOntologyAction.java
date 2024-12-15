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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.models.common.FreeMindFileFilter;
import io.github.doddle_owl.models.common.TurtleFileFilter;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.concept_tree.ConceptTreeNode;
import io.github.doddle_owl.models.concept_tree.VerbConcept;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.utils.ConceptTreeMaker;
import io.github.doddle_owl.utils.FreeMindModelMaker;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.concept_selection.ConceptSelectionPanel;
import io.github.doddle_owl.views.concept_tree.ConceptTreePanel;
import io.github.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * @author Takeshi Morita
 */
public class LoadOntologyAction extends AbstractAction {

    private final String conversionType;
    private final FileFilter turtleFileFilter;
    private final FileFilter freeMindFileFilter;
    public static final String OWL_ONTOLOGY = "OWL";
    public static final String FREEMIND_ONTOLOGY = "FREEMIND";

    public LoadOntologyAction(String title, String type) {
        super(title);
        conversionType = type;
        turtleFileFilter = new TurtleFileFilter();
        freeMindFileFilter = new FreeMindFileFilter();
    }

    private void loadFreeMindOntology(DODDLEProjectPanel currentProject, File file) {
        ConceptSelectionPanel conceptSelectionPanel = currentProject
                .getConceptSelectionPanel();
        ClassTreeConstructionPanel constructClassPanel = currentProject.getConstructClassPanel();
        PropertyTreeConstructionPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        if (!file.exists()) {
            return;
        }
        constructClassPanel.initialize();
        constructPropertyPanel.initialize();
        currentProject.resetURIConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(
                conceptSelectionPanel.getInputConceptSet());

        Element docElement = FreeMindModelMaker.getDocumentElement(file);
        Element rootNode = null;
        Element nounRootNode = null;
        Element verbRootNode = null;
        NodeList rootNodeList = docElement.getChildNodes();
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            if (rootNodeList.item(i).getNodeName().equals("node")) {
                rootNode = (Element) rootNodeList.item(i);
            }
        }
        rootNodeList = rootNode.getChildNodes();
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            if (rootNodeList.item(i).getNodeName().equals("node")) {
                rootNode = (Element) rootNodeList.item(i);
                if (rootNode.getAttribute("ID").equals(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI)) {
                    nounRootNode = rootNode;
                } else if (rootNode.getAttribute("ID").equals(
                        ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI)) {
                    verbRootNode = rootNode;
                }
            }
        }

        Concept rootNounConcept = new VerbConcept(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI, "");
        rootNounConcept.addLabel(new DODDLELiteral("ja", "名詞的概念 (Is-a)"));
        rootNounConcept.addLabel(new DODDLELiteral("en", "Is-a Root Class"));
        ConceptTreeNode rootTreeNode = new ConceptTreeNode(rootNounConcept,
                DODDLE_OWL.getCurrentProject());
        FreeMindModelMaker.setConceptTreeModel(rootTreeNode, nounRootNode);

        currentProject.initUserIDCount();
        DefaultTreeModel treeModel = new DefaultTreeModel(rootTreeNode);
        treeModel = constructClassPanel.setConceptTreeModel(treeModel);
        constructClassPanel.setVisibleIsaTree(true);
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructClassPanel.setConceptDriftManagementResult();
        treeModel.reload();

        currentProject.setUserIDCount(currentProject.getUserIDCount());
        VerbConcept rootVerbConcept = new VerbConcept(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI, "");
        rootVerbConcept.addLabel(new DODDLELiteral("ja", "動詞的概念"));
        rootVerbConcept.addLabel(new DODDLELiteral("en", "Root Property"));
        rootTreeNode = new ConceptTreeNode(rootVerbConcept, DODDLE_OWL.getCurrentProject());
        FreeMindModelMaker.setConceptTreeModel(rootTreeNode, verbRootNode);
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeModel = constructPropertyPanel.setConceptTreeModel(treeModel);
        constructPropertyPanel.setVisibleIsaTree(true);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructPropertyPanel.setConceptDriftManagementResult();
        treeModel.reload();
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        expandTrees(currentProject);
    }

    public void loadOWLOntology(DODDLEProjectPanel currentProject, File file) {
        if (file.exists()) {
            loadOWLOntology(currentProject, RDFDataMgr.loadModel(file.getAbsolutePath()));
        }
    }

    private void loadOWLOntology(DODDLEProjectPanel currentProject, Model model) {
        ConceptSelectionPanel conceptSelectionPanel = currentProject
                .getConceptSelectionPanel();
        ClassTreeConstructionPanel constructClassPanel = currentProject.getConstructClassPanel();
        PropertyTreeConstructionPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        constructClassPanel.initialize();
        constructPropertyPanel.initialize();
        currentProject.resetURIConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(
                conceptSelectionPanel.getInputConceptSet());

        currentProject.initUserIDCount();
        TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject,
                model, ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI),
                ConceptTreePanel.CLASS_ISA_TREE);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treeModel = constructClassPanel.setConceptTreeModel(treeModel);
        constructClassPanel.setVisibleIsaTree(true);
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructClassPanel.setConceptDriftManagementResult();
        treeModel.reload();

        rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI),
                ConceptTreePanel.CLASS_HASA_TREE);
        treeModel = new DefaultTreeModel(rootNode);
        constructClassPanel.setHasaTreeModel(treeModel);
        constructClassPanel.setVisibleHasaTree(true);
        treeModel.reload();

        currentProject.setUserIDCount(currentProject.getUserIDCount());
        rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI),
                ConceptTreePanel.PROPERTY_ISA_TREE);
        treeModel = new DefaultTreeModel(rootNode);
        treeModel = constructPropertyPanel.setConceptTreeModel(treeModel);
        constructPropertyPanel.setVisibleIsaTree(true);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructPropertyPanel.setConceptDriftManagementResult();
        treeModel.reload();

        rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI),
                ConceptTreePanel.PROPERTY_HASA_TREE);
        treeModel = new DefaultTreeModel(rootNode);
        constructPropertyPanel.setHasaTreeModel(treeModel);
        constructPropertyPanel.setVisibleHasaTree(true);
        treeModel.reload();

        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        expandTrees(currentProject);
    }

    private void expandTrees(DODDLEProjectPanel currentProject) {
        currentProject.getConstructClassPanel().expandIsaTree();
        currentProject.getConstructClassPanel().expandHasaTree();
        currentProject.getConstructPropertyPanel().expandIsaTree();
        currentProject.getConstructPropertyPanel().expandHasaTree();
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        if (conversionType.equals(OWL_ONTOLOGY)) {
            chooser.addChoosableFileFilter(turtleFileFilter);
        } else if (conversionType.equals(FREEMIND_ONTOLOGY)) {
            chooser.addChoosableFileFilter(freeMindFileFilter);
        }
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            DODDLEProjectPanel currentProject = DODDLE_OWL.getCurrentProject();
            if (conversionType.equals(OWL_ONTOLOGY)) {
                loadOWLOntology(currentProject, chooser.getSelectedFile());
                DODDLE_OWL.STATUS_BAR.setText(Translator.getTerm("OpenOWLOntologyAction"));
            } else if (conversionType.equals(FREEMIND_ONTOLOGY)) {
                loadFreeMindOntology(currentProject, chooser.getSelectedFile());
                DODDLE_OWL.STATUS_BAR.setText(Translator.getTerm("OpenFreeMindOntologyAction"));
            }
        }
    }
}
