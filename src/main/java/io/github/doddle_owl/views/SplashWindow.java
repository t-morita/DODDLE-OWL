/*
 * Project Name: DODDLE-OWL (a Domain Ontology rapiD DeveLopment Environment - OWL extension)
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

package io.github.doddle_owl.views;

import io.github.doddle_owl.models.common.DODDLEConstants;
import io.github.doddle_owl.utils.Utils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
 * @author Takeshi Morita
 */
public class SplashWindow extends JWindow implements HyperlinkListener {

    public SplashWindow(Frame rootFrame) {
        ImageIcon logo = Utils.getImageIcon("doddle_splash.png");
        JLabel logoLabel = new JLabel(logo);
        JEditorPane editor = new JEditorPane("text/html", "");
        editor.addHyperlinkListener(this);
        editor.setEditable(false);
        String builder = "<font face=TimesNewRoman>" +
                "Name:  DODDLE-OWL  <br>" +
                "Version: " + DODDLEConstants.VERSION + "<br>" +
                "Copyright (C) 2004-2024 Takeshi Morita.<br>" +
                "Contact: morita@it.aoyama.ac.jp<br>" +
                "License: GPL<br>" +
                "Project Website: <a href=https://doddle-owl.github.io/>https://doddle-owl.github.io/</a>" +
                "</font>";
        editor.setText(builder);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.add(logoLabel, BorderLayout.CENTER);
        panel.add(editor, BorderLayout.EAST);
        getContentPane().add(panel);
        editor.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        pack();
        setLocationRelativeTo(rootFrame);
        setVisible(true);
    }

    public void hyperlinkUpdate(HyperlinkEvent ae) {
        try {
            if (ae.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                Desktop.getDesktop().browse(new URI("https://doddle-owl.github.io/"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SplashWindow(null);
    }
}
