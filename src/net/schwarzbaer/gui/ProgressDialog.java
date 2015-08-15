package net.schwarzbaer.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressDialog extends StandardDialog implements ActionListener {
	private static final long serialVersionUID = 1401683964054921965L;
	private JLabel taskTitle;
	private JProgressBar progressbar;
	private CancelListener cancelListener;
	private boolean canceled;

	public ProgressDialog(Window parent, String title, ModalityType modality) {
		super(parent, title, modality);
		createGUI();
		this.cancelListener = null;
		this.canceled = false;
	}

	public ProgressDialog(Window parent, String title) {
		this(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
	}
	
	private void createGUI() {
		
		JPanel contentPane = new JPanel(new BorderLayout(3,3));
		contentPane.add(taskTitle = new JLabel(""), BorderLayout.NORTH);
		contentPane.add(progressbar = new JProgressBar(JProgressBar.HORIZONTAL), BorderLayout.CENTER);
		contentPane.add(GUI.createRightAlignedPanel(GUI.createButton("Cancel","Cancel",this)),BorderLayout.SOUTH);
		
		progressbar.setIndeterminate(true);
		
		super.createGUI( contentPane );
		super.setSizeAsMinSize();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Cancel")) {
			if (cancelListener!=null)
				cancelListener.cancelTask();
			canceled = true;
			this.closeDialog();
			return;
		}
	}
	
	@Override
	public void showDialog(final Position position) {
		new Thread(new Runnable() {
			@Override public void run() {
				ProgressDialog.super.showDialog(position);
			}
		}).start();
	}

	@Override
	public void showDialog() {
		new Thread(new Runnable() {
			@Override public void run() {
				ProgressDialog.super.showDialog();
			}
		}).start();
	}

	public void setTaskTitle(String str) {
		taskTitle.setText(str);
	}
	
	public void setValue(int min, int value, int max) {
		progressbar.setValue(value);
		progressbar.setMaximum(max);
		if (progressbar.isIndeterminate())
			progressbar.setIndeterminate(false);
	}
	public void setValue(int value, int max) {
		setValue(0,value,max);
	}
	public void setValue(int value) {
		if (progressbar.isIndeterminate())
			throw new IllegalStateException("Can't set value of progress without setting min and max.");
		progressbar.setValue(value);
	}

	public void setCancelListener(CancelListener cancelListener) {
		this.cancelListener = cancelListener;
	}
	
	public boolean wasCanceled() {
		return canceled;
	}

	public static interface CancelListener {
		public void cancelTask();
	}
}
