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

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.concept_selection.Concept;

import java.util.*;

/**
 * @author Takeshi Morita
 */
public class SwoogleWebServiceData {

    private final Map<String, Double> swtTermRankMap;

    private final Set<Resource> classSet;
    private final Set<Resource> propertySet;
    private final Set<Resource> relatedPropertySet;
    private final Set<Resource> expandClassSet;
    private final Map<Resource, Set<Resource>> propertyDomainSetMap;
    private final Map<Resource, Set<Resource>> propertyRangeSetMap;

    private final Map<Resource, Set<String>> conceptInputWordSetMap;

    private final Map<String, SwoogleOWLMetaData> uriSwoogleOWLMetaDataMap;
    private static Map<String, ReferenceOWLOntology> uriRefOntologyMap;

    public SwoogleWebServiceData() {
        swtTermRankMap = new HashMap<>();
        classSet = new HashSet<>();
        expandClassSet = new HashSet<>();
        propertySet = new HashSet<>();
        relatedPropertySet = new HashSet<>();
        propertyDomainSetMap = new HashMap<>();
        propertyRangeSetMap = new HashMap<>();
        uriSwoogleOWLMetaDataMap = new HashMap<>();
        uriRefOntologyMap = new HashMap<>();
        conceptInputWordSetMap = new HashMap<>();
    }

    public void initData() {
        swtTermRankMap.clear();
        classSet.clear();
        expandClassSet.clear();
        propertySet.clear();
        relatedPropertySet.clear();
        propertyDomainSetMap.clear();
        propertyRangeSetMap.clear();
        uriRefOntologyMap.clear();
        conceptInputWordSetMap.clear();
    }

    public void putTermRank(String uri, double rank) {
        swtTermRankMap.put(uri, rank);
    }

    public Double getTermRank(String uri) {
        return swtTermRankMap.get(uri);
    }

    public void putRefOntology(String uri, ReferenceOWLOntology refOntology) {
        if (isRelatedOntology(refOntology)) {
            uriRefOntologyMap.put(uri, refOntology);
            DODDLE_OWL.getLogger().info("Regist Ontology: " + uri);
        } else {
            DODDLE_OWL.getLogger().info("Unnecessary Ontology: " + uri);
        }
    }

    public ReferenceOWLOntology getRefOntology(String uri) {
        return uriRefOntologyMap.get(uri);
    }

    public Set<String> getRefOntologyURISet() {
        return uriRefOntologyMap.keySet();
    }

    public Collection<ReferenceOWLOntology> getRefOntologies() {
        return uriRefOntologyMap.values();
    }

    public void putSwoogleOWLMetaData(String uri, SwoogleOWLMetaData data) {
        uriSwoogleOWLMetaDataMap.put(uri, data);
    }

    public SwoogleOWLMetaData getSwoogleOWLMetaData(String uri) {
        return uriSwoogleOWLMetaDataMap.get(uri);
    }

    public void addClass(String inputWord, Resource cls) {
        classSet.add(cls);
        if (conceptInputWordSetMap.get(cls) != null) {
            Set<String> inputWordSet = conceptInputWordSetMap.get(cls);
            inputWordSet.add(inputWord);
        } else {
            Set<String> inputWordSet = new HashSet<>();
            inputWordSet.add(inputWord);
            conceptInputWordSetMap.put(cls, inputWordSet);
        }
    }

    public Set<Resource> getClassSet() {
        return classSet;
    }

    public void addProperty(Resource property) {
        propertySet.add(property);
    }

    public void addRelatedProperty(Resource property) {
        relatedPropertySet.add(property);
    }

    public Set<Resource> getPropertySet() {
        return propertySet;
    }

    public Set<Resource> getRelatedPropertySet() {
        return relatedPropertySet;
    }

    public Set<Resource> getAllProperty() {
        Set<Resource> allPropertySet = new HashSet<>();
        allPropertySet.addAll(propertySet);
        allPropertySet.addAll(relatedPropertySet);
        return allPropertySet;
    }

