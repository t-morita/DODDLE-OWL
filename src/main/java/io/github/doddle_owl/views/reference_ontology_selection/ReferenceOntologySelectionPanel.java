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
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.utils.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class ReferenceOntologySelectionPanel extends JPanel implements ActionListener {

    private final NameSpaceTable nsTable;

    private SwoogleWebServiceWrapperPanel swoogleWebServiceWrapperPanel;
    private final GeneralOntologySelectionPanel generalOntologySelectionPanel;
    private final OWLOntologySelectionPanel owlOntologySelectionPanel;

    public void initialize() {
        swoogleWebServiceWrapperPanel.initialize();
        generalOntologySelectionPanel.initialize();
        owlOntologySelectionPanel.initialize();
    }

    public ReferenceOntologySelectionPanel() {
        nsTable = new NameSpaceTable();
        owlOntologySelectionPanel = new OWLOntologySelectionPanel(nsTable);
        generalOntologySelectionPanel = new GeneralOntologySelectionPanel(nsTable);
        if (DODDLEConstants.IS_INTEGRATING_SWOOGLE) {
            swoogleWebServiceWrapperPanel = new SwoogleWebServiceWrapperPanel(nsTable,
                    owlOntologySelectionPanel);
        }

        JButton nextTabButton = new JButton(Translator.getTerm("DocumentSelectionPanel"));
        nextTabButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(nextTabButton, BorderLayout.EAST);

        var mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab(Translator.getTerm("GenericOntologySelectionPanel"), null, generalOntologySelectionPanel);
        mainTabbedPane.addTab(Translator.getTerm("NameSpaceTable"), null, nsTable);
        mainTabbedPane.addTab(Translator.getTerm("OWLOntologySelectionPanel"), null,
                owlOntologySelectionPanel);
        if (DODDLEConstants.IS_INTEGRATING_SWOOGLE) {
            mainTabbedPane.addTab(Translator.getTerm("SwoogleWebServiceWrapperPanel"), null,
                    swoogleWebServiceWrapperPanel);
        } else {
            mainTabbedPane.addTab(Translator.getTerm("SwoogleWebServiceWrapperPanel"), null,
                    new JPanel());
        }

        setLayout(new BorderLayout());
        add(mainTabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public NameSpaceTable getNSTable() {
        return nsTable;
    }

    public String getPrefix(String ns) {
        return nsTable.getPrefix(ns);
    }

    public void actionPerformed(ActionEvent e) {
        DODDLE_OWL.setSelectedIndex(DODDLEConstants.INPUT_DOCUMENT_SELECTION_PANEL);
    }

    public void saveOWLMetaDataSet(File saveDir) {
        owlOntologySelectionPanel.saveOWLMetaDataSet(saveDir);
    }

    public void loadOWLMetaDataSet(File loadDir) {
        owlOntologySelectionPanel.loadOWLMetaDataSet(loadDir);
    }

    public void saveGeneralOntologyInfo(File saveFile) {
        generalOntologySelectionPanel.saveGeneralOntologyInfo(saveFile);
    }

    public void loadGeneralOntologyInfo(File loadFile) {
        generalOntologySelectionPanel.loadGeneralOntologyInfo(loadFile);
    }

    public String getEnableDicList() {
        return generalOntologySelectionPanel.getEnableDicList();
    }

    public boolean isEDREnable() {
        return generalOntologySelectionPanel.isEDREnable();
    }

    public boolean isEDRTEnable() {
        return generalOntologySelectionPanel.isEDRTEnable();
    }

    public boolean isWordNetEnable() {
        return generalOntologySelectionPanel.isWordNetEnable();
    }

    public boolean isJpnWordNetEnable() {
        return generalOntologySelectionPanel.isJpnWordNetEnable();
    }

    public boolean isJWOEnable() {
        return generalOntologySelectionPanel.isJWOEnable();
    }

    public void resetGeneralOntologiesCheckBoxes() {
        generalOntologySelectionPanel.resetCheckBoxes();
    }

}
