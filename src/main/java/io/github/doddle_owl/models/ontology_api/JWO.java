package io.github.doddle_owl.models.ontology_api;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.reference_ontology_selection.ReferenceOWLOntology;
import io.github.doddle_owl.utils.OWLOntologyManager;
import io.github.doddle_owl.views.reference_ontology_selection.NameSpaceTable;
import org.apache.jena.tdb1.TDB1Factory;

import java.io.File;

public class JWO {

    private static Dataset dataset;
    public static boolean isAvailable = false;

    public static boolean initJWODic(NameSpaceTable nameSpaceTable) {
        File jwoDir = new File(DODDLEConstants.JWO_HOME);
        if (jwoDir.exists()) {
            if (OWLOntologyManager.getRefOntology(jwoDir.getAbsolutePath()) == null) {
                dataset = TDB1Factory.createDataset(jwoDir.getAbsolutePath());
                Model ontModel = dataset.getDefaultModel();
                ReferenceOWLOntology refOnt = new ReferenceOWLOntology(ontModel, jwoDir.getAbsolutePath(), nameSpaceTable);
                OWLOntologyManager.addRefOntology(refOnt.getURI(), refOnt);
            }
            isAvailable = true;
        } else {
            isAvailable = false;
        }
        return isAvailable;
    }

    public static void closeDataSet() {
        if (dataset != null) {
            dataset.close();
        }
    }
}
