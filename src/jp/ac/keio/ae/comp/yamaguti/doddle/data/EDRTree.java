package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/*
 /**
 * @author takeshi morita
 */
public class EDRTree {

    private boolean isSpecial;
    private static EDRTree edrTree;
    private static EDRTree edrtTree;
    private static Map<String, Set<TreeNode>> idNodeSetMap = new HashMap<String, Set<TreeNode>>();

    public static EDRTree getEDRTree() {
        if (edrTree == null) {
            edrTree = new EDRTree(false);
        }
        return edrTree;
    }

    public static EDRTree getEDRTTree() {
        if (edrtTree == null) {
            edrtTree = new EDRTree(true);
        }
        return edrtTree;
    }

    private EDRTree(boolean t) {
        isSpecial = t;
    }

    public void clear() {
        idNodeSetMap.clear();
    }

    public Set<List<String>> getURIPathToRootSet(String id) {
        Set<List<String>> pathToRootSet = new HashSet<List<String>>();
        String treeData = EDRDic.getTreeData(isSpecial, id);

        if (treeData == null) { // 上位・下位関係が定義されていない（できない）概念
            pathToRootSet.add(Arrays.asList(new String[] { getURI(id)}));
            return pathToRootSet;
        }

        String[] pathArray = treeData.split("\\|");
        for (int i = 1; i < pathArray.length; i++) {
            String[] idArray = pathArray[i].split("\t");
            List<String> path = new ArrayList<String>();
            for (int j = 0; j < idArray.length; j++) {
                String nid = idArray[j];
                path.add(getURI(nid));
            }
            pathToRootSet.add(path);
        }
        return pathToRootSet;
    }

    public Set<List<Concept>> getConceptPathToRootSet(String id) {
        Concept c = getConcept(id);
        Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
        String treeData = EDRDic.getTreeData(isSpecial, id);

        if (treeData == null) { // 上位・下位関係が定義されていない（できない）概念
            pathToRootSet.add(Arrays.asList(new Concept[] { c}));
            return pathToRootSet;
        }

        String[] pathArray = treeData.split("\\|");
        for (int i = 1; i < pathArray.length; i++) {
            String[] idArray = pathArray[i].split("\t");
            List<Concept> path = new ArrayList<Concept>();
            for (int j = 0; j < idArray.length; j++) {
                String nid = idArray[j];
                path.add(getConcept(nid));
            }
            pathToRootSet.add(path);
        }
        return pathToRootSet;
    }

