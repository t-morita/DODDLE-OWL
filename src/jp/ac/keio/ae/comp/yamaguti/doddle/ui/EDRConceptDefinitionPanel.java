package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class EDRConceptDefinitionPanel extends JPanel implements ActionListener {

    private JList domainList;
    private DefaultListModel domainListModel;
    private JList rangeList;
    private DefaultListModel rangeListModel;
    private JButton addDomainButton;
    private JButton deleteDomainButton;
    private JButton addRangeButton;
    private JButton deleteRangeButton;

    private static ConceptSelectionDialog conceptSelectionDialog;
    private DODDLEProject project;

    public EDRConceptDefinitionPanel(DODDLEProject p) {
        project = p;
        conceptSelectionDialog = new ConceptSelectionDialog(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE,
                "Class Selection Dialog");

        domainListModel = new DefaultListModel();
        domainList = new JList(domainListModel);
        JScrollPane domainListScroll = new JScrollPane(domainList);
        domainListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("PropertyTreePanel.Domain")));

        addDomainButton = new JButton(Translator.getString("ConceptTreePanel.Add"));
        addDomainButton.addActionListener(this);
        deleteDomainButton = new JButton(Translator.getString("ConceptTreePanel.Remove"));
        deleteDomainButton.addActionListener(this);
        JPanel domainButtonPanel = new JPanel();
        domainButtonPanel.setLayout(new GridLayout(1, 2));
        domainButtonPanel.add(addDomainButton);
        domainButtonPanel.add(deleteDomainButton);

        JPanel domainPanel = new JPanel();
        domainPanel.setLayout(new BorderLayout());
        domainPanel.add(domainListScroll, BorderLayout.CENTER);
        domainPanel.add(domainButtonPanel, BorderLayout.SOUTH);

        rangeListModel = new DefaultListModel();
        rangeList = new JList(rangeListModel);
        JScrollPane rangeListScroll = new JScrollPane(rangeList);
        rangeListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("PropertyTreePanel.Range")));

        addRangeButton = new JButton(Translator.getString("ConceptTreePanel.Add"));
        addRangeButton.addActionListener(this);
        deleteRangeButton = new JButton(Translator.getString("ConceptTreePanel.Remove"));
        deleteRangeButton.addActionListener(this);
        JPanel rangeButtonPanel = new JPanel();
        rangeButtonPanel.setLayout(new GridLayout(1, 2));
        rangeButtonPanel.add(addRangeButton);
        rangeButtonPanel.add(deleteRangeButton);

        JPanel rangePanel = new JPanel();
        rangePanel.setLayout(new BorderLayout());
        rangePanel.add(rangeListScroll, BorderLayout.CENTER);
        rangePanel.add(rangeButtonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createTitledBorder(""));
        setLayout(new GridLayout(1, 2));
        add(domainPanel);
        add(rangePanel);
    }

    public void init() {
        domainListModel.clear();
        rangeListModel.clear();
    }

    public void setDomainList(Set<String> domainSet) {
        int num = 0;
        for (String uri: domainSet) {
            Concept c = project.getConcept(uri);
            if (c == null) {
                c = OWLOntologyManager.getConcept(uri);
            }
            if (project.getOntologySelectionPanel().isEDREnable() && c == null) {
                c = EDRDic.getEDRConcept(uri);
            }
            if (c != null) {
                domainListModel.add(num++, c);
            }
        }
    }

    public void setRangeList(Set<String> rangeSet) {
        int num = 0;
        for (String uri: rangeSet) {
            Concept c = project.getConcept(uri);
            if (c == null) {
                c = OWLOntologyManager.getConcept(uri);
            }
            if (project.getOntologySelectionPanel().isEDREnable() && c == null) {
                c = EDRDic.getEDRConcept(uri);
            }
            if (c != null) {
                rangeListModel.add(num++, c);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addDomainButton) {
            addRegion(domainListModel);
        } else if (e.getSource() == addRangeButton) {
            addRegion(rangeListModel);
        } else if (e.getSource() == deleteDomainButton) {
            removeRegion(domainList, domainListModel);
        } else if (e.getSource() == deleteRangeButton) {
            removeRegion(rangeList, rangeListModel);
        }
    }

    /**
     * 定義域，または，値域の追加
     */
    private void addRegion(DefaultListModel regionListModel) {
        JTree verbConceptTree = project.getConstructPropertyPanel().getConceptTree();
        if (verbConceptTree != null && verbConceptTree.getLastSelectedPathComponent() != null) {
            conceptSelectionDialog.setTreeModel(project.getConstructClassPanel().getConceptTree().getModel());
            conceptSelectionDialog.setVisible(true);
            Set addConceptSet = conceptSelectionDialog.getConceptSet();
            // System.out.println("add concept set: " + addConceptSet);
            ConceptTreeNode node = (ConceptTreeNode) verbConceptTree.getLastSelectedPathComponent();
            VerbConcept concept = (VerbConcept) node.getConcept();
            for (Iterator i = addConceptSet.iterator(); i.hasNext();) {
                Concept c = (Concept) i.next();
                // System.out.println("add: " + c);
                if (regionListModel == domainListModel && !concept.getDomainSet().contains(c.getURI())) {
                    concept.addDomain(c.getURI());
                    regionListModel.addElement(c);
                } else if (regionListModel == rangeListModel && !concept.getRangeSet().contains(c.getURI())) {
                    concept.addRange(c.getURI());
                    regionListModel.addElement(c);
                }
            }
            // System.out.println("domain: " + concept.getDomainSet());
            // System.out.println("range: " + concept.getRangeSet());
            node.setConcept(concept);
        }
    }

    /**
     * 定義域，または，値域の削除
     */
    private void removeRegion(JList regionList, DefaultListModel regionListModel) {
        JTree verbConceptTree = project.getConstructPropertyPanel().getConceptTree();
        ConceptTreeNode node = (ConceptTreeNode) verbConceptTree.getLastSelectedPathComponent();
        VerbConcept concept = (VerbConcept) node.getConcept();
        Object[] removeValues = regionList.getSelectedValues();
        for (int i = 0; i < removeValues.length; i++) {
            Concept c = (Concept) removeValues[i];
            // System.out.println("remove: " + c);
            if (regionListModel == domainListModel) {
                concept.deleteDomain(c.getURI());
            } else if (regionListModel == rangeListModel) {
                concept.deleteRange(c.getURI());
            }
            regionListModel.removeElement(c);
        }
        node.setConcept(concept);
    }
}
