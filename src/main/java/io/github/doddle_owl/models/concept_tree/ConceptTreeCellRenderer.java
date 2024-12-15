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

package io.github.doddle_owl.models.concept_tree;

import io.github.doddle_owl.utils.Utils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author Takeshi Morita
 */
public class ConceptTreeCellRenderer extends DefaultTreeCellRenderer {

    private ImageIcon SIN_ICON;
    private ImageIcon MULTIPLE_SIN_ICON;
    private ImageIcon BEST_MATCH_ICON;
    private ImageIcon MULTIPLE_BEST_MATCH_ICON;

    public static final String NOUN_CONCEPT_TREE = "Noun Concept Tree";
    public static final String VERB_CONCEPT_TREE = "Verb Concept Tree";

    public ConceptTreeCellRenderer(String type) {
        if (type.equals(NOUN_CONCEPT_TREE)) {
            ImageIcon CLASS_SIN_ICON = Utils.getImageIcon("sin_icon.png");
            SIN_ICON = CLASS_SIN_ICON;
            ImageIcon CLASS_MULTIPLE_SIN_ICON = Utils.getImageIcon("multiple_sin_icon.png");
            MULTIPLE_SIN_ICON = CLASS_MULTIPLE_SIN_ICON;
            ImageIcon CLASS_BEST_MATCH_ICON = Utils.getImageIcon("best_match_icon.png");
            BEST_MATCH_ICON = CLASS_BEST_MATCH_ICON;
            ImageIcon CLASS_MULTIPLE_BEST_MATCH_ICON = Utils.getImageIcon("multiple_best_match_icon.png");
            MULTIPLE_BEST_MATCH_ICON = CLASS_MULTIPLE_BEST_MATCH_ICON;
        } else if (type.equals(VERB_CONCEPT_TREE)) {
            ImageIcon PROP_SIN_ICON = Utils.getImageIcon("sin_icon.png");
            SIN_ICON = PROP_SIN_ICON;
            ImageIcon PROP_MULTIPLE_SIN_ICON = Utils.getImageIcon("multiple_sin_icon.png");
            MULTIPLE_SIN_ICON = PROP_MULTIPLE_SIN_ICON;
            ImageIcon PROP_BEST_MATCH_ICON = Utils.getImageIcon("best_match_icon.png");
            BEST_MATCH_ICON = PROP_BEST_MATCH_ICON;
            ImageIcon PROP_MULTIPLE_BEST_MATCH_ICON = Utils.getImageIcon("multiple_best_match_icon.png");
            MULTIPLE_BEST_MATCH_ICON = PROP_MULTIPLE_BEST_MATCH_ICON;
        }
        setOpaque(true);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        setText(value.toString());

        if (selected) {
            setBackground(new Color(0, 0, 128));
            setForeground(Color.white);
        } else {
            setBackground(Color.white);
            setForeground(Color.black);
        }

        if (value.getClass().equals(ConceptTreeNode.class)) {
            ConceptTreeNode node = (ConceptTreeNode) value;
            if (node.isInputConcept() || node.isUserConcept()) {
                if (node.isMultipleInheritance()) {
                    setIcon(MULTIPLE_BEST_MATCH_ICON);
                } else {
                    setIcon(BEST_MATCH_ICON);
                }
            } else {
                if (node.isMultipleInheritance()) {
                    setIcon(MULTIPLE_SIN_ICON);
                } else {
                    setIcon(SIN_ICON);
                }
            }
        }
        return component;
    }
}
