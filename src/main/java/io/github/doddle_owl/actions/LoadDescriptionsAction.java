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

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Takeshi Morita
 */
public class LoadDescriptionsAction extends AbstractAction {

    public LoadDescriptionsAction(String title) {
        super(title);
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        ClassTreeConstructionPanel classPanel = DODDLE_OWL.getCurrentProject().getConstructClassPanel();
        PropertyTreeConstructionPanel propertyPanel = DODDLE_OWL.getCurrentProject().getConstructPropertyPanel();

        Map<String, DODDLELiteral> classWordDescriptionMap = new HashMap<>();
        Map<String, DODDLELiteral> propertyWordDescriptionMap = new HashMap<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(chooser.getSelectedFile()), StandardCharsets.UTF_8));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] lines = line.split("\t");
                if (lines.length == 4) {
                    String type = lines[0];
                    String lang = lines[1];
                    String word = lines[2];
                    String description = lines[3];
                    DODDLELiteral descriptionLiteral = new DODDLELiteral(lang, description);
                    if (type.equals("class")) {
                        classWordDescriptionMap.put(word, descriptionLiteral);
                    } else if (type.equals("property")) {
                        propertyWordDescriptionMap.put(word, descriptionLiteral);                        
                    }
                }
            }
        } catch (IOException uee) {
            uee.printStackTrace();
        }
        classPanel.loadDescriptions(classWordDescriptionMap);
        propertyPanel.loadDescriptions(propertyWordDescriptionMap);
        DODDLE_OWL.STATUS_BAR.setText("Load Descriptions Done");
    }

}
