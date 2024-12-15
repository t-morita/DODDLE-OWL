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

package io.github.doddle_owl.models.ontology_api;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.dictionary.Dictionary;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.utils.Translator;

import javax.swing.*;
import java.util.*;

/**
 * @author shigeta
 * @author Takeshi Morita
 */
public class WordNet {
    public static boolean isAvailable = false;
    private static Dictionary dictionary;

    public static boolean initWordNetDictionary() {
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
            isAvailable = true;
        } catch (JWNLException e) {
            e.printStackTrace();
            DODDLE_OWL.getLogger().info(Translator.getTerm("WordNetLoadErrorMessage"));
            JOptionPane.showMessageDialog(null, Translator.getTerm("WordNetLoadErrorMessage"));
            isAvailable = false;
        }
        return isAvailable;
    }

    public static IndexWord getIndexWord(POS pos, String word) {
        IndexWord indexWord = null;
        try {
            indexWord = dictionary.lookupIndexWord(pos, word);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return indexWord;
    }

    private final Set<String> adverbConceptSet = new HashSet<>();

    private boolean isAdverb(String concept) {
        if (adverbConceptSet.contains(concept)) {
            return true;
        }
        IndexWord retiw;
        try {
            retiw = dictionary.lookupIndexWord(POS.ADVERB, concept);
        } catch (JWNLException je) {
            retiw = null;
        }
        if (retiw != null) {
            adverbConceptSet.add(concept);
        }
        return (retiw != null);
    }

    private boolean isNumber(String concept) {
        if (concept.startsWith("1") || concept.startsWith("2") || concept.startsWith("3")
                || concept.startsWith("4") || concept.startsWith("5") || concept.startsWith("6")
                || concept.startsWith("7") || concept.startsWith("8") || concept.startsWith("9")
                || concept.startsWith("0")) {
            return true;
        } else return concept.equals("one") || concept.equals("two") || concept.equals("three")
                || concept.equals("four") || concept.equals("five") || concept.equals("six")
                || concept.equals("seven") || concept.equals("eight") || concept.equals("nine")
                || concept.equals("ten") || concept.equals("eleven") || concept.equals("twelve")
                || concept.equals("thirteen") || concept.equals("fourteen")
                || concept.equals("fifteen") || concept.equals("sixteen")
                || concept.equals("seventeen") || concept.equals("eighteen")
                || concept.equals("nineteen") || concept.equals("twenty")
                || concept.equals("thirty") || concept.equals("forty") || concept.equals("fifty")
                || concept.equals("sixty") || concept.equals("seventy") || concept.equals("eighty")
                || concept.equals("ninety") || concept.equals("hundred")
                || concept.equals("thousand");
    }

    private boolean isVerb(String concept) {
        return (concept.equals("be") || concept.equals("are") || concept.equals("take")
                || concept.equals("make") || concept.equals("do") || concept.equals("have")
                || concept.equals("give") || concept.equals("given"));
    }

    private boolean isNogood(String concept) {
        return (concept.equals("ha") || concept.equals("or") || concept.equals("he")
                || concept.equals("at") || concept.equals("who") || concept.equals("wa")
                || concept.equals("ii") || concept.equals("iii"));
    }

    public boolean isInvalid(String concept) {
        return (isVerb(concept) || isAdverb(concept) || isNumber(concept) || isNogood(concept));
    }

    public static Set<Set<String>> getSiblingConceptSet(Long offset) {
        Set<Set<String>> siblingIDSet = new HashSet<>();
        try {
            Synset synset = dictionary.getSynsetAt(POS.NOUN, offset);
            PointerTargetNodeList siblingNodeList = PointerUtils.getCoordinateTerms(synset);
            Set<String> idList = new HashSet<>();
            for (PointerTargetNode node : siblingNodeList) {
                idList.add(Long.toString(node.getSynset().getOffset()));
            }
            siblingIDSet.add(idList);
        } catch (JWNLException jwnle) {
            jwnle.printStackTrace();
        }
        return siblingIDSet;
    }

    public static Set<Set<String>> getSubIDSet(Long offset) {
        Set<Set<String>> subIDSet = new HashSet<>();
        try {
            Synset synset = dictionary.getSynsetAt(POS.NOUN, offset);
            PointerTargetTree hyponymTree = PointerUtils.getHyponymTree(synset);
            // 多重継承を許すようにしている
            List<PointerTargetNodeList> treeNodeLists = hyponymTree.reverse();
            for (PointerTargetNodeList nodeList : treeNodeLists) {
                Set<String> idList = new HashSet<>();
                for (PointerTargetNode node : nodeList) {
                    idList.add(Long.toString(node.getSynset().getOffset()));
                }
                subIDSet.add(idList);
            }
        } catch (JWNLException jwnle) {
            jwnle.printStackTrace();
        }
        return subIDSet;
    }

    public static Set<List<Concept>> getPathToRootSet(Long offset) {
        Set<List<Concept>> pathToRootSet = new HashSet<>();
        try {
            Synset synset = dictionary.getSynsetAt(POS.NOUN, offset);
            PointerTargetTree hypernymTree = PointerUtils.getHypernymTree(synset);
            // 多重継承を許すようにしている
            List<PointerTargetNodeList> treeNodeLists = hypernymTree.reverse();
            for (PointerTargetNodeList nodeList : treeNodeLists) {
                List<Concept> conceptList = new ArrayList<>();
                for (PointerTargetNode node : nodeList) {
                    Concept c = getWNConcept(Long.toString(node.getSynset().getOffset()));
                    conceptList.add(c);
                }
                pathToRootSet.add(conceptList);
            }
        } catch (JWNLException jwnle) {
            jwnle.printStackTrace();
        }
        return pathToRootSet;
    }

    private static boolean isEnglish(String iw) {
        return iw.matches("(\\w|\\s)*");
    }

    public static Set<String> getURISet(String word) {
        Set<String> uriSet = new HashSet<>();
        if (!WordNet.isAvailable) {
            return uriSet;
        }
        if (!isEnglish(word)) {
            return uriSet;
        }
        IndexWord indexWord = WordNet.getNounIndexWord(word);
        if (indexWord == null) {
            return uriSet;
        }
        for (Synset synset : indexWord.getSenses()) {
            if (synset.containsWord(word)) {
                uriSet.add(DODDLEConstants.WN_URI + synset.getOffset());
            }
        }
        return uriSet;
    }

    public static Set<List<String>> getURIPathToRootSet(Long offset) {
        Set<List<String>> pathToRootSet = new HashSet<>();
        try {
            Synset synset = dictionary.getSynsetAt(POS.NOUN, offset);
            PointerTargetTree hypernymTree = PointerUtils.getHypernymTree(synset);
            // 多重継承を許すようにしている
            List<PointerTargetNodeList> treeNodeLists = hypernymTree.reverse();
            for (PointerTargetNodeList nodeList : treeNodeLists) {
                List<String> uriList = new ArrayList<>();
                for (PointerTargetNode node : nodeList) {
                    String uri = DODDLEConstants.WN_URI + node.getSynset().getOffset();
                    uriList.add(uri);
                }
                pathToRootSet.add(uriList);
            }
        } catch (JWNLException jwnle) {
            jwnle.printStackTrace();
        }
        return pathToRootSet;
    }

    private static final Map<String, Concept> idConceptMap = new HashMap<>();

    public static Concept getWNConcept(String id) {
        if (dictionary == null) {
            return null;
        }
        String uri = DODDLEConstants.WN_URI + id;
        try {
            if (idConceptMap.get(uri) != null) {
                return idConceptMap.get(uri);
            }
            Synset synset = dictionary.getSynsetAt(POS.NOUN, Long.valueOf(id));
            Concept c = new Concept(uri, "");
            for (Word word : synset.getWords()) {
                c.addLabel(new DODDLELiteral("en", word.getLemma()));
            }
            c.addDescription(new DODDLELiteral("en", synset.getGloss()));
            idConceptMap.put(uri, c);
            return c;
        } catch (JWNLException | NumberFormatException jwnle) {
            jwnle.printStackTrace();
        }
        return null;
    }

    public static IndexWord getNounIndexWord(String word) {
        try {
            return dictionary.lookupIndexWord(POS.NOUN, word);
        } catch (JWNLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public IndexWord getVerbIndexWord(String word) {
        try {
            return dictionary.lookupIndexWord(POS.VERB, word);
        } catch (JWNLException e) {
            return null;
        }
    }

}