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

/**
 * @author Takeshi Morita
 */
public class Morpheme {
	private final String surface;
	private String kana;
	private final String basic;
	private String pos;

	public static final String NOUN = "名詞-";
	public static final String NOUN_NUM = "名詞-数";
	public static final String VERB = "動詞-"; // ハイフンをいれないと形容動詞などでもヒットしてし?､
	public static final String SYMBOL = "記号-";
	public static final String SYMBOL_ALPHABET = "記号-アルファベット";
	public static final String UNKNOWN_WORD = "未知語";
	public static final String SYMBOL_OPENED_PARENTHESIS = "記号-括弧開";
	public static final String SYMBOL_CLOSED_PARENTHESIS = "記号-括弧閉";

	public Morpheme(String[] elems) {
		this(elems[0], elems[1], elems[2], elems[3]);
	}

	public Morpheme(String s, String b) {
		surface = s;
		basic = b;
	}

	public Morpheme(String s, String k, String b, String p) {
		b = b.replaceAll("（", "(");
		b = b.replaceAll("）", ")");
		surface = s;
		kana = k;
		basic = b;
		pos = p;
	}

	public String getSurface() {
		return surface;
	}

	public String getKana() {
		return kana;
	}

	public String getBasic() {
		return basic;
	}

	public String getPos() {
		return pos;
	}

	public boolean equals(Object obj) {
		Morpheme om = (Morpheme) obj;
		return om.getSurface().equals(surface);
	}

	public String toString() {
		return surface;
	}

	public int hashCode() {
		return 0;
	}
}
