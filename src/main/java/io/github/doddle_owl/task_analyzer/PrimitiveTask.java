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

import java.util.*;

/**
 * @author Takeshi Morita
 */
class PrimitiveTask {

    private final List<Segment> subjectList;
    private Segment predicate;
    private final List<Segment> objectList;

    public PrimitiveTask(Segment predicate) {
        this.predicate = predicate;
        subjectList = new ArrayList<>();
        objectList = new ArrayList<>();
    }

    public List<Segment> getObjectList() {
        return objectList;
    }

    public String getObjectString() {
        StringBuilder builder = new StringBuilder();
        for (Segment seg : objectList) {
            builder.append(seg);
        }
        return builder.toString();
    }

    public void addObject(Segment object) {
        objectList.add(object);
    }

    public Segment getPredicate() {
        return predicate;
    }

    public String getPredicateString() {
        return predicate.toString();
    }

    public void setPredicate(Segment predicate) {
        this.predicate = predicate;
    }

    public List<Segment> getSubjectList() {
        return subjectList;
    }

    public String getSubjectString() {
        StringBuilder builder = new StringBuilder();
        for (Segment seg : subjectList) {
            builder.append(seg);
        }
        return builder.toString();
    }

    public void addSubject(Segment subject) {
        subjectList.add(subject);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\t");
        for (Segment seg : subjectList) {
            builder.append(seg);
        }
        builder.append("\t");
        for (Segment seg : objectList) {
            builder.append(seg);
        }
        builder.append("\t");
        builder.append(predicate);
        builder.append("\t");
        return builder.toString();
    }
}
