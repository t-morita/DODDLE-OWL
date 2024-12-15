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
import io.github.doddle_owl.utils.ConceptTreeMaker;
import io.github.doddle_owl.utils.EDR2DoddleDicConverter;
import io.github.doddle_owl.utils.EDR2DoddleDicConverter.DictionaryType;
import io.github.doddle_owl.utils.JPNWN2DODDLEDicConverter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class DODDLEDicConverterUI extends JDialog implements ActionListener {

	private final JRadioButton edrRadioButton;
	private final JRadioButton edrtRadioButton;
	private final JRadioButton jpnwnRadioButton;

	private final JRadioButton txtBox;
	// private JCheckBox dbBox;
	private final JRadioButton owlBox;

	private final JTextField orgDicPathField;
	private final JButton refOrgDicPathButton;
	private final JTextField doddleDicPathField;
	private final JButton refDoddleDicPathButton;
	private final JButton convertButton;
	private final JButton exitButton;
	private static final JLabel progressLabel = new JLabel();
	private static final JProgressBar progressBar = new JProgressBar();

	public DODDLEDicConverterUI() {
		edrRadioButton = new JRadioButton("EDR");
		edrRadioButton.setSelected(true);
		edrtRadioButton = new JRadioButton("EDRT");
		jpnwnRadioButton = new JRadioButton("JPN WordNet");

		ButtonGroup group = new ButtonGroup();
		group.add(edrRadioButton);
		group.add(edrtRadioButton);
		group.add(jpnwnRadioButton);
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new GridLayout(1, 3));
		radioButtonPanel.setBorder(BorderFactory.createTitledBorder("Dictionary Type"));
		radioButtonPanel.add(edrRadioButton);
		radioButtonPanel.add(edrtRadioButton);
		radioButtonPanel.add(jpnwnRadioButton);

		txtBox = new JRadioButton("Text", true);
		// dbBox = new JCheckBox("Berkely DB", false);
		owlBox = new JRadioButton("OWL", false);
		group = new ButtonGroup();
		group.add(txtBox);
		group.add(owlBox);
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridLayout(1, 2));
		checkBoxPanel.setBorder(BorderFactory.createTitledBorder("Conversion Type"));
		checkBoxPanel.add(txtBox);
		// checkBoxPanel.add(dbBox);
		checkBoxPanel.add(owlBox);

		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new GridLayout(1, 2));
		optionPanel.add(radioButtonPanel);
		optionPanel.add(checkBoxPanel);

		orgDicPathField = new JTextField(40);
		orgDicPathField.setEditable(false);
		refOrgDicPathButton = new JButton("Browse");
		refOrgDicPathButton.addActionListener(this);
		JPanel orgDicPathPanel = new JPanel();
		orgDicPathPanel.setLayout(new BorderLayout());
		orgDicPathPanel.add(orgDicPathField, BorderLayout.CENTER);
		orgDicPathPanel.add(refOrgDicPathButton, BorderLayout.EAST);
		orgDicPathPanel.setBorder(BorderFactory.createTitledBorder("Input Dictionary Path"));

		doddleDicPathField = new JTextField(40);
		doddleDicPathField.setEditable(false);
		refDoddleDicPathButton = new JButton("Browse");
		refDoddleDicPathButton.addActionListener(this);
		JPanel doddleDicPanel = new JPanel();
		doddleDicPanel.setLayout(new BorderLayout());
		doddleDicPanel.add(doddleDicPathField, BorderLayout.CENTER);
		doddleDicPanel.add(refDoddleDicPathButton, BorderLayout.EAST);
		doddleDicPanel.setBorder(BorderFactory.createTitledBorder("Output Dictionary Path"));

		convertButton = new JButton("Convert");
		convertButton.addActionListener(this);
		JPanel progressBarPanel = new JPanel();
		progressBarPanel.setLayout(new BorderLayout());
		progressBarPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
		progressBarPanel.add(progressBar, BorderLayout.CENTER);
		progressBarPanel.add(convertButton, BorderLayout.EAST);

		exitButton = new JButton("Close");
		exitButton.addActionListener(this);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(exitButton, BorderLayout.EAST);

		progressLabel.setBorder(BorderFactory.createTitledBorder("Message"));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(5, 1));
		mainPanel.add(optionPanel);
		mainPanel.add(orgDicPathPanel);
		mainPanel.add(doddleDicPanel);
		mainPanel.add(progressBarPanel);
		mainPanel.add(progressLabel);

		Container contentPane = getContentPane();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		pack();
		setTitle("DODDLE Dic Converter");
		setLocationRelativeTo(null);
	}

	public static void addProgressValue() {
		progressBar.setValue(progressBar.getValue() + 1);
	}

	public static void setProgressText(String text) {
		progressLabel.setText(text);
	}

	private void setDicPath(JTextField textField) {
		File currentDirectory = new File(textField.getText());
		JFileChooser jfc = new JFileChooser(currentDirectory);
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		jfc.setDialogTitle("Select Directory");
		int fd = jfc.showOpenDialog(this);
		if (fd == JFileChooser.APPROVE_OPTION) {
			if (getDicType() == DictionaryType.JPNWN && textField == orgDicPathField) {
				textField.setText(jfc.getSelectedFile().toString());
			} else {
				textField.setText(jfc.getSelectedFile().toString() + File.separator);
			}
		}
	}

	private DictionaryType getDicType() {
		if (edrRadioButton.isSelected()) {
			return DictionaryType.EDR;
		} else if (edrtRadioButton.isSelected()) {
			return DictionaryType.EDRT;
		} else if (jpnwnRadioButton.isSelected()) {
			return DictionaryType.JPNWN;
		}
		return DictionaryType.EDR;
	}

	private void convertDoddleDic() {
		new Thread() {

			private void makeRelationData(DictionaryType dicType) {
				if (dicType == DictionaryType.EDR) {
					progressLabel.setText("Make Relation Data");
					EDR2DoddleDicConverter.readRelationData();
					EDR2DoddleDicConverter.writeRelationData();
					EDR2DoddleDicConverter.clearRelationMaps();
					addProgressValue();
				} else if (dicType == DictionaryType.JPNWN) {
					// has-a関係の抽出を行う
				}
			}

			private void makeRelationIndex(DictionaryType dicType) {
				if (dicType == DictionaryType.EDR) {
					progressLabel.setText("Make Relation Index");
					EDR2DoddleDicConverter.writeRelationIndex();
					addProgressValue();
				} else if (dicType == DictionaryType.JPNWN) {
					// make has-a dic index
				}
			}

			private void makeTreeData(DictionaryType dicType) {
				progressLabel.setText("Make ID SubIDSet Map");

				if (dicType == DictionaryType.JPNWN) {
					JPNWN2DODDLEDicConverter.readTreeData(ConceptTreeMaker.JPNWN_CLASS_ROOT_ID);
					JPNWN2DODDLEDicConverter.writeTreeData();
					JPNWN2DODDLEDicConverter.clearTreeData();
				} else {
					if (dicType == DictionaryType.EDR) {
						EDR2DoddleDicConverter.readTreeData(ConceptTreeMaker.EDR_CLASS_ROOT_ID);
					} else {
						EDR2DoddleDicConverter.readTreeData(ConceptTreeMaker.EDRT_CLASS_ROOT_ID);
					}
					EDR2DoddleDicConverter.writeTreeData();
					EDR2DoddleDicConverter.clearTreeData();
				}
				addProgressValue();
			}

			private void makeTreeIndex(DictionaryType dicType) {
				progressLabel.setText("Make Tree Index");
				if (dicType == DictionaryType.JPNWN) {
					JPNWN2DODDLEDicConverter.writeTreeIndex();
				} else {
					EDR2DoddleDicConverter.writeTreeIndex();
				}
				addProgressValue();
			}

			private void makeConceptData(DictionaryType dicType) {
				progressLabel.setText("Make Concept Data");
				if (dicType == DictionaryType.JPNWN) {
					JPNWN2DODDLEDicConverter.readConceptData();
					JPNWN2DODDLEDicConverter.writeConceptData();
				} else {
					EDR2DoddleDicConverter.readConceptData();
					EDR2DoddleDicConverter.writeConceptData();
				}
				addProgressValue();
			}

			private void makeConceptIndex(DictionaryType dicType) {
				progressLabel.setText("Make Concept Index");
				if (dicType == DictionaryType.JPNWN) {
					JPNWN2DODDLEDicConverter.readConceptIndex();
					JPNWN2DODDLEDicConverter.writeConceptIndex();
					JPNWN2DODDLEDicConverter.clearDataFilePointerList();
				} else {
					EDR2DoddleDicConverter.readConceptIndex();
					EDR2DoddleDicConverter.writeConceptIndex();
					EDR2DoddleDicConverter.clearDataFilePointerList();
				}
				addProgressValue();
			}

			private void makeWordData(DictionaryType dicType) {
				progressLabel.setText("Make Word Data");
				if (dicType == DictionaryType.JPNWN) {
					JPNWN2DODDLEDicConverter.readWordData();
					JPNWN2DODDLEDicConverter.writeWordData();
					clearJPNWNDicMap();
				} else {
					EDR2DoddleDicConverter.readWordData();
					EDR2DoddleDicConverter.writeWordData();
					clearEDRDicMap();
				}
				addProgressValue();
			}

			private void makeWordIndex(DictionaryType dicType) {
				progressLabel.setText("Make Word Index");
				if (dicType == DictionaryType.JPNWN) {
					JPNWN2DODDLEDicConverter.writeWordIndex();
				} else {
					EDR2DoddleDicConverter.writeWordIndex();
				}
				addProgressValue();
			}

			private void clearJPNWNDicMap() {
				progressLabel.setText("Clear Maps");
				JPNWN2DODDLEDicConverter.clearIDDefinitionMap();
				JPNWN2DODDLEDicConverter.clearWordIDSetMap();
				JPNWN2DODDLEDicConverter.clearIDFilePointerMap();
				JPNWN2DODDLEDicConverter.clearWordFilePointerSetMap();
			}

			private void clearEDRDicMap() {
				progressLabel.setText("Clear Maps");
				EDR2DoddleDicConverter.clearIDDefinitionMap();
				EDR2DoddleDicConverter.clearWordIDSetMap();
				EDR2DoddleDicConverter.clearIDFilePointerMap();
				EDR2DoddleDicConverter.clearWordFilePointerSetMap();
			}

			private void makeTextDataAndIndex(DictionaryType dicType) {
				makeRelationData(dicType);
				makeRelationIndex(dicType);
				makeTreeData(dicType);
				makeTreeIndex(dicType);
				makeConceptData(dicType);
				makeConceptIndex(dicType);
				makeWordData(dicType);
				makeWordIndex(dicType);
			}

			void convertOWL(DictionaryType dicType) {
				if (dicType == DictionaryType.JPNWN) {
					Model ontModel = ModelFactory.createDefaultModel();
					JPNWN2DODDLEDicConverter.readConceptData();
					JPNWN2DODDLEDicConverter.writeOWLConceptData(ontModel,
							DODDLEConstants.JPN_WN_URI);
					JPNWN2DODDLEDicConverter.saveOntology(ontModel, dicType + ".owl");
					Model treeOntModel = ModelFactory.createDefaultModel();
					JPNWN2DODDLEDicConverter.writeOWLTreeData(treeOntModel,
							DODDLEConstants.JPN_WN_URI);
					JPNWN2DODDLEDicConverter.saveOntology(treeOntModel, dicType + "_tree.owl");
				} else {
					String ns = "";
					if (dicType == DictionaryType.EDR) {
						ns = DODDLEConstants.EDR_URI;
					} else if (dicType == DictionaryType.EDRT) {
						ns = DODDLEConstants.EDRT_URI;
					}
					Model jaOntModel = ModelFactory.createDefaultModel();
					Model enOntModel = ModelFactory.createDefaultModel();
					EDR2DoddleDicConverter.writeOWLConceptData(jaOntModel, enOntModel, ns);
					EDR2DoddleDicConverter.saveOntology(jaOntModel, dicType + "_ja.owl");
					EDR2DoddleDicConverter.saveOntology(enOntModel, dicType + "_en.owl");
					Model treeOntModel = ModelFactory.createDefaultModel();
					EDR2DoddleDicConverter.writeOWLTreeData(treeOntModel, ns);
					EDR2DoddleDicConverter.saveOntology(treeOntModel, dicType + "_tree.owl");

					if (dicType == DictionaryType.EDR) {
						Model regionOntModel = ModelFactory.createDefaultModel();
						EDR2DoddleDicConverter.writeOWLRegionData(regionOntModel, ns);
						EDR2DoddleDicConverter
								.saveOntology(regionOntModel, dicType + "_region.owl");
					}
				}
			}

			private void setTXTProgressValue(DictionaryType dicType) {
				progressBar.setValue(0);
				progressLabel.setText("");
				int value = 0;
				if (dicType == DictionaryType.EDR) {
					value = 11;
				} else if (dicType == DictionaryType.EDRT) {
					value = 9;
				} else if (dicType == DictionaryType.JPNWN) {
					value = 15;
				}
				progressBar.setMaximum(value);
			}

			private void setOWLProgressValue(DictionaryType dicType) {
				progressBar.setValue(0);
				progressLabel.setText("");
				int value = 0;
				if (dicType == DictionaryType.EDR) {
					value = 9;
				} else if (dicType == DictionaryType.EDRT) {
					value = 7;
				} else if (dicType == DictionaryType.JPNWN) {
					value = 5;
				}
				progressBar.setMaximum(value);
			}

			public void run() {
				DictionaryType dicType = getDicType();
				if (dicType == DictionaryType.JPNWN) {
					JPNWN2DODDLEDicConverter.setJPNWNPath(orgDicPathField.getText());
					boolean isEnable = JPNWN2DODDLEDicConverter.initJPNWNDB();
					if (!isEnable) {
						initProgressBar("Error: FileNotFound");
						return;
					}
					JPNWN2DODDLEDicConverter.setDODDLEDicPath(doddleDicPathField.getText());
				} else {
					boolean isEnable = EDR2DoddleDicConverter.setEDRDicPath(
							orgDicPathField.getText(), dicType);
					if (!isEnable) {
						initProgressBar("Error: FileNotFound");
						return;
					}
					EDR2DoddleDicConverter.setDODDLEDicPath(doddleDicPathField.getText());
				}
				try {
					if (owlBox.isSelected()) {
						setOWLProgressValue(dicType);
						convertOWL(dicType);
					} else if (txtBox.isSelected()) {
						setTXTProgressValue(dicType);
						makeTextDataAndIndex(dicType);
					}
					progressLabel.setText("Done");
				} catch (Exception e) {
					e.printStackTrace();
					initProgressBar("Error");
				}
			}
		}.start();
	}

	public static void initProgressBar(String msg) {
		progressLabel.setText(msg);
		progressBar.setValue(0);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == exitButton) {
			setVisible(false);
		} else if (e.getSource() == refOrgDicPathButton) {
			setDicPath(orgDicPathField);
		} else if (e.getSource() == refDoddleDicPathButton) {
			setDicPath(doddleDicPathField);
		} else if (e.getSource() == convertButton) {
			convertDoddleDic();
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			DODDLEDicConverterUI converter = new DODDLEDicConverterUI();
			converter.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
