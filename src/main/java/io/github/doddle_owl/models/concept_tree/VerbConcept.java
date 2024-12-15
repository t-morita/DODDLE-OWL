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

package io.github.doddle_owl.models.concept_tree;

import io.github.doddle_owl.models.concept_selection.Concept;

import java.util.*;

/**
 * @author Takeshi Morita
 */
public class VerbConcept extends Concept {

    private final Set<String> domainSet;
    private final Set<String> rangeSet;

    public VerbConcept(Concept c) {
        super(c);
        domainSet = new TreeSet<>();
        rangeSet = new TreeSet<>();
    }

    public VerbConcept(String uri, String concept) {
        super(uri, concept);
        domainSet = new TreeSet<>();
        rangeSet = new TreeSet<>();
    }

    public void addAllDomain(Set<String> set) {
        domainSet.addAll(set);
    }

    public void addAllRange(Set<String> set) {
        rangeSet.addAll(set);
    }

    public void addDomain(String id) {
        domainSet.add(id);
    }

    public Set<String> getDomainSet() {
        return domainSet;
    }

    public Set<String> getRangeSet() {
        return rangeSet;
    }

    public void addRange(String id) {
        rangeSet.add(id);
    }

    public void deleteDomain(String id) {
        // System.out.println("delete domain");
        domainSet.remove(id);
    }

    public void deleteRange(String id) {
        // System.out.println("delete range");
        rangeSet.remove(id);
    }
}
