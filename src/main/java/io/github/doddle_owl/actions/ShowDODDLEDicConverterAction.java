package io.github.doddle_owl.actions;

import io.github.doddle_owl.utils.Utils;
import io.github.doddle_owl.views.DODDLEDicConverterUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowDODDLEDicConverterAction extends AbstractAction {

    private final String title;

    public ShowDODDLEDicConverterAction(String title) {
        super(title, Utils.getImageIcon("baseline_build_black_18dp.png"));
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEDicConverterUI converter = new DODDLEDicConverterUI();
        converter.setVisible(true);
    }
}
