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

import io.github.doddle_owl.DODDLE_OWL;

import java.io.File;
import java.io.InputStream;

/**
 * @author Takeshi Morita
 */
public class OWLOntologyExtractionTemplate {

    private File searchOWLMetaDataTemplate;
    private File searchClassSetTemplate;
    private File searchPropertySetTemplate;
    private File searchConceptTemplate;
    private File searchRegionSetTemplate;
    private File searchSubConceptTemplate;

    public OWLOntologyExtractionTemplate() {
        setSearchOWLMetaDataTemplate(new File("ontology_templates/SearchOWLMetaData.rq"));
        setSearchClassSetTemplate(new File("ontology_templates/SearchClassSet.rq"));
        setSearchPropertySetTemplate(new File("ontology_templates/SearchPropertySet.rq"));
        setSearchConceptTemplate(new File("ontology_templates/SearchConcept.rq"));
        setSearchRegionSetTemplate(new File("ontology_templates/SearchRegionSet.rq"));
        setSearchSubConceptTemplate(new File("ontology_templates/SearchSubConcept.rq"));
    }

    public String getSearchOWLMetaDataTemplateLabel() {
        if (searchOWLMetaDataTemplate.exists()) {
            return searchOWLMetaDataTemplate.getAbsolutePath();
        }
        return "Default Search OWL Meta Data Template";
    }

    public File getSearchOWLMetaDataTemplate() {
        return searchOWLMetaDataTemplate;
    }

    public InputStream getDefaultSearchOWLMetaDataTemplate() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("ontology_templates/SearchOWLMetaData.rq");
    }

    private void setSearchOWLMetaDataTemplate(File searchOWLMetaDataTemplate) {
        this.searchOWLMetaDataTemplate = searchOWLMetaDataTemplate;
    }

    public String getSearchSubConceptTemplateLabel() {
        if (searchSubConceptTemplate.exists()) {
            return searchSubConceptTemplate.getAbsolutePath();
        }
        return "Default Search Sub Concept Template";
    }

    public File getSearchSubConceptTemplate() {
        return searchSubConceptTemplate;
    }

    public InputStream getDefaultSearchSubConceptTemplate() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("ontology_templates/SearchSubConcept.rq");
    }

    public void setSearchSubConceptTemplate(File searchSubConceptTemplate) {
        this.searchSubConceptTemplate = searchSubConceptTemplate;
    }

    public String getSearchClassSetTemplateLabel() {
        if (searchClassSetTemplate.exists()) {
            return searchClassSetTemplate.getAbsolutePath();
        }
        return "Default Search Class Set Template";
    }

    public File getSearchClassSetTemplate() {
        return searchClassSetTemplate;
    }

    public InputStream getDefaultSearchClassSetTemplate() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("ontology_templates/SearchClassSet.rq");
    }

    public void setSearchClassSetTemplate(File searchClassSetTemplate) {
        this.searchClassSetTemplate = searchClassSetTemplate;
    }

    public String getSearchConceptTemplateLabel() {
        if (searchConceptTemplate.exists()) {
            return searchConceptTemplate.getAbsolutePath();
        }
        return "Default Search Concept Template";
    }

    public File getSearchConceptTemplate() {
        return searchConceptTemplate;
    }

    public InputStream getDefaultSearchConceptTemplate() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("ontology_templates/SearchConcept.rq");
    }

    public void setSearchConceptTemplate(File searchConceptTemplate) {
        this.searchConceptTemplate = searchConceptTemplate;
    }

    public String getSearchPropertySetTemplateLabel() {
        if (searchPropertySetTemplate.exists()) {
            return searchPropertySetTemplate.getAbsolutePath();
        }
        return "Default Search Property Set Template";
    }

    public File getSearchPropertySetTemplate() {
        return searchPropertySetTemplate;
    }

    public InputStream getDefaultSearchPropertySetTemplate() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("ontology_templates/SearchPropertySet.rq");
    }

    public void setSearchPropertySetTemplate(File searchPropertySetTemplate) {
        this.searchPropertySetTemplate = searchPropertySetTemplate;
    }

    public String getSearchRegionSetTemplateLabel() {
        if (searchRegionSetTemplate.exists()) {
            return searchRegionSetTemplate.getAbsolutePath();
        }
        return "Default Search Region Set Template";
    }

    public File getSearchRegionSetTemplate() {
        return searchRegionSetTemplate;
    }

    public InputStream getDefaultSearchRegionSetTemplate() {
        return DODDLE_OWL.class.getClassLoader().getResourceAsStream("ontology_templates/SearchRegionSet.rq");
    }

    public void setSearchRegionSetTemplate(File searchRegionSetTemplate) {
        this.searchRegionSetTemplate = searchRegionSetTemplate;
    }

}
