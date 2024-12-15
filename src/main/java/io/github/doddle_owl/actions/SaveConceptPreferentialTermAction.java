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
import java.util.Map;

/**
 * @author Takeshi Morita
 */
public class SaveConceptPreferentialTermAction extends AbstractAction {

    public SaveConceptPreferentialTermAction(String title) {
        super(title);
    }

    public void saveIDPreferentialTerm(DODDLEProjectPanel currentProject, File file) {
        ClassTreeConstructionPanel constructClassPanel = currentProject.getConstructClassPanel();
        PropertyTreeConstructionPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
        try {

            Map uriPreferentialTermMap = constructClassPanel.getIDPreferentialTermMap();
            uriPreferentialTermMap.putAll(constructPropertyPanel.getIDPreferentialTermMap());
            StringBuilder buf = new StringBuilder();
            for (Object o : uriPreferentialTermMap.keySet()) {
                String id = (String) o;
                String preferentialTerm = (String) uriPreferentialTermMap.get(id);
                buf.append(id);
                buf.append("\t");
                buf.append(preferentialTerm);
                buf.append(System.lineSeparator());
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
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
        saveIDPreferentialTerm(currentProject, chooser.getSelectedFile());
    }
}