    public Set<Resource> getConceptSet() {
        Set<Resource> conceptSet = new HashSet<>();
        conceptSet.addAll(classSet);
        conceptSet.addAll(propertySet);
        conceptSet.addAll(relatedPropertySet);
        return conceptSet;
    }

    private void addPropertyRegion(Map<Resource, Set<Resource>> propertyRegionSetMap, Resource property, Resource region) {
        if (propertyRegionSetMap.get(property) != null) {
            Set<Resource> regionSet = propertyRegionSetMap.get(property);
            regionSet.add(region);
        } else {
            Set<Resource> regionSet = new HashSet<>();
            regionSet.add(region);
            propertyRegionSetMap.put(property, regionSet);
        }
    }

    public void addPropertyDomain(Resource property, Resource domain) {
        addPropertyRegion(propertyDomainSetMap, property, domain);
    }

    public void addPropertyRange(Resource property, Resource range) {
        addPropertyRegion(propertyRangeSetMap, property, range);
    }

    private Set<Resource> getExpandClassSet(Resource cls) {
        Set<Resource> expandClassSet = new HashSet<>();
        for (String uri : uriRefOntologyMap.keySet()) {
            ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
            Set<List<Concept>> pathToRoot = refOnto.getPathToRootSet(cls.getURI());
            for (List<Concept> clist : pathToRoot) {
                for (Concept c : clist) {
                    expandClassSet.add(c.getResource());
                    Set<String> inputWordSet = conceptInputWordSetMap.get(cls);
                    if (conceptInputWordSetMap.get(c.getResource()) != null) {
                        Set<String> extInputWordSet = conceptInputWordSetMap.get(c.getResource());
                        extInputWordSet.addAll(inputWordSet);
                    } else {
                        Set<String> extInputWordSet = new HashSet<>(inputWordSet);
                        conceptInputWordSetMap.put(c.getResource(), extInputWordSet);
                    }
                }
            }
        }
        return expandClassSet;
    }

    private void setExpandClassSet() {
        for (Resource cls : classSet) {
            expandClassSet.addAll(getExpandClassSet(cls));
        }
    }

    private void removeUnnecessaryRegionSet(Map<Resource, Set<Resource>> propertyRegionSetMap) {
        for (Resource property : propertyRegionSetMap.keySet()) {
            Set<Resource> unnecessaryRegionSet = new HashSet<>();
            Set<Resource> regionSet = propertyRegionSetMap.get(property);
            for (Resource region : regionSet) {
                if (!expandClassSet.contains(region)) {
                    unnecessaryRegionSet.add(region);
                }
            }
            regionSet.removeAll(unnecessaryRegionSet);
        }
    }

    /**
     * 定義域と値域の両方が定義されているプロパティを獲得
     *
     * @param property
     */
    private boolean isNecessaryProperty(Resource property) {
        return (getDomainSet(property) != null && getRangeSet(property) != null);
    }

    /**
     * 定義域と値域の両方に入力単語に関連するクラスが定義されているプロパティをpropertySetに追加
     */
    public void addNecessaryPropertySet() {
        for (Resource property : relatedPropertySet) {
            if (isNecessaryProperty(property)) {
                propertySet.add(property);
            }
        }
    }

    /**
     * 継承している定義域と値域を追加
     */
    public void addInheritedRegionSet() {
        for (Resource property : relatedPropertySet) {
            for (String uri : uriRefOntologyMap.keySet()) {
                ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
                Set<String> refOntDomainSet = refOnto.getDomainSet(property.getURI());
                for (String domain : refOntDomainSet) {
                    addPropertyDomain(property, ResourceFactory.createResource(domain));
                }
                Set<String> refOntRangeSet = refOnto.getRangeSet(property.getURI());
                for (String range : refOntRangeSet) {
                    addPropertyRange(property, ResourceFactory.createResource(range));
                }
            }
        }
    }

    /**
     * 入力概念または入力概念の上位概念以外の定義域と値域は削除する
     */
    public void removeUnnecessaryRegionSet() {
        setExpandClassSet();
        removeUnnecessaryRegionSet(propertyDomainSetMap);
        removeUnnecessaryRegionSet(propertyRangeSetMap);
    }

