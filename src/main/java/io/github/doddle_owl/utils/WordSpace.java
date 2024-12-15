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

package io.github.doddle_owl.utils;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.models.concept_definition.ConceptPair;
import io.github.doddle_owl.models.concept_definition.WordSpaceData;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.document_selection.Document;
import io.github.doddle_owl.views.concept_definition.ConceptDefinitionPanel;

import java.util.*;

/**
 * @author Takeshi Morita
 */
public class WordSpace {

    private List<String> corpusTokenList;

    private final Map<String, Integer> gramNumMap;
    private Map<String, List<ConceptPair>> wordSpaceResult;

    private WordSpaceData wsData;
    private final List<ConceptPair> allConceptPairs;

    private final List<String> inputWordList;
    private final Document document;
    private final ConceptDefinitionPanel conceptDefinitionPanel;

    public WordSpace(ConceptDefinitionPanel cdp, Document doc) {
        document = doc;
        conceptDefinitionPanel = cdp;
        inputWordList = conceptDefinitionPanel.getInputTermList();

        gramNumMap = new HashMap<>();
        wordSpaceResult = new HashMap<>();
        allConceptPairs = new ArrayList<>();
        makeTokenList(doc, inputWordList);
    }

    public Document getDocument() {
        return document;
    }

    public void setWSData(WordSpaceData d) {
        wsData = d;
    }

    private void makeTokenList(Document doc, List<String> inputWordList) {
        if (doc.getLang().equals("ja")) {
            // makeJaTokenList(DocumentSelectionPanel.getTextString(doc),
            // inputWordList);
            makeJaTokenList(doc.getText(), inputWordList);
        } else if (doc.getLang().equals("en")) {
            // makeEnTokenList(DocumentSelectionPanel.getTextString(doc));
            makeEnTokenList(doc.getText());
        }
    }

    private void makeEnTokenList(String text) {
        if (text == null) {
            return;
        }
        corpusTokenList = new ArrayList<>();
        text = text.replaceAll("\\.|．", "");
        for (String token : text.split("\\s+")) {
            corpusTokenList.add(token.toLowerCase());
        }
        Utils.addEnCompoundWord(corpusTokenList, inputWordList); // 以下，複合語の追加
    }

    private void makeJaTokenList(String text, List<String> inputWordList) {
        corpusTokenList = new ArrayList<>();
        if (text == null) {
            return;
        }
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokenList = tokenizer.tokenize(text);
        for (Token token : tokenList) {
            String basicStr = token.getBaseForm();
            if (basicStr.equals("*")) {
                basicStr = token.getSurface();
            }
            corpusTokenList.add(basicStr);
        }
        Utils.addJaCompoundWord(corpusTokenList, inputWordList); // 以下，複合語の追加
    }

    public Map<String, List<ConceptPair>> calcWordSpaceResult(List<String> targetInputWordList) {
        allConceptPairs.clear();
        if (corpusTokenList.size() == 0) {
            return null;
        }
        if (0 < wsData.getGramNumber()) {
            for (int i = 1; i <= wsData.getGramNumber(); i++) {
                setGram(i, corpusTokenList);
            }
        }
        DODDLE_OWL.STATUS_BAR.addProjectValue();
        // System.out.println("all gram num" + gramNumMap.size());
        List<String> gramText = makeGramText(corpusTokenList);
        DODDLE_OWL.STATUS_BAR.addProjectValue();
        // System.out.println("gram txt: " + gramText);
        int allGramNum = gramNumMap.size();
        gramNumMap.clear();
        int[][] matrix = getGramMatrix(allGramNum, gramText);
        DODDLE_OWL.STATUS_BAR.addProjectValue();
        wordSpaceResult = getWordSpaceResult(matrix, allGramNum, targetInputWordList);
        DODDLE_OWL.STATUS_BAR.addProjectValue();
        // System.out.println("result: " + wordSpaceResult);
        return wordSpaceResult;
    }

    public Map<String, List<ConceptPair>> getWordSpaceResult() {
        return wordSpaceResult;
    }

    private int[][] getGramMatrix(int allGramNum, List<String> gramText) {
        // System.out.println("All Gram Num: " + allGramNum);// +++
        // System.out.println("Gram Text Size: " + gramText.size());

        int new_gram_number = 1;
        int[][] matrix = new int[allGramNum][allGramNum];
        gramNumMap.put(gramText.get(0), 0);

        for (int i = 0; i < gramText.size(); i++) {
            int row = gramNumMap.get(gramText.get(i));
            for (int j = 1; j <= wsData.getFrontScope(); j++) {
                int place = i - j;
                if (place < 0) {
                    break;
                }
                int col = gramNumMap.get(gramText.get(place));
                matrix[row][col]++;
            }

            for (int j = 1; j <= wsData.getBehindScope(); j++) {
                int place = i + j;
                if (place >= gramText.size()) {
                    break;
                }
                int col;
                String gram = gramText.get(place);
                if (gramNumMap.containsKey(gram)) {
                    col = gramNumMap.get(gram);
                } else {
                    gramNumMap.put(gram, new_gram_number);
                    col = new_gram_number;
                    new_gram_number++;
                }
                matrix[row][col]++;
            }
        }
        return matrix;
    }

