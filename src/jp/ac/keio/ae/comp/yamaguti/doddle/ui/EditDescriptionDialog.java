package jp.ac.keio.ae.comp.yamaguti.doddle.ui;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class EditDescriptionDialog extends JDialog implements ActionListener {

    private DODDLELiteral description;

    private JTextField langField;
    private JTextArea descriptionArea;

    private JButton applyButton;
    private JButton cancelButton;

    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 250;

    public EditDescriptionDialog(Frame rootFrame) {
        super(rootFrame, Translator.getString("EditDescriptionDialog.Title"), true);

        description = new DODDLELiteral("", "");

        langField = new JTextField(5);
        JComponent langFieldP = Utils.createTitledPanel(langField, DODDLEConstants.LANG, 50, 20);
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        JScrollPane commentAreaScroll = new JScrollPane(descriptionArea);
        commentAreaScroll.setBorder(BorderFactory.createTitledBorder(DODDLEConstants.DESCRIPTION));

        applyButton = new JButton(DODDLEConstants.OK);
        applyButton.setMnemonic('o');
        applyButton.addActionListener(this);
        cancelButton = new JButton(DODDLEConstants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(Utils.createWestPanel(langFieldP), BorderLayout.NORTH);
        getContentPane().add(commentAreaScroll, BorderLayout.CENTER);
        getContentPane().add(Utils.createEastPanel(buttonPanel), BorderLayout.SOUTH);

        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setLocationRelativeTo(rootFrame);
        setVisible(false);
    }

    public void setDescription(DODDLELiteral description) {
        langField.setText(description.getLang());
        descriptionArea.setText(description.getString());
    }

    public DODDLELiteral getDescription() {
        return description;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            description.setLang(langField.getText());
            description.setString(descriptionArea.getText());
            descriptionArea.requestFocus();
            setVisible(false);
        } else if (e.getSource() == cancelButton) {
            description.setLang("");
            description.setString("");
            descriptionArea.requestFocus();
            setVisible(false);
        }
    }
}
