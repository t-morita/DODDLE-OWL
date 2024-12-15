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
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * EDRおよびEDR専門辞書からDODDLEで利用する形式の辞書データに変換するユーティリティクラス
 *
 * @author Takeshi Morita
 */
public class EDR2DoddleDicConverter {

    public enum DictionaryType {
        EDR, EDRT, JPNWN
    }

    private static final String WORD_DATA = "word.data";
    private static final String WORD_INDEX = "word.index";
    private static final String CONCEPT_DATA = "concept.data";
    private static final String CONCEPT_INDEX = "concept.index";
    private static final String RELATION_DATA = "relation.data";
    private static final String RELATION_INDEX = "relation.index";
    private static final String TREE_DATA = "tree.data";
    private static final String TREE_INDEX = "tree.index";

    private static String DODDLE_DIC_HOME = "C:/DODDLE-OWL/DODDLE_DIC/";
    private static String EDR_HOME = "C:/DODDLE-OWL/EDR_TextData/";
    private static String CPH_DIC_PATH = EDR_HOME + "CPH.DIC";
    private static String JWD_DIC_PATH = EDR_HOME + "JWD.DIC";
    private static String EWD_DIC_PATH = EDR_HOME + "EWD.DIC";

    private static String CPC_DIC_PATH = EDR_HOME + "CPC.DIC";
    private static String CPT_DIC_PATH = EDR_HOME + "CPT.DIC";

    private static TreeModel edrTreeModel;
    private static final Map<String, Set<String>> idSubIDSetMap = new HashMap<>();
    private static final TreeMap<String, Set<TreeNode>> idNodeSetMap = new TreeMap<>();

    private static final TreeSet<String> relationConceptIDSet = new TreeSet<>();

    private static final List<Long> dataFilePointerList = new ArrayList<>();
    private static final Map<String, Long> idFilePointerMap = new HashMap<>();
    private static final TreeMap<String, Concept> idDefinitionMap = new TreeMap<>();
    private static final TreeMap<String, Set<String>> wordIDSetMap = new TreeMap<>();
    private static final TreeMap<String, Set<Long>> wordFilePointerSetMap = new TreeMap<>();

    private static final Map<String, Set<String>> agentMap = new HashMap<>();
    private static final Map<String, Set<String>> objectMap = new HashMap<>();
    private static final Map<String, Set<String>> goalMap = new HashMap<>();
    private static final Map<String, Set<String>> placeMap = new HashMap<>();
    private static final Map<String, Set<String>> implementMap = new HashMap<>();
    private static final Map<String, Set<String>> a_objectMap = new HashMap<>();
    private static final Map<String, Set<String>> sceneMap = new HashMap<>();
    private static final Map<String, Set<String>> causeMap = new HashMap<>();

    public static void clearRelationMaps() {
        agentMap.clear();
        objectMap.clear();
        goalMap.clear();
        placeMap.clear();
        implementMap.clear();
        a_objectMap.clear();
        sceneMap.clear();
        causeMap.clear();
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
        edrTreeModel = null;
    }

    public static void setDODDLEDicPath(String dirName) {
        DODDLE_DIC_HOME = dirName;
    }

    public static boolean setEDRDicPath(String dirName, DictionaryType dicType) {
        EDR_HOME = dirName;
        if (dicType == DictionaryType.EDR) {
            CPH_DIC_PATH = EDR_HOME + "CPH.DIC";
            JWD_DIC_PATH = EDR_HOME + "JWD.DIC";
            EWD_DIC_PATH = EDR_HOME + "EWD.DIC";
            CPC_DIC_PATH = EDR_HOME + "CPC.DIC";
            CPT_DIC_PATH = EDR_HOME + "CPT.DIC";
        } else if (dicType == DictionaryType.EDRT) {
            CPH_DIC_PATH = EDR_HOME + "TCPH.DIC";
            JWD_DIC_PATH = EDR_HOME + "TJWD.DIC";
            EWD_DIC_PATH = EDR_HOME + "TEWD.DIC";
            CPC_DIC_PATH = EDR_HOME + "TCPC.DIC";
        }
        return new File(CPH_DIC_PATH).exists();
    }

