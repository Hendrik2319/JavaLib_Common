package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class FileSelector implements ActionListener {

	private final Component parent;
	private final String id;
	private final Type type;
	private final FileSelectorListener listener;
	private JFileChooser fileChooser;
	private JButton button;
	private JTextField field;
	private JComboBox<String> cmbbx;
	private Color defaultFieldBackground;

	public FileSelector(Component parent, String id, FileSelectorListener listener) {
		this(parent, id, Type.WITHOUT_ALTERNATIVES, listener);
	}
	
	public FileSelector(Component parent, String id, Type type, FileSelectorListener listener) {
		this.parent = parent;
		this.id = id;
		this.type = type;
		this.listener = listener;
		fileChooser = new JFileChooser(".");
		fileChooser.setMultiSelectionEnabled(false);
		button = null;
		field = null;
		cmbbx = null;
		switch(type) {
		case WITH_ALTERNATIVES   :
			JComboBox<String> comboBox = new JComboBox<String>();
			comboBox.setEditable(true);
			defaultFieldBackground = comboBox.getBackground();
			break;
		case WITHOUT_ALTERNATIVES:
			defaultFieldBackground = new JTextField().getBackground();
			break;
		}
		
	}
	
	public void setCurrentDirectory(String dir) {
		File file = new File(dir);
		System.out.println("old CurrentDirectory = \""+fileChooser.getCurrentDirectory()+"\"");
		System.out.println("new CurrentDirectory = \""+file+"\"");
		
		try { fileChooser.setCurrentDirectory(file); }
		catch (Exception e) {
			System.out.println("Can't change current directory of filechooser to \""+file+"\".");
		}
	}

	public void addAlternative(String dir) {
		System.out.printf("addAlternative(\"%s\")\r\n",dir);
		if (type==Type.WITHOUT_ALTERNATIVES) return;
		cmbbx.addItem(dir);
		
	}

	public Iterator<String> getAlternatives() {
		if (type==Type.WITHOUT_ALTERNATIVES) return null;
		// TODO Auto-generated method stub
		return null;
	}

	public void setEnabled(boolean b) {
		button.setEnabled(b);
		switch(type) {
		case WITH_ALTERNATIVES   : cmbbx .setEnabled(b); break;
		case WITHOUT_ALTERNATIVES: field .setEnabled(b); break;
		}
	}

	public Component getInputField() {
		switch(type) {
		case WITH_ALTERNATIVES:
			if (cmbbx==null) {
				cmbbx = new JComboBox<String>(new AlternativeModel());
				cmbbx.setActionCommand("select field");
				cmbbx.addActionListener(this);
				cmbbx.setEditable(true);
			}
			return cmbbx;
		case WITHOUT_ALTERNATIVES:
			if (field==null) field = GUI.createTextField("select field", this, true, null);
			return field;
		}
		return null;
	}

	public Component getSelectButton(String title) {
		if (button==null) button = GUI.createButton(title, "select button", this, GUI.getFileIcon(new File(".")));
		return button;
	}

	public void setDirSelectionOnly   () { fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); }
	public void setFileSelectionOnly  () { fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); }
	public void setFileAndDirSelection() { fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("select button")) {
			if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				setFieldText(selectedFile.toString());
				if (checkDir(selectedFile))
					listener.fileSelectionChanged(id, selectedFile);
			}
			return;
		}
		if (e.getActionCommand().equals("select field")) {
			File selectedFile = new File(getFieldText());
			if (checkDir(selectedFile) && listener.isFileANewChoice(id, selectedFile)) {
				listener.fileSelectionChanged(id, selectedFile);
				fileChooser.setSelectedFile(selectedFile);
			}
			return;
		}
	}

	private String getFieldText() {
		switch(type) {
		case WITH_ALTERNATIVES   : return cmbbx.getSelectedItem().toString();
		case WITHOUT_ALTERNATIVES: return field.getText();
		}
		return null;
	}
	
	private void setFieldText(String txt) {
		switch(type) {
		case WITH_ALTERNATIVES   : cmbbx.setSelectedItem(txt); break;
		case WITHOUT_ALTERNATIVES: field.setText(txt); break;
		}
	}
	
	private boolean checkDir(File selectedFile) {
		
		if (!listener.isFileOK(id, selectedFile)) {
			switch(type) {
			case WITH_ALTERNATIVES   : cmbbx.setBackground(Color.RED); break;
			case WITHOUT_ALTERNATIVES: field.setBackground(Color.RED); break;
			}
			return false;
		}
		
		switch(type) {
		case WITH_ALTERNATIVES   : cmbbx.setBackground(defaultFieldBackground); break;
		case WITHOUT_ALTERNATIVES: field.setBackground(defaultFieldBackground); break;
		}
		return true;
	}
	
	public static enum Type {
		WITH_ALTERNATIVES,
		WITHOUT_ALTERNATIVES
	}
	
	public class AlternativeModel implements MutableComboBoxModel<String> {
		
		private final Vector<ListDataListener> listeners;
		private final Vector<String> items;
		private String selectedItem;

		public AlternativeModel() {
			listeners = new Vector<ListDataListener>();
			items = new Vector<String>();
		}
		
		@Override public void    addListDataListener(ListDataListener ldl) { listeners.   add(ldl); }
		@Override public void removeListDataListener(ListDataListener ldl) { listeners.remove(ldl); }
		
		private void fireContentsChanged(ListDataEvent e) {
			Iterator<ListDataListener> it = listeners.iterator();
			while(it.hasNext()) it.next().contentsChanged(e);
		}
		private void fireIntervalAdded(ListDataEvent e) {
			Iterator<ListDataListener> it = listeners.iterator();
			while(it.hasNext()) it.next().intervalAdded(e);
		}
		private void fireIntervalRemoved(ListDataEvent e) {
			Iterator<ListDataListener> it = listeners.iterator();
			while(it.hasNext()) it.next().intervalRemoved(e);
		}

		@Override public int getSize() { return items.size(); }
		@Override public String getElementAt(int i) {
			System.out.printf("getElementAt(%d) [=\"%s\"]\r\n",i,items.get(i));
			return items.get(i);
		}

		@Override
		public Object getSelectedItem() {
			System.out.printf("getSelectedItem() [=\"%s\"]\r\n",this.selectedItem);
			return this.selectedItem;
		}
	
		@Override
		public void setSelectedItem(Object selectedItem) {
			this.selectedItem = (selectedItem==null?null:selectedItem.toString());
			System.out.printf("setSelectedItem(\"%s\")\r\n",selectedItem);
		}

		@Override
		public void addElement(String item) {
			System.out.printf("addElement(\"%s\")\r\n",item);
			items.add(item);
		}

		@Override
		public void insertElementAt(String item, int i) {
			System.out.printf("insertElementAt(\"%s\",%d)\r\n",item,i);
			items.insertElementAt(item,i);
		}

		@Override
		public void removeElement(Object item) {
			System.out.printf("removeElement(\"%s\")\r\n",item);
			items.remove(item);
		}

		@Override
		public void removeElementAt(int i) {
			System.out.printf("removeElementAt(%d)\r\n",i);
			items.removeElementAt(i);
		}
	
	}

	public interface FileSelectorListener {
		public void    fileSelectionChanged(String id, File file);
		public boolean isFileOK            (String id, File file);
		public boolean isFileANewChoice    (String id, File file);
	}
}