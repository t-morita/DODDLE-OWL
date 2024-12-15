/*
 * Project Name: DODDLE-OWL (a Domain Ontology rapiD DeveLopment Environment - OWL extension)
 * Project Website: https://doddle-owl.github.io/
 *
 * Copyright (C) 2004-2024 Takeshi Morita. All rights reserved.
 *
 * This file is part of DODDLE-OWL.
 *
 * DODDLE-OWL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DODDLE-OWL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DODDLE-OWL.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.github.doddle_owl.models.common;

/**
 * @author Takeshi Morita
 */
public class DODDLEConstants {

    public static final int DIVIDER_SIZE = 10;
    public static final String VERSION = "24.12.1";

    public static boolean DEBUG = false;
    public static boolean IS_INTEGRATING_SWOOGLE = true;
    public static String LANG = "en"; // DB構築時に必要

    public static final int ONTOLOGY_SELECTION_PANEL = 0;
    public static final int INPUT_DOCUMENT_SELECTION_PANEL = 1;
    public static final int INPUT_WORD_SELECTION_PANEL = 2;
    public static final int DISAMBIGUATION_PANEL = 3;
    public static final int TAXONOMIC_PANEL = 4;
    public static String BASE_URI = "https://doddle-owl.github.io#";
    public static final String DODDLE_URI = "https://doddle-owl.github.io#";
    public static String BASE_PREFIX = "doddle-owl";
    public static final String EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR#";
    public static final String OLD_EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR/";
    public static final String EDRT_URI = "http://www2.nict.go.jp/kk/e416/EDRT#";
    public static final String WN_URI = "http://wordnet.princeton.edu/wn/";
    public static final String JPN_WN_URI = "http://nlpwww.nict.go.jp/wn-ja/";
    public static final String JWO_URI = "http://www.wikipediaontology.org/class/";

    public static String EDR_HOME = "C:/DODDLE-OWL/EDR_DIC/";
    public static String EDRT_HOME = "C:/DODDLE-OWL/EDRT_DIC/";
    public static String JWN_HOME = "C:/DODDLE-OWL/jwn/";
    public static String JWO_HOME = "C:/DODDLE-OWL/jwo/";
    public static String PROJECT_HOME = "./";
}