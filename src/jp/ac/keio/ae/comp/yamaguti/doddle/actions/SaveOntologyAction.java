/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.w3c.dom.*;
import org.w3c.dom.Document;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class SaveOntologyAction extends AbstractAction {

    private String conversionType;
    public static final String OWL_ONTOLOGY = "OWL";
    public static final String FREEMIND_ONTOLOGY = "FREEMIND";

    public SaveOntologyAction(String title, String type) {
        super(title);
        conversionType = type;
    }

    public static Model getOntology(DODDLEProject currentProject) {
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
        ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();

        Model ontology = JenaModelMaker.makeClassModel(constructClassPanel.getIsaTreeModelRoot(), ModelFactory
                .createDefaultModel(), ConceptTreePanel.CLASS_ISA_TREE);
        JenaModelMaker.makeClassModel(constructClassPanel.getHasaTreeModelRoot(), ontology,
                ConceptTreePanel.CLASS_HASA_TREE);
        JenaModelMaker.makePropertyModel(constructPropertyPanel.getIsaTreeModelRoot(), ontology,
                ConceptTreePanel.PROPERTY_ISA_TREE);
        JenaModelMaker.makePropertyModel(constructPropertyPanel.getHasaTreeModelRoot(), ontology,
                ConceptTreePanel.PROPERTY_HASA_TREE);
        conceptDefinitionPanel.addConceptDefinition(ontology);
        return ontology;
    }

    public void saveFreeMindOntology(DODDLEProject project, File file) {
        try {
            ConstructClassPanel constructClassPanel = project.getConstructClassPanel();
            ConstructPropertyPanel constructPropertyPanel = project.getConstructPropertyPanel();

            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
            Document document = docbuilder.newDocument();

            Element freeMindNode = document.createElement("map");
            freeMindNode.setAttribute("version", "0.9.0 Beta8");
            document.appendChild(freeMindNode);

            Element freeMindRootNode = document.createElement("node");
            freeMindRootNode.setAttribute("ID", FreeMindModelMaker.FREEMIND_URI + "RootConcept");
            freeMindRootNode.setAttribute("TEXT", "ルート概念");
            freeMindNode.appendChild(freeMindRootNode);

            ConceptTreeNode rootNode = constructClassPanel.getIsaTreeModelRoot();
            Element freeMindNounConceptRootNode = FreeMindModelMaker.getFreeMindElement(rootNode, document);
            freeMindRootNode.appendChild(freeMindNounConceptRootNode);
            FreeMindModelMaker.makeFreeMindModel(document, rootNode, freeMindNounConceptRootNode);

            rootNode = constructPropertyPanel.getIsaTreeModelRoot();
            Element freeMindVerbConceptRootNode = FreeMindModelMaker.getFreeMindElement(rootNode, document);
            freeMindRootNode.appendChild(freeMindVerbConceptRootNode);
            FreeMindModelMaker.makeFreeMindModel(document, rootNode, freeMindVerbConceptRootNode);

            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer transformer = tfactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(file));
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerConfigurationException tfe) {
            tfe.printStackTrace();
        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }

    public void saveOWLOntology(DODDLEProject project, File file) {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            Model ontModel = getOntology(project);
            RDFWriter rdfWriter = ontModel.getWriter("RDF/XML-ABBREV");
            rdfWriter.setProperty("xmlbase", DODDLEConstants.BASE_URI);
            rdfWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
            rdfWriter.write(ontModel, writer, DODDLEConstants.BASE_URI);
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
    
    public void saveOWLOntology(int projectID, IDBConnection con, DODDLEProject project) {
        ModelMaker maker = ModelFactory.createModelRDBMaker(con);
        Model model = maker.createModel("DODDLE Project "+projectID);
        model.add(getOntology(project));
        model.close();
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            DODDLEProject currentProject = DODDLE.getCurrentProject();
            if (conversionType.equals(OWL_ONTOLOGY)) {
                saveOWLOntology(currentProject, file);
                DODDLE.STATUS_BAR.setText(Translator.getTerm("SaveOWLOntologyAction"));
            } else if (conversionType.equals(FREEMIND_ONTOLOGY)) {
                saveFreeMindOntology(currentProject, file);
                DODDLE.STATUS_BAR.setText(Translator.getTerm("SaveFreeMindOntologyAction"));
            }
        }
    }

}
