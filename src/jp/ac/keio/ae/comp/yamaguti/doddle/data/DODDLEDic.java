/*
 * @(#)  2006/04/02
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class DODDLEDic {

    public static Concept getConcept(String uri) {
        Resource res = ResourceFactory.createResource(uri);
        String id = Utils.getLocalName(res);
        String ns = Utils.getNameSpace(res);
        if (ns.equals(DODDLE.EDR_URI)) {
            return EDRDic.getEDRConcept(id);
        } else if (ns.equals(DODDLE.EDRT_URI)) {
            return EDRDic.getEDRTConcept(id);
        } else if (ns.equals(DODDLE.WN_URI)) { return WordNetDic.getWNConcept(id); }
        return null;
    }

    public static Set getIDSet(String word) {
        Set idSet = EDRDic.getEDRIDSet(word);
        if (idSet != null) { return idSet; }
        idSet = EDRDic.getEDRTIDSet(word);
        if (idSet != null) { return idSet; }
        return null;
    }
}
