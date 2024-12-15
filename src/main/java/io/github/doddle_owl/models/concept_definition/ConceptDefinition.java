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
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.ontology_api.EDRTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.*;

/**
 * @author Takeshi Morita
 */
public class ConceptDefinition {

    private final Set<String> verbSet;

    private final Map<String, Map<String, Set<String>>> relationMap;
    public static final String[] relationList = {"agent", "object", "goal", "implement", "a-object", "place", "scene",
            "cause"};
    private static ConceptDefinition conceptDefintion;

    public static ConceptDefinition getInstance() {
        if (conceptDefintion == null) {
            conceptDefintion = new ConceptDefinition();
        }
        return conceptDefintion;
    }

    private ConceptDefinition() {
        Map<String, Set<String>> agentMap = new HashMap<>();
        Map<String, Set<String>> objectMap = new HashMap<>();
        Map<String, Set<String>> goalMap = new HashMap<>();
        Map<String, Set<String>> implementMap = new HashMap<>();
        Map<String, Set<String>> a_objectMap = new HashMap<>();
        Map<String, Set<String>> placeMap = new HashMap<>();
        Map<String, Set<String>> sceneMap = new HashMap<>();
        Map<String, Set<String>> causeMap = new HashMap<>();
        verbSet = new TreeSet<>();
        Map[] relationMapList = {agentMap, objectMap, goalMap, implementMap, a_objectMap, placeMap, sceneMap, causeMap};
        relationMap = new HashMap<>();
        for (int i = 0; i < relationList.length; i++) {
            relationMap.put(relationList[i], relationMapList[i]);
        }
    }

    public void printMap() {
        try {
            int n = 0;
            // System.out.println(verbSet.size());
            for (String verbID : verbSet) {
                StringBuilder buf = new StringBuilder();
                buf.append(verbID).append("||");
                for (String relation : relationList) {
                    buf.append(relation).append("\t");
                    Map<String, Set<String>> map = relationMap.get(relation);
                    if (map.get(verbID) == null) {
                        buf.append("||");
                    } else {
                        Set<String> idSet = map.get(verbID);
                        for (String id : idSet) {
                            buf.append(id).append("\t");
                        }
                    }
                }
                System.out.println(n++);
                buf.append(System.lineSeparator());
                BufferedWriter writer = Files.newBufferedWriter(Paths.get("conceptDescription3.1ex.txt"), StandardCharsets.UTF_8);
                try (writer) {
                    writer.write(buf.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putVerbID(Map<String, Set<String>> map, String verbID, Set<String> idSet) {
        if (idSet.size() == 0) {
            return;
        }
        map.put(verbID, idSet);
    }

    private void putExpandVerbID(Map<String, Set<String>> map, String verbID, Set<String> idSet) {
        if (idSet.size() == 0) {
            return;
        }
        Set<String> verbIDSet = getSubURISet(verbID);
        verbIDSet.add(verbID);
        for (String vid : verbIDSet) {
            verbSet.add(vid);
            // 下位動詞的概念を登録
            // 登録されていたら，さらにidSetを追加する
            if (map.get(vid) == null) {
                map.put(vid, idSet);
            } else {
                Set<String> set = map.get(vid);
                set.addAll(idSet);
                map.put(vid, set);
            }
        }
    }

    public Set<String> makeIDSet(String[] idArray) {
        Set<String> idSet = new HashSet<>();
        if (1 < idArray.length) {
            Collections.addAll(idSet, idArray);
        }
        return idSet;
    }

    public Set<String> makeExpandIDSet(String[] uriArray) {
        Set<String> uriSet = new HashSet<>();
        if (1 < uriArray.length) {
            for (String uri : uriArray) {
                uriSet.add(uri);
                uriSet.addAll(getSubURISet(uri));
            }
        }
        return uriSet;
    }

    /**
     * IDと関係子を受け取り，関係の値としてIDを含む動詞的概念の セットを返す
     */
    public Set<String> getVerbIDSet(String id, String relation) {
        Set<String> verbIDSet = new HashSet<>();
        Map<String, Set<String>> map = relationMap.get(relation);
        for (Entry<String, Set<String>> entry : map.entrySet()) {
            String verbID = entry.getKey();
            Set<String> idSet = entry.getValue();
            if (idSet.contains(id)) {
                verbIDSet.add(verbID);
                continue;
            }
            // マッチしない場合には，対象概念の上位概念に概念集合マッチするかどうかを調べていく
            Set<String> supURISet = getSupURISet(id);
            for (String tid : idSet) {
                if (supURISet.contains(tid)) {
                    verbIDSet.add(verbID);
                    break;
                }
            }
        }
        return verbIDSet;
    }

    /**
     * idを受け取り，そのIDのルートまでのパスに存在するURIのセットを返す
     */
    private Set<String> getSupURISet(String id) {
        Set<String> uriSet = new HashSet<>();
        for (List<String> supIDList : EDRTree.getEDRTree().getURIPathToRootSet(id)) {
            uriSet.addAll(supIDList);
        }
        return uriSet;
    }

    /**
     * idを受け取り，そのIDの下位に存在するURIのセットを返す
     *
     * @param uri
     */
    public Set<String> getSubURISet(String uri) {
        Set<String> uriSet = new HashSet<>();
        for (Set<String> subURISet : EDRTree.getEDRTree().getSubURISet(uri)) {
            uriSet.addAll(subURISet);
        }
        return uriSet;
    }

    /**
     * 動詞的概念と関係子を引数として受け取り，動詞的概念と関係子を 通して関係のある名詞的概念のセットを返す
     *
     * @param verbID
     * @param relation
     */
    public Set<String> getIDSet(String verbID, String relation) {
        Map<String, Set<String>> map = relationMap.get(relation);
        return map.get(verbID);
    }

    public Set<String> getURISet(String relation, String verbID, List<List<Concept>> trimmedConceptList) {
        Map<String, Set<String>> map = relationMap.get(relation);
        // System.out.println(relation + " map key size: " +
        // map.keySet().size());
        Set<String> uriSet = new HashSet<>();
        if (map.get(verbID) != null) {
            Set<String> idSet = map.get(verbID);
            for (String id : idSet) {
                uriSet.add(DODDLEConstants.EDR_URI + id);
            }
        }
        for (List<Concept> list : trimmedConceptList) {
            for (Concept trimmedConcept : list) {
                if (map.get(trimmedConcept.getLocalName()) != null) {
                    Set<String> idSet = map.get(trimmedConcept.getLocalName());
                    for (String id : idSet) {
                        uriSet.add(DODDLEConstants.EDR_URI + id);
                    }
                }
            }
        }
        if (0 < uriSet.size()) {
            System.out.println("org uriset size: " + uriSet.size());
        }
        return uriSet;
    }

    public static void main(String[] args) {
        // EDRTree.initialize();
        ConceptDefinition cd = ConceptDefinition.getInstance();
        // System.out.println("initialize done");
        Set<String> verbIDSet = cd.getVerbIDSet("ID0a78fb", "agent");
        // System.out.println(verbIDSet.size());
        for (String verbID : verbIDSet) {
            System.out.println("verb id: " + verbID);
        }
        Set<String> idSet = cd.getIDSet("ID061c7d", "agent");
        for (String id : idSet) {
            System.out.println("id: " + id);
        }
    }
}
