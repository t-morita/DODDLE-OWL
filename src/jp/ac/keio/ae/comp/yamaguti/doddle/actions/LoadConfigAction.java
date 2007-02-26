/*
 * @(#)  2007/02/12
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class LoadConfigAction extends AbstractAction {
    private OptionDialog optionDialog;
    
    public LoadConfigAction(String title, OptionDialog dialog) {
        super(title);
        optionDialog = dialog;
    }
    
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        optionDialog.loadConfig(chooser.getSelectedFile());
    }
}