    /**
     * propertyの定義域を返す
     *
     * @param property
     */
    private Set<Resource> getDomainSet(Resource property) {
        return propertyDomainSetMap.get(property);
    }

    /**
     * propertyの値域を返す
     *
     * @param property
     */
    private Set<Resource> getRangeSet(Resource property) {
        return propertyRangeSetMap.get(property);
    }

    /**
     * クラス，プロパティ，関連プロパティのいずれかを含むオントロジーを関連オントロジーとする
     */
    private boolean isRelatedOntology(ReferenceOWLOntology refOnto) {
        Set<Resource> conceptSet = getConceptSet();
        for (Resource concept : conceptSet) {
            if (refOnto.getConcept(concept.getURI()) != null) {
                return true;
            }
        }
        return false;
    }

    public int getAllRelationCount() {
        int relCnt = 0;
        for (Resource property : getAllProperty()) {
            if (propertyDomainSetMap.get(property) != null && propertyRangeSetMap.get(property) != null) {
                int cnt = propertyDomainSetMap.get(property).size() * propertyRangeSetMap.get(property).size();
                relCnt += cnt;
            }
        }
        return relCnt;
    }

    public int getValidRelationCount() {
        int relCnt = 0;
        Map<String, Set<Resource>> pairPropertySetMap = new HashMap<>();
        Map<String, Set<Resource>> extPairPropertySetMap = new HashMap<>();
        for (Resource property : propertySet) {
            if (propertyDomainSetMap.get(property) != null && propertyRangeSetMap.get(property) != null) {
                int cnt = propertyDomainSetMap.get(property).size() * propertyRangeSetMap.get(property).size();
                relCnt += cnt;
                for (Resource domain : propertyDomainSetMap.get(property)) {
                    for (Resource range : propertyRangeSetMap.get(property)) {
                        for (String domainInputWord : conceptInputWordSetMap.get(domain)) {
                            for (String rangeInputWord : conceptInputWordSetMap.get(range)) {
                                String pair = domainInputWord + "---" + rangeInputWord;
                                if (classSet.contains(domain) && classSet.contains(range)) {
                                    if (pairPropertySetMap.get(pair) != null) {
                                        Set<Resource> propertySet = pairPropertySetMap.get(pair);
                                        propertySet.add(property);
                                    } else {
                                        Set<Resource> propertySet = new HashSet<>();
                                        propertySet.add(property);
                                        pairPropertySetMap.put(pair, propertySet);
                                    }
                                } else {
                                    if (extPairPropertySetMap.get(pair) != null) {
                                        Set<Resource> propertySet = extPairPropertySetMap.get(pair);
                                        propertySet.add(property);
                                    } else {
                                        Set<Resource> propertySet = new HashSet<>();
                                        propertySet.add(property);
                                        extPairPropertySetMap.put(pair, propertySet);
                                    }
                                }
                            }
                        }
                        DODDLE_OWL.getLogger().info(domain + " => " + property + " => " + range);
                    }
                }
            }
        }

        for (String pair : pairPropertySetMap.keySet()) {
            Set<Resource> propertySet = pairPropertySetMap.get(pair);
            DODDLE_OWL.getLogger().info("厳密照合: " + pair + ": " + propertySet.size());
            //DODDLE.getLogger().info( "厳密照合: " + propertySet);
        }
        for (String pair : extPairPropertySetMap.keySet()) {
            Set<Resource> propertySet = extPairPropertySetMap.get(pair);
            DODDLE_OWL.getLogger().info("拡張照合: " + pair + ": " + propertySet.size());
            //DODDLE.getLogger().info( "拡張照合: " + propertySet);
        }
        return relCnt;
    }

