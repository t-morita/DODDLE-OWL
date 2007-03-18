/*
 * @(#)  2007/03/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class SwoogleWebServiceData {

    private Map<String, Double> swtTermRankMap;

    private Set<Resource> classSet;
    private Set<Resource> propertySet;
    private Map<Resource, Set<Resource>> propertyDomainSetMap;
    private Map<Resource, Set<Resource>> propertyRangeSetMap;

    private Map<String, SwoogleOWLMetaData> uriSwoogleOWLMetaDataMap;
    private static Map<String, ReferenceOWLOntology> uriRefOntologyMap;

    public SwoogleWebServiceData() {
        swtTermRankMap = new HashMap<String, Double>();
        classSet = new HashSet<Resource>();
        propertySet = new HashSet<Resource>();
        propertyDomainSetMap = new HashMap<Resource, Set<Resource>>();
        propertyRangeSetMap = new HashMap<Resource, Set<Resource>>();
        uriSwoogleOWLMetaDataMap = new HashMap<String, SwoogleOWLMetaData>();
        uriRefOntologyMap = new HashMap<String, ReferenceOWLOntology>();
    }

    public void initData() {
        swtTermRankMap.clear();
        classSet.clear();
        propertySet.clear();
        propertyDomainSetMap.clear();
        propertyRangeSetMap.clear();
        uriRefOntologyMap.clear();
    }

    public void putTermRank(String uri, double rank) {
        swtTermRankMap.put(uri, rank);
    }

    public Double getTermRank(String uri) {
        return swtTermRankMap.get(uri);
    }

    public void putRefOntology(String uri, ReferenceOWLOntology refOntology) {
        uriRefOntologyMap.put(uri, refOntology);
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

    public void addClass(Resource property) {
        classSet.add(property);
    }

    public Set<Resource> getClassSet() {
        return classSet;
    }

    public void addProperty(Resource property) {
        propertySet.add(property);
    }

    public Set<Resource> getPropertySet() {
        return propertySet;
    }
    
    public Set<Resource> getConceptSet() {
        Set<Resource> conceptSet = new HashSet<Resource>();
        conceptSet.addAll(classSet);
        conceptSet.addAll(propertySet);
        return conceptSet;
    }

    private void addPropertyRegion(Map<Resource, Set<Resource>> propertyRegionSetMap, Resource property, Resource region) {
        if (propertyRegionSetMap.get(property) != null) {
            Set<Resource> regionSet = propertyRegionSetMap.get(property);
            regionSet.add(region);
        } else {
            Set<Resource> regionSet = new HashSet<Resource>();
            regionSet.add(region);
        }
    }

    public void addPropertyDomain(Resource property, Resource domain) {
        addPropertyRegion(propertyDomainSetMap, property, domain);
    }

    public void addPropertyRange(Resource property, Resource range) {
        addPropertyRegion(propertyRangeSetMap, property, range);
    }

    private void removeUnnecessaryRegionSet(Map<Resource, Set<Resource>> propertyRegionSetMap) {
        for (Resource property : propertyRegionSetMap.keySet()) {
            Set<Resource> unnecessaryRegionSet = new HashSet<Resource>();
            Set<Resource> regionSet = propertyRegionSetMap.get(property);
            for (Resource region : regionSet) {
                if (!classSet.contains(region)) {
                    unnecessaryRegionSet.add(region);
                }
            }
            regionSet.removeAll(unnecessaryRegionSet);
        }
    }

    /**
     * 定義域と値域の両方に入力単語に関連するクラスが定義されていないプロパティは削除する
     */
    public void removeUnnecessaryPropertySet() {
        Set<Resource> unnecessaryPropertySet = new HashSet<Resource>();
        for (Resource property : propertySet) {
            if (getDomainSet(property) == null || getRangeSet(property) == null) {
                unnecessaryPropertySet.add(property);
            }
        }
        propertySet.removeAll(unnecessaryPropertySet);
    }

    /**
     * 入力単語に関連しない定義域と値域は削除する
     */
    public void removeUnnecessaryRegionSet() {
        removeUnnecessaryRegionSet(propertyDomainSetMap);
        removeUnnecessaryRegionSet(propertyRangeSetMap);
    }

    public Set<Resource> getDomainSet(Resource property) {
        return propertyDomainSetMap.get(property);
    }

    public Set<Resource> getRangeSet(Resource property) {
        return propertyRangeSetMap.get(property);
    }

    /**
     * inputWordRatio, relationCountを計算する
     * 
     */
    public void calcOntologyRank(Set<String> inputWordSet) {
        Set<String> unnecessaryOntologyURISet = new HashSet<String>();
        for (String uri : uriRefOntologyMap.keySet()) {
            ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
            double inputConceptCnt = 0;
            for (String inputWord : inputWordSet) {
                if (refOnto.getURISet(inputWord) != null) {
                    inputConceptCnt++;
                }
            }
            refOnto.getOntologyRank().setInputWordRatio(inputConceptCnt / inputWordSet.size());
            int relationCnt = 0;
            for (Resource property : propertySet) {
                if (refOnto.getConcept(property.getURI()) != null) {
                    if (propertyDomainSetMap.get(property) != null && propertyRangeSetMap.get(property) != null) {
                        int cnt = propertyDomainSetMap.get(property).size() * propertyRangeSetMap.get(property).size();
                        relationCnt += cnt;
                    }
                }
            }
            refOnto.getOntologyRank().setRelationCount(relationCnt);
            System.out.println(refOnto);
            if (inputConceptCnt == 0 && relationCnt == 0) {
                System.out.println("不要: " + refOnto);
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
