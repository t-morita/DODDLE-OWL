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

package io.github.doddle_owl.views.reference_ontology_selection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.ProjectFileNames;
import io.github.doddle_owl.models.reference_ontology_selection.OWLOntologyExtractionTemplate;
import io.github.doddle_owl.models.reference_ontology_selection.ReferenceOWLOntology;
import io.github.doddle_owl.utils.FreeMindModelMaker;
import io.github.doddle_owl.utils.OWLOntologyManager;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * @author Takeshi Morita
 */
class OWLOntologySelectionPanel extends JPanel implements ActionListener,
        ListSelectionListener {

    private final JList ontologyList;
    private final DefaultListModel listModel;
    private final OWLMetaDataPanel owlMetaDataPanel;
    private final JButton addOWLFileButton;
    private final JButton addOWLURIButton;
    private final JButton deleteButton;
    private final NameSpaceTable nsTable;


    public void initialize() {

    }

    public OWLOntologySelectionPanel(NameSpaceTable nst) {
        nsTable = nst;
        listModel = new DefaultListModel();
        ontologyList = new JList(listModel);
        ontologyList.addListSelectionListener(this);
        var ontologyListScroll = new JScrollPane(ontologyList);
        ontologyListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("OntologyList")));
        addOWLFileButton = new JButton(Translator.getTerm("AddOntologyFromFileButton"));
        addOWLFileButton.addActionListener(this);
        addOWLURIButton = new JButton(Translator.getTerm("AddOntologyFromURIButton"));
        addOWLURIButton.addActionListener(this);
        deleteButton = new JButton(Translator.getTerm("RemoveButton"));
        deleteButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        buttonPanel.add(addOWLFileButton);
        buttonPanel.add(addOWLURIButton);
        buttonPanel.add(deleteButton);

        JPanel ontologyListPanel = new JPanel();
        ontologyListPanel.setPreferredSize(new Dimension(100, 150));
        ontologyListPanel.setMinimumSize(new Dimension(100, 150));
        ontologyListPanel.setLayout(new BorderLayout());
        ontologyListPanel.add(ontologyListScroll, BorderLayout.CENTER);
        ontologyListPanel.add(Utils.createWestPanel(buttonPanel), BorderLayout.SOUTH);

        owlMetaDataPanel = new OWLMetaDataPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ontologyListPanel, owlMetaDataPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(DODDLEConstants.DIVIDER_SIZE);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    private String getType(String str) {
        String ext = FileUtils.getFilenameExt(str);
        String type = "RDF/XML";
        if (ext.equals("n3")) {
            type = "N3";
        }
        return type;
    }

    public void addOWLOntology(ReferenceOWLOntology refOnt) {
        OWLOntologyManager.addRefOntology(refOnt.getURI(), refOnt);
        listModel.addElement(refOnt.getURI());
        owlMetaDataPanel.setMetaData(refOnt);
    }

    private void addOWLOntology(Model ontModel, String uri) {
        try {
            ReferenceOWLOntology refOnt = new ReferenceOWLOntology(ontModel, uri, nsTable);
            addOWLOntology(refOnt);
            DODDLE_OWL.STATUS_BAR.setText("Add Reference OWL Ontology");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Can not set the selected ontology_api", "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addOWLFile() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String uri = file.getAbsolutePath();
            if (!listModel.contains(uri)) {
                try {
                    Model ontModel;
                    if (uri.endsWith("mm")) {
                        ontModel = FreeMindModelMaker.getOWLModel(file);
                    } else {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        ontModel = Utils.getOntModel(fileInputStream, "---", getType(uri),
                                DODDLEConstants.BASE_URI);
                    }
                    addOWLOntology(ontModel, uri);
                } catch (FileNotFoundException fne) {
                    fne.printStackTrace();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Can not set the selected ontology_api",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }

    private void addOWLURI() {
        String url = JOptionPane.showInputDialog(this, Translator.getTerm("InputURIMessage"),
                "http://www.yamaguchi.comp.ae.keio.ac.jp/doddle/sample.owl");
        if (url != null) {
            if (!listModel.contains(url)) {
                try {
                    InputStream inputStream = new URL(url).openStream();
                    Model ontModel = Utils.getOntModel(inputStream, "---", getType(url),
                            DODDLEConstants.BASE_URI);
                    addOWLOntology(ontModel, url);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error",
                            "Can not set the selected ontology_api", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }

    private void delete() {
        List<String> values = ontologyList.getSelectedValuesList();
        for (String value : values) {
            OWLOntologyManager.removeRefOntology(value);
            listModel.removeElement(value);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addOWLFileButton) {
            addOWLFile();
        } else if (e.getSource() == addOWLURIButton) {
            addOWLURI();
        } else if (e.getSource() == deleteButton) {
            delete();
        }
    }

    public void saveOWLMetaDataSet(File saveDir) {
        try {
            for (int i = 0; i < listModel.getSize(); i++) {
                int num = i + 1;
                String uri = (String) listModel.getElementAt(i);
                ReferenceOWLOntology refOnt = OWLOntologyManager.getRefOntology(uri);
                Properties properties = new Properties();
                properties.setProperty("isAvailable", String.valueOf(refOnt.isAvailable()));
                properties.setProperty("Location", refOnt.getURI());
                OWLOntologyExtractionTemplate template = refOnt.getOWLOntologyExtractionTemplate();
                properties.setProperty("SearchConceptTemplate", template.getSearchConceptTemplate()
                        .getAbsolutePath());
                properties.setProperty("SearchSubConceptTemplate", template
                        .getSearchSubConceptTemplate().getAbsolutePath());
                properties.setProperty("SearchClassSetTemplate", template
                        .getSearchClassSetTemplate().getAbsolutePath());
                properties.setProperty("SearchPropertySetTemplate", template
                        .getSearchPropertySetTemplate().getAbsolutePath());
                properties.setProperty("SearchRegionSetTemplate", template
                        .getSearchRegionSetTemplate().getAbsolutePath());
                Path path = Paths.get(saveDir + "/" + ProjectFileNames.OWL_META_DATA_FILE + num + ".txt");
                BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                try (writer) {
                    properties.store(writer, "Save OWL Meta Data" + num);
                }
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public void loadOWLMetaDataSet(File loadDir) {
        if (!loadDir.exists()) {
            return;
        }
        File[] owlMetaDataFiles = loadDir.listFiles();
        BufferedReader reader = null;
        try {
            for (File owlMetaDataFile : owlMetaDataFiles) {
                if (!owlMetaDataFile.isFile()) {
                    continue;
                }
                FileInputStream fis = new FileInputStream(owlMetaDataFile);
                reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                Properties properties = new Properties();
                properties.load(reader);
                boolean isAvailable = Boolean.valueOf(properties.getProperty("isAvailable"));
                String uri = properties.getProperty("Location");
                File file = new File(uri);
                if (file.exists()) {
                    Model ontModel;
                    if (uri.endsWith("mm")) {
                        ontModel = FreeMindModelMaker.getOWLModel(file);
                    } else {
                        FileInputStream inputStream = new FileInputStream(file);
                        ontModel = ModelFactory.createDefaultModel();
                        ontModel.read(inputStream, DODDLEConstants.BASE_URI, getType(uri));
                    }
                    addOWLOntology(ontModel, uri);
                } else {
                    InputStream inputStream = new URL(uri).openStream();
                    Model ontModel = Utils.getOntModel(inputStream, "---", getType(uri),
                            DODDLEConstants.BASE_URI);
                    ontModel.read(inputStream, DODDLEConstants.BASE_URI, getType(uri));
                    addOWLOntology(ontModel, uri);
                }
                ReferenceOWLOntology refOnt = OWLOntologyManager.getRefOntology(uri);
                refOnt.setAvailable(isAvailable);
                OWLOntologyExtractionTemplate template = refOnt.getOWLOntologyExtractionTemplate();
                template.setSearchConceptTemplate(new File(properties
                        .getProperty("SearchConceptTemplate")));
                template.setSearchSubConceptTemplate(new File(properties
                        .getProperty("SearchSubConceptTemplate")));
                template.setSearchClassSetTemplate(new File(properties
                        .getProperty("SearchClassSetTemplate")));
                template.setSearchPropertySetTemplate(new File(properties
                        .getProperty("SearchPropertySetTemplate")));
                template.setSearchRegionSetTemplate(new File(properties
                        .getProperty("SearchRegionSetTemplate")));
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            DODDLE_OWL.getLogger().info("MalformedURLException");
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        if (0 < ontologyList.getModel().getSize()) {
            ontologyList.setSelectedIndex(0);
        }
    }

    public static void main(String[] args) {
        Translator.loadDODDLEComponentOntology(DODDLEConstants.LANG);
        JFrame frame = new JFrame();
        NameSpaceTable nsTable = new NameSpaceTable();
        frame.getContentPane().add(new OWLOntologySelectionPanel(nsTable));
        frame.setSize(new Dimension(500, 400));
        frame.setVisible(true);
    }

    public void valueChanged(ListSelectionEvent e) {
        String uri = (String) ontologyList.getSelectedValue();
        ReferenceOWLOntology refOnt = OWLOntologyManager.getRefOntology(uri);
        if (refOnt != null) {
            owlMetaDataPanel.setMetaData(refOnt);
        }
    }
}