    public int getDefinedRelationCount() {
        Set<String> relSet = new HashSet<>();
        for (String uri : uriRefOntologyMap.keySet()) {
            ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
            // System.out.println("onturi: "+uri);
            // System.out.println("refontpropertyset:
            // "+refOnto.getPropertySet());
            for (String property : refOnto.getPropertySet()) {
                Resource propRes = ResourceFactory.createResource(property);
                Set<Resource> domainSet = propertyDomainSetMap.get(propRes);
                Set<Resource> rangeSet = propertyRangeSetMap.get(propRes);
                if (propertySet.contains(propRes) && domainSet != null && rangeSet != null) {
                    // System.out.println("property: "+property);
                    // System.out.println("refdomain:
                    // "+refOnto.getDomainSet(property));
                    // System.out.println("refrange:
                    // "+refOnto.getRangeSet(property));
                    for (String domain : refOnto.getDomainSet(property)) {
                        Resource domainRes = ResourceFactory.createResource(domain);
                        if (domainSet.contains(domainRes)) {
                            for (String range : refOnto.getRangeSet(property)) {
                                Resource rangeRes = ResourceFactory.createResource(range);
                                if (rangeSet.contains(rangeRes)) {
                                    relSet.add("defined:\t" + domain + "\t" + property + "\t" + range);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String rel : relSet) {
            DODDLE_OWL.getLogger().info(rel);
        }
        return relSet.size();
    }

    private void countDefinedConcept(String type) {
        Set<Resource> conceptSet = null;
        if (type.equals("class")) {
            conceptSet = classSet;
        } else if (type.equals("property")) {
            conceptSet = propertySet;
        }
        Set<Resource> validConceptSet = new HashSet<>();
        for (Resource concept : conceptSet) {
            Set<String> refOntoURISet = uriRefOntologyMap.keySet();
            for (String uri : refOntoURISet) {
                ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
//                System.out.println("concept: "+concept.getURI());
//                System.out.println("uri: "+uri);
//                System.out.println("refonto:"+refOnto.getURI());
//                System.out.println("clssize: "+refOnto.getClassSet().size());
                if (refOnto.getConcept(concept.getURI()) != null) {
                    validConceptSet.add(concept);
                    DODDLE_OWL.getLogger().info(type + ": " + concept + " => " + "ontology_api: " + uri);
                    break;
                }
            }
        }
        DODDLE_OWL.getLogger().info("Defined " + type + " Set size: " + validConceptSet.size());
    }

    public void countDefinedConcept() {
        countDefinedConcept("class");
        countDefinedConcept("property");
    }

    /**
     * inputWordRatio, relationCountを計算する
     */
    public void calcOntologyRank(Set<String> inputWordSet) {
        Set<String> unnecessaryOntologyURISet = new HashSet<>();
        for (String uri : uriRefOntologyMap.keySet()) {
            ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
            double inputConceptCnt = 0;
            for (String inputWord : inputWordSet) {
                if (refOnto.getURISet(inputWord) != null) {
                    inputConceptCnt++;
                }
            }
            refOnto.getOntologyRank().setInputWordCount(inputConceptCnt);
            refOnto.getOntologyRank().setInputWordRatio(inputConceptCnt / inputWordSet.size());
            int relationCnt = 0;
            for (String property : refOnto.getPropertySet()) {
                Resource propRes = ResourceFactory.createResource(property);
                Set<Resource> domainSet = propertyDomainSetMap.get(propRes);
                Set<Resource> rangeSet = propertyRangeSetMap.get(propRes);
                if (propertySet.contains(propRes) && domainSet != null && rangeSet != null) {
                    int domainCnt = 0;
                    for (String domain : refOnto.getDomainSet(property)) {
                        Resource domainRes = ResourceFactory.createResource(domain);
                        if (domainSet.contains(domainRes)) {
                            domainCnt++;
                        }
                    }
                    int rangeCnt = 0;
                    for (String range : refOnto.getRangeSet(property)) {
                        Resource rangeRes = ResourceFactory.createResource(range);
                        if (rangeSet.contains(rangeRes)) {
                            rangeCnt++;
                        }
                    }
                    relationCnt += (domainCnt * rangeCnt);
                }
            }
            refOnto.getOntologyRank().setRelationCount(relationCnt);
            if (inputConceptCnt == 0 && relationCnt == 0) {
                unnecessaryOntologyURISet.add(refOnto.getURI());
            }
        }
        for (String uri : unnecessaryOntologyURISet) {
            uriRefOntologyMap.remove(uri);
        }
    }

    public String toString() {
        return uriSwoogleOWLMetaDataMap.toString();
    }
}
