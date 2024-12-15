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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/**
 * @author Takeshi Morita
 */
class UseCaseTask {

    private String id;
    private String description;
    private final List<PrimitiveTask> primitiveTaskList;

    public UseCaseTask(String fileName) {
        Pattern pattern = Pattern.compile(".*[［\\[](.*)[］\\]].*");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String title = matcher.group(1);
            String[] elems = title.split("：");
            String id = elems[0];

            this.description = elems[1];
            this.id = id;
        }
        primitiveTaskList = new ArrayList<>();
    }

    private UseCaseTask(String description, String id) {
        this.description = description;
        this.id = id;
        primitiveTaskList = new ArrayList<>();
    }

    public String getID() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void addAllTask(List<PrimitiveTask> taskList) {
        primitiveTaskList.addAll(taskList);
    }

    private void addTask(PrimitiveTask task) {
        primitiveTaskList.add(task);
    }

    private List<PrimitiveTask> getPrimitiveTaskList() {
        return primitiveTaskList;
    }

    public String toString() {
        return id + ": " + description;
    }

    public static void main(String[] args) {
        try {
            File taskSetDir = new File("task_set");
            File[] taskFiles = taskSetDir.listFiles();
            List<UseCaseTask> useCaseTaskList = new ArrayList<>();
            for (File file : taskFiles) {
                // System.out.println(file);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                reader.readLine(); // 1行?ﾇ?ﾌてる
                UseCaseTask useCaseTask = null;
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] elems = line.split("\t");
                    if (0 < elems[1].length()) {
                        useCaseTask = new UseCaseTask(elems[1], elems[2]);
                        useCaseTaskList.add(useCaseTask);
                    }
                    Morpheme preMor = new Morpheme(elems[4], "", "", "");
                    Segment preSeg = new Segment(-1);
                    preSeg.addMorpheme(preMor);

                    Morpheme objMor = new Morpheme(elems[6], "", "", "");
                    Segment objSeg = new Segment(-1);
                    objSeg.addMorpheme(objMor);

                    String[] sbjMors = elems[5].split("、");
                    for (String sm : sbjMors) {
                        Morpheme sbjMor = new Morpheme(sm, "", "", "");
                        Segment sbjSeg = new Segment(-1);
                        sbjSeg.addMorpheme(sbjMor);
                        PrimitiveTask task = new PrimitiveTask(preSeg);
                        task.addObject(objSeg);
                        task.addSubject(sbjSeg);
                        useCaseTask.addTask(task);
                    }
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("input_word_set.txt"),
                    StandardCharsets.UTF_8));
            // BufferedReader reader = new BufferedReader(new
            // InputStreamReader(new FileInputStream(
            // "auto_extracted_term_set.txt"), "UTF8"));

            Set<String> termSet = new HashSet<>();
            while (reader.ready()) {
                String line = reader.readLine();
                termSet.add(line);
            }
            System.out.println("用語数: " + termSet.size());
            System.out.println("ユースケース記述数: " + useCaseTaskList.size());
            int taskNum = 0;
            int validTaskNum = 0;
            Set<String> undefinedTermSet = new HashSet<>();
            for (UseCaseTask useCaseTask : useCaseTaskList) {
                System.out.println(useCaseTask);
                for (PrimitiveTask task : useCaseTask.getPrimitiveTaskList()) {
                    taskNum++;
                    boolean isDefinedSubjectConcept = termSet.contains(task.getSubjectString());
                    boolean isDefinedObjectConcept = termSet.contains(task.getObjectString());

                    if (!(isDefinedSubjectConcept && isDefinedObjectConcept)) {
                        System.out.print(task.getPredicateString());
                        System.out.print("\t");
                        if (isDefinedSubjectConcept) {
                            System.out.print(task.getSubjectString());
                        } else {
                            undefinedTermSet.add(task.getSubjectString());
                            System.out.print("<" + task.getSubjectString() + ">");
                        }
                        System.out.print("\t");
                        if (isDefinedObjectConcept) {
                            System.out.println(task.getObjectString());
                        } else {
                            undefinedTermSet.add(task.getObjectString());
                            System.out.println("<" + task.getObjectString() + ">");
                        }
                    } else {
                        validTaskNum++;
                        System.out.println("*" + task);
                    }
                }
            }
            System.out.println(validTaskNum + "/" + taskNum);

            System.out.println("未定義語数: "+undefinedTermSet.size());
            Set<String> undefinedTermWithCaseSet = new HashSet<>();
            for (String term : undefinedTermSet) {
                if (term.contains("(") || term.contains(")")) {
                    undefinedTermWithCaseSet.add(term);
                } else {
                    System.out.println(term);
                }
            }            
            for (String term : undefinedTermWithCaseSet) {
                System.out.println(term);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
