package io.github.doddle_owl.models.ontology_api;

import io.github.doddle_owl.models.common.DODDLEConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReferenceOntologyTest {

    @BeforeEach
    void setUp() {
        DODDLEConstants.EDR_HOME = "/Users/t_morita/DODDLE-OWL/EDR-DIC/";
        DODDLEConstants.EDRT_HOME = "/Users/t_morita/DODDLE-OWL/EDRT-DIC/";
        DODDLEConstants.JWO_HOME = "/Users/t_morita/DODDLE-OWL/jwo";

        EDR.initEDRDic();
        EDR.initEDRTDic();
        JaWordNet.initJPNWNDic();
        WordNet.initWordNetDictionary();
        JWO.initJWODic(null);
    }

    @Test
    @DisplayName("DODDLEDicからEDRのConceptを取得")
    void getEDRConcept() {
        String expected = "dog";
        String actual = ReferenceOntology.getConcept(DODDLEConstants.EDR_URI + "ID3bdc67").getWord();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("DODDLEDicからEDRのConceptを取得")
    void getEDRTConcept() {
        String expected = "ツリー検索";
        String actual = ReferenceOntology.getConcept(DODDLEConstants.EDRT_URI + "ID2deac6").getWord();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("DODDLEDicからWordNetのConceptを取得")
    void getWordNetConcept() {
        String expected = "computer";
        String actual = ReferenceOntology.getConcept(DODDLEConstants.WN_URI + "03086983").getWord();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("DODDLEDicから日本語WordNetのConceptを取得")
    void getJpWordNetConcept() {
        String expected = "urban_area";
        String actual = ReferenceOntology.getConcept(DODDLEConstants.JPN_WN_URI + "08675967-n").getWord();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("DODDLEDicから日本語WikipediaオントロジーのConceptを取得")
    void getJWOConcept() {
        String expected = "大学";
        String actual = ReferenceOntology.getConcept(DODDLEConstants.JWO_URI + URLEncoder.encode("大学", StandardCharsets.UTF_8)).getWord();
        assertEquals(expected, actual);
    }
}