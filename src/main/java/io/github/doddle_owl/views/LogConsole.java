/*
 * @(#)GPLogConsole.java 1.0 29.01.2003
 *
 * Copyright (C) 2003 luzar
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package io.github.doddle_owl.views;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import io.github.doddle_owl.models.common.DODDLEConstants;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * Shows the System.in and System.out in a nice JFrame.
 * <p>
 * The Frame looks like this: <br>
 * <img src="doc-files/GPLogConsole.jpg">
 *
 * @author Thomas Genssler (FZI)
 * @author Sven Luzar
 * <p>
 * modified GPLogConsole -> LogConsole
 */
public class LogConsole extends JDialog {

    /**
     * The PrintStream for the System.err
     */
    private PrintStream stderr = null;

    /**
     * Title of the Frame
     */
    private String frameTitle;

    /**
     * Card Layout for the Window
     */
    private final CardLayout cardLayout = new CardLayout();
    /**
     * Text area for the System.err output
     */
    private final JTextArea stderrText = new JTextArea();
    /**
     * ScrollPane for the System.out text area
     */
    private final JScrollPane stdoutScrollPane = new JScrollPane();
    /**
     * Text area for the System.out output
     */
    private final JTextArea stdoutText = new JTextArea();
    /**
     * ScrollPane for the System.err text area
     */
    private final JScrollPane stderrScrollPane = new JScrollPane();
    /**
     * Tabbed pane for the System.out and System.err text area
     */
    private final JTabbedPane jTabbedPane1 = new JTabbedPane();
    /**
     * PopUpMenu for save and clear the output textareas
     */
    private final InternalPopupMenu popup = new InternalPopupMenu();

    /**
     * creates an instance
     */
    public LogConsole(Frame rootFrame, String title, Image icon) {
        super(rootFrame);
        frameTitle = title;
        /**
         * Icon for the Window
         */
        Image myIcon = icon;

        if ((frameTitle == null) || (frameTitle.equals(""))) {
            frameTitle = "Test drive";
        }

        this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setLocationRelativeTo(rootFrame);
    }

