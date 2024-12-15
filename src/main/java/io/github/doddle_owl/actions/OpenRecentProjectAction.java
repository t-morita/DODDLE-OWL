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

import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class OpenRecentProjectAction extends OpenProjectAction {

    private final File projectFile;

    public OpenRecentProjectAction(String project, DODDLE_OWL ddl) {
        this.title = Translator.getTerm("OpenProjectAction");
        projectFile = new File(project);
        doddle = ddl;
    }

    public void actionPerformed(ActionEvent e) {
        openFile = projectFile;
        DODDLE_OWL.doddleProjectPanel.initProject();
        OpenProjectWorker worker = new OpenProjectWorker(11);
        DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
