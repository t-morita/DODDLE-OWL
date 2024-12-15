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

package io.github.doddle_owl.models.concept_selection;

import io.github.doddle_owl.views.OptionDialog;

/**
 * @author Takeshi Morita
 */
public class TreeConstructionOption {

    private Concept c;
    private String option;
    private boolean isReplaceSubConcepts;

    public TreeConstructionOption(Concept c) {
        this.c = c;
        if (OptionDialog.isCompoundWordSetSameConcept()) {
            option = "SAME";
        } else {
            option = "SUB";
        }
    }

    public TreeConstructionOption(Concept c, String opt) {
        this.c = c;
        option = opt;
    }

    public void setConcept(Concept c) {
        this.c = c;
    }

    public Concept getConcept() {
        return c;
    }

    public void setOption(String opt) {
        option = opt;
    }

    public String getOption() {
        return option;
    }

    public boolean isReplaceSubConcepts() {
        return isReplaceSubConcepts;
    }

    public void setIsReplaceSubConcepts(boolean t) {
        isReplaceSubConcepts = t;
    }
    
    public String toString() {
        return "option: "+option+" concept: "+c+" is Replace Sub Concept: "+isReplaceSubConcepts;
    }
}
