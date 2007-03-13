/*
 * @(#)  2007/03/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.net.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class SwoogleWebServiceWrapper {

    private static SwoogleWebServiceData swoogleWebServiceData = new SwoogleWebServiceData();

    public static String SWOOGLE_QUERY_RESULTS_DIR = "C:/DODDLE-OWL/swoogle_query_results/";
    public static String OWL_ONTOLOGIES_DIR = "C:/DODDLE-OWL/owl_ontologies/";

    private static final String ONTOLOGY_URL = "ontology_url";
    private static final String PROPERTY = "property";
    private static final String CLASS = "class";
    private static final String ENCODING = "encoding";
    private static final String RDF_TYPE = "rdf_type";

    public static final String RESOURCE_DIR = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";

    /**
     * 保存されているSwoogleクエリー結果からswoogleWebServiceDataのuriSwoogleOWLMetaDataMapにOWLメタデータを格納する
     */
    public static void initSwoogleOWLMetaData() {
        QueryExecution qexec = null;
        try {
            File swoogleQueryResultsDir = new File(SWOOGLE_QUERY_RESULTS_DIR);
            File[] files = swoogleQueryResultsDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File queryResultFile = files[i];
                if (queryResultFile.getName().indexOf("queryType%3Dsearch_swd_ontology") != -1
                        || queryResultFile.getName().indexOf("queryType%3Ddigest_swd") != -1) {
                    Model model = getModel(new FileInputStream(queryResultFile));
                    String sparqlQueryString = SPARQLQueryUtil.getQueryString(getSearchOntologyQuery());
                    Query query = QueryFactory.create(sparqlQueryString);
                    qexec = QueryExecutionFactory.create(query, model);
                    ResultSet results = qexec.execSelect();
                    while (results.hasNext()) {
                        QuerySolution qs = results.nextSolution();
                        saveOntology(qs);
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

    private static InputStream getSearchOntologyQuery() {
        return DODDLE.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + "swoogle_queries/SearchOntology.rq");
    }

    private static InputStream getListDocumentsUsingTermQuery() {
        return DODDLE.class.getClassLoader().getResourceAsStream(
                RESOURCE_DIR + "swoogle_queries/listDocumentsUsingTerm.rq");
    }

    private static InputStream getListPropertiesOfaRegionClassQuery() {
        return DODDLE.class.getClassLoader().getResourceAsStream(
                RESOURCE_DIR + "swoogle_queries/listPropertiesOfaRegionClass.rq");
    }

    private static InputStream getListRegionClassesOfaPropertyQuery() {
        return DODDLE.class.getClassLoader().getResourceAsStream(
                RESOURCE_DIR + "swoogle_queries/listRegionClassesOfaProperty.rq");
    }

    private static Model getModel(InputStream inputStream) {
        Model model = ModelFactory.createDefaultModel();
        model.read(inputStream, DODDLE.BASE_URI, "RDF/XML");
        return model;
    }

    private static void saveFile(File file, InputStream inputStream, String encoding) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
            reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
            while (reader.ready()) {
                String line = reader.readLine();
                writer.write(line);
                writer.write("\n");
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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

    private static void saveOntology(File file, InputStream inputStream, String encoding) {
        saveFile(file, inputStream, encoding);
    }

    private static void saveQueryResult(File file, InputStream inputStream) {
        saveFile(file, inputStream, "UTF-8");
    }

    private static Model getSwoogleQueryResultModel(String restQuery) {
        Model model = null;
        try {
            String encodedRestQuery = URLEncoder.encode(restQuery, "UTF-8");
            System.out.println(encodedRestQuery);
            if (!SWOOGLE_QUERY_RESULTS_DIR.endsWith(File.separator)) {
                SWOOGLE_QUERY_RESULTS_DIR += File.separator;
            }
            File queryCachFile = new File(SWOOGLE_QUERY_RESULTS_DIR + encodedRestQuery);
            if (queryCachFile.exists()) {
                System.out.println("cach");
                model = getModel(new FileInputStream(queryCachFile));
            } else {
                URL url = new URL(restQuery);
                saveQueryResult(queryCachFile, url.openStream());
                model = getModel(url.openStream());
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (FileNotFoundException fne) {
            fne.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return model;
    }

    private static void saveOntology(QuerySolution qs) {
        try {
            Resource ontologyURL = (Resource) qs.get(ONTOLOGY_URL);
            Literal encoding = (Literal) qs.get(ENCODING);
            Resource rdfType = (Resource) qs.get(RDF_TYPE);
            SwoogleOWLMetaData owlMetaData = new SwoogleOWLMetaData(ontologyURL.getURI(), encoding.getString(), rdfType
                    .getURI());
            swoogleWebServiceData.putSwoogleOWLMetaData(ontologyURL.getURI(), owlMetaData);
            URL ontURL = new URL(ontologyURL.getURI());
            if (!OWL_ONTOLOGIES_DIR.endsWith(File.separator)) {
                OWL_ONTOLOGIES_DIR += File.separator;
            }
            File ontFile = new File(OWL_ONTOLOGIES_DIR + owlMetaData.getEncodedURL());
            System.out.println(ontologyURL);
            if (!ontFile.exists()) {
                System.out.println("save ontology: " + ontologyURL);
                try {
                    saveOntology(ontFile, ontURL.openStream(), owlMetaData.getFileEncoding());
                } catch (Exception e) {
                    System.err.println("ignore exception !!");
                }
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
    }

    /*
     * 獲得したオントロジーの中から入力単語に関連するクラス及びプロパティを抽出する
     */
    private static void getSWTSet() {
        // 未実装
    }

    public static void searchOntology(String inputWord) {
        QueryExecution qexec = null;
        try {
            String restQuery = "http://logos.cs.umbc.edu:8080/swoogle31/q?queryType=search_swd_ontology&searchString=def:"
                    + inputWord + "&key=demo";
            Model model = getSwoogleQueryResultModel(restQuery);
            String sparqlQueryString = SPARQLQueryUtil.getQueryString(getSearchOntologyQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                saveOntology(qs);
            }
            getSWTSet();
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    private static void searchListPropertiesOfaRegionClass(String restQuery) {
        QueryExecution qexec = null;
        try {
            System.out.println(restQuery);
            Model model = getSwoogleQueryResultModel(restQuery);
            System.out.println(model.size());
            String sparqlQueryString = SPARQLQueryUtil.getQueryString(getListPropertiesOfaRegionClassQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource property = (Resource) qs.get(PROPERTY);
                System.out.println(property);
                swoogleWebServiceData.addProperty(property);
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public static void searchListPropertiesOfaDomainClass(String encodedDomainURI) {
        try {
            String restQuery = "http://logos.cs.umbc.edu:8080/swoogle31/q?queryType=rel_swd_instance_domain_c2p&searchString="
                    + URLEncoder.encode(encodedDomainURI, "UTF-8") + "&key=demo";
            searchListPropertiesOfaRegionClass(restQuery);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    public static void searchListPropertiesOfaRangeClass(String encodedRangeURI) {
        try {
            String restQuery = "http://logos.cs.umbc.edu:8080/swoogle31/q?queryType=rel_swd_instance_range_c2p&searchString="
                    + URLEncoder.encode(encodedRangeURI, "UTF-8") + "&key=demo";
            searchListPropertiesOfaRegionClass(restQuery);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    private static void searchListRegionClassOfaProperty(String restQuery) {
        QueryExecution qexec = null;
        try {
            System.out.println(restQuery);
            Model model = getSwoogleQueryResultModel(restQuery);
            System.out.println(model.size());
            String sparqlQueryString = SPARQLQueryUtil.getQueryString(getListRegionClassesOfaPropertyQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource classResource = (Resource) qs.get(CLASS);
                System.out.println(classResource);
                swoogleWebServiceData.addClass(classResource);
            }
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public static void searchListDomainClassOfaProperty(String propertyURI) {
        try {
            String restQuery = "http://logos.cs.umbc.edu:8080/swoogle31/q?queryType=rel_swd_instance_domain_p2c&searchString="
                    + URLEncoder.encode(propertyURI, "UTF-8") + "&key=demo";
            searchListRegionClassOfaProperty(restQuery);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    public static void searchListRangeClassOfaProperty(String propertyURI) {
        try {
            String restQuery = "http://logos.cs.umbc.edu:8080/swoogle31/q?queryType=rel_swd_instance_range_p2c&searchString="
                    + URLEncoder.encode(propertyURI, "UTF-8") + "&key=demo";
            searchListRegionClassOfaProperty(restQuery);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    public static void searchListDocumentsUsingTerm(String swtURI) {
        QueryExecution qexec = null;
        try {
            String restQuery = "http://logos.cs.umbc.edu:8080/swoogle31/q?queryType=rel_swt_swd&searchString="
                    + URLEncoder.encode(swtURI, "UTF-8") + "&key=demo";
            System.out.println(restQuery);
            Model model = getSwoogleQueryResultModel(restQuery);
            System.out.println(model.size());
            String sparqlQueryString = SPARQLQueryUtil.getQueryString(getListDocumentsUsingTermQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource ontologyURL = (Resource) qs.get(ONTOLOGY_URL);
                System.out.println(ontologyURL);
                SwoogleOWLMetaData owlMetaData = swoogleWebServiceData.getSwoogleOWLMetaData(ontologyURL.getURI());
                if (owlMetaData == null) {
                    System.out.println("search digest semantic web document");
                    searchDigestSemanticWebDocument(ontologyURL.getURI());
                }
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public static void searchDigestSemanticWebDocument(String swdURI) {
        QueryExecution qexec = null;
        try {
            String restQuery = "http://logos.cs.umbc.edu:8080/swoogle31/q?queryType=digest_swd&searchString="
                    + URLEncoder.encode(swdURI, "UTF-8") + "&key=demo";
            System.out.println(restQuery);
            Model model = getSwoogleQueryResultModel(restQuery);
            System.out.println(model.size());
            String sparqlQueryString = SPARQLQueryUtil.getQueryString(getSearchOntologyQuery());
            Query query = QueryFactory.create(sparqlQueryString);
            qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                saveOntology(qs);
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    public static void main(String[] args) {
        // SwoogleWebServiceWrapper.searchOntology("business");
        // System.out.println("domain");
        // SwoogleWebServiceWrapper
        // .searchListPropertiesOfaDomainClass("http://makna.ag-nbi.de/test-ontologies/imdb-ontology#Writer");
        // SwoogleWebServiceWrapper
        // .searchListDomainClassOfaProperty("http://makna.ag-nbi.de/test-ontologies/imdb-ontology#writes");
        // System.out.println("range");
        // SwoogleWebServiceWrapper
        // .searchListPropertiesOfaRangeClass("http://makna.ag-nbi.de/test-ontologies/imdb-ontology#Writer");
        // SwoogleWebServiceWrapper
        // .searchListRangeClassOfaProperty("http://makna.ag-nbi.de/test-ontologies/imdb-ontology#writes");
        // SwoogleWebServiceWrapper
        // .searchListDocumentsUsingTerm("http://makna.ag-nbi.de/test-ontologies/imdb-ontology#Movie");
        SwoogleWebServiceWrapper.initSwoogleOWLMetaData();
    }
}
