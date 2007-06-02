package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.hp.hpl.jena.rdf.model.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/*
 * @(#)  2005/07/17
 */

/**
 * @author takeshi morita
 */
public class ConceptInformationPanel extends JPanel implements ActionListener {

    private Concept selectedConcept;

    private JLabel uriLabel;
    private JComboBox prefixComboBox;
    private JTextField localNameField;
    private JButton setURIButton;

    private LabelPanel labelPanel;
    private DescriptionPanel descriptionPanel;

    private JLabel trimmedNodeCntLabel;
    private JLabel trimmedNodeCntValueLabel;

    private JTree conceptTree;
    private JTree hasaTree;
    private ConceptDriftManagementPanel conceptDriftManagementPanel;

    private EDRConceptDefinitionPanel edrConceptDefinitionPanel;

    private void init(JTree tree, DefaultTreeCellRenderer renderer) {
        conceptTree = tree;
        uriLabel = new JLabel(Translator.getTerm("URILabel") + ": ");
        prefixComboBox = new JComboBox();
        // prefixComboBox.setBorder(BorderFactory.createTitledBorder("Prefix"));
        localNameField = new JTextField(25);
        setURIButton = new JButton(Translator.getTerm("SetURIButton"));
        setURIButton.addActionListener(this);

        JPanel uriPanel = new JPanel();
        uriPanel.setLayout(new GridLayout(1, 2));
        uriPanel.add(prefixComboBox);
        uriPanel.add(localNameField);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(uriLabel, BorderLayout.WEST);
        northPanel.add(uriPanel, BorderLayout.CENTER);
        northPanel.add(setURIButton, BorderLayout.EAST);

        labelPanel = new LabelPanel(LiteralPanel.LABEL, this);
        descriptionPanel = new DescriptionPanel(LiteralPanel.DESCRIPTION);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2));
        centerPanel.add(labelPanel);
        centerPanel.add(descriptionPanel);

        trimmedNodeCntLabel = new JLabel(Translator.getTerm("TrimmedConceptCountLabel") + "： ");
        trimmedNodeCntValueLabel = new JLabel("");
        JPanel trimmedNodeCntPanel = new JPanel();
        trimmedNodeCntPanel.add(trimmedNodeCntLabel);
        trimmedNodeCntPanel.add(trimmedNodeCntValueLabel);

        setLayout(new BorderLayout());
        add(getWestPanel(northPanel), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(getWestPanel(trimmedNodeCntPanel), BorderLayout.SOUTH);
        setTreeConfig(conceptTree, renderer);
    }

    public void clearPanel() {
        labelPanel.clearData();
        labelPanel.clearLabelField();
        labelPanel.clearTypicalWordValue();
        descriptionPanel.clearData();
        prefixComboBox.setModel(new DefaultComboBoxModel());
        localNameField.setText("");
        trimmedNodeCntValueLabel.setText("");
    }

    public ConceptInformationPanel(JTree isaTree, JTree hasaTree, DefaultTreeCellRenderer renderer,
            ConceptDriftManagementPanel cdmp) {
        this.hasaTree = hasaTree;
        setTreeConfig(this.hasaTree, renderer);
        init(isaTree, renderer);
        conceptDriftManagementPanel = cdmp;
    }

    public ConceptInformationPanel(JTree isaTree, JTree hasaTree, DefaultTreeCellRenderer renderer,
            EDRConceptDefinitionPanel ecdp, ConceptDriftManagementPanel cdmp) {
        edrConceptDefinitionPanel = ecdp;
        this.hasaTree = hasaTree;
        setTreeConfig(this.hasaTree, renderer);
        init(isaTree, renderer);
        conceptDriftManagementPanel = cdmp;
    }

    private void setTreeConfig(JTree tree, DefaultTreeCellRenderer renderer) {
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            private void setURI(String uri) {
                NameSpaceTable nsTable = DODDLE.getCurrentProject().getOntologySelectionPanel().getNSTable();
                Object[] prefixSet = nsTable.getPrefixSet().toArray();
                Arrays.sort(prefixSet);
                DefaultComboBoxModel model = new DefaultComboBoxModel(prefixSet);
                prefixComboBox.setModel(model);
                Resource uriRes = ResourceFactory.createResource(uri);
                String ns = Utils.getNameSpace(uriRes);
                String ln = Utils.getLocalName(uriRes);
                String prefix = nsTable.getPrefix(ns);
                if (prefix != null) {
                    prefixComboBox.setSelectedItem(prefix);
                }
                localNameField.setText(ln);
            }

            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if (path == null) { return; }
                ConceptTreeNode conceptTreeNode = (ConceptTreeNode) path.getLastPathComponent();
                if (conceptTreeNode != null) {
                    selectedConcept = conceptTreeNode.getConcept();
                    labelPanel.setSelectedConcept(selectedConcept);
                    descriptionPanel.setSelectedConcept(selectedConcept);
                    labelPanel.setLabelLangList();
                    labelPanel.setLabelList();
                    descriptionPanel.setDescriptionLangList();
                    descriptionPanel.setDescriptionList();
                    setURI(conceptTreeNode.getURI());
                    labelPanel.setTypicalWord(conceptTreeNode.getInputWord());
                    StringBuilder trimmedCntStr = new StringBuilder();
                    for (int trimmedCnt : conceptTreeNode.getTrimmedCountList()) {
                        trimmedCntStr.append(trimmedCnt + ", ");
                    }
                    trimmedNodeCntValueLabel.setText(trimmedCntStr.toString());
                    if (edrConceptDefinitionPanel != null && conceptTreeNode.getConcept() instanceof VerbConcept) {
                        edrConceptDefinitionPanel.init();
                        VerbConcept vc = (VerbConcept) conceptTreeNode.getConcept();
                        edrConceptDefinitionPanel.setDomainList(vc.getDomainSet());
                        edrConceptDefinitionPanel.setRangeList(vc.getRangeSet());
                    }
                    conceptDriftManagementPanel.traAction(conceptTreeNode);
                }
            }
        });

        renderer.setFont(new Font("Dialog", Font.PLAIN, 14));
        tree.setCellRenderer(renderer);
        tree.setEditable(true);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setVisible(false);
    }

    private JComponent getWestPanel(JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(component, BorderLayout.WEST);
        return panel;
    }

    private void deleteConceptTreeNode() {
        if (conceptTree.getSelectionCount() == 1) {
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) conceptTree.getLastSelectedPathComponent();
            if (node.getParent() != null) {
                model.removeNodeFromParent((DefaultMutableTreeNode) conceptTree.getLastSelectedPathComponent());
            }
        }
    }

    private boolean isSameConcept(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        return concept.getURI().equals(node.getConcept().getURI());
    }

    private void searchSameConceptTreeNode(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (isSameConcept(concept, childNode, sameConceptSet)) {
                sameConceptSet.add(childNode);
            }
            searchSameConceptTreeNode(concept, childNode, sameConceptSet);
        }
    }

    public void reloadConceptTreeNode(Concept concept) {
        reloadConceptTreeNode(concept, conceptTree);
    }

    public void reloadHasaTreeNode(Concept concept) {
        if (hasaTree != null) {
            reloadConceptTreeNode(concept, hasaTree);
        }
    }

    private void reloadConceptTreeNode(Concept concept, JTree tree) {
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        Set<ConceptTreeNode> sameConceptSet = new HashSet<ConceptTreeNode>();
        searchSameConceptTreeNode(concept, rootNode, sameConceptSet);
        for (ConceptTreeNode node : sameConceptSet) {
            treeModel.reload(node);
        }
    }

    public Concept getSelectedConcept() {
        return selectedConcept;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setURIButton) {
            NameSpaceTable nsTable = DODDLE.getCurrentProject().getOntologySelectionPanel().getNSTable();
            String ns = nsTable.getNS((String) prefixComboBox.getSelectedItem());
            String uri = ns + localNameField.getText();
            selectedConcept.setURI(uri);
        } else {
            if (!(conceptTree.getSelectionCount() == 1)) { return; }
            TreePath path = conceptTree.getSelectionPath();
            ConceptTreeNode conceptTreeNode = (ConceptTreeNode) path.getLastPathComponent();
            Concept concept = conceptTreeNode.getConcept();
            selectedConcept = concept;
            labelPanel.setSelectedConcept(selectedConcept);
            descriptionPanel.setSelectedConcept(selectedConcept);
            conceptTreeNode.setConcept(concept);
        }
    }

    /**
     * @param concept
     * @param label
     */
    public void setTypicalWord(Concept concept, DODDLELiteral label) {
        if (label != null) {
            concept.setInputLabel(label);
            labelPanel.setTypicalWord(label.getString());
            reloadConceptTreeNode(concept);
        }
    }
}
