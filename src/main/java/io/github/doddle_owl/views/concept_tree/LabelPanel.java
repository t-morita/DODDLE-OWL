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

import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.concept_selection.LiteralPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takeshi Morita
 */
public class LabelPanel extends LiteralPanel implements ActionListener {

    private final JLabel preferredTermValueLabel;
    private final JTextField langField;
    private final JTextField labelField;
    private final JButton addLabelButton;
    private final JButton deleteLabelButton;
    private final JButton editLabelButton;
    private final JButton setPreferredLabelButton;

    private final ConceptInformationPanel conceptInfoPanel;

    @Override
    public void setSelectedConcept(Concept c) {
        super.setSelectedConcept(c);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public LabelPanel(String type, ConceptInformationPanel ciPanel) {
        super(Translator.getTerm("LanguageLabel"), Translator.getTerm("LabelList"), type);
        conceptInfoPanel = ciPanel;

        JLabel preferredTermLabel = new JLabel(Translator.getTerm("PreferredTermLabel") + ": ");
        preferredTermValueLabel = new JLabel("");
        setPreferredLabelButton = new JButton(Translator.getTerm("SetPreferredTermButton"));
        setPreferredLabelButton.addActionListener(this);
        var preferredTermPanel = new JPanel();
        preferredTermPanel.setLayout(new GridLayout(1, 3));
        preferredTermPanel.add(setPreferredLabelButton);
        preferredTermPanel.add(preferredTermLabel);
        preferredTermPanel.add(preferredTermValueLabel);

        var langLabel = new JLabel(Translator.getTerm("LangTextField"));
        langField = new JTextField(5);
        var labelLabel = new JLabel(Translator.getTerm("LabelTextField"));
        labelField = new JTextField(15);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridLayout(2,2));
        fieldPanel.add(langLabel);
        fieldPanel.add(labelLabel);
        fieldPanel.add(langField);
        fieldPanel.add(labelField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));
        addLabelButton = new JButton(Translator.getTerm("AddButton"));
        addLabelButton.addActionListener(this);
        deleteLabelButton = new JButton(Translator.getTerm("RemoveButton"));
        deleteLabelButton.addActionListener(this);
        editLabelButton = new JButton(Translator.getTerm("EditButton"));
        editLabelButton.addActionListener(this);
        buttonPanel.add(addLabelButton);
        buttonPanel.add(editLabelButton);
        buttonPanel.add(deleteLabelButton);

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BorderLayout());
        editPanel.add(fieldPanel, BorderLayout.NORTH);
        editPanel.add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createTitledBorder(Translator.getTerm("LabelBorder")));
        add(preferredTermPanel, BorderLayout.NORTH);
        add(editPanel, BorderLayout.SOUTH);
    }

    public void setPreferentialTerm(String word) {
        preferredTermValueLabel.setText(word);
    }

    public void clearPreferentialTermValue() {
        preferredTermValueLabel.setText("");
    }

    public void clearLabelField() {
        langField.setText("");
        labelField.setText("");
    }

    public void setLabelList() {
        super.setLabelList();
        if (langJList.getSelectedValuesList().isEmpty()) {
            langField.setText("");
        } else if (langJList.getSelectedValuesList().size() == 1) {
            langField.setText(langJList.getSelectedValue().toString());
        }
    }

    public void setField() {
        if (literalJList.getSelectedValuesList().isEmpty()) {
            labelField.setText("");
        } else if (literalJList.getSelectedValuesList().size() == 1) {
            DODDLELiteral label = (DODDLELiteral) literalJList.getSelectedValue();
            labelField.setText(label.getString());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (selectedConcept == null) {
            return;
        }
        if (e.getSource() == addLabelButton) {
            selectedConcept.addLabel(new DODDLELiteral(langField.getText(), labelField.getText()));
            setLabelLangList();
            clearLabelField();
        } else if (e.getSource() == deleteLabelButton) {
            java.util.List<DODDLELiteral> labelList = literalJList.getSelectedValuesList();
            for (DODDLELiteral label : labelList) {
                if (label.getString().equals(preferredTermValueLabel.getText())) {
                    conceptInfoPanel.setPreferentialTerm(selectedConcept, new DODDLELiteral("", ""));
                }
                selectedConcept.removeLabel(label);
            }
            setLabelLangList();
            clearLabelField();
        } else if (e.getSource() == editLabelButton) {
            if (literalJList.getSelectedIndices().length == 1 && 0 < labelField.getText().length()) {
                DODDLELiteral label = (DODDLELiteral) literalJList.getSelectedValue();
                String labelText = label.getString();
                if (label.getLang().equals(langField.getText())) {
                    label.setString(labelField.getText());
                } else {
                    selectedConcept.removeLabel(label);
                    label.setLang(langField.getText());
                    label.setString(labelField.getText());
                    selectedConcept.addLabel(label);
                }
                if (labelText.equals(preferredTermValueLabel.getText())) {
                    conceptInfoPanel.setPreferentialTerm(selectedConcept, label);
                }
                setLabelLangList();
                clearLabelField();
                conceptInfoPanel.reloadConceptTreeNode(selectedConcept);
                conceptInfoPanel.reloadHasaTreeNode(selectedConcept);
            }
        } else if (e.getSource() == setPreferredLabelButton) {
            DODDLELiteral label = (DODDLELiteral) literalJList.getSelectedValue();
            conceptInfoPanel.setPreferentialTerm(selectedConcept, label);
        }
    }
}
