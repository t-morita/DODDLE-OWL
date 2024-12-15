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

package io.github.doddle_owl.views.concept_tree;

import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.concept_tree.CompoundConceptTreeInterface;
import io.github.doddle_owl.models.concept_tree.ConceptTreeNode;
import io.github.doddle_owl.utils.ConceptTreeMaker;
import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.views.common.ConceptSelectionDialog;
import io.github.doddle_owl.views.common.UndefinedTermListPanel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Takeshi Morita
 */
public abstract class ConceptTreeConstructionPanel extends JPanel implements CompoundConceptTreeInterface {
    ConceptTreePanel isaTreePanel;
    ConceptTreePanel hasaTreePanel;
    UndefinedTermListPanel undefinedTermListPanel;
    ConceptInformationPanel conceptInfoPanel;

    ConceptDriftManagementPanel conceptDriftManagementPanel;
    final ConceptTreeMaker treeMaker = ConceptTreeMaker.getInstance();

    int trimmedConceptNum;
    int beforeTrimmingConceptNum;
    int addedSINNum;
    double addedAbstractCompoundConceptCnt;
    double averageAbstracCompoundConceptGroupSiblingConceptCnt;

    DODDLEProjectPanel project;

    public void initUndo() {
        isaTreePanel.initUndo();
        hasaTreePanel.initUndo();
    }

    public void clearPanel() {
        conceptInfoPanel.clearPanel();
    }

    public Concept getSelectedConcept() {
        return conceptInfoPanel.getSelectedConcept();
    }


    public ConceptDriftManagementPanel getConceptDriftManagementPanel() {
        return conceptDriftManagementPanel;
    }

    public void setUndefinedTermListModel(ListModel model) {
        undefinedTermListPanel.setUndefinedTermListModel(model);
        repaint();
    }

    public Map getIDPreferentialTermMap() {
        return isaTreePanel.getConceptPreferentialTermMap();
    }

    public void loadIDPreferentialTerm(Map idPreferentialTermMap) {
        isaTreePanel.loadConceptPreferentialTerm(idPreferentialTermMap);
    }

    public TreeModel getConceptTreeModel() {
        return isaTreePanel.getConceptTree().getModel();
    }

    public void loadDescriptions(Map<String, DODDLELiteral> wordDescriptionMap) {
        isaTreePanel.loadDescriptions(wordDescriptionMap);
    }

    public Concept getConcept(String uri) {
        if (uri.equals(DODDLEConstants.BASE_URI + "DID0")) {
            return ConceptSelectionDialog.agentConcept;
        } else if (uri.equals(DODDLEConstants.BASE_URI + "DID1")) {
            return ConceptSelectionDialog.objectConcept;
        }
        TreeModel treeModel = getConceptTreeModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        if (rootNode.getURI().equals(uri)) {
            return rootNode.getConcept();
        }
        Concept concept = null;
        Set<Concept> allConcept = getConceptSet();
        for (Concept c : allConcept) {
            if (c.getURI().equals(uri)) {
                concept = c;
                return concept;
            }
        }
        return concept;
    }

    public Set<Concept> getConceptSet() {
        TreeModel treeModel = getConceptTreeModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        Set<Concept> conceptSet = new HashSet<>();
        getConceptSet(rootNode, conceptSet);
        return conceptSet;
    }

