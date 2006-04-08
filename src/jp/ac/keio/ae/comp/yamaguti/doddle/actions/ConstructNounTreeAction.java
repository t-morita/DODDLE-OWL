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
public class ConstructNounTreeAction extends AbstractAction {

    public ConstructNounTreeAction() {
        super(Translator.getString("DisambiguationPanel.ConstructClasses"));
    }

    public void actionPerformed(ActionEvent e) {
        new ConstructTreeAction(false, DODDLE.getCurrentProject()).constructTree();
    }
}
