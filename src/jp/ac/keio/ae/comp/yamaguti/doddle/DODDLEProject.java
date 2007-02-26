package jp.ac.keio.ae.comp.yamaguti.doddle;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.infonode.docking.*;
import net.infonode.docking.util.*;

/**
 * @author takeshi morita
 */
public class DODDLEProject extends JInternalFrame implements ActionListener {
    private View[] views;    
    private RootWindow rootWindow;
    private OntologySelectionPanel ontSelectionPanel;
    private DocumentSelectionPanel docSelectionPanel;
    private InputWordSelectionPanel inputWordSelectinPanel;
    private DisambiguationPanel disambiguationPanel;
    private ConstructClassPanel constructClassPanel;
    private ConstructPropertyPanel constructPropertyPanel;
    private ConceptDefinitionPanel conceptDefinitionPanel;
    private VisualizationPanel visualizationPanel;

    private int userIDCount;
    private Map<String, Concept> uriConceptMap;

    private JMenu projectMenu;
    private JCheckBoxMenuItem projectMenuItem;

    private View visualizationPanelView;
    
    public DODDLEProject(String title, JMenu pm) {
        super(title, true, true, true, true);
        projectMenu = pm;
        projectMenuItem = new JCheckBoxMenuItem(title);
        projectMenuItem.addActionListener(this);
        projectMenu.add(projectMenuItem);

        userIDCount = 0;
        uriConceptMap = new HashMap<String, Concept>();
        constructClassPanel = new ConstructClassPanel(this);
        ontSelectionPanel = new OntologySelectionPanel();
        constructPropertyPanel = new ConstructPropertyPanel(this);
        disambiguationPanel = new DisambiguationPanel(constructClassPanel, constructPropertyPanel, this);
        inputWordSelectinPanel = new InputWordSelectionPanel(disambiguationPanel);
        docSelectionPanel = new DocumentSelectionPanel(inputWordSelectinPanel, this);
        conceptDefinitionPanel = new ConceptDefinitionPanel(this);
        if (DODDLE.getDODDLEPlugin() != null) {
            visualizationPanel = new VisualizationPanel(this);
        }
        disambiguationPanel.setDocumentSelectionPanel(docSelectionPanel);

        views = new View[7];
        ViewMap viewMap = new ViewMap();

        views[0] = new View(Translator.getString("OntologySelectionPanel.Text"), Utils.getImageIcon("ontology.png"),
                ontSelectionPanel);
        views[1] = new View(Translator.getString("DocumentSelectionPanel.Text"), Utils.getImageIcon("open_doc.gif"),
                docSelectionPanel);
        views[2] = new View(Translator.getString("InputWordSelectionPanel.Text"), Utils.getImageIcon("input_words.png"),
                inputWordSelectinPanel);
        views[3] = new View(Translator.getString("DisambiguationPanel.Text"), Utils.getImageIcon("disambiguation.png"),
                disambiguationPanel);
        views[4] = new View(Translator.getString("ClassTreePanel.Text"), Utils.getImageIcon("class_tree.png"),
                constructClassPanel);
        views[5] = new View(Translator.getString("PropertyTreePanel.Text"), Utils.getImageIcon("property_tree.png"),
                constructPropertyPanel);
        views[6] = new View(Translator.getString("ConceptDefinitionPanel.Text"), Utils.getImageIcon("non-taxonomic.png"),
                conceptDefinitionPanel);        
        for (int i = 0; i < views.length; i++) {
            viewMap.addView(i, views[i]);
        }
        if (DODDLE.getDODDLEPlugin() != null) {
            visualizationPanelView = new View(Translator.getString("VisualizationPanel.Text"), Utils.getImageIcon("mr3_logo.png"),
                    visualizationPanel);
            viewMap.addView(7, visualizationPanelView);
        }
        rootWindow = Utils.createDODDLERootWindow(viewMap);
        getContentPane().add(rootWindow, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                int messageType = JOptionPane.showConfirmDialog(rootWindow, getTitle() + "\nプロジェクトを終了しますか？");
                if (messageType == JOptionPane.YES_OPTION) {
                    projectMenu.remove(projectMenuItem);
                    dispose();
                }
            }
        });
        setSize(800, 600);
    }

    public void setXGALayout() {
        if (visualizationPanelView != null) {
            rootWindow.setWindow(new TabWindow(new DockingWindow[]{views[0],views[1],views[2],views[3],views[4],views[5],views[6], visualizationPanelView }));            
        } else {
            rootWindow.setWindow(new TabWindow(new DockingWindow[]{views[0],views[1],views[2],views[3],views[4],views[5],views[6] }));
        }
        views[0].restoreFocus();
    }
    
    public void setXGALayoutForAll() {
        setXGALayout();
        ontSelectionPanel.setXGALayout();
        docSelectionPanel.setXGALayout();
        inputWordSelectinPanel.setXGALayout();
        disambiguationPanel.setXGALayout();
        constructClassPanel.setXGALayout();
        constructPropertyPanel.setXGALayout();
        conceptDefinitionPanel.setXGALayout();    
    }
    
    public void setUXGALayoutForAll() {
        setXGALayout();
        ontSelectionPanel.setUXGALayout();
        docSelectionPanel.setXGALayout();
        inputWordSelectinPanel.setUXGALayout();
        disambiguationPanel.setUXGALayout();
        constructClassPanel.setUXGALayout();
        constructPropertyPanel.setUXGALayout();
        conceptDefinitionPanel.setUXGALayout();        
    }
    
    public void setProjectName(String name) {
        projectMenuItem.setText(name);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == projectMenuItem) {
            for (int i = 0; i < projectMenu.getItemCount(); i++) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) projectMenu.getItem(i);
                item.setSelected(false);
            }
            projectMenuItem.setSelected(true);
            toFront();
            try {
                setSelected(true);
            } catch (PropertyVetoException pve) {
                pve.printStackTrace();
            }
        }
    }

    public void resetURIConceptMap() {
        uriConceptMap.clear();
    }

    public Set getAllConcept() {
        return uriConceptMap.keySet();
    }

    public void putConcept(String uri, Concept c) {
        uriConceptMap.put(uri, c);
    }

    public Concept getConcept(String uri) {
        return uriConceptMap.get(uri);
    }

    public void initUserIDCount() {
        userIDCount = 0;
    }

    public int getUserIDCount() {
        return userIDCount;
    }

    public String getUserIDStr() {
        return "UID" + Integer.toString(userIDCount++);
    }

    public void setUserIDCount(int id) {
        if (userIDCount < id) {
            userIDCount = id;
        }
    }

    public OntologySelectionPanel getOntologySelectionPanel() {
        return ontSelectionPanel;
    }

    public DocumentSelectionPanel getDocumentSelectionPanel() {
        return docSelectionPanel;
    }

    public InputWordSelectionPanel getInputWordSelectionPanel() {
        return inputWordSelectinPanel;
    }

    public DisambiguationPanel getDisambiguationPanel() {
        return disambiguationPanel;
    }

    public InputWordModel makeInputWordModel(String iw, Map<String, Set<Concept>> wcSetMap) {
        return disambiguationPanel.makeInputWordModel(iw, wcSetMap);
    }

    public ConstructPropertyPanel getConstructPropertyPanel() {
        return constructPropertyPanel;
    }

    public ConstructClassPanel getConstructClassPanel() {
        return constructClassPanel;
    }

    public ConceptDefinitionPanel getConceptDefinitionPanel() {
        return conceptDefinitionPanel;
    }

    public void setSelectedIndex(int i) {
        views[i].restoreFocus();
    }

    public boolean isPerfectMatchedAmbiguityCntCheckBox() {
        return disambiguationPanel.isPerfectMatchedAmbiguityCntCheckBox();
    }

    public boolean isPerfectMatchedSystemAddedWordCheckBox() {
        return disambiguationPanel.isPerfectMatchedSystemAddedWordCheckBox();
    }

    public boolean isPartialMatchedAmbiguityCntCheckBox() {
        return disambiguationPanel.isPartialMatchedAmbiguityCntCheckBox();
    }

    public boolean isPartialMatchedComplexWordCheckBox() {
        return disambiguationPanel.isPartialMatchedComplexWordCheckBox();
    }

    public boolean isPartialMatchedMatchedWordBox() {
        return disambiguationPanel.isPartialMatchedMatchedWordBox();
    }

}
