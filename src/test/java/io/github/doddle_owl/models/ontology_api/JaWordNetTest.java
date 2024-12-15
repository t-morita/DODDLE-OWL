package io.github.doddle_owl.models.ontology_api;

import io.github.doddle_owl.models.common.DODDLEConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JaWordNetTest {

    @BeforeEach
    void setUp() {
        DODDLEConstants.JWN_HOME = "/Users/t_morita/DODDLE-OWL/jpwn_dict_1.1";
        JaWordNet.initJPNWNDic();
    }

    @Test
    void getSynsetSet() {
        int expected = 4;
        int actual = JaWordNet.getJPNWNSynsetSet("食べる").size();
        assertEquals(expected, actual);
    }

    @Test
    void getConcept() {
        String expected = "urban_area";
        String actual = JaWordNet.getConcept("08675967-n").getWord();
        assertEquals(expected, actual);
    }

}