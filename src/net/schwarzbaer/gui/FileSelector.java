package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;


public class FileSelector implements ActionListener {

	private JFileChooser fileChooser;
	private JButton button;
	private JTextField field;
	private final Component parent;
	private final FileSelectorListener listener;
	private final String id;

	public FileSelector(Component parent, String id, FileSelectorListener listener) {
		this.parent = parent;
		this.id = id;
		this.listener = listener;
		button = null;
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
	}
	
	public void setCurrentDirectory(String dir) {
		fileChooser.setCurrentDirectory(new File(dir));
	}

	public void addAlternative(String dir) {
		// TODO Auto-generated method stub
		
	}

	public Iterator<String> getAlternatives() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setEnabled(boolean b) {
		button.setEnabled(b);
		field .setEnabled(b);
	}

	public Component getInputField() {
		if (field==null) field = GUI.createTextField("select field", this, true, null);
		return field;
	}

	public Component getSelectButton(String title) {
		if (button==null) button = GUI.createButton(title, "select button", this, GUI.getFileIcon(new File(".")));
		return button;
	}

	public void setDirSelectionOnly() {
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	public void setFileSelectionOnly() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

	public void setFileAndDirSelection() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("select button")) {
			if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				field.setText(selectedFile.toString());
				if (checkDir(selectedFile))
					listener.fileSelectionChanged(id, selectedFile);
			}
			return;
		}
		if (e.getActionCommand().equals("select field")) {
			File selectedFile = new File(field.getText());
			if (checkDir(selectedFile) && listener.isFileANewChoice(id, selectedFile)) {
				listener.fileSelectionChanged(id, selectedFile);
				fileChooser.setSelectedFile(selectedFile);
			}
			return;
		}
	}
	
	private boolean checkDir(File selectedFile) {
		
		if (!listener.isFileOK(id, selectedFile)) {
			field.setBackground(Color.RED);
			return false;
		}
		
		field.setBackground(new JTextField().getBackground());
		return true;
	}
	
	public interface FileSelectorListener {
		public void    fileSelectionChanged(String id, File file);
		public boolean isFileOK            (String id, File file);
		public boolean isFileANewChoice    (String id, File file);
	}
}