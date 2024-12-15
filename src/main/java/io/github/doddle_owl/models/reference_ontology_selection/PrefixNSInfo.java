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

package io.github.doddle_owl.models.reference_ontology_selection;

/**
 * 
 * @author Takeshi Morita
 */
public class PrefixNSInfo {

    private final String prefix;
    private final String nameSpace;
    private final boolean isAvailable;

    public PrefixNSInfo(String p, String ns, boolean t) {
        prefix = p;
        nameSpace = ns;
        isAvailable = t;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String toString() {
        return "prefix: " + prefix + " | NameSpace: " + nameSpace + " | available: " + isAvailable;
    }
}
