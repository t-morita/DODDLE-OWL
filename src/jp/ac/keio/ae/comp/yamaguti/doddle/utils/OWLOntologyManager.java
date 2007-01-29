/*
 * @(#)  2006/12/16
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

/**
 * @author takeshi morita
 */
public class OWLOntologyManager {

    private static Map<String, ReferenceOWLOntology> refOntMap = new HashMap<String, ReferenceOWLOntology>();
    
    public static void addRefOntology(String uri, ReferenceOWLOntology ontInfo) {
        refOntMap.put(uri, ontInfo);
    }

    public static void removeRefOntology(String uri) {
        refOntMap.remove(uri);
    }
    
    public static Collection<ReferenceOWLOntology> getRefOntologySet() {
        return refOntMap.values();
    }
    
}
