package io.github.doddle_owl.utils;

import io.github.doddle_owl.models.common.ConceptDistanceModel;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.ontology_api.EDR;
import io.github.doddle_owl.models.ontology_api.WordNet;
import io.github.doddle_owl.models.reference_ontology_selection.ReferenceOWLOntology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static io.github.doddle_owl.utils.CalcConceptDistanceUtil.*;
import static org.junit.jupiter.api.Assertions.*;

public class CalcConceptDistanceUtilTest {

    @Nested
    @DisplayName("EDRにおける概念距離計算のテスト")
    class CalcConceptDistanceInEDRTest {
        @BeforeEach
        void setup() {
            DODDLEConstants.EDR_HOME = "/Users/t_morita/DODDLE-OWL/EDR-DIC/";
            EDR.initEDRDic();
        }

        @Test
        @DisplayName("EDRにおける最短概念間距離計算のテスト")
        void getShortestConceptDistanceInEDR() {
            int expected = 11;
            int actual = getShortestConceptDistance(EDR.getEDRConcept("ID3bdc67"), EDR.getEDRConcept("ID3bc83c"));
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("EDRにおける最長概念間距離計算のテスト")
        void getLongestConceptDistanceInEDR() {
            int expected = 17;
            int actual = getLongestConceptDistance(EDR.getEDRConcept("ID3bdc67"), EDR.getEDRConcept("ID3bc83c"));
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("EDRにおける平均概念間距離計算のテスト")
        void getAverageConceptDistanceInEDR() {
            int expected = 14;
            int actual = getAverageConceptDistance(EDR.getEDRConcept("ID3bdc67"), EDR.getEDRConcept("ID3bc83c"));
            assertEquals(expected, actual);
        }

    }

    @Nested
    @DisplayName("EDRTにおける概念距離計算のテスト")
    class CalcConceptDistanceInEDRTTest {
        @BeforeEach
        void setup() {
            DODDLEConstants.EDRT_HOME = "/Users/t_morita/DODDLE-OWL/EDRT-DIC/";
            EDR.initEDRTDic();
        }

        @Test
        @DisplayName("EDRTにおける最短概念間距離計算のテスト")
        void getShortestConceptDistanceInEDR() {
//            fail();
        }

        @Test
        @DisplayName("EDRTにおける最長概念間距離計算のテスト")
        void getLongestConceptDistanceInEDR() {
//            fail();
        }

        @Test
        @DisplayName("EDRTにおける平均概念間距離計算のテスト")
        void getAverageConceptDistanceInEDR() {
//            fail();
        }

    }

    @Nested
    @DisplayName("WordNetにおける概念距離計算のテスト")
    class CalcConceptDistanceInWordNetTest {
        @BeforeEach
        void setup() {
            WordNet.initWordNetDictionary();
        }

        @Test
        @DisplayName("WordNetにおける最短概念間距離計算のテスト")
        void getShortestConceptDistanceInWordNet() {
//            fail();
        }

        @Test
        @DisplayName("WordNetにおける最長概念間距離計算のテスト")
        void getLongestConceptDistanceInWordNet() {
//            fail();
        }

        @Test
        @DisplayName("WordNetにおける平均概念間距離計算のテスト")
        void getAverageConceptDistanceInWordNet() {
//            fail();
        }

    }


    @Nested
    @DisplayName("日本語WordNetにおける概念距離計算のテスト")
    class CalcConceptDistanceInJpWordNetTest {
        @BeforeEach
        void setup() {
        }

        @Test
        @DisplayName("日本語WordNetにおける最短概念間距離計算のテスト")
        void getShortestConceptDistanceInJpWordNet() {
//            fail();
        }

        @Test
        @DisplayName("日本語WordNetにおける最長概念間距離計算のテスト")
        void getLongestConceptDistanceInJpWordNet() {
//            fail();
        }

        @Test
        @DisplayName("日本語WordNetにおける平均概念間距離計算のテスト")
        void getAverageConceptDistanceInJpWordNet() {
//            fail();
//            int expected = 1;
//            int actual = getAverageConceptDistance(JaWordNet.getConcept("2001223"), JaWordNet.getConcept("2037721"));
//            assertEquals(expected, actual);
        }
    }

    static void getOWLConceptDistance(String fname, String uri1, String uri2) {
        OWLOntologyManager.addRefOntology(new File(fname));
        Concept c1 = OWLOntologyManager.getConcept(uri1);
        Concept c2 = OWLOntologyManager.getConcept(uri2);

        ConceptDistanceModel resultModel = null;
        List<ConceptDistanceModel> cdModelList = getConceptDistanceModelList(c1, c2);
        for (ConceptDistanceModel cdModel : cdModelList) {
            if (resultModel == null) {
                resultModel = cdModel;
                continue;
            }
            if (resultModel.getShortestCommonAncestorDepth() < cdModel.getShortestCommonAncestorDepth()) {
                resultModel = cdModel;
            }
        }
        System.err.println(resultModel);
    }

    static void getOWLLongestDepth(String fname) {
        OWLOntologyManager.addRefOntology(new File(fname));
        ReferenceOWLOntology refOnt = OWLOntologyManager.getRefOntology(new File(fname).getAbsolutePath());
        int depth = 0;
        Set<String> classSet = refOnt.getClassSet();
        for (String cls : classSet) {
            Set<List<Concept>> pathToRootSet = refOnt.getPathToRootSet(cls);
            for (List<Concept> path : pathToRootSet) {
                if (depth < path.size()) {
                    depth = path.size();
                    System.out.println(path);
                }
            }
        }
        System.out.println("depth: " + depth);
    }


    static void testWNConceptDistance() {
        System.out.println(getAverageConceptDistance(WordNet.getWNConcept("2001223"), WordNet.getWNConcept("2037721")));
    }

    static void testOwlLongestDepth(String[] args) {
        if (args.length != 2) {
            return;
        }
        Translator.loadDODDLEComponentOntology(DODDLEConstants.LANG);
        String ontFileName = args[0];
        String inputFileName = args[1];

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName),
                    StandardCharsets.UTF_8));
            // while (reader.ready()) {
            // String line = reader.readLine();
            // String[] uris = line.split("\\s+");
            // getOWLConceptDistance(ontFileName, uris[0], uris[1]);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getOWLLongestDepth(ontFileName);
    }
}
