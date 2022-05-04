package net.schwarzbaer.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MultiStepProgressDialog extends StandardDialog {
	private static final long serialVersionUID = 7276275006093575333L;
	
	private final Vector<Runnable> tasks;
	private final Vector<CancelListener> cancelListeners;
	private boolean canceled;
	private boolean wasOpened;
	private String monitorObj;
	private final JPanel contentPane;
	private final int minWidth;

	public MultiStepProgressDialog(Window parent, String title, int minWidth) {
		super(parent,title);
		this.minWidth = minWidth;
		tasks = new Vector<Runnable>();
		cancelListeners = new Vector<>();
		canceled = false;
		wasOpened = false;
		monitorObj = "";
		
		addWindowListener(new WindowAdapter() {
			@Override public void windowOpened(WindowEvent e) {
				wasOpened = true;
				synchronized (monitorObj) { monitorObj.notifyAll(); }
			}
			@Override public void windowClosed (WindowEvent e) { cancel(); }
			@Override public void windowClosing(WindowEvent e) { cancel(); }
		});
		
		contentPane = new JPanel(new GridBagLayout());
		
		createGUI(
			contentPane,
			createButton("Cancel", true, e->{ cancel(); })
		);
	}

	private static JButton createButton(String text, boolean enabled, ActionListener al) {
		JButton comp = new JButton(text,null);
		comp.setEnabled(enabled);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}

	protected void addTask(String title, Consumer<ProgressView> task) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		JLabel label = new JLabel(title+": ");
		label.setVerticalAlignment(JLabel.TOP);
		label.setHorizontalAlignment(JLabel.LEFT);
		
		ProgressDialog.ProgressPanel progressPanel = new ProgressDialog.ProgressPanel(ProgressView.ProgressDisplay.None, false, false);
		progressPanel.setTaskTitle("Waiting ...");
		progressPanel.setValue(0, 100);
		if (minWidth>0) progressPanel.setProgressBarMinWidth(minWidth);
		
		c.weighty = 1;
		c.weightx = 0;
		c.gridwidth = 1;
		contentPane.add(label,c);
		
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		contentPane.add(progressPanel,c);
		
		tasks.add(()->{
			if (wasCanceled()) {
				SwingUtilities.invokeLater(()->{
					progressPanel.setTaskTitle("Canceled");
					progressPanel.setValue(0, 100);
				});
			} else {
				task.accept(progressPanel);
				SwingUtilities.invokeLater(()->{
					progressPanel.setTaskTitle("Finished");
					progressPanel.setValue(100, 100);
				});
			}
		});
	}

	public void start() {
		Thread thread = new Thread(()->{
			waitUntilDialogIsVisible();
			for (Runnable task : tasks) task.run();
			SwingUtilities.invokeLater(this::closeDialog);
		});
		addCancelListener(thread::interrupt);
		thread.start();
		showDialog();
	}
	
	private void waitUntilDialogIsVisible() {
		while(!wasOpened)
			try { synchronized (monitorObj) { monitorObj.wait(); } }
			catch (InterruptedException e) {}
	}
	
	private synchronized void cancel() {
		if (!canceled) for (CancelListener cl:cancelListeners) cl.cancelTask();
		canceled = true;
	}

	public synchronized boolean wasCanceled() {
		return canceled;
	}

	public static interface CancelListener {
		public void cancelTask();
	}

	public void addCancelListener(CancelListener cancelListener) {
		cancelListeners.add(cancelListener);
	}
}

