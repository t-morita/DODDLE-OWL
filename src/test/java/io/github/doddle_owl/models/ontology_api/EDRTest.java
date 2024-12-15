package io.github.doddle_owl.models.ontology_api;

import io.github.doddle_owl.models.common.DODDLEConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EDRTest {

    @BeforeEach
    void setUp() {
        DODDLEConstants.EDR_HOME = "/Users/t_morita/DODDLE-OWL/EDR-DIC/";
        DODDLEConstants.EDRT_HOME = "/Users/t_morita/DODDLE-OWL/EDRT-DIC/";
        EDR.initEDRDic();
        EDR.initEDRTDic();
    }

    @Test
    void getEDRConcept() {
        String expected = "dog";
        String actual = EDR.getEDRConcept("ID3bdc67").getWord();
        assertEquals(expected, actual);
    }

    @Test
    void getEDRTConcept() {
        String expected = "ツリー検索";
        String actual = EDR.getEDRTConcept("ID2deac6").getWord();
        assertEquals(expected, actual);
    }

}