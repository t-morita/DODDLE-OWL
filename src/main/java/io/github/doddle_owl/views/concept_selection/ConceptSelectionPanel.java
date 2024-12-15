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

package io.github.doddle_owl.views.concept_selection;

import io.github.doddle_owl.utils.*;
import org.apache.jena.rdf.model.ResourceFactory;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.actions.ConstructNounAndVerbTreeAction;
import io.github.doddle_owl.actions.ConstructNounTreeAction;
import io.github.doddle_owl.actions.ConstructTreeAction;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.DODDLELiteral;
import io.github.doddle_owl.models.concept_selection.Concept;
import io.github.doddle_owl.models.concept_selection.EvalConcept;
import io.github.doddle_owl.models.concept_selection.InputModule;
import io.github.doddle_owl.models.concept_selection.TreeConstructionOption;
import io.github.doddle_owl.models.concept_tree.ConceptTreeNode;
import io.github.doddle_owl.models.ontology_api.EDRTree;
import io.github.doddle_owl.models.ontology_api.JaWordNetTree;
import io.github.doddle_owl.models.ontology_api.ReferenceOntology;
import io.github.doddle_owl.models.ontology_api.WordNet;
import io.github.doddle_owl.models.term_selection.TermModel;
import io.github.doddle_owl.views.DODDLEProjectPanel;
import io.github.doddle_owl.views.OptionDialog;
import io.github.doddle_owl.views.common.UndefinedTermListPanel;
import io.github.doddle_owl.views.concept_tree.ClassTreeConstructionPanel;
import io.github.doddle_owl.views.concept_tree.PropertyTreeConstructionPanel;
import io.github.doddle_owl.views.document_selection.DocumentSelectionPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Takeshi Morita
 */
