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

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.concept_selection.LiteralPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Takeshi Morita
 */
class DescriptionPanel extends LiteralPanel implements ActionListener {

    private final JButton addDescriptionButton;
    private final JButton deleteDescriptionButton;
    private final JButton editDescriptionButton;

    public DescriptionPanel(String type) {
        super(Translator.getTerm("LanguageLabel"), Translator.getTerm("DescriptionList"), type);

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new GridLayout(1, 3));
        addDescriptionButton = new JButton(Translator.getTerm("AddButton"));
        addDescriptionButton.addActionListener(this);
        deleteDescriptionButton = new JButton(Translator.getTerm("RemoveButton"));
        deleteDescriptionButton.addActionListener(this);
        editDescriptionButton = new JButton(Translator.getTerm("EditButton"));
        editDescriptionButton.addActionListener(this);
        editPanel.add(addDescriptionButton);
        editPanel.add(editDescriptionButton);
        editPanel.add(deleteDescriptionButton);

        setBorder(BorderFactory.createTitledBorder(Translator.getTerm("DescriptionBorder")));
        add(editPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (selectedConcept == null) { return; }
        if (e.getSource() == addDescriptionButton) {
            EditDescriptionDialog editDescriptionDialog = new EditDescriptionDialog(DODDLE_OWL.rootFrame);
            editDescriptionDialog.setVisible(true);
            selectedConcept.addDescription(editDescriptionDialog.getDescription());
            setDescriptionLangList();
        } else if (e.getSource() == deleteDescriptionButton) {
            List<DODDLELiteral> descriptionList = literalJList.getSelectedValuesList();
            for (DODDLELiteral literal: descriptionList)  {
                selectedConcept.removeDescription(literal);
            }
            setDescriptionLangList();
        } else if (e.getSource() == editDescriptionButton) {
            if (literalJList.getSelectedValuesList().size() == 1) {
                EditDescriptionDialog editDescriptionDialog = new EditDescriptionDialog(DODDLE_OWL.rootFrame);
                DODDLELiteral description = (DODDLELiteral) literalJList.getSelectedValue();
                editDescriptionDialog.setDescription(description);
                editDescriptionDialog.setVisible(true);
                DODDLELiteral editDescription = editDescriptionDialog.getDescription();
                if (0 < editDescription.getString().length()) {
                    if (editDescription.getLang().equals(description.getLang())) {
                        description.setString(editDescription.getString());
                    } else {
                        selectedConcept.removeDescription(description);
                        description.setLang(editDescription.getLang());
                        description.setString(editDescription.getString());
                        selectedConcept.addDescription(description);
                    }
                    setDescriptionLangList();
                }
            }
        }
    }
}
