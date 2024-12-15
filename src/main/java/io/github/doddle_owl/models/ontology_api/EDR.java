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

package io.github.doddle_owl.models.ontology_api;

import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.concept_selection.Concept;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class EDR {
    public static boolean isEDRAvailable = false;
    public static boolean isEDRTAvailable = false;
    private static final String TREE_DATA_FILE = "tree.data";
    private static final String WORD_DATA_FILE = "word.data";
    private static final String CONCEPT_DATA_FILE = "concept.data";

    private static final String TREE_INDEX_FILE = "tree.index";
    private static final String WORD_INDEX_FILE = "word.index";
    private static final String CONCEPT_INDEX_FILE = "concept.index";

    private static RandomAccessFile edrTreeDataFile;
    private static RandomAccessFile edrWordDataFile;
    private static RandomAccessFile edrConceptDataFile;
    private static RandomAccessFile edrRelationDataFile;

    private static RandomAccessFile edrTreeIndexFile;
    private static RandomAccessFile edrWordIndexFile;
    private static RandomAccessFile edrConceptIndexFile;
    private static RandomAccessFile edrRelationIndexFile;

    private static RandomAccessFile edrtTreeDataFile;
    private static RandomAccessFile edrtWordDataFile;
    private static RandomAccessFile edrtConceptDataFile;

    private static RandomAccessFile edrtTreeIndexFile;
    private static RandomAccessFile edrtWordIndexFile;
    private static RandomAccessFile edrtConceptIndexFile;

    private static Map<String, Concept> edrURIConceptMap; // キャッシュ用
    private static Map<String, Concept> edrtURIConceptMap; // キャッシュ用
    private static Map<String, Set<String>> edrWordIDSetMap; // キャッシュ用
    private static Map<String, Set<String>> edrtWordIDSetMap; // キャッシュ用

    public static boolean initEDRDic() {
        if (edrURIConceptMap != null) {
            return isEDRAvailable;
        }
        edrURIConceptMap = new HashMap<>();
        edrWordIDSetMap = new HashMap<>();
        String baseDir = DODDLEConstants.EDR_HOME + File.separator;
        try {
            edrTreeDataFile = new RandomAccessFile(baseDir + TREE_DATA_FILE, "r");
            edrWordDataFile = new RandomAccessFile(baseDir + WORD_DATA_FILE, "r");
            edrConceptDataFile = new RandomAccessFile(baseDir + CONCEPT_DATA_FILE, "r");
            String RELATION_DATA_FILE = "relation.data";
            edrRelationDataFile = new RandomAccessFile(baseDir + RELATION_DATA_FILE, "r");

            edrTreeIndexFile = new RandomAccessFile(baseDir + TREE_INDEX_FILE, "r");
            edrWordIndexFile = new RandomAccessFile(baseDir + WORD_INDEX_FILE, "r");
            edrConceptIndexFile = new RandomAccessFile(baseDir + CONCEPT_INDEX_FILE, "r");
            String RELATION_INDEX_FILE = "relation.index";
            edrRelationIndexFile = new RandomAccessFile(baseDir + RELATION_INDEX_FILE, "r");
            isEDRAvailable = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            isEDRAvailable = false;
        }
        return isEDRAvailable;
    }

    public static boolean initEDRTDic() {
        if (edrtURIConceptMap != null) {
            return isEDRTAvailable;
        }
        edrtURIConceptMap = new HashMap<>();
        edrtWordIDSetMap = new HashMap<>();
        String baseDir = DODDLEConstants.EDRT_HOME + File.separator;
        try {
            edrtTreeDataFile = new RandomAccessFile(baseDir + TREE_DATA_FILE, "r");
            edrtWordDataFile = new RandomAccessFile(baseDir + WORD_DATA_FILE, "r");
            edrtConceptDataFile = new RandomAccessFile(baseDir + CONCEPT_DATA_FILE, "r");

            edrtTreeIndexFile = new RandomAccessFile(baseDir + TREE_INDEX_FILE, "r");
            edrtWordIndexFile = new RandomAccessFile(baseDir + WORD_INDEX_FILE, "r");
            edrtConceptIndexFile = new RandomAccessFile(baseDir + CONCEPT_INDEX_FILE, "r");
            isEDRTAvailable = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            isEDRTAvailable = false;
        }
        return isEDRTAvailable;
    }

    private static long getIndexFpListSize(boolean isSpecial) {
        RandomAccessFile indexFpListFile;
        if (isSpecial) {
            indexFpListFile = edrtWordIndexFile;
        } else {
            indexFpListFile = edrWordIndexFile;
        }
        try {
            return indexFpListFile.length() / 10;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getIndexFp(long fp, boolean isSpecial) {
        RandomAccessFile indexFpListFile;
        if (isSpecial) {
            indexFpListFile = edrtWordIndexFile;
        } else {
            indexFpListFile = edrWordIndexFile;
        }
        try {
            indexFpListFile.seek(fp);
            String fpStr = indexFpListFile.readLine();
            if (fpStr == null) {
                return -1;
            }
            return Long.valueOf(fpStr);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getIndexFileSize(RandomAccessFile indexFile) {
        try {
            return indexFile.length() / 10;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getConceptIndexFileSize(boolean isSpecial) {
        RandomAccessFile indexFile;
        if (isSpecial) {
            indexFile = edrtConceptIndexFile;
        } else {
            indexFile = edrConceptIndexFile;
        }
        return getIndexFileSize(indexFile);
    }

    private static long getTreeIndexFileSize(boolean isSpecial) {
        RandomAccessFile indexFile;
        if (isSpecial) {
            indexFile = edrtTreeIndexFile;
        } else {
            indexFile = edrTreeIndexFile;
        }
        return getIndexFileSize(indexFile);
    }

    private static long getRelationIndexFileSize() {
        return getIndexFileSize(edrRelationIndexFile);
    }

    private static long getDataFp(long fp, RandomAccessFile indexFile) {
        try {
            indexFile.seek(fp);
            return Long.valueOf(indexFile.readLine());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getConceptDataFp(long fp, boolean isSpecial) {
        RandomAccessFile indexFile;
        if (isSpecial) {
            indexFile = edrtConceptIndexFile;
        } else {
            indexFile = edrConceptIndexFile;
        }
        return getDataFp(fp, indexFile);
    }

    private static long getTreeDataFp(long fp, boolean isSpecial) {
        RandomAccessFile indexFile;
        if (isSpecial) {
            indexFile = edrtTreeIndexFile;
        } else {
            indexFile = edrTreeIndexFile;
        }
        return getDataFp(fp, indexFile);
    }

    private static long getRelationDataFp(long fp) {
        return getDataFp(fp, edrRelationIndexFile);
    }

    private static String getTermAndIndexFpSet(long ifp, boolean isSpecial) {
        RandomAccessFile indexFile;
        if (isSpecial) {
            indexFile = edrtWordDataFile;
        } else {
            indexFile = edrWordDataFile;
        }
        try {
            // System.out.println("ifp: " + ifp);
            indexFile.seek(ifp);
            return new String(indexFile.readLine().getBytes("ISO8859_1"), StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static String getData(long dfp, RandomAccessFile dataFile, String encoding) {
        try {
            // System.out.println("dfp: " + dfp);
            dataFile.seek(dfp);
            return new String(dataFile.readLine().getBytes("ISO8859_1"), encoding);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static String getConceptData(long dfp, boolean isSpecial) {
        RandomAccessFile dataFile;
        if (isSpecial) {
            dataFile = edrtConceptDataFile;
        } else {
            dataFile = edrConceptDataFile;
        }
        return getData(dfp, dataFile, "UTF-8");
    }

    private static String getTreeData(long dfp, boolean isSpecial) {
        RandomAccessFile dataFile;
        if (isSpecial) {
            dataFile = edrtTreeDataFile;
        } else {
            dataFile = edrTreeDataFile;
        }
        return getData(dfp, dataFile, "ISO8859_1");
    }

    private static String getRelationData(long dfp) {
        return getData(dfp, edrRelationDataFile, "ISO8859_1");
    }

    private static String getConceptData(boolean isSpecial, String id) {
        long low = 0;
        long conceptIndexFileSize = getConceptIndexFileSize(isSpecial);
        long high = conceptIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            if (conceptIndexFileSize - 1 <= mid) {
                return null;
            }
            long conceptDataFP = getConceptDataFp(mid * 10, isSpecial);
            if (conceptDataFP == -1) {
                return null;
            }
            // System.out.println("mid: " + mid);
            String conceptData = getConceptData(conceptDataFP, isSpecial);
            if (conceptData == null) {
                return null;
            }
            String[] lines = conceptData.split("\t");
            String searchedID = lines[0];
            // System.out.println(searchedID.compareTo(id));
            if (searchedID.compareTo(id) == 0) {
                // System.out.println(conceptData);
                return conceptData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    public static String getTreeData(boolean isSpecial, String id) {
        long low = 0;
        long treeIndexFileSize = getTreeIndexFileSize(isSpecial);
        long high = treeIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            // System.out.println("mid: " + mid);
            if (treeIndexFileSize - 1 <= mid) {
                return null;
            }
            long treeDataFP = getTreeDataFp(mid * 10, isSpecial);
            if (treeDataFP == -1) {
                return null;
            }
            String treeData = getTreeData(treeDataFP, isSpecial);
            if (treeData == null) {
                return null;
            }
            String[] lines = treeData.split("\t");
            String searchedID = lines[0];
            // System.out.println(searchedID.compareTo(id));
            if (searchedID.compareTo(id) == 0) {
                // System.out.println(conceptData);
                return treeData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    private static String getRelationData(String id) {
        long low = 0;
        long relationIndexFileSize = getRelationIndexFileSize();
        long high = relationIndexFileSize;

        while (low <= high) {
            long mid = (low + high) / 2;
            if (relationIndexFileSize - 1 <= mid) {
                return null;
            }
            long relationDataFP = getRelationDataFp(mid * 10);
            if (relationDataFP == -1) {
                return null;
            }
            String relationData = getRelationData(relationDataFP);
            if (relationData == null) {
                return null;
            }
            String[] lines = relationData.split("\t");
            String searchedID = lines[0];
            // System.out.println(searchedID.compareTo(id));
            if (searchedID.compareTo(id) == 0) {
                // System.out.println(conceptData);
                return relationData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    private static Concept getConcept(long dfp, boolean isSpecial) {
        RandomAccessFile dataFile;
        try {
            if (isSpecial) {
                dataFile = edrtConceptDataFile;
            } else {
                dataFile = edrConceptDataFile;
            }
            dataFile.seek(dfp);
            String data = new String(dataFile.readLine().getBytes("ISO8859_1"), StandardCharsets.UTF_8);
            // System.out.println(data);
            String[] dataArray = data.split("\\^");
            String[] conceptData = new String[4];
            String id = dataArray[0].replaceAll("\t", "");
            System.arraycopy(dataArray, 1, conceptData, 0, conceptData.length);

            String uri;
            Concept c;
            if (isSpecial) {
                uri = DODDLEConstants.EDRT_URI + id;
                c = new Concept(uri, conceptData);
                edrtURIConceptMap.put(uri, c);
            } else {
                uri = DODDLEConstants.EDR_URI + id;
                c = new Concept(uri, conceptData);
                edrURIConceptMap.put(uri, c);
            }
            return c;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static Set<Long> getdataFpSet(boolean isSpecial, long high, String term) {
        long low = 0;
        Set<Long> dataFpSet = new HashSet<>();
        while (low <= high) {
            long mid = (low + high) / 2;
            // System.out.println("mid: " + mid);
            long indexFP = getIndexFp(mid * 10, isSpecial);
            if (indexFP == -1) {
                return dataFpSet;
            }
            String line = getTermAndIndexFpSet(indexFP, isSpecial);
            // System.out.println(line);
            String[] lines = line.split("\t");
            String searchedTerm = lines[0];
            // System.out.println(searchedTerm.compareTo(term));
            if (searchedTerm.compareTo(term) == 0) {
                for (int i = 1; i < lines.length; i++) {
                    dataFpSet.add(Long.valueOf(lines[i]));
                }
                // System.out.println(searchedTerm);
                return dataFpSet;
            } else if (0 < searchedTerm.compareTo(term)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return dataFpSet;
    }

    private static Set<String> getIDSet(String word, boolean isSpecial) {
        Map<String, Set<String>> wordIDSetMap;
        if (isSpecial) {
            wordIDSetMap = edrtWordIDSetMap;
        } else {
            wordIDSetMap = edrWordIDSetMap;
        }

        if (wordIDSetMap.get(word) != null) {
            return wordIDSetMap.get(word);
        }
        // System.out.println(word);
        Set<Long> dataFpSet = getdataFpSet(isSpecial, getIndexFpListSize(isSpecial), word);
        // System.out.println(word + ": " + dataFpSet);
        Set<String> idSet = new HashSet<>();
        for (Long dfp : dataFpSet) {
            // System.out.println(dfp);
            Concept c = getConcept(dfp, isSpecial);
            // System.out.println(c.getLocalName());
            idSet.add(c.getLocalName());
        }
        wordIDSetMap.put(word, idSet);

        return idSet;
    }

    public static Set<String> getEDRTIDSet(String word) {
        return getIDSet(word, true);
    }

    public static Set<String> getEDRIDSet(String word) {
        return getIDSet(word, false);
    }

    private static void addURISet(String data, String relation, Set<String> uriSet) {
        String[] idSet = data.split("\\|" + relation)[1].split("\t");
        for (String id : idSet) {
            if (id.contains("|")) {
                break;
            }
            if (!id.equals("")) {
                uriSet.add(DODDLEConstants.EDR_URI + id);
            }
        }
    }

    /**
     * 入力概念集合を入力として，その中から動詞的概念の集合を返す
     */
    public static Set<Concept> getVerbConceptSet(Set<Concept> inputConceptSet) {
        Set<Concept> verbConceptSet = new HashSet<>();
        for (Concept c : inputConceptSet) {
            String id = c.getLocalName();
            String data = getRelationData(id);
            if (data != null && (data.contains("|agent") || data.contains("|object"))) { // agentとobjectの場合のみを考慮
                verbConceptSet.add(c);
            }
        }
        return verbConceptSet;
    }

    public static Set<String> getRelationValueSet(String relation, String vid,
                                                  List<List<Concept>> trimmedConceptList) {
        Set<String> uriSet = new HashSet<>();
        String data = getRelationData(vid);
        if (data != null) {
            if (!data.contains("|" + relation)) {
                return uriSet;
            }
            addURISet(data, relation, uriSet);
        }
        for (List<Concept> conceptList : trimmedConceptList) {
            for (Concept c : conceptList) {
                String tid = c.getLocalName();
                data = getRelationData(tid);
                if (data == null) {
                    continue;
                }
                if (!data.contains("|" + relation)) {
                    continue;
                }
                addURISet(data, relation, uriSet);
            }
        }
        return uriSet;
    }

    private static Concept getConcept(String id, boolean isSpecial) {
        String ns;
        Map<String, Concept> uriConceptMap;
        if (isSpecial) {
            ns = DODDLEConstants.EDRT_URI;
            uriConceptMap = edrtURIConceptMap;
        } else {
            ns = DODDLEConstants.EDR_URI;
            uriConceptMap = edrURIConceptMap;
        }

        String uri = ns + id;
        if (uriConceptMap.get(uri) != null) {
            return uriConceptMap.get(uri);
        }
        String data = getConceptData(isSpecial, id);
        String[] dataArray = data.split("\\^");
        String[] conceptData = new String[4];
        System.arraycopy(dataArray, 1, conceptData, 0, conceptData.length);

        Concept c = new Concept(uri, conceptData);
        uriConceptMap.put(uri, c);
        return c;
    }

    public static Concept getEDRTConcept(String id) {
        return getConcept(id, true);
    }

    public static Concept getEDRConcept(String id) {
        return getConcept(id, false);
    }

}
