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

package io.github.doddle_owl.task_analyzer;

import java.util.*;

/**
 * @author Takeshi Morita
 */
public class Segment {

    private int modificationRelationNum;
    private final List<Morpheme> morphemeList;
    private Segment dependToSegment;
    private Segment dependFromSegment;

    public Segment getDependFromSegment() {
        return dependFromSegment;
    }

    public void setDependFromSegment(Segment dependFromSegment) {
        this.dependFromSegment = dependFromSegment;
    }

    public Segment(int num) {
        morphemeList = new ArrayList<>();
        modificationRelationNum = num;
    }

    public Segment(Segment toSeg) {
        morphemeList = new ArrayList<>();
        dependToSegment = toSeg;
    }

    public Segment getDependToSegment() {
        return dependToSegment;
    }

    public void setDependToSegment(Segment seg) {
        dependToSegment = seg;
    }

    public void addMorpheme(Morpheme m) {
        morphemeList.add(m);
    }

    public int getModificationRelationNum() {
        return modificationRelationNum;
    }

    public void setModificationRelationNum(int num) {
        modificationRelationNum = num;
    }

    public List<Morpheme> getMorphemeList() {
        return morphemeList;
    }

    public boolean equals(Object obj) {
        Segment os = (Segment) obj;
        List<Morpheme> otherMorList = os.getNounMorphemeList();
        List<Morpheme> nounMorList = getNounMorphemeList();
        if (otherMorList.size() == nounMorList.size()) {
            for (int i = 0; i < otherMorList.size(); i++) {
                Morpheme m = nounMorList.get(i);
                Morpheme om = otherMorList.get(i);
                if (!m.equals(om)) { return false; }
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        return 0;
    }

    private List<Morpheme> getNounMorphemeList() {
        List<Morpheme> nounMorList = new ArrayList<>();
        boolean isIncludeSymbolParenthesis = isIncludeSymbolParenthesis();
        for (Morpheme m : morphemeList) {
            if (m.getPos().equals(Morpheme.NOUN_NUM) || m.getPos().contains(Morpheme.VERB)) {
                continue;
            }
            if (m.getSurface().equals("・")) { // 中黒の場合は，名詞句リストに追加
                nounMorList.add(m);
            } else if (m.getPos().contains(Morpheme.NOUN) || m.getPos().contains(Morpheme.VERB)
                    || m.getPos().equals(Morpheme.UNKNOWN_WORD)) {
                // || m.getPos().equals("助詞-接続助詞")
                if (!m.getSurface().equals("他") && !m.getPos().equals("名詞-非自立-助動詞語幹")) {
                    nounMorList.add(m);
                }
            } else if (isIncludeSymbolParenthesis) {
                if (m.getPos().equals(Morpheme.SYMBOL_OPENED_PARENTHESIS)
                        || m.getPos().equals(Morpheme.SYMBOL_CLOSED_PARENTHESIS)) {
                    nounMorList.add(m);
                }
            }
        }
        if (0 < nounMorList.size() && nounMorList.get(0).getSurface().equals("“")
                && nounMorList.get(nounMorList.size() - 1).getSurface().equals("”")) {
            nounMorList.remove(0);
            nounMorList.remove(nounMorList.size() - 1);
        }
        if (0 < nounMorList.size() && nounMorList.get(0).getSurface().equals("・")) { // 先頭行の中黒は削除する
            nounMorList.remove(0);
        }
        return nounMorList;
    }

    public boolean isIncludingNoKaku() {
        for (Morpheme m : getMorphemeList()) {
            if (m.getSurface().equals("の")) { return true; }
        }
        return false;
    }

    public boolean isSubjectSegment() {
        for (Morpheme m : getMorphemeList()) {
            if (m.getSurface().equals("は") || m.getSurface().equals("が")) { return true; }
        }
        return false;
    }

    public boolean isObjectSegment() {
        for (Morpheme m : getMorphemeList()) {
            if (m.getSurface().equals("を")) { return true; }
        }
        return false;
    }

    public boolean isIncludingVerb() {
        for (Morpheme m : morphemeList) {
            if (m.getPos().contains(Morpheme.VERB)) { return true; }
        }
        return false;
    }

    public String getRefinedPhrase() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < morphemeList.size(); i++) {
            Morpheme m = morphemeList.get(i);
            if (m.getPos().contains(Morpheme.SYMBOL_ALPHABET)) {
                continue;
            }
            if (i == 0 && (m.getSurface().equals("　") || m.getSurface().equals("・"))) {
                continue;
            }
            if (i == 0 && m.getPos().contains(Morpheme.NOUN_NUM)) {
                continue;
            }
            if (m.getSurface().equals("他")) {
                continue;
            }
            builder.append(m.getSurface());
        }
        return builder.toString();
    }

    public String getCompoundNounPhrase() {
        List<Morpheme> nounMorphmeList = getNounMorphemeList();
        if (1 < nounMorphmeList.size()) { return getNounPhrase(); }
        return null;
    }

    public String getNounPhrase() {
        StringBuilder builder = new StringBuilder();
        List<Morpheme> nounMorphemeList = getNounMorphemeList();
        for (int i = 0; i < nounMorphemeList.size(); i++) {
            Morpheme m = nounMorphemeList.get(i);
            if (i == nounMorphemeList.size() - 1 && m.getPos().contains(Morpheme.VERB)) {
                builder.append(m.getBasic());
            } else {
                builder.append(m.getSurface());
            }
        }
        return builder.toString();
    }

    public boolean isNounPhrase() {
        boolean isNounPhrase = true;
        for (Morpheme m : morphemeList) {
            if (!m.getPos().contains(Morpheme.NOUN)) { return false; }
        }
        return isNounPhrase;
    }

    private boolean isSymbolOpendParenthesis(Morpheme m) {
        return m.getPos().equals(Morpheme.SYMBOL_OPENED_PARENTHESIS)
                && (m.getSurface().equals("（") || m.getSurface().equals("(") || m.getSurface().equals("“"));
    }

    private boolean isSymbolClosedParenthesis(Morpheme m) {
        return m.getPos().equals(Morpheme.SYMBOL_CLOSED_PARENTHESIS)
                && (m.getSurface().equals("）") || m.getSurface().equals(")") || m.getSurface().equals("”"));
    }

    public boolean isIncludeSymbolParenthesis() {
        boolean isOpenedParenthesis = false;
        boolean isClosedParenthtesis = false;
        for (Morpheme m : morphemeList) {
            if (isSymbolOpendParenthesis(m)) {
                isOpenedParenthesis = true;
            } else if (isSymbolClosedParenthesis(m)) {
                isClosedParenthtesis = true;
            }
        }
        return isOpenedParenthesis && isClosedParenthtesis;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Morpheme m : morphemeList) {
            builder.append(m.toString());
        }
        // builder.append(":");
        // builder.append(modificationRelationNum);
        return builder.toString();
    }
}
