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

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.parsers.*;

import io.github.doddle_owl.models.document_selection.Document;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * @author Takeshi Morita
 */
public class CabochaDocument {

    private String docName;
    private Document document;
    private final List<Sentence> sentenceList;
    private final Set<Segment> segmentSet;
    private final Map<String, Integer> compoundWordCountMap;
    private final Map<String, Integer> compoundWordWithNokakuCountMap;
    private final Map<Segment, Set<Segment>> segmentMap;
    private final Process cabochaProcess;
    public static final String CHARSET = "UTF-8";

    private CabochaDocument(Process cp) {
        cabochaProcess = cp;
        sentenceList = new ArrayList<>();
        segmentSet = new HashSet<>();
        compoundWordCountMap = new HashMap<>();
        compoundWordWithNokakuCountMap = new HashMap<>();
        segmentMap = new HashMap<>();
    }

    public CabochaDocument(Document doc, Process cp) {
        this(cp);
        document = doc;
        cabochaDocReader();
    }

    private void setMorpheme(NodeList tokElementList, Segment segment) {
        for (int i = 0; i < tokElementList.getLength(); i++) {
            Element tokElement = (Element) tokElementList.item(i);
            String surface = tokElement.getTextContent();
            String[] elems = tokElement.getAttribute("feature").split(",");
            StringBuilder pos = new StringBuilder(elems[0]);
            for (int j = 1; j < 3; j++) {
                if (!elems[j].equals("*")) {
                    pos.append("-").append(elems[j]);
                }
            }
            String basic = surface;
            String kana = surface;
            if (elems.length == 9) {
                basic = elems[6];
                kana = elems[7];
            }
            Morpheme morpheme = new Morpheme(surface, kana, basic, pos.toString());
            segment.addMorpheme(morpheme);
        }
    }

    private void setChunk(NodeList chunkElementList, Sentence sentence) {
        for (int i = 0; i < chunkElementList.getLength(); i++) {
            Element chunkElement = (Element) chunkElementList.item(i);
            int link = Integer.parseInt(chunkElement.getAttribute("link"));
            Segment segment = new Segment(link);
            sentence.addSegment(segment);
            NodeList tokElementList = chunkElement.getElementsByTagName("tok");
            setMorpheme(tokElementList, segment);
        }
    }

    private void cabochaReader(File outputFile) {
        Segment segment = null;
        Sentence sentence = new Sentence();
        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(outputFile);
            Element root = doc.getDocumentElement();
            NodeList sentenceElementList = root.getElementsByTagName("sentence");
            for (int i = 0; i < sentenceElementList.getLength(); i++) {
                Element sentenceElement = (Element) sentenceElementList.item(i);
                NodeList chunkElementList = sentenceElement.getElementsByTagName("chunk");
                setChunk(chunkElementList, sentence);
                sentence.mergeSegments();
                setSegmentMap(sentence);
                setCompoundWordCountMap(sentence);
                setCompoundWordWithNokakuCountMap(sentence);
                segmentSet.addAll(sentence.getSegmentList());
                sentenceList.add(sentence);
                sentence = new Sentence();
            }
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }
    }

    private File saveCabochaOutput() {
        File tmpFile = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(cabochaProcess.getInputStream(), CHARSET));
            tmpFile = File.createTempFile("cabochaOutputTemp", null);
            BufferedWriter tmpWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile),
                    CabochaDocument.CHARSET));
            tmpWriter.write("<?xml version=\"1.0\" encoding=\"" + CHARSET + "\" ?>");
            tmpWriter.write("<root>");
            String line;
            while ((line = reader.readLine()) != null) {
                tmpWriter.write(line);
            }
            tmpWriter.write("</root>");
            tmpWriter.flush();
            tmpWriter.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        return tmpFile;
    }

    private void cabochaDocReader() {
        File outputFile = saveCabochaOutput();
        cabochaReader(outputFile);
    }

    public String getDocName() {
        return docName;
    }

    public Set<Segment> getSegmentSet() {
        return segmentSet;
    }

    private void setSegmentMap(Sentence sentence) {
        Map<Segment, Set<Segment>> sentenceMap = sentence.getSegmentMap();
        for (Entry<Segment, Set<Segment>> entry : sentenceMap.entrySet()) {
            if (segmentMap.get(entry.getKey()) != null) {
                Set<Segment> segSet = segmentMap.get(entry.getKey());
                segSet.addAll(entry.getValue());
            } else {
                segmentMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordCountMap(Sentence sentence) {
        Map<String, Integer> sentenceMap = sentence.getCompoundWordCountMap();
        for (Entry<String, Integer> entry : sentenceMap.entrySet()) {
            compoundWordCountMap.merge(entry.getKey(), entry.getValue(), (a, b) -> b + a);
        }
    }

    private void setCompoundWordWithNokakuCountMap(Sentence sentence) {
        Map<String, Integer> sentenceMap = sentence.getCompoundWordWithNokakuCountMap();
        for (Entry<String, Integer> entry : sentenceMap.entrySet()) {
            compoundWordWithNokakuCountMap.merge(entry.getKey(), entry.getValue(), (a, b) -> b + a);
        }
    }

    public List<Sentence> getSentenceList() {
        return sentenceList;
    }

    public Map<Segment, Set<Segment>> getSegmentMap() {
        return segmentMap;
    }

    public Set<String> getCompoundWordSet() {
        return compoundWordCountMap.keySet();
    }

    public Map<String, Integer> getCompoundWordCountMap() {
        return compoundWordCountMap;
    }

    public Set<String> getCompoundWordWithNokakuSet() {
        return compoundWordWithNokakuCountMap.keySet();
    }

    public Map<String, Integer> getCompoundWordWithNokakuCountMap() {
        return compoundWordWithNokakuCountMap;
    }

    public List<PrimitiveTask> getPrimitiveTaskList() {
        List<PrimitiveTask> primitiveTaskList = new ArrayList<>();
        for (Sentence sentence : sentenceList) {
            primitiveTaskList.addAll(sentence.getTaskDescriptionSet());
        }
        return primitiveTaskList;
    }

    public void printTaskDescriptions() {
        for (Sentence sentence : sentenceList) {
            System.out.println("(文): " + sentence);
            for (PrimitiveTask taskDescription : sentence.getTaskDescriptionSet()) {
                System.out.println(taskDescription);
            }
            System.out.println();
        }
    }

    public String toString() {
        return document.getFile().getName() + " sentence size: " + sentenceList.size() + " segment size: "
                + segmentSet.size();
    }
}
