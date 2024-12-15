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

package io.github.doddle_owl.views.common;

import io.github.doddle_owl.utils.Translator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Takeshi Morita
 */
public class UndefinedTermListPanel extends JPanel implements ActionListener {

    public static boolean isDragUndefinedList = false;

    private final JButton searchButton;
    private final JTextField searchField;

    private final JTextField addWordField;
    private final JButton addButton;
    private final JButton removeButton;

    private ListModel undefinedTermListModel;
    private final JList undefinedTermJList;
    private final TitledBorder undefinedTermJListTitle;

    public UndefinedTermListPanel() {
        searchButton = new JButton(Translator.getTerm("SearchButton"));
        searchButton.addActionListener(this);
        searchField = new JTextField(30);
        searchField.addActionListener(this);
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        undefinedTermListModel = new DefaultListModel();
        undefinedTermJList = new JList(undefinedTermListModel);
        JScrollPane unefinedWordJListScroll = new JScrollPane(undefinedTermJList);
        undefinedTermJListTitle = BorderFactory.createTitledBorder(Translator.getTerm("UndefinedTermList"));
        unefinedWordJListScroll.setBorder(undefinedTermJListTitle);
        undefinedTermJList.setDragEnabled(true);
        new DropTarget(undefinedTermJList, new UndefinedTermJListDropTargetAdapter());

        addWordField = new JTextField(30);
        addWordField.addActionListener(this);
        addButton = new JButton(Translator.getTerm("AddButton"));
        addButton.addActionListener(this);
        removeButton = new JButton(Translator.getTerm("RemoveButton"));
        removeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        JPanel addRemovePanel = new JPanel();
        addRemovePanel.setLayout(new BorderLayout());
        addRemovePanel.add(addWordField, BorderLayout.CENTER);
        addRemovePanel.add(buttonPanel, BorderLayout.EAST);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(addRemovePanel, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(unefinedWordJListScroll, BorderLayout.CENTER);
        setBorder(BorderFactory.createTitledBorder(Translator.getTerm("UndefinedTermListPanel")));
    }

    class UndefinedTermJListDropTargetAdapter extends DropTargetAdapter {

        public void dragEnter(DropTargetDragEvent dtde) {
            isDragUndefinedList = true;
        }

        public void drop(DropTargetDropEvent dtde) {
        }
    }

    public List getSelectedValuesList() {
        return undefinedTermJList.getSelectedValuesList();
    }

    public DefaultListModel getModel() {
        return (DefaultListModel) undefinedTermListModel;
    }

    public DefaultListModel getViewModel() {
        if (undefinedTermJList.getModel() == undefinedTermListModel) {
            return null;
        }
        return (DefaultListModel) undefinedTermJList.getModel();
    }

    public String getSearchRegex() {
        return searchField.getText();
    }

    public void clearSelection() {
        undefinedTermJList.clearSelection();
    }

    private void setTitle() {
        undefinedTermJListTitle.setTitle(Translator.getTerm("UndefinedTermList"));
    }

    public void setTitleWithSize() {
        undefinedTermJListTitle.setTitle(Translator.getTerm("UndefinedTermList") + " （"
                + undefinedTermJList.getModel().getSize() + "/" + getModel().getSize() + "）");
        repaint();
    }

    public void setUndefinedTermListModel(ListModel model) {
        undefinedTermListModel = model;
        undefinedTermJList.setModel(model);
        setTitle();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchField || e.getSource() == searchButton) {
            String searchRegex = searchField.getText();
            if (searchRegex.length() == 0) {
                undefinedTermJList.setModel(undefinedTermListModel);
            } else {
                DefaultListModel searchListModel = new DefaultListModel();
                for (int i = 0; i < undefinedTermListModel.getSize(); i++) {
                    String word = (String) undefinedTermListModel.getElementAt(i);
                    if (word.matches(searchRegex)) {
                        searchListModel.addElement(word);
                    }
                }
                undefinedTermJList.setModel(searchListModel);
            }
            setTitleWithSize();
        } else if (e.getSource() == addButton || e.getSource() == addWordField) {
            String addWord = addWordField.getText();
            if (0 < addWord.length()) {
                for (int i = 0; i < undefinedTermListModel.getSize(); i++) {
                    String word = (String) undefinedTermListModel.getElementAt(i);
                    if (word.equals(addWord)) {
                        return;
                    }
                }
                ((DefaultListModel) undefinedTermListModel).addElement(addWord);
                if (getViewModel() != null && addWord.matches(getSearchRegex())) {
                    getViewModel().addElement(addWord);
                }
                setTitleWithSize();
                addWordField.setText("");
            }
        } else if (e.getSource() == removeButton) {
            DefaultListModel model = (DefaultListModel) undefinedTermJList.getModel();
            List removeWordList = undefinedTermJList.getSelectedValuesList();
            for (Object word : removeWordList) {
                model.removeElement(word);
                ((DefaultListModel) undefinedTermListModel).removeElement(word);
            }
            setTitleWithSize();
        }
    }
}
