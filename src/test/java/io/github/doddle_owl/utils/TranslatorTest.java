package io.github.doddle_owl.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslatorTest {

    @BeforeEach
    void setup() {
        Translator.loadDODDLEComponentOntology("en");
    }

    @Test
    void getTerm() {
        String expected = "DODDLE-OWL";
        String actual = Translator.getTerm("ApplicationName");
        assertEquals(expected, actual);
    }

    @Test
    void getDescription() {
        String expected = "Do you want to quit DODDLE-OWL?";
        String actual = Translator.getDescription("QuitAction");
        assertEquals(expected, actual);
    }
}