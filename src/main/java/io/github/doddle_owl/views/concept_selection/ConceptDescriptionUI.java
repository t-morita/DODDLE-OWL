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

package io.github.doddle_owl.views.concept_selection;

import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.concept_definition.ConceptDefinition;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Takeshi Morita
 */
class ConceptDescriptionUI extends JPanel implements ListSelectionListener, ActionListener {

    private Set<Concept> inputConceptSet;

    private final JLabel idLabel;
    private final JTextField idField;
    private final JButton evalButton;
    private final JCheckBox showOnlyInputConceptsButton;

    private final JList fromRelationJList;
    private final JList verbIDJList;
    private final JList subVerbIDJList;
    private final JList toRelationJList;
    private final JList nounIDJList;
    private final JList subNounIDJList;

    private final ConceptDefinition conceptDescription;

    public ConceptDescriptionUI() {
        conceptDescription = ConceptDefinition.getInstance();

        idLabel = new JLabel("");
        idField = new JTextField();
        evalButton = new JButton("eval");
        evalButton.addActionListener(this);
        showOnlyInputConceptsButton = new JCheckBox("入力概念のみ表示");
        showOnlyInputConceptsButton.setSelected(true);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(evalButton);
        buttonPanel.add(showOnlyInputConceptsButton);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(idLabel, BorderLayout.WEST);
        inputPanel.add(idField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        fromRelationJList = new JList();
        fromRelationJList.addListSelectionListener(this);
        JScrollPane fromRelationJListScroll = new JScrollPane(fromRelationJList);
        fromRelationJListScroll.setBorder(BorderFactory.createTitledBorder("←関係子"));
        verbIDJList = new JList();
        verbIDJList.addListSelectionListener(this);
        verbIDJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane verbIDJListScroll = new JScrollPane(verbIDJList);
        verbIDJListScroll.setBorder(BorderFactory.createTitledBorder("動詞的概念リスト"));
        subVerbIDJList = new JList();
        subVerbIDJList.addListSelectionListener(this);
        subVerbIDJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane subVerbIDJListScroll = new JScrollPane(subVerbIDJList);
        subVerbIDJListScroll.setBorder(BorderFactory.createTitledBorder("動詞的概念の下位概念リスト"));
        JPanel verbPanel = new JPanel();
        verbPanel.setLayout(new GridLayout(2, 1));
        verbPanel.add(verbIDJListScroll);
        verbPanel.add(subVerbIDJListScroll);
        toRelationJList = new JList();
        toRelationJList.addListSelectionListener(this);
        JScrollPane toRelationJListScroll = new JScrollPane(toRelationJList);
        toRelationJListScroll.setBorder(BorderFactory.createTitledBorder("関係子→"));
        nounIDJList = new JList();
        nounIDJList.addListSelectionListener(this);
        JScrollPane nounIDJListScroll = new JScrollPane(nounIDJList);
        nounIDJListScroll.setBorder(BorderFactory.createTitledBorder("名詞的概念リスト"));
        subNounIDJList = new JList();
        subNounIDJList.addListSelectionListener(this);
        JScrollPane subNounIDJListScroll = new JScrollPane(subNounIDJList);
        subNounIDJListScroll.setBorder(BorderFactory.createTitledBorder("名詞的概念の下位概念リスト"));
        JPanel nounPanel = new JPanel();
        nounPanel.setLayout(new GridLayout(2, 1));
        nounPanel.add(nounIDJListScroll);
        nounPanel.add(subNounIDJListScroll);

        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new GridLayout(1, 4));
        selectionPanel.add(fromRelationJListScroll);
        selectionPanel.add(verbPanel);
        selectionPanel.add(toRelationJListScroll);
        selectionPanel.add(nounPanel);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(selectionPanel, BorderLayout.CENTER);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == fromRelationJList) {
            setVerbIDList();
        } else if (e.getSource() == verbIDJList) {
            setSubVerbIDList();
            setToRelationList();
        } else if (e.getSource() == toRelationJList) {
            setNounIDList();
        } else if (e.getSource() == nounIDJList) {
            setSubNounIDList();
        }
    }

    public void setConcept(String id) {
        idLabel.setText(id);
        setFromRelationList();
    }

    public void setInputConceptSet(Set<Concept> set) {
        inputConceptSet = set;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == evalButton) {
            setConcept(idField.getText());
        }
    }

    private void setVerbIDList() {
        Set verbIDSet = new TreeSet();
        List<String> relationList = fromRelationJList.getSelectedValuesList();
        for (String relation : relationList) {
            verbIDSet.addAll(conceptDescription.getVerbIDSet(idLabel.getText(), relation));
        }
        if (showOnlyInputConceptsButton.isSelected()) {
            Set inputVerbIDSet = new TreeSet();
            for (Object o : verbIDSet) {
                String verbID = (String) o;
                if (inputConceptSet.contains(verbID)) {
                    inputVerbIDSet.add(verbID);
                }
            }
            verbIDJList.setListData(inputVerbIDSet.toArray());
        } else {
            verbIDJList.setListData(verbIDSet.toArray());
        }
    }

    private void setSubVerbIDList() {
        String verbID = (String) verbIDJList.getSelectedValue();
        Set<String> subVerbURISet = new TreeSet<>(conceptDescription.getSubURISet(verbID));
        if (showOnlyInputConceptsButton.isSelected()) {
            Set<String> inputSubVerbIDSet = new TreeSet<>();
            for (String subVerbID : subVerbURISet) {
                if (inputConceptSet.contains(subVerbID)) {
                    inputSubVerbIDSet.add(subVerbID);
                }
            }
            subVerbIDJList.setListData(inputSubVerbIDSet.toArray());
        } else {
            subVerbIDJList.setListData(subVerbURISet.toArray());
        }
    }

    private void setSubNounIDList() {
        String nounID = (String) nounIDJList.getSelectedValue();
        Set subNounIDSet = new TreeSet(conceptDescription.getSubURISet(nounID));
        if (showOnlyInputConceptsButton.isSelected()) {
            Set inputSubNounIDSet = new TreeSet();
            for (Object o : subNounIDSet) {
                String subNounID = (String) o;
                if (inputConceptSet.contains(subNounID)) {
                    inputSubNounIDSet.add(subNounID);
                }
            }
            subNounIDJList.setListData(inputSubNounIDSet.toArray());
        } else {
            subNounIDJList.setListData(subNounIDSet.toArray());
        }
    }

    private void setFromRelationList() {
        // Set fromRelationSet =
        // conceptDescription.getFromRelationSet(idLabel.getText());
        // fromRelationJList.setListData(fromRelationSet.toArray());
        fromRelationJList.setListData(ConceptDefinition.relationList);
    }

    private void setToRelationList() {
        // String verbID = (String) verbIDJList.getSelectedValue();
        // toRelationJList.setListData(conceptDescription.getToRelationSet(verbID).toArray());
        toRelationJList.setListData(ConceptDefinition.relationList);
    }

    private void setNounIDList() {
        Set nounIDSet = new TreeSet();
        String verbID = (String) verbIDJList.getSelectedValue();
        List<String> toRelationList = toRelationJList.getSelectedValuesList();
        for (String relation : toRelationList) {
            if (conceptDescription.getIDSet(verbID, relation) != null) {
                nounIDSet.addAll(conceptDescription.getIDSet(verbID, relation));
            }
        }
        if (showOnlyInputConceptsButton.isSelected()) {
            Set inputNounIDSet = new TreeSet();
            for (Object o : nounIDSet) {
                String nounID = (String) o;
                if (inputConceptSet.contains(nounID)) {
                    inputNounIDSet.add(nounID);
                }
            }
            subNounIDJList.setListData(inputNounIDSet.toArray());
        } else {
            nounIDJList.setListData(nounIDSet.toArray());
        }
    }

    public static void main(String[] args) {
        // EDRTree.initialize();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 800, 600);
        ConceptDescriptionUI panel = new ConceptDescriptionUI();
        Container contentPane = frame.getContentPane();
        contentPane.add(panel);
        frame.setVisible(true);
    }
}
