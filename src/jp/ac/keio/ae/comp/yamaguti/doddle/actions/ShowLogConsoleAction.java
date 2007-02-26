/*
 * @(#)  2007/02/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class ShowLogConsoleAction extends AbstractAction {

    private LogConsole logConsole;

    public ShowLogConsoleAction(String title, LogConsole console) {
        super(title);
        logConsole = console;
    }

    public void actionPerformed(ActionEvent e) {
        logConsole.setVisible(true);
    }
}
