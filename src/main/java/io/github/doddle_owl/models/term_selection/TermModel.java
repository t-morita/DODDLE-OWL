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

package io.github.doddle_owl.models.term_selection;

import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.task_analyzer.Morpheme;
import io.github.doddle_owl.utils.Translator;

import java.util.Iterator;
import java.util.List;

/**
 * @author Takeshi Morita
 */
public class TermModel implements Comparable {

    private final int matchedPoint;
    private final int ambiguousCnt;
    private final String inputWord;
    private final String matchedInputWord;
    private final List<Morpheme> morphemeList;
    private final String wordListStr;
    private boolean isSystemAdded;

    private final DODDLEProjectPanel project;

    public TermModel(String w, List<Morpheme> mList, String miw, int ac, int mp, DODDLEProjectPanel p) {
        project = p;
        inputWord = w;
        morphemeList = mList;
        matchedInputWord = miw;
        StringBuilder buf = new StringBuilder("(");
        for (Iterator i = morphemeList.iterator(); i.hasNext(); ) {
            Morpheme m = (Morpheme) i.next();
            String word = m.getBasic();
            if (i.hasNext()) {
                buf.append(word).append("+");
            } else {
                buf.append(word).append(")");
            }
        }
        wordListStr = buf.toString();
        ambiguousCnt = ac;
        matchedPoint = mp;
    }

    public void setIsSystemAdded(boolean t) {
        isSystemAdded = t;
    }

    public boolean isSystemAdded() {
        return isSystemAdded;
    }

    // 部分照合かどうか
    public boolean isPartiallyMatchTerm() {
        // 1 < wordList.size()の条件を2006/10/5に追加
        // 「打合せ」が「打合す」と照合してしまうため
        return !inputWord.equals(matchedInputWord) && 1 < morphemeList.size();
    }

    // 完全照合かどうか
    private boolean isPerfectlyMatchWord() {
        return !isPartiallyMatchTerm();
    }

    public int compareTo(Object o) {
        TermModel oiwModel = (TermModel) o;
        int onum = oiwModel.getAmbiguousCnt();
        String oword = oiwModel.getTerm();
        if (this.ambiguousCnt < onum) {
            return 1;
        } else if (this.ambiguousCnt > onum) {
            return -1;
        } else {
            return oword.compareTo(inputWord);
        }
    }

    public String getTerm() {
        return inputWord;
    }

    public String getMatchedTerm() {
        return matchedInputWord;
    }

    public int getMatchedPoint() {
        return matchedPoint;
    }

    private int getAmbiguousCnt() {
        return ambiguousCnt;
    }

    public List<Morpheme> getMorphemeList() {
        return morphemeList;
    }

    public String getTopBasicWord() {
        return morphemeList.get(0).getBasic();
    }

    public String getBasicWordWithoutTopWord() {
        StringBuilder builder = new StringBuilder();
        for (Morpheme m : morphemeList) {
            builder.append(m.getBasic());
        }
        return builder.toString();
    }

    public int getCompoundWordLength() {
        return morphemeList.size();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(inputWord);
        if (isPartiallyMatchTerm() && project.isPartiallyMatchedCompoundWordCheckBox()) {
            buf.append(" ").append(wordListStr);
        }
        if (isPartiallyMatchTerm() && project.isPartiallyMatchedMatchedWordBox()) {
            buf.append(" (").append(matchedInputWord).append(") ");
        }
        if (isPartiallyMatchTerm() && project.isPartiallyMatchedAmbiguityCntCheckBox()) {
            buf.append(" (").append(ambiguousCnt).append(")");
        }
        if (isPerfectlyMatchWord() && project.isPerfectlyMatchedAmbiguityCntCheckBox()) {
            buf.append(" (").append(ambiguousCnt).append(")");
        }
        if (isSystemAdded() && project.isPerfectlyMatchedSystemAddedWordCheckBox()) {
            buf.append(" (").append(Translator.getTerm("SystemAddedLabel")).append(")");
        }
        return buf.toString();
    }
}