    private static Concept getConcept(String id) {
        if (idDefinitionMap.get(id) != null) {
            return idDefinitionMap.get(id);
        }
        Concept c = new Concept(id, "");
        idDefinitionMap.put(id, c);
        return c;
    }

    public static void readTreeData(String rootID) {
        BufferedReader reader = null;
        try {
            System.out.println("Make Tree Data: Reading CPC.DIC");
            DODDLEDicConverterUI.setProgressText("Make Tree Data: Reading CPC.DIC");
            FileInputStream fis = new FileInputStream(CPC_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] elements = line.split("\t");
                if (elements.length != 4) {
                    continue;
                }
                String id = "ID" + elements[1];
                String subID = "ID" + elements[2];
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
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootID);
            Set<TreeNode> nodeSet = new HashSet<>();
            nodeSet.add(rootNode);
            idNodeSetMap.put(rootID, nodeSet);
            makeEDRTree(rootID, rootNode);
            edrTreeModel = new DefaultTreeModel(rootNode);
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
    }

    private static void makeEDRTree(String id, DefaultMutableTreeNode node) {
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
                makeEDRTree(subID, subNode);
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
            TreeNode[] pathToRoot = ((DefaultTreeModel) edrTreeModel).getPathToRoot(node);
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
                // Windowsの改行コード CR LFに合わせる必要があるためnewLineは使えない
                // writer.newLine();
                writer.write("\r\n");
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
                writer.write("\r\n");
                i++;
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Make Tree Index: " + i);
                    System.out.println("Make Tree Index: " + i);
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
                idFilePointerMap.put(id, dfp);
                dataFilePointerList.add(dfp);
                dfp = raf.getFilePointer();
                i++;
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Make Concept Index: " + i);
                    System.out.println("Make Concept Index: " + i);
                }
            }
            raf.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(ioe.getMessage());
        }
    }

    public static void writeConceptIndex() {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + CONCEPT_INDEX);
            writer = new BufferedWriter(new OutputStreamWriter(os, "ISO8859_1"));

            for (long dfp : dataFilePointerList) {
                DecimalFormat df = new DecimalFormat("00000000");
                writer.write(df.format(dfp));
                writer.write("\r\n");
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
    }

    public static void readConceptData() {
        try {
            System.out.println("Make Concept Data: Reading CPH.DIC");
            DODDLEDicConverterUI.setProgressText("Make Concept Data: Reading CPH.DIC");
            readCPHDic();
            DODDLEDicConverterUI.addProgressValue();
            System.out.println("Make Concept Data: Reading JWD.DIC");
            DODDLEDicConverterUI.setProgressText("Make Concept Data: Reading JWD.DIC");
            readJWDDic();
            DODDLEDicConverterUI.addProgressValue();
            System.out.println("Make Concept Data: Reading EWD.DIC");
            DODDLEDicConverterUI.setProgressText("Make Concept Data: Reading EWD.DIC");
            readEWDDic();
            DODDLEDicConverterUI.addProgressValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } catch (IOException uee) {
            uee.printStackTrace();
        }
    }

    public static void writeConceptData() {
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + CONCEPT_DATA);
            writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            System.out.println("Make Concept Data: Writing concept.data");
            DODDLEDicConverterUI.setProgressText("Make Concept Data: Writing concept.data");
            for (Entry<String, Concept> entry : idDefinitionMap.entrySet()) {
                String id = entry.getKey();
                Concept concept = entry.getValue();
                writer.write(id);
                writer.write("\t^");
                Map<String, List<DODDLELiteral>> langLabelListMap = concept.getLangLabelListMap();
                writerLiteralString(writer, langLabelListMap.get("ja"));
                writerLiteralString(writer, langLabelListMap.get("en"));
                Map<String, List<DODDLELiteral>> langDescriptionListMap = concept
                        .getLangDescriptionListMap();
                writerLiteralString(writer, langDescriptionListMap.get("ja"));
                writerLiteralString(writer, langDescriptionListMap.get("en"));
                writer.write("\r\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } catch (IOException uee) {
            uee.printStackTrace();
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

    public static void writeOWLConceptData(Model jaOntModel, Model enOntModel, String ns) {
        DODDLEDicConverterUI.setProgressText("Writing OWL Concept");
        System.out.println("Writing OWL Concept");
        writeOWLCPHData(jaOntModel, enOntModel, ns);
        writeOWLEWDData(jaOntModel, enOntModel, ns);
        writeOWLJWDData(jaOntModel, enOntModel, ns);
    }

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void readEWDDic() throws FileNotFoundException, UnsupportedEncodingException,
            IOException {
        FileInputStream fis = new FileInputStream(EWD_DIC_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
        while (reader.ready()) {
            String line = reader.readLine();
            line = AccentSymbolConverter.convertAccentSymbol(line);
            line = line.replaceAll("\"\"", "");
            line = line.replaceAll("\"", "");
            String[] elements = line.split("\t");
            if (elements.length != 20) {
                continue;
            }
            String enWord = elements[1];
            String id = "ID" + elements[12];
            String enCWord = elements[13];
            String jaWord = elements[14].split("\\[")[0];
            String enDescription = elements[15];
            String jaDescription = elements[16];

            if (!enWord.equals(enCWord)) {
                Concept c = getConcept(id);
                c.addLabel(new DODDLELiteral("ja", jaWord));
                c.addLabel(new DODDLELiteral("en", enWord));
                c.addDescription(new DODDLELiteral("ja", jaDescription));
                c.addDescription(new DODDLELiteral("en", enDescription));
                idDefinitionMap.put(id, c);
            }
        }
        reader.close();
    }

    private static void writeOWLEWDData(Model jaOntModel, Model enOntModel, String ns) {
        DODDLEDicConverterUI.setProgressText("Writing OWL EWD Data");
        System.out.println("Writing OWL EWD Data");
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(EWD_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            int i = 0;
            while (reader.ready()) {
                i++;
                String line = reader.readLine();
                line = AccentSymbolConverter.convertAccentSymbol(line);
                line = line.replaceAll("\"\"", "");
                line = line.replaceAll("\"", "");
                String[] elements = line.split("\t");
                if (elements.length != 20) {
                    continue;
                }
                String enWord = elements[1];
                String id = "ID" + elements[12];
                String enCWord = elements[13];
                String jaWord = elements[14].split("\\[")[0];
                String enDescription = elements[15];
                String jaDescription = elements[16];

                if (!enWord.equals(enCWord)) {
                    Resource uri = ResourceFactory.createResource(ns + id);
                    jaOntModel.add(uri, RDFS.label, jaOntModel.createLiteral("ja", jaWord));
                    jaOntModel
                            .add(uri, RDFS.comment, jaOntModel.createLiteral("ja", jaDescription));
                    enOntModel.add(uri, RDFS.label, enOntModel.createLiteral("en", enWord));
                    enOntModel
                            .add(uri, RDFS.comment, enOntModel.createLiteral("en", enDescription));
                }
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Writing OWL EWD Data: " + i);
                    System.out.println(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void readJWDDic() throws FileNotFoundException, UnsupportedEncodingException,
            IOException {
        FileInputStream fis = new FileInputStream(JWD_DIC_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
        while (reader.ready()) {
            String line = reader.readLine();
            line = AccentSymbolConverter.convertAccentSymbol(line);
            line = line.replaceAll("\"\"", "");
            line = line.replaceAll("\"", "");
            String[] elements = line.split("\t");
            if (elements.length != 19) {
                continue;
            }
            String jaWord = elements[1].split("\\[")[0];

            String invariableWord = elements[2].split("\\(")[0];
            String pos = elements[5];
            String id = "ID" + elements[11];
            String enWord = elements[12];
            String jaCWord = elements[13].split("\\[")[0];
            String enDescription = elements[14];
            String jaDescription = elements[15];

            if (!jaWord.equals(jaCWord)) {
                Concept c = getConcept(id);
                c.addLabel(new DODDLELiteral("ja", jaWord));
                c.addLabel(new DODDLELiteral("en", enWord));
                c.addDescription(new DODDLELiteral("ja", jaDescription));
                c.addDescription(new DODDLELiteral("en", enDescription));
                idDefinitionMap.put(id, c);
            }
            String[] posSet = pos.split(";");
            if (posSet.length == 2 && posSet[0].equals("JN1") && posSet[1].equals("JVE")) {
                Concept c = getConcept(id);
                c.addLabel(new DODDLELiteral("ja", invariableWord));
                c.addLabel(new DODDLELiteral("en", enWord));
                c.addDescription(new DODDLELiteral("ja", jaDescription));
                c.addDescription(new DODDLELiteral("en", enDescription));
                idDefinitionMap.put(id, c);
            }
        }
        reader.close();
    }

    private static void writeOWLJWDData(Model jaOntModel, Model enOntModel, String ns) {
        DODDLEDicConverterUI.setProgressText("Writing OWL JWD Data");
        System.out.println("Writing OWL JWD Data");
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(JWD_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            int i = 0;
            while (reader.ready()) {
                i++;
                String line = reader.readLine();
                line = AccentSymbolConverter.convertAccentSymbol(line);
                line = line.replaceAll("\"\"", "");
                line = line.replaceAll("\"", "");
                String[] elements = line.split("\t");
                if (elements.length != 19) {
                    continue;
                }
                String jaWord = elements[1].split("\\[")[0];

                String invariableWord = elements[2].split("\\(")[0];
                String pos = elements[5];
                String id = "ID" + elements[11];
                String enWord = elements[12];
                String jaCWord = elements[13].split("\\[")[0];
                String enDescription = elements[14];
                String jaDescription = elements[15];

                Resource uri = ResourceFactory.createResource(ns + id);
                if (!jaWord.equals(jaCWord)) {
                    jaOntModel.add(uri, RDFS.label, jaOntModel.createLiteral(jaWord, "ja"));
                    enOntModel.add(uri, RDFS.label, enOntModel.createLiteral(enWord, "en"));
                    jaOntModel
                            .add(uri, RDFS.comment, jaOntModel.createLiteral(jaDescription, "ja"));
                    enOntModel
                            .add(uri, RDFS.comment, enOntModel.createLiteral(enDescription, "en"));
                }
                String[] posSet = pos.split(";");
                if (posSet.length == 2 && posSet[0].equals("JN1") && posSet[1].equals("JVE")) {
                    jaOntModel.add(uri, RDFS.label, jaOntModel.createLiteral(invariableWord, "ja"));
                    enOntModel.add(uri, RDFS.label, enOntModel.createLiteral(enWord, "en"));
                    jaOntModel
                            .add(uri, RDFS.comment, jaOntModel.createLiteral(jaDescription, "ja"));
                    enOntModel
                            .add(uri, RDFS.comment, enOntModel.createLiteral(enDescription, "en"));
                }
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Writing OWL JWD Data: " + i);
                    System.out.println(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void readCPHDic() throws FileNotFoundException, UnsupportedEncodingException,
            IOException {
        FileInputStream fis = new FileInputStream(CPH_DIC_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
        while (reader.ready()) {
            String line = reader.readLine();
            line = AccentSymbolConverter.convertAccentSymbol(line);
            line = line.replaceAll("\"\"", "");
            line = line.replaceAll("\"", "");
            String[] elements = line.split("\t");
            if (elements.length != 7) {
                continue;
            }
            String id = "ID" + elements[1];
            String enWord = elements[2];
            String jaWord = elements[3];
            String enDescription = elements[4];
            String jaDescription = elements[5];
            if (1 < jaWord.split("\\[").length) {
                jaWord = jaWord.split("\\[")[0];
            }
            if (jaDescription.matches("\\[.*\\]")) {
                jaDescription = jaDescription.split("\\[")[1].split("\\]")[0];
            }
            Concept c = new Concept(id, "");
            c.addLabel(new DODDLELiteral("ja", jaWord));
            c.addLabel(new DODDLELiteral("en", enWord));
            c.addDescription(new DODDLELiteral("en", enDescription));
            c.addDescription(new DODDLELiteral("ja", jaDescription));
            idDefinitionMap.put(id, c);
        }
        reader.close();
    }

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void writeOWLCPHData(Model jaOntModel, Model enOntModel, String ns) {
        BufferedReader reader = null;
        try {
            DODDLEDicConverterUI.setProgressText("Writing OWL CPH Data");
            System.out.println("Writing OWL CPH Data");
            FileInputStream fis = new FileInputStream(CPH_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            int i = 0;
            while (reader.ready()) {
                i++;
                String line = reader.readLine();
                line = AccentSymbolConverter.convertAccentSymbol(line);
                line = line.replaceAll("\"\"", "");
                line = line.replaceAll("\"", "");
                String[] elements = line.split("\t");
                if (elements.length != 7) {
                    continue;
                }
                String id = "ID" + elements[1];
                String enWord = elements[2];
                String jaWord = elements[3];
                String enDescription = elements[4];
                String jaDescription = elements[5];
                if (1 < jaWord.split("\\[").length) {
                    jaWord = jaWord.split("\\[")[0];
                }
                if (jaDescription.matches("\\[.*\\]")) {
                    jaDescription = jaDescription.split("\\[")[1].split("\\]")[0];
                }
                Resource uri = ResourceFactory.createResource(ns + id);
                jaOntModel.add(uri, RDF.type, OWL.Class);
                jaOntModel.add(uri, RDFS.label, jaOntModel.createLiteral("ja", jaWord));
                jaOntModel.add(uri, RDFS.comment, jaOntModel.createLiteral("ja", jaDescription));
                enOntModel.add(uri, RDF.type, OWL.Class);
                enOntModel.add(uri, RDFS.label, enOntModel.createLiteral("en", enWord));
                enOntModel.add(uri, RDFS.comment, enOntModel.createLiteral("en", enDescription));
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Writing OWL CPH Data: " + i);
                    System.out.println(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    }

    public static void writeWordData() {
        System.out.println("Make Word Data: Writing word.data");
        DODDLEDicConverterUI.setProgressText("Make Word Data: Writing word.data");
        try {
            Path path = Paths.get(DODDLE_DIC_HOME + WORD_DATA);
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            try (writer) {
                for (Entry<String, Set<Long>> entry : wordFilePointerSetMap.entrySet()) {
                    String word = entry.getKey();
                    Set<Long> filePointerSet = entry.getValue();
                    writer.write(word);
                    writer.write("\t");
                    for (Long filePointer : filePointerSet) {
                        writer.write(String.valueOf(filePointer));
                        writer.write("\t");
                    }
                    writer.write("\r\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(e.getMessage());
        }
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
                writer.write("\r\n");
                i++;
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Make Word Index: " + i);
                    System.out.println("Make Word Index: " + i);
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
    }

    public static void writeOWLTreeData(Model ontModel, String ns) {
        DODDLEDicConverterUI.setProgressText("Writing OWL Tree Data");
        System.out.println("Writing OWL Tree Data");
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(CPC_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            int i = 0;
            while (reader.ready()) {
                i++;
                String line = reader.readLine();
                String[] elements = line.split("\t");
                if (elements.length != 4) {
                    continue;
                }
                String id = "ID" + elements[1];
                String subID = "ID" + elements[2];
                Resource concept = ResourceFactory.createResource(ns + id);
                Resource subConcept = ResourceFactory.createResource(ns + subID);
                ontModel.add(subConcept, RDFS.subClassOf, concept);
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Writing OWL Tree Data: " + i);
                    System.out.println(i);
                }
            }
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

    private static void putID(String fid, String tid, Map<String, Set<String>> map) {
        if (map.get(fid) != null) {
            Set<String> idSet = map.get(fid);
            idSet.add(tid);
            map.put(fid, idSet);
        } else {
            Set<String> idSet = new HashSet<>();
            idSet.add(tid);
            map.put(fid, idSet);
        }
    }

    public static void readRelationData() {
        BufferedReader reader = null;
        try {
            System.out.println("Make Relation Data: Reading CPT.DIC");
            DODDLEDicConverterUI.setProgressText("Make Relation Data: Reading CPT.DIC");
            FileInputStream fis = new FileInputStream(CPT_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] elements = line.split("\t");
                if (elements.length != 7) {
                    continue;
                }
                String fid = "ID" + elements[2];
                String rel = elements[3];
                String tid = "ID" + elements[4];
                String tf = elements[5];
                if (tf.equals("0")) {
                    continue;
                }
                relationConceptIDSet.add(fid);
                switch (rel) {
                    case "agent":
                        putID(fid, tid, agentMap);
                        break;
                    case "object":
                        putID(fid, tid, objectMap);
                        break;
                    case "goal":
                        putID(fid, tid, goalMap);
                        break;
                    case "place":
                        putID(fid, tid, placeMap);
                        break;
                    case "implement":
                        putID(fid, tid, implementMap);
                        break;
                    case "a-object":
                        putID(fid, tid, a_objectMap);
                        break;
                    case "scene":
                        putID(fid, tid, sceneMap);
                        break;
                    case "cause":
                        putID(fid, tid, causeMap);
                        break;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DODDLEDicConverterUI.initProgressBar(ioe.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    public static void writeRelationData() {
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + RELATION_DATA);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "ISO8859_1"));

            System.out.println("Make Relation Data: Writing relation.data");
            DODDLEDicConverterUI.setProgressText("Make Relation Data: Writing relation.dat");

            for (String id : relationConceptIDSet) {
                writer.write(id);
                writer.write("\t^");
                writeRelationData("agent", id, agentMap, writer);
                writeRelationData("object", id, objectMap, writer);
                writeRelationData("goal", id, goalMap, writer);
                writeRelationData("place", id, placeMap, writer);
                writeRelationData("implement", id, implementMap, writer);
                writeRelationData("a_object", id, a_objectMap, writer);
                writeRelationData("scene", id, sceneMap, writer);
                writeRelationData("cause", id, causeMap, writer);
                writer.write("\r\n");
            }
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

    private static void writeRelationData(String type, String vid, Map<String, Set<String>> map,
                                          Writer writer) {
        try {
            if (map.get(vid) != null) {
                writer.write("|");
                writer.write(type);
                writer.write("\t");
                Set<String> tidSet = map.get(vid);
                for (String tid : tidSet) {
                    writer.write(tid);
                    writer.write("\t");
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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
                writer.write("\r\n");
                i++;
                if (i % 10000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Make Relation Index: " + i);
                    System.out.println("Make Relation Index: " + i);
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
        System.out.println("Writing OWL Domain and Range Data");
        DODDLEDicConverterUI.setProgressText("Writing OWL Domain and Range Data");
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(CPT_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            int i = 0;
            while (reader.ready()) {
                i++;
                String line = reader.readLine();
                String[] elements = line.split("\t");
                if (elements.length != 7) {
                    continue;
                }
                String fid = "ID" + elements[2];
                String rel = elements[3];
                String tid = "ID" + elements[4];
                String tf = elements[5];
                if (tf.equals("0")) {
                    continue;
                }
                Resource fres = ResourceFactory.createResource(ns + fid);
                Resource tres = ResourceFactory.createResource(ns + tid);
                ontModel.add(fres, RDF.type, OWL.ObjectProperty);
                switch (rel) {
                    case "agent":
                        ontModel.add(fres, RDFS.domain, tres);
                        break;
                    case "object":
                        ontModel.add(fres, RDFS.range, tres);
                        break;
                    case "goal":
                        break;
                    case "place":
                        break;
                    case "implement":
                        break;
                    case "a-object":
                        break;
                    case "scene":
                        break;
                    case "cause":
                        break;
                }
                if (i % 1000 == 0) {
                    DODDLEDicConverterUI.setProgressText("Writing OWL Domain and Range Data: " + i);
                    System.out.println(i);
                }
            }
        } catch (IOException e) {
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
        DODDLEDicConverterUI.addProgressValue();
    }

    public static void saveOntology(Model ontModel, String fileName) {
        DODDLEDicConverterUI.setProgressText("Save " + fileName);
        System.out.println("Save " + fileName);
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + fileName);
            writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            RDFWriterI rdfWriter = ontModel.getWriter("RDF/XML");
            rdfWriter.setProperty("xmlbase", DODDLEConstants.BASE_URI);
            rdfWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
            rdfWriter.write(ontModel, writer, DODDLEConstants.BASE_URI);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    ontModel.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        DODDLEDicConverterUI.addProgressValue();
    }
}
