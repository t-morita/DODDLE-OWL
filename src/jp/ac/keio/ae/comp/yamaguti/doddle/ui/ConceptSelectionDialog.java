/*
 * @(#)  2005/09/15
 *
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class ConceptSelectionDialog extends JDialog implements ActionListener, ListSelectionListener,
        TreeSelectionListener {

    private JList predefinedRelationJList;

    private Set selectedConceptSet;
    private JTree conceptTree;
    private JButton expandButton;
    private JButton applyButton;
    private JButton cancelButton;

    public static Concept agentConcept = new Concept(DODDLE.BASE_URI+"DID0", "agent");
    public static Concept objectConcept = new Concept(DODDLE.BASE_URI+"DID1", "object");

    public ConceptSelectionDialog(String type, String title) {
        selectedConceptSet = new HashSet();

        conceptTree = new JTree(new DefaultTreeModel(null));
        conceptTree.addTreeSelectionListener(this);
        conceptTree.setCellRenderer(new ConceptTreeCellRenderer(type));
        JScrollPane conceptTreeScroll = new JScrollPane(conceptTree);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(conceptTreeScroll, BorderLayout.CENTER);
        if (type.equals(ConceptTreeCellRenderer.VERB_CONCEPT_TREE)) {
            agentConcept = new Concept(DODDLE.BASE_URI+"DID0", "agent");
            objectConcept = new Concept(DODDLE.BASE_URI+"DID1", "object");
            predefinedRelationJList = new JList(new Concept[] { agentConcept, objectConcept});
            predefinedRelationJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            predefinedRelationJList.addListSelectionListener(this);
            predefinedRelationJList.setPreferredSize(new Dimension(150, 150));
            predefinedRelationJList.setBorder(BorderFactory.createTitledBorder("Predfined Relation"));
            mainPanel.add(predefinedRelationJList, BorderLayout.EAST);
        }

        expandButton = new JButton(Translator.getString("ConceptTreePanel.expandConceptTree"));
        expandButton.addActionListener(this);
        applyButton = new JButton("OK");
        applyButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(expandButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        JPanel eastButtonPanel = new JPanel();
        eastButtonPanel.setLayout(new BorderLayout());
        eastButtonPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(eastButtonPanel, BorderLayout.SOUTH);

        setTitle(title);
        setSize(800, 600);
        setModal(true);
        setLocationRelativeTo(DODDLE.rootPane);
    }

    public void setSingleSelection() {
        conceptTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void setTreeModel(TreeModel model) {
        conceptTree.setModel(model);
        expandAllPath();
    }

    private void expandAllPath() {
        for (int i = 0; i < conceptTree.getRowCount(); i++) {
            conceptTree.expandPath(conceptTree.getPathForRow(i));
        }
    }

    public void setVisible(boolean t) {
        conceptTree.clearSelection();
        super.setVisible(t);
    }

    public Set getConceptSet() {
        return selectedConceptSet;
    }

    public Concept getConcept() {
        if (selectedConceptSet.size() == 1) { return (Concept) selectedConceptSet.toArray()[0]; }
        return null;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == expandButton) {
            expandAllPath();
        } else if (e.getSource() == applyButton) {
            selectedConceptSet.clear();
            if (predefinedRelationJList != null && predefinedRelationJList.getSelectedValue() != null) {
                selectedConceptSet.add(predefinedRelationJList.getSelectedValue());
            } else {
                TreePath[] paths = conceptTree.getSelectionPaths();
                for (int i = 0; i < paths.length; i++) {
                    ConceptTreeNode node = (ConceptTreeNode) paths[i].getLastPathComponent();
                    selectedConceptSet.add(node.getConcept());
                }
            }
            setVisible(false);
        } else if (e.getSource() == cancelButton) {
            selectedConceptSet.clear();
            setVisible(false);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == predefinedRelationJList) {
            conceptTree.clearSelection();
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (e.getSource() == conceptTree && predefinedRelationJList != null) {
            predefinedRelationJList.clearSelection();
        }
    }
}
