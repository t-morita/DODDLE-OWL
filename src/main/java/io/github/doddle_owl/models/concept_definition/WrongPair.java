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

package io.github.doddle_owl.models.concept_definition;

/**
 * @author Takeshi Morita
 */
public class WrongPair {
    private final String domain;
    private final String range;

    public WrongPair(String d, String r) {
        domain = d;
        range = r;
    }

    @Override
    public boolean equals(Object obj) {
        WrongPair wp = (WrongPair) obj;
        return wp.getDomain().equals(domain) && wp.getRange().equals(range);
    }

    @Override
    public int hashCode() {
        return domain.hashCode() + range.hashCode();
    }

    public String getDomain() {
        return domain;
    }

    public String getRange() {
        return range;
    }
}
