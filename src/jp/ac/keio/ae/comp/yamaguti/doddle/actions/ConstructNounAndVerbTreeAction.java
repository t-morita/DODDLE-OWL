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
public class ConstructNounAndVerbTreeAction extends AbstractAction {

    public ConstructNounAndVerbTreeAction() {
        super(Translator.getString("DisambiguationPanel.ConstructClassesAndProperties"));
    }

    public void actionPerformed(ActionEvent e) {
        new ConstructTreeAction(true, DODDLE.getCurrentProject()).constructTree();
    }
}