    private Map<String, List<ConceptPair>> getWordSpaceResult(int[][] matrix, int allGramNum,
                                                              List<String> targetInputWordList) {
        Map<String, List<ConceptPair>> wordPairMap = new HashMap<>();

        for (int i = 0; i < targetInputWordList.size(); i++) {
            String w1 = targetInputWordList.get(i);
            List<ConceptPair> pairList = new ArrayList<>();
            for (int j = 0; j < targetInputWordList.size(); j++) {
                String w2 = targetInputWordList.get(j);
                if (i != j) {
                    Concept c1 = conceptDefinitionPanel.getConcept(w1);
                    if (c1 == null) {
                        c1 = new Concept();
                        c1.addLabel(new DODDLELiteral("ja", w1));
                    }
                    Concept c2 = conceptDefinitionPanel.getConcept(w2);
                    if (c2 == null) {
                        c2 = new Concept();
                        c2.addLabel(new DODDLELiteral("ja", w2));
                    }
                    Double similarity = getSimilarityValue(c1, c2, matrix, allGramNum);
                    // System.out.println(c1 + "=>" + c2 + " = " + similarity);
                    if (wsData.getUnderValue() <= similarity) {
                        ConceptPair pair = new ConceptPair(w1, w2, similarity);
                        allConceptPairs.add(pair);
                        pairList.add(pair);
                    }
                }
            }
            wordPairMap.put(w1, pairList);
        }

        return wordPairMap;
    }

    private void setVec(int[] vec, List<String> cLabelList, int[][] matrix, int allGramNum) {
        for (String w1 : cLabelList) {
            // System.out.println("----" + w1);
            if (gramNumMap.containsKey(w1)) {
                for (int j = 0; j < allGramNum; j++) {
                    vec[j] += matrix[gramNumMap.get(w1)][j];
                }
            }
        }
    }

    /**
     * 概念の見出しを同義語集合として，それらとの共起関係も計算
     */
    private void setLabelList(Concept c, List<String> conceptLabelList) {
        if (c == null) {
            return;
        }
        Map<String, List<DODDLELiteral>> langListMap = c.getLangLabelListMap();
        for (List<DODDLELiteral> labelList : langListMap.values()) {
            for (DODDLELiteral label : labelList) {
                // 英語ラベルと日本語ラベルを両方とも考慮
                conceptLabelList.add(label.getString());
            }
        }
    }

    private Double getSimilarityValue(Concept c1, Concept c2, int[][] matrix, int allGramNum) {
        List<String> c1LabelList = new ArrayList<>();
        List<String> c2LabelList = new ArrayList<>();

        setLabelList(c1, c1LabelList);
        setLabelList(c2, c2LabelList);

        int[] vec1 = new int[allGramNum];
        int[] vec2 = new int[allGramNum];

        // System.out.println("C1::" + c1LabelList);
        // System.out.println("C2::" + c2LabelList);
        for (int i = 0; i < allGramNum; i++) {
            vec1[i] = 0;
            vec2[i] = 0;
        }

        setVec(vec1, c1LabelList, matrix, allGramNum);
        setVec(vec2, c2LabelList, matrix, allGramNum);

        double absVec1 = 0;
        double absVec2 = 0;
        double innerProduct = 0;
        for (int i = 0; i < allGramNum; i++) {
            // System.out.println(concept1[i] + "--" + concept2[i]);
            innerProduct += vec1[i] * vec2[i];
            absVec1 += Math.pow(vec1[i], 2);
            absVec2 += Math.pow(vec2[i], 2);
        }
        absVec1 = StrictMath.sqrt(absVec1);
        absVec2 = StrictMath.sqrt(absVec2);
        double similarity = innerProduct / (absVec1 * absVec2);
        if (innerProduct == 0) {
            return 0.0;
        }
        return similarity;
    }

    /*
     * tokenListは，入力文書の形態素の配列
     */
    private void setGram(int gramNum, List<String> tokenList) {
        for (Iterator<String> i = tokenList.iterator(); i.hasNext(); ) {
            StringBuilder gramBuf = new StringBuilder();
            for (int j = 0; j < gramNum; j++) {
                if (i.hasNext()) {
                    gramBuf.append(i.next());
                    gramBuf.append("_");
                }
            }
            String gram = gramBuf.substring(0, gramBuf.length() - 1);
            if (gramNumMap.containsKey(gram)) {
                int num = gramNumMap.get(gram);
                gramNumMap.put(gram, num + 1);
            } else {
                gramNumMap.put(gram, 1);
            }
        }
    }

    private List<String> makeGramText(List<String> tokenList) {
        List<String> gramText = new ArrayList<>();
        for (Iterator<String> i = tokenList.iterator(); i.hasNext(); ) {
            List<String> gramList = new ArrayList<>();
            for (int j = 0; j < wsData.getGramNumber(); j++) {
                if (i.hasNext()) {
                    gramList.add(i.next());
                }
            }
            addUsableGramToList(gramList, gramText);
        }
        return gramText;
    }

    private boolean isInputWord(String key) {
        return inputWordList.contains(key);
    }

    private boolean isUsableGram(String key) {
        if (gramNumMap.containsKey(key)) {
            int num = gramNumMap.get(key);
            if (wsData.getGramCount() <= num || isInputWord(key)) {
                return true;
            }
            gramNumMap.remove(key);
        }
        return false;
    }

    private List<String> addUsableGramToList(List<String> gramList, List<String> gramText) {
        for (int i = 1; i <= wsData.getGramNumber(); i++) {
            for (int j = 0; j <= wsData.getGramNumber() - i; j++) {
                StringBuilder gramBuf = new StringBuilder();
                for (int k = 0; k < i; k++) {
                    if ((k + j) < gramList.size()) {
                        gramBuf.append(gramList.get(k + j));
                        gramBuf.append("_");
                    }
                }
                if (0 < gramBuf.length()) {
                    String gram = gramBuf.substring(0, gramBuf.length() - 1);
                    if (isUsableGram(gram)) {
                        gramText.add(gram);
                    }
                }
            }
        }
        return gramText;
    }

    public List<ConceptPair> getAllConceptPairs() {
        return allConceptPairs;
    }
}