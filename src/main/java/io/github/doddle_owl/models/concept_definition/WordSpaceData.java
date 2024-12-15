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

package io.github.doddle_owl.models.concept_definition;

/**
 * @author Takeshi Morita
 */
public class WordSpaceData {

    private int gramNumber;
    private int gramCount;
    private int frontScope;
    private int behindScope;
    private double underValue;

    public WordSpaceData() {
    }

    public WordSpaceData(int gn, int gc, int fs, int bs, double uv) {
        gramNumber = gn;
        gramCount = gc;
        frontScope = fs;
        behindScope = bs;
        underValue = uv;
    }

    public void setGramNumber(int n) {
        gramNumber = n;
    }

    public int getGramNumber() {
        return gramNumber;
    }

    public void setGramCount(int n) {
        gramCount = n;
    }

    public int getGramCount() {
        return gramCount;
    }

    public void setFrontScope(int n) {
        frontScope = n;
    }

    public int getFrontScope() {
        return frontScope;
    }

    public void setBehindScope(int n) {
        behindScope = n;
    }

    public int getBehindScope() {
        return behindScope;
    }

    public void setUnderValue(double n) {
        underValue = n;
    }

    public double getUnderValue() {
        return underValue;
    }
}