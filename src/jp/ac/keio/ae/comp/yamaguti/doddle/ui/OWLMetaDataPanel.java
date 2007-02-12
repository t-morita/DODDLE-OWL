/*
 * @(#)  2007/02/09
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OWLMetaDataPanel extends JPanel implements ActionListener {

    private JCheckBox isAvailableCheckBox;

    private JLabel locationTitleLabel;
    private JLabel locationLabel;

    private JLabel searchSubConceptTemplateLabel;
    private JTextField searchSubConceptTemplateField;
    private JButton setSearchSubConceptTemplateButton;
    private JLabel searchConceptTemplateLabel;
    private JTextField searchConceptTemplateField;
    private JButton setSearchConceptTemplateButton;
    private JLabel searchLabelSetTemplateLabel;
    private JTextField searchLabelSetTemplateField;
    private JButton setSearchLabelSetTemplateButton;
    private JLabel searchClassSetTemplateLabel;
    private JTextField searchClassSetTemplateField;
    private JButton setSearchClassSetTemplateButton;
    private JLabel searchPropertySetTemplateLabel;
    private JTextField searchPropertySetTemplateField;
    private JButton setSearchPropertySetTemplateButton;
    private JLabel searchDomainSetTemplateLabel;
    private JTextField searchDomainSetTemplateField;
    private JButton setSearchDomainSetTemplateButton;
    private JLabel searchRangeSetTemplateLabel;
    private JTextField searchRangeSetTemplateField;
    private JButton setSearchRangeSetTemplateButton;

    private ReferenceOWLOntology refOnt;
    private static final int TEXT_FIELD_WIDTH = 50;

    public OWLMetaDataPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(9, 1));

        isAvailableCheckBox = new JCheckBox(Translator.getString("OWLOntologySelectionPanel.isAvailable"));
        isAvailableCheckBox.addActionListener(this);
        mainPanel.add(isAvailableCheckBox);

        locationTitleLabel = new JLabel("Location: ");
        locationLabel = new JLabel();
        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new BorderLayout());
        locationPanel.add(locationTitleLabel, BorderLayout.WEST);
        locationPanel.add(locationLabel, BorderLayout.CENTER);
        mainPanel.add(locationPanel);
        String browse = Translator.getString("OWLOntologySelectionPanel.Browse");
        searchConceptTemplateLabel = new JLabel("SearchConceptTemplate: ");
        searchConceptTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchConceptTemplateField.setEditable(false);
        setSearchConceptTemplateButton = new JButton(browse);
        setSearchConceptTemplateButton.addActionListener(this);
        JPanel panel = getTemplatePanel(searchConceptTemplateLabel, searchConceptTemplateField,
                setSearchConceptTemplateButton);
        mainPanel.add(panel);

        searchSubConceptTemplateLabel = new JLabel("SearchSubConceptTemplate: ");
        searchSubConceptTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchSubConceptTemplateField.setEditable(false);
        setSearchSubConceptTemplateButton = new JButton(browse);
        setSearchSubConceptTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchSubConceptTemplateLabel, searchSubConceptTemplateField,
                setSearchSubConceptTemplateButton);
        mainPanel.add(panel);

        searchLabelSetTemplateLabel = new JLabel("SearchLabelSetTemplate: ");
        searchLabelSetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchLabelSetTemplateField.setEditable(false);
        setSearchLabelSetTemplateButton = new JButton(browse);
        setSearchLabelSetTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchLabelSetTemplateLabel, searchLabelSetTemplateField,
                setSearchLabelSetTemplateButton);
        mainPanel.add(panel);

        searchClassSetTemplateLabel = new JLabel("SearchClassSetTemplate: ");
        searchClassSetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchClassSetTemplateField.setEditable(false);
        setSearchClassSetTemplateButton = new JButton(browse);
        setSearchClassSetTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchClassSetTemplateLabel, searchClassSetTemplateField,
                setSearchClassSetTemplateButton);
        mainPanel.add(panel);

        searchPropertySetTemplateLabel = new JLabel("SearchPropertySetTemplate: ");
        searchPropertySetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchPropertySetTemplateField.setEditable(false);
        setSearchPropertySetTemplateButton = new JButton(browse);
        setSearchPropertySetTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchPropertySetTemplateLabel, searchPropertySetTemplateField,
                setSearchPropertySetTemplateButton);
        mainPanel.add(panel);

        searchDomainSetTemplateLabel = new JLabel("SearchDomainSetTemplate: ");
        searchDomainSetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchDomainSetTemplateField.setEditable(false);
        setSearchDomainSetTemplateButton = new JButton(browse);
        setSearchDomainSetTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchDomainSetTemplateLabel, searchDomainSetTemplateField,
                setSearchDomainSetTemplateButton);
        mainPanel.add(panel);

        searchRangeSetTemplateLabel = new JLabel("SearchRangeSetTemplate: ");
        searchRangeSetTemplateField = new JTextField(TEXT_FIELD_WIDTH);
        searchRangeSetTemplateField.setEditable(false);
        setSearchRangeSetTemplateButton = new JButton(browse);
        setSearchRangeSetTemplateButton.addActionListener(this);
        panel = getTemplatePanel(searchRangeSetTemplateLabel, searchRangeSetTemplateField,
                setSearchRangeSetTemplateButton);
        mainPanel.add(panel);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    public void setMetaData(ReferenceOWLOntology ont) {
        refOnt = ont;
        OWLOntologyExtractionTemplate owlExtractionTemplate = refOnt.getOWLOntologyExtractionTemplate();
        isAvailableCheckBox.setSelected(refOnt.isAvailable());
        locationLabel.setText(refOnt.getURI());
        searchConceptTemplateField.setText(owlExtractionTemplate.getSearchConceptTemplate());
        searchSubConceptTemplateField.setText(owlExtractionTemplate.getSearchSubConceptTemplate());
        searchLabelSetTemplateField.setText(owlExtractionTemplate.getSearchLabelSetTemplate());
        searchClassSetTemplateField.setText(owlExtractionTemplate.getSearchClassSetTemplate());
        searchPropertySetTemplateField.setText(owlExtractionTemplate.getSearchPropertySetTemplate());
        searchDomainSetTemplateField.setText(owlExtractionTemplate.getSearchDomainSetTemplate());
        searchRangeSetTemplateField.setText(owlExtractionTemplate.getSearchRangeSetTemplate());
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
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return ""; }
        return chooser.getSelectedFile().getAbsolutePath();
    }

    public void actionPerformed(ActionEvent e) {
        if (refOnt == null) { return; }
        String templateFileName = "";
        OWLOntologyExtractionTemplate owlExtractionTemplate = refOnt.getOWLOntologyExtractionTemplate();
        if (e.getSource() == setSearchLabelSetTemplateButton) {
            templateFileName = getTemplateFileName(searchLabelSetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchLabelSetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchLabelSetTemplate(templateFileName);
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchClassSetTemplateButton) {
            templateFileName = getTemplateFileName(searchClassSetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchClassSetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchClassSetTemplate(templateFileName);
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchPropertySetTemplateButton) {
            templateFileName = getTemplateFileName(searchPropertySetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchPropertySetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchPropertySetTemplate(templateFileName);
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchDomainSetTemplateButton) {
            templateFileName = getTemplateFileName(searchDomainSetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchDomainSetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchDomainSetTemplate(templateFileName);
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchRangeSetTemplateButton) {
            templateFileName = getTemplateFileName(searchRangeSetTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchRangeSetTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchRangeSetTemplate(templateFileName);
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchConceptTemplateButton) {
            templateFileName = getTemplateFileName(searchConceptTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchConceptTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchConceptTemplate(templateFileName);
                refOnt.reload();
            }
        } else if (e.getSource() == setSearchSubConceptTemplateButton) {
            templateFileName = getTemplateFileName(searchSubConceptTemplateField.getText());
            if (0 < templateFileName.length()) {
                searchSubConceptTemplateField.setText(templateFileName);
                owlExtractionTemplate.setSearchSubConceptTemplate(templateFileName);
                refOnt.reload();
            }
        } else if (e.getSource() == isAvailableCheckBox) {
            refOnt.setAvailable(isAvailableCheckBox.isSelected());
        }
    }
}
