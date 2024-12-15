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


import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takeshi Morita
 */
class EditDescriptionDialog extends JDialog implements ActionListener {

    private final DODDLELiteral description;

    private final JTextField langField;
    private final JTextArea descriptionArea;

    private final JButton applyButton;
    private final JButton cancelButton;

    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 250;

    public EditDescriptionDialog(Frame rootFrame) {
        super(rootFrame, Translator.getTerm("EditDescriptionDialog"), true);

        description = new DODDLELiteral("", "");

        langField = new JTextField(5);
        JComponent langFieldP = Utils.createTitledPanel(langField, Translator.getTerm("LangTextField"), 50, 20);
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        JScrollPane commentAreaScroll = new JScrollPane(descriptionArea);
        commentAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("DescriptionBorder")));

        applyButton = new JButton(Translator.getTerm("OKButton"));
        applyButton.setMnemonic('o');
        applyButton.addActionListener(this);
        cancelButton = new JButton(Translator.getTerm("CancelButton"));
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(Utils.createWestPanel(langFieldP), BorderLayout.NORTH);
        getContentPane().add(commentAreaScroll, BorderLayout.CENTER);
        getContentPane().add(Utils.createEastPanel(buttonPanel), BorderLayout.SOUTH);

        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setLocationRelativeTo(rootFrame);
        setVisible(false);
    }

    public void setDescription(DODDLELiteral description) {
        langField.setText(description.getLang());
        descriptionArea.setText(description.getString());
    }

    public DODDLELiteral getDescription() {
        return description;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            description.setLang(langField.getText());
            description.setString(descriptionArea.getText());
            descriptionArea.requestFocus();
            setVisible(false);
        } else if (e.getSource() == cancelButton) {
            description.setLang("");
            description.setString("");
            descriptionArea.requestFocus();
            setVisible(false);
        }
    }
}
