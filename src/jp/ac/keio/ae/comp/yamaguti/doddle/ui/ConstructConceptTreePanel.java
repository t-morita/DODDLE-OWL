/*
 * @(#)  2006/04/06
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.infonode.docking.*;

/**
 * @author takeshi morita
 */
public abstract class ConstructConceptTreePanel extends JPanel implements ComplexConceptTreeInterface {
    protected ConceptTreePanel isaTreePanel;
    protected ConceptTreePanel hasaTreePanel;
    protected UndefinedWordListPanel undefinedWordListPanel;
    protected ConceptInformationPanel conceptInfoPanel;

    protected ConceptDriftManagementPanel conceptDriftManagementPanel;
    protected ConceptTreeMaker treeMaker = ConceptTreeMaker.getInstance();

    protected int trimmedConceptNum;
    protected int beforeTrimmingConceptNum;
    protected int addedSINNum;
    protected double addedAbstractComplexConceptCnt;
    protected double averageAbstracComplexConceptGroupSiblingConceptCnt;

    protected View[] mainViews;
    protected RootWindow rootWindow;

    protected DODDLEProject project;

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

    public void setUndefinedWordListModel(ListModel model) {
        undefinedWordListPanel.setUndefinedWordListModel(model);
        repaint();
    }

    public Map getIDTypicalWordMap() {
        return isaTreePanel.getConceptTypicalWordMap();
    }

    public void loadIDTypicalWord(Map idTypicalWordMap) {
        isaTreePanel.loadConceptTypicalWord(idTypicalWordMap);
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
        } else if (uri.equals(DODDLEConstants.BASE_URI + "DID1")) { return ConceptSelectionDialog.objectConcept; }
        TreeModel treeModel = getConceptTreeModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        if (rootNode.getURI().equals(uri)) { return rootNode.getConcept(); }
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
        Set<Concept> conceptSet = new HashSet<Concept>();
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

    public double getAddedAbstractComplexConceptCnt() {
        return addedAbstractComplexConceptCnt;
    }

    public double getAverageAbstracComplexConceptGroupSiblingConceptCnt() {
        return averageAbstracComplexConceptGroupSiblingConceptCnt;
    }

    public void init() {
        addedAbstractComplexConceptCnt = 0;
        averageAbstracComplexConceptGroupSiblingConceptCnt = 0;
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
        if (conceptTree.getModel().getRoot() instanceof ConceptTreeNode) { return (ConceptTreeNode) conceptTree
                .getModel().getRoot(); }
        return null;
    }   

    public void expandIsaTree() {
        JTree conceptTree = isaTreePanel.getConceptTree();
        for (int i = 0; i < conceptTree.getRowCount(); i++) {
            conceptTree.expandPath(conceptTree.getPathForRow(i));
        }
    }

    public Map<String, Concept> getComplexWordConceptMap() {
        return isaTreePanel.getComplexWordConceptMap();
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
        TreeSet<ConceptTreeNode> sortedChildNodeSet = new TreeSet<ConceptTreeNode>();
        for (int i = 0; i < node.getChildCount(); i++) {
            sortedChildNodeSet.add((ConceptTreeNode)node.getChildAt(i));
        }
        for (ConceptTreeNode childNode: sortedChildNodeSet) {
            ConceptTreeNode sortedChildNode = new ConceptTreeNode(childNode, project);
            sortedNode.add(sortedChildNode);
            makeSortedTreeModel(childNode, sortedChildNode);
        }
    }
    
    public DefaultTreeModel setConceptTreeModel(TreeModel model) {
        ConceptTreeNode rootNode = (ConceptTreeNode)model.getRoot();
        ConceptTreeNode sortedRootNode = new ConceptTreeNode(rootNode, project);
        DefaultTreeModel sortedTreeModel = new DefaultTreeModel(sortedRootNode);
        makeSortedTreeModel(rootNode, sortedRootNode);
        checkMultipleInheritance(sortedTreeModel);
        isaTreePanel.getConceptTree().setModel(sortedTreeModel);
        return sortedTreeModel;
    }

    public void checkMultipleInheritance(TreeModel model) {
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
        if (conceptTree.getModel().getRoot() instanceof ConceptTreeNode) { return (ConceptTreeNode) conceptTree
                .getModel().getRoot(); }
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
