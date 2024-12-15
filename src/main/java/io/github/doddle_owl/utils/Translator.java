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

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import io.github.doddle_owl.models.common.DODDLEConstants;

import java.io.InputStream;
import java.util.*;

/*
 *
 * @author Takeshi Morita
 *
 */
public class Translator {

    private static ResourceBundle resourceBundle;
    private static Map<String, String> uriTermMap;
    private static Map<String, String> uriDescriptionMap;

    public static String getString(String sKey) {
        try {
            return resourceBundle.getString(sKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED";
        }
    }

    static {
        Set<Locale> systemLocaleSet = new HashSet<>();
        systemLocaleSet.add(Locale.JAPAN);
        systemLocaleSet.add(Locale.ENGLISH);
        systemLocaleSet.add(Locale.CHINA);
    }

    public static void loadDODDLEComponentOntology(String lang) {
        uriTermMap = new HashMap<>();
        uriDescriptionMap = new HashMap<>();
        Model ontModel = ModelFactory.createDefaultModel();
        InputStream ins = Utils.class.getClassLoader().getResourceAsStream("doddle_components.ttl");
        ontModel.read(ins, DODDLEConstants.BASE_URI, "TURTLE");

        for (Resource res : ontModel.listSubjectsWithProperty(RDF.type, OWL.Class).toList()) {
            for (Statement stmt : res.listProperties(RDFS.label).toList()) {
                Literal label = (Literal) stmt.getObject();
                if (label.getLanguage().equals(lang)) {
                    uriTermMap.put(res.getURI(), label.getString());
                }
            }
            for (Statement stmt : res.listProperties(RDFS.comment).toList()) {
                Literal description = (Literal) stmt.getObject();
                if (description != null && description.getLanguage().equals(lang)) {
                    uriDescriptionMap.put(res.getURI(), description.getString());
                }
            }
        }
        ontModel.close();
    }

    public static String getTerm(String key) {
        return uriTermMap.get(DODDLEConstants.DODDLE_URI + key); // BASE_URIは変化することがあるため，DODDLE_URIを用いる
    }

    public static String getDescription(String key) {
        return uriDescriptionMap.get(DODDLEConstants.DODDLE_URI + key); // BASE_URIは変化することがあるため，DODDLE_URIを用いる
    }

    public static void main(String[] args) {
        Translator.loadDODDLEComponentOntology("ja");
        System.out.println(Translator.getTerm("PropertyTreeConstructionPanel"));
        System.out.println(Translator.getTerm("RangeLabel"));
        System.out.println(Translator.getTerm("RemoveCorrectConceptPairButton"));
    }
}
