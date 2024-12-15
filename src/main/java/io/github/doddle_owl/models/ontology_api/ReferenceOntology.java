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

package io.github.doddle_owl.models.ontology_api;

import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.utils.OWLOntologyManager;
import io.github.doddle_owl.utils.Utils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class ReferenceOntology {

    public static Concept getConcept(String uri) {
        Resource res = ResourceFactory.createResource(uri);
        String id = Utils.getLocalName(res);
        String ns = Utils.getNameSpace(res);
        Concept c = OWLOntologyManager.getConcept(uri);

        if (c != null) {
            return c;
        }
        if (EDR.isEDRAvailable && ns.equals(DODDLEConstants.EDR_URI)) {
            return EDR.getEDRConcept(id);
        } else if (EDR.isEDRTAvailable && ns.equals(DODDLEConstants.EDRT_URI)) {
            return EDR.getEDRTConcept(id);
        } else if (WordNet.isAvailable && ns.equals(DODDLEConstants.WN_URI)) {
            return WordNet.getWNConcept(id);
        } else if (JaWordNet.isAvailable && ns.equals(DODDLEConstants.JPN_WN_URI)) {
            return JaWordNet.getConcept(id);
        }
        return null;
    }

    public static Set<String> getURISet(String word) {
        Set<String> uriSet = new HashSet<>();
        if (EDR.isEDRAvailable) {
            Set<String> idSet = EDR.getEDRIDSet(word);
            for (String id : idSet) {
                uriSet.add(DODDLEConstants.EDR_URI + id);
            }
        }
        if (EDR.isEDRTAvailable) {
            Set<String> idSet = EDR.getEDRTIDSet(word);
            for (String id : idSet) {
                uriSet.add(DODDLEConstants.EDRT_URI + id);
            }
        }
        uriSet.addAll(WordNet.getURISet(word));
        uriSet.addAll(JaWordNet.getSynsetSet(word));
        uriSet.addAll(OWLOntologyManager.getURISet(word));
        return uriSet;
    }
}
