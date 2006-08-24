/*
 * @(#)  2005/09/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class SplashWindow extends JWindow {

    public SplashWindow() {
        ImageIcon logo = Utils.getImageIcon("doddle_splash.png");
        JLabel logoLabel = new JLabel(logo);
        getContentPane().add(logoLabel, BorderLayout.CENTER);
        JLabel versionInfoLabel = new JLabel("β Version: " + DODDLE.VERSION);
        versionInfoLabel.setFont(versionInfoLabel.getFont().deriveFont(Font.PLAIN, 14));
        JLabel copyRightLabel = new JLabel("Copyright (C) 2004-2006 MMM Project");
        copyRightLabel.setFont(versionInfoLabel.getFont().deriveFont(Font.PLAIN, 14));
        JLabel urlLabel = new JLabel("http://mmm.semanticweb.org/doddle/");
        urlLabel.setFont(versionInfoLabel.getFont().deriveFont(Font.PLAIN, 14));

        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        infoPanel.setLayout(new GridLayout(3, 1));
        infoPanel.add(versionInfoLabel);
        infoPanel.add(copyRightLabel);
        infoPanel.add(urlLabel);
        getContentPane().add(infoPanel, BorderLayout.SOUTH);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation(screenSize.width / 2 - (frameSize.width / 2), screenSize.height / 2 - (frameSize.height / 2));

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        setVisible(true);
    }
    public static void main(String[] args) {
        new SplashWindow();
    }
}
