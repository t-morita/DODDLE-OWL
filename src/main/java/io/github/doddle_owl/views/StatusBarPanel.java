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

import io.github.doddle_owl.DODDLE_OWL;
import io.github.doddle_owl.utils.Translator;
import io.github.doddle_owl.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

/**
 * @author Takeshi Morita
 */
public class StatusBarPanel extends Panel implements ActionListener {

    private boolean isLock;

    private long startTime;
    private int elapsedTime;

    private int maxValue;
    private int currentValue;

    private final JTextField statusField;
    private final JButton cancelButton;
    private final JProgressBar progressBar;
    private static final Color STATUS_BAR_COLOR = new Color(240, 240, 240);

    private Thread timer;

    public StatusBarPanel() {
        cancelButton = new JButton(Translator.getTerm("CancelButton"), Utils.getImageIcon("baseline_cancel_black_18dp.png"));
        cancelButton.addActionListener(this);
        cancelButton.setVisible(false);
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        progressBar.setStringPainted(false);
        progressBar.setVisible(false);
        statusField = new JTextField();
        statusField.setBackground(STATUS_BAR_COLOR);
        statusField.setEditable(false);
        JPanel progressBarPanel = new JPanel();
        progressBarPanel.setLayout(new BorderLayout());
        progressBarPanel.add(progressBar, BorderLayout.CENTER);
        progressBarPanel.add(cancelButton, BorderLayout.EAST);
        setLayout(new BorderLayout());
        add(statusField, BorderLayout.CENTER);
        add(progressBarPanel, BorderLayout.EAST);
    }

    private SwingWorker<String, String> worker;

    public void setSwingWorker(SwingWorker<String, String> w) {
        worker = w;
    }

    public void actionPerformed(ActionEvent e) {
        if (worker != null) {
            worker.cancel(true);
            DODDLE_OWL.STATUS_BAR.unLock();
            DODDLE_OWL.STATUS_BAR.hideProgressBar();
        }
    }

    private String lastMessage;

    public void setLastMessage(String msg) {
        lastMessage = msg;
    }

    public void printMessage(String msg) {
        lastMessage = msg;
        setCurrentTime();
    }

    private void startTimer() {
        timer = new Thread(() -> {
            while (progressBar.isVisible()) {
                try {
                    Thread.sleep(1000);
                    setCurrentTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DODDLE_OWL.getLogger().info(Translator.getTerm("TotalTimeMessage") + ": " + elapsedTime);
            setValue(lastMessage);
        });
        timer.start();
    }

    public void setCurrentTime() {
        elapsedTime = (int) (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
        statusField.setText(lastMessage + ": " + Translator.getTerm("ElapsedTimeMessage") + ": " + elapsedTime);
    }

    public void lock() {
        isLock = true;
    }

    public void unLock() {
        isLock = false;
    }

    public void startTime() {
        if (!isLock) {
            startTime = Calendar.getInstance().getTimeInMillis();
            setCurrentTime();
        }
    }

    public double getProgressTime() {
        return elapsedTime;
    }

    public void initNormal(int max) {
        if (!isLock) {
            maxValue = max;
            progressBar.setIndeterminate(false);
            progressBar.setMinimum(0);
            progressBar.setMaximum(maxValue);
            progressBar.setValue(0);
            currentValue = 0;
            progressBar.setVisible(true);
            cancelButton.setVisible(true);
            startTimer();
        }
    }

    public void initIndeterminate() {
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        cancelButton.setVisible(true);
    }

    public void addValue(String message) {
        currentValue++;
        if (maxValue < 10) {
            setValue(message);
        } else if (currentValue % (maxValue / 10) == 0) {
            setValue(message);
        }
    }

    public void addProjectValue() {
        currentValue++;
        if (maxValue < 20) {
            setValue();
        } else if (currentValue % (maxValue / 10) == 0) {
            setValue();
        }
    }

    public void addValue() {
        if (!isLock) {
            addProjectValue();
        }
    }

    public void setValue(String message) {
        statusField.setText(message);
        progressBar.setValue(currentValue);
        progressBar.paintImmediately(progressBar.getVisibleRect());
    }

    private void setValue() {
        progressBar.setValue(currentValue);
    }

    public void setValue(int value) {
        progressBar.setValue(value);
    }

    public int getValue() {
        return progressBar.getValue();
    }

    public void setMaximum(int max) {
        progressBar.setMaximum(max);
    }

    public void setMinimum(int min) {
        progressBar.setMinimum(min);
    }

    public void hideProgressBar() {
        if (!isLock) {
            worker = null;
            progressBar.setVisible(false);
            cancelButton.setVisible(false);
            timer = null;
        }
    }

    public void setText(String text) {
        statusField.setText(text);
    }

    public String getText() {
        return statusField.getText();
    }
}
