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

import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.VersionInfoDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

/**
 * @author Takeshi Morita
 */
public class ShowVersionInfoAction extends AbstractAction {

    private final Frame rootFrame;
    private WeakReference<VersionInfoDialog> versionInfoDialogRef;
    private static final String TITLE = Translator.getTerm("VersionMenu");
    private static final ImageIcon ICON = Utils.getImageIcon("baseline_help_black_18dp.png");

    public ShowVersionInfoAction(Frame frame) {
        super(TITLE, ICON);
        rootFrame = frame;
        versionInfoDialogRef = new WeakReference<>(null);
        setValues();
    }

    public String getTitle() {
        return TITLE;
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
    }

    private VersionInfoDialog getVersionInfoDialog() {
        VersionInfoDialog result = versionInfoDialogRef.get();
        if (result == null) {
            result = new VersionInfoDialog(rootFrame, TITLE, ICON);
            versionInfoDialogRef = new WeakReference<>(result);
        }
        return result;
    }

    public void actionPerformed(ActionEvent e) {
        getVersionInfoDialog().setVisible(true);
    }
}
