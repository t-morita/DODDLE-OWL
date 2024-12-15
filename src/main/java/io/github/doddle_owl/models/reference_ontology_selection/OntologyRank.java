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

package io.github.doddle_owl.models.reference_ontology_selection;

/**
 * @author Takeshi Morita
 */
public class OntologyRank  implements Comparable{

    private double inputWordCount;   
    private double swoogleOntoRank; // Swoogle's OntoRank
    private double inputWordRatio;    // concept count / input word count
    private int relationCount;             // domain-property-range relation count
    
    public OntologyRank() {
    }

    public void setInputWordCount(double cnt) {
        inputWordCount = cnt;
    }
    
    private double getInputWordRatio() {
        return inputWordRatio;
    }

    public void setInputWordRatio(double inputWordRatio) {
        this.inputWordRatio = inputWordRatio;
    }

    private int getRelationCount() {
        return relationCount;
    }

    public void setRelationCount(int relationCount) {
        this.relationCount = relationCount;
    }

    private double getSwoogleOntoRank() {
        return swoogleOntoRank;
    }

    public void setSwoogleOntoRank(double swoogleOntoRank) {
        this.swoogleOntoRank = swoogleOntoRank;
    }

    public int compareTo(Object obj) {
        OntologyRank ontoRank = (OntologyRank) obj;
        if (ontoRank.getInputWordRatio() < inputWordRatio) {
            return 1;
        } else if (ontoRank.getInputWordRatio() > inputWordRatio) {
            return -1;
        } else {
            if (ontoRank.getRelationCount() < relationCount) {
                return 1;
            } else if (ontoRank.getRelationCount() > relationCount) {
                return -1;
            } else {
                return Double.compare(swoogleOntoRank, ontoRank.getSwoogleOntoRank());
            }
        }
    }
    
    public String toString() {
        return "["+String.format("%.3f", swoogleOntoRank)+"]["+String.format("%.3f", inputWordRatio)+"]["+inputWordCount+"]["+relationCount+"]";
    }
}
