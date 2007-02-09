/*
 * @(#)  2006/12/14
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class ReferenceOWLOntology {
    private String uri;
    private Model ontModel;
    private Map<String, Set<String>> wordURIsMap;
    private Map<String, Concept> uriConceptMap;
    private Set<String> classSet;
    private Set<String> propertySet;
    private Set<Resource> conceptResourceSet;
    private Map<String, Set<String>> domainMap;
    private Map<String, Set<String>> rangeMap;

    private NameSpaceTable nsTable;
    private OWLOntologyExtractionTemplate owlExtractionTemplate;

    public ReferenceOWLOntology(InputStream is, String uri, NameSpaceTable nst) {
        this.uri = uri;
        owlExtractionTemplate = new OWLOntologyExtractionTemplate();
        nsTable = nst;
        ontModel = ModelFactory.createDefaultModel();
        ontModel.read(is, DODDLE.BASE_URI, getType(uri));
        wordURIsMap = new HashMap<String, Set<String>>();
        uriConceptMap = new HashMap<String, Concept>();
        classSet = new HashSet<String>();
        propertySet = new HashSet<String>();
        conceptResourceSet = new HashSet<Resource>();
        domainMap = new HashMap<String, Set<String>>();
        rangeMap = new HashMap<String, Set<String>>();
        makeWordURIsMap();
    }
    
    public void reload() {
        wordURIsMap.clear();
        uriConceptMap.clear();
        classSet.clear();
        propertySet.clear();
        conceptResourceSet.clear();
        domainMap.clear();
        rangeMap.clear();
        makeWordURIsMap();
    }

    private String getType(String str) {
        String ext = FileUtils.getFilenameExt(str);
        String type = "RDF/XML";
        if (ext.equals("n3")) {
            type = "N3";
        }
        return type;
    }

    private static final String SUB_CONCEPT_QUERY_STRING = "subConcept";
    private static final String CLASS_QUERY_STRING = "class";
    private static final String PROPERTY_QUERY_STRING = "property";
    private static final String LABEL_QUERY_STRING = "label";
    private static final String EXPLANATION_QUERY_STRING = "explanation";
    private static final String DOMAIN_QUERY_STRING = "domain";
    private static final String RANGE_QUERY_STRING = "range";

    public String getURI() {
        return uri;
    }

    public OWLOntologyExtractionTemplate getOWLOntologyExtractionTemplate() {
        return owlExtractionTemplate;
    }

    private String getQueryString(String fileName) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            // UTF-8にすると一行目がうまく解析できない
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-16"));
            while (reader.ready()) {
                String line = reader.readLine();
                builder.append(line);
                builder.append(" ");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    private QueryExecution getQueryExcecution(String fileName) {
        String queryString = getQueryString(fileName);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        return qexec;
    }

    private void setPropertySet(String searchPropertiesTemplate) {
        QueryExecution qexec = getQueryExcecution(searchPropertiesTemplate);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource propertyRes = (Resource) qs.get(PROPERTY_QUERY_STRING);
                propertySet.add(propertyRes.getURI());
                conceptResourceSet.add(propertyRes);
                nsTable.addNameSpaceTable(Utils.getNameSpace(propertyRes));
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    private void setClassSet(String searchClassesTemplate) {
        QueryExecution qexec = getQueryExcecution(searchClassesTemplate);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource classRes = (Resource) qs.get(CLASS_QUERY_STRING);
                classSet.add(classRes.getURI());
                conceptResourceSet.add(classRes);
                nsTable.addNameSpaceTable(Utils.getNameSpace(classRes));
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public void makeWordURIsMap() {
        setClassSet(owlExtractionTemplate.getSearchClassSetTemplate());
        setPropertySet(owlExtractionTemplate.getSearchPropertySetTemplate());
        for (Resource conceptResource : conceptResourceSet) {
            // ローカル名をラベルとして扱う
            String localName = conceptResource.getLocalName();
            if (wordURIsMap.get(localName) != null) {
                Set<String> uris = wordURIsMap.get(localName);
                uris.add(conceptResource.getURI());
            } else {
                Set<String> uris = new HashSet<String>();
                uris.add(conceptResource.getURI());
                wordURIsMap.put(localName, uris);
            }
            setWordURIsMap(conceptResource.getURI(), owlExtractionTemplate.getSearchLabelSetTemplate());
        }
    }

    private void setWordURIsMap(String uri, String searchLabelSetTemplate) {
        String queryString = getQueryString(searchLabelSetTemplate);
        queryString = queryString.replaceAll("\\?uri", "<" + uri + ">"); // ?uriを<概念URI>に置換
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Literal label = (Literal) qs.get(LABEL_QUERY_STRING);
                if (wordURIsMap.get(label.getString()) != null) {
                    Set<String> uris = wordURIsMap.get(label.getString());
                    uris.add(uri);
                } else {
                    Set<String> uris = new HashSet<String>();
                    uris.add(uri);
                    wordURIsMap.put(label.getString(), uris);
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public Set<String> getPropertySet() {
        return propertySet;
    }

    public Set<String> getDomainSet(String uri) {
        if (domainMap.get(uri) != null) { return domainMap.get(uri); }
        setDomainSet(owlExtractionTemplate.getSearchDomainSetTemplate());
        if (domainMap.get(uri) != null) { return domainMap.get(uri); }
        return new HashSet<String>();
    }

    private void setDomainSet(String searchDomainSetTemplate) {
        QueryExecution qexec = getQueryExcecution(searchDomainSetTemplate);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource propertyRes = (Resource) qs.get(PROPERTY_QUERY_STRING);
                Resource domainRes = (Resource) qs.get(DOMAIN_QUERY_STRING);
                if (domainMap.get(propertyRes.getURI()) != null) {
                    Set<String> domainSet = domainMap.get(propertyRes.getURI());
                    domainSet.add(domainRes.getURI());
                } else {
                    Set<String> domainSet = new HashSet<String>();
                    domainSet.add(domainRes.getURI());
                    domainMap.put(propertyRes.getURI(), domainSet);
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public Set<String> getRangeSet(String uri) {
        if (rangeMap.get(uri) != null) { return rangeMap.get(uri); }
        setRangeSet(owlExtractionTemplate.getSearchRangeSetTemplate());
        if (rangeMap.get(uri) != null) { return rangeMap.get(uri); }
        return new HashSet<String>();
    }

    private void setRangeSet(String searchRangeSetTemplate) {
        QueryExecution qexec = getQueryExcecution(searchRangeSetTemplate);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource propertyRes = (Resource) qs.get(PROPERTY_QUERY_STRING);
                Resource rangeRes = (Resource) qs.get(RANGE_QUERY_STRING);
                if (rangeMap.get(propertyRes.getURI()) != null) {
                    Set<String> rangeSet = rangeMap.get(propertyRes.getURI());
                    rangeSet.add(rangeRes.getURI());
                } else {
                    Set<String> rangeSet = new HashSet<String>();
                    rangeSet.add(rangeRes.getURI());
                    rangeMap.put(propertyRes.getURI(), rangeSet);
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public Set<String> getURISet(String word) {
        return wordURIsMap.get(word);
    }

    public Concept getConcept(String uri) {
        return getConcept(uri, owlExtractionTemplate.getSearchConceptTemplate());
    }

    private Concept getConcept(String uri, String searchConceptTemplate) {
        if (uriConceptMap.get(uri) != null) { return uriConceptMap.get(uri); }
        Concept concept = new Concept(uri, "");
        String queryString = getQueryString(searchConceptTemplate);
        queryString = queryString.replaceAll("\\?concept", "<" + uri + ">"); // ?conceptを<概念URI>に置換
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Literal label = (Literal) qs.get(LABEL_QUERY_STRING);
                Literal explanation = (Literal) qs.get(EXPLANATION_QUERY_STRING);

                if (label != null) {
                    if (label.getLanguage().equals("ja") && 0 < label.getString().length()) {
                        concept.addJaWord(label.getString());
                    }

                    if ((label.getLanguage().equals("en") || label.getLanguage().equals(""))
                            && 0 < label.getString().length()) {
                        concept.addEnWord(label.getString());
                    }
                }
                if (explanation != null) {
                    if (explanation.getLanguage().equals("ja") && 0 < explanation.getString().length()) {
                        concept.setJaExplanation(explanation.getString());
                    }

                    if ((explanation.getLanguage().equals("en") || explanation.getLanguage().equals(""))
                            && 0 < explanation.getString().length()) {
                        concept.setEnExplanation(explanation.getString());
                    }
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
        uriConceptMap.put(uri, concept);
        return concept;
    }

    public Set<List<Concept>> getPathToRootSet(String uri) {
        Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
        ArrayList<Concept> pathToRoot = new ArrayList<Concept>();
        pathToRoot.add(getConcept(uri));
        Property subConceptOf = null;
        if (propertySet.contains(uri)) {
            subConceptOf = RDFS.subPropertyOf;
        } else {
            subConceptOf = RDFS.subClassOf;
        }
        pathToRootSet.addAll(setPathToRoot(ontModel.createResource(uri), pathToRoot, subConceptOf));
        return pathToRootSet;
    }

    public Set<List<Concept>> setPathToRoot(Resource conceptRes, List<Concept> pathToRoot, Property subConceptOf) {
        Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
        if (!ontModel.listObjectsOfProperty(conceptRes, subConceptOf).hasNext()) {
            pathToRootSet.add(pathToRoot);
            return pathToRootSet;
        }
        for (NodeIterator i = ontModel.listObjectsOfProperty(conceptRes, subConceptOf); i.hasNext();) {
            RDFNode node = i.nextNode();
            if (node instanceof Resource && !node.isAnon()) {
                List<Concept> pathToRootClone = new ArrayList<Concept>(pathToRoot);
                Resource supConceptRes = (Resource) node;
                if (!(ConceptTreeMaker.isDODDLEClassRootURI(supConceptRes.getURI()) || ConceptTreeMaker
                        .isDODDLEPropertyRootURI(supConceptRes.getURI()))) {
                    pathToRootClone.add(0, getConcept(supConceptRes.getURI()));
                }
                pathToRootSet.addAll(setPathToRoot(supConceptRes, pathToRootClone, subConceptOf));
            }
        }
        return pathToRootSet;
    }

    public Set<String> getSubURISet(String uri) {
        Set<String> subURISet = new HashSet<String>();
        String queryString = getQueryString(owlExtractionTemplate.getSearchSubConceptTemplate());
        queryString = queryString.replaceAll("\\?concept", "<" + uri + ">"); // ?conceptを<概念URI>に置換
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource subConcept = (Resource) qs.get(SUB_CONCEPT_QUERY_STRING);
                subURISet.add(subConcept.getURI());
            }
        } finally {
            qexec.close();
        }
        return subURISet;
    }

    public void getSubURISet(String uri, Set<String> nounURISet, Set<String> refineSubURISet) {
        Set<String> subURISet = getSubURISet(uri);
        if (subURISet.size() == 0) { return; }
        for (String subURI : subURISet) {
            if (nounURISet.contains(subURI)) {
                refineSubURISet.add(subURI);
            }
        }
        if (0 < refineSubURISet.size()) { return; }
        for (String subURI : subURISet) {
            getSubURISet(subURI, nounURISet, refineSubURISet);
        }
    }

    public static void main(String[] args) {
        try {
            NameSpaceTable nsTable = new NameSpaceTable();
            ReferenceOWLOntology info = new ReferenceOWLOntology(new FileInputStream("test.owl"), "test.owl", nsTable);
            System.out.println(info.getURISet("Resource"));
            System.out.println(info.getURISet("animal"));
            System.out.println(info.getURISet("dog"));
            System.out.println(info.getURISet("cat"));
            System.out.println(info.getURISet("動物"));
            System.out.println(info.getURISet("犬"));
            System.out.println(info.getURISet("猫"));
            System.out.println(info.getURISet("ひっかく"));
            System.out.println(info.getURISet("bow"));
            Concept c = info.getConcept("http://mmm.semanticweb.org/mr3#animal");
            System.out.println("en word: " + c.getEnWord());
            System.out.println("ja word: " + c.getJaWord());
            System.out.println("ja exp: " + c.getJaExplanation());
            System.out.println("en exp: " + c.getEnExplanation());
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#cat"));
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#testdog"));
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#animal"));
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#bow"));
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#hikkaku"));
            System.out.println(info.getDomainSet("http://mmm.semanticweb.org/mr3#bow"));
            System.out.println(info.getRangeSet("http://mmm.semanticweb.org/mr3#bow"));
            System.out.println(info.getDomainSet("http://mmm.semanticweb.org/mr3#hikkaku"));
            System.out.println(info.getRangeSet("http://mmm.semanticweb.org/mr3#hikkaku"));
            Set<List<Concept>> pathToRootSet = info.getPathToRootSet("http://mmm.semanticweb.org/mr3#Siamese");
            System.out.println("path to root: " + pathToRootSet);
            System.out.println(pathToRootSet.size());
        } catch (FileNotFoundException fne) {
            fne.printStackTrace();
        }
    }
}
