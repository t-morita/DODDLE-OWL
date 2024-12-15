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

package io.github.doddle_owl.views.reference_ontology_selection;

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.reference_ontology_selection.OWLOntologyExtractionTemplate;
import io.github.doddle_owl.models.reference_ontology_selection.ReferenceOWLOntology;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Takeshi Morita
 */
class OWLMetaDataPanel extends JPanel implements ActionListener {

    private final JCheckBox isAvailableCheckBox;

    private final JLabel locationLabel;

    private final OWLMetaDataTablePanel owlMetaDataTablePanel;

    private final JTextField searchSubConceptTemplateField;
    private final JButton setSearchSubConceptTemplateButton;
    private final JTextField searchConceptTemplateField;
    private final JButton setSearchConceptTemplateButton;
    private final JTextField searchClassSetTemplateField;
    private final JButton setSearchClassSetTemplateButton;
    private final JTextField searchPropertySetTemplateField;
    private final JButton setSearchPropertySetTemplateButton;
    private final JTextField searchRegionSetTemplateField;
    private final JButton setSearchRegionSetTemplateButton;

    private ReferenceOWLOntology refOnt;
    private static final int TEXT_FIELD_WIDTH = 30;

    public OWLMetaDataPanel() {

        JPanel templatePanel = new JPanel();
        templatePanel.setLayout(new GridLayout(8, 1));

        isAvailableCheckBox = new JCheckBox(Translator.getTerm("ValidCheckBox"));
        isAvailableCheckBox.addActionListener(this);
        templatePanel.add(isAvailableCheckBox);

        JLabel locationTitleLabel = new JLabel(Translator.getTerm("LocationLabel") + ": ");
        locationLabel = new JLabel();
        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new BorderLayout());
        locationPanel.add(locationTitleLabel, BorderLayout.WEST);
        locationPanel.add(locationLabel, BorderLayout.CENTER);
        templatePanel.add(locationPanel);
        String browse = Translator.getTerm("ReferenceButton");

        JLabel searchClassSetTemplateLabel = new JLabel(Translator.getTerm("SearchClassSetTemplateLabel") + ": ");
        searchClassSetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchClassSetTemplateField.setEditable(false);
        setSearchClassSetTemplateButton = new JButton(browse);
        setSearchClassSetTemplateButton.addActionListener(this);
        JPanel panel = getTemplatePanel(searchClassSetTemplateLabel, searchClassSetTemplateField,
                setSearchClassSetTemplateButton);
        templatePanel.add(Utils.createEastPanel(panel));

        JLabel searchPropertySetTemplateLabel = new JLabel(Translator.getTerm("SearchPropertySetTemplateLabel") + ": ");
        searchPropertySetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchPropertySetTemplateField.setEditable(false);
        setSearchPropertySetTemplateButton = new JButton(browse);
        setSearchPropertySetTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchPropertySetTemplateLabel, searchPropertySetTemplateField,
                setSearchPropertySetTemplateButton);
        templatePanel.add(Utils.createEastPanel(panel));

        JLabel searchConceptTemplateLabel = new JLabel(Translator.getTerm("SearchConceptTemplateLabel") + ": ");
        searchConceptTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchConceptTemplateField.setEditable(false);
        setSearchConceptTemplateButton = new JButton(browse);
        setSearchConceptTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchConceptTemplateLabel, searchConceptTemplateField, setSearchConceptTemplateButton);
        templatePanel.add(Utils.createEastPanel(panel));

        JLabel searchSubConceptTemplateLabel = new JLabel(Translator.getTerm("SearchSubConceptTemplateLabel") + ": ");
        searchSubConceptTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchSubConceptTemplateField.setEditable(false);
        setSearchSubConceptTemplateButton = new JButton(browse);
        setSearchSubConceptTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchSubConceptTemplateLabel, searchSubConceptTemplateField,
                setSearchSubConceptTemplateButton);
        templatePanel.add(Utils.createEastPanel(panel));

        JLabel searchRegionSetTemplateLabel = new JLabel(Translator.getTerm("SearchRegionSetTemplateLabel") + ": ");
        searchRegionSetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchRegionSetTemplateField.setEditable(false);
        setSearchRegionSetTemplateButton = new JButton(browse);
        setSearchRegionSetTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchRegionSetTemplateLabel, searchRegionSetTemplateField,
                setSearchRegionSetTemplateButton);
        templatePanel.add(Utils.createEastPanel(panel));

        owlMetaDataTablePanel = new OWLMetaDataTablePanel();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(Utils.createNorthPanel(templatePanel), BorderLayout.CENTER);
        mainPanel.add(owlMetaDataTablePanel, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.WEST);
    }

    public void setMetaData(ReferenceOWLOntology ont) {
        refOnt = ont;
        OWLOntologyExtractionTemplate owlExtractionTemplate = refOnt.getOWLOntologyExtractionTemplate();
        owlMetaDataTablePanel.setModel(refOnt.getOWLMetaDataTableModel());
        isAvailableCheckBox.setSelected(refOnt.isAvailable());
        locationLabel.setText(refOnt.getURI());
        searchConceptTemplateField.setText(owlExtractionTemplate.getSearchConceptTemplateLabel());
        searchSubConceptTemplateField.setText(owlExtractionTemplate.getSearchSubConceptTemplateLabel());
        searchClassSetTemplateField.setText(owlExtractionTemplate.getSearchClassSetTemplateLabel());
        searchPropertySetTemplateField.setText(owlExtractionTemplate.getSearchPropertySetTemplateLabel());
        searchRegionSetTemplateField.setText(owlExtractionTemplate.getSearchRegionSetTemplateLabel());
    }

    private JPanel getTemplatePanel(JLabel label, JTextField field, JButton button) {
        JPanel templatePanel = new JPanel();
        templatePanel.add(label);
        templatePanel.add(field);
        templatePanel.add(button);
        return templatePanel;
    }

    private String getTemplateFileName(String currentDir) {
        JFileChooser chooser = new JFileChooser(currentDir);
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return ""; }
        return chooser.getSelectedFile().getAbsolutePath();
    }

    public void actionPerformed(ActionEvent e) {
        if (refOnt == null) { return; }
        String templateFileName;
        OWLOntologyExtractionTemplate owlExtractionTemplate = refOnt.getOWLOntologyExtractionTemplate();
        if (e.getSource() == setSearchClassSetTemplateButton) {
            templateFileName = getTemplateFileName(searchClassSetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchClassSetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchClassSetTemplate(new File(templateFileName));
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchPropertySetTemplateButton) {
            templateFileName = getTemplateFileName(searchPropertySetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchPropertySetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchPropertySetTemplate(new File(templateFileName));
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchRegionSetTemplateButton) {
            templateFileName = getTemplateFileName(searchRegionSetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchRegionSetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchRegionSetTemplate(new File(templateFileName));
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchConceptTemplateButton) {
            templateFileName = getTemplateFileName(searchConceptTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchConceptTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchConceptTemplate(new File(templateFileName));
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchSubConceptTemplateButton) {
            templateFileName = getTemplateFileName(searchSubConceptTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchSubConceptTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchSubConceptTemplate(new File(templateFileName));
                refOnt.reload();
            }
        } else if (e.getSource() == isAvailableCheckBox) {
            refOnt.setAvailable(isAvailableCheckBox.isSelected());
        }
    }
}
