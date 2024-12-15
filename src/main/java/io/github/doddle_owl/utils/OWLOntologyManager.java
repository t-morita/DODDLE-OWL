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

package io.github.doddle_owl.utils;

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.reference_ontology_selection.ReferenceOWLOntology;
import io.github.doddle_owl.views.reference_ontology_selection.NameSpaceTable;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class OWLOntologyManager {

    private static final Map<String, ReferenceOWLOntology> refOntMap = new HashMap<>();

    public static void addRefOntology(String uri, ReferenceOWLOntology ontInfo) {
        refOntMap.put(uri, ontInfo);
    }

    public static void addRefOntology(File ontFile) {
        try {
            String uri = ontFile.getAbsolutePath();
            Model ontModel = Utils.getOntModel(new FileInputStream(ontFile), "---", "RDF/XML",
                    DODDLEConstants.BASE_URI);
            ReferenceOWLOntology refOnt = new ReferenceOWLOntology(ontModel, uri,
                    new NameSpaceTable());
            addRefOntology(uri, refOnt);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }

    public static ReferenceOWLOntology getRefOntology(String uri) {
        return refOntMap.get(uri);
    }

    public static void removeRefOntology(String uri) {
        refOntMap.remove(uri);
    }

    private static Collection<ReferenceOWLOntology> getRefOntologySet() {
        return refOntMap.values();
    }

    public static Set<List<Concept>> getPathToRootSet(String uri) {
        Set<List<Concept>> pathToRootSet = new HashSet<>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<List<Concept>> set = refOnt.getPathToRootSet(uri);
                if (set != null) {
                    pathToRootSet.addAll(set);
                }
            }
        }
        return pathToRootSet;
    }

    public static Set<List<String>> getURIPathToRootSet(String uri) {
        Set<List<String>> pathToRootSet = new HashSet<>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<List<String>> set = refOnt.getURIPathToRootSet(uri);
                if (set != null) {
                    pathToRootSet.addAll(set);
                }
            }
        }
        return pathToRootSet;
    }

    public static Set<Concept> getVerbConceptSet(Set<Concept> inputConceptSet) {
        Set<Concept> verbConceptSet = new HashSet<>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                for (String uri : refOnt.getPropertySet()) {
                    Concept c = refOnt.getConcept(uri);
                    if (inputConceptSet.contains(c)) {
                        verbConceptSet.add(c);
                    }
                }
            }
        }
        return verbConceptSet;
    }

    public static Concept getConcept(String uri) {
        Concept concept = null;
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                concept = refOnt.getConcept(uri);
            }
        }
        return concept;
    }

    public static Set<String> getDomainSet(Concept c, List<List<Concept>> trimmedConceptList) {
        Set<String> domainSet = new HashSet<>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                domainSet.addAll(refOnt.getDomainSet(c.getURI()));
                for (List<Concept> list : trimmedConceptList) {
                    for (Concept trimmedConcept : list) {
                        domainSet.addAll(refOnt.getDomainSet(trimmedConcept.getURI()));
                    }
                }
            }
        }
        return domainSet;
    }

    public static Set<String> getRangeSet(Concept c, List<List<Concept>> trimmedConceptList) {
        Set<String> rangeSet = new HashSet<>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                rangeSet.addAll(refOnt.getRangeSet(c.getURI()));
                for (List<Concept> list : trimmedConceptList) {
                    for (Concept trimmedConcept : list) {
                        rangeSet.addAll(refOnt.getRangeSet(trimmedConcept.getURI()));
                    }
                }
            }
        }
        return rangeSet;
    }

    public static Set<String> getSubURISet(String uri, Set<String> nounURISet) {
        Set<String> subURISet = new HashSet<>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<String> uriSet = new HashSet<>();
                refOnt.getSubURISet(uri, nounURISet, uriSet);
                subURISet.addAll(uriSet);
            }
        }
        return subURISet;
    }

    public static Set<String> getURISet(String word) {
        Set<String> uriSet = new HashSet<>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<String> set = refOnt.getURISet(word);
                if (set == null) {
                    continue;
                }
                uriSet.addAll(set);
            }
        }
        if (DODDLE_OWL.getCurrentProject().getOntologySelectionPanel().isJWOEnable()) {
            return uriSet;
        }
        Set<String> newURISet = new HashSet<>();
        for (String uri : uriSet) {
            if (!uri.contains(DODDLEConstants.JWO_URI)) {
                newURISet.add(uri);
            }
        }
        return newURISet;
    }

    public static void setOWLConceptSet(String word, Set<Concept> conceptSet) {
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<String> uriSet = refOnt.getURISet(word);
                if (uriSet == null) {
                    continue;
                }
                for (String uri : uriSet) {
                    if (!DODDLE_OWL.getCurrentProject().getOntologySelectionPanel().isJWOEnable()
                            && uri.contains(DODDLEConstants.JWO_URI)) {
                        // System.out.println("removed: " + uri);
                    } else {
                        conceptSet.add(refOnt.getConcept(uri));
                    }
                }
            }
        }
    }
}
