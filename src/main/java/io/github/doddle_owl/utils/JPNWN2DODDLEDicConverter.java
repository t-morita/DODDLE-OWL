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

import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.views.DODDLEDicConverterUI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Takeshi Morita
 */
public class JPNWN2DODDLEDicConverter {

    private static final String WORD_DATA = "word.data";
    private static final String WORD_INDEX = "word.index";
    private static final String CONCEPT_DATA = "concept.data";
    private static final String CONCEPT_INDEX = "concept.index";
    private static final String RELATION_DATA = "relation.data";
    private static final String RELATION_INDEX = "relation.index";
    private static final String TREE_DATA = "tree.data";
    private static final String TREE_INDEX = "tree.index";
    public static String HASA_DATA = "has-a.data";
    public static String HASA_INDEX = "has-a.index";

    private static String DODDLE_DIC_HOME = "C:/DODDLE-OWL/JPNWN_DIC/";
    private static String JPNWN_PATH = "C:/DODDLE-OWL/wnjpn-0.9_addindex.db";

    private static TreeModel jpnwnTreeModel;
    private static final Map<String, Set<String>> idSubIDSetMap = new HashMap<>();
    private static final Map<String, Set<String>> idSupIDSetMap = new HashMap<>();
    private static final TreeMap<String, Set<TreeNode>> idNodeSetMap = new TreeMap<>();

    private static TreeSet<String> relationConceptIDSet = new TreeSet<>();

    private static final List<Long> dataFilePointerList = new ArrayList<>();
    private static final Map<String, Long> idFilePointerMap = new HashMap<>();
    private static final TreeMap<String, Concept> idDefinitionMap = new TreeMap<>();
    private static final TreeMap<String, Set<String>> wordIDSetMap = new TreeMap<>();
    private static final TreeMap<String, Set<Long>> wordFilePointerSetMap = new TreeMap<>();

    private static final Map<String, Set<String>> agentMap = new HashMap<>();
    private static final Map<String, Set<String>> objectMap = new HashMap<>();

    private static java.sql.Statement stmt;

