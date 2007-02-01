/*
 * @(#)  2006/02/28
 * 
 * EDRおよびEDR専門辞書からDODDLEで利用する形式の辞書データに変換するユーティリティクラス
 * 
 * 
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class EDR2DoddleDicConverter {

    private static String DODDLE_DIC_PATH = "C:/DODDLE_DIC";
    private static String EDR_TEXT_DIC_PATH = "C:/special_data/EDR_TextData/";
    private static String EDR_CPH_DIC_PATH = EDR_TEXT_DIC_PATH + "CPH.DIC";
    private static String EDR_JWD_DIC_PATH = EDR_TEXT_DIC_PATH + "JWD.DIC";
    private static String EDR_EWD_DIC_PATH = EDR_TEXT_DIC_PATH + "EWD.DIC";

    private static String EDR_CPC_DIC_PATH = EDR_TEXT_DIC_PATH + "CPC.DIC";

    private static String EDR_CPT_DIC_PATH = EDR_TEXT_DIC_PATH + "CPT.DIC";

    private static TreeMap<String, Concept> idDefinitionMap = new TreeMap<String, Concept>();
    private static TreeMap<String, Set<String>> wordIDSetMap = new TreeMap<String, Set<String>>();
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

    public static void clearIDDefinitionMap() {
        idDefinitionMap.clear();
    }

    public static void clearWordIDSetMap() {
        wordIDSetMap.clear();
    }

    public static void clearIDSubIDSetMap() {
        idSubIDSetMap.clear();
    }

    public static void setDODDLEDicPath(String dirName) {
        DODDLE_DIC_PATH = dirName;
    }

    public static void setEDRDicPath(String dirName, String dicType) {
        EDR_TEXT_DIC_PATH = dirName;
        if (dicType.equals("EDR")) {
            EDR_CPH_DIC_PATH = EDR_TEXT_DIC_PATH + "CPH.DIC";
            EDR_JWD_DIC_PATH = EDR_TEXT_DIC_PATH + "JWD.DIC";
            EDR_EWD_DIC_PATH = EDR_TEXT_DIC_PATH + "EWD.DIC";
            EDR_CPC_DIC_PATH = EDR_TEXT_DIC_PATH + "CPC.DIC";
            EDR_CPT_DIC_PATH = EDR_TEXT_DIC_PATH + "CPT.DIC";
        } else if (dicType.equals("EDRT")) {
            EDR_CPH_DIC_PATH = EDR_TEXT_DIC_PATH + "TCPH.DIC";
            EDR_JWD_DIC_PATH = EDR_TEXT_DIC_PATH + "TJWD.DIC";
            EDR_EWD_DIC_PATH = EDR_TEXT_DIC_PATH + "TEWD.DIC";
            EDR_CPC_DIC_PATH = EDR_TEXT_DIC_PATH + "TCPC.DIC";
        }
    }

    public static void setEDRDICPath(boolean isSpecial) {
        if (isSpecial) {
            EDR_TEXT_DIC_PATH = "C:/special_data/EDRT_TextData/";
            EDR_CPH_DIC_PATH = EDR_TEXT_DIC_PATH + "TCPH.DIC";
            EDR_JWD_DIC_PATH = EDR_TEXT_DIC_PATH + "TJWD.DIC";
            EDR_EWD_DIC_PATH = EDR_TEXT_DIC_PATH + "TEWD.DIC";
            EDR_CPC_DIC_PATH = EDR_TEXT_DIC_PATH + "TCPC.DIC";
        }
    }

    private static Concept getConcept(String id) {
        if (idDefinitionMap.get(id) != null) { return idDefinitionMap.get(id); }
        Concept c = new Concept(id, "");
        idDefinitionMap.put(id, c);
        return c;
    }

    public static void makeIDDefinitionMap() {
        BufferedWriter writer = null;
        try {
            System.out.println("read CPHDic");
            EDR2DoddleDicConverterUI.setProgressText("make idDefinitionMap: read CPH Dic");
            readCPHDic();
            EDR2DoddleDicConverterUI.addProgressValue();
            System.out.println("read CPHDic done");
            System.out.println("read JWD Dic");
            EDR2DoddleDicConverterUI.setProgressText("make idDefinitionMap: read JWD Dic");
            readJWDDic();
            EDR2DoddleDicConverterUI.addProgressValue();
            System.out.println("read JWD Dic done");
            System.out.println("read EWD Dic");
            EDR2DoddleDicConverterUI.setProgressText("make idDefinitionMap: read EWD Dic");
            readEWDDic();
            EDR2DoddleDicConverterUI.addProgressValue();
            System.out.println("read EWD Dic done");
            System.out.println("write idDefinitionMap");
            EDR2DoddleDicConverterUI.setProgressText("make idDefinitionMap: write idDefinitionMap");
            StringBuilder idList = new StringBuilder("");
            StringBuilder definitionList = new StringBuilder("");
            for (Iterator i = idDefinitionMap.entrySet().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                String id = (String) entry.getKey();
                Concept c = (Concept) entry.getValue();
                idList.append(id + "|");
                definitionList.append(c.getJaWord() + "^" + c.getEnWord() + "^" + c.getJaExplanation() + "\t^"
                        + c.getEnExplanation() + "\t^\"");
            }
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_PATH + "idDefinitionMapforEDR.txt");
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF16"));
            writer.write(idList.toString() + "\n");
            writer.write(definitionList.toString() + "\n");
            writer.close();
            System.out.println("write idDefinitionMap done");
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

    /**
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void readEWDDic() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        FileInputStream fis = new FileInputStream(EDR_EWD_DIC_PATH);
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
            String enExp = elements[15];
            String jaExp = elements[16];

            if (!enWord.equals(enCWord)) {
                Concept c = getConcept(id);
                c.addJaWord(jaWord);
                c.addEnWord(enWord);
                c.setJaExplanation(jaExp);
                c.setEnExplanation(enExp);
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
        FileInputStream fis = new FileInputStream(EDR_JWD_DIC_PATH);
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
            String enExp = elements[14];
            String jaExp = elements[15];

            if (!jaWord.equals(jaCWord)) {
                Concept c = getConcept(id);
                c.addJaWord(jaWord);
                c.addEnWord(enWord);
                c.setJaExplanation(jaExp);
                c.setEnExplanation(enExp);
                idDefinitionMap.put(id, c);
            }
            String[] posSet = pos.split(";");
            if (posSet.length == 2 && posSet[0].equals("JN1") && posSet[1].equals("JVE")) {
                Concept c = getConcept(id);
                c.addJaWord(invariableWord);
                c.addEnWord(enWord);
                c.setJaExplanation(jaExp + "\t");
                c.setEnExplanation(enExp + "\t");
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
        FileInputStream fis = new FileInputStream(EDR_CPH_DIC_PATH);
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
            String enExp = elements[4];
            String jaExp = elements[5];
            if (1 < jaWord.split("\\[").length) {
                jaWord = jaWord.split("\\[")[0];
            }
            if (jaExp.matches("\\[.*\\]")) {
                jaExp = jaExp.split("\\[")[1].split("\\]")[0];
            }
            Concept c = new Concept(id, jaWord + "\t");
            c.setEnWord(enWord + "\t");
            c.setEnExplanation(enExp);
            c.setJaExplanation(jaExp);
            idDefinitionMap.put(id, c);
        }
        reader.close();
    }

    private static void putWordID(String word, String id) {
        if (word.replaceAll("\\s*", "").length() == 0) {
            // System.out.println("空白文字: " + word);
            return;
        }
        word = word.replaceAll("\t", " ");
        if (wordIDSetMap.get(word) != null) {
            Set<String> idSet = wordIDSetMap.get(word);
            idSet.add(id);
        } else {
            Set<String> idSet = new HashSet<String>();
            idSet.add(id);
            wordIDSetMap.put(word, idSet);
        }
    }

    public static void makeWordIDSetMap() {
        for (Concept c : idDefinitionMap.values()) {
            String[] enWords = c.getEnWords();
            for (int i = 0; i < enWords.length; i++) {
                putWordID(enWords[i], c.getURI());
            }
            String[] jaWords = c.getJaWords();
            for (int i = 0; i < jaWords.length; i++) {
                putWordID(jaWords[i], c.getURI());
            }
            // 15文字以下の説明の場合，多義性解消時に参照できるようにする
            if (c.getEnExplanation().length() <= 15) {
                putWordID(c.getEnExplanation(), c.getURI());
            }
            if (c.getJaExplanation().length() <= 15) {
                putWordID(c.getJaExplanation(), c.getURI());
            }
        }
        StringBuilder wordList = new StringBuilder("");
        StringBuilder idList = new StringBuilder("");
        for (Iterator i = wordIDSetMap.entrySet().iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();
            String word = (String) entry.getKey();
            Set<String> idSet = (Set<String>) entry.getValue();
            wordList.append(word + "\t");
            for (String id : idSet) {
                idList.append(id + " ");
            }
            idList.append("|");
        }
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_PATH + "wordIDSetMapforEDR.txt");
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
            writer.write(wordList.toString() + "\n");
            writer.write(idList.toString() + "\n");
            writer.close();
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

    public static void makeIDSubIDSetMap() {
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(EDR_CPC_DIC_PATH);
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
            reader.close();
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_PATH + "idSubIDSetMapforEDR.txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "SJIS"));
            for (Iterator i = idSubIDSetMap.entrySet().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                String id = (String) entry.getKey();
                Set<String> subIDSet = (Set<String>) entry.getValue();
                writer.write(id + "\t");
                for (String subID : subIDSet) {
                    writer.write(subID + "\t");
                }
                writer.write("\n");
            }
            writer.close();
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

    private static void putID(String fid, String tid, String tf, Map<String, Set<String>> map) {
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

    private static void writeTID(String vid, Map<String, Set<String>> map, Writer writer) throws IOException {
        if (map.get(vid) != null) {
            Set<String> tidSet = map.get(vid);
            for (String tid : tidSet) {
                writer.write(tid + "\t");
            }
        }
    }

    public static void makeConceptDefinitionMap() {
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(EDR_CPT_DIC_PATH);
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
                    putID(fid, tid, tf, agentMap);
                } else if (rel.equals("object")) {
                    putID(fid, tid, tf, objectMap);
                } else if (rel.equals("goal")) {
                    putID(fid, tid, tf, goalMap);
                } else if (rel.equals("place")) {
                    putID(fid, tid, tf, placeMap);
                } else if (rel.equals("implement")) {
                    putID(fid, tid, tf, implementMap);
                } else if (rel.equals("a-object")) {
                    putID(fid, tid, tf, a_objectMap);
                } else if (rel.equals("scene")) {
                    putID(fid, tid, tf, sceneMap);
                } else if (rel.equals("cause")) {
                    putID(fid, tid, tf, causeMap);
                }
            }
            reader.close();
            FileOutputStream fos = new FileOutputStream(DODDLE_DIC_PATH + "conceptDefinitionforEDR.txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "SJIS"));
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
            writer.close();
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

    public static void main(String[] args) {
        boolean isSpecial = false;
        EDR2DoddleDicConverter.setEDRDICPath(isSpecial);
        System.out.println("isSpecial: " + isSpecial);
        System.out.println("make idDefinitionMap");
        EDR2DoddleDicConverter.makeIDDefinitionMap();
        System.out.println("make idDefinitionMap done");
        System.out.println("make wordIDSetMap");
        EDR2DoddleDicConverter.makeWordIDSetMap();
        System.out.println("make wordIDSetMap done");
        System.out.println("clear Map");
        EDR2DoddleDicConverter.clearIDDefinitionMap();
        EDR2DoddleDicConverter.clearWordIDSetMap();
        System.out.println("clear Map done");
        System.out.println("make idSubIDSetMap");
        EDR2DoddleDicConverter.makeIDSubIDSetMap();
        System.out.println("make idSubIDSetMap done");
        if (!isSpecial) {
            System.out.println("clear Map");
            EDR2DoddleDicConverter.clearIDSubIDSetMap();
            System.out.println("make conceptDefinitionMap");
            EDR2DoddleDicConverter.makeConceptDefinitionMap();
            System.out.println("make conceptDefinitionMap done");
        }
    }
}
