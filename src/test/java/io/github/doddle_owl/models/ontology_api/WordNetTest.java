package io.github.doddle_owl.models.ontology_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WordNetTest {

    @BeforeEach
    void setUp() {
        WordNet.initWordNetDictionary();
    }

    @Test
    void getWN31Concept() {
        String expected = "03086983";
        String actual = WordNet.getWNConcept("03086983").getLocalName();
        assertEquals(expected, actual);
    }

    @Test
    void getWN31NounIndexWord() {
        String expected = "computer";
        String actual = WordNet.getNounIndexWord(expected).getLemma();
        assertEquals(expected, actual);
    }

    @Test
    void getWN31URISet() {
        int expected = 7;
        int actual = WordNet.getURISet("dog").size();
        assertEquals(expected, actual);
    }
}