    private void getSubURI(TreeNode node, Set<String> subIDSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode childNode = node.getChildAt(i);
            // subConceptSet.add(childNode.toString());
            String id = childNode.toString();
            subIDSet.add(getURI(id));
            getSubURI(childNode, subIDSet);
        }
    }

    private void getSubIDSet(TreeNode node, Set<String> subIDSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            subIDSet.add(childNode.toString());
        }
    }

    public void getSubIDSet(String id, Set<String> nounIDSet, Set<String> refineSubIDSet) {
        Set<String> subIDSet = new HashSet<String>();
        Set<TreeNode> nodeSet = idNodeSetMap.get(id);
        if (nodeSet == null) { return; }
        for (TreeNode node : nodeSet) {
            getSubIDSet(node, subIDSet);
        }
        if (subIDSet.size() == 0) { return; }
        for (String subID : subIDSet) {
            if (nounIDSet.contains(subID)) {
                refineSubIDSet.add(subID);
            }
        }
        if (0 < refineSubIDSet.size()) { return; }
        for (String subID : subIDSet) {
            getSubIDSet(subID, nounIDSet, refineSubIDSet);
        }
    }

    public Set<Set<String>> getSubURISet(String id) {
        Set<TreeNode> nodeSet = idNodeSetMap.get(id);
        Set<Set<String>> subURIsSet = new HashSet<Set<String>>();
        if (nodeSet == null) { return subURIsSet; }
        for (TreeNode node : nodeSet) {
            Set<String> subURISet = new HashSet<String>();
            getSubURI(node, subURISet);
            subURIsSet.add(subURISet);
        }
        return subURIsSet;
    }

    private String getURI(String id) {
        if (isSpecial) { return DODDLEConstants.EDRT_URI + id; }
        return DODDLEConstants.EDR_URI + id;
    }

    private Concept getConcept(String id) {
        if (isSpecial) { return EDRDic.getEDRTConcept(id); }
        return EDRDic.getEDRConcept(id);
    }

    public Set<Set<String>> getSiblingURISet(String id) {
        Set<TreeNode> nodeSet = idNodeSetMap.get(id);
        Set<Set<String>> siblingIDsSet = new HashSet<Set<String>>();
        if (nodeSet == null) { return siblingIDsSet; }
        for (TreeNode node : nodeSet) {
            Set<String> siblingIDSet = new HashSet<String>();
            // System.out.println("NODE: " + node);
            TreeNode parentNode = node.getParent();
            // System.out.println("PARENT_NODE: " + parentNode);
            if (parentNode != null) {
                for (int i = 0; i < parentNode.getChildCount(); i++) {
                    TreeNode siblingNode = parentNode.getChildAt(i);
                    if (siblingNode != node) {
                        String sid = siblingNode.toString();
                        siblingIDSet.add(getURI(sid));
                    }
                }
            }
            siblingIDsSet.add(siblingIDSet);
        }
        return siblingIDsSet;
    }

    public void makeEDRTree(Set<String> idSet) {
        Set<List<String>> pathSet = new HashSet<List<String>>();
        for (String id : idSet) {
            pathSet.addAll(getURIPathToRootSet(id));
        }
        String rootID = null;
        if (isSpecial) {
            rootID = ConceptTreeMaker.EDRT_CLASS_ROOT_ID;
        } else {
            rootID = ConceptTreeMaker.EDR_CLASS_ROOT_ID;
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootID);
        Set<TreeNode> nodeSet = new HashSet<TreeNode>();
        nodeSet.add(rootNode);
        idNodeSetMap.put(rootID, nodeSet);

        for (List<String> nodeList : pathSet) {
            // nodeList -> [x, x, x, ..., 入力概念]
            addTreeNode(rootNode, nodeList);
        }
    }

    /**
     * 概念木のノードと概念Xを含む木をあらわすリストを渡して, リストを概念木の中の適切な場所に追加する
     * 
     * nodeList -> a > b > c > X
     */
    private boolean addTreeNode(DefaultMutableTreeNode treeNode, List<String> nodeList) {
        if (nodeList.isEmpty()) { return false; }
        if (treeNode.isLeaf()) { return insertTreeNodeList(treeNode, nodeList); }
        for (Enumeration i = treeNode.children(); i.hasMoreElements();) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) i.nextElement();
            String firstNode = nodeList.get(0);
            if (firstNode != null && node.toString().equals(firstNode)) { return addTreeNode(node, nodeList.subList(1,
                    nodeList.size())); }
        }
        return insertTreeNodeList(treeNode, nodeList);
    }

    /**
     * IDXを含む木をあらわすリストを概念木に挿入する
     */
    private boolean insertTreeNodeList(DefaultMutableTreeNode treeNode, List nodeList) {
        if (nodeList.isEmpty()) { return false; }
        String firstNode = (String) nodeList.get(0);
        if (firstNode != null) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(firstNode);
            treeNode.add(childNode);
            if (idNodeSetMap.get(firstNode) == null) {
                Set<TreeNode> nodeSet = new HashSet<TreeNode>();
                nodeSet.add(childNode);
                idNodeSetMap.put(firstNode, nodeSet);
            } else {
                Set<TreeNode> nodeSet = idNodeSetMap.get(firstNode);
                nodeSet.add(childNode);
            }
            insertTreeNodeList(childNode, nodeList.subList(1, nodeList.size()));
        }
        return true;
    }

}
