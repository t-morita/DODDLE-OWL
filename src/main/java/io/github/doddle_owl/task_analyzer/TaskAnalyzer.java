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

import io.github.doddle_owl.models.document_selection.Document;
import io.github.doddle_owl.views.document_selection.DocumentSelectionPanel;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Takeshi Morita
 */
public class TaskAnalyzer {

    private final List<CabochaDocument> cabochaDocList;
    private final List<UseCaseTask> useCaseTaskList;
    private final Map<String, Integer> compoundWordCountMap;
    private final Map<String, Integer> compoundWordWithNokakuCountMap;
    private Process cabochaProcess;

    private final Set<Segment> segmentSet; // 全文書の全文節の集合を保存
    private final Map<Segment, Set<Segment>> segmentMap; // 文節とその文節に係っている文節の集合を保存

    public TaskAnalyzer() {
        cabochaDocList = new ArrayList<>();
        useCaseTaskList = new ArrayList<>();
        segmentMap = new HashMap<>();
        segmentSet = new HashSet<>();
        compoundWordCountMap = new HashMap<>();
        compoundWordWithNokakuCountMap = new HashMap<>();
    }

    public void loadUseCaseTask(String useCaseDir) {
        File docDir = new File(useCaseDir);
        File[] files = docDir.listFiles();
        for (File file : files) {
            System.out.println(file.getName());
            loadUseCaseTask(file);
        }
    }

    public CabochaDocument loadUseCaseTask(File file) {
        File tmpFile = null;
        BufferedWriter tmpWriter = null;
        CabochaDocument cabochaDoc = null;
        try {
            UseCaseTask useCaseTask = new UseCaseTask(file.getName());
            useCaseTaskList.add(useCaseTask);
            Document doc = new Document(file);

            tmpFile = File.createTempFile("cabochaTemp", null);
            tmpWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), CabochaDocument.CHARSET));
            doc.setText(doc.getText().replaceAll("\t", "。"));
            tmpWriter.write(doc.getText());
            tmpWriter.flush();
            tmpWriter.close();

            // --output-format=XML 
            ProcessBuilder processBuilder = new ProcessBuilder(
                    DocumentSelectionPanel.Japanese_Dependency_Structure_Analyzer, "-f3", tmpFile.getAbsolutePath());
            cabochaProcess = processBuilder.start();
            cabochaDoc = new CabochaDocument(doc, cabochaProcess);
            cabochaDocList.add(cabochaDoc);
            segmentSet.addAll(cabochaDoc.getSegmentSet());
            setSegmentMap(cabochaDoc);
            setCompoundWordCountMap(cabochaDoc);
            setCompoundWordWithNokakuCountMap(cabochaDoc);
            useCaseTask.addAllTask(cabochaDoc.getPrimitiveTaskList());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (tmpWriter != null) {
                try {
                    tmpWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tmpFile != null) {
                tmpFile.deleteOnExit();
            }
            destroyProcess();
        }
        return cabochaDoc;
    }

    public void destroyProcess() {
        if (cabochaProcess != null) {
            cabochaProcess.destroy();
        }
    }

    public List<UseCaseTask> getUseCaseTaskList() {
        return useCaseTaskList;
    }

    private void setSegmentMap(CabochaDocument doc) {
        Map<Segment, Set<Segment>> docMap = doc.getSegmentMap();
        for (Entry<Segment, Set<Segment>> entry : docMap.entrySet()) {
            if (segmentMap.get(entry.getKey()) != null) {
                Set<Segment> segSet = segmentMap.get(entry.getKey());
                segSet.addAll(entry.getValue());
            } else {
                segmentMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordCountMap(CabochaDocument doc) {
        Map<String, Integer> docMap = doc.getCompoundWordCountMap();
        for (Entry<String, Integer> entry : docMap.entrySet()) {
            compoundWordCountMap.merge(entry.getKey(), entry.getValue(), (a, b) -> b + a);
        }
    }

    private void setCompoundWordWithNokakuCountMap(CabochaDocument doc) {
        Map<String, Integer> docMap = doc.getCompoundWordWithNokakuCountMap();
        for (Entry<String, Integer> entry : docMap.entrySet()) {
            compoundWordWithNokakuCountMap.merge(entry.getKey(), entry.getValue(), (a, b) -> b + a);
        }
    }

    public Map<Segment, Set<Segment>> getSegmentMap() {
        return segmentMap;
    }

    public Map<String, Integer> getCompounWordCountMap() {
        return compoundWordCountMap;
    }

    public Map<String, Integer> getCompoundWordWithNokakuCountMap() {
        return compoundWordWithNokakuCountMap;
    }

    public Set<Segment> getSegmentSet() {
        return segmentSet;
    }

    public void printSegmentSetWithNokaku() {
        System.out.println();
        System.out.println("<の格を含む文節の係り受け>");
        for (Entry<Segment, Set<Segment>> entry : segmentMap.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append(entry.getKey().getNounPhrase()).append(" => ");
            boolean isIncludingNokaku = false;
            for (Segment seg : entry.getValue()) {
                if (seg.isIncludingNoKaku()) {
                    isIncludingNokaku = true;
                    builder.append(seg).append(", ");
                }
            }
            if (isIncludingNokaku) {
                System.out.println(builder.toString());
            }
        }
    }

    public void printNounAndVerbSet() {
        System.out.println();
        System.out.println("<文節から抽出した形態素>");
        Set<String> nounAndVerbSet = new HashSet<>();
        for (Segment segment : segmentSet) {
            for (Morpheme m : segment.getMorphemeList()) {
                if (m.getPos().equals(Morpheme.NOUN_NUM)) {
                    continue;
                }
                if (m.getPos().contains(Morpheme.NOUN) || m.getPos().contains(Morpheme.VERB)) {
                    nounAndVerbSet.add(m.getBasic());
                }
            }
        }
        for (String s : nounAndVerbSet) {
            System.out.println(s);
        }
    }

    public void printCompoundWordSetAndCount() {
        System.out.println();
        System.out.println("<文節から抽出した複合語>");
        for (Entry<String, Integer> entry : compoundWordCountMap.entrySet()) {
            // System.out.println(entry.getKey() + ": " + entry.getValue());
            System.out.println(entry.getKey());
        }
    }

    public void printCompoundWordWithNokakuSetAndCount() {
        System.out.println();
        System.out.println("<の格を含む複合語>");
        for (Entry<String, Integer> entry : compoundWordWithNokakuCountMap.entrySet()) {
            // System.out.println(entry.getKey() + ": " + entry.getValue());
            System.out.println(entry.getKey());
        }
    }

    public void printTaskDescriptions() {
        System.out.println();
        System.out.println("<タスク記述支援>");
        int i = 1;
        for (CabochaDocument cabochaDoc : cabochaDocList) {
            System.out.println("UC-06-" + i);
            cabochaDoc.printTaskDescriptions();
            System.out.println();
            i++;
        }
    }
}