    /**
     * Overriden, in order to be able to deal with window events
     */
    protected void processWindowEvent(WindowEvent e) {
        //
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            // only close the window when we are not in embedded mode
            // release resources and exit if we are not running embedded,
            // buttonImage.buttonEdge., as
            // part of another application
            // super.processWindowEvent(buttonEdge);
            this.dispose();
        }
    }

    /**
     * Initialises the Swing components
     */
    private void jbInit() {
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });
        this.setTitle(frameTitle);
        this.getContentPane().setLayout(cardLayout);
        // if (myIcon != null) {
        // this.setIconImage(myIcon);
        // }

        // re-direct stderr and stdout
        redirect();

        stderrText.setForeground(Color.red);
        stderrText.setBackground(Color.white);
        stderrText.setEditable(false);
        stderrText.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                stderrText_mouseClicked(e);
            }
        });
        stdoutText.setForeground(Color.black);
        stdoutText.setEditable(false);
        stdoutText.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                stdoutText_mouseClicked(e);
            }
        });
        jTabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
        jTabbedPane1.setMinimumSize(new Dimension(400, 400));
        jTabbedPane1.setPreferredSize(new Dimension(400, 400));
        this.getContentPane().add(jTabbedPane1, "jTabbedPane1");
        jTabbedPane1.add(stdoutScrollPane, "Standard out");

        jTabbedPane1.add(stderrScrollPane, "Standard error");

        stderrScrollPane.getViewport().add(stderrText, null);
        stdoutScrollPane.getViewport().add(stdoutText, null);

        // make sure the last updated log is always in front
        stdoutText.getDocument().addDocumentListener(
                new MyDocumentListener(jTabbedPane1, stdoutScrollPane));
        stderrText.getDocument().addDocumentListener(
                new MyDocumentListener(jTabbedPane1, stderrScrollPane));

        this.pack();
    }

    /*
     * Sets the new OutputStream for System.out and System.err
     */
    private void redirect() {
        JTextComponentAppender appender = new JTextComponentAppender();
        appender.setLayout(new PatternLayout("[%5p][%c{1}][%d{yyyy-MMM-dd HH:mm:ss}]: %m\n"));
        appender.setJTextCompnent(stdoutText);
        Logger.getRootLogger().addAppender(appender);

        /**
         * The PrintStream for the System.out
         */
        PrintStream stdout = System.out;
        stderr = System.err;
        PrintStream stdoutStream = new JTextAreaOutputStream(stdoutText, stdout, true);
        PrintStream stderrStream = new JTextAreaOutputStream(stderrText, stderr, true);
        if (!DODDLEConstants.DEBUG) {
            System.setOut(stdoutStream);
            System.setErr(stderrStream);
        }
    }

    /**
     * JTextComponentにログを表示するためのAppender
     */
    class JTextComponentAppender extends AppenderSkeleton {

        private JTextComponent textComponent;

        JTextComponentAppender() {
        }

        public JTextComponentAppender(final Layout layout) {
            setLayout(layout);
            activateOptions();
        }

        public JTextComponent getJTextComponent() {
            return textComponent;
        }

        void setJTextCompnent(JTextComponent component) {
            this.textComponent = component;
        }

        public void activateOptions() {
            if (textComponent == null) {
                textComponent = new JTextPane();
            }
        }

        protected void append(LoggingEvent event) {
            if (layout == null) {
                textComponent.setText(textComponent.getText() + event.getRenderedMessage());
            } else {
                textComponent.setText(textComponent.getText() + layout.format(event));
            }
        }

        public void close() {
            clearModel();
        }

        void clearModel() {
            textComponent.setText("");
        }

        public boolean requiresLayout() {
            return false;
        }
    }

    /**
     * A PrintStream for the text area output.
     *
     * @author Sven Luzar
     */
    class JTextAreaOutputStream extends PrintStream {
        /**
         * the target for this printstream
         */
        private final JTextArea target;

        /**
         * the original PrintStream to forward this stream to the original
         * stream
         */
        private final PrintStream orig;

        /**
         * Flag is true if the stream should forward the output to the original
         * stream
         */
        private final boolean showOrig;

        /**
         * creates an instance
         */
        JTextAreaOutputStream(JTextArea t, PrintStream orig, boolean showOrig) {
            super(new ByteArrayOutputStream());
            target = t;

            this.showOrig = showOrig;
            this.orig = orig;
        }

        /**
         * writes a boolean value to the target
         */
        public void print(boolean b) {
            if (showOrig)
                orig.print(b);
            if (b)
                target.append("true");
            else
                target.append("false");
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes a boolean value to the target
         */
        public void println(boolean b) {
            if (showOrig)
                orig.println(b);

            if (b)
                target.append("true\n");
            else
                target.append("false\n");
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(char c) {
            if (showOrig)
                orig.print(c);

            char[] tmp = new char[1];
            tmp[0] = c;
            target.append(new String(tmp));
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void println(char c) {
            if (showOrig)
                orig.println(c);

            char[] tmp = new char[2];
            tmp[0] = c;
            tmp[1] = '\n';
            target.append(new String(tmp));
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(char[] s) {
            if (showOrig)
                orig.print(s);

            target.append(new String(s));
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void println(char[] s) {
            if (showOrig)
                orig.println(s);

            target.append(new String(s) + System.lineSeparator());
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(double d) {
            if (showOrig)
                orig.print(d);

            target.append(Double.toString(d));
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void println(double d) {
            if (showOrig)
                orig.println(d);

            target.append(d + System.lineSeparator());
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(float f) {
            if (showOrig)
                orig.print(f);

            target.append(Float.toString(f));
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void println(float f) {
            if (showOrig)
                orig.println(f);

            target.append(f + System.lineSeparator());
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(int i) {
            if (showOrig)
                orig.print(i);

            target.append(Integer.toString(i));
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void println(int i) {
            if (showOrig)
                orig.println(i);

            target.append(i + System.lineSeparator());
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(long l) {
            if (showOrig)
                orig.print(l);

            target.append(Long.toString(l));
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void println(long l) {
            if (showOrig)
                orig.println(l);

            target.append(l + System.lineSeparator());
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(Object o) {
            if (showOrig)
                orig.print(o);

            target.append(o.toString());
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void println(Object o) {
            if (showOrig)
                orig.println(o);

            target.append(o.toString() + System.lineSeparator());
            target.setCaretPosition(target.getText().length());
        }

        /**
         * writes the value to the target
         */
        public void print(String s) {
            if (showOrig)
                orig.print(s);

            target.append(s);
            target.setCaretPosition(target.getText().length());
            if (orig == stderr) {
                // setVisible(true);
            }
        }

        /**
         * writes the value to the target
         */
        public void println(String s) {
            if (showOrig)
                orig.println(s);

            target.append(s + System.lineSeparator());
            target.setCaretPosition(target.getText().length());
            if (orig == stderr) {
                // setVisible(true);
            }
        }

        /**
         * writes the value to the target
         */
        public void println() {
            if (showOrig)
                orig.println();

            target.append(System.lineSeparator());
            target.setCaretPosition(target.getText().length());
        }
    }

    /**
     * disposes this window
     */
    private void this_windowDispose(WindowEvent e) {
        this.dispose();
    }

    /**
     * closes this window
     */
    private void this_windowClosing(WindowEvent e) {
        this_windowDispose(e);
        System.exit(0);
    }

    /**
     * Shows the popup menu for the System.out textarea
     */
    private void stdoutText_mouseClicked(MouseEvent e) {
        if (e.getModifiersEx() == MouseEvent.META_DOWN_MASK) {
            popup.setTextArea(stdoutText);
            popup.show(this.stdoutText, e.getX(), e.getY());
        }

    }

    /**
     * Shows the popup menu for the System.err textarea
     */
    private void stderrText_mouseClicked(MouseEvent e) {
        if (e.getModifiersEx() == MouseEvent.META_DOWN_MASK) {
            popup.setTextArea(stderrText);
            popup.show(this.stderrText, e.getX(), e.getY());
        }

    }

}

/**
 * Document listener to detect changes at the text areas and switches the right
 * one text area to front.
 */

class MyDocumentListener implements DocumentListener {
    /**
     * The Tabbed pane to switch the right one text area to front
     */
    private final JTabbedPane paneToSwitch;
    /**
     * The component which is in front
     */
    private final Component componentInFront;

    /**
     * creats an instance of this listener
     */
    public MyDocumentListener(JTabbedPane paneToSwitch, Component inFront) {
        this.paneToSwitch = paneToSwitch;
        this.componentInFront = inFront;
    }

    /**
     * Calls getInFront()
     *
     * @see #getInFront
     */
    public void changedUpdate(DocumentEvent e) {
        getInFront();
    }

    /**
     * Calls getInFront()
     *
     * @see #getInFront
     */
    public void insertUpdate(DocumentEvent e) {
        getInFront();
    }

    /**
     * Calls getInFront()
     *
     * @see #getInFront
     */
    public void removeUpdate(DocumentEvent e) {
        getInFront();
    }

    /**
     * Switches the rights one text area to front
     */
    private void getInFront() {
        // bring the attached component in front
        paneToSwitch.setSelectedComponent(this.componentInFront);
    }
}

/**
 * Internal Popup Menu with a clear and a save button to clear or save the text
 * areas.
 */

class InternalPopupMenu extends JPopupMenu {
    /**
     * The current textarea
     */
    private JTextArea currentWindow = null;

    /**
     * creates an instance
     */
    public InternalPopupMenu() {
        super();
        /**
         * Menu item for clearing the text area
         */
        JMenuItem jMenuItemClearWindow = new JMenuItem("Clear output");
        this.add(jMenuItemClearWindow);
        this.addSeparator();
        /**
         * Menu item for saving the text area
         */
        JMenuItem jMenuItemSaveToFile = new JMenuItem("Save to file...");
        this.add(jMenuItemSaveToFile);

        jMenuItemClearWindow.addActionListener(e -> clearWindow());
        jMenuItemSaveToFile.addActionListener(e -> saveWindowToFile());
        // jMenuItemSaveToFile.setEnabled(false);//.disable();
    }

    /**
     * Sets the current text area
     */
    public void setTextArea(JTextArea ta) {
        currentWindow = ta;
    }

    /**
     * clears the window
     */
    private void clearWindow() {
        currentWindow.setText("");
    }

    /**
     * Shows a file chooser and saves the file to the selected name
     */
    private void saveWindowToFile() {
        JFileChooser fileDlg = new JFileChooser();
        // fileDlg.setFileFilter(filter);
        // no file selected.
        if (JFileChooser.APPROVE_OPTION != fileDlg.showSaveDialog(null)) {
            System.out.println("No file selected");
            return;
        }
        File f = fileDlg.getSelectedFile();

        if (!f.canWrite())
            System.err.println("Can'buttonText write to file " + f.getAbsolutePath());
        try {
            PrintStream os = new PrintStream(new FileOutputStream(f));
            os.println(currentWindow.getText());
            os.close();

            clearWindow();
        } catch (FileNotFoundException e) {
            System.err.println("Can'buttonText write to file " + f.getAbsolutePath());
        }
    }
}
