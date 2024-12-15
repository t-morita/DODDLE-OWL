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

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class ConstructNounTreeAction extends AbstractAction {

    public ConstructNounTreeAction() {
        super(Translator.getTerm("ClassTreeConstructionAction"));
    }

    public void actionPerformed(ActionEvent e) {
        SwingWorker<String, String> worker = new SwingWorker<>() {
            public String doInBackground() {
                DODDLE_OWL.STATUS_BAR.initNormal(9);
                DODDLE_OWL.STATUS_BAR.startTime();
                DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("ClassTreeConstructionAction"));
                new ConstructTreeAction(false, DODDLE_OWL.getCurrentProject()).constructTree();
                DODDLE_OWL.getCurrentProject().addLog("ClassTreeConstructionAction");
                return "done";
            }
        };
        DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
