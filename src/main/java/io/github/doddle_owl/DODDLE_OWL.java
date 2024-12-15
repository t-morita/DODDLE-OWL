/*
 * Project Name: DODDLE-OWL (a Domain Ontology rapiD DeveLopment Environment - OWL extension)
 *
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

package io.github.doddle_owl;

import com.formdev.flatlaf.FlatLightLaf;
import io.github.doddle_owl.actions.*;
import io.github.doddle_owl.views.*;
import org.apache.commons.cli.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.models.common.ProjectFileNames;
import io.github.doddle_owl.models.concept_selection.InputModule;
import io.github.doddle_owl.models.ontology_api.JWO;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.UpperConceptManager;
import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.document_selection.DocumentSelectionPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
public class DODDLE_OWL extends JFrame {

    private static final Logger logger = Logger.getLogger("DODDLE-OWL");
    private final OptionDialog optionDialog;
    private final LogConsole logConsole;

    public static Frame rootFrame;
    public static JRootPane rootPane;
    public static DODDLEProjectPanel doddleProjectPanel;
    private static JMenu recentProjectMenu;
    public static StatusBarPanel STATUS_BAR;
    public static Set<String> GENERAL_ONTOLOGY_NAMESPACE_SET;

    private NewProjectAction newProjectAction;
    private OpenProjectAction openProjectAction;
    private SaveProjectAction saveProjectAction;
    private static SaveProjectAsAction saveProjectAsAction;
    private LoadDescriptionsAction loadDescriptionAction;
    private LoadConceptPreferentialTermAction loadConceptDisplayTermAction;
    private SaveConceptPreferentialTermAction saveConceptDisplayTermAction;
    private LoadOntologyAction loadOWLOntologyAction;
    private LoadOntologyAction loadFreeMindOntologyAction;
    private SaveOntologyAction saveOWLOntologyAction;
    private SaveOntologyAction saveFreeMindOntologyAction;
    private ShowLogConsoleAction showLogConsoleAction;
    private ShowOptionDialogAction showOptionDialogAction;
    private ShowVersionInfoAction showVersionInfoAction;
    private ShowManualAction showManualAction;

    private ShowDODDLEDicConverterAction showDODDLEDicConverterAction;

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 768;

    @Override
    public Image getIconImage() {
        return super.getIconImage();
    }

    public static Property HASA_PROPERTY;

    public DODDLE_OWL() {
        rootPane = getRootPane();
        rootFrame = this;
        optionDialog = new OptionDialog(this);
        setFileLogger();
        logConsole = new LogConsole(this, Translator.getTerm("LogConsoleDialog"), null);
        STATUS_BAR = new StatusBarPanel();
        GENERAL_ONTOLOGY_NAMESPACE_SET = new HashSet<>();
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.EDR_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.EDRT_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.WN_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.JPN_WN_URI);
        HASA_PROPERTY = ResourceFactory.createProperty(DODDLEConstants.BASE_URI + "partOf");

        Container contentPane = getContentPane();
        makeActions();
        makeMenuBar();
        contentPane.add(getToolBar(), BorderLayout.NORTH);
        doddleProjectPanel = new DODDLEProjectPanel();
        contentPane.add(doddleProjectPanel, BorderLayout.CENTER);
        contentPane.add(STATUS_BAR, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if (Desktop.isDesktopSupported()) {
            var desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler((e, response) -> exit());
            }
        }
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setTitle(Translator.getTerm("ApplicationName") + " - " + Translator.getTerm("VersionMenu") + ": "
                + DODDLEConstants.VERSION + " - " + Translator.getTerm("NewProjectAction"));
        setVisible(true);
    }

    public OptionDialog getOptionDialog() {
        return optionDialog;
    }

    public static SaveProjectAsAction getSaveProjectAsAction() {
        return saveProjectAsAction;
    }

    public void exit() {
        int messageType = JOptionPane.showConfirmDialog(rootPane, Translator.getDescription("QuitAction"),
                Translator.getTerm("QuitAction"), JOptionPane.YES_NO_OPTION);
        if (messageType == JOptionPane.YES_OPTION) {
            if (isExistingCurrentProject()) {
                getCurrentProject().getDocumentSelectionPanel().destroyProcesses();
                JWO.closeDataSet();
            }
            dispose();
            System.exit(0);
        }
    }

    public List<String> loadRecentProject() {
        List<String> recentProjects = new ArrayList<>();
        try {
            File recentProjectFile = new File(DODDLEConstants.PROJECT_HOME, ProjectFileNames.RECENT_PROJECTS_FILE);
            if (!recentProjectFile.exists()) {
                return recentProjects;
            }
            BufferedReader reader = Files.newBufferedReader(Paths.get(recentProjectFile.getAbsolutePath()),
                    StandardCharsets.UTF_8);
            try (reader) {
                while (reader.ready()) {
                    recentProjects.add(reader.readLine());
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return recentProjects;
    }

    public void saveRecentProject(List<String> recentProjects) {
        try {
            File recentProjectFile = new File(DODDLEConstants.PROJECT_HOME, ProjectFileNames.RECENT_PROJECTS_FILE);
            if (!recentProjectFile.exists()) {
                return;
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recentProjectFile), StandardCharsets.UTF_8));
            try (writer) {
                int cnt = 0;
                DODDLE_OWL.recentProjectMenu.removeAll();
                for (String project : recentProjects) {
                    if (cnt == 10) {
                        break;
                    }
                    writer.write(project);
                    writer.newLine();
                    JMenuItem item = new JMenuItem(project);
                    item.addActionListener(new OpenRecentProjectAction(project, this));
                    DODDLE_OWL.recentProjectMenu.add(item);
                    cnt++;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void makeActions() {
        newProjectAction = new NewProjectAction(Translator.getTerm("NewProjectAction"));
        openProjectAction = new OpenProjectAction(Translator.getTerm("OpenProjectAction"), this);
        saveProjectAction = new SaveProjectAction(Translator.getTerm("SaveProjectAction"), this);
        saveProjectAsAction = new SaveProjectAsAction(Translator.getTerm("SaveAsProjectAction"), this);
        loadDescriptionAction = new LoadDescriptionsAction(Translator.getTerm("OpenDescriptionsAction"));
        loadConceptDisplayTermAction = new LoadConceptPreferentialTermAction(
                Translator.getTerm("OpenConceptPreferentialTermMapAction"));
        saveConceptDisplayTermAction = new SaveConceptPreferentialTermAction(
                Translator.getTerm("SaveConceptPreferentialTermMapAction"));
        saveOWLOntologyAction = new SaveOntologyAction(Translator.getTerm("SaveOWLOntologyAction"),
                SaveOntologyAction.OWL_ONTOLOGY);
        saveFreeMindOntologyAction = new SaveOntologyAction(Translator.getTerm("SaveFreeMindOntologyAction"),
                SaveOntologyAction.FREEMIND_ONTOLOGY);
        loadOWLOntologyAction = new LoadOntologyAction(Translator.getTerm("OpenOWLOntologyAction"),
                LoadOntologyAction.OWL_ONTOLOGY);
        loadFreeMindOntologyAction = new LoadOntologyAction(Translator.getTerm("OpenFreeMindOntologyAction"),
                LoadOntologyAction.FREEMIND_ONTOLOGY);
        showLogConsoleAction = new ShowLogConsoleAction(Translator.getTerm("ShowLogConsoleAction"), logConsole);
        showOptionDialogAction = new ShowOptionDialogAction(Translator.getTerm("ShowOptionDialogAction"), this);
        showVersionInfoAction = new ShowVersionInfoAction(this);
        showManualAction = new ShowManualAction(Translator.getTerm("ShowManualAction"));
        showDODDLEDicConverterAction = new ShowDODDLEDicConverterAction("DODDLE Dic Converter");
    }

    private void makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Translator.getTerm("FileMenu"));
        fileMenu.add(newProjectAction);
        fileMenu.addSeparator();
        fileMenu.add(openProjectAction);
        recentProjectMenu = new JMenu(Translator.getTerm("OpenRecentProjectsMenu"));
        List<String> recentProjects = loadRecentProject();
        saveRecentProject(recentProjects);
        fileMenu.add(recentProjectMenu);
        JMenu loadMenu = new JMenu(Translator.getTerm("OpenMenu"));
        loadMenu.add(new LoadInputTermSetAction(Translator.getTerm("OpenInputTermListAction")));
        loadMenu.add(new LoadTermInfoTableAction(Translator.getTerm("OpenInputTermTableAction")));
        loadMenu.add(loadDescriptionAction);
        loadMenu.add(new LoadTermEvalConceptSetAction(Translator.getTerm("OpenInputConceptSelectionResultAction")));
        loadMenu.add(new LoadTermConceptMapAction(Translator.getTerm("OpenInputTermConceptMapAction")));
        loadMenu.add(loadOWLOntologyAction);
        loadMenu.add(loadFreeMindOntologyAction);
        loadMenu.add(loadConceptDisplayTermAction);
        fileMenu.add(loadMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveProjectAction);
        fileMenu.add(saveProjectAsAction);
        JMenu saveMenu = new JMenu(Translator.getTerm("SaveMenu"));
        // saveMenu.add(new SaveMatchedWordList("辞書に載っていた入力語彙を保存"));
        saveMenu.add(new SaveInputTermSetAction(Translator.getTerm("SaveInputTermListAction")));
        saveMenu.add(new SaveTermInfoTableAction(Translator.getTerm("SaveInputTermTableAction")));
        saveMenu.add(new SaveTermEvalConceptSetAction(Translator.getTerm("SaveInputConceptSelectionResultAction")));
        saveMenu.add(new SaveTermConceptMapAction(Translator.getTerm("SaveInputTermConceptMapAction")));
        saveMenu.add(new SavePerfectlyMatchedTermAction(Translator.getTerm("SaveExactMatchTermListAction")));
        saveMenu.add(new SavePerfectlyMatchedTermWithCompoundWordAction(Translator
                .getTerm("SaveExactMatchTermCompoundWordMapAction")));
        saveMenu.add(saveOWLOntologyAction);
        saveMenu.add(saveFreeMindOntologyAction);
        saveMenu.add(saveConceptDisplayTermAction);
        fileMenu.add(saveMenu);
        fileMenu.addSeparator();
        fileMenu.add(new ExitAction(Translator.getTerm("QuitAction"), this));
        menuBar.add(fileMenu);

        JMenu toolMenu = new JMenu(Translator.getTerm("ToolMenu"));
        toolMenu.add(new ShowAllTermAction(Translator.getTerm("ShowAllTermAction")));
        toolMenu.addSeparator();
        toolMenu.add(new AutomaticDisAmbiguationAction(Translator.getTerm("AutomaticInputConceptSelectionAction")));
        toolMenu.addSeparator();
        toolMenu.add(new ConstructNounTreeAction());
        toolMenu.add(new ConstructNounAndVerbTreeAction());
        toolMenu.addSeparator();
        toolMenu.add(showDODDLEDicConverterAction);
        toolMenu.addSeparator();
        toolMenu.add(showLogConsoleAction);
        toolMenu.addSeparator();
        toolMenu.add(showOptionDialogAction);
        menuBar.add(toolMenu);

        menuBar.add(getHelpMenu());
        setJMenuBar(menuBar);
    }

    private JMenu getHelpMenu() {
        JMenu menu = new JMenu(Translator.getTerm("HelpMenu"));
        menu.add(showVersionInfoAction);
        menu.add(showManualAction);
        return menu;
    }

    private JToolBar getToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(newProjectAction).setToolTipText(newProjectAction.getTitle());
        toolBar.add(openProjectAction).setToolTipText(openProjectAction.getTitle());
        toolBar.add(saveProjectAction).setToolTipText(saveProjectAction.getTitle());
        toolBar.add(saveProjectAsAction).setToolTipText(saveProjectAsAction.getTitle());
        toolBar.addSeparator();
        toolBar.add(showOptionDialogAction).setToolTipText(saveProjectAsAction.getTitle());
        return toolBar;
    }

    public static DODDLEProjectPanel getCurrentProject() {
        return doddleProjectPanel;
    }

    private static boolean isExistingCurrentProject() {
        return doddleProjectPanel != null;
    }

    public void loadConceptDisplayTerm(DODDLEProjectPanel currentProject, File file) {
        loadConceptDisplayTermAction.loadIDPreferentialTerm(currentProject, file);
    }

    public void saveConceptDisplayTerm(DODDLEProjectPanel currentProject, File file) {
        saveConceptDisplayTermAction.saveIDPreferentialTerm(currentProject, file);
    }

    public void saveOntology(DODDLEProjectPanel currentProject, File file) {
        saveOWLOntologyAction.saveOWLOntology(currentProject, file);
    }

    public void loadOntology(DODDLEProjectPanel currentProject, File file) {
        loadOWLOntologyAction.loadOWLOntology(currentProject, file);
    }

    public static void setSelectedIndex(int index) {
        doddleProjectPanel.setSelectedIndex(index);
    }

    public void loadBaseURI(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            try (reader) {
                DODDLEConstants.BASE_URI = reader.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void setPath(Properties properties) {
        DODDLEConstants.EDR_HOME = properties.getProperty("EDR_HOME");
        DODDLEConstants.EDRT_HOME = properties.getProperty("EDRT_HOME");
        DODDLEConstants.JWN_HOME = properties.getProperty("JWN_HOME");
        DODDLEConstants.JWO_HOME = properties.getProperty("JWO_HOME");
        DocumentSelectionPanel.PERL_EXE = properties.getProperty("PERL_EXE");
        DocumentSelectionPanel.Japanese_Morphological_Analyzer = properties
                .getProperty("Japanese_Morphological_Analyzer");
        DocumentSelectionPanel.Japanese_Dependency_Structure_Analyzer = properties
                .getProperty("Japanese_Dependency_Structure_Analyzer");
        DODDLEConstants.BASE_URI = properties.getProperty("BASE_URI");
        DODDLEConstants.BASE_PREFIX = properties.getProperty("BASE_PREFIX");
        DODDLEConstants.PROJECT_HOME = properties.getProperty("PROJECT_DIR");
        UpperConceptManager.UPPER_CONCEPT_LIST = properties.getProperty("UPPER_CONCEPT_LIST");
        DODDLEConstants.LANG = properties.getProperty("LANG");
    }

    private static void setPath() {
        try {
            Preferences userPrefs = Preferences.userNodeForPackage(DODDLE_OWL.class);
            String[] keys = userPrefs.keys();
            if (0 < keys.length) {
                Properties properties = new Properties();
                for (String key : keys) {
                    properties.put(key, userPrefs.get(key, ""));
                }
                setPath(properties);
            }
        } catch (BackingStoreException bse) {
            bse.printStackTrace();
        }
    }

    private static void setProgressValue() {
        InputModule.INIT_PROGRESS_VALUE = 887253;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setFileLogger() {
        try {
            Path logFilePath = Paths.get(DODDLEConstants.PROJECT_HOME + File.separator + "doddle_log.txt");
            if (Files.notExists(logFilePath)) {
                Files.createFile(logFilePath);
            }
            Handler fileHandler = new FileHandler(logFilePath.toString(), true);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void initOptions(String[] args) {
        Options options = new Options();
        options.addOption("g", "DEBUG", false, "");
        options.addOption("l", "LANG", true, "");
        options.addOption("s", "Swoolgle", false, "");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            setPath();
            setProgressValue();
            if (cmd.hasOption("g")) {
                getLogger().setLevel(Level.SEVERE);
                DODDLEConstants.DEBUG = true;
            }
            if (cmd.hasOption("l")) {
                DODDLEConstants.LANG = cmd.getOptionValue("l");
            }
            if (cmd.hasOption("s")) {
                DODDLEConstants.IS_INTEGRATING_SWOOGLE = true;
            }
            if (DODDLEConstants.LANG == null) {
                DODDLEConstants.LANG = "ja";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FlatLightLaf.install();
        SplashWindow splashWindow = new SplashWindow(null);
        DODDLE_OWL.initOptions(args);
        Translator.loadDODDLEComponentOntology(DODDLEConstants.LANG);
        try {
            ToolTipManager.sharedInstance().setEnabled(true);
            UIManager.put("TitledBorder.border", new LineBorder(new Color(200, 200, 200), 1));
            if (Taskbar.isTaskbarSupported()) {
                var taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(Utils.getImageIcon("doddle_splash.png").getImage());
                }
            }
            new DODDLE_OWL();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.setVisible(false);
        }
    }
}