    private void getConceptSet(ConceptTreeNode node, Set<Concept> conceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            conceptSet.add(childNode.getConcept());
            getConceptSet(childNode, conceptSet);
        }
    }

    public JTree getIsaTree() {
        return isaTreePanel.getConceptTree();
    }

    public TreeModel getDefaultConceptTreeModel(Set pathSet, String type) {
        return treeMaker.getDefaultConceptTreeModel(pathSet, project, type);
    }

    public void addJPWord(String identity, String word) {
        isaTreePanel.addJPWord(identity, word);
    }

    public void addSubConcept(String identity, String word) {
        isaTreePanel.addSubConcept(identity, word);
    }

    public double getAddedAbstractCompoundConceptCnt() {
        return addedAbstractCompoundConceptCnt;
    }

    public double getAverageAbstracCompoundConceptGroupSiblingConceptCnt() {
        return averageAbstracCompoundConceptGroupSiblingConceptCnt;
    }

    void initialize() {
        addedAbstractCompoundConceptCnt = 0;
        averageAbstracCompoundConceptGroupSiblingConceptCnt = 0;
        ConceptTreeMaker.getInstance().init();
        isaTreePanel.getConceptTree().setModel(new DefaultTreeModel(null));
    }

    public int getBeforeTrimmingConceptNum() {
        return beforeTrimmingConceptNum;
    }

    public int getAddedSINNum() {
        return addedSINNum;
    }

    public int getTrimmedConceptNum() {
        return trimmedConceptNum;
    }

    public int getAfterTrimmingConceptNum() {
        return beforeTrimmingConceptNum - trimmedConceptNum;
    }

    public int getAllConceptCnt() {
        // System.out.println(Utils.getAllConcept(conceptTreePanel.getConceptTree().getModel()));
        return Utils.getAllConcept(isaTreePanel.getConceptTree().getModel()).size();
    }

    public double getChildCntAverage() {
        return Utils.getChildCntAverage(isaTreePanel.getConceptTree().getModel());
    }

    public Set<String> getAllConceptURI() {
        return isaTreePanel.getAllConceptURI();
    }

    public ConceptTreeNode getIsaTreeModelRoot() {
        JTree conceptTree = isaTreePanel.getConceptTree();
        if (conceptTree.getModel().getRoot() instanceof ConceptTreeNode) {
            return (ConceptTreeNode) conceptTree.getModel().getRoot();
        }
        return null;
    }

    public void expandIsaTree() {
        JTree conceptTree = isaTreePanel.getConceptTree();
        for (int i = 0; i < conceptTree.getRowCount(); i++) {
            conceptTree.expandPath(conceptTree.getPathForRow(i));
        }
    }

    public Map<String, Concept> getCompoundWordConceptMap() {
        return isaTreePanel.getCompoundWordConceptMap();
    }

    public Set getSupConceptSet(String id) {
        return isaTreePanel.getSupConceptSet(id);
    }

    public void setVisibleIsaTree(boolean isVisible) {
        isaTreePanel.getConceptTree().setVisible(isVisible);
    }

    public void setConceptDriftManagementResult() {
        conceptDriftManagementPanel.setConceptDriftManagementResult();
    }

    private void makeSortedTreeModel(ConceptTreeNode node, ConceptTreeNode sortedNode) {
        TreeSet<ConceptTreeNode> sortedChildNodeSet = new TreeSet<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            sortedChildNodeSet.add((ConceptTreeNode) node.getChildAt(i));
        }
        for (ConceptTreeNode childNode : sortedChildNodeSet) {
            ConceptTreeNode sortedChildNode = new ConceptTreeNode(childNode, project);
            sortedNode.add(sortedChildNode);
            makeSortedTreeModel(childNode, sortedChildNode);
        }
    }

    public DefaultTreeModel setConceptTreeModel(TreeModel model) {
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        ConceptTreeNode sortedRootNode = new ConceptTreeNode(rootNode, project);
        DefaultTreeModel sortedTreeModel = new DefaultTreeModel(sortedRootNode);
        makeSortedTreeModel(rootNode, sortedRootNode);
        checkMultipleInheritance(sortedTreeModel);
        isaTreePanel.getConceptTree().setModel(sortedTreeModel);
        treeMaker.conceptDriftManagement(sortedTreeModel);
        setConceptDriftManagementResult();
        return sortedTreeModel;
    }

    private void checkMultipleInheritance(TreeModel model) {
        isaTreePanel.checkAllMultipleInheritanceNode(model);
    }

    public void searchSameConceptTreeNode(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        isaTreePanel.searchSameConceptTreeNode(concept, node, sameConceptSet);
    }

    public void deleteLinkToUpperConcept(ConceptTreeNode targetDeleteNode) {
        isaTreePanel.deleteLinkToUpperConcept(targetDeleteNode);
    }

    public void setHasaTreeModel(TreeModel model) {
        hasaTreePanel.getConceptTree().setModel(model);
    }

    public void setVisibleHasaTree(boolean isVisible) {
        hasaTreePanel.getConceptTree().setVisible(isVisible);
    }

    public void expandHasaTree() {
        JTree conceptTree = hasaTreePanel.getConceptTree();
        for (int i = 0; i < conceptTree.getRowCount(); i++) {
            conceptTree.expandPath(conceptTree.getPathForRow(i));
        }
    }

    public ConceptTreeNode getHasaTreeModelRoot() {
        JTree conceptTree = hasaTreePanel.getConceptTree();
        if (conceptTree.getModel().getRoot() instanceof ConceptTreeNode) {
            return (ConceptTreeNode) conceptTree
                    .getModel().getRoot();
        }
        return null;
    }

    public JTree getHasaTree() {
        return hasaTreePanel.getConceptTree();
    }

    public void selectIsaTreeNode(Concept targetConcept, Concept parentConcept) {
        ConceptTreeNode rootNode = (ConceptTreeNode) isaTreePanel.getConceptTree().getModel().getRoot();
        isaTreePanel.selectConceptTreeNode(rootNode, targetConcept, parentConcept);
    }

    public void selectHasaTreeNode(Concept targetConcept, Concept parentConcept) {
        ConceptTreeNode rootNode = (ConceptTreeNode) hasaTreePanel.getConceptTree().getModel().getRoot();
        hasaTreePanel.selectConceptTreeNode(rootNode, targetConcept, parentConcept);
    }
}
