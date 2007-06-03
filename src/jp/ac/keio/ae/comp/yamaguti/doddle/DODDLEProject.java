package jp.ac.keio.ae.comp.yamaguti.doddle;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.List;

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

    private boolean isInitialized;
    
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
    private List<String> logList;
    private UndoManager undoManager;

    private JCheckBoxMenuItem projectMenuItem;

    private View visualizationPanelView;

    class NewProjectWorker extends SwingWorker implements PropertyChangeListener {

        private int currentTaskCnt;
        private DODDLEProject project;

        public NewProjectWorker(int taskCnt, DODDLEProject project) {            
            currentTaskCnt = 1;
            this.project = project;
            addPropertyChangeListener(this);
            DODDLE.STATUS_BAR.setLastMessage(Translator.getTerm("NewProjectAction"));
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(taskCnt);
            DODDLE.STATUS_BAR.lock();
        }

        @Override
        protected Object doInBackground() {
            try {                
                projectMenuItem = new JCheckBoxMenuItem(title);
                projectMenuItem.addActionListener(project);                
                undoManager = new UndoManager(project);
                
                userIDCount = 0;
                uriConceptMap = new HashMap<String, Concept>();
                logList = new ArrayList<String>();
                addLog("NewProjectAction");
                constructClassPanel = new ConstructClassPanel(project);
                setProgress(currentTaskCnt++);
                ontSelectionPanel = new OntologySelectionPanel();
                setProgress(currentTaskCnt++);
                constructPropertyPanel = new ConstructPropertyPanel(project);
                setProgress(currentTaskCnt++);
                disambiguationPanel = new DisambiguationPanel(constructClassPanel, constructPropertyPanel, project);
                setProgress(currentTaskCnt++);
                inputWordSelectinPanel = new InputWordSelectionPanel(disambiguationPanel);
                setProgress(currentTaskCnt++);
                docSelectionPanel = new DocumentSelectionPanel(inputWordSelectinPanel, project);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel = new ConceptDefinitionPanel(project);
                setProgress(currentTaskCnt++);
                if (DODDLE.getDODDLEPlugin() != null) {
                    visualizationPanel = new VisualizationPanel(project);
                }
                setProgress(currentTaskCnt++);
                disambiguationPanel.setDocumentSelectionPanel(docSelectionPanel);
                
                views = new View[7];
                ViewMap viewMap = new ViewMap();

                views[0] = new View(Translator.getTerm("DocumentSelectionPanel"), Utils.getImageIcon("open_doc.gif"),
                        docSelectionPanel);
                views[1] = new View(Translator.getTerm("InputWordSelectionPanel"), Utils
                        .getImageIcon("input_words.png"), inputWordSelectinPanel);
                views[2] = new View(Translator.getTerm("OntologySelectionPanel"), Utils.getImageIcon("ontology.png"),
                        ontSelectionPanel);
                views[3] = new View(Translator.getTerm("DisambiguationPanel"),
                        Utils.getImageIcon("disambiguation.png"), disambiguationPanel);
                views[4] = new View(Translator.getTerm("ClassTreeConstructionPanel"), Utils
                        .getImageIcon("class_tree.png"), constructClassPanel);
                views[5] = new View(Translator.getTerm("PropertyTreeConstructionPanel"), Utils
                        .getImageIcon("property_tree.png"), constructPropertyPanel);
                views[6] = new View(Translator.getTerm("ConceptDefinitionPanel"), Utils
                        .getImageIcon("non-taxonomic.png"), conceptDefinitionPanel);

                for (int i = 0; i < views.length; i++) {
                    viewMap.addView(i, views[i]);
                }
                if (DODDLE.getDODDLEPlugin() != null) {
                    visualizationPanelView = new View(Translator.getTerm("VisualizationPanel"), Utils
                            .getImageIcon("mr3_logo.png"), visualizationPanel);
                    viewMap.addView(7, visualizationPanelView);
                }

                rootWindow = Utils.createDODDLERootWindow(viewMap);
                getContentPane().add(rootWindow, BorderLayout.CENTER);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                addInternalFrameListener(new InternalFrameAdapter() {
                    public void internalFrameClosing(InternalFrameEvent e) {
                        int messageType = JOptionPane.showConfirmDialog(rootWindow, getTitle() + "\n"+Translator.getTerm("ExitProjectMessage"));
                        if (messageType == JOptionPane.YES_OPTION) { 
                            DODDLE.removeProjectMenuItem(projectMenuItem);
                            dispose();
                        }
                    }
                });
                setSize(800, 600);
                setProgress(currentTaskCnt++);                

                DODDLE.desktop.add(project);
                project.toFront();
                DODDLE.desktop.setSelectedFrame(project);                
                DODDLE.addProjectMenuItem(projectMenuItem);                

                setXGALayoutForAll();
                setProgress(currentTaskCnt++);                                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {                
                project.setVisible(true); //かならず表示させるため
                try {
                    project.setMaximum(true); // setVisibleより前にしてしまうと，初期サイズ(800x600)で最大化されてしまう
                } catch(PropertyVetoException pve) {
                    pve.printStackTrace();
                }
                DODDLE.STATUS_BAR.unLock();
                DODDLE.STATUS_BAR.hideProgressBar();
                ToolTipManager.sharedInstance().setEnabled(true);
                isInitialized = true;
            }
            return "done";
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE.STATUS_BAR.setValue(currentTaskCnt);
            }
        }
    }

    private String getTerm(String msg) {
        String term = Translator.getTerm(msg);
        if (term == null) {
            term = msg;
        }
        return term;
    }
    
    public void addLog(String msg) {
        String log = Calendar.getInstance().getTime()+ ": "+getTerm(msg);
        logList.add(log);
    }
    
    public void addLog(String msg, Object option) {
        String log = Calendar.getInstance().getTime()+ ": "+getTerm(msg)+": "+option;
        logList.add(log);
    }
    
    public void saveLog(File file) {
        if (logList == null) { return; }
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            for (String log: logList) {
                writer.write(log);
                writer.write("\n");
            }
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
    
    public void loadLog(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            while (reader.ready()) {
                String line = reader.readLine();
                logList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }

    public DODDLEProject(String title) {
        super(title, true, true, true, true);        
        NewProjectWorker worker = new NewProjectWorker(9, this);
        worker.execute();
    }

    public void initUndoManager() {
        undoManager.initUndoManager();
    }
    
    public void addCommand(Concept parentConcept, Concept targetConcept, String treeType) {
        undoManager.addCommand(parentConcept, targetConcept, treeType);
    }
    
    public void undo() {
        undoManager.undo();
    }
    
    public void redo() {
        undoManager.redo();
    }
    
    public boolean canUndo() {
        return undoManager.canUndo();
    }
    
    public boolean canRedo() {
        return undoManager.canRedo();  
    }
    
    
    public JMenuItem getProjectMenuItem() {
        return projectMenuItem;
    }

    public void setXGALayout() {
        if (visualizationPanelView != null) {
            rootWindow.setWindow(new TabWindow(new DockingWindow[] { views[0], views[1], views[2], views[3], views[4],
                    views[5], views[6], visualizationPanelView}));
        } else {
            rootWindow.setWindow(new TabWindow(new DockingWindow[] { views[0], views[1], views[2], views[3], views[4],
                    views[5], views[6]}));
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
            JMenu projectMenu = DODDLE.projectMenu;
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

    public boolean isPerfectlyMatchedAmbiguityCntCheckBox() {
        return disambiguationPanel.isPerfectlyMatchedAmbiguityCntCheckBox();
    }

    public boolean isPerfectlyMatchedSystemAddedWordCheckBox() {
        return disambiguationPanel.isPerfectlyMatchedSystemAddedWordCheckBox();
    }

    public boolean isPartiallyMatchedAmbiguityCntCheckBox() {
        return disambiguationPanel.isPartiallyMatchedAmbiguityCntCheckBox();
    }

    public boolean isPartiallyMatchedComplexWordCheckBox() {
        return disambiguationPanel.isPartiallyMatchedComplexWordCheckBox();
    }

    public boolean isPartiallyMatchedMatchedWordBox() {
        return disambiguationPanel.isPartiallyMatchedMatchedWordBox();
    }

}
