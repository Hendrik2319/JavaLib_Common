package net.schwarzbaer.gui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextAreaDialog extends StandardDialog {
	private static final long serialVersionUID = 1135036865387978283L;
	
	private final JTextArea textArea;

	private TextAreaDialog(Window parent, String title, ModalityType modality, boolean repeatedUseOfDialogObject, int width, int height, boolean textEditable, boolean textAreaLineWrap) {
		super(parent, title, modality, repeatedUseOfDialogObject);
		
		JPopupMenu textViewContextMenu = new JPopupMenu();
		textArea = new JTextArea();
		textArea.setEditable(textEditable);
		textArea.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3)
					textViewContextMenu.show(textArea, e.getX(), e.getY());
			}
		});
		
		textArea.setLineWrap(textAreaLineWrap);
		textArea.setWrapStyleWord(textAreaLineWrap);
		textViewContextMenu.add(createCheckBoxMenuItem("Line Wrap", textAreaLineWrap, isChecked->{
			textArea.setLineWrap(isChecked);
			textArea.setWrapStyleWord(isChecked);
		}) );
		
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);
		textAreaScrollPane.setPreferredSize(new Dimension(width,height));
		
		createGUI(textAreaScrollPane, createButton("Close",e->closeDialog()));
	}
	
	public static void showText(Window parent, String title, int width, int height, boolean textAreaLineWrap, String text) {
		TextAreaDialog dlg = new TextAreaDialog(parent, title, ModalityType.APPLICATION_MODAL, false, width, height, false, textAreaLineWrap);
		dlg.setText(text);
		dlg.showDialog();
	}

	public void setText(String text) {
		textArea.setText(text);
	}

	public String getText() {
		return textArea.getText();
	}

	private static JButton createButton(String title, ActionListener al) {
		JButton comp = new JButton(title);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}

	private static JCheckBoxMenuItem createCheckBoxMenuItem(String title, boolean isChecked, Consumer<Boolean> setValue) {
		JCheckBoxMenuItem comp = new JCheckBoxMenuItem(title,isChecked);
		if (setValue!=null) comp.addActionListener(e->setValue.accept(comp.isSelected()));
		return comp;
	}
}
