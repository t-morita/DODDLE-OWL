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
 * @author shigeta
 * @author Takeshi Morita
 */
public class ConceptPair implements Comparable {

    private String fromConcept;
    private String toConcept;
    private Double relationValue = 0.0;

    private Double wsValue = 0.0;
    private Double arValue = 0.0;
    private Double nvValue = 0.0;
    private Double sumValue = 0.0;

    private boolean isCorrectPair = false;
    private boolean isNotFoundPair = false;

    public int compareTo(Object o) {
        ConceptPair pair = (ConceptPair) o;
        if (pair.getRelatoinValue().equals(this.getRelatoinValue())) {
            return pair.getToConceptLabel().compareTo(
                    this.getToConceptLabel());
        }
        return pair.getRelatoinValue().compareTo(this.getRelatoinValue());
    }

    public void setRelationValue(String method) {
        // method == ws or ar or nv
        switch (method) {
            case "ws":
                wsValue = relationValue;
                break;
            case "ar":
                arValue = relationValue;
                break;
            case "nv":
                nvValue = relationValue;
                break;
        }
    }

    public void setSumValue() {
        double sum;
        sum = wsValue + arValue + nvValue;
        sumValue = sum;
    }

    public ConceptPair(String A, String B, Double value) {
        fromConcept = A;
        toConcept = B;
        relationValue = value;
    }

    public ConceptPair(String A, String B) {
        fromConcept = A;
        toConcept = B;
    }

    public ConceptPair() {
    }

    public void setValues(String fromC, String toC, Double value) {
        fromConcept = fromC;
        toConcept = toC;
        relationValue = value;
    }

    private String getCombinationToString() {
        return fromConcept + "->" + toConcept;
    }

    public String toString() {
        return fromConcept + "-->" + toConcept + "\t\t" + relationValue;
    }

    public String toIdentifier() {
        return fromConcept + ":" + toConcept;
    }

    /**
     * 引数のNonTaxonomicRelationPair の値を加算
     *
     * @param aPair
     */
    public void addValues(ConceptPair aPair) {
        double wsDouble;
        double arDouble;
        double nvDouble;
        double sumDouble;

        wsDouble = wsValue + aPair.getWsValue();
        arDouble = arValue + aPair.getArValue();
        nvDouble = nvValue + aPair.getNvValue();
        sumDouble = wsDouble + arDouble + nvDouble;

        wsValue = wsDouble;
        arValue = arDouble;
        nvValue = nvDouble;
        sumValue = sumDouble;
    }

    public String toOffsetAndNameString() {
        return fromConcept + "-->" + toConcept + "=" + relationValue;
    }

    public String[] getTableData() {
        String[] str = new String[2];
        str[0] = toConcept;
        str[1] = String.format("%.3f", Double.parseDouble(relationValue.toString()));
        return str;
    }

    public String getToConceptLabel() {
        return toConcept;
    }

    public String getFromConceptLabel() {
        return fromConcept;
    }

    public boolean isSameCombination(ConceptPair pair) {
        return (pair.getCombinationToString().equals(this.getCombinationToString()));
    }

    public boolean isSameCombination(String str) {
        return (this.getCombinationToString().equals(str));
    }

    public Double getRelatoinValue() {
        return relationValue;
    }

    public double getRelationDoubleValue() {
        return relationValue;
    }

    private Double getArValue() {
        return arValue;
    }

    public boolean isCorrectPair() {
        return isCorrectPair;
    }

    private Double getNvValue() {
        return nvValue;
    }

    public Double getSumValue() {
        return sumValue;
    }

    private Double getWsValue() {
        return wsValue;
    }

    /**
     * @param double1
     */
    public void setArValue(Double double1) {
        arValue = double1;
    }

    /**
     * @param b
     */
    public void setCorrectPair(boolean b) {
        isCorrectPair = b;
    }

    /**
     * @param double1
     */
    public void setNvValue(Double double1) {
        nvValue = double1;
    }

    /**
     * @param double1
     */
    public void setSumValue(Double double1) {
        sumValue = double1;
    }

    /**
     * @param double1
     */
    public void setWsValue(Double double1) {
        wsValue = double1;
    }

    public boolean isNotFoundPair() {
        return isNotFoundPair;
    }

    /**
     * @param b
     */
    public void setNotFoundPair(boolean b) {
        isNotFoundPair = b;
    }
}