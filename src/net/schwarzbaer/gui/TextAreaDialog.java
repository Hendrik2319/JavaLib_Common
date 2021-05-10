package net.schwarzbaer.gui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextAreaDialog extends StandardDialog {
	private static final long serialVersionUID = 1135036865387978283L;
	
	private final JTextArea textArea;

	private TextAreaDialog(Window parent, String title, ModalityType modality, boolean repeatedUseOfDialogObject, int width, int height, boolean textEditable) {
		super(parent, title, modality, repeatedUseOfDialogObject);
		
		textArea = new JTextArea();
		textArea.setEditable(textEditable);
		
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);
		textAreaScrollPane.setPreferredSize(new Dimension(width,height));
		
		createGUI(textAreaScrollPane, createButton("Close",e->closeDialog()));
	}
	
	static void showText(Window parent, String title, int width, int height, String text) {
		TextAreaDialog dlg = new TextAreaDialog(parent, title, ModalityType.APPLICATION_MODAL, false, width, height, false);
		dlg.setText(text);
		dlg.showDialog();
	}

	public void setText(String text) {
		textArea.setText(text);
	}

	public String getText() {
		return textArea.getText();
	}

	private JButton createButton(String title, ActionListener al) {
		JButton comp = new JButton(title);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}
}
