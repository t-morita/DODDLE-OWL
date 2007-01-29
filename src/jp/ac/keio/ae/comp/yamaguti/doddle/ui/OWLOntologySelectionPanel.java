/*
 * @(#)  2006/12/15
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.util.*;

/**
 * @author takeshi morita
 */
public class OWLOntologySelectionPanel extends JPanel implements ActionListener {

    private JList ontologyList;
    private DefaultListModel listModel;
    private JButton addOWLFileButton;
    private JButton addOWLURIButton;
    private JButton deleteButton;

    public OWLOntologySelectionPanel() {
        listModel = new DefaultListModel();
        ontologyList = new JList(listModel);
        ontologyList.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("OWLOntologySelectionPanel.OntologyList")));
        addOWLFileButton = new JButton(Translator.getString("Add") + "(File)");
        addOWLFileButton.addActionListener(this);
        addOWLURIButton = new JButton(Translator.getString("Add") + "(URI)");
        addOWLURIButton.addActionListener(this);
        deleteButton = new JButton(Translator.getString("Remove"));
        deleteButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addOWLFileButton);
        buttonPanel.add(addOWLURIButton);
        buttonPanel.add(deleteButton);

        setLayout(new BorderLayout());
        add(new JScrollPane(ontologyList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String getType(String str) {
        String ext = FileUtils.getFilenameExt(str);
        String type = "RDF/XML";
        if (ext.equals("n3")) {
            type = "N3";
        }
        return type;
    }
    
    private void addOWLFile() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!listModel.contains(file.getAbsolutePath())) {
                try {
                    ReferenceOWLOntology refOnt = new ReferenceOWLOntology(new FileInputStream(file), getType(file.getAbsolutePath()));
                    OWLOntologyManager.addRefOntology(file.getAbsolutePath(), refOnt);                    
                    listModel.addElement(file.getAbsolutePath());
                } catch(FileNotFoundException fne) {
                    fne.printStackTrace();
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(this, "Error", "Can not set the selected ontology", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void addOWLURI() {
        String url = JOptionPane.showInputDialog(this, Translator.getString("OWLOntologySelectionPanel.InputURI"),
                "http://mmm.semanticweb.org/doddle/sample.owl");
        if (url != null) {
            if (!listModel.contains(url)) {
                try {
                    ReferenceOWLOntology refOnt = new ReferenceOWLOntology(new URL(url).openStream(), getType(url));
                    OWLOntologyManager.addRefOntology(url, refOnt);
                    listModel.addElement(url);
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(this, "Error", "Can not set the selected ontology", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void delete() {
        Object[] values = ontologyList.getSelectedValues();
        for (int i = 0; i < values.length; i++) {
            OWLOntologyManager.removeRefOntology((String)values[i]);
            listModel.removeElement(values[i]);
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

    public static void main(String[] args) {
        Translator.loadResourceBundle(DODDLE.LANG);
        JFrame frame = new JFrame();
        frame.getContentPane().add(new OWLOntologySelectionPanel());
        frame.setSize(new Dimension(500, 400));
        frame.setVisible(true);
    }
}
