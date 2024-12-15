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

package io.github.doddle_owl.models.concept_selection;

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.views.reference_ontology_selection.ReferenceOntologySelectionPanel;
import io.github.doddle_owl.utils.Utils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Takeshi Morita
 */
public class Concept implements Serializable {

	transient private Resource uri;
	private DODDLELiteral inputLabel;
	private final Map<String, List<DODDLELiteral>> langLabelListMap;
	private final Map<String, List<DODDLELiteral>> langDescriptionListMap;

	public Concept() {
		langLabelListMap = new HashMap<>();
		langDescriptionListMap = new HashMap<>();
	}

	protected Concept(Concept c) {
		uri = c.getResource();
		langLabelListMap = c.getLangLabelListMap();
		langDescriptionListMap = c.getLangDescriptionListMap();
		if (c.getInputLabel() != null && !c.getInputLabel().getString().equals("")) {
			inputLabel = c.getInputLabel();
		}
	}

	public Concept(String uri, String word) {
		setURI(uri);
		langLabelListMap = new HashMap<>();
		if (0 < word.length()) {
			DODDLELiteral literal = new DODDLELiteral(DODDLEConstants.LANG, word);
			List<DODDLELiteral> literalList = new ArrayList<>();
			literalList.add(literal);
			langLabelListMap.put(DODDLEConstants.LANG, literalList);
		}
		langDescriptionListMap = new HashMap<>();
	}

	public Concept(String uri, String[] items) {
		setURI(uri);

		langLabelListMap = new HashMap<>();
		langDescriptionListMap = new HashMap<>();

		String jaWord = removeNullWords(items[0]);
		List<DODDLELiteral> jaLabelLiteralList = new ArrayList<>();
		for (String jw : jaWord.split("\t")) {
			if (0 < jw.length()) {
				DODDLELiteral literal = new DODDLELiteral("ja", jw);
				jaLabelLiteralList.add(literal);
			}
		}
		if (0 < jaLabelLiteralList.size()) {
			langLabelListMap.put("ja", jaLabelLiteralList);
		}

		String enWord = removeNullWords(items[1]);
		List<DODDLELiteral> enLabelLiteralList = new ArrayList<>();
		for (String ew : enWord.split("\t")) {
			if (0 < ew.length()) {
				DODDLELiteral literal = new DODDLELiteral("en", ew);
				enLabelLiteralList.add(literal);
			}
		}
		if (0 < enLabelLiteralList.size()) {
			langLabelListMap.put("en", enLabelLiteralList);
		}

		String jaDescription = removeNullWords(items[2]);
		jaDescription = jaDescription.replaceAll("\t", "");
		if (0 < jaDescription.length()) {
			List<DODDLELiteral> jaDescriptionLiteralList = new ArrayList<>();
			DODDLELiteral jaDescriptionLiteral = new DODDLELiteral("ja", jaDescription);
			jaDescriptionLiteralList.add(jaDescriptionLiteral);
			langDescriptionListMap.put("ja", jaDescriptionLiteralList);
		}

		String enDescription = removeNullWords(items[3]);
		enDescription = enDescription.replaceAll("\t", "");
		if (0 < enDescription.length()) {
			List<DODDLELiteral> enDescriptionLiteralList = new ArrayList<>();
			DODDLELiteral enDescriptionLiteral = new DODDLELiteral("en", enDescription);
			enDescriptionLiteralList.add(enDescriptionLiteral);
			langDescriptionListMap.put("en", enDescriptionLiteralList);
		}

		setInputWord();
	}

	public void setURI(String uri) {
		this.uri = ResourceFactory.createResource(uri);
	}

	private String removeNullWords(String str) {
		return str.replaceAll("\\*\\*\\*", "");
	}

	private void setInputWord() {
		inputLabel = null;
		List<DODDLELiteral> literalList = langLabelListMap.get(DODDLEConstants.LANG);
		if (literalList != null && 0 < literalList.size()) {
			inputLabel = literalList.get(0);
			return;
		}
		for (String lang : langLabelListMap.keySet()) {
			literalList = langLabelListMap.get(lang);
			if (literalList != null && 0 < literalList.size()) {
				inputLabel = literalList.get(0);
				return;
			}
		}
	}

	public void setInputLabel(DODDLELiteral label) {
		inputLabel = label;
	}

	public DODDLELiteral getInputLabel() {
		return inputLabel;
	}

