/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class NewProjectAction extends AbstractAction {

    private String title;

    public NewProjectAction(String title) {
        super(title, Utils.getImageIcon("new.gif"));
        this.title = title;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    }

    public String getTitle() {
        return title;
    }

    public void actionPerformed(ActionEvent e) {
        DODDLE.STATUS_BAR.setLastMessage(Translator.getTerm("NewProjectAction"));
        DODDLE.STATUS_BAR.startTime();
        DODDLE.STATUS_BAR.initNormal(12);
        DODDLE.STATUS_BAR.lock();
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            public String doInBackground() {
                DODDLE.newProject();
                return "done";
            }
        };
        DODDLE.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
