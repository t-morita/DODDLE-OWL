/*
 * Project Name: DODDLE (a Domain Ontology rapiD DeveLopment Environment)
 * Project Website: http://doddle-owl.sourceforge.net/
 *
 * Copyright (C) 2004-2009 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class EditDescriptionDialog extends JDialog implements ActionListener {

    private DODDLELiteral description;

    private JTextField langField;
    private JTextArea descriptionArea;

    private JButton applyButton;
    private JButton cancelButton;

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
