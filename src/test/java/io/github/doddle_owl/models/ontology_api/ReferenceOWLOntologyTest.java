package io.github.doddle_owl.models.ontology_api;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.reference_ontology_selection.ReferenceOWLOntology;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.views.reference_ontology_selection.NameSpaceTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

public class ReferenceOWLOntologyTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getClassSet() {
//        assertTrue(false);
    }

    @Test
    void getPropertySet() {
//        assertTrue(false);
    }

    @Test
    void getDomainSet() {
//        assertTrue(false);
    }

    @Test
    void getRangeSet() {
//        assertTrue(false);
    }

    @Test
    void getURISet() {
//        assertTrue(false);
    }

    @Test
    void getConcept() {
//        assertTrue(false);
    }


    static void main(String[] args) {
        try {
            Translator.loadDODDLEComponentOntology(DODDLEConstants.LANG);
            NameSpaceTable nsTable = new NameSpaceTable();
            Model ontModel = ModelFactory.createDefaultModel();
            String fileName = DODDLEConstants.PROJECT_HOME + "test.owl";
            ontModel.read(new FileInputStream(fileName), DODDLEConstants.BASE_URI, "RDF/XML");
            ReferenceOWLOntology info = new ReferenceOWLOntology(ontModel, fileName, nsTable);
            System.out.println("res: " + info.getURISet("Resource")); // すべて小文字に変換しているため
            System.out.println(info.getURISet("animal"));
            System.out.println(info.getURISet("dog"));
            System.out.println(info.getURISet("cat"));
            System.out.println(info.getURISet("動物"));
            System.out.println(info.getURISet("犬"));
            System.out.println(info.getURISet("猫"));
            System.out.println(info.getURISet("ひっかく"));
            System.out.println(info.getURISet("bow"));
            Concept c = info.getConcept("http://mmm.semanticweb.org/mr3#animal");
            System.out.println("word: " + c.getWord());
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#cate"));
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#cat").getWord());
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#testdog").getWord());
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#animal").getWord());
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#bow").getWord());
            System.out.println(info.getConcept("http://mmm.semanticweb.org/mr3#hikkaku").getWord());
            System.out.println(info.getDomainSet("http://mmm.semanticweb.org/mr3#bow"));
            System.out.println(info.getRangeSet("http://mmm.semanticweb.org/mr3#bow"));
            System.out.println(info.getDomainSet("http://mmm.semanticweb.org/mr3#hikkaku"));
            System.out.println(info.getRangeSet("http://mmm.semanticweb.org/mr3#hikkaku"));
            Set<List<Concept>> pathToRootSet = info
                    .getPathToRootSet("http://mmm.semanticweb.org/mr3#specialSiamese");
            System.out.println("path to root: " + pathToRootSet);
            System.out.println(pathToRootSet.size());
        } catch (FileNotFoundException fne) {
            fne.printStackTrace();
        }
    }
}