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

import io.github.doddle_owl.models.concept_selection.Concept;

/**
 * @author Takeshi Morita
 */
public class NonTaxonomicRelation {
    private String domain;
    private String range;
    private Concept relation;
    private boolean isMetaProperty;

    public NonTaxonomicRelation(String d, String r) {
        domain = d;
        range = r;
    }

    public NonTaxonomicRelation(String d, Concept rel, String r) {
        domain = d;
        range = r;
        relation = rel;
    }

    public boolean isValid() {
        return !(domain.equals("") || range.equals(""));
    }

    public boolean isSameRelation(Concept rel) {
        if (rel == null || relation == null) { return false; }
        return rel.equals(relation);
    }

    @Override
    public boolean equals(Object obj) {
        NonTaxonomicRelation nonTaxRel = (NonTaxonomicRelation) obj;
        return nonTaxRel.getDomain().equals(domain) && nonTaxRel.getRange().equals(range)
                && isSameRelation(nonTaxRel.getRelation());
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public int hashCode() {
        int hashCode = domain.hashCode() + range.hashCode();
        if (relation != null) {
            hashCode += relation.hashCode();
        }
        return hashCode;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    private Concept getRelation() {
        return relation;
    }

    public void setRelation(Concept relation) {
        this.relation = relation;
    }

    public boolean isMetaProperty() {
        return isMetaProperty;
    }

    public void setMetaProperty(boolean isMetaProperty) {
        this.isMetaProperty = isMetaProperty;
    }

    public Object[] getAcceptedTableData() {
        return new Object[] {isMetaProperty, domain, relation, range};
    }

    public Object[] getWrongTableData() {
        return new Object[] { domain, range};
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(isMetaProperty);
        builder.append("\t");
        builder.append(domain);
        builder.append("\t");
        if (relation != null) {
            builder.append(relation.getURI());
            builder.append("\t");
        }
        builder.append(range);
        return builder.toString();
    }
}
