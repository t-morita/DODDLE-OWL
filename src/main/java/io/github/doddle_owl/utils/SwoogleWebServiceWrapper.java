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

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.reference_ontology_selection.ReferenceOWLOntology;
import io.github.doddle_owl.models.reference_ontology_selection.SwoogleOWLMetaData;
import io.github.doddle_owl.models.reference_ontology_selection.SwoogleWebServiceData;
import io.github.doddle_owl.views.reference_ontology_selection.NameSpaceTable;
import io.github.doddle_owl.views.StatusBarPanel;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Takeshi Morita
 */
public class SwoogleWebServiceWrapper {

    private static final int MAX_CNT = 5;

    private static final Set<Resource> literalResourceSet = new HashSet<>();

    static {
        literalResourceSet.add(ResourceFactory
                .createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Literal"));
        literalResourceSet.add(ResourceFactory
                .createResource("http://www.w3.org/2001/XMLSchema#string"));
        literalResourceSet.add(ResourceFactory
                .createResource("http://www.w3.org/2001/XMLSchema#date"));
        literalResourceSet.add(ResourceFactory
                .createResource("http://www.w3.org/2001/XMLSchema#dateTime"));
        literalResourceSet.add(ResourceFactory
                .createResource("http://www.w3.org/2001/XMLSchema#double"));
        literalResourceSet.add(ResourceFactory
                .createResource("http://www.w3.org/2001/XMLSchema#float"));
        literalResourceSet.add(ResourceFactory
                .createResource("http://www.w3.org/2001/XMLSchema#int"));
    }

    private static List<String> owlOntologyList;
    private static List<String> swoogleQueryList;

    private static NameSpaceTable nsTable;
    private static final SwoogleWebServiceData swoogleWebServiceData = new SwoogleWebServiceData();

    private static final String SWOOGLE_WEB_SERVICE_URI = " http://sparql.cs.umbc.edu:80/swoogle31/q?";
    private static final String SWOOGLE_WEB_SERVICE_KEY = "&key=demo";
    private static final String SWOOGLE_QUERY_RESULTS_DIR = DODDLEConstants.PROJECT_HOME + File.separator + "swoogle_query_results_tmp";
    private static final String SWOOGLE_QUERY_RESULT_LIST_FILE = "swoogle_query_files.txt";
    private static final String OWL_ONTOLOGIES_DIR = DODDLEConstants.PROJECT_HOME + File.separator + "owl_ontologies";
    private static final String OWL_ONTOLOGY_RESULT_LIST_FILE = "owl_files.txt";

    private static final String ONTOLOGY_URL = "ontology_url";
    private static final String ONTOLOGY_RANK = "ontoRank";
    private static final String SWT_URI = "swt_uri";
    private static final String TERM_RANK = "termRank";
    private static final String PROPERTY = "property";
    private static final String CLASS = "class";
    private static final String ENCODING = "encoding";
    private static final String RDF_TYPE = "rdf_type";
    private static final String FILE_TYPE = "file_type";
    private static final String REFERENCE_TYPE = "reference_type";

    public static SwoogleWebServiceData getSwoogleWebServiceData() {
        return swoogleWebServiceData;
    }

    private static void initOWLOntologyList() {
        File dir = new File(OWL_ONTOLOGIES_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(OWL_ONTOLOGIES_DIR + File.separator + OWL_ONTOLOGY_RESULT_LIST_FILE);
        // System.out.println(file.getAbsolutePath());
        owlOntologyList = new ArrayList<>();
        if (!file.exists()) {
            return;
        }
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (reader.ready()) {
                String line = reader.readLine();
                owlOntologyList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initSwoogleQueryList() {
        File dir = new File(SWOOGLE_QUERY_RESULTS_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(SWOOGLE_QUERY_RESULTS_DIR + File.separator
                + SWOOGLE_QUERY_RESULT_LIST_FILE);
        // System.out.println(file.getAbsolutePath());
        swoogleQueryList = new ArrayList<>();
        if (!file.exists()) {
            return;
        }
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (reader.ready()) {
                String line = reader.readLine();
                swoogleQueryList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存されているSwoogleクエリー結果からswoogleWebServiceDataのuriSwoogleOWLMetaDataMapにOWLメタデータを格納する
     */
    private static void initSwoogleOWLMetaData() {
        if (swoogleQueryList == null) {
            return;
        }
        QueryExecution qexec = null;
        try {
            for (int i = 0; i < swoogleQueryList.size(); i++) {
                String queryTypeAndSearchString = swoogleQueryList.get(i);
                int index = swoogleQueryList.lastIndexOf(queryTypeAndSearchString);
                if (queryTypeAndSearchString.contains("queryType=search_swd_ontology")
                        || queryTypeAndSearchString.contains("queryType=digest_swd")) {
                    File file = new File(SWOOGLE_QUERY_RESULTS_DIR + File.separator + "query_"
                            + (index + 1));
                    if (!file.exists()) {
                        continue;
                    }
                    Model model = getModel(new FileInputStream(file), DODDLEConstants.BASE_URI);
                    String sparqlQueryString = SPARQLQueryUtil
                            .getQueryString(getSearchOntologyQuery());
                    Query query = QueryFactory.create(sparqlQueryString);
                    qexec = QueryExecutionFactory.create(query, model);
                    ResultSet results = qexec.execSelect();
                    while (results.hasNext()) {
                        QuerySolution qs = results.nextSolution();
                        loadOWLMetaData(qs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public static void setNameSpaceTable(NameSpaceTable nstbl) {
        nsTable = nstbl;
    }

    public static void deleteOWLOntologies() {
        File ontologyDir = new File(OWL_ONTOLOGIES_DIR);
        File[] files = ontologyDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            // files[i].delete();
        }
    }

    public static void deleteSwoogleQueryResults() {
        File ontologyDir = new File(SWOOGLE_QUERY_RESULTS_DIR);
        File[] files = ontologyDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            // files[i].delete();
        }
    }

    private static InputStream getSearchOntologyQuery() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("swoogle_queries/" + "SearchOntology.rq");
    }

    private static InputStream getSearchTermQuery() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("swoogle_queries/" + "SearchTerm.rq");
    }

    private static InputStream getListDocumentsUsingTermQuery() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("swoogle_queries/" + "listDocumentsUsingTerm.rq");
    }

    private static InputStream getListPropertiesOfaRegionClassQuery() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("swoogle_queries/" + "listPropertiesOfaRegionClass.rq");
    }

    private static InputStream getListRegionClassesOfaPropertyQuery() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("swoogle_queries/" + "listRegionClassesOfaProperty.rq");
    }

    private static Model getModel(InputStream inputStream, String baseURI) {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(inputStream, baseURI, "RDF/XML");
        } catch (Exception e) {
            System.out.println("RDF Parse Exception");
        }
        return model;
    }

    private static void saveFile(File file, InputStream inputStream, String encoding) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), encoding));
            reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
            String line;
            while ((line = reader.readLine()) != null) { // reader.ready()を使うと書き込み途中で終了する場合がある.
                writer.write(line);
                writer.newLine();
            }
        } catch (FileNotFoundException fne) {
            System.out.println("fileName error: " + file);
        } catch (IOException uee) {
            uee.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static void appendURI(String uri, File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            writer.append(uri);
            writer.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void appendOntology(String url) {
        appendURI(url,
                new File(OWL_ONTOLOGIES_DIR + File.separator + OWL_ONTOLOGY_RESULT_LIST_FILE));
        owlOntologyList.add(url);
    }

    private static void appendQuery(String url) {
        appendURI(url, new File(SWOOGLE_QUERY_RESULTS_DIR + File.separator
                + SWOOGLE_QUERY_RESULT_LIST_FILE));
        swoogleQueryList.add(url);
    }

    private static void saveOntology(String url, File file, InputStream inputStream, String encoding) {
        saveFile(file, inputStream, encoding);
        appendOntology(url);
    }

    private static void saveQueryResult(String queryTypeAndSearchString, File file,
                                        InputStream inputStream) {
        saveFile(file, inputStream, "UTF-8");
        try {
            DODDLE_OWL.getLogger().info("sleep 2 sec");
            Thread.sleep(2000); // 1秒間間隔をあけてアクセスする
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        appendQuery(queryTypeAndSearchString);
    }

    private static Model getSwoogleQueryResultModel(String queryTypeAndSearchString) {
        String restQuery = SWOOGLE_WEB_SERVICE_URI + queryTypeAndSearchString
                + SWOOGLE_WEB_SERVICE_KEY;
        Model model = null;
        try {
            // System.out.println(queryTypeAndSearchString);
            int index = swoogleQueryList.lastIndexOf(queryTypeAndSearchString);
            index += 1;
            File queryCachFile = new File(SWOOGLE_QUERY_RESULTS_DIR + File.separator + "query_"
                    + index);
            if (queryCachFile.exists()) {
                DODDLE_OWL.getLogger().info("Using Cashed Data");
                model = getModel(new FileInputStream(queryCachFile), DODDLEConstants.BASE_URI);
            } else {
                queryCachFile = new File(SWOOGLE_QUERY_RESULTS_DIR + File.separator + "query_"
                        + (swoogleQueryList.size() + 1));
                URL url = new URL(restQuery);
                saveQueryResult(queryTypeAndSearchString, queryCachFile, url.openStream());
                model = getModel(url.openStream(), DODDLEConstants.BASE_URI);
            }
        } catch (IOException fne) {
            fne.printStackTrace();
        }
        if (model == null) {
            model = ModelFactory.createDefaultModel();
        }
        return model;
    }

    private static void loadOWLMetaData(QuerySolution qs) {
        Resource ontologyURL = (Resource) qs.get(ONTOLOGY_URL);
        Literal ontoRank = (Literal) qs.get(ONTOLOGY_RANK);
        Literal encoding = (Literal) qs.get(ENCODING);
        Literal fileType = (Literal) qs.get(FILE_TYPE);
        Resource rdfType = (Resource) qs.get(RDF_TYPE);
        SwoogleOWLMetaData owlMetaData = new SwoogleOWLMetaData(ontologyURL, encoding, fileType,
                rdfType, ontoRank);
        swoogleWebServiceData.putSwoogleOWLMetaData(ontologyURL.getURI(), owlMetaData);
    }

    private static void saveOntology(QuerySolution qs) {
        File ontFile = null;
        URL ontURL = null;
        try {
            Resource ontologyURL = (Resource) qs.get(ONTOLOGY_URL);
            Literal ontoRank = (Literal) qs.get(ONTOLOGY_RANK);
            Literal encoding = (Literal) qs.get(ENCODING);
            Literal fileType = (Literal) qs.get(FILE_TYPE);
            Resource rdfType = (Resource) qs.get(RDF_TYPE);

            DODDLE_OWL.getLogger().info("Try to save: " + ontologyURL.getURI());
            switch (ontologyURL.getURI()) {
                case "http://c703-deri03.uibk.ac.at:8080/people/mappings.owl":
                    System.out.println("skip: http://c703-deri03.uibk.ac.at:8080/people/mappings.owl");
                    return;
                case "http://www.daml.org/2004/05/unspsc/unspsc.owl":
                    System.out.println("skip: http://www.daml.org/2004/05/unspsc/unspsc.owl");
                    return;
                case "http://www.daml.org/2004/05/unspsc/unspsc":
                    System.out.println("skip: http://www.daml.org/2004/05/unspsc/unspsc");
                    return;
                case "http://dmag.upf.edu/ontologies/2004/10/ODRL-DD-11.owl":
                    System.out.println("skip: http://dmag.upf.edu/ontologies/2004/10/ODRL-DD-11.owl");
                    return;
            }
            SwoogleOWLMetaData owlMetaData = new SwoogleOWLMetaData(ontologyURL, encoding,
                    fileType, rdfType, ontoRank);
            swoogleWebServiceData.putSwoogleOWLMetaData(ontologyURL.getURI(), owlMetaData);
            ontURL = new URL(ontologyURL.getURI());
            int index = owlOntologyList.lastIndexOf(owlMetaData.getURL());
            index += 1;
            ontFile = new File(OWL_ONTOLOGIES_DIR + File.separator + "onto_" + index);
            if (!ontFile.exists()) {
                DODDLE_OWL.getLogger().info("Save Ontology: " + ontologyURL);
                try {
                    ontFile = new File(OWL_ONTOLOGIES_DIR + File.separator + "onto_"
                            + (owlOntologyList.size() + 1));
                    saveOntology(ontologyURL.getURI(), ontFile, ontURL.openStream(),
                            owlMetaData.getFileEncoding());
                } catch (Exception e) {
                    DODDLE_OWL.getLogger().severe("ignore exception !!");
                }
            } else {
                DODDLE_OWL.getLogger().warning("Using Cashed Data");
            }
            if (swoogleWebServiceData.getRefOntology(ontologyURL.getURI()) == null) {
                Model ontModel = Utils.getOntModel(new FileInputStream(ontFile),
                        fileType.getString(), Utils.getRDFType(rdfType),
                        Utils.getNameSpace(ontologyURL));
                ReferenceOWLOntology refOnto = new ReferenceOWLOntology(ontModel,
                        ontologyURL.getURI(), nsTable);
                refOnto.getOntologyRank().setSwoogleOntoRank(owlMetaData.getOntoRank());
                swoogleWebServiceData.putRefOntology(ontologyURL.getURI(), refOnto);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (FileNotFoundException fne) {
            fne.printStackTrace();
            DODDLE_OWL.getLogger().severe("Save Ontology Exception: File => " + ontFile.getAbsolutePath() + " URL => " + ontURL);
        }
    }

    /**
     * 獲得したオントロジーの中から入力単語に関連するクラス及びプロパティを抽出する
     */
    private static void setSWTSet(Set<String> inputWordSet) {
        for (String uri : swoogleWebServiceData.getRefOntologyURISet()) {
            ReferenceOWLOntology refOnt = swoogleWebServiceData.getRefOntology(uri);
            for (String inputWord : inputWordSet) {
                // 英単語はすべて小文字に変換してから検索する
                Set<String> conceptURISet = refOnt.getURISet(inputWord);
                if (conceptURISet == null) {
                    continue;
                }
                for (String conceptURI : conceptURISet) {
                    Resource conceptResource = ResourceFactory.createResource(conceptURI);
                    if (refOnt.getPropertySet().contains(conceptURI)) {
                        swoogleWebServiceData.addProperty(conceptResource);
                    } else {
                        swoogleWebServiceData.addClass(inputWord, conceptResource);
                    }
                }
            }
        }
    }

    /**
     * 入力単語に関連するオントロジーを獲得
     *
     * @param inputWord
     */
    private static void searchOntology(String inputWord) {
        // demo以外のキーがもらえば検索結果数に応じて獲得するオントロジーの数を決められる
        // とりあえず上位10個のみを対象とする
        for (int i = 1; i < 10; i += 10) {
            QueryExecution qexec = null;
            try {
                String queryTypeAndSearchString = "queryType=search_swd_ontology&searchString=def:"
                        + inputWord + "&searchStart=" + i;
                DODDLE_OWL.getLogger().info("Search Ontology: " + inputWord);
                DODDLE_OWL.STATUS_BAR.setText("Search Ontology: " + inputWord);
                Model model = getSwoogleQueryResultModel(queryTypeAndSearchString);
                String sparqlQueryString = SPARQLQueryUtil.getQueryString(getSearchOntologyQuery());
                Query query = QueryFactory.create(sparqlQueryString);
                if (model != null) {
                    qexec = QueryExecutionFactory.create(query, model);
                    ResultSet results = qexec.execSelect();
                    while (results.hasNext()) {
                        QuerySolution qs = results.nextSolution();
                        saveOntology(qs);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (qexec != null) {
                    qexec.close();
                }
            }
        }
    }

    private static final Set<String> classWordSet = new HashSet<>();
    private static final Set<String> propertyWordSet = new HashSet<>();

    /**
     * 入力単語に関連するSWTを獲得
     *
     * @param maxCnt
     * @param inputWord
     * @param type
     */
    private static void searchTerms(int maxCnt, boolean isSearchLabel, String inputWord, String type) {
        QueryExecution qexec = null;
        try {
            String searchString;
            if (isSearchLabel) {
                searchString = "(localname:" + inputWord + " OR label:" + inputWord + ") ";
            } else {
                searchString = "(localname:" + inputWord + ") ";
            }
            searchString += "(type:owl." + type + " OR type:rdfs." + type + " OR type:daml." + type
                    + ")";

            String queryTypeAndSearchString = "queryType=search_swt&searchString="
                    + URLEncoder.encode(searchString, StandardCharsets.UTF_8);
            DODDLE_OWL.getLogger().info("Search Terms: " + inputWord);
            DODDLE_OWL.STATUS_BAR.setText("Search Terms: " + inputWord);
            Model model = getSwoogleQueryResultModel(queryTypeAndSearchString);
            if (model == null) {
                System.out.println("model == null");
                return;
            }
            String sparqlQueryString = SPARQLQueryUtil.getQueryString(getSearchTermQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            int cnt = 0;
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                if (cnt == maxCnt) {
                    break;
                }
                cnt++;
                Resource swtURI = (Resource) qs.get(SWT_URI);
                Literal termRank = (Literal) qs.get(TERM_RANK);
                if (type.equals(CLASS)) {
                    classWordSet.add(inputWord);
                    swoogleWebServiceData.addClass(inputWord, swtURI);
                } else if (type.equals(PROPERTY)) {
                    propertyWordSet.add(inputWord);
                    swoogleWebServiceData.addProperty(swtURI);
                }
                swoogleWebServiceData.putTermRank(swtURI.getURI(), termRank.getDouble());
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    private static void searchListPropertiesOfaRegionClass(String queryTypeAndSearchString,
                                                           String regionURI, Property regionType) {
        Resource region = ResourceFactory.createResource(regionURI);
        QueryExecution qexec = null;
        try {
            Model model = getSwoogleQueryResultModel(queryTypeAndSearchString);
            String sparqlQueryString = SPARQLQueryUtil
                    .getQueryString(getListPropertiesOfaRegionClassQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource property = (Resource) qs.get(PROPERTY);
                swoogleWebServiceData.addRelatedProperty(property);
                if (regionType == RDFS.domain) {
                    swoogleWebServiceData.addPropertyDomain(property, region);
                } else if (regionType == RDFS.range) {
                    swoogleWebServiceData.addPropertyRange(property, region);
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    /**
     * 入力したクラスを定義域とするプロパティを獲得
     */
    private static void searchListPropertiesOfaDomainClass(String domainURI) {
        String queryTypeAndSearchString = "queryType=rel_swd_instance_domain_c2p&searchString="
                + URLEncoder.encode(domainURI, StandardCharsets.UTF_8);
        DODDLE_OWL.getLogger().info("Search List Properties Of a Domain Class: " + domainURI);
        DODDLE_OWL.STATUS_BAR.setText("Search List Properties Of a Domain Class: " + domainURI);
        searchListPropertiesOfaRegionClass(queryTypeAndSearchString, domainURI, RDFS.domain);
    }

    /**
     * 入力したクラスを値域とするプロパティを獲得
     */
    private static void searchListPropertiesOfaRangeClass(String rangeURI) {
        String queryTypeAndSearchString = "queryType=rel_swd_instance_range_c2p&searchString="
                + URLEncoder.encode(rangeURI, StandardCharsets.UTF_8);
        DODDLE_OWL.getLogger().info("Search List Properties Of a Range Class: " + rangeURI);
        DODDLE_OWL.STATUS_BAR.setText("Search List Properties Of a Range Class: " + rangeURI);
        searchListPropertiesOfaRegionClass(queryTypeAndSearchString, rangeURI, RDFS.range);
    }

    private static void searchListRegionClassOfaProperty(String queryTypeAndSearchString,
                                                         String propertyURI, Property regionType) {
        Resource property = ResourceFactory.createResource(propertyURI);
        QueryExecution qexec = null;
        try {
            Model model = getSwoogleQueryResultModel(queryTypeAndSearchString);
            String sparqlQueryString = SPARQLQueryUtil
                    .getQueryString(getListRegionClassesOfaPropertyQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource classResource = (Resource) qs.get(CLASS);
                // この時点では定義域と値域はすべて格納しておく
                if (regionType == RDFS.domain) {
                    swoogleWebServiceData.addPropertyDomain(property, classResource);
                } else if (regionType == RDFS.range) {
                    swoogleWebServiceData.addPropertyRange(property, classResource);
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    /**
     * プロパティのURIを入力として，そのプロパティの定義域を獲得
     */
    private static void searchListDomainClassOfaProperty(String propertyURI) {
        String queryTypeAndSearchString = "queryType=rel_swd_instance_domain_p2c&searchString="
                + URLEncoder.encode(propertyURI, StandardCharsets.UTF_8);
        DODDLE_OWL.getLogger().info("Search List Domain Class Of a Property: " + propertyURI);
        DODDLE_OWL.STATUS_BAR.setText("Search List Domain Class Of a Property: " + propertyURI);
        searchListRegionClassOfaProperty(queryTypeAndSearchString, propertyURI, RDFS.domain);
    }

    /**
     * プロパティのURIを入力として，そのプロパティの値域を獲得
     */
    private static void searchListRangeClassOfaProperty(String propertyURI) {
        String queryTypeAndSearchString = "queryType=rel_swd_instance_range_p2c&searchString="
                + URLEncoder.encode(propertyURI, StandardCharsets.UTF_8);
        DODDLE_OWL.getLogger().info("Search List Range Class Of a Property: " + propertyURI);
        DODDLE_OWL.STATUS_BAR.setText("Search List Range Class Of a Property: " + propertyURI);
        searchListRegionClassOfaProperty(queryTypeAndSearchString, propertyURI, RDFS.range);
    }

    /**
     * SWTが定義されているオントロジーを獲得
     */
    private static void searchListDocumentsUsingTerm(String swtURI) {
        QueryExecution qexec = null;
        try {
            String queryTypeAndSearchString = "queryType=rel_swt_swd&searchString="
                    + URLEncoder.encode(swtURI, StandardCharsets.UTF_8);
            DODDLE_OWL.getLogger().info("Search List Documents Using Term: " + swtURI);
            DODDLE_OWL.STATUS_BAR.setText("Search List Documents Using Term: " + swtURI);
            Model model = getSwoogleQueryResultModel(queryTypeAndSearchString);
            String sparqlQueryString = SPARQLQueryUtil
                    .getQueryString(getListDocumentsUsingTermQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource ontologyURL = (Resource) qs.get(ONTOLOGY_URL);
                Resource referenceType = (Resource) qs.get(REFERENCE_TYPE);
                if (ontologyURL != null
                        && referenceType
                        .getURI()
                        .equals("http://daml.umbc.edu/ontologies/webofbelief/1.4/wob.owl#hasClassDefinitionIn")
                        || referenceType
                        .getURI()
                        .equals("http://daml.umbc.edu/ontologies/webofbelief/1.4/wob.owl#hasPropertyDefinitionIn")) {
                    SwoogleOWLMetaData owlMetaData = swoogleWebServiceData
                            .getSwoogleOWLMetaData(ontologyURL.getURI());
                    if (owlMetaData == null) {
                        searchDigestSemanticWebDocument(ontologyURL.getURI());
                    }
                }
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    /**
     * SWDのメタデータを獲得
     */
    private static void searchDigestSemanticWebDocument(String swdURI) {
        QueryExecution qexec = null;
        try {
            String queryTypeAndSearchString = "queryType=digest_swd&searchString="
                    + URLEncoder.encode(swdURI, StandardCharsets.UTF_8);
            DODDLE_OWL.getLogger().info("Search Digest Semantic Web Document: " + swdURI);
            Model model = getSwoogleQueryResultModel(queryTypeAndSearchString);
            String sparqlQueryString = SPARQLQueryUtil.getQueryString(getSearchOntologyQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                saveOntology(qs);
                break; // 補助的に利用しているので１つでも見つかったらbreak
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    private static void searchTerms(Set<String> inputWordSet, boolean isSearchLabel) {
        for (String inputWord : inputWordSet) {
            searchTerms(MAX_CNT, isSearchLabel, inputWord, CLASS);
            searchTerms(MAX_CNT, isSearchLabel, inputWord, PROPERTY);
        }
        // 属性を獲得するため
        for (Resource litRes : literalResourceSet) {
            swoogleWebServiceData.addClass("Literal", litRes);
        }

        DODDLE_OWL.getLogger().info("Class word set: " + classWordSet.size() + "/" + inputWordSet.size() + ": " + classWordSet);
        DODDLE_OWL.getLogger().info("Property word set: " + propertyWordSet.size() + "/" + inputWordSet.size() + ": "
                + propertyWordSet);
        DODDLE_OWL.getLogger().info("Class URI cnt: " + swoogleWebServiceData.getClassSet().size());
        for (Resource cls : swoogleWebServiceData.getClassSet()) {
            System.out.println("Class URI: " + cls.getURI());
        }
        DODDLE_OWL.getLogger().info("Property URI cnt: " + swoogleWebServiceData.getPropertySet().size());
        for (Resource prop : swoogleWebServiceData.getPropertySet()) {
            System.out.println("Property URI: " + prop.getURI());
        }
        DODDLE_OWL.STATUS_BAR.addProjectValue();
    }

    private static void searchListPropertiesOfRegionClasses() {
        for (Resource classResource : swoogleWebServiceData.getClassSet()) {
            if (!literalResourceSet.contains(classResource)) {
                searchListPropertiesOfaDomainClass(classResource.getURI());
            } else {
                System.out.println("literal res: " + classResource);
            }
            searchListPropertiesOfaRangeClass(classResource.getURI());
        }
        DODDLE_OWL.STATUS_BAR.addProjectValue();
    }

    private static void searchListRegionClassOfProperties() {
        Set<Resource> allPropertySet = swoogleWebServiceData.getAllProperty();
        for (Resource propertyResource : allPropertySet) {
            searchListDomainClassOfaProperty(propertyResource.getURI());
            searchListRangeClassOfaProperty(propertyResource.getURI());
        }
        DODDLE_OWL.STATUS_BAR.addProjectValue();
    }

    private static Set<String> getRelatedWordSet(Set<String> inputWordSet) {
        Set<String> relatedWordSet = new HashSet<>(inputWordSet);
        for (Resource property : swoogleWebServiceData.getAllProperty()) {
            relatedWordSet.add(Utils.getLocalName(property));
        }
        return relatedWordSet;
    }

    private static void searchOntologies(Set<String> inputWordSet) {
        // Set<String> relatedWordSet = getRelatedWordSet(inputWordSet); //
        // JSAI全国大会の実験ではメモリ不足のため処理しない

        int cnt = 1;
        for (String word : inputWordSet) {
            DODDLE_OWL.getLogger().info("input word: " + cnt + "/" + inputWordSet.size() + ": " + word);
            searchOntology(word);
            cnt++;
        }
        setSWTSet(inputWordSet);
        cnt = 1;
        int definedConceptCnt = 0;
        Set<Resource> conceptSet = swoogleWebServiceData.getConceptSet();
        for (Resource conceptResource : conceptSet) {
            DODDLE_OWL.getLogger().info("concept: " + cnt + "/" + conceptSet.size() + ": " + conceptResource);
            DODDLE_OWL.getLogger().info("defined concept cnt: " + definedConceptCnt);
            boolean isDefinedConcept = false;
            for (String uri : swoogleWebServiceData.getRefOntologyURISet()) {
                ReferenceOWLOntology refOnto = swoogleWebServiceData.getRefOntology(uri);
                if (refOnto.getClassSet().contains(conceptResource.getURI())
                        || refOnto.getPropertySet().contains(conceptResource.getURI())) {
                    DODDLE_OWL.getLogger().info("defined concept: " + conceptResource);
                    definedConceptCnt++;
                    isDefinedConcept = true;
                    break;
                }
            }
            // searchOntologyで獲得できなかったオントロジーを獲得
            if (!isDefinedConcept) {
                DODDLE_OWL.getLogger().info("cannot get ontology_api using search ontology_api service: " + conceptResource);
                searchListDocumentsUsingTerm(conceptResource.getURI());
            }
            cnt++;
        }
        DODDLE_OWL.STATUS_BAR.addProjectValue();
    }

    private static void printInfo(int level) {
        DODDLE_OWL.getLogger().info(+level + ": Class size: " + swoogleWebServiceData.getClassSet().size());
        DODDLE_OWL.getLogger().info(level + " Related Property size: "
                + swoogleWebServiceData.getRelatedPropertySet().size());
        DODDLE_OWL.getLogger().info(level + " Property size: " + swoogleWebServiceData.getPropertySet().size());
        DODDLE_OWL.getLogger().info(level + " Relation cnt: " + swoogleWebServiceData.getAllRelationCount());
    }

    private static void refinePropertiesAndRegionSet() {
        swoogleWebServiceData.addInheritedRegionSet();
        printInfo(4);
        swoogleWebServiceData.removeUnnecessaryRegionSet();
        printInfo(5);
        swoogleWebServiceData.addNecessaryPropertySet();
        printInfo(6);
    }

    /**
     * 入力単語に関連するオントロジーを獲得する
     *
     * @param inputWordSet
     */
    public static void acquireRelevantOWLOntologies(Set<String> inputWordSet, boolean isSearchLabel) {
        DODDLE_OWL.STATUS_BAR.setText("Acquire OWL Ontologies Start");
        DODDLE_OWL.STATUS_BAR.startTime();
        DODDLE_OWL.STATUS_BAR.initNormal(6);
        DODDLE_OWL.STATUS_BAR.lock();
        searchTerms(inputWordSet, isSearchLabel);

        searchListPropertiesOfRegionClasses();
        printInfo(1);

        searchListRegionClassOfProperties();
        printInfo(2);

        searchOntologies(inputWordSet);
        printInfo(3);

        refinePropertiesAndRegionSet();

        DODDLE_OWL.STATUS_BAR.addProjectValue();

        DODDLE_OWL.STATUS_BAR.setText("Calc Ontology Rank");
        swoogleWebServiceData.calcOntologyRank(inputWordSet);
        DODDLE_OWL.STATUS_BAR.addProjectValue();

        swoogleWebServiceData.countDefinedConcept();

        Object[] refOntologies = swoogleWebServiceData.getRefOntologies().toArray();
        Arrays.sort(refOntologies);
        DODDLE_OWL.getLogger().info("獲得オントロジー数: " + refOntologies.length);
        for (Object refOntology : refOntologies) {
            DODDLE_OWL.getLogger().info(refOntology.toString());
        }
        DODDLE_OWL.getLogger().info("概念定義数： " + swoogleWebServiceData.getValidRelationCount());
        DODDLE_OWL.getLogger().info("オントロジーに定義されている概念定義数： " + swoogleWebServiceData.getDefinedRelationCount());

        DODDLE_OWL.STATUS_BAR.unLock();
        DODDLE_OWL.STATUS_BAR.hideProgressBar();
        DODDLE_OWL.STATUS_BAR.setText("Acquire Ontologies done");
    }

    public static void initSwoogleWebServiceWrapper() {
        // System.out.println("Init OWL Ontology List");
        initOWLOntologyList();
        // System.out.println("Init Swoogle Query List");
        initSwoogleQueryList();
        // System.out.println("Init Swoogle OWL MetaData");
        initSwoogleOWLMetaData();
    }

    public static void main(String[] args) {
        Translator.loadDODDLEComponentOntology(DODDLEConstants.LANG);
        DODDLE_OWL.STATUS_BAR = new StatusBarPanel();
        DODDLE_OWL.getLogger().setLevel(Level.INFO);
        DODDLE_OWL.setFileLogger();

        initSwoogleWebServiceWrapper();
        SwoogleWebServiceWrapper.setNameSpaceTable(new NameSpaceTable());

        /*
         * String[] inputWords = new String[] { "time", "address", "act",
         * "dispatch", "quality", "letter", "contract", "offer", "effect",
         * "day", "price", "conduct", "circumstance", "party", "envelope",
         * "delivery", "silence", "quantity", "intention", "delay",
         * "acceptance", "addition", "telex", "residence", "person",
         * "transmission", "telephone", "reply", "modification", "invitation",
         * "rejection", "indication", "withdrawal", "holiday", "proposal",
         * "payment", "goods", "discrepancy", "assent", "revocation", "offeree",
         * "counteroffer", "communication_system", "speech_act",
         * "place_of_business", "offerer"};
         */

        /*
         * String[] inputWords = new String[] { "tennis_club",
         * "letter_bookannual_fee", "first_fee", "collected_letter",
         * "postal_address", "membership_fee", "postal_mail",
         * "incoming_mail_number", "membership_card", "birth_date",
         * "general_meeting", "commencement_date", "zip_code", "house_number",
         * "line_number", "new_member", "membership_number", "member_number",
         * "letter_book", "telephone_number", "bank_transfer", "firstname",
         * "maximum_number_of_member", "member_register", "street", "surname",
         * "register", "residence", "telephone", "letter", "applicant", "sex",
         * "mailbox", "name", "mail", "address", "administrator", "secretary",
         * "application", "fee", "member", "invoice"};
         */
        // inputWords = new String[] {"mail", "name", "address"};
        String[] inputWords = new String[]{"tennis_club"};
        Set<String> inputWordSet = new HashSet<>();
        Collections.addAll(inputWordSet, inputWords);
        SwoogleWebServiceWrapper.acquireRelevantOWLOntologies(inputWordSet, false);
    }
}
