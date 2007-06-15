/*
 * @(#)  2007/05/25
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/**
 * @author takeshi morita
 */
public class DODDLEConstants {

    public static int DIVIDER_SIZE = 10;
    public static final String VERSION = "2007-06-16";

    public static boolean DEBUG = false;
    public static boolean IS_USING_DB = false;
    public static boolean IS_INTEGRATING_SWOOGLE = false;
    public static String LANG = "en"; // DB構築時に必要

    public static final int DOCUMENT_SELECTION_PANEL = 0;
    public static final int INPUT_WORD_SELECTION_PANEL = 1;
    public static final int ONTOLOGY_SELECTION_PANEL = 2;
    public static final int DISAMBIGUATION_PANEL = 3;
    public static final int TAXONOMIC_PANEL = 4;
    public static String BASE_URI = "http://www.yamaguti.comp.ae.keio.ac.jp/doddle#";
    public static final String DODDLE_URI = "http://www.yamaguti.comp.ae.keio.ac.jp/doddle#";
    public static String BASE_PREFIX = "keio";
    public static String EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR#";
    public static String OLD_EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR/";
    public static String EDRT_URI = "http://www2.nict.go.jp/kk/e416/EDRT#";
    public static String WN_URI = "http://wordnet.princeton.edu/wn/";

    public static String EDR_HOME = "C:/DODDLE-OWL/EDR_DIC/";
    public static String EDRT_HOME = "C:/DODDLE-OWL/EDRT_DIC/";
    public static String SEN_HOME = "C:/DODDLE-OWL/SEN_DIC/sen-1.2.1/";
    public static String PROJECT_HOME = "C:/DODDLE-OWL/DODDLEProject/";
    public static String WORDNET_HOME = "C:/program files/wordnet/2.0/dict/";
}