	public void addLabel(DODDLELiteral labelLiteral) {
		if (labelLiteral.getString().length() == 0) {
			return;
		}
		String string = labelLiteral.getString();
		string = string.replaceAll("\t", "");
		labelLiteral.setString(string);
		String lang = labelLiteral.getLang();
		if (labelLiteral.getLang().length() == 0) {
			lang = "default";
		}
		List<DODDLELiteral> labelLiteralList = langLabelListMap.get(lang);
		if (labelLiteralList != null) {
			if (!labelLiteralList.contains(labelLiteral)) {
				labelLiteralList.add(labelLiteral);
			}
		} else {
			labelLiteralList = new ArrayList<>();
			labelLiteralList.add(labelLiteral);
			langLabelListMap.put(lang, labelLiteralList);
		}
	}

	public void removeLabel(DODDLELiteral label) {
		if (label.getLang().length() == 0) {
			List<DODDLELiteral> labelLiteralList = langLabelListMap.get("default");
			labelLiteralList.remove(label);
			if (labelLiteralList.size() == 0) {
				langLabelListMap.remove("default");
			}
		} else {
			List<DODDLELiteral> labelLiteralList = langLabelListMap.get(label.getLang());
			labelLiteralList.remove(label);
			if (labelLiteralList.size() == 0) {
				langLabelListMap.remove(label.getLang());
			}
		}
	}

	public void addDescription(DODDLELiteral descriptionLiteral) {
		if (descriptionLiteral.getString().length() == 0) {
			return;
		}
		String lang = descriptionLiteral.getLang();
		if (descriptionLiteral.getLang().length() == 0) {
			lang = "default";
		}
		List<DODDLELiteral> descriptionLiteralList = langDescriptionListMap.get(lang);
		if (descriptionLiteralList != null) {
			if (!descriptionLiteralList.contains(descriptionLiteral)) {
				descriptionLiteralList.add(descriptionLiteral);
			}
		} else {
			descriptionLiteralList = new ArrayList<>();
			descriptionLiteralList.add(descriptionLiteral);
			langDescriptionListMap.put(descriptionLiteral.getLang(), descriptionLiteralList);
		}
	}

	public void removeDescription(DODDLELiteral description) {
		if (description.getLang().length() == 0) {
			List<DODDLELiteral> descriptionLiteralList = langDescriptionListMap.get("default");
			descriptionLiteralList.remove(description);
			if (descriptionLiteralList.size() == 0) {
				langDescriptionListMap.remove("default");
			}
		} else {
			List<DODDLELiteral> descriptionLiteralList = langDescriptionListMap.get(description
					.getLang());
			descriptionLiteralList.remove(description);
			if (descriptionLiteralList.size() == 0) {
				langDescriptionListMap.remove(description.getLang());
			}
		}
	}

	public Resource getResource() {
		return uri;
	}

	public String getURI() {
		return uri.getURI();
	}

	public Map<String, List<DODDLELiteral>> getLangLabelListMap() {
		return langLabelListMap;
	}

	public Map<String, List<DODDLELiteral>> getLangDescriptionListMap() {
		return langDescriptionListMap;
	}

	public String getWord() {
		if (inputLabel != null && 0 < inputLabel.getString().length()) {
			return inputLabel.getString();
		}
		List<DODDLELiteral> literalList = langLabelListMap.get(DODDLEConstants.LANG);
		if (literalList != null && 0 < literalList.size()) {
			return literalList.get(0).getString();
		}
		literalList = langDescriptionListMap.get(DODDLEConstants.LANG);
		if (literalList != null && 0 < literalList.size()) {
			return literalList.get(0).getString();
		}
		for (String lang : langLabelListMap.keySet()) {
			literalList = langLabelListMap.get(lang);
			if (literalList != null && 0 < literalList.size()) {
				return literalList.get(0).getString();
			}
		}
		for (String lang : langDescriptionListMap.keySet()) {
			literalList = langDescriptionListMap.get(lang);
			if (literalList != null && 0 < literalList.size()) {
				return literalList.get(0).getString();
			}
		}
		return uri.getURI();
	}

	public String getNameSpace() {
		return Utils.getNameSpace(uri);
	}

	public String getLocalName() {
		return Utils.getLocalName(uri);
	}

	public String getQName() {
		if (DODDLE_OWL.getCurrentProject() == null) {
			return getLocalName();
		}
		ReferenceOntologySelectionPanel ontSelectionPanel = DODDLE_OWL.getCurrentProject()
				.getOntologySelectionPanel();
		String prefix = ontSelectionPanel.getPrefix(getNameSpace());
		return prefix + ":" + getLocalName();
	}

	public String toString() {
		return getWord() + " [" + getQName() + "]";
	}

	public boolean equals(Object c) {
		Concept concept = (Concept) c;
		return getURI().equals(concept.getURI());
	}
}
