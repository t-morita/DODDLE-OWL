/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class LoadIDTypicalWordAction extends AbstractAction {

    private DODDLE doddle;
    
    public LoadIDTypicalWordAction(String title, DODDLE ddl) {
        super(title);
        doddle = ddl;
    }

    public void loadIDTypicalWord(DODDLEProject currentProject, File file) {
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        if (!file.exists()) { return; }
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            Map<String, String> idTypicalWordMap = new HashMap<String, String>();
            while (reader.ready()) {
                String line = reader.readLine();
                String[] idInputWord = line.replaceAll("\n", "").split("\t");
                if (idInputWord.length == 2) {
                    idTypicalWordMap.put(idInputWord[0], idInputWord[1]);
                }
            }
            constructClassPanel.loadIDTypicalWord(idTypicalWordMap);
            constructPropertyPanel.loadIDTypicalWord(idTypicalWordMap);
            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
        
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_DIR);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        DODDLEProject currentProject = DODDLE.getCurrentProject();
        loadIDTypicalWord(currentProject, chooser.getSelectedFile());
    }
}

