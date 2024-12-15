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
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class LiteralPanel extends JPanel implements ListSelectionListener {

    protected final JList langJList;
    protected final JList literalJList;
    protected DefaultListModel literalJListModel;

    protected Concept selectedConcept;

    private final String literalType;

    private static final int LANG_SIZE = 80;
    public static final String LABEL = "LABEL";
    public static final String DESCRIPTION = "DESCRIPTION";

    public LiteralPanel(String langTitle, String literalTitle, String type) {
        literalType = type;
        langJList = new JList();
        langJList.addListSelectionListener(this);
        JScrollPane langJListScroll = new JScrollPane(langJList);
        langJListScroll.setPreferredSize(new Dimension(LANG_SIZE, 10));
        langJListScroll.setMinimumSize(new Dimension(LANG_SIZE, 10));
        langJListScroll.setBorder(BorderFactory.createTitledBorder(langTitle));
        literalJList = new JList();
        literalJList.addListSelectionListener(this);
        JScrollPane literalJListScroll = new JScrollPane(literalJList);
        literalJListScroll.setBorder(BorderFactory.createTitledBorder(literalTitle));

        setLayout(new BorderLayout());
        JPanel langAndLabelPanel = new JPanel();
        langAndLabelPanel.setLayout(new BorderLayout());
        langAndLabelPanel.add(langJListScroll, BorderLayout.WEST);
        langAndLabelPanel.add(literalJListScroll, BorderLayout.CENTER);
        add(langAndLabelPanel, BorderLayout.CENTER);
    }

    public void clearData() {
        langJList.setListData(new Object[0]);
        literalJList.setListData(new Object[0]);
    }

    private void setLangList(Set<String> langSet) {
        langJList.setListData(langSet.toArray());
        if (langSet.size() == 0) {
            return;
        }
        langJList.setSelectedValue(DODDLEConstants.LANG, true);
        if (langJList.getSelectedValue() == null) {
            langJList.setSelectedIndex(0);
        }
    }

    public void setSelectedConcept(Concept c) {
        selectedConcept = c;
    }

    public void setLabelLangList() {
        setLangList(selectedConcept.getLangLabelListMap().keySet());
    }

    public void setDescriptionLangList() {
        setLangList(selectedConcept.getLangDescriptionListMap().keySet());
    }

    public void setDescriptionList() {
        DefaultListModel listModel = new DefaultListModel();
        List langList = langJList.getSelectedValuesList();
        Map<String, List<DODDLELiteral>> langDescriptionListMap = selectedConcept.getLangDescriptionListMap();
        for (Object lang : langList) {
            if (langDescriptionListMap.get(lang) != null) {
                for (DODDLELiteral description : langDescriptionListMap.get(lang)) {
                    listModel.addElement(description);
                }
            }
        }
        literalJList.setModel(listModel);
    }

    public void setLabelList() {
        DefaultListModel listModel = new DefaultListModel();
        List langList = langJList.getSelectedValuesList();
        Map<String, List<DODDLELiteral>> langLabelListMap = selectedConcept.getLangLabelListMap();
        for (Object lang : langList) {
            if (langLabelListMap.get(lang) != null) {
                for (DODDLELiteral label : langLabelListMap.get(lang)) {
                    if (0 < label.getString().length()) {
                        listModel.addElement(label);
                    }
                }
            }
        }
        literalJList.setModel(listModel);
    }

    protected void setField() {
    }

    public void valueChanged(ListSelectionEvent e) {
        if (literalType.equals(LABEL)) {
            if (e.getSource() == langJList) {
                setLabelList();
            } else if (e.getSource() == literalJList) {
                setField();
            }
        } else if (literalType.equals(DESCRIPTION)) {
            if (e.getSource() == langJList) {
                setDescriptionList();
            }
        }
    }
}