public class ConceptSelectionPanel extends JPanel implements ListSelectionListener,
        ActionListener, TreeSelectionListener {

    private Set<String> termSet;
    private Set<String> systemAddedTermSet;
    private Set<Concept> inputConceptSet; // 入力概念のセット
    private Set<Concept> systemAddedInputConceptSet; // システムが追加した入力概念のセット
    private Set<Concept> inputNounConceptSet; // 入力名詞的概念のセット
    private Set<Concept> inputVerbConceptSet; // 入力動詞的概念のセット

    private Set<TermModel> termModelSet; // 入力単語モデルのセット
    private Map<String, Set<Concept>> termConceptSetMap; // 入力単語と入力単語を見出しとして含む概念のマッピング
    private Map<String, Set<Concept>> termCorrespondConceptSetMap; // 入力単語と適切に対応する概念のマッピング
    private Map<String, Set<EvalConcept>> termEvalConceptSetMap;
    private Map<TermModel, TreeConstructionOption> compoundConstructTreeOptionMap;

    private TitledBorder exactMatchTermJListTitle;
    private TitledBorder partialMatchTermJListTitle;

    private JTextField searchTermField;
    private JButton searchTermButton;

    private JList exactMatchTermJList; // 完全照合した単語リスト
    private Set<TermModel> exactMatchTermModelSet;
    private JList partialMatchTermJList; // 部分照合した単語リスト
    private Set<TermModel> partialMatchTermModelSet;
    private final JList conceptSetJList;
    private final UndefinedTermListPanel undefinedTermListPanel;

    private JCheckBox exactMatchAmbiguityCntCheckBox;
    private JCheckBox exactMatchIsSyncCheckBox;
    private JCheckBox exactMatchIsSystemAddedTermCheckBox;

    private JCheckBox partialMatchCompoundWordCheckBox;
    private JCheckBox partialMatchMatchedTermBox;
    private JCheckBox partialMatchAmbiguityCntCheckBox;
    private JCheckBox partialMatchShowOnlyRelatedCompoundWordsCheckBox;

    private Concept selectedConcept;
    private final LiteralPanel labelPanel;
    private final LiteralPanel descriptionPanel;

    private final JPanel constructTreeOptionPanel;
    private final JPanel partialMatchConstructTreeOptionPanel;
    private final JPanel exactMatchConstructTreeOptionPanel;
    private final JPanel systemAddedPerfectlyMatchedConstructTreeOptionPanel;

    private final JCheckBox replaceSubClassesCheckBox;

    private final JRadioButton addAsSubConceptRadioButton;
    private final JRadioButton addAsSameConceptRadioButton;

    private final JList highlightPartJList;
    private final JEditorPane documentArea;
    private final JCheckBox highlightInputTermCheckBox;
    private final JCheckBox showAroundConceptTreeCheckBox;
    private final JTree aroundConceptTree;

    private final InputModule inputModule;
    private final ClassTreeConstructionPanel constructClassPanel;
    private final PropertyTreeConstructionPanel constructPropertyPanel;

    private final ConstructionTypePanel constructionTypePanel;
    private final PerfectlyMatchedOptionPanel perfectlyMatchedOptionPanel;
    private final PartiallyMatchedOptionPanel partiallyMatchedOptionPanel;
    // private JButton showConceptDescriptionButton;

    private final AutomaticDisAmbiguationAction automaticDisAmbiguationAction;
    private ConstructTreeAction constructNounTreeAction;
    private ConstructTreeAction constructNounAndVerbTreeAction;

    private Action savePerfectlyMatchedTermAction;
    private Action savePerfectlyMatchedTermWithCompoundWordAcion;

    // private ConceptDescriptionFrame conceptDescriptionFrame;

    private DocumentSelectionPanel docSelectionPanel;

    private final DODDLEProjectPanel project;

    private boolean isConstructNounAndVerbTree;

    public static Concept nullConcept;
    private static final EvalConcept nullEvalConcept = new EvalConcept(null, -1);


    public void initialize() {
        if (termSet != null) {
            termSet.clear();
        } else {
            termSet = new HashSet<>();
        }
        if (inputConceptSet != null) {
            inputConceptSet.clear();
        } else {
            inputConceptSet = new HashSet<>();
        }
        if (systemAddedInputConceptSet != null) {
            systemAddedInputConceptSet.clear();
        } else {
            systemAddedInputConceptSet = new HashSet<>();
        }
        if (termConceptSetMap != null) {
            termConceptSetMap.clear();
        } else {
            termConceptSetMap = new HashMap<>();
        }
        if (termCorrespondConceptSetMap != null) {
            termCorrespondConceptSetMap.clear();
        } else {
            termCorrespondConceptSetMap = new HashMap<>();
        }
        if (termEvalConceptSetMap != null) {
            termEvalConceptSetMap.clear();
        } else {
            termEvalConceptSetMap = new HashMap<>();
        }
        if (compoundConstructTreeOptionMap != null) {
            compoundConstructTreeOptionMap.clear();
        } else {
            compoundConstructTreeOptionMap = new HashMap<>();
        }
        exactMatchAmbiguityCntCheckBox.setSelected(false);
        exactMatchIsSyncCheckBox.setSelected(false);
        exactMatchIsSystemAddedTermCheckBox.setSelected(false);
        partialMatchCompoundWordCheckBox.setSelected(false);
        partialMatchMatchedTermBox.setSelected(false);
        partialMatchAmbiguityCntCheckBox.setSelected(false);
        partialMatchShowOnlyRelatedCompoundWordsCheckBox.setSelected(false);
        searchTermField.setText("");
        inputModule.initialize();
        initTermList();
        clearPanel();
        labelPanel.clearData();
        descriptionPanel.clearData();
    }

    public ConceptSelectionPanel(ClassTreeConstructionPanel tp, PropertyTreeConstructionPanel pp,
                                 DODDLEProjectPanel p) {
        project = p;
        constructClassPanel = tp;
        constructPropertyPanel = pp;
        nullConcept = new Concept("null", Translator.getTerm("NotAvailableLabel"));
        inputModule = new InputModule(project);

        conceptSetJList = new JList();
        conceptSetJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        conceptSetJList.addListSelectionListener(this);
        JScrollPane conceptJListScroll = new JScrollPane(conceptSetJList);
        conceptJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("ConceptList")));

        labelPanel = new LiteralPanel(Translator.getTerm("LanguageLabel"),
                Translator.getTerm("LabelList"), LiteralPanel.LABEL);
        descriptionPanel = new LiteralPanel(Translator.getTerm("LanguageLabel"),
                Translator.getTerm("DescriptionList"), LiteralPanel.DESCRIPTION);

        addAsSameConceptRadioButton = new JRadioButton(
                Translator.getTerm("SameConceptRadioButton"), true);
        addAsSameConceptRadioButton.addActionListener(this);
        addAsSubConceptRadioButton = new JRadioButton(Translator.getTerm("SubConceptRadioButton"));
        addAsSubConceptRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(addAsSameConceptRadioButton);
        group.add(addAsSubConceptRadioButton);
        constructTreeOptionPanel = new JPanel();
        constructTreeOptionPanel.setLayout(new BorderLayout());
        constructTreeOptionPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getTerm("TreeConstructionOptionBorder")));
        partialMatchConstructTreeOptionPanel = new JPanel();
        partialMatchConstructTreeOptionPanel.setLayout(new GridLayout(1, 2));
        partialMatchConstructTreeOptionPanel.add(addAsSameConceptRadioButton);
        partialMatchConstructTreeOptionPanel.add(addAsSubConceptRadioButton);

        exactMatchConstructTreeOptionPanel = new JPanel();
        systemAddedPerfectlyMatchedConstructTreeOptionPanel = new JPanel();
        systemAddedPerfectlyMatchedConstructTreeOptionPanel.setLayout(new BorderLayout());
        replaceSubClassesCheckBox = new JCheckBox("下位概念に置換");
        replaceSubClassesCheckBox.addActionListener(this);
        systemAddedPerfectlyMatchedConstructTreeOptionPanel.add(replaceSubClassesCheckBox,
                BorderLayout.CENTER);

        constructTreeOptionPanel.add(partialMatchConstructTreeOptionPanel, BorderLayout.CENTER);

        JPanel labelAndDescriptionPanel = new JPanel();
        labelAndDescriptionPanel.setLayout(new GridLayout(2, 1));
        labelAndDescriptionPanel.add(labelPanel);
        labelAndDescriptionPanel.add(descriptionPanel);

        JPanel conceptInfoPanel = new JPanel();
        conceptInfoPanel.setLayout(new BorderLayout());
        conceptInfoPanel.add(labelAndDescriptionPanel, BorderLayout.CENTER);
        conceptInfoPanel.add(constructTreeOptionPanel, BorderLayout.SOUTH);
        conceptInfoPanel.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("ConceptInformationPanel")));

        undefinedTermListPanel = new UndefinedTermListPanel();

        highlightPartJList = new JList();
        highlightPartJList.addListSelectionListener(this);
        JScrollPane highlightPartJListScroll = new JScrollPane(highlightPartJList);
        highlightPartJListScroll.setBorder(BorderFactory.createTitledBorder("行番号"));
        highlightPartJListScroll.setPreferredSize(new Dimension(100, 100));
        documentArea = new JEditorPane("text/html", "");
        documentArea.setEditable(false);
        JScrollPane documentAreaScroll = new JScrollPane(documentArea);
        highlightInputTermCheckBox = new JCheckBox(
                Translator.getTerm("HighlightInputTermCheckBox"), true);
        highlightInputTermCheckBox.addActionListener(this);

        showAroundConceptTreeCheckBox = new JCheckBox(
                Translator.getTerm("ShowConceptTreeCheckBox"), true);
        showAroundConceptTreeCheckBox.addActionListener(this);

        TreeModel aroundConceptTreeModel = new DefaultTreeModel(null);
        aroundConceptTree = new JTree(aroundConceptTreeModel);
        aroundConceptTree.addTreeSelectionListener(this);
        aroundConceptTree.setEditable(false);
        aroundConceptTree.setCellRenderer(new AroundTreeCellRenderer());
        JScrollPane aroundConceptTreeScroll = new JScrollPane(aroundConceptTree);

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(aroundConceptTreeScroll, BorderLayout.CENTER);
        treePanel.add(showAroundConceptTreeCheckBox, BorderLayout.SOUTH);
        treePanel.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("ConceptTreePanel")));

        JPanel documentPanel = new JPanel();
        documentPanel.setLayout(new BorderLayout());
        documentPanel.add(documentAreaScroll, BorderLayout.CENTER);
        documentPanel.add(highlightInputTermCheckBox, BorderLayout.SOUTH);
        documentPanel.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("InputDocumentArea")));

        automaticDisAmbiguationAction = new AutomaticDisAmbiguationAction(
                Translator.getTerm("AutomaticInputConceptSelectionAction"));

        constructionTypePanel = new ConstructionTypePanel();
        perfectlyMatchedOptionPanel = new PerfectlyMatchedOptionPanel();
        partiallyMatchedOptionPanel = new PartiallyMatchedOptionPanel();

        JButton constructNounTreeButton = new JButton(new ConstructNounTreeAction());
        JButton constructNounAndVerbTreeButton = new JButton(new ConstructNounAndVerbTreeAction());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(constructNounTreeButton);
        buttonPanel.add(constructNounAndVerbTreeButton);
        JPanel buttonLayoutPanel = new JPanel();
        buttonLayoutPanel.setLayout(new BorderLayout());
        buttonLayoutPanel.add(buttonPanel, BorderLayout.EAST);

        JTabbedPane optionTab = new JTabbedPane();
        optionTab.add(perfectlyMatchedOptionPanel, Translator.getTerm("ExactMatchOptionBorder"));
        optionTab.add(partiallyMatchedOptionPanel, Translator.getTerm("PartialMatchOptionBorder"));
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BorderLayout());
        optionPanel.add(optionTab, BorderLayout.CENTER);
        optionPanel.add(buttonLayoutPanel, BorderLayout.SOUTH);

        var xBoxPanel1 = new JPanel();
        xBoxPanel1.setLayout(new BoxLayout(xBoxPanel1, BoxLayout.X_AXIS));
        xBoxPanel1.add(conceptJListScroll);
        xBoxPanel1.add(conceptInfoPanel);
        xBoxPanel1.add(undefinedTermListPanel);

        var xBoxPanel2 = new JPanel();
        xBoxPanel2.setLayout(new BoxLayout(xBoxPanel2, BoxLayout.X_AXIS));
        xBoxPanel2.add(treePanel);
        xBoxPanel2.add(documentPanel);

        var yBoxPanel = new JPanel();
        yBoxPanel.setLayout(new BoxLayout(yBoxPanel, BoxLayout.Y_AXIS));
        yBoxPanel.add(xBoxPanel1);
        yBoxPanel.add(xBoxPanel2);

        var mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.add(getTermListPanel());
        mainSplitPane.add(yBoxPanel);

        setLayout(new BorderLayout());
        add(mainSplitPane, BorderLayout.CENTER);
        add(optionPanel, BorderLayout.SOUTH);
        initialize();
    }

    public void removeRefOntConceptLabel(Concept c, boolean isInputConcept) {
        if (!isInputConcept || perfectlyMatchedOptionPanel.isIncludeRefOntConceptLabel()) {
            return;
        }
        Set<DODDLELiteral> removedLabelSet = new HashSet<>();
        for (String lang : c.getLangLabelListMap().keySet()) {
            for (DODDLELiteral label : c.getLangLabelListMap().get(lang)) {
                if (!(termSet.contains(label.getString()) || systemAddedTermSet.contains(label
                        .getString()))) {
                    removedLabelSet.add(label);
                }
            }
        }
        for (DODDLELiteral removedLabel : removedLabelSet) {
            c.removeLabel(removedLabel);
        }
    }

    public void setConstructNounAndVerbTree(boolean t) {
        isConstructNounAndVerbTree = t;
    }

    public boolean isConstructNounAndVerbTree() {
        return isConstructNounAndVerbTree;
    }

    class EditPanel extends JPanel implements ActionListener {
        private final JTextField inputTermField;
        private final JButton addInputTermButton;
        private final JButton removeInputTermButton;

        EditPanel() {
            inputTermField = new JTextField();
            addInputTermButton = new JButton(Translator.getTerm("AddButton"));
            addInputTermButton.addActionListener(this);
            removeInputTermButton = new JButton(Translator.getTerm("RemoveButton"));
            removeInputTermButton.addActionListener(this);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(addInputTermButton);
            buttonPanel.add(removeInputTermButton);

            setLayout(new BorderLayout());
            add(inputTermField, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.EAST);
        }

        public void actionPerformed(ActionEvent e) {
            JList termJList = getTargetTermJList();
            Set<TermModel> termModelSet = getTargetTermModelSet();

            if (e.getSource() == addInputTermButton) {
                Set<String> inputTermSet = new HashSet<>();
                if (0 < inputTermField.getText().length()) {
                    inputTermSet.add(inputTermField.getText());
                    addInputTermSet(inputTermSet, 0);
                    inputTermField.setText("");
                }
            } else if (e.getSource() == removeInputTermButton) {
                List<TermModel> values = termJList.getSelectedValuesList();
                for (TermModel removeTermModel : values) {
                    termSet.remove(removeTermModel.getTerm());
                    termModelSet.remove(removeTermModel);
                    ConceptSelectionPanel.this.termModelSet.remove(removeTermModel);
                    termCorrespondConceptSetMap.remove(removeTermModel.getTerm());
                    compoundConstructTreeOptionMap.remove(removeTermModel);
                }
                termJList.setListData(termModelSet.toArray());
                termJList.setSelectedIndex(0);
                project.getConceptDefinitionPanel().setInputConceptSet();
            }
        }
    }

    public Set<String> getURISetForReplaceSubConcepts() {
        Set<String> uriSet = new HashSet<>();
        for (TreeConstructionOption option : compoundConstructTreeOptionMap.values()) {
            if (option.isReplaceSubConcepts()) {
                uriSet.add(option.getConcept().getURI());
            }
        }
        return uriSet;
    }

    public ConstructionTypePanel getConstructionTypePanel() {
        return constructionTypePanel;
    }

    public PerfectlyMatchedOptionPanel getPerfectlyMatchedOptionPanel() {
        return perfectlyMatchedOptionPanel;
    }

    public PartiallyMatchedOptionPanel getPartiallyMatchedOptionPanel() {
        return partiallyMatchedOptionPanel;
    }

    public class ConstructionTypePanel extends JPanel {
        private final JRadioButton newButton;
        private final JRadioButton addButton;

        ConstructionTypePanel() {
            newButton = new JRadioButton(Translator.getTerm("NewRadioButton"), true);
            addButton = new JRadioButton(Translator.getTerm("AddRadioButton"));
            ButtonGroup group = new ButtonGroup();
            group.add(newButton);
            group.add(addButton);
            add(newButton);
            add(addButton);
            setBorder(BorderFactory.createTitledBorder(Translator
                    .getTerm("TreeConstructionOptionBorder")));
        }

        public boolean isNewConstruction() {
            return newButton.isSelected();
        }
    }

    public class PerfectlyMatchedOptionPanel extends JPanel implements ActionListener {
        private final JCheckBox constructionBox;
        private final JCheckBox trimmingBox;
        private final JCheckBox includeRefOntConceptLabelBox;

        PerfectlyMatchedOptionPanel() {
            constructionBox = new JCheckBox(Translator.getTerm("ConstructionCheckBox"), true);
            constructionBox.addActionListener(this);
            trimmingBox = new JCheckBox(Translator.getTerm("TrimmingCheckBox"), true);
            includeRefOntConceptLabelBox = new JCheckBox(
                    Translator.getTerm("AddLabelsFromReferenceOntology"), true);
            add(constructionBox);
            add(trimmingBox);
            add(includeRefOntConceptLabelBox);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == constructionBox) {
                trimmingBox.setEnabled(constructionBox.isSelected());
                includeRefOntConceptLabelBox.setEnabled(constructionBox.isSelected());
            }
        }

        void setConstruction(boolean t) {
            constructionBox.setSelected(t);
        }

        void setTrimming(boolean t) {
            trimmingBox.setSelected(t);
        }

        public void setIncludeRefOntConceptLabel(boolean t) {
            includeRefOntConceptLabelBox.setSelected(t);
        }

        public boolean isConstruction() {
            return constructionBox.isSelected();
        }

        public boolean isTrimming() {
            return trimmingBox.isSelected();
        }

        boolean isIncludeRefOntConceptLabel() {
            return includeRefOntConceptLabelBox.isSelected();
        }
    }

    public class PartiallyMatchedOptionPanel extends JPanel implements ActionListener {
        private final JCheckBox constructionBox;
        private final JCheckBox trimmingBox;
        private final JCheckBox addAbstractConceptBox;
        private final JTextField abstractConceptChildNodeNumField;

        PartiallyMatchedOptionPanel() {
            constructionBox = new JCheckBox(Translator.getTerm("ConstructionCheckBox"), true);
            constructionBox.addActionListener(this);
            trimmingBox = new JCheckBox(Translator.getTerm("TrimmingCheckBox"), true);
            addAbstractConceptBox = new JCheckBox(
                    Translator.getTerm("AddAbstractInternalNodeCheckBox"), false);
            abstractConceptChildNodeNumField = new JTextField(2);
            abstractConceptChildNodeNumField.setText("2");
            add(constructionBox);
            add(trimmingBox);
            add(addAbstractConceptBox);
            add(abstractConceptChildNodeNumField);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == constructionBox) {
                constructionBoxAction(constructionBox.isSelected());
            }
        }

        private void constructionBoxAction(boolean t) {
            trimmingBox.setEnabled(t);
            addAbstractConceptBox.setEnabled(t);
        }

        void setConstruction(boolean t) {
            constructionBox.setSelected(t);
            constructionBoxAction(t);
        }

        void setTrimming(boolean t) {
            trimmingBox.setSelected(t);
        }

        void setAddAbstractConcept(boolean t) {
            addAbstractConceptBox.setSelected(t);
        }

        public boolean isConstruction() {
            return constructionBox.isSelected();
        }

        public boolean isTrimming() {
            return trimmingBox.isSelected();
        }

        public boolean isAddAbstractConcept() {
            return addAbstractConceptBox.isSelected();
        }

        public int getAbstractConceptChildNodeNum() {
            int num = 2;
            String numStr = abstractConceptChildNodeNumField.getText();
            try {
                num = Integer.parseInt(numStr);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            return num;
        }
    }

    public Map<TermModel, TreeConstructionOption> getCompoundConstructTreeOptionMap() {
        return compoundConstructTreeOptionMap;
    }

    private JPanel getTermListPanel() {
        JPanel exactMatchTermListPanel = getExactMatchTermListPanel();
        JPanel partialMatchTermListPanel = getPartialMatchTermListPanel();

        var termListTabbedPane = new JTabbedPane();
        termListTabbedPane.addTab(Translator.getTerm("ExactMatchTermListPanel"), null,
                exactMatchTermListPanel);
        termListTabbedPane.addTab(Translator.getTerm("PartialMatchTermListPanel"), null,
                partialMatchTermListPanel);

        JPanel termListPanel = new JPanel();
        termListPanel.setLayout(new BorderLayout());
        termListPanel.add(getSearchTermPanel(), BorderLayout.NORTH);
        termListPanel.add(termListTabbedPane, BorderLayout.CENTER);
        termListPanel.add(new EditPanel(), BorderLayout.SOUTH);
        termListPanel.setPreferredSize(new Dimension(300, 100));
        termListPanel.setMinimumSize(new Dimension(300, 100));
        termListPanel.setBorder(BorderFactory.createTitledBorder(
                Translator.getTerm("InputTermListArea")));

        return termListPanel;
    }

    private JPanel getSearchTermPanel() {
        searchTermField = new JTextField();
        searchTermField.addActionListener(this);
        searchTermButton = new JButton(Translator.getTerm("SearchButton"));
        searchTermButton.addActionListener(this);
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchTermField, BorderLayout.CENTER);
        searchPanel.add(searchTermButton, BorderLayout.EAST);
        return searchPanel;
    }

    private JPanel getExactMatchTermListPanel() {
        exactMatchTermJList = new JList();
        exactMatchTermJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        exactMatchTermJList.addListSelectionListener(this);
        JScrollPane perfectlyMatchedTermListScroll = new JScrollPane(exactMatchTermJList);
        exactMatchTermJListTitle = BorderFactory.createTitledBorder(Translator.getTerm("ExactMatchTermList"));
        // perfectMatchedWordListScroll.setBorder(perfectMatchedWordJListTitle);

        exactMatchAmbiguityCntCheckBox = new JCheckBox(
                Translator.getTerm("SenseCountCheckBox"), true);
        exactMatchAmbiguityCntCheckBox.addActionListener(this);
        exactMatchIsSyncCheckBox = new JCheckBox(
                Translator.getTerm("SyncPartialMatchTermListCheckBox"), true);
        exactMatchIsSystemAddedTermCheckBox = new JCheckBox(
                Translator.getTerm("SystemAddedInputTermCheckBox"), true);
        exactMatchIsSystemAddedTermCheckBox.addActionListener(this);
        JPanel perfectlyMatchedFilterPanel = new JPanel();
        perfectlyMatchedFilterPanel.setLayout(new GridLayout(3, 1));
        perfectlyMatchedFilterPanel.add(exactMatchAmbiguityCntCheckBox);
        perfectlyMatchedFilterPanel.add(exactMatchIsSystemAddedTermCheckBox);
        perfectlyMatchedFilterPanel.add(exactMatchIsSyncCheckBox);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(perfectlyMatchedTermListScroll, BorderLayout.CENTER);
        panel.add(perfectlyMatchedFilterPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel getPartialMatchTermListPanel() {
        partialMatchTermJList = new JList();
        partialMatchTermJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partialMatchTermJList.addListSelectionListener(this);
        JScrollPane partiallyMatchedTermListScroll = new JScrollPane(partialMatchTermJList);
        partialMatchTermJListTitle = BorderFactory.createTitledBorder(Translator
                .getTerm("PartiallyMatchedTermList"));
        // partialMatchedWordListScroll.setBorder(partialMatchedWordJListTitle);

        partialMatchCompoundWordCheckBox = new JCheckBox(
                Translator.getTerm("ShowMorphemeListCheckBox"), true);
        partialMatchCompoundWordCheckBox.addActionListener(this);
        partialMatchMatchedTermBox = new JCheckBox(Translator.getTerm("MatchResultCheckBox"),
                true);
        partialMatchMatchedTermBox.addActionListener(this);
        partialMatchAmbiguityCntCheckBox = new JCheckBox(
                Translator.getTerm("SenseCountCheckBox"), true);
        partialMatchAmbiguityCntCheckBox.addActionListener(this);
        partialMatchShowOnlyRelatedCompoundWordsCheckBox = new JCheckBox(
                Translator.getTerm("ShowOnlyCorrespondCompoundWordsCheckBox"), false);
        partialMatchShowOnlyRelatedCompoundWordsCheckBox.addActionListener(this);
        JPanel partialMatchedFilterPanel = new JPanel();
        partialMatchedFilterPanel.setLayout(new GridLayout(2, 2));
        partialMatchedFilterPanel.add(partialMatchCompoundWordCheckBox);
        partialMatchedFilterPanel.add(partialMatchMatchedTermBox);
        partialMatchedFilterPanel.add(partialMatchAmbiguityCntCheckBox);
        JPanel partialMatchedOptionPanel = new JPanel();
        partialMatchedOptionPanel.setLayout(new BorderLayout());
        partialMatchedOptionPanel.add(partialMatchedFilterPanel, BorderLayout.CENTER);
        partialMatchedOptionPanel.add(partialMatchShowOnlyRelatedCompoundWordsCheckBox,
                BorderLayout.SOUTH);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(partiallyMatchedTermListScroll, BorderLayout.CENTER);
        panel.add(partialMatchedOptionPanel, BorderLayout.SOUTH);

        return panel;
    }

    public int getPartiallyMatchedTermCnt() {
        if (partialMatchTermModelSet == null) {
            return 0;
        }
        return partialMatchTermModelSet.size();
    }

    private int getPerfectlyMatchedTermCnt(boolean isSystemAdded) {
        if (exactMatchTermModelSet == null) {
            return 0;
        }
        int num = 0;
        for (TermModel iwModel : exactMatchTermModelSet) {
            if (isSystemAdded) {
                if (iwModel.isSystemAdded()) {
                    num++;
                }
            } else {
                if (!iwModel.isSystemAdded()) {
                    num++;
                }
            }
        }
        return num;
    }

    public int getInputTermCnt() {
        return getPartiallyMatchedTermCnt() + getPerfectlyMatchedTermCnt() + getUndefinedTermCnt();
    }

    public int getPerfectlyMatchedTermCnt() {
        return getPerfectlyMatchedTermCnt(false);
    }

    public int getSystemAddedPerfectlyMatchedTermCnt() {
        return getPerfectlyMatchedTermCnt(true);
    }

    public int getMatchedTermCnt() {
        return getPartiallyMatchedTermCnt() + getPerfectlyMatchedTermCnt()
                + getSystemAddedPerfectlyMatchedTermCnt();
    }

    public int getUndefinedTermCnt() {
        return undefinedTermListPanel.getModel().getSize();
    }

    public UndefinedTermListPanel getUndefinedTermListPanel() {
        return undefinedTermListPanel;
    }

    public void setDocumentSelectionPanel(DocumentSelectionPanel p) {
        docSelectionPanel = p;
    }

    public void selectTopList() {
        if (exactMatchTermJList.getModel().getSize() != 0) {
            exactMatchTermJList.setSelectedIndex(0);
        } else {
            clearConceptInfoPanel();
        }
    }

    private void clearConceptInfoPanel() {
        labelPanel.clearData();
        descriptionPanel.clearData();
        setPartiallyMatchedOptionButton(false);
    }

    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = aroundConceptTree.getSelectionPath();
        if (path != null) {
            ConceptTreeNode node = (ConceptTreeNode) path.getLastPathComponent();
            selectedConcept = node.getConcept();
            labelPanel.setSelectedConcept(selectedConcept);
            descriptionPanel.setSelectedConcept(selectedConcept);
            labelPanel.setLabelLangList();
            descriptionPanel.setDescriptionLangList();
        }
    }

    private void saveCompoundOption(String option) {
        JList termJList = getTargetTermJList();
        TermModel iwModel = (TermModel) termJList.getSelectedValue();
        if (iwModel != null && iwModel.isPartiallyMatchTerm()) {
            TreeConstructionOption ctOption = compoundConstructTreeOptionMap.get(iwModel);
            ctOption.setOption(option);
            compoundConstructTreeOptionMap.put(iwModel, ctOption);
        }
    }

    private void saveReplaceSubConceptsOption() {
        JList termJList = getTargetTermJList();
        TermModel iwModel = (TermModel) termJList.getSelectedValue();
        if (iwModel != null && iwModel.isSystemAdded()) {
            TreeConstructionOption ctOption = compoundConstructTreeOptionMap.get(iwModel);
            ctOption.setIsReplaceSubConcepts(replaceSubClassesCheckBox.isSelected());
            compoundConstructTreeOptionMap.put(iwModel, ctOption);
        }
    }

    private void showOnlyRelatedCompoundWords() {
        if (partialMatchShowOnlyRelatedCompoundWordsCheckBox.isSelected()) {
            TermModel targetIWModel = (TermModel) exactMatchTermJList
                    .getSelectedValue();
            if (targetIWModel == null) {
                return;
            }
            Set searchedPartiallyMatchedTermModelSet = new TreeSet();
            for (TermModel iwModel : partialMatchTermModelSet) {
                if (iwModel.getMatchedTerm().equals(targetIWModel.getMatchedTerm())) {
                    searchedPartiallyMatchedTermModelSet.add(iwModel);
                }
            }
            partialMatchTermJList.setListData(searchedPartiallyMatchedTermModelSet.toArray());
            partialMatchTermJListTitle.setTitle(Translator.getTerm("PartiallyMatchTermList")
                    + " (" + searchedPartiallyMatchedTermModelSet.size() + "/"
                    + partialMatchTermModelSet.size() + ")");
        } else {
            partialMatchTermJList.setListData(partialMatchTermModelSet.toArray());
            partialMatchTermJListTitle.setTitle(Translator.getTerm("PartiallyMatchTermList")
                    + " (" + partialMatchTermModelSet.size() + ")");
        }
        exactMatchTermJList.repaint();
        partialMatchTermJList.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exactMatchTermJList
                || e.getSource() == partialMatchTermJList) {
            exactMatchTermJList.repaint();
            partialMatchTermJList.repaint();
        } else if (e.getSource() == highlightInputTermCheckBox) {
            highlightInputTerm();
        } else if (e.getSource() == showAroundConceptTreeCheckBox) {
            showAroundConceptTree();
        } else if (e.getSource() == addAsSameConceptRadioButton) {
            saveCompoundOption("SAME");
        } else if (e.getSource() == addAsSubConceptRadioButton) {
            saveCompoundOption("SUB");
        } else if (e.getSource() == replaceSubClassesCheckBox) {
            saveReplaceSubConceptsOption();
        } else if (e.getSource() == exactMatchAmbiguityCntCheckBox
                || e.getSource() == partialMatchAmbiguityCntCheckBox
                || e.getSource() == partialMatchCompoundWordCheckBox
                || e.getSource() == partialMatchMatchedTermBox
                || e.getSource() == exactMatchIsSystemAddedTermCheckBox) {
            exactMatchTermJList.repaint();
            partialMatchTermJList.repaint();
        } else if (e.getSource() == partialMatchShowOnlyRelatedCompoundWordsCheckBox) {
            showOnlyRelatedCompoundWords();
        } else if (e.getSource() == searchTermButton || e.getSource() == searchTermField) {
            String keyWord = searchTermField.getText();
            if (keyWord.length() == 0) {
                exactMatchTermJList.setListData(exactMatchTermModelSet.toArray());
                exactMatchTermJListTitle.setTitle(Translator
                        .getTerm("ExactMatchTermList")
                        + " ("
                        + exactMatchTermModelSet.size() + ")");

                partialMatchTermJList.setListData(partialMatchTermModelSet.toArray());
                partialMatchTermJListTitle.setTitle(Translator
                        .getTerm("PartialMatchTermList")
                        + " ("
                        + partialMatchTermModelSet.size() + ")");
            } else {
                Set searchedPerfectlyMatchedTermModelSet = new TreeSet();
                Set searchedPartiallyMatchedTermModelSet = new TreeSet();
                for (TermModel iwModel : exactMatchTermModelSet) {
                    if (iwModel.getTerm().contains(keyWord)) {
                        searchedPerfectlyMatchedTermModelSet.add(iwModel);
                    }
                }
                exactMatchTermJList.setListData(searchedPerfectlyMatchedTermModelSet
                        .toArray());
                exactMatchTermJListTitle.setTitle(Translator
                        .getTerm("PerfectlyMatchedTermList")
                        + " ("
                        + searchedPerfectlyMatchedTermModelSet.size()
                        + "/"
                        + exactMatchTermModelSet.size() + ")");

                TermModel targetIWModel = (TermModel) exactMatchTermJList
                        .getSelectedValue();
                if (targetIWModel == null && 0 < exactMatchTermJList.getModel().getSize()) {
                    targetIWModel = (TermModel) exactMatchTermJList.getModel()
                            .getElementAt(0);
                    exactMatchTermJList.setSelectedValue(targetIWModel, true);
                }
                for (TermModel iwModel : partialMatchTermModelSet) {
                    if (iwModel.getTerm().contains(keyWord)) {
                        if (partialMatchShowOnlyRelatedCompoundWordsCheckBox.isSelected()) {
                            if (targetIWModel != null
                                    && iwModel.getMatchedTerm().equals(
                                    targetIWModel.getMatchedTerm())) {
                                searchedPartiallyMatchedTermModelSet.add(iwModel);
                            }
                        } else {
                            searchedPartiallyMatchedTermModelSet.add(iwModel);
                        }
                    }
                }

                partialMatchTermJList.setListData(searchedPartiallyMatchedTermModelSet
                        .toArray());
                partialMatchTermJListTitle.setTitle(Translator
                        .getTerm("PartiallyMatchedTermList")
                        + " ("
                        + searchedPartiallyMatchedTermModelSet.size()
                        + "/"
                        + partialMatchTermModelSet.size() + ")");
            }
        }
    }

    /**
     * 入力文書中の入力単語を強調表示する
     */
    private void highlightInputTerm() {
        if (highlightInputTermCheckBox.isSelected()) {
            JList termJList = getTargetTermJList();
            TermModel iwModel = (TermModel) termJList.getSelectedValue();
            if (iwModel != null) {
                // String targetLines =
                // docSelectionPanel.getTargetTextLines(iwModel.getWord());
                String targetLines = docSelectionPanel.getTargetHtmlLines(iwModel.getTerm());
                documentArea.setText(targetLines);
            }
        } else {
            documentArea.setText("");
        }
    }

    public boolean getExactMatchAmbiguityCntCheckBox() {
        return exactMatchAmbiguityCntCheckBox.isSelected();
    }

    public boolean isPerfectlyMatchedSystemAddedTermCheckBox() {
        return exactMatchIsSystemAddedTermCheckBox.isSelected();
    }

    public boolean getPartialMatchAmbiguityCntCheckBox() {
        return partialMatchAmbiguityCntCheckBox.isSelected();
    }

    public boolean getPartialMatchCompoundWordCheckBox() {
        return partialMatchCompoundWordCheckBox.isSelected();
    }

    public boolean getPartialMatchMatchedTermBox() {
        return partialMatchMatchedTermBox.isSelected();
    }

    public ConstructTreeAction getConstructNounTreeAction() {
        return constructNounTreeAction;
    }

    public ConstructTreeAction getConstructNounAndVerbTreeAction() {
        return constructNounAndVerbTreeAction;
    }

    public Action getSavePerfectlyMatchedTermAction() {
        return savePerfectlyMatchedTermAction;
    }

    /*
     * 特に現状では使っていない
     */
    public Action getSavePerfectlyMatchedTermWithCompoundWordAction() {
        return savePerfectlyMatchedTermWithCompoundWordAcion;
    }

    public Map<String, Set<Concept>> getTermCorrespondConceptSetMap() {
        return termCorrespondConceptSetMap;
    }

    public Map<String, Set<Concept>> getTermConceptSetMap() {
        return termConceptSetMap;
    }

    public void loadTermConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            loadTermCorrespondConceptSetMap(chooser.getSelectedFile());
        }
    }

    public void loadTermCorrespondConceptSetMap(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            Set<String> inputTermSet = new HashSet<>();
            while (termModelSet == null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (TermModel iwModel : termModelSet) {
                inputTermSet.add(iwModel.getTerm());
            }

            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] termURI = line.replaceAll(System.lineSeparator(), "").split(",");
                    if (0 < termURI[0].length()) {
                        String term = termURI[0];
                        TermModel iwModel = inputModule.makeInputTermModel(term);
                        if (iwModel != null && inputTermSet.contains(iwModel.getTerm())) {
                            Set<Concept> correspondConceptSet = new HashSet<>();
                            for (int i = 1; i < termURI.length; i++) {
                                String uri = termURI[i];
                                Concept c = ReferenceOntology.getConcept(uri);
                                // 参照していないオントロジーの概念と対応づけようとした場合にnullとなる
                                if (c != null) {
                                    correspondConceptSet.add(c);
                                } else if (uri.equals("null")) {
                                    correspondConceptSet.add(nullConcept);
                                    break;
                                }
                            }
                            if (0 < correspondConceptSet.size()) {
                                termCorrespondConceptSetMap.put(iwModel.getTerm(), correspondConceptSet);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTermCorrespondConceptSetMap(int projectID, Statement stmt) {
        try {
            Set<String> inputTermSet = new HashSet<>();
            while (termModelSet == null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (TermModel iwModel : termModelSet) {
                inputTermSet.add(iwModel.getTerm());
            }

            String sql = "SELECT * from input_term_concept_map where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String inputTerm = URLDecoder.decode(rs.getString("Input_Term"), StandardCharsets.UTF_8);
                String inputConcept = rs.getString("Input_Concept");

                TermModel iwModel = inputModule.makeInputTermModel(inputTerm);
                if (iwModel != null && inputTermSet.contains(iwModel.getTerm())) {
                    Set<Concept> correspondConceptSet;
                    if (termCorrespondConceptSetMap.get(iwModel.getTerm()) != null) {
                        correspondConceptSet = termCorrespondConceptSetMap.get(iwModel.getTerm());
                    } else {
                        correspondConceptSet = new HashSet<>();
                    }
                    Concept c = ReferenceOntology.getConcept(inputConcept);
                    if (c != null) { // 参照していないオントロジーの概念と対応づけようとした場合にnullとなる
                        correspondConceptSet.add(c);
                    } else if (inputConcept.equals("null")) {
                        correspondConceptSet.add(nullConcept);
                        break;
                    }
                    if (0 < correspondConceptSet.size()) {
                        termCorrespondConceptSetMap.put(iwModel.getTerm(), correspondConceptSet);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setInputConceptSet() {
        inputConceptSet.clear();
        systemAddedInputConceptSet.clear();
        if (termModelSet == null) {
            return;
        }
        for (TermModel iwModel : termModelSet) {
            Set<Concept> correspondConceptSet = termCorrespondConceptSetMap.get(iwModel.getTerm());
            if (correspondConceptSet == null) {
                correspondConceptSet = new HashSet<>();
            }
            if (iwModel.isPartiallyMatchTerm()) {
                termCorrespondConceptSetMap.put(iwModel.getTerm(), correspondConceptSet);
            }
            if (correspondConceptSet.size() == 0) {
                Set<Concept> conceptSet = termConceptSetMap.get(iwModel.getMatchedTerm());
                if (conceptSet != null) {
                    Concept c = (Concept) conceptSet.toArray()[0];
                    correspondConceptSet.add(c);
                    termCorrespondConceptSetMap.put(iwModel.getTerm(), correspondConceptSet);
                }
            }
            if (correspondConceptSet.size() == 1 && correspondConceptSet.contains(nullConcept)) {
                compoundConstructTreeOptionMap.remove(iwModel);
                continue;
            }
            if (iwModel.isPartiallyMatchTerm()) {
                for (Concept c : correspondConceptSet) { // 最初の概念だけを扱っても良い．
                    TreeConstructionOption ctOption = new TreeConstructionOption(c);
                    compoundConstructTreeOptionMap.put(iwModel, ctOption);
                }
            }
            for (Concept c : correspondConceptSet) {
                if (c != null) {
                    c.setInputLabel(new DODDLELiteral("", iwModel.getMatchedTerm())); // メインとなる見出しを設定する
                    inputConceptSet.add(c);
                    if (iwModel.isSystemAdded()) {
                        systemAddedInputConceptSet.add(c);
                    }
                }
            }
        }
    }

    public void addInputConcept(Concept c) {
        inputConceptSet.add(c);
    }

    public void deleteSystemAddedConcept(Concept c) {
        systemAddedInputConceptSet.remove(c);
    }

    private void setPartiallyMatchedOptionButton(boolean t) {
        addAsSameConceptRadioButton.setEnabled(t);
        addAsSubConceptRadioButton.setEnabled(t);
    }

    private void selectAmbiguousConcept(JList termJList) {
        if (!termJList.isSelectionEmpty()) {
            String orgTerm = ((TermModel) termJList.getSelectedValue()).getTerm();
            String selectedTerm = ((TermModel) termJList.getSelectedValue()).getMatchedTerm();
            Set<Concept> conceptSet = termConceptSetMap.get(selectedTerm);

            Set<EvalConcept> evalConceptSet;

            if (!(termEvalConceptSetMap == null || termEvalConceptSetMap.get(selectedTerm) == null)) {
                evalConceptSet = termEvalConceptSetMap.get(selectedTerm);
            } else {
                evalConceptSet = getEvalConceptSet(conceptSet);
                evalConceptSet.add(nullEvalConcept);
            }

            conceptSetJList.setListData(evalConceptSet.toArray());
            Set<Concept> correspondConceptSet = termCorrespondConceptSetMap.get(orgTerm);
            if (correspondConceptSet != null) {
                if (correspondConceptSet.size() == 1 && correspondConceptSet.contains(nullConcept)) {
                    conceptSetJList.setSelectedValue(nullEvalConcept, true);
                    setPartiallyMatchedOptionButton(false);
                    return;
                }

                int[] selectedIndices = new int[correspondConceptSet.size()];
                int index = 0;
                ListModel model = conceptSetJList.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    EvalConcept evalConcept = (EvalConcept) model.getElementAt(i);
                    if (evalConcept.getConcept() == null) {
                        continue;
                    }
                    if (correspondConceptSet.contains(evalConcept.getConcept())) {
                        selectedIndices[index++] = i;
                    }
                }
                conceptSetJList.setSelectedIndices(selectedIndices);
            } else {
                conceptSetJList.setSelectedIndex(0);
                EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                correspondConceptSet = new HashSet<>();
                correspondConceptSet.add(evalConcept.getConcept());
                termCorrespondConceptSetMap.put(orgTerm, correspondConceptSet);
            }
            highlightInputTerm();

            // 完全照合単語，システムが追加した完全照合単語，部分照合単語に応じて
            // 階層構築オプションパネルを切り替える
            TermModel iwModel = (TermModel) termJList.getSelectedValue();
            if (iwModel.isPartiallyMatchTerm()) {
                switchConstructTreeOptionPanel(partialMatchConstructTreeOptionPanel);
                setPartiallyMatchedOptionButton(true);
                TreeConstructionOption ctOption = compoundConstructTreeOptionMap.get(iwModel);
                if (ctOption != null) {
                    if (ctOption.getOption().equals("SAME")) {
                        addAsSameConceptRadioButton.setSelected(true);
                    } else {
                        addAsSubConceptRadioButton.setSelected(true);
                    }
                } else {
                    EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                    if (evalConcept != null) {
                        ctOption = new TreeConstructionOption(evalConcept.getConcept());
                        compoundConstructTreeOptionMap.put(iwModel, ctOption);
                        addAsSameConceptRadioButton.setSelected(true);
                    }
                }
            } else if (iwModel.isSystemAdded()) {
                switchConstructTreeOptionPanel(systemAddedPerfectlyMatchedConstructTreeOptionPanel);
                if (compoundConstructTreeOptionMap.get(iwModel) == null) {
                    replaceSubClassesCheckBox.setSelected(false);
                    EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                    TreeConstructionOption ctOption = new TreeConstructionOption(evalConcept.getConcept());
                    compoundConstructTreeOptionMap.put(iwModel, ctOption);
                } else {
                    TreeConstructionOption ctOption = compoundConstructTreeOptionMap.get(iwModel);
                    replaceSubClassesCheckBox.setSelected(ctOption.isReplaceSubConcepts());
                }
            } else {
                switchConstructTreeOptionPanel(exactMatchConstructTreeOptionPanel);
            }
        }
    }

    private void switchConstructTreeOptionPanel(JPanel optionPanel) {
        constructTreeOptionPanel.remove(partialMatchConstructTreeOptionPanel);
        constructTreeOptionPanel.remove(exactMatchConstructTreeOptionPanel);
        constructTreeOptionPanel.remove(systemAddedPerfectlyMatchedConstructTreeOptionPanel);
        constructTreeOptionPanel.add(optionPanel);
        constructTreeOptionPanel.validate();
        constructTreeOptionPanel.repaint();
    }

    /**
     * @param conceptSet
     * @return
     */
    private Set<EvalConcept> getEvalConceptSet(Set<Concept> conceptSet) {
        Set<EvalConcept> evalConceptSet = new TreeSet<>();
        for (Concept c : conceptSet) {
            evalConceptSet.add(new EvalConcept(c, 0));
        }
        return evalConceptSet;
    }

    private JList getTargetTermJList() {
        /*
        if (termListRootWindow.getWindow().getLastFocusedChildWindow() == null) {
            return exactMatchTermJList;
        }
        DockingWindow lastFocusedWindow = termListRootWindow.getWindow()
                .getLastFocusedChildWindow();
        if (lastFocusedWindow.getTitle().equals(termListViews[0].getViewProperties().getTitle())) {
            return exactMatchTermJList;
        } else if (lastFocusedWindow.getTitle().equals(
                termListViews[1].getViewProperties().getTitle())) {
            return partialMatchTermJList;
        }
        */
        // TODO fix
        return exactMatchTermJList;
    }

    private Set<TermModel> getTargetTermModelSet() {
        if (getTargetTermJList() == exactMatchTermJList) {
            return exactMatchTermModelSet;
        } else if (getTargetTermJList() == partialMatchTermJList) {
            return partialMatchTermModelSet;
        }
        return exactMatchTermModelSet;
    }

    private void syncPartiallyMatchedAmbiguousConceptSet(String orgTerm,
                                                         Set<Concept> correspondConceptSet) {
        if (!exactMatchIsSyncCheckBox.isSelected()) {
            return;
        }
        for (TermModel iwModel : partialMatchTermModelSet) {
            if (iwModel.getMatchedTerm().equals(orgTerm)) {
                termCorrespondConceptSetMap.put(iwModel.getTerm(), correspondConceptSet);
            }
        }
    }

    public Concept getSelectedConcept() {
        return selectedConcept;
    }

    private void selectCorrectConcept(JList termJList) {
        if (!termJList.isSelectionEmpty() && !conceptSetJList.isSelectionEmpty()) {
            TermModel iwModel = (TermModel) termJList.getSelectedValue();
            List<EvalConcept> evalConcepts = conceptSetJList.getSelectedValuesList();
            String term = iwModel.getTerm();
            for (EvalConcept evalConcept : evalConcepts) {
                if (evalConcept == nullEvalConcept) {
                    Set<Concept> correspondConceptSet = new HashSet<>();
                    correspondConceptSet.add(nullConcept);
                    termCorrespondConceptSetMap.put(term, correspondConceptSet);
                    syncPartiallyMatchedAmbiguousConceptSet(term, correspondConceptSet);
                    labelPanel.clearData();
                    descriptionPanel.clearData();
                    aroundConceptTree.setModel(new DefaultTreeModel(null));
                    switchConstructTreeOptionPanel(exactMatchConstructTreeOptionPanel);
                    return;
                }
            }

            if (getTargetTermJList() == exactMatchTermJList) {
                switchConstructTreeOptionPanel(exactMatchConstructTreeOptionPanel);
            } else {
                switchConstructTreeOptionPanel(partialMatchConstructTreeOptionPanel);
            }

            Set<Concept> correspondConceptSet = new HashSet<>();

            for (EvalConcept evalConcept : evalConcepts) {
                correspondConceptSet.add(evalConcept.getConcept());
            }
            termCorrespondConceptSetMap.put(term, correspondConceptSet);
            syncPartiallyMatchedAmbiguousConceptSet(term, correspondConceptSet);
            selectedConcept = evalConcepts.get(0).getConcept();
            if (selectedConcept != null) {
                labelPanel.setSelectedConcept(selectedConcept);
                descriptionPanel.setSelectedConcept(selectedConcept);
                labelPanel.setLabelLangList();
                labelPanel.setLabelList();
                descriptionPanel.setDescriptionLangList();
                descriptionPanel.setDescriptionList();
            }

            showAroundConceptTree();
            for (Concept c : correspondConceptSet) {
                TreeConstructionOption ctOption = compoundConstructTreeOptionMap.get(iwModel);
                if (ctOption != null) {
                    ctOption.setConcept(c);
                    compoundConstructTreeOptionMap.put(iwModel, ctOption);
                }
            }
            project.addLog("Concept Selection", selectedConcept);
        }
    }

    /**
     * 選択されている概念のオントロジー中のルートまでのパスを表示
     */
    private void showAroundConceptTree() {
        if (showAroundConceptTreeCheckBox.isSelected()) {
            List<EvalConcept> evalConceptList = conceptSetJList.getSelectedValuesList();
            Set<List<Concept>> pathToRootSet = new HashSet<>();
            for (EvalConcept ec : evalConceptList) {
                pathToRootSet.addAll(OWLOntologyManager.getPathToRootSet(ec.getConcept().getURI()));
                int pathSize = 0;
                for (List<Concept> pathToRoot : pathToRootSet) {
                    if (pathSize < pathToRoot.size()) {
                        pathSize = pathToRoot.size();
                    }
                }
                if (pathSize <= 1
                        && DODDLE_OWL.GENERAL_ONTOLOGY_NAMESPACE_SET.contains(ec.getConcept()
                        .getNameSpace())) {
                    pathToRootSet.clear();
                    switch (ec.getConcept().getNameSpace()) {
                        case DODDLEConstants.EDR_URI:
                            pathToRootSet.addAll(EDRTree.getEDRTree().getConceptPathToRootSet(
                                    ec.getConcept().getLocalName()));
                            break;
                        case DODDLEConstants.EDRT_URI:
                            pathToRootSet.addAll(EDRTree.getEDRTTree().getConceptPathToRootSet(
                                    ec.getConcept().getLocalName()));
                            break;
                        case DODDLEConstants.WN_URI:
                            pathToRootSet.addAll(WordNet.getPathToRootSet(Long.valueOf(ec.getConcept()
                                    .getLocalName())));
                            break;
                        case DODDLEConstants.JPN_WN_URI:
                            pathToRootSet.addAll(JaWordNetTree.getJPNWNTree().getConceptPathToRootSet(
                                    ec.getConcept().getLocalName()));
                            break;
                    }
                }
            }
            TreeModel model = constructClassPanel.getDefaultConceptTreeModel(pathToRootSet,
                    ConceptTreeMaker.DODDLE_CLASS_ROOT_URI);
            aroundConceptTree.setModel(model);
            for (int i = 0; i < aroundConceptTree.getRowCount(); i++) {
                aroundConceptTree.expandPath(aroundConceptTree.getPathForRow(i));
            }
        } else {
            aroundConceptTree.setModel(new DefaultTreeModel(null));
        }
    }

    private final ImageIcon bestMatchIcon = Utils.getImageIcon("best_match_icon.png");
    private final ImageIcon ConceptNodeIcon = Utils.getImageIcon("sin_icon.png");

    class AroundTreeCellRenderer extends DefaultTreeCellRenderer {

        AroundTreeCellRenderer() {
            setOpaque(true);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component component = super.getTreeCellRendererComponent(tree, value, selected,
                    expanded, leaf, row, hasFocus);

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
                if (node.isLeaf()) {
                    setIcon(bestMatchIcon);
                } else {
                    setIcon(ConceptNodeIcon);
                }
            }
            return component;
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == exactMatchTermJList) {
            selectAmbiguousConcept(exactMatchTermJList);
            showOnlyRelatedCompoundWords();
        } else if (e.getSource() == partialMatchTermJList) {
            selectAmbiguousConcept(partialMatchTermJList);
        } else if (e.getSource() == conceptSetJList) {
            selectCorrectConcept(getTargetTermJList());
        } else if (e.getSource() == highlightPartJList) {
            jumpHilightPart();
        }
    }

    private void jumpHilightPart() {
        Integer lineNum = (Integer) highlightPartJList.getSelectedValue();
        Rectangle rect = documentArea.getVisibleRect();
        rect.y = 0;
        documentArea.scrollRectToVisible(rect);
        int lineHeight = documentArea.getFontMetrics(documentArea.getFont()).getHeight();
        // System.out.println(lineHeight);
        rect.y = (lineNum + 1) * lineHeight;
        documentArea.scrollRectToVisible(rect);
    }

    public void initTermList() {
        systemAddedTermSet = new HashSet<>();
        termModelSet = inputModule.getTermModelSet();
        exactMatchTermModelSet = new TreeSet<>();
        partialMatchTermModelSet = new TreeSet<>();

        for (TermModel itModel : termModelSet) {
            if (itModel.isPartiallyMatchTerm()) {
                partialMatchTermModelSet.add(itModel);
            } else {
                exactMatchTermModelSet.add(itModel);
                if (itModel.isSystemAdded()) {
                    systemAddedTermSet.add(itModel.getTerm());
                }
            }
        }
        exactMatchTermJList.setListData(exactMatchTermModelSet.toArray());
        exactMatchTermJListTitle.setTitle(Translator.getTerm("ExactMatchTermList")
                + " (" + exactMatchTermModelSet.size() + ")");

        partialMatchTermJList.setListData(partialMatchTermModelSet.toArray());
        partialMatchTermJListTitle.setTitle(Translator.getTerm("PartialMatchTermList")
                + " (" + partialMatchTermModelSet.size() + ")");

        termConceptSetMap = inputModule.getTermConceptSetMap();

        Set<String> undefinedTermSet = inputModule.getUndefinedTermSet();
        DefaultListModel listModel = undefinedTermListPanel.getModel();
        listModel.clear();
        for (String term : undefinedTermSet) {
            if (0 < term.length()) {
                listModel.addElement(term);
            }
        }
        undefinedTermListPanel.setTitleWithSize();
        exactMatchTermJList.repaint();
        partialMatchTermJList.repaint();
    }

    public boolean isLoadInputTermSet() {
        return inputModule.isLoadInputTermSet();
    }

    public void loadInputTermSet(File file, int taskCnt) {
        if (!file.exists()) {
            inputModule.setIsLoadInputTermSet();
            return;
        }
        Set<String> termSet = new HashSet<>();
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String term = line.replaceAll(System.lineSeparator(), "");
                    termSet.add(term);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadInputTermSet(termSet, taskCnt);
    }

    public void loadInputTermSet(int projectID, Statement stmt, int taskCnt) {
        Set<String> inputTermSet = new HashSet<>();
        try {
            String sql = "SELECT * from input_term_set where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String inputTerm = URLDecoder.decode(rs.getString("Input_Term"), StandardCharsets.UTF_8);
                inputTermSet.add(inputTerm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadInputTermSet(inputTermSet, taskCnt);
    }

    private void clearPanel() {
        conceptSetJList.setListData(new Object[0]);
        clearConceptInfoPanel();
        documentArea.setText("");
        aroundConceptTree.setModel(new DefaultTreeModel(null));
    }

    public void loadInputTermSet(Set<String> termSet, int taskCnt) {
        this.termSet = termSet;
        exactMatchTermJList.clearSelection();
        partialMatchTermJList.clearSelection();
        undefinedTermListPanel.clearSelection();
        compoundConstructTreeOptionMap.clear();
        EDRTree.getEDRTree().clear();
        EDRTree.getEDRTTree().clear();
        clearPanel();
        inputModule.initData(termSet, taskCnt);
    }

    public void makeEDRTree() {
        EDRTree.getEDRTree().makeEDRTree(getIDSet(DODDLEConstants.EDR_URI));
        EDRTree.getEDRTTree().makeEDRTree(getIDSet(DODDLEConstants.EDRT_URI));
    }

    private Set<String> getIDSet(String ns) {
        Set<String> idSet = new HashSet<>();
        for (TermModel itModel : exactMatchTermModelSet) {
            String term = itModel.getTerm();
            Set<Concept> conceptSet = termConceptSetMap.get(term);
            if (conceptSet == null) {
                continue;
            }
            for (Concept c : termConceptSetMap.get(term)) {
                if (c.getNameSpace().equals(ns)) {
                    idSet.add(c.getLocalName());
                }
            }
        }
        return idSet;
    }

    public void addInputTermSet(Set<String> addedTermSet, int taskCnt) {
        clearPanel();
        if (termSet == null) {
            termSet = new HashSet<>();
        }
        termSet.addAll(addedTermSet);
        inputModule.initData(termSet, taskCnt);
    }

    public Set<Concept> getInputConceptSet() {
        return inputConceptSet;
    }

    public Set<Concept> getSystemAddedInputConceptSet() {
        return systemAddedInputConceptSet;
    }

    public Set<Concept> getInputNounConceptSet() {
        return inputNounConceptSet;
    }

    public Set<Concept> getInputVerbConceptSet() {
        return inputVerbConceptSet;
    }

    public Set<TermModel> getTermModelSet() {
        return inputModule.getTermModelSet();
    }

    public AutomaticDisAmbiguationAction getAutomaticDisAmbiguationAction() {
        return automaticDisAmbiguationAction;
    }

    // public class ShowConceptDescriptionAction extends AbstractAction {
    //
    // public ShowConceptDescriptionAction(String title) {
    // super(title);
    // }
    //
    // public void actionPerformed(ActionEvent e) {
    // EvalConcept evalConcept = (EvalConcept)
    // conceptSetJList.getSelectedValue();
    // if (evalConcept != null) {
    // conceptDescriptionFrame.setConcept(evalConcept.getConcept().getURI());
    // setInputConceptSet();
    // conceptDescriptionFrame.setInputConceptSet();
    // conceptDescriptionFrame.setVisible(true);
    // }
    // }
    // }

    class ConceptDescriptionFrame extends JFrame {

        private final ConceptDescriptionUI conceptDescrptionPanel;

        ConceptDescriptionFrame() {
            setBounds(50, 50, 800, 600);
            conceptDescrptionPanel = new ConceptDescriptionUI();
            Container contentPane = getContentPane();
            contentPane.add(conceptDescrptionPanel);
        }

        public void setConcept(String id) {
            conceptDescrptionPanel.setConcept(id);
        }

        public void setInputConceptSet() {
            conceptDescrptionPanel.setInputConceptSet(inputConceptSet);
        }
    }

    public class AutomaticDisAmbiguationAction extends AbstractAction {

        private Set<String> termSet;

        AutomaticDisAmbiguationAction(String title) {
            super(title);
        }

        private Map<Concept, EvalConcept> getConceptEvalConceptMap() {
            Map<Concept, EvalConcept> conceptEvalConceptMap = new HashMap<>();
            for (TermModel termModel : exactMatchTermModelSet) {
                String inputTerm = termModel.getMatchedTerm();
                for (Concept c : termConceptSetMap.get(inputTerm)) {
                    if (conceptEvalConceptMap.get(c) == null) {
                        conceptEvalConceptMap.put(c, new EvalConcept(c, 0));
                    }
                }
            }
            return conceptEvalConceptMap;
        }

        private void calcEvalValueUsingSpreadActivatingAlgorithm(int i,
                                                                 Object[] allDisambiguationCandidate,
                                                                 Map<Concept, EvalConcept> conceptEvalConceptMap) {
            for (int j = i + 1; j < allDisambiguationCandidate.length; j++) {
                Concept c1 = (Concept) allDisambiguationCandidate[i];
                EvalConcept ec1 = conceptEvalConceptMap.get(c1);
                Concept c2 = (Concept) allDisambiguationCandidate[j];
                EvalConcept ec2 = conceptEvalConceptMap.get(c2);
                double ev = 0;
                if (OptionDialog.isCheckShortestSpreadActivation()) {
                    ev = CalcConceptDistanceUtil.getShortestConceptDistance(c1, c2);
                } else if (OptionDialog.isCheckLongestSpreadActivation()) {
                    ev = CalcConceptDistanceUtil.getLongestConceptDistance(c1, c2);
                } else if (OptionDialog.isCheckAverageSpreadActivation()) {
                    ev = CalcConceptDistanceUtil.getAverageConceptDistance(c1, c2);
                }
                if (0 < ev) {
                    ec1.setEvalValue(ec1.getEvalValue() + (1 / ev));
                    ec2.setEvalValue(ec2.getEvalValue() + (1 / ev));
                }
            }
        }

        private void calcEvalValueUsingSupSubSibConcepts(Concept c, EvalConcept ec) {
            int evalValue = 0;
            if (OptionDialog.isCheckSupConcepts()) {
                evalValue += cntRelevantSupConcepts(c);
            }
            if (OptionDialog.isCheckSubConcepts()) {
                evalValue += cntRelevantSubConcepts(c);
            }
            if (OptionDialog.isCheckSiblingConcepts()) {
                evalValue += cntRelevantSiblingConcepts(c);
            }
            ec.setEvalValue(ec.getEvalValue() + evalValue);
        }

        /**
         * 多義性のある概念リストと入力語彙を入力として，評価値つき概念リストを返すメソッド
         */
        void setTermEvalConceptSetMap() {
            if (termModelSet == null) {
                return;
            }
            termSet = new HashSet<>();
            for (TermModel iwModel : exactMatchTermModelSet) {
                termSet.add(iwModel.getTerm());
            }
            termEvalConceptSetMap = new HashMap<>();
            Map<Concept, EvalConcept> conceptEvalConceptMap = getConceptEvalConceptMap();

            DODDLE_OWL.STATUS_BAR.setLastMessage(Translator
                    .getTerm("AutomaticInputConceptSelectionAction"));
            DODDLE_OWL.STATUS_BAR.startTime();
            DODDLE_OWL.STATUS_BAR.initNormal(conceptEvalConceptMap.keySet().size());
            Object[] allDisambiguationCandidate = conceptEvalConceptMap.keySet().toArray();
            for (int i = 0; i < allDisambiguationCandidate.length; i++) {
                Concept c = (Concept) allDisambiguationCandidate[i];
                EvalConcept ec = conceptEvalConceptMap.get(c);
                if (OptionDialog.isUsingSpreadActivatingAlgorithm()) {
                    calcEvalValueUsingSpreadActivatingAlgorithm(i,
                            allDisambiguationCandidate, conceptEvalConceptMap);
                }
                calcEvalValueUsingSupSubSibConcepts(c, ec);
                DODDLE_OWL.STATUS_BAR.setLastMessage(Translator
                        .getTerm("AutomaticInputConceptSelectionAction")
                        + " ("
                        + (i + 1)
                        + "/"
                        + allDisambiguationCandidate.length + ")");
                DODDLE_OWL.STATUS_BAR.addValue();
            }

            for (TermModel termModel : exactMatchTermModelSet) {
                String inputTerm = termModel.getMatchedTerm();
                Set<Concept> conceptSet = termConceptSetMap.get(inputTerm);
                Set<EvalConcept> evalConceptSet = new TreeSet<>();
                for (Concept c : conceptSet) {
                    if (conceptEvalConceptMap.get(c) != null) {
                        evalConceptSet.add(conceptEvalConceptMap.get(c));
                    }
                }
                evalConceptSet.add(nullEvalConcept);
                termEvalConceptSetMap.put(inputTerm, evalConceptSet);
            }
            DODDLE_OWL.STATUS_BAR.hideProgressBar();
        }

        private Set<Set<String>> getSiblingConceptSet(Concept c) {
            Set<Set<String>> siblingConceptSet = null;
            switch (c.getNameSpace()) {
                case DODDLEConstants.EDR_URI:
                    siblingConceptSet = EDRTree.getEDRTree().getSiblingURISet(c.getURI());
                    break;
                case DODDLEConstants.EDRT_URI:
                    siblingConceptSet = EDRTree.getEDRTTree().getSiblingURISet(c.getURI());
                    break;
                case DODDLEConstants.WN_URI:
                    siblingConceptSet = WordNet.getSiblingConceptSet(Long.valueOf(c.getLocalName()));
                    break;
            }
            return siblingConceptSet;
        }

        private Set<Set<String>> getSubConceptSet(Concept c) {
            Set<Set<String>> subConceptSet = null;
            switch (c.getNameSpace()) {
                case DODDLEConstants.EDR_URI:
                    subConceptSet = EDRTree.getEDRTree().getSubURISet(c.getURI());
                    break;
                case DODDLEConstants.EDRT_URI:
                    subConceptSet = EDRTree.getEDRTTree().getSubURISet(c.getURI());
                    break;
                case DODDLEConstants.WN_URI:
                    subConceptSet = WordNet.getSubIDSet(Long.valueOf(c.getLocalName()));
                    break;
            }
            return subConceptSet;
        }

        private Set<List<String>> getPathToRootSet(Concept c) {
            Set<List<String>> pathSet = null;
            switch (c.getNameSpace()) {
                case DODDLEConstants.EDR_URI:
                    pathSet = EDRTree.getEDRTree().getURIPathToRootSet(c.getLocalName());
                    break;
                case DODDLEConstants.EDRT_URI:
                    pathSet = EDRTree.getEDRTTree().getURIPathToRootSet(c.getLocalName());
                    break;
                case DODDLEConstants.WN_URI:
                    pathSet = WordNet.getURIPathToRootSet(Long.valueOf(c.getLocalName()));
                    break;
                case DODDLEConstants.JPN_WN_URI:
                    pathSet = JaWordNetTree.getJPNWNTree().getURIPathToRootSet(c.getLocalName());
                    break;
            }
            return pathSet;
        }

        private int getMaxEvalValue(Set<Collection<String>> pathSet, String cid) {
            if (pathSet == null) {
                return 0;
            }
            int maxEvalValue = 0;
            for (Collection<String> path : pathSet) {
                int evalValue = 0;
                for (String uri : path) {
                    if (uri == null) {
                        continue;
                    }
                    String id = Utils.getLocalName(ResourceFactory.createResource(uri));
                    if (id.equals(cid)) {
                        continue;
                    }
                    Concept c = ReferenceOntology.getConcept(uri);
                    // System.out.println(c);
                    if (isIncludeInputTerms(termSet, c)) {
                        evalValue++;
                    }
                }
                if (maxEvalValue < evalValue) {
                    maxEvalValue = evalValue;
                }
            }
            return maxEvalValue;
        }

        private int cntRelevantSiblingConcepts(Concept c) {
            Set pathSet = getSiblingConceptSet(c);
            return getMaxEvalValue(pathSet, c.getLocalName());
        }

        private int cntRelevantSupConcepts(Concept c) {
            Set pathSet = getPathToRootSet(c);
            return getMaxEvalValue(pathSet, c.getLocalName());
        }

        private int cntRelevantSubConcepts(Concept c) {
            Set pathSet = getSubConceptSet(c);
            return getMaxEvalValue(pathSet, c.getLocalName());
        }

        private boolean isIncludeInputTerms(Set<String> termSet, Concept c) {
            if (c == null) {
                return false;
            }
            Map<String, List<DODDLELiteral>> langLabelListMap = c.getLangLabelListMap();
            for (List<DODDLELiteral> labelList : langLabelListMap.values()) {
                for (DODDLELiteral label : labelList) {
                    if (termSet.contains(label.getString())) {
                        return true;
                    }
                }
            }
            Map<String, List<DODDLELiteral>> langDescriptionListMap = c.getLangDescriptionListMap();
            for (List<DODDLELiteral> descriptionList : langDescriptionListMap.values()) {
                for (DODDLELiteral description : descriptionList) {
                    if (termSet.contains(description.getString())) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void doAutomaticDisambiguation() {
            SwingWorker<String, String> worker = new SwingWorker<>() {
                public String doInBackground() {
                    makeEDRTree();
                    setTermEvalConceptSetMap();
                    return "done";
                }
            };
            DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        }

        public void doDisambiguationTest() {
            new Thread(() -> {
                OptionDialog.setUsingSpreadActivationAlgorithmForDisambiguation(false);
                boolean[][] pattern = {{true, false, false}, {false, true, false},
                        {false, false, true}, {true, true, false}, {true, false, true},
                        {false, true, true}, {true, true, true}};
                String[] patternName = {"Sup.txt", "Sub.txt", "Sib.txt", "Sup_Sub.txt",
                        "Sup_Sib.txt", "Sib_Sub.txt", "Sup_Sub_Sib.txt"};

                for (int i = 0; i < pattern.length; i++) {
                    System.out.println(pattern[i][0] + " " + pattern[i][1] + " "
                            + pattern[i][2]);
                    OptionDialog.setSupDisambiguation(pattern[i][0]);
                    OptionDialog.setSubDisambiguation(pattern[i][1]);
                    OptionDialog.setSiblingDisambiguation(pattern[i][2]);
                    setTermEvalConceptSetMap();
                    saveTermEvalConceptSet(new File(patternName[i]));
                }
                OptionDialog.setSupDisambiguation(false);
                OptionDialog.setSubDisambiguation(false);
                OptionDialog.setSiblingDisambiguation(false);
                OptionDialog.setUsingSpreadActivationAlgorithmForDisambiguation(true);

                OptionDialog.setShortestSpreadActivatingAlgorithmforDisambiguation(true);
                System.out.println("shortest");
                setTermEvalConceptSetMap();
                saveTermEvalConceptSet(new File("shortest.txt"));

                OptionDialog.setLongestSpreadActivatingAlgorithmforDisambiguation(true);
                System.out.println("longest");
                setTermEvalConceptSetMap();
                saveTermEvalConceptSet(new File("longest.txt"));

                OptionDialog.setAverageSpreadActivatingAlgorithmforDisambiguation(true);
                System.out.println("average");
                setTermEvalConceptSetMap();
                saveTermEvalConceptSet(new File("average.txt"));

            }).start();
        }

        public void actionPerformed(ActionEvent e) {
            doAutomaticDisambiguation();
        }
    }

    public void showAllTerm() {
        JFrame frame = new JFrame();
        Set termSet = new TreeSet();
        if (termModelSet != null) {
            for (TermModel iwModel : termModelSet) {
                termSet.add(iwModel.getTerm());
            }
            termSet.addAll(inputModule.getUndefinedTermSet());
            JList list = new JList();
            list.setBorder(BorderFactory.createTitledBorder("入力されたすべての単語(" + termSet.size() + ")"));
            list.setListData(termSet.toArray());
            JScrollPane listScroll = new JScrollPane(list);
            frame.getContentPane().add(listScroll);
            frame.setBounds(50, 50, 200, 600);
            frame.setVisible(true);
        }
    }

    public void saveConstructTreeOption(File file) {
        try {
            Properties properties = new Properties();
            properties.setProperty("ConstructTree.isTreeConstruction",
                    String.valueOf(perfectlyMatchedOptionPanel.isConstruction()));
            properties.setProperty("ConstructTree.isTrimmingInternalNode",
                    String.valueOf(perfectlyMatchedOptionPanel.isTrimming()));
            properties.setProperty("ConstructTree.isConstructionWithCompoundWordTree",
                    String.valueOf(partiallyMatchedOptionPanel.isConstruction()));
            properties.setProperty("ConstructTree.isTrimmingInternalNodeWithCompoundWordTree",
                    String.valueOf(partiallyMatchedOptionPanel.isTrimming()));
            properties.setProperty("ConstructTree.isAddAbstractConceptWithCompoundWordTree",
                    String.valueOf(partiallyMatchedOptionPanel.isAddAbstractConcept()));
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                properties.store(writer, "Construct Tree Option");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertConstructTreeOption() {

    }

    public void loadConstructTreeOption(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            Properties properties = new Properties();
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                properties.load(reader);
            }
            boolean t = Boolean.valueOf(properties.getProperty("ConstructTree.isTreeConstruction"));
            perfectlyMatchedOptionPanel.setConstruction(t);
            t = Boolean.valueOf(properties.getProperty("ConstructTree.isTrimmingInternalNode"));
            perfectlyMatchedOptionPanel.setTrimming(t);
            t = Boolean.valueOf(properties.getProperty("ConstructTree.isConstructionWithCompoundWordTree"));
            partiallyMatchedOptionPanel.setConstruction(t);
            t = Boolean.valueOf(properties.getProperty("ConstructTree.isTrimmingInternalNodeWithCompoundWordTree"));
            partiallyMatchedOptionPanel.setTrimming(t);
            t = Boolean.valueOf(properties.getProperty("ConstructTree.isAddAbstractConceptWithCompoundWordTree"));
            partiallyMatchedOptionPanel.setAddAbstractConcept(t);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveInputTermConstructTreeOptionSet(File file) {
        if (compoundConstructTreeOptionMap == null) {
            return;
        }
        try {
            StringBuilder buf = new StringBuilder();
            for (TermModel iwModel : compoundConstructTreeOptionMap.keySet()) {
                TreeConstructionOption ctOption = compoundConstructTreeOptionMap.get(iwModel);
                if (iwModel != null && ctOption != null && ctOption.getConcept() != null) {
                    buf.append(iwModel.getTerm());
                    buf.append("\t");
                    buf.append(ctOption.getConcept().getURI());
                    buf.append("\t");
                    buf.append(ctOption.getOption());
                    buf.append(System.lineSeparator());
                }
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertInputTermConstructTreeOptionSet(int projectID, Statement stmt,
                                                      String inputTerm, String concept, String option) {
        try {
            String sql = "INSERT INTO input_term_construct_tree_option (Project_ID,Input_Term,Input_Concept,Tree_Option) "
                    + "VALUES("
                    + projectID
                    + ",'"
                    + URLEncoder.encode(inputTerm, StandardCharsets.UTF_8)
                    + "','"
                    + concept + "','" + option + "')";
            stmt.executeUpdate(sql);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void loadInputTermConstructTreeOptionSet(File file) {
        if (!file.exists()) {
            return;
        }
        compoundConstructTreeOptionMap = new HashMap<>();
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] strs = line.split("\t");
                    String iw = strs[0];
                    String uri = strs[1];
                    String opt = strs[2];
                    if (0 < iw.length()) {
                        TermModel iwModel = inputModule.makeInputTermModel(iw);
                        compoundConstructTreeOptionMap.put(iwModel,
                                new TreeConstructionOption(ReferenceOntology.getConcept(uri), opt));
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadInputTermConstructTreeOptionSet(int projectID, Statement stmt) {
        compoundConstructTreeOptionMap = new HashMap<>();
        try {
            String sql = "SELECT * from input_term_construct_tree_option where Project_ID="
                    + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String inputTerm = URLDecoder.decode(rs.getString("Input_Term"), StandardCharsets.UTF_8);
                String inputConcept = rs.getString("Input_Concept");
                String treeOption = rs.getString("Tree_Option");
                if (0 < inputTerm.length()) {
                    TermModel iwModel = inputModule.makeInputTermModel(inputTerm);
                    compoundConstructTreeOptionMap
                            .put(iwModel,
                                    new TreeConstructionOption(ReferenceOntology.getConcept(inputConcept),
                                            treeOption));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TermModel makeInputTermModel(String iw) {
        return inputModule.makeInputTermModel(iw.replaceAll("_", " "));
    }

    public void saveInputTermSet(File file) {
        if (termModelSet == null) {
            return;
        }
        try {
            StringBuilder buf = new StringBuilder();
            for (TermModel iwModel : termModelSet) {
                if (!iwModel.isSystemAdded()) {
                    buf.append(iwModel.getTerm());
                    buf.append(System.lineSeparator());
                }
            }
            for (String inputTerm : inputModule.getUndefinedTermSet()) {
                buf.append(inputTerm);
                buf.append(System.lineSeparator());
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void insertInputTerm(int projectID, Statement stmt, String inputTerm) {
        try {
            String sql = "INSERT INTO input_term_set (Project_ID,Input_Term) " + "VALUES("
                    + projectID + ",'" + URLEncoder.encode(inputTerm, StandardCharsets.UTF_8) + "')";
            stmt.executeUpdate(sql);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void loadUndefinedTermSet(File file) {
        if (!file.exists()) {
            return;
        }
        DefaultListModel undefinedTermListModel = undefinedTermListPanel.getModel();
        undefinedTermListModel.clear();
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        undefinedTermListModel.addElement(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        constructClassPanel.setUndefinedTermListModel(undefinedTermListModel);
        constructPropertyPanel.setUndefinedTermListModel(undefinedTermListModel);
    }

    public void loadUndefinedTermSet(int projectID, Statement stmt) {
        DefaultListModel undefinedTermListModel = undefinedTermListPanel.getModel();
        undefinedTermListModel.clear();
        try {
            String sql = "SELECT * from undefined_term_set where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String term = URLDecoder.decode(rs.getString("Term"), StandardCharsets.UTF_8);
                if (term != null) {
                    undefinedTermListModel.addElement(term);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        constructClassPanel.setUndefinedTermListModel(undefinedTermListModel);
        constructPropertyPanel.setUndefinedTermListModel(undefinedTermListModel);
    }

    public void loadInputConceptSet(File file) {
        if (!file.exists()) {
            return;
        }
        inputConceptSet = new HashSet<>();
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String uri = reader.readLine();
                    Concept c = ReferenceOntology.getConcept(uri);
                    if (c != null) {
                        inputConceptSet.add(c);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadInputConceptSet(int projectID, Statement stmt) {
        inputConceptSet = new HashSet<>();
        try {
            String sql = "SELECT * from input_concept_set where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String inputConcept = rs.getString("Input_Concept");
                Concept c = ReferenceOntology.getConcept(inputConcept);
                if (c != null) {
                    inputConceptSet.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUndefinedTermSet(File file) {
        DefaultListModel undefinedTermListModel = undefinedTermListPanel.getModel();
        if (undefinedTermListModel == null) {
            return;
        }
        try {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < undefinedTermListModel.getSize(); i++) {
                String undefinedTerm = (String) undefinedTermListModel.getElementAt(i);
                buf.append(undefinedTerm);
                buf.append(System.lineSeparator());
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveInputConceptSet(File file) {
        if (inputConceptSet == null) {
            return;
        }
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            StringBuilder buf = new StringBuilder();
            for (Concept c : inputConceptSet) {
                if (!systemAddedInputConceptSet.contains(c)) {
                    buf.append(c.getURI());
                    buf.append(System.lineSeparator());
                }
            }
            writer.write(buf.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void insertInputConcept(int projectID, Statement stmt) {
        try {
            for (Concept c : inputConceptSet) {
                String sql = "INSERT INTO input_concept_set (Project_ID,Input_Concept) "
                        + "VALUES(" + projectID + ",'" + c.getURI() + "')";
                stmt.executeUpdate(sql);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void saveTermCorrespondConceptSetMap(File file) {
        try {
            StringBuilder buf = new StringBuilder();
            for (Entry<String, Set<Concept>> stringSetEntry : getTermCorrespondConceptSetMap().entrySet()) {
                String term = stringSetEntry.getKey();
                Set<Concept> conceptSet = stringSetEntry.getValue();
                buf.append(term).append(",");
                for (Concept c : conceptSet) {
                    buf.append(c.getURI()).append(",");
                }
                buf.append(System.lineSeparator());
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void insertInputTermConceptSetMap(int projectID, Statement stmt, String inputTerm,
                                             Set<Concept> conceptSet) {
        try {
            for (Concept c : conceptSet) {
                String sql = "INSERT INTO input_term_concept_map (Project_ID,Input_Term,Input_Concept) "
                        + "VALUES("
                        + projectID
                        + ",'"
                        + URLEncoder.encode(inputTerm, StandardCharsets.UTF_8)
                        + "','" + c.getURI() + "')";
                stmt.executeUpdate(sql);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void saveTermConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE_OWL.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveTermCorrespondConceptSetMap(file);
        }
    }

    public void saveTermEvalConceptSet(File file) {
        if (termEvalConceptSetMap == null || termModelSet == null) {
            return;
        }
        try {
            StringBuilder buf = new StringBuilder();
            for (TermModel iwModel : termModelSet) {
                Set<EvalConcept> evalConceptSet = termEvalConceptSetMap.get(iwModel
                        .getMatchedTerm());
                if (evalConceptSet == null) {
                    continue;
                }
                buf.append(iwModel.getTerm());
                double evalValue = -1;
                for (EvalConcept ec : evalConceptSet) {
                    if (evalValue == ec.getEvalValue()) {
                        buf.append("\t").append(ec.getConcept().getURI());
                    } else {
                        if (ec.getConcept() != null) {
                            buf.append("||").append(ec.getEvalValue()).append("\t").append(ec.getConcept().getURI());
                            evalValue = ec.getEvalValue();
                        }
                    }
                }
                buf.append(System.lineSeparator());
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void savePerfectlyMatchedTerm() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE_OWL.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            savePerfectlyMatchedTerm(file);
        }
    }

    private void savePerfectlyMatchedTerm(File file) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                for (TermModel iwModel : exactMatchTermModelSet) {
                    writer.write(iwModel.getTerm());
                    writer.newLine();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void savePerfectlyMatchedTermWithCompoundWord() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE_OWL.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            savePerfectlyMatchedTermWithCompoundWord(file);
        }
    }

    private void savePerfectlyMatchedTermWithCompoundWord(File file) {
        try {
            Map perfectlyMatchedTermWithCompoundWordMap = new TreeMap();
            for (TermModel iwModel : termModelSet) {
                if (perfectlyMatchedTermWithCompoundWordMap.get(iwModel.getMatchedTerm()) != null) {
                    Set compoundWordSet = (Set) perfectlyMatchedTermWithCompoundWordMap.get(iwModel
                            .getMatchedTerm());
                    compoundWordSet.add(iwModel.getTerm());
                    perfectlyMatchedTermWithCompoundWordMap.put(iwModel.getMatchedTerm(),
                            compoundWordSet);
                } else {
                    Set compoundWordSet = new TreeSet();
                    compoundWordSet.add(iwModel.getTerm());
                    perfectlyMatchedTermWithCompoundWordMap.put(iwModel.getMatchedTerm(),
                            compoundWordSet);
                }
            }

            StringBuilder buf = new StringBuilder();
            for (Object o1 : perfectlyMatchedTermWithCompoundWordMap.keySet()) {
                String matchedTerm = (String) o1;
                buf.append(matchedTerm).append("=>");
                Set compoundWordSet = (Set) perfectlyMatchedTermWithCompoundWordMap.get(matchedTerm);
                for (Object o : compoundWordSet) {
                    String compoundWord = (String) o;
                    buf.append(compoundWord).append(",");
                }
                buf.append(System.lineSeparator());
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (writer) {
                writer.write(buf.toString());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveTermEvalConceptSet() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE_OWL.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveTermEvalConceptSet(file);
        }
    }

    public void loadTermEvalConceptSet() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            loadTermEvalConceptSet(file);
        }
    }

    public void loadTermEvalConceptSet(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            termEvalConceptSetMap = new HashMap<>();
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] termAndResults = line.split("\\|\\|");
                    String term = termAndResults[0];
                    Set<EvalConcept> evalConceptSet = new TreeSet<>();
                    for (int i = 1; i < termAndResults.length; i++) {
                        String[] valueAndURIs = termAndResults[i].split("\t");
                        double value = Double.parseDouble(valueAndURIs[0]);
                        for (int j = 1; j < valueAndURIs.length; j++) {
                            String uri = valueAndURIs[j];
                            Concept c = ReferenceOntology.getConcept(uri);
                            evalConceptSet.add(new EvalConcept(c, value));
                        }
                    }
                    evalConceptSet.add(nullEvalConcept);
                    termEvalConceptSetMap.put(term, evalConceptSet);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadTermEvalConceptSet(int projectID, java.sql.Statement stmt) {
        try {
            termEvalConceptSetMap = new HashMap<>();
            String sql = "SELECT * from term_eval_concept_set where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            Map<Integer, String> termIDMap = new HashMap<>();
            while (rs.next()) {
                String term = URLDecoder.decode(rs.getString("Term"), StandardCharsets.UTF_8);
                int termID = rs.getInt("Term_ID");
                termIDMap.put(termID, term);
            }

            for (Entry<Integer, String> entry : termIDMap.entrySet()) {
                int termID = entry.getKey();
                String term = entry.getValue();
                sql = "SELECT * from eval_concept_set where Project_ID=" + projectID
                        + " and Term_ID=" + termID;
                rs = stmt.executeQuery(sql);
                Set<EvalConcept> evalConceptSet = new TreeSet<>();
                while (rs.next()) {
                    String concept = rs.getString("Concept");
                    double evalValue = rs.getDouble("Eval_Value");
                    Concept c = ReferenceOntology.getConcept(concept);
                    evalConceptSet.add(new EvalConcept(c, evalValue));
                }
                evalConceptSet.add(nullEvalConcept);
                termEvalConceptSetMap.put(term, evalConceptSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}