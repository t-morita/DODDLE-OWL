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

import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.LogConsole;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class ShowLogConsoleAction extends AbstractAction {

    private final String title;
    private final LogConsole logConsole;

    public ShowLogConsoleAction(String title, LogConsole console) {
        super(title, Utils.getImageIcon("ic_message_black_18dp.png"));
        this.title = title;
        logConsole = console;
    }

    public String getTitle() {
        return title;
    }

    public void actionPerformed(ActionEvent e) {
        logConsole.setVisible(true);
    }
}
