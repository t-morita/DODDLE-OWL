/*
 * @(#)  2006/02/28
 * 
 * EDRおよびEDR専門辞書からDODDLEで利用する形式の辞書データに変換するユーティリティクラス
 * 
 * 
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Map.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class EDR2DoddleDicConverter {

    public static enum DictionaryType {
        EDR, EDRT
    }

    public static String INDEX_FILE = "index.edr";
    public static String INDEX_FP_LIST_FILE = "index_fp_list.edr";
    public static String DATA_FILE = "data.edr";
    public static String DATA_FP_LIST_FILE = "data_fp_list.edr";

    private static String DODDLE_DIC_HOME = "C:/DODDLE-OWL/DODDLE_DIC/";
    private static String EDR_HOME = "C:/DODDLE-OWL/EDR_TextData/";
    private static String CPH_DIC_PATH = EDR_HOME + "CPH.DIC";
    private static String JWD_DIC_PATH = EDR_HOME + "JWD.DIC";
    private static String EWD_DIC_PATH = EDR_HOME + "EWD.DIC";

    private static String CPC_DIC_PATH = EDR_HOME + "CPC.DIC";
    private static String CPT_DIC_PATH = EDR_HOME + "CPT.DIC";

    private static List<Long> dataFilePointerList = new ArrayList<Long>();
    private static Map<String, Long> idFilePointerMap = new HashMap<String, Long>();
    private static TreeMap<String, Concept> idDefinitionMap = new TreeMap<String, Concept>();
    private static TreeMap<String, Set<String>> wordIDSetMap = new TreeMap<String, Set<String>>();
    private static TreeMap<String, Set<Long>> wordFilePointerSetMap = new TreeMap<String, Set<Long>>();
    private static Map<String, Set<String>> idSubIDSetMap = new HashMap<String, Set<String>>();

    private static Map<String, Set<String>> agentMap = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> objectMap = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> goalMap = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> placeMap = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> implementMap = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> a_objectMap = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> sceneMap = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> causeMap = new HashMap<String, Set<String>>();

    private static TreeSet<String> vidSet = new TreeSet<String>();

    public static void clearConceptDefinitionMaps() {
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

    public static void clearIDSubIDSetMap() {
        idSubIDSetMap.clear();
    }

    public static void setDODDLEDicPath(String dirName) {
        DODDLE_DIC_HOME = dirName;
    }

    public static void setEDRDicPath(String dirName, DictionaryType dicType) {
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
    }

    public static void setEDRDICPath(boolean isSpecial) {
        if (isSpecial) {
            EDR_HOME = "C:/DODDLE-OWL/EDRT_TextData/";
            CPH_DIC_PATH = EDR_HOME + "TCPH.DIC";
            JWD_DIC_PATH = EDR_HOME + "TJWD.DIC";
            EWD_DIC_PATH = EDR_HOME + "TEWD.DIC";
            CPC_DIC_PATH = EDR_HOME + "TCPC.DIC";
        }
    }

    private static Concept getConcept(String id) {
        if (idDefinitionMap.get(id) != null) { return idDefinitionMap.get(id); }
        Concept c = new Concept(id, "");
        idDefinitionMap.put(id, c);
        return c;
    }

    public static void makeIDFilePointerMap() {
        try {
            int i = 0;
            RandomAccessFile raf = new RandomAccessFile(DODDLE_DIC_HOME + DATA_FILE, "r");
            String line = "";
            long dfp = 0;
            while ((line = raf.readLine()) != null) {
                line = new String(line.getBytes("ISO8859_1"), "UTF-8");
                String id = line.split("\t\\^")[0];
                // System.out.println(line);
                // System.out.println(id + ":" + raf.getFilePointer());
                idFilePointerMap.put(id, dfp);
                dataFilePointerList.add(dfp);
                dfp = raf.getFilePointer();
                i++;
                if (i % 10000 == 0) {
                    EDR2DoddleDicConverterUI.setProgressText("Make IDFilePointerMap: " + i);
                    System.out.println("Make IDFilePointerMap: " + i);
                }
            }
            EDR2DoddleDicConverterUI.setProgressText("Make IDFilePointerMap: done");
            raf.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void writeDataFilePointerList() {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + DATA_FP_LIST_FILE);
            writer = new BufferedWriter(new OutputStreamWriter(os, "ISO8859_1"));

            for (long dfp : dataFilePointerList) {
                DecimalFormat df = new DecimalFormat("00000000");
                writer.write(df.format(dfp));
                writer.newLine();
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

    public static void writeData() {
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + DATA_FILE);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            System.out.println("Make Data: Writing data.edr");
            EDR2DoddleDicConverterUI.setProgressText("Make Data: Writing data.edr");
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
                writeConceptDefinition(id, writer);
                writer.write("\n");
            }
            System.out.println("Make Data: Writing data.edr");
            EDR2DoddleDicConverterUI.setProgressText("Make Data: Writing data.edr done");
            EDR2DoddleDicConverterUI.addProgressValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
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
        }
    }

    private static void writeConceptDefinition(String id, Writer writer) {
        try {
            writer.write("\t^");
            writeConceptDefinition("agent", id, agentMap, writer);
            writeConceptDefinition("object", id, objectMap, writer);
            writeConceptDefinition("goal", id, goalMap, writer);
            writeConceptDefinition("place", id, placeMap, writer);
            writeConceptDefinition("implement", id, implementMap, writer);
            writeConceptDefinition("a_object", id, a_objectMap, writer);
            writeConceptDefinition("scene", id, sceneMap, writer);
            writeConceptDefinition("cause", id, causeMap, writer);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void writeOWLConcept(Model ontModel, String ns) {
        for (Entry<String, Concept> entry : idDefinitionMap.entrySet()) {
            String id = entry.getKey();
            Concept c = entry.getValue();
            Resource concept = ResourceFactory.createResource(ns + id);
            ontModel.add(concept, RDF.type, OWL.Class);
            Map<String, List<DODDLELiteral>> langLabelListMap = c.getLangLabelListMap();
            for (String lang : langLabelListMap.keySet()) {
                for (DODDLELiteral label : langLabelListMap.get(lang)) {
                    ontModel.add(concept, RDFS.label, ontModel.createLiteral(label.getString(), label.getLang()));
                }
            }
            Map<String, List<DODDLELiteral>> langDescriptionListMap = c.getLangDescriptionListMap();
            for (String lang : langDescriptionListMap.keySet()) {
                for (DODDLELiteral description : langDescriptionListMap.get(lang)) {
                    ontModel.add(concept, RDFS.comment, ontModel.createLiteral(description.getString(), description
                            .getLang()));
                }
            }
        }
    }

    public static void makeData() {
        try {
            System.out.println("Reading CPH.DIC");
            EDR2DoddleDicConverterUI.setProgressText("Make Data: Reading CPH.DIC");
            readCPHDic();
            EDR2DoddleDicConverterUI.addProgressValue();
            System.out.println("Read CPH.DIC");
            System.out.println("Reading JWD.DIC");
            EDR2DoddleDicConverterUI.setProgressText("Make Data: Reading JWD.DIC");
            readJWDDic();
            EDR2DoddleDicConverterUI.addProgressValue();
            System.out.println("Read JWD.DIC");
            System.out.println("Reading EWD.DIC");
            EDR2DoddleDicConverterUI.setProgressText("Make Data: Reading EWD.DIC");
            readEWDDic();
            EDR2DoddleDicConverterUI.addProgressValue();
            System.out.println("Read EWD.DIC");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void readEWDDic() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        FileInputStream fis = new FileInputStream(EWD_DIC_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
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

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void readJWDDic() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        FileInputStream fis = new FileInputStream(JWD_DIC_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
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

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void readCPHDic() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        FileInputStream fis = new FileInputStream(CPH_DIC_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
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
            Set<Long> idSet = new HashSet<Long>();
            // System.out.println(word+": "+id+": "+idFilePointerMap.get(id));
            idSet.add(idFilePointerMap.get(id));
            wordFilePointerSetMap.put(word, idSet);
        }
    }

    public static void writeIndex() {
        EDR2DoddleDicConverterUI.setProgressText("Make Index: Writing Index File");
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + INDEX_FILE);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            for (Entry<String, Set<Long>> entry : wordFilePointerSetMap.entrySet()) {
                String word = entry.getKey();
                Set<Long> filePointerSet = entry.getValue();
                writer.write(word);
                writer.write("\t");
                for (Long filePointer : filePointerSet) {
                    writer.write(String.valueOf(filePointer));
                    writer.write("\t");
                }
                writer.write("\n");
            }
            EDR2DoddleDicConverterUI.setProgressText("Make Index done");
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void writeIndexFilePointerList() {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + INDEX_FP_LIST_FILE);
            writer = new BufferedWriter(new OutputStreamWriter(os, "ISO8859_1"));

            int i = 0;
            DecimalFormat df = new DecimalFormat("00000000");
            RandomAccessFile raf = new RandomAccessFile(DODDLE_DIC_HOME + INDEX_FILE, "r");
            while (raf.readLine() != null) {
                writer.write(df.format(raf.getFilePointer()));
                writer.newLine();
                i++;
                if (i % 10000 == 0) {
                    EDR2DoddleDicConverterUI.setProgressText("Make EDRIndexFilePointerList: " + i);
                    System.out.println("Make EDRIndexFilePointerList: " + i);
                }
            }
            EDR2DoddleDicConverterUI.setProgressText("Make IDFilePointerMap: done");
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

    public static void makeIndex() {
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

    public static void writeIDSubIDSetMap() {
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + "idSubIDSetMapforEDR.txt");
            writer = new BufferedWriter(new OutputStreamWriter(fos, "SJIS"));
            for (Entry<String, Set<String>> entry : idSubIDSetMap.entrySet()) {
                String id = entry.getKey();
                Set<String> subIDSet = entry.getValue();
                writer.write(id + "\t");
                for (String subID : subIDSet) {
                    writer.write(subID + "\t");
                }
                writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void writeIDSubIDOWL(Model ontModel, String ns) {
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(CPC_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis));
            while (reader.ready()) {
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
    }

    public static void makeIDSubIDSetMap() {
        BufferedReader reader = null;
        try {
            System.out.println("Reading CPC.DIC");
            EDR2DoddleDicConverterUI.setProgressText("Make Data: Reading CPC.DIC");
            FileInputStream fis = new FileInputStream(CPC_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis));
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
                    Set<String> subIDSet = new HashSet<String>();
                    subIDSet.add(subID);
                    idSubIDSetMap.put(id, subIDSet);
                }
            }
            System.out.println("Reading CPC.DIC");
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
    }

    private static void putID(String fid, String tid, Map<String, Set<String>> map) {
        if (map.get(fid) != null) {
            Set<String> idSet = map.get(fid);
            idSet.add(tid);
            map.put(fid, idSet);
        } else {
            Set<String> idSet = new HashSet<String>();
            idSet.add(tid);
            map.put(fid, idSet);
        }
    }

    private static void writeConceptDefinition(String type, String vid, Map<String, Set<String>> map, Writer writer) {
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

    private static void writeTID(String vid, Map<String, Set<String>> map, Writer writer) throws IOException {
        if (map.get(vid) != null) {
            Set<String> tidSet = map.get(vid);
            for (String tid : tidSet) {
                writer.write(tid);
                writer.write("\t");
            }
        }
    }

    public static void writeRegionOWL(Model ontModel, String ns) {
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(CPT_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis));
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
                Resource fres = ResourceFactory.createResource(ns + fid);
                Resource tres = ResourceFactory.createResource(ns + tid);
                ontModel.add(fres, RDF.type, OWL.ObjectProperty);
                if (rel.equals("agent")) {
                    ontModel.add(fres, RDFS.domain, tres);
                } else if (rel.equals("object")) {
                    ontModel.add(fres, RDFS.range, tres);
                } else if (rel.equals("goal")) {
                } else if (rel.equals("place")) {
                } else if (rel.equals("implement")) {
                } else if (rel.equals("a-object")) {
                } else if (rel.equals("scene")) {
                } else if (rel.equals("cause")) {
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
    }

    public static void makeConceptDefinitionMap2() {
        BufferedReader reader = null;
        try {
            System.out.println("Reading CPT.DIC");
            EDR2DoddleDicConverterUI.setProgressText("Make Data: Reading CPT.DIC");
            FileInputStream fis = new FileInputStream(CPT_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis));
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
                vidSet.add(fid);
                if (rel.equals("agent")) {
                    putID(fid, tid, agentMap);
                } else if (rel.equals("object")) {
                    putID(fid, tid, objectMap);
                } else if (rel.equals("goal")) {
                    putID(fid, tid, goalMap);
                } else if (rel.equals("place")) {
                    putID(fid, tid, placeMap);
                } else if (rel.equals("implement")) {
                    putID(fid, tid, implementMap);
                } else if (rel.equals("a-object")) {
                    putID(fid, tid, a_objectMap);
                } else if (rel.equals("scene")) {
                    putID(fid, tid, sceneMap);
                } else if (rel.equals("cause")) {
                    putID(fid, tid, causeMap);
                }
            }
            System.out.println("Read CPT.DIC done");
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
    }

    public static void makeConceptDefinitionMap() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            FileInputStream fis = new FileInputStream(CPT_DIC_PATH);
            reader = new BufferedReader(new InputStreamReader(fis));
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
                vidSet.add(fid);

                if (rel.equals("agent")) {
                    putID(fid, tid, agentMap);
                } else if (rel.equals("object")) {
                    putID(fid, tid, objectMap);
                } else if (rel.equals("goal")) {
                    putID(fid, tid, goalMap);
                } else if (rel.equals("place")) {
                    putID(fid, tid, placeMap);
                } else if (rel.equals("implement")) {
                    putID(fid, tid, implementMap);
                } else if (rel.equals("a-object")) {
                    putID(fid, tid, a_objectMap);
                } else if (rel.equals("scene")) {
                    putID(fid, tid, sceneMap);
                } else if (rel.equals("cause")) {
                    putID(fid, tid, causeMap);
                }
            }
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_HOME + "conceptDefinitionforEDR.txt");
            writer = new BufferedWriter(new OutputStreamWriter(fos, "SJIS"));
            for (String vid : vidSet) {
                if (agentMap.get(vid) != null && objectMap.get(vid) != null && goalMap.get(vid) != null
                        && placeMap.get(vid) != null && implementMap.get(vid) != null && a_objectMap.get(vid) != null
                        && sceneMap.get(vid) != null && causeMap.get(vid) != null) {
                    continue;
                }
                writer.write(vid + "|");
                writer.write("agent\t");
                writeTID(vid, agentMap, writer);
                writer.write("|");
                writer.write("object\t");
                writeTID(vid, objectMap, writer);
                writer.write("|");
                writer.write("goal\t");
                writeTID(vid, goalMap, writer);
                writer.write("|");
                writer.write("place\t");
                writeTID(vid, placeMap, writer);
                writer.write("|");
                writer.write("implement\t");
                writeTID(vid, implementMap, writer);
                writer.write("|");
                writer.write("a_object\t");
                writeTID(vid, a_objectMap, writer);
                writer.write("|");
                writer.write("scene\t");
                writeTID(vid, sceneMap, writer);
                writer.write("|");
                writer.write("cause\t");
                writeTID(vid, causeMap, writer);
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    public static void saveOntology(Model ontModel, String fileName) {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(DODDLE_DIC_HOME + fileName);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            RDFWriter rdfWriter = ontModel.getWriter("RDF/XML-ABBREV");
            rdfWriter.setProperty("xmlbase", DODDLEConstants.BASE_URI);
            rdfWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
            rdfWriter.write(ontModel, writer, DODDLEConstants.BASE_URI);
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
}
