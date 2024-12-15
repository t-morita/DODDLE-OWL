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

import io.github.doddle_owl.models.common.DODDLEToken;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Chasen(ver 2.3.3), Mecab(ver 0.93)を利用して，文書を入力として，品詞，単語，原形を獲得するクラス
 *
 * @author Takeshi Morita
 */
public class DODDLEStringTagger {

	public enum MophologicalAnalyzerType {
		CHASEN, MECAB
	}

	private static Process process;
	private MophologicalAnalyzerType type;
	private static final String Japanese_Morphological_Analyzer = "D:/Program Files (x86)/MeCab/bin/mecab.exe";
	private static DODDLEStringTagger tagger;

	private DODDLEStringTagger() {
		if (Japanese_Morphological_Analyzer.contains("chasen")) {
			type = MophologicalAnalyzerType.CHASEN;
		} else if (Japanese_Morphological_Analyzer.contains("mecab")) {
			type = MophologicalAnalyzerType.MECAB;
		}
	}

	private static void initProcess() {
		try {
			Runtime rt = Runtime.getRuntime();
			process = rt.exec(Japanese_Morphological_Analyzer);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private BufferedReader getBufferedReader() {
		return new BufferedReader(new InputStreamReader(process.getInputStream()));
	}

	private BufferedWriter getBufferedWriter() {
		return new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
	}

	private List<DODDLEToken> analyze(String text) {
		List<DODDLEToken> tokenList = new ArrayList<>();
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			initProcess();
			writer = getBufferedWriter();
			// System.out.println("get writer");
			writer.write(text);
			// System.out.println("write text");

			reader = getBufferedReader();
			// System.out.println("get reader");
			while (reader.ready()) {
				String line = reader.readLine();
				System.out.println("line: " + line);
				if (type == MophologicalAnalyzerType.CHASEN) {
					String[] lines = line.split("\t");
					if (4 <= lines.length) {
						// System.out.println(line);
						tokenList.add(new DODDLEToken(lines[3], lines[2], lines[0]));
					}
				} else if (type == MophologicalAnalyzerType.MECAB) {
					String[] lines = line.split("\t");
					if (lines.length == 2) {
						String[] attrs = lines[1].split(",");
						tokenList.add(new DODDLEToken(attrs[0], attrs[6], lines[0]));
					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ioe2) {
				ioe2.printStackTrace();
			}
		}
		return tokenList;
	}

	private static DODDLEStringTagger getInstance() {
		if (tagger == null) {
			tagger = new DODDLEStringTagger();
		}
		return tagger;
	}

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				"C:/DODDLE-OWL/InputDocument.txt"), StandardCharsets.UTF_8));
		StringBuilder text = new StringBuilder();
		while (reader.ready()) {
			String line = reader.readLine();
			text.append(line).append(System.lineSeparator());

		}
		reader.close();

		System.out.println("text: " + text);

		DODDLEStringTagger tagger2 = DODDLEStringTagger.getInstance();
		List<DODDLEToken> tokenList = tagger2.analyze(text.toString());
		System.out.println(tokenList.size());
		for (DODDLEToken token : tokenList) {
			System.out.print(token.getPos() + "\t");
			System.out.print(token.getString() + "\t");
			System.out.print(token.getBasicString() + "\t");
			System.out.println();
		}
	}
}
