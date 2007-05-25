/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class LoadInputWordSetAction extends AbstractAction {

    public LoadInputWordSetAction(String title) {
        super(title);
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProject currentProject = DODDLE.getCurrentProject();
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();

        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            disambiguationPanel.loadInputWordSet(chooser.getSelectedFile());
            DODDLE.setSelectedIndex(DODDLEConstants.DISAMBIGUATION_PANEL);
        }
    }
}

