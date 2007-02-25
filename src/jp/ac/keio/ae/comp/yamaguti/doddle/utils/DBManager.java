package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

import com.sleepycat.bind.*;
import com.sleepycat.bind.serial.*;
import com.sleepycat.bind.tuple.*;
import com.sleepycat.collections.*;
import com.sleepycat.je.*;

/**
 * @author takeshi morita
 */
public class DBManager {

    private EDRDatabase db;
    private EDRViews views;

    /**
     * Open the database and views.
     */
    public DBManager(boolean isReadOnly, String dicPath) throws DatabaseException {
        db = new EDRDatabase(dicPath, isReadOnly);
        views = new EDRViews(db);
    }

    private Concept concept;
    private String conceptURI;

    private Set<String> wordSet;
    private DODDLEProject project;

    private Set<InputWordModel> inputWordModelSet;
    private Set<String> undefinedWordSet;
    private Map<String, Set<Concept>> wordConceptSetMap;

    public Set<InputWordModel> getInputWordModelSet() {
        return inputWordModelSet;
    }

    public Map<String, Set<Concept>> getWordConceptSetMap() {
        return wordConceptSetMap;
    }

    public Set<String> getUndefinedWordSet() {
        return undefinedWordSet;
    }

    public void initDataWithDB(Set<String> iwSet, DODDLEProject p) {
        project = p;
        wordSet = iwSet;
        inputWordModelSet = new TreeSet<InputWordModel>();
        undefinedWordSet = new TreeSet<String>();
        wordConceptSetMap = new HashMap<String, Set<Concept>>();

        TransactionRunner runner = new TransactionRunner(db.getEnvironment());
        try {
            runner.run(new TransactionWorker() {
                public void doWork() throws Exception {
                    DODDLE.STATUS_BAR.startTime();
                    DODDLE.STATUS_BAR.initNormal(wordSet.size());

                    Set<String> matchedWordSet = new HashSet<String>();
                    for (String word : wordSet) {
                        InputWordModel iwModel = project.getDisambiguationPanel().makeInputWordModel(word,
                                wordConceptSetMap);
                        if (iwModel != null) {
                            inputWordModelSet.add(iwModel);
                            matchedWordSet.add(iwModel.getMatchedWord());
                        } else {
                            undefinedWordSet.add(word);
                        }
                        DODDLE.STATUS_BAR.addValue();
                    }
                    // 部分照合した複合語中で，完全照合単語リストに含まれない照合した単語を完全照合単語として追加
                    matchedWordSet.removeAll(wordSet);
                    for (String matchedWord : matchedWordSet) {
                        InputWordModel iwModel = project.makeInputWordModel(matchedWord, wordConceptSetMap);
                        if (iwModel != null) {
                            iwModel.setIsSystemAdded(true);
                            inputWordModelSet.add(iwModel);
                        }
                    }
                    DODDLE.STATUS_BAR.hideProgressBar();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map getVerbIDAgentIDSetMap() {
        return views.getVerbIDAgentIDSetMap();
    }

    public Map getVerbIDObjectIDSetMap() {
        return views.getVerbIDObjectIDSetMap();
    }

    public Set<String> getEDRIDSet(String subIW) {
        return (Set) views.getWordIDSetMap().get(subIW);
    }

    private InputWordModel setInputWord(String iw, DODDLEProject p) {
        InputWordModel iwModel = p.getDisambiguationPanel().makeInputWordModel(iw, wordConceptSetMap);
        if (iwModel != null) {
            inputWordModelSet.add(iwModel);
        } else {
            undefinedWordSet.add(iw);
        }
        return iwModel;
    }

    public void setEDRConcept(String uri) {
        concept = null;
        conceptURI = uri;
        TransactionRunner runner = new TransactionRunner(db.getEnvironment());
        try {
            runner.run(new TransactionWorker() {
                public void doWork() throws Exception {
                    concept = (Concept) views.getURIConceptMap().get(conceptURI);
                    concept.setURI(conceptURI);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Concept getEDRConcept() {
        return concept;
    }

    public void test(boolean isSpecial) throws Exception {
        TransactionRunner runner = new TransactionRunner(db.getEnvironment());
        runner.run(new EDRDatabaseTester(isSpecial));
        runner.run(new EDRConceptDefinitionDatabaseTester());
    }

    public void makeDB(String prefix, String dicPath, boolean isSpecial) throws Exception {
        TransactionRunner runner = new TransactionRunner(db.getEnvironment());
        runner.run(new EDRDatabaseMaker(prefix, dicPath, isSpecial));
        if (!isSpecial) {
            runner.run(new EDRConceptDefinitionDatabaseMaker(dicPath));
        }
    }

    private class EDRDatabaseTester implements TransactionWorker {

        boolean isSpecial;

        EDRDatabaseTester(boolean t) {
            isSpecial = t;
        }

        void testWordIDSetDBAccess() {
            Map wordIDSetMap = views.getWordIDSetMap();
            Set idSet = (Set) wordIDSetMap.get("概念");
            System.out.println(idSet);
            idSet = (Set) wordIDSetMap.get("起爆");
            System.out.println(idSet);
            idSet = (Set) wordIDSetMap.get("起動");
            System.out.println(idSet);
        }

        void testIDConceptDBAccess() {
            Map<String, Concept> idConceptMap = views.getURIConceptMap();
            Concept c = idConceptMap.get("3d02a7");
            System.out.println(c.getWord());
            c = idConceptMap.get("444d17");
            System.out.println(c.getWord());
            c = idConceptMap.get("0ebb6e");
            System.out.println(c.getWord());
        }

        void testEDRTIDConceptDBAccess() {
            Map<String, Concept> idConceptMap = views.getURIConceptMap();
            Concept c = idConceptMap.get("3cbda3");
            System.out.println(c.getWord());
            c = idConceptMap.get("3c84ef");
            System.out.println(c.getWord());
        }

        void testEDRTWordIDSetDBAccess() {
            Map wordIDSetMap = views.getWordIDSetMap();
            Set idSet = (Set) wordIDSetMap.get("デジタル通信技術");
            System.out.println(idSet);
            idSet = (Set) wordIDSetMap.get("デジタル論理回路検査");
            System.out.println(idSet);
        }

        public void doWork() throws Exception {
            if (isSpecial) {
                System.out.println("EDRT idDefinitionMap Test");
                testEDRTIDConceptDBAccess();
                System.out.println("EDRT wordIDSetMap Test");
                testEDRTWordIDSetDBAccess();
            } else {
                System.out.println("EDR idDefinitionMap Test");
                testIDConceptDBAccess();
                System.out.println("EDR wordIDSetMap Test");
                testWordIDSetDBAccess();
            }
        }
    }

    private class EDRConceptDefinitionDatabaseTester implements TransactionWorker {
        public void doWork() throws Exception {
            Map verbIDAgentIDSetMap = views.getVerbIDAgentIDSetMap();
            Map verbIDObjectIDSetMap = views.getVerbIDObjectIDSetMap();

            System.out.println("EDR conceptDefinition Test");
            Set aidSet = (Set) verbIDAgentIDSetMap.get("061c7d");
            System.out.println(aidSet);
            Set oidSet = (Set) verbIDObjectIDSetMap.get("061c7d");
            System.out.println(oidSet);
        }
    }

    private class EDRConceptDefinitionDatabaseMaker implements TransactionWorker {

        private String CONCEPT_DEFINITION = DODDLE.EDR_HOME + "conceptDefinitionforEDR.txt";

        public EDRConceptDefinitionDatabaseMaker(String dicPath) {
            CONCEPT_DEFINITION = dicPath + "conceptDefinitionforEDR.txt";
        }

        private Set makeIDSet(String[] idArray) {
            Set idSet = new HashSet();
            if (1 < idArray.length) {
                for (int i = 1; i < idArray.length; i++) {
                    String id = idArray[i];
                    idSet.add(id);
                }
            }
            return idSet;
        }

        public void makeVerbIDRelationIDSetDB() {
            System.out.println("Make VerbIDAgentIDSetDB Start");
            BufferedReader reader = null;
            try {
                InputStream inputStream = new FileInputStream(CONCEPT_DEFINITION);
                reader = new BufferedReader(new InputStreamReader(inputStream, "Shift_JIS"));
                Map verbIDAgentIDSetMap = views.getVerbIDAgentIDSetMap();
                Map verbIDObjectIDSetMap = views.getVerbIDObjectIDSetMap();
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] lines = line.replaceAll("\n", "").split("\\|");
                    String verbID = lines[0];
                    verbIDAgentIDSetMap.put(verbID, makeIDSet(lines[1].split("\t")));
                    verbIDObjectIDSetMap.put(verbID, makeIDSet(lines[2].split("\t")));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
            System.out.println("Make VerbIDAgentIDSetDB Done");
        }

        public void doWork() throws Exception {
            EDR2DoddleDicConverterUI.setProgressText("Make VerbIDAgentIDSetDB");
            makeVerbIDRelationIDSetDB();
            EDR2DoddleDicConverterUI.addProgressValue();
        }
    }

    private class EDRDatabaseMaker implements TransactionWorker {

        private String prefix;
        private boolean isSpecial;
        private String ID_DEFINITION_MAP = "C:/DODDLE_DIC/idDefinitionMapforEDR.txt";
        private String WORD_IDSet_MAP = "C:/DODDLE_DIC/wordIDSetMapforEDR.txt";
        private String EDRT_ID_DEFINITION_MAP = "C:/DODDLE_EDRT_DIC/idDefinitionMapforEDR.txt";
        private String EDRT_WORD_IDSet_MAP = "C:/DODDLE_EDRT_DIC/wordIDSetMapforEDR.txt";

        EDRDatabaseMaker(String prefix, String dicPath, boolean isSpecial) {
            this.prefix = prefix;
            this.isSpecial = isSpecial;
            ID_DEFINITION_MAP = dicPath + "idDefinitionMapforEDR.txt";
            WORD_IDSet_MAP = dicPath + "wordIDSetMapforEDR.txt";
            EDRT_ID_DEFINITION_MAP = ID_DEFINITION_MAP;
            EDRT_WORD_IDSet_MAP = WORD_IDSet_MAP;
        }

        public void makeWordIDSetDB(String path) {
            System.out.println("Make WordIDSetDB Start");
            BufferedReader reader = null;
            try {
                InputStream inputStream = new FileInputStream(path);
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));

                String line = reader.readLine().replaceAll("\n", "");
                String[] allWordList = line.split("\t");
                line = reader.readLine().replaceAll("\n", "");
                String[] wordIDSetList = line.split("\\|");

                System.out.println("all word size: " + allWordList.length);
                System.out.println("word IDSet size: " + wordIDSetList.length);
                Map wordIDSetMap = views.getWordIDSetMap();
                for (int i = 0; i < allWordList.length; i++) {
                    if (allWordList[i].replaceAll("\\s*", "").length() == 0) {
                        System.out.println("空白文字: " + allWordList[i]);
                        continue;
                    }
                    String[] idListArray = wordIDSetList[i].replaceAll("\n", "").split("\\s");
                    wordIDSetMap.put(allWordList[i], new HashSet(Arrays.asList(idListArray)));
                    // wordIDSetMap.put(allWordList[i], wordIDSetList[i]);
                    if (i % 10000 == 0) {
                        System.out.println(i + "/" + allWordList.length);
                        EDR2DoddleDicConverterUI.setProgressText("Make WordIDSetDB" + ": " + i + "/"
                                + allWordList.length);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
            System.out.println("Make WordIDSetDB Done");
        }

        void makeURIConceptDB(String path, boolean isSpecial) {
            System.out.println("Make uriConceptDB Start");
            BufferedReader reader = null;
            try {
                InputStream inputStream = new FileInputStream(path);
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF16"));

                String line = reader.readLine().replaceAll("\n", "");
                String[] allIDList = line.split("\\|");
                System.out.println("id size: " + allIDList.length);
                line = reader.readLine().replaceAll("\n", "");
                String[] definitionList = line.split("\"");
                System.out.println("definition list size: " + definitionList.length);

                Map uriConceptMap = views.getURIConceptMap();
                for (int i = 0; i < allIDList.length; i++) {
                    String id = allIDList[i];
                    // System.out.println("id: "+id+"def: "+definitionList[i]);
                    String nameSpace = "";
                    if (isSpecial) {
                        nameSpace = DODDLE.EDRT_URI;
                    } else {
                        nameSpace = DODDLE.EDR_URI;
                    }
                    String uri = nameSpace + id;
                    Concept edrConcept = new Concept(uri, definitionList[i].split("\\^"));
                    uriConceptMap.put(uri, edrConcept); // データベースに追加
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
            System.out.println("Make idConceptDB Done");
        }

        public void doWork() {
            if (isSpecial) {
                EDR2DoddleDicConverterUI.setProgressText("Make idConceptDB");
                makeURIConceptDB(EDRT_ID_DEFINITION_MAP, isSpecial);
                EDR2DoddleDicConverterUI.addProgressValue();
                EDR2DoddleDicConverterUI.setProgressText("Make WordIDSetDB");
                makeWordIDSetDB(EDRT_WORD_IDSet_MAP);
                EDR2DoddleDicConverterUI.addProgressValue();
            } else {
                EDR2DoddleDicConverterUI.setProgressText("Make idConceptDB");
                makeURIConceptDB(ID_DEFINITION_MAP, isSpecial);
                EDR2DoddleDicConverterUI.addProgressValue();
                EDR2DoddleDicConverterUI.setProgressText("Make WordIDSetDB");
                makeWordIDSetDB(WORD_IDSet_MAP);
                EDR2DoddleDicConverterUI.addProgressValue();
            }
        }
    }

    public void close() throws DatabaseException {
        db.close();
    }

    public static void main(String[] args) {
        DBManager edrDBManager = null;
        DBManager edrtDBManager = null;
        try {

            DODDLE.setPath();
            edrDBManager = new DBManager(false, DODDLE.EDR_HOME);
            edrtDBManager = new DBManager(false, DODDLE.EDRT_HOME);
            if (args.length == 1 && args[0].equals("-makeDB")) {
                edrDBManager.makeDB("edr", "C:/DODDLE_DIC/", false);
                edrtDBManager.makeDB("edrt", "C:/DODDLE_EDRT_DIC/", true);
            } else if (args.length == 1 && args[0].equals("-test")) {
                edrDBManager.test(false);
                edrtDBManager.test(true);
            }
        } catch (Exception e) {
            // If an exception reaches this point, the last transaction did not
            // complete. If the exception is RunRecoveryException, follow
            // the Berkeley DB recovery procedures before running again.
            e.printStackTrace();
        } finally {
            if (edrDBManager != null || edrtDBManager != null) {
                try {
                    // Always attempt to close the database cleanly.
                    edrDBManager.close();
                    edrtDBManager.close();
                    System.out.println("Close DB");
                } catch (Exception e) {
                    System.err.println("Exception during database close:");
                    e.printStackTrace();
                }
            }
        }
    }

    public class EDRDatabase {

        private static final String CLASS_CATALOG = "java_class_catalog";
        private static final String ID_WORD_MAP = "id_word_map.db";
        private static final String WORD_IDs_MAP = "word_ids_map.db";
        private static final String VERBID_AGENT_IDSET_MAP = "verbid_agent_idset_map.db";
        private static final String VERBID_OBJECT_IDSET_MAP = "verbid_object_idset_map.db";

        private Environment env;
        private Database idConceptDb;
        private Database wordIDsDb;
        private Database verbIDAgentIDSetDb;
        private Database verbIDObjectIDSetDb;
        private StoredClassCatalog javaCatalog;

        /**
         * Open all storage containers, indices, and catalogs.
         */
        public EDRDatabase(String homeDirectory, boolean isReadOnly) throws DatabaseException {
            System.out.println("Opening environment in: " + homeDirectory);
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setTransactional(true);
            envConfig.setAllowCreate(true);
            env = new Environment(new File(homeDirectory), envConfig);

            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setTransactional(true);
            dbConfig.setAllowCreate(true);
            dbConfig.setReadOnly(isReadOnly);

            Database catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
            javaCatalog = new StoredClassCatalog(catalogDb);

            idConceptDb = env.openDatabase(null, ID_WORD_MAP, dbConfig);
            wordIDsDb = env.openDatabase(null, WORD_IDs_MAP, dbConfig);
            verbIDAgentIDSetDb = env.openDatabase(null, VERBID_AGENT_IDSET_MAP, dbConfig);
            verbIDObjectIDSetDb = env.openDatabase(null, VERBID_OBJECT_IDSET_MAP, dbConfig);
        }

        /**
         * Return the storage environment for the database.
         */
        public final Environment getEnvironment() {
            return env;
        }

        /**
         * Return the class catalog.
         */
        public final StoredClassCatalog getClassCatalog() {
            return javaCatalog;
        }

        public final Database getIDConceptDatabase() {
            return idConceptDb;
        }

        public final Database getWordIDsDatabase() {
            return wordIDsDb;
        }

        public final Database getVerbIDAgentIDSetDatabase() {
            return verbIDAgentIDSetDb;
        }

        public final Database getVerbIDObjectIDSetDatabase() {
            return verbIDObjectIDSetDb;
        }

        public void close() throws DatabaseException {
            idConceptDb.close();
            wordIDsDb.close();
            verbIDAgentIDSetDb.close();
            verbIDObjectIDSetDb.close();
            javaCatalog.close();
            env.close();
        }
    }

    public class EDRViews {

        private StoredSortedMap uriConceptMap;
        private StoredSortedMap wordIDSetMap;
        private StoredSortedMap verbIDAgentIDSetMap;
        private StoredSortedMap verbIDObjectIDSetMap;

        /**
         * Create the data bindings and collection views.
         */
        public EDRViews(EDRDatabase db) {
            ClassCatalog catalog = db.getClassCatalog();
            EntryBinding idConceptDataBinding = new SerialBinding(catalog, Concept.class);
            EntryBinding stringBinding = TupleBinding.getPrimitiveBinding(String.class);
            EntryBinding setBinding = new SerialBinding(catalog, Set.class);

            uriConceptMap = new StoredSortedMap(db.getIDConceptDatabase(), stringBinding, idConceptDataBinding, true);
            wordIDSetMap = new StoredSortedMap(db.getWordIDsDatabase(), stringBinding, setBinding, true);
            verbIDAgentIDSetMap = new StoredSortedMap(db.getVerbIDAgentIDSetDatabase(), stringBinding, setBinding, true);
            verbIDObjectIDSetMap = new StoredSortedMap(db.getVerbIDObjectIDSetDatabase(), stringBinding, setBinding,
                    true);
        }

        public final StoredSortedMap getURIConceptMap() {
            return uriConceptMap;
        }

        public final StoredSortedMap getWordIDSetMap() {
            return wordIDSetMap;
        }

        public final StoredSortedMap getVerbIDAgentIDSetMap() {
            return verbIDAgentIDSetMap;
        }

        public final StoredSortedMap getVerbIDObjectIDSetMap() {
            return verbIDObjectIDSetMap;
        }
    }
}
