package net.schwarzbaer.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class ProgressDialog extends StandardDialog implements ActionListener {
	private static final long serialVersionUID = 1401683964054921965L;
	private JLabel taskTitle;
	private JProgressBar progressbar;
	private Vector<CancelListener> cancelListeners;
	private boolean canceled;
	
	public static void main(String[] args) {
		GUI.setSystemLookAndFeel();
		StandardMainWindow smw = new StandardMainWindow("Test");
		smw.startGUI(new JPanel());
		
		final ProgressDialog pd = new ProgressDialog(smw,"Test");
		
		pd.addCancelListener(new CancelListener() {
			@Override public void cancelTask() {
				pd.closeDialog();
			}
		});
		
		new Thread(new Runnable() {
			@Override public void run() {
				int max = 1000000000;
				pd.setTaskTitle("Import station adresses:");
				pd.setValue(0, max);
				for (int i=0; i<max; i++) {
					Vector<String> temp = new Vector<String>();
					temp.add("");
					temp.clear();
					pd.setValue(i+1);
				}
				pd.closeDialog();
			}
		}).start();
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				pd.showDialog();
			}
		});
	}
	
	public ProgressDialog(Window parent, String title, ModalityType modality) {
		super(parent, title, modality);
		createGUI();
		this.cancelListeners = new Vector<CancelListener>();
		this.canceled = false;
	}

	public ProgressDialog(Window parent, String title) {
		this(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
	}
	
	private void createGUI() {
		
		JPanel progressbarPane = new JPanel(new BorderLayout(3,3));
		progressbarPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		progressbarPane.add(taskTitle = new JLabel("  "), BorderLayout.NORTH);
		progressbarPane.add(progressbar = new JProgressBar(JProgressBar.HORIZONTAL), BorderLayout.CENTER);
		
		JPanel contentPane = new JPanel(new BorderLayout(3,3));
		contentPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		contentPane.add(progressbarPane);
		contentPane.add(GUI.createRightAlignedPanel(GUI.createButton("Cancel","Cancel",this)),BorderLayout.SOUTH);
		
		progressbar.setIndeterminate(true);
//		progressbar.setPreferredSize(new Dimension(10, 10));
		
		super.createGUI( contentPane );
		super.setSizeAsMinSize();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Cancel")) {
			for (CancelListener cl:cancelListeners) cl.cancelTask();
			canceled = true;
			this.closeDialog();
			return;
		}
	}
	
//	@Override
//	public void showDialog(final Position position) {
//		new Thread(new Runnable() {
//			@Override public void run() {
//				ProgressDialog.super.showDialog(position);
//			}
//		}).start();
//	}
//
//	@Override
//	public void showDialog() {
//		new Thread(new Runnable() {
//			@Override public void run() {
//				ProgressDialog.super.showDialog();
//			}
//		}).start();
//	}
	
	@Override
	public void closeDialog() {
		super.closeDialog();
//		dispose();
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

	public void addCancelListener(CancelListener cancelListener) {
		cancelListeners.add(cancelListener);
	}
	
	public boolean wasCanceled() {
		return canceled;
	}

	public static interface CancelListener {
		public void cancelTask();
	}
}