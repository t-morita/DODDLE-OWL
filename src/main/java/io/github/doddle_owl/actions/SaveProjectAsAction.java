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
import io.github.doddle_owl.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class SaveProjectAsAction extends SaveProjectAction {

    public SaveProjectAsAction(String title, DODDLE_OWL ddl) {
        super(title, Utils.getImageIcon("baseline_save_alt_black_18dp.png"), ddl);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() + KeyEvent.SHIFT_DOWN_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProjectPanel currentProject = DODDLE_OWL.getCurrentProject();
        if (currentProject == null) {
            return;
        }
        File saveFile = getSaveFile();
        if (saveFile != null) {
            saveProject(saveFile, currentProject);
        }
    }
}
