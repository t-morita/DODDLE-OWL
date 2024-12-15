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

package io.github.doddle_owl.utils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.concept_tree.ConceptTreeNode;
import io.github.doddle_owl.models.concept_tree.VerbConcept;
import io.github.doddle_owl.models.ontology_api.EDRTree;
import io.github.doddle_owl.models.ontology_api.JaWordNetTree;
import io.github.doddle_owl.models.ontology_api.WordNet;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.views.concept_tree.ConceptTreePanel;
import io.github.doddle_owl.views.concept_selection.ConceptSelectionPanel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class ConceptTreeMaker {

    private final List<List<ConceptTreeNode>> mraResultList;
    private final SinNodeListSorter sinNodeListSorter;
    private final List<ConceptTreeNode> traResultList;
    private final Set<Concept> multipleInheritanceConceptSet;
    private Set<Concept> inputConceptSet;
    private static final Set<Concept> trimmedConceptSet = new HashSet<>();

    private static final ConceptTreeMaker maker = new ConceptTreeMaker();
    public static final String EDR_CLASS_ROOT_ID = "ID3aa966";
    public static final String EDRT_CLASS_ROOT_ID = "ID2f3526";
    public static final String JPNWN_CLASS_ROOT_ID = "JPNWN_ROOT";
    public static final String DODDLE_CLASS_ROOT_URI = DODDLEConstants.DODDLE_URI + "CLASS_ROOT";
    public static final String DODDLE_CLASS_HASA_ROOT_URI = DODDLEConstants.DODDLE_URI + "CLASS_HASA_ROOT";
    public static final String DODDLE_PROPERTY_ROOT_URI = DODDLEConstants.DODDLE_URI + "PROP_ROOT";
    public static final String DODDLE_PROPERTY_HASA_ROOT_URI = DODDLEConstants.DODDLE_URI + "PROPERTY_HASA_ROOT";

    private ConceptTreeMaker() {
        inputConceptSet = new HashSet<>();
        mraResultList = new ArrayList<>();
        sinNodeListSorter = new SinNodeListSorter();
        traResultList = new ArrayList<>();
        multipleInheritanceConceptSet = new HashSet<>();
    }

    public static boolean isDODDLEClassRootURI(String uri) {
        return uri.equals(DODDLE_CLASS_ROOT_URI);
    }

    public static boolean isDODDLEPropertyRootURI(String uri) {
        return uri.equals(DODDLE_PROPERTY_ROOT_URI);
    }

    public void setInputConceptSet(Set<Concept> cSet) {
        inputConceptSet = cSet;
    }

    public void init() {
        inputConceptSet = new HashSet<>();
        mraResultList.clear();
        traResultList.clear();
        beforeTrimmingConceptNum = 0;
        multipleInheritanceConceptSet.clear();
    }

    public Set<List<Concept>> getPathListSet(Set<Concept> conceptSet) {
        inputConceptSet = conceptSet;
        Set<List<Concept>> pathSet = new HashSet<>();
        for (Concept c : conceptSet) {
            Set<List<Concept>> tmpPathSet = OWLOntologyManager.getPathToRootSet(c.getURI());
            int pathSize = 0;
            for (List<Concept> pathToRoot : tmpPathSet) {
                if (pathSize < pathToRoot.size()) {
                    pathSize = pathToRoot.size();
                }
            }
            if (pathSize <= 1 && DODDLE_OWL.GENERAL_ONTOLOGY_NAMESPACE_SET.contains(c.getNameSpace())) {
                switch (c.getNameSpace()) {
                    case DODDLEConstants.EDR_URI:
                        pathSet.addAll(EDRTree.getEDRTree().getConceptPathToRootSet(c.getLocalName()));
                        break;
                    case DODDLEConstants.EDRT_URI:
                        pathSet.addAll(EDRTree.getEDRTTree().getConceptPathToRootSet(c.getLocalName()));
                        break;
                    case DODDLEConstants.WN_URI:
                        pathSet.addAll(WordNet.getPathToRootSet(Long.valueOf(c.getLocalName())));
                        break;
                    case DODDLEConstants.JPN_WN_URI:
                        pathSet.addAll(JaWordNetTree.getJPNWNTree().getConceptPathToRootSet(c.getLocalName()));
                        break;
                }
            } else {
                pathSet.addAll(tmpPathSet);
            }
        }
        return pathSet;
    }

    public static ConceptTreeMaker getInstance() {
        return maker;
    }

    /**
     * 概念名(String)のリストを渡して,TreeModelを返す
     *
     * @param pathSet 概念(String)のリスト
     * @return TreeModel
     */
    public TreeModel getDefaultConceptTreeModel(Set<List<Concept>> pathSet, DODDLEProjectPanel project, String type) {
        Concept rootConcept = null;
        switch (type) {
            case ConceptTreeMaker.DODDLE_CLASS_ROOT_URI:
                rootConcept = new VerbConcept(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI, "");
                rootConcept.addLabel(new DODDLELiteral("ja", "名詞的概念 (Is-a)"));
                rootConcept.addLabel(new DODDLELiteral("en", "Is-a Root Class"));
                break;
            case ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI:
                rootConcept = new VerbConcept(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI, "");
                rootConcept.addLabel(new DODDLELiteral("ja", "動詞的概念 (Is-a)"));
                rootConcept.addLabel(new DODDLELiteral("en", "Is-a Root Property"));
                break;
            case ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI:
                rootConcept = new VerbConcept(ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI, "");
                rootConcept.addLabel(new DODDLELiteral("ja", "名詞的概念(Has-a)"));
                rootConcept.addLabel(new DODDLELiteral("en", "Has-a Root Class"));
                break;
            case ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI:
                rootConcept = new VerbConcept(ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI, "");
                rootConcept.addLabel(new DODDLELiteral("ja", "動詞的概念(Has-a)"));
                rootConcept.addLabel(new DODDLELiteral("en", "Has-a Root Property"));
                break;
        }
        ConceptTreeNode rootNode = new ConceptTreeNode(rootConcept, project);
        rootNode.setIsUserConcept(true);
        for (List<Concept> nodeList : pathSet) {
            // nodeList -> [x, x, x, ..., 入力概念]
            addTreeNode(rootNode, nodeList, project);
        }
        return new DefaultTreeModel(rootNode);
    }

    public int getTrimmedConceptNum() {
        return trimmedConceptSet.size();
    }

    private int beforeTrimmingConceptNum;

    public int getBeforeTrimmingConceptNum() {
        return beforeTrimmingConceptNum;
    }

    /**
     * 概念名(String)のリストを渡して,TreeModelを返す 概念変動を行っている。
     *
     * @param pathSet 概念(String)のリスト
     * @return TreeModel
     */
    public TreeModel getTrimmedTreeModel(Set<List<Concept>> pathSet, DODDLEProjectPanel project, String type) {
        TreeModel treeModel = getDefaultConceptTreeModel(pathSet, project, type);
        beforeTrimmingConceptNum = Utils.getAllConcept(treeModel).size();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        trimmedConceptSet.clear();
        if (project.getConceptSelectionPanel().getPerfectlyMatchedOptionPanel().isTrimming()) {
            trimTree(rootNode);
            removeSameNode(rootNode);
        }
        return treeModel;
    }

    /**
     * 不必要なノードかどうか
     */
    private boolean isUnnecessaryNode(ConceptTreeNode node) {
        return (node.getChildCount() == 1 && !isInputConcept(node.getConcept()));
    }

    /**
     * 不要な中間概念を剪定
     */
    private void trimTree(ConceptTreeNode treeNode) {
        for (TreeNode node : Collections.list(treeNode.children())) {
            ConceptTreeNode childNode = (ConceptTreeNode) node;
            if (isUnnecessaryNode(childNode)) {
                // 子供が一つしかない場合にしか，トリミングは行われないため，getChildAt(0)でよい
                ConceptTreeNode grandChildNode = (ConceptTreeNode) childNode.getChildAt(0);
                trimmedConceptSet.add(childNode.getConcept()); // 削除された概念を保存
                // 剪定される概念が保持している剪定概念を保存
                grandChildNode.addAllTrimmedConcept(childNode.getTrimmedConceptList().get(0));
                grandChildNode.addTrimmedConcept(childNode.getConcept()); // 削除された概念を保存
                treeNode.add(grandChildNode);
                treeNode.remove(childNode);
                trimTree(treeNode);
            } else {
                trimTree(childNode);
            }
        }
    }

    /**
     * Trimmingしたことにより，同じレベルで同一の概念が含まれる場合があるため， それらを除去する．
     *
     * @param treeNode
     */
    private void removeSameNode(ConceptTreeNode treeNode) {
        Map<Concept, List<ConceptTreeNode>> sameNodeMap = new HashMap<>();
        for (Enumeration i = treeNode.children(); i.hasMoreElements(); ) {
            ConceptTreeNode childNode = (ConceptTreeNode) i.nextElement();
            List<ConceptTreeNode> sameNodeList = sameNodeMap.get(childNode.getConcept());
            if (sameNodeList == null) {
                sameNodeList = new ArrayList<>();
            }
            sameNodeList.add(childNode);
            sameNodeMap.put(childNode.getConcept(), sameNodeList);
            if (0 < childNode.getChildCount()) {
                removeSameNode(childNode);
            }
        }
        for (List<ConceptTreeNode> sameNodeList : sameNodeMap.values()) {
            if (1 < sameNodeList.size()) {
                ConceptTreeNode baseNode = sameNodeList.get(0);
                for (int j = 1; j < sameNodeList.size(); j++) {
                    ConceptTreeNode removeNode = sameNodeList.get(j);
                    baseNode.addTrimmedConceptList(removeNode.getTrimmedConceptList().get(0));
                }
                // System.out.println("samenodesize: " + sameNodeList.size());
                for (int j = 1; j < sameNodeList.size(); j++) {
                    // System.out.println("j: " + j);
                    treeNode.remove(sameNodeList.get(j));
                }
            }
        }
    }

    /**
     * 概念木のノードと概念Xを含む木をあらわすリストを渡して, リストを概念木の中の適切な場所に追加する
     * <p>
     * nodeList -> a > b > c > X
     */
    private boolean addTreeNode(ConceptTreeNode treeNode, List<Concept> nodeList, DODDLEProjectPanel project) {
        if (nodeList.isEmpty()) {
            return false;
        }
        if (treeNode.isLeaf()) {
            return insertTreeNodeList(treeNode, nodeList, project);
        }
        for (Enumeration i = treeNode.children(); i.hasMoreElements(); ) {
            ConceptTreeNode node = (ConceptTreeNode) i.nextElement();
            Concept firstNode = nodeList.get(0);
            if (firstNode != null && node.getConcept().getURI().equals(firstNode.getURI())) {
                return addTreeNode(node,
                        nodeList.subList(1, nodeList.size()), project);
            }
        }
        return insertTreeNodeList(treeNode, nodeList, project);
    }

    /**
     * 概念Xを含む木をあらわすリストを概念木に挿入する
     */
    private boolean insertTreeNodeList(DefaultMutableTreeNode treeNode, List nodeList, DODDLEProjectPanel project) {
        if (nodeList.isEmpty()) {
            return false;
        }
        Concept firstNode = (Concept) nodeList.get(0);
        if (firstNode != null) {
            boolean isInputConcept = !isSystemAddedConcept(firstNode) && isInputConcept(firstNode);
            VerbConcept c = new VerbConcept(firstNode);
            project.getConceptSelectionPanel().removeRefOntConceptLabel(c, isInputConcept);
            ConceptTreeNode childNode = new ConceptTreeNode(c, project);
            childNode.setIsInputConcept(isInputConcept);
            treeNode.add(childNode);
            insertTreeNodeList(childNode, nodeList.subList(1, nodeList.size()), project);
        }
        return true;
    }

    /**
     * 概念変動を行うために必要な情報を分析し，セットする
     */
    public void conceptDriftManagement(TreeModel model) {
        mraResultList.clear();
        traResultList.clear();
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        matchedResultAnalysis(rootNode);
        trimmedResultAnalysis(rootNode, 3);
        multipleInheritanceConceptSet.clear();
        getMultipleInheritanceConceptSet(rootNode);
    }

    public void resetTRA() {
        traResultList.clear();
    }

    public void resetMRA() {
        mraResultList.clear();
    }

    /**
     * TRA(Trimmed Result Analysis)を行う．
     */
    public void trimmedResultAnalysis(ConceptTreeNode node, int trimmedCnt) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            List<Integer> trimmedCount = childNode.getTrimmedCountList();
            for (int cnt : trimmedCount) {
                if (trimmedCnt <= cnt) {
                    traResultList.add(childNode);
                    break;
                }
            }
            trimmedResultAnalysis(childNode, trimmedCnt);
        }
    }

    private boolean isSameSinNode(ConceptTreeNode sinNode, ConceptTreeNode lastSinNode) {
        return lastSinNode != null && lastSinNode.toString().equals(sinNode.toString());
    }

    /**
     * MRA(Matched Result Analysis)を行う．
     */
    public void matchedResultAnalysis(ConceptTreeNode rootNode) {
        List<ConceptTreeNode> sinNodeList = new ArrayList<>();
        getSINNodeSet(rootNode, sinNodeList);
        sinNodeList.sort(sinNodeListSorter);
        ConceptTreeNode lastSinNode = null;
        for (ConceptTreeNode sinNode : sinNodeList) {
            List<ConceptTreeNode> stm = new ArrayList<>();
            stm.add(sinNode);
            getSubTreesManuallyMoved(sinNode, stm);
            if (isSameSinNode(sinNode, lastSinNode)) {
                mraResultList.get(mraResultList.size() - 1).addAll(stm);
            } else {
                mraResultList.add(stm);
            }
            lastSinNode = sinNode;
        }
    }

    /**
     * SINノードをソート
     *
     * @author Takeshi Morita
     */
    class SinNodeListSorter implements Comparator {
        public int compare(Object o1, Object o2) {
            ConceptTreeNode n1 = (ConceptTreeNode) o1;
            ConceptTreeNode n2 = (ConceptTreeNode) o2;
            return n1.toString().compareTo(n2.toString());
        }
    }

    /**
     * STM(Sub Trees manually Moved)を得る． MRA(Matched Result Analysis)で用いる．
     *
     * @param node
     * @param stm
     */
    private void getSubTreesManuallyMoved(ConceptTreeNode node, List<ConceptTreeNode> stm) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.isInputConcept()) {
                stm.add(childNode);
                getSubTreesManuallyMoved(childNode, stm);
            }
        }
    }

    /**
     * SIN(a Salient Internal Nodes)の集合を得る．
     *
     * @param node
     * @param sinNodeSet
     */
    private void getSINNodeSet(ConceptTreeNode node, List<ConceptTreeNode> sinNodeSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.isSINNode()) {
                sinNodeSet.add(childNode);
            }
            getSINNodeSet(childNode, sinNodeSet);
        }
    }

    public Set<Concept> getMulipleInheritanceConceptSet() {
        return multipleInheritanceConceptSet;
    }

    public Set<Concept> getMulipleInheritanceConceptSet(ConceptTreeNode node) {
        multipleInheritanceConceptSet.clear();
        getMultipleInheritanceConceptSet(node);
        return multipleInheritanceConceptSet;
    }

    /**
     * 多重継承概念の集合を得る．
     *
     * @param node
     */
    private void getMultipleInheritanceConceptSet(ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.isMultipleInheritance()) {
                multipleInheritanceConceptSet.add(childNode.getConcept());
            }
            getMultipleInheritanceConceptSet(childNode);
        }
    }

    public List<List<ConceptTreeNode>> getMRAresult() {
        return mraResultList;
    }

    public List<ConceptTreeNode> getTRAresult() {
        return traResultList;
    }

    private boolean isSystemAddedConcept(Concept c) {
        ConceptSelectionPanel conceptSelectionPanel = DODDLE_OWL.getCurrentProject()
                .getConceptSelectionPanel();
        Set<Concept> systemAddedInputConceptSet = conceptSelectionPanel.getSystemAddedInputConceptSet();
        if (systemAddedInputConceptSet == null) {
            return false;
        }
        for (Concept saic : systemAddedInputConceptSet) {
            if (saic != null && saic.getURI().equals(c.getURI())) {
                return true;
            }
        }
        return false;
    }

    private boolean isInputConcept(Concept c) {
        if (inputConceptSet == null) {
            return false;
        }
        for (Concept ic : inputConceptSet) {
            if (ic == null) {
                return false;
            }
            if (ic.getURI().equals(c.getURI())) {
                return true;
            }
        }
        return false;
    }

    private static Map<Resource, Set<Resource>> supSubSetMap;

    public TreeNode getConceptTreeRoot(DODDLEProjectPanel currentProject, Model model, Resource rootResource, String type) {
        Concept rootConcept = null;
        Property property = null;
        if (type.equals(ConceptTreePanel.CLASS_ISA_TREE)) {
            property = RDFS.subClassOf;
            rootConcept = new VerbConcept(DODDLE_CLASS_ROOT_URI, "");
            rootConcept.addLabel(new DODDLELiteral("ja", "名詞的概念 (Is-a)"));
            rootConcept.addLabel(new DODDLELiteral("en", "Is-a Root Class"));
        } else if (type.equals(ConceptTreePanel.CLASS_HASA_TREE)) {
            property = DODDLE_OWL.HASA_PROPERTY;
            rootConcept = new VerbConcept(DODDLE_CLASS_HASA_ROOT_URI, "");
            rootConcept.addLabel(new DODDLELiteral("ja", "名詞的概念 (has-a)"));
            rootConcept.addLabel(new DODDLELiteral("en", "Has-a Root Class"));
        }
        supSubSetMap = new HashMap<>();
        for (Resource resource : model.listSubjectsWithProperty(RDF.type).toList()) {
            for (Statement stmt : resource.listProperties(property).toList()) {
                Resource supResource = (Resource) stmt.getObject();
                Set<Resource> subResourceSet = supSubSetMap.get(supResource);
                if (subResourceSet == null) {
                    subResourceSet = new HashSet<>();
                }
                subResourceSet.add(resource);
                subResourceSet.remove(supResource);
                supSubSetMap.put(supResource, subResourceSet);
            }
        }

        ConceptTreeNode rootNode = new ConceptTreeNode(rootConcept, currentProject);
        rootNode.setIsUserConcept(true);
        if (supSubSetMap.get(rootResource) != null) {
            makeTree(currentProject, rootResource, rootNode);
        } else { // 前のバージョンとの互換性を保持するため
            rootResource = ResourceFactory.createResource(DODDLEConstants.EDR_URI + EDR_CLASS_ROOT_ID);
            if (supSubSetMap.get(rootResource) != null) {
                makeTree(currentProject, rootResource, rootNode);
            }
            rootResource = ResourceFactory.createResource(DODDLEConstants.OLD_EDR_URI + EDR_CLASS_ROOT_ID);
            if (supSubSetMap.get(rootResource) != null) {
                makeTree(currentProject, rootResource, rootNode);
            }
        }
        return rootNode;
    }

    public TreeNode getPropertyTreeRoot(DODDLEProjectPanel currentProject, Model model, Resource rootResource, String type) {
        Concept rootProperty = null;
        Property property = null;
        if (type.equals(ConceptTreePanel.PROPERTY_ISA_TREE)) {
            property = RDFS.subPropertyOf;
            rootProperty = new VerbConcept(DODDLE_PROPERTY_ROOT_URI, "");
            rootProperty.addLabel(new DODDLELiteral("ja", "動詞的概念"));
            rootProperty.addLabel(new DODDLELiteral("en", "Root Property"));
        } else if (type.equals(ConceptTreePanel.PROPERTY_HASA_TREE)) {
            property = DODDLE_OWL.HASA_PROPERTY;
            rootProperty = new VerbConcept(DODDLE_PROPERTY_HASA_ROOT_URI, "");
            rootProperty.addLabel(new DODDLELiteral("ja", "動詞的概念 (has-a)"));
            rootProperty.addLabel(new DODDLELiteral("en", "Has-a Root Property"));
        }

        supSubSetMap = new HashMap<>();
        for (Resource resource : model.listSubjectsWithProperty(RDF.type).toList()) {
            for (Statement stmt : resource.listProperties(property).toList()) {
                Resource supResource = (Resource) stmt.getObject();
                Set<Resource> subResourceSet = supSubSetMap.get(supResource);
                if (subResourceSet == null) {
                    subResourceSet = new HashSet<>();
                }
                subResourceSet.add(resource);
                supSubSetMap.put(supResource, subResourceSet);
            }
        }
        // System.out.println(supSubSetMap);
        ConceptTreeNode rootNode = new ConceptTreeNode(rootProperty, currentProject);
        rootNode.setIsUserConcept(true);
        if (supSubSetMap.get(rootResource) != null) {
            makeTree(currentProject, rootResource, rootNode);
        } else { // 前のバージョンとの互換性を保持するため
            rootResource = ResourceFactory
                    .createResource(DODDLEConstants.OLD_EDR_URI + "ID" + DODDLE_PROPERTY_ROOT_URI);
            if (supSubSetMap.get(rootResource) != null) {
                makeTree(currentProject, rootResource, rootNode);
            }
        }
        return rootNode;
    }

    private void makeTree(DODDLEProjectPanel currentProject, Resource resource, DefaultMutableTreeNode node) {
        Set<Resource> subRDFSSet = supSubSetMap.get(resource);
        for (Resource subRDFS : subRDFSSet) {
            VerbConcept concept = new VerbConcept(subRDFS.getURI(), "");
            Statement prefLabelStmt = subRDFS.getProperty(JenaModelMaker.SKOS_PREFLABEL);
            if (prefLabelStmt != null) { // prefLabelが設定されていない場合
                Literal prefLabel = (Literal) prefLabelStmt.getObject();
                concept.setInputLabel(new DODDLELiteral(prefLabel.getLanguage(), prefLabel.getString()));
            }
            for (Statement stmt : subRDFS.listProperties(RDFS.label).toList()) {
                Literal lit = (Literal) stmt.getObject();
                concept.addLabel(new DODDLELiteral(lit.getLanguage(), lit.getString()));
            }
            for (Statement stmt : subRDFS.listProperties(RDFS.comment).toList()) {
                Literal lit = (Literal) stmt.getObject();
                concept.addDescription(new DODDLELiteral(lit.getLanguage(), lit.getString()));
            }

            Set<String> domainSet = new HashSet<>();
            for (Statement stmt : subRDFS.listProperties(RDFS.domain).toList()) {
                Resource domain = (Resource) stmt.getObject();
                domainSet.add(domain.getURI());
            }
            concept.addAllDomain(domainSet);

            Set<String> rangeSet = new HashSet<>();
            for (Statement stmt : subRDFS.listProperties(RDFS.range).toList()) {
                Resource range = (Resource) stmt.getObject();
                rangeSet.add(range.getURI());
            }
            concept.addAllRange(rangeSet);

            boolean isInputConcept = concept != null && isInputConcept(concept);
            ConceptTreeNode subNode = new ConceptTreeNode(concept, currentProject);
            subNode.setIsInputConcept(isInputConcept);
            if (subRDFS.getNameSpace().equals(DODDLEConstants.BASE_URI) && subRDFS.getLocalName().contains("UID")) {
                currentProject.setUserIDCount(Integer.parseInt(subRDFS.getLocalName().split("UID")[1]));
                subNode.setIsUserConcept(true);
            }
            if (supSubSetMap.get(subRDFS) != null) {
                makeTree(currentProject, subRDFS, subNode);
            }
            node.add(subNode);
        }
    }
}