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

package io.github.doddle_owl.actions;

import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Takeshi Morita
 */
public class LoadConceptPreferentialTermAction extends AbstractAction {

    public LoadConceptPreferentialTermAction(String title) {
        super(title);
    }

    public void loadIDPreferentialTerm(DODDLEProjectPanel currentProject, File file) {
        ClassTreeConstructionPanel constructClassPanel = currentProject.getConstructClassPanel();
        PropertyTreeConstructionPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        if (!file.exists()) {
            return;
        }
        try {
            Map<String, String> idPreferentialTermMap = new HashMap<>();
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] idInputWord = line.replaceAll(System.lineSeparator(), "").split("\t");
                    if (idInputWord.length == 2) {
                        idPreferentialTermMap.put(idInputWord[0], idInputWord[1]);
                    }
                }
            }
            constructClassPanel.loadIDPreferentialTerm(idPreferentialTermMap);
            constructPropertyPanel.loadIDPreferentialTerm(idPreferentialTermMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) {
            return;
        }
        DODDLEProjectPanel currentProject = DODDLE_OWL.getCurrentProject();
        loadIDPreferentialTerm(currentProject, chooser.getSelectedFile());
    }
}
