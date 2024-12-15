package io.github.doddle_owl.models.ontology_api;

import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.utils.OWLOntologyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JWOTest {

    @BeforeEach
    void setUp() {
        DODDLEConstants.JWO_HOME = "/Users/t_morita/DODDLE-OWL/jwo";
        JWO.initJWODic(null);
    }

    @Test
    void initJWODic() {
        assertTrue(JWO.isAvailable);
    }

    @Test
    void getJWOConcept() {
        String expected = "大学";
        String actual = OWLOntologyManager.getConcept(DODDLEConstants.JWO_URI + URLEncoder.encode("大学", StandardCharsets.UTF_8)).getWord();
        assertEquals(expected, actual);
    }
}