    public static boolean initJPNWNDB() {
        Connection connection;
        try {
            // load the sqlite-JDBC driver using the current class loader
            Class.forName("org.sqlite.JDBC");
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + JPNWN_PATH);
            stmt = connection.createStatement();
            stmt.setQueryTimeout(30); // set timeout to 30 sec.

            createIndex();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     * 以下のページを参考にさせていただきました． 理系大学院留学日記:
     * http://w-it.jp/shima/2009/03/wordnet_java_api.html
     */
    private static void createIndex() {
        try {
            stmt.execute("DROP INDEX sense_idx;");
            System.out.println("CREATE INDEX sense_synset_idx ON sense (synset);");
            DODDLEDicConverterUI.setProgressText("CREATE INDEX sense_synset_idx ON sense (synset);");
            stmt.execute("CREATE INDEX sense_synset_idx ON sense (synset);");
            System.out.println("CREATE INDEX sense_wordid_idx ON sense (wordid);");
            DODDLEDicConverterUI.setProgressText("CREATE INDEX sense_wordid_idx ON sense (wordid);");
            stmt.execute("CREATE INDEX sense_wordid_idx ON sense (wordid);");
            System.out.println("CREATE INDEX synset_id_idx ON synset (synset);");
            DODDLEDicConverterUI.setProgressText("CREATE INDEX synset_id_idx ON synset (synset);");
            stmt.execute("CREATE INDEX synset_id_idx ON synset (synset);");
            System.out.println("CREATE INDEX synset_def_id_idx ON synset_def (synset);");
            DODDLEDicConverterUI.setProgressText("CREATE INDEX synset_def_id_idx ON synset_def (synset);");
            stmt.execute("CREATE INDEX synset_def_id_idx ON synset_def (synset);");
        } catch (SQLException sqle) {
            // sqle.printStackTrace();
            System.out.println("SKIP: CREATE INDEX");
            DODDLEDicConverterUI.setProgressText("SKIP: CREATE INDEX");
        }
    }

    public static void setJPNWNPath(String path) {
        JPNWN_PATH = path;
    }

    public static void clearRelationMaps() {
        agentMap.clear();
        objectMap.clear();
    }

    public static void clearIDDefinitionMap() {
        idDefinitionMap.clear();
    }

    public static void clearWordIDSetMap() {
        wordIDSetMap.clear();
    }

    public static void clearIDFilePointerMap() {
        idFilePointerMap.clear();
    }

    public static void clearDataFilePointerList() {
        dataFilePointerList.clear();
    }

    public static void clearWordFilePointerSetMap() {
        wordFilePointerSetMap.clear();
    }

    public static void clearTreeData() {
        idNodeSetMap.clear();
        idSubIDSetMap.clear();
        idSupIDSetMap.clear();
        jpnwnTreeModel = null;
    }

    public static void setDODDLEDicPath(String dirName) {
        DODDLE_DIC_HOME = dirName;
    }

    private static void setIDSubIDSetMap(String id, String subID) {
        if (idSubIDSetMap.get(id) != null) {
            Set<String> subIDSet = idSubIDSetMap.get(id);
            subIDSet.add(subID);
            idSubIDSetMap.put(id, subIDSet);
        } else {
            Set<String> subIDSet = new HashSet<>();
            subIDSet.add(subID);
            idSubIDSetMap.put(id, subIDSet);
        }
    }

    private static void setIDSupIDSetMap(String id, String supID) {
        if (idSupIDSetMap.get(id) != null) {
            Set<String> supIDSet = idSupIDSetMap.get(id);
            supIDSet.add(supID);
            idSupIDSetMap.put(id, supIDSet);
        } else {
            Set<String> supIDSet = new HashSet<>();
            supIDSet.add(supID);
            idSupIDSetMap.put(id, supIDSet);
        }
    }

    public static void readTreeData(String rootID) {
        BufferedReader reader = null;
        try {
            System.out.println("Make Tree Data");
            DODDLEDicConverterUI.setProgressText("Make Tree Data");
            ResultSet rs = stmt.executeQuery("select * from synlink where link='hype'");
            while (rs.next()) {
                String id = rs.getString("synset2");
                String subID = rs.getString("synset1");
                setIDSubIDSetMap(id, subID);
                setIDSupIDSetMap(subID, id);
            }
            Set<String> rootIDSet = new HashSet<>();
            for (String id : idSubIDSetMap.keySet()) {
                if (idSupIDSetMap.get(id) == null) {
                    rootIDSet.add(id);
                }
            }
            idSubIDSetMap.put(rootID, rootIDSet);
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootID);
            Set<TreeNode> nodeSet = new HashSet<>();
            nodeSet.add(rootNode);
            idNodeSetMap.put(rootID, nodeSet);
            makeJPNWNTree(rootID, rootNode);
            jpnwnTreeModel = new DefaultTreeModel(rootNode);
        } catch (Exception e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    private static void makeJPNWNTree(String id, DefaultMutableTreeNode node) {
        for (String subID : idSubIDSetMap.get(id)) {
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subID);
            if (idNodeSetMap.get(subID) == null) {
                Set<TreeNode> nodeSet = new HashSet<>();
                nodeSet.add(subNode);
                idNodeSetMap.put(subID, nodeSet);
            } else {
                Set<TreeNode> nodeSet = idNodeSetMap.get(subID);
                nodeSet.add(subNode);
            }
            if (idSubIDSetMap.get(subID) != null) {
                makeJPNWNTree(subID, subNode);
            }
            node.add(subNode);
        }
    }

    private static Set<List<String>> getIDPathToRootSet(String id) {
        Set<TreeNode> nodeSet = idNodeSetMap.get(id);
        Set<List<String>> pathToRootSet = new HashSet<>();
        if (nodeSet == null) { // 上位・下位関係が定義されていない（できない）概念
            pathToRootSet.add(Collections.singletonList(id));
            return pathToRootSet;
        }
        for (TreeNode node : nodeSet) {
            TreeNode[] pathToRoot = ((DefaultTreeModel) jpnwnTreeModel).getPathToRoot(node);
            List<String> path = new ArrayList<>();
            for (TreeNode treeNode : pathToRoot) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) treeNode;
                String nid = (String) n.getUserObject();
                path.add(nid);
            }
            pathToRootSet.add(path);
        }
        return pathToRootSet;
    }

    public static void writeTreeData() {
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + TREE_DATA);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "ISO8859_1"));
            for (String id : idNodeSetMap.keySet()) {
                writer.write(id);
                Set<List<String>> pathSet = getIDPathToRootSet(id);
                for (List<String> path : pathSet) {
                    writer.write("\t|");
                    for (String sid : path) {
                        writer.write(sid);
                        writer.write("\t");
                    }
                }
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void writeTreeIndex() {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + TREE_INDEX);
            writer = new BufferedWriter(new OutputStreamWriter(os, "ISO8859_1"));

            int i = 0;
            DecimalFormat df = new DecimalFormat("00000000");
            RandomAccessFile raf = new RandomAccessFile(DODDLE_DIC_HOME + TREE_DATA, "r");
            while (raf.readLine() != null) {
                writer.write(df.format(raf.getFilePointer()));
                writer.newLine();
                i++;
                if (i % 10000 == 0) {
                    System.out.println("Make Tree Index: " + i);
                    DODDLEDicConverterUI.setProgressText("Make Tree Index: " + i);
                }
            }
            raf.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(ioe.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void readConceptIndex() {
        try {
            int i = 0;
            RandomAccessFile raf = new RandomAccessFile(DODDLE_DIC_HOME + CONCEPT_DATA, "r");
            String line;
            long dfp = 0;
            while ((line = raf.readLine()) != null) {
                line = new String(line.getBytes("ISO8859_1"), StandardCharsets.UTF_8);
                String id = line.split("\t\\^")[0];
                // System.out.println(line);
                // System.out.println(id + ":" + raf.getFilePointer());
                idFilePointerMap.put(id, dfp);
                dataFilePointerList.add(dfp);
                dfp = raf.getFilePointer();
                i++;
                if (i % 10000 == 0) {
                    System.out.println("Make Concept Index: " + i);
                    DODDLEDicConverterUI.setProgressText("Make Concept Index: " + i);
                }
            }
            raf.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(ioe.getMessage());
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void writeConceptIndex() {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + CONCEPT_INDEX);
            writer = new BufferedWriter(new OutputStreamWriter(os, "ISO8859_1"));

            for (long dfp : dataFilePointerList) {
                DecimalFormat df = new DecimalFormat("00000000");
                writer.write(df.format(dfp));
                writer.newLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(ioe.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void readConceptData() {
        try {
            System.out.println("Make Concept Data");
            DODDLEDicConverterUI.setProgressText("Make Concept Data");
            List<String> synsetList = new ArrayList<>();
            ResultSet rs = stmt.executeQuery("select * from synset");
            while (rs.next()) {
                String pos = rs.getString("pos");
                if (pos.equals("n") || pos.equals("v")) {
                    synsetList.add(rs.getString("synset"));
                }
            }
            int n = 0;
            for (String synset : synsetList) {
                Concept c = new Concept(synset, "");
                if (n % 10000 == 0) {
                    System.out.println(n + ": " + Calendar.getInstance().getTime());
                    DODDLEDicConverterUI.setProgressText(n + ": " + Calendar.getInstance().getTime());
                }
                n++;
                rs = stmt.executeQuery("select * from synset_def where synset='" + synset + "'");
                while (rs.next()) {
                    String def = rs.getString("def");
                    String lang = rs.getString("lang");
                    if (lang.equals("eng")) {
                        c.addDescription(new DODDLELiteral("en", def));
                    } else if (lang.equals("jpn")) {
                        c.addDescription(new DODDLELiteral("ja", def));
                    }
                }

                List<String> wordIDList = new ArrayList<>();
                rs = stmt.executeQuery("select * from sense where synset='" + synset + "'");
                while (rs.next()) {
                    wordIDList.add(rs.getString("wordid"));
                }
                for (String wid : wordIDList) {
                    rs = stmt.executeQuery("select * from word where wordid='" + wid + "'");
                    while (rs.next()) {
                        String lemma = rs.getString("lemma");
                        String lang = rs.getString("lang");
                        if (lang.equals("eng")) {
                            c.addLabel(new DODDLELiteral("en", lemma));
                        } else if (lang.equals("jpn")) {
                            c.addLabel(new DODDLELiteral("ja", lemma));
                        }
                    }
                }
                idDefinitionMap.put(synset, c);
            }
            addJPNWNRootConcept();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    private static void addJPNWNRootConcept() {
        String rootID = ConceptTreeMaker.JPNWN_CLASS_ROOT_ID;
        Concept c = new Concept(rootID, "");
        c.addLabel(new DODDLELiteral("en", "JPNWN Root Concept"));
        c.addLabel(new DODDLELiteral("ja", "JPNWN ルート概念"));
        c.addDescription(new DODDLELiteral("en", "Japanese WordNet Root Concept"));
        c.addDescription(new DODDLELiteral("ja", "日本語WordNetのルート概念"));
        idDefinitionMap.put(rootID, c);
    }

    public static void main(String[] args) {
        initJPNWNDB();
        readConceptData();
        writeConceptData();
        readConceptIndex();
        writeConceptIndex();
        readWordData();
        writeWordData();
        writeWordIndex();
        readTreeData(ConceptTreeMaker.JPNWN_CLASS_ROOT_ID);
        writeTreeData();
        writeTreeIndex();
    }

    public static void writeConceptData() {
        try {
            Path path = Paths.get(DODDLE_DIC_HOME + CONCEPT_DATA);
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            System.out.println("Make Concept Data: Writing concept.data");
            DODDLEDicConverterUI.setProgressText("Make Concept Data: Writing concept.data");
            try (writer) {
                for (Entry<String, Concept> entry : idDefinitionMap.entrySet()) {
                    String id = entry.getKey();
                    Concept concept = entry.getValue();
                    writer.write(id);
                    writer.write("\t^");
                    Map<String, List<DODDLELiteral>> langLabelListMap = concept.getLangLabelListMap();
                    writerLiteralString(writer, langLabelListMap.get("ja"));
                    writerLiteralString(writer, langLabelListMap.get("en"));
                    Map<String, List<DODDLELiteral>> langDescriptionListMap = concept.getLangDescriptionListMap();
                    writerLiteralString(writer, langDescriptionListMap.get("ja"));
                    writerLiteralString(writer, langDescriptionListMap.get("en"));
                    writer.newLine();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } catch (IOException uee) {
            uee.printStackTrace();
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    private static void writerLiteralString(Writer writer, List<DODDLELiteral> labelList) {
        try {
            if (labelList != null) {
                for (DODDLELiteral label : labelList) {
                    writer.write(label.getString());
                    writer.write("\t");
                }
                writer.write("^");
            } else {
                writer.write("\t^");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(ioe.getMessage());
        }
    }

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static void writeOWLConceptData(Model ontModel, String ns) {
        System.out.println("Writing OWL Concept Data");
        DODDLEDicConverterUI.setProgressText("Writing OWL Concept Data");
        int i = 0;
        for (Entry<String, Concept> entry : idDefinitionMap.entrySet()) {
            String id = entry.getKey();
            Concept concept = entry.getValue();
            Resource uri = ResourceFactory.createResource(ns + id);
            ontModel.add(uri, RDF.type, OWL.Class);
            Map<String, List<DODDLELiteral>> langLabelListMap = concept.getLangLabelListMap();
            List<DODDLELiteral> labelList = langLabelListMap.get("ja");
            if (labelList != null) {
                for (DODDLELiteral label : labelList) {
                    ontModel.add(uri, RDFS.label, ontModel.createLiteral(label.getString(), "ja"));
                }
            }
            labelList = langLabelListMap.get("en");
            if (labelList != null) {
                for (DODDLELiteral label : labelList) {
                    ontModel.add(uri, RDFS.label, ontModel.createLiteral(label.getString(), "en"));
                }
            }
            Map<String, List<DODDLELiteral>> langDescriptionListMap = concept.getLangDescriptionListMap();
            labelList = langDescriptionListMap.get("ja");
            if (labelList != null) {
                for (DODDLELiteral label : labelList) {
                    ontModel.add(uri, RDFS.comment, ontModel.createLiteral(label.getString(), "ja"));
                }
            }
            labelList = langDescriptionListMap.get("en");
            if (labelList != null) {
                for (DODDLELiteral label : labelList) {
                    ontModel.add(uri, RDFS.comment, ontModel.createLiteral(label.getString(), "en"));
                }
            }
            if (i % 10000 == 0) {
                System.out.println("Writing OWL Concept Data: " + i);
                DODDLEDicConverterUI.setProgressText("Writing OWL Concept Data: " + i);
            }
            i++;
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    private static void putWordFilePointer(String word, String id) {
        if (word.replaceAll("\\s*", "").length() == 0) {
            // System.out.println("空白文字: " + word);
            return;
        }
        word = word.replaceAll("\t", " ");
        if (wordFilePointerSetMap.get(word) != null) {
            // System.out.println(word+": "+id+": "+idFilePointerMap.get(id));
            Set<Long> idSet = wordFilePointerSetMap.get(word);
            idSet.add(idFilePointerMap.get(id));
        } else {
            Set<Long> idSet = new HashSet<>();
            // System.out.println(word+": "+id+": "+idFilePointerMap.get(id));
            idSet.add(idFilePointerMap.get(id));
            wordFilePointerSetMap.put(word, idSet);
        }
    }

    public static void readWordData() {
        for (Concept c : idDefinitionMap.values()) {
            Map<String, List<DODDLELiteral>> langLabelListMap = c.getLangLabelListMap();
            for (List<DODDLELiteral> labelList : langLabelListMap.values()) {
                for (DODDLELiteral label : labelList) {
                    putWordFilePointer(label.getString(), c.getLocalName());
                }
            }
            Map<String, List<DODDLELiteral>> langDescriptonListMap = c.getLangDescriptionListMap();
            for (List<DODDLELiteral> descriptionList : langDescriptonListMap.values()) {
                for (DODDLELiteral description : descriptionList) {
                    // 15文字以下の説明の場合，多義性解消時に参照できるようにする
                    if (description.getString().length() <= 15) {
                        putWordFilePointer(description.getString(), c.getLocalName());
                    }
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void writeWordData() {
        System.out.println("Make Word Data: Writing word.data");
        DODDLEDicConverterUI.setProgressText("Make Word Data: Writing word.data");
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + WORD_DATA);
            writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            for (Entry<String, Set<Long>> entry : wordFilePointerSetMap.entrySet()) {
                String word = entry.getKey();
                Set<Long> filePointerSet = entry.getValue();
                writer.write(word);
                writer.write("\t");
                for (Long filePointer : filePointerSet) {
                    writer.write(String.valueOf(filePointer));
                    writer.write("\t");
                }
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void writeWordIndex() {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + WORD_INDEX);
            writer = new BufferedWriter(new OutputStreamWriter(os, "ISO8859_1"));

            int i = 0;
            DecimalFormat df = new DecimalFormat("00000000");
            RandomAccessFile raf = new RandomAccessFile(DODDLE_DIC_HOME + WORD_DATA, "r");
            while (raf.readLine() != null) {
                writer.write(df.format(raf.getFilePointer()));
                writer.newLine();
                i++;
                if (i % 10000 == 0) {
                    System.out.println("Make Word Index: " + i);
                    DODDLEDicConverterUI.setProgressText("Make Word Index: " + i);
                }
            }
            raf.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(ioe.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void writeOWLTreeData(Model ontModel, String ns) {
        System.out.println("Writing OWL Tree Data");
        DODDLEDicConverterUI.setProgressText("Writing OWL Tree Data");
        try {
            int i = 0;
            ResultSet rs = stmt.executeQuery("select * from synlink where link='hype'");
            while (rs.next()) {
                String id = rs.getString("synset2");
                String subID = rs.getString("synset1");
                Resource concept = ResourceFactory.createResource(ns + id);
                Resource subConcept = ResourceFactory.createResource(ns + subID);
                ontModel.add(subConcept, RDFS.subClassOf, concept);
                if (i % 10000 == 0) {
                    System.out.println("Writing OWL Tree Data: " + i);
                    DODDLEDicConverterUI.setProgressText("Writing OWL Tree Data: " + i);
                }
                i++;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(sqle.getMessage());
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void readRelationData() {
    }

    public static void writeRelationData() {
    }

    public static void writeRelationIndex() {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + RELATION_INDEX);
            writer = new BufferedWriter(new OutputStreamWriter(os, "ISO8859_1"));

            int i = 0;
            DecimalFormat df = new DecimalFormat("00000000");
            RandomAccessFile raf = new RandomAccessFile(DODDLE_DIC_HOME + RELATION_DATA, "r");
            while (raf.readLine() != null) {
                writer.write(df.format(raf.getFilePointer()));
                writer.newLine();
                i++;
                if (i % 10000 == 0) {
                    System.out.println("Make Relation Index: " + i);
                    DODDLEDicConverterUI.setProgressText("Make Relation Index: " + i);
                }
            }
            raf.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public static void writeOWLRegionData(Model ontModel, String ns) {
        DODDLEDicConverterUI.setProgressText("Writing OWL Domain and Range Data");
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void saveOntology(Model ontModel, String fileName) {
        System.out.println("Save " + fileName);
        DODDLEDicConverterUI.setProgressText("Save " + fileName);
        try {
            RDFWriterI rdfWriter = ontModel.getWriter("RDF/XML");
            rdfWriter.setProperty("xmlbase", DODDLEConstants.BASE_URI);
            rdfWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
            Path path = Paths.get(DODDLE_DIC_HOME + fileName);
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            try (writer) {
                rdfWriter.write(ontModel, writer, DODDLEConstants.BASE_URI);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DODDLEDicConverterUI.addProgressValue();
    }

}
