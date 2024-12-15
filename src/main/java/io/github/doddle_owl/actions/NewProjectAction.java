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
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.DODDLEProjectPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class NewProjectAction extends AbstractAction {

    private final String title;

    public NewProjectAction(String title) {
        super(title, Utils.getImageIcon("baseline_create_black_18dp.png"));
        this.title = title;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    public String getTitle() {
        return title;
    }

    public void actionPerformed(ActionEvent e) {
        int messageType = JOptionPane.showConfirmDialog(DODDLE_OWL.rootPane, Translator.getTerm("SaveProjectMessage"),
                Translator.getTerm("NewProjectAction"), JOptionPane.YES_NO_CANCEL_OPTION);
        if (messageType != JOptionPane.CANCEL_OPTION) {
            if (messageType == JOptionPane.YES_OPTION) {
                DODDLEProjectPanel currentProject = DODDLE_OWL.getCurrentProject();
                if (currentProject == null) {
                    return;
                }
                File saveFile = DODDLE_OWL.getSaveProjectAsAction().getSaveFile();
                if (saveFile != null) {
                    DODDLE_OWL.getSaveProjectAsAction().saveProject(saveFile, currentProject);
                }
            }
            DODDLE_OWL.doddleProjectPanel.initProject();
        }
    }
}
