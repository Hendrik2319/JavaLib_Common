package net.schwarzbaer.gui;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class StandardMainWindow extends JFrame implements WindowListener {
	private static final long serialVersionUID = -6515512014217265169L;
	
	private final CloseListener closeListener;
	private final DefaultCloseOperation defaultCloseOperation;

    public StandardMainWindow(String title, CloseListener closeListener, DefaultCloseOperation defaultCloseOperation ) throws HeadlessException {
        super(title);
        this.closeListener = closeListener;
		this.defaultCloseOperation = defaultCloseOperation;
    }
    public StandardMainWindow(String title, CloseListener closeListener                 ) throws HeadlessException { this(title,closeListener,DefaultCloseOperation.DO_NOTHING_ON_CLOSE); }
    public StandardMainWindow(String title, DefaultCloseOperation defaultCloseOperation ) throws HeadlessException { this(title,null,defaultCloseOperation); }
    public StandardMainWindow(String title) throws HeadlessException { this(title,null,DefaultCloseOperation.EXIT_ON_CLOSE); }
    public StandardMainWindow(            ) throws HeadlessException { this(""); }

    public void startGUI( JComponent contentPane ) {
        startGUI( contentPane, null, null );
    }
    public void startGUI( JComponent contentPane, JMenuBar menuBar ) {
        startGUI( contentPane, menuBar, null );
    }
    public void startGUI( JComponent contentPane, JMenuBar menuBar, int width, int height ) {
        startGUI( contentPane, menuBar, new Dimension( width, height ) );
    }
    public void startGUI( JComponent contentPane, int width, int height ) {
        startGUI( contentPane, null, new Dimension( width, height ) );
    }
    public void startGUI( JComponent contentPane, Dimension size ) {
    	startGUI( contentPane, null, size );
    }
    public void startGUI( JComponent contentPane, JMenuBar menuBar, Dimension size ) {
    	prepareGUI(contentPane, menuBar);
        finishGUI(size);
    }
    public void prepareGUI(JComponent contentPane) {
    	prepareGUI(contentPane, null);
    }
    
    public enum DefaultCloseOperation {
    	DO_NOTHING_ON_CLOSE(JFrame.DO_NOTHING_ON_CLOSE),
    	DISPOSE_ON_CLOSE(JFrame.DISPOSE_ON_CLOSE),
    	EXIT_ON_CLOSE(JFrame.EXIT_ON_CLOSE);
    	
    	public int operationID;
		private DefaultCloseOperation(int operationID) {
			this.operationID = operationID;
    	}
    }
    public void prepareGUI(JComponent contentPane, JMenuBar menuBar) {
		setDefaultCloseOperation( defaultCloseOperation.operationID );
	    addWindowListener(this);
	    setContentPane( contentPane );
	    if (menuBar!=null) setJMenuBar(menuBar);
	}
    public void finishGUI() {
		finishGUI(null);
    }
    public void finishGUI(Dimension size) {
		pack();
        if (size!=null) setSize(size); else size = getSize();
        setLocationToScreenCenter(size, getGraphicsConfiguration().getBounds());
        setVisible( true );
	}
	private void setLocationToScreenCenter(Dimension size, Rectangle screen) {
		setLocation(
            (screen.width -size.width )/2+screen.x,
            (screen.height-size.height)/2+screen.y
        );
	}

    public void setSizeAsMinSize() {
        Dimension d = this.getSize();
        this.setMinimumSize(d);
    }

    public void limitSizeToFractionOfScreenSize(float d) {
    	Dimension size = getSize();
        Rectangle screen = getGraphicsConfiguration().getBounds();
        System.out.println("size: "+size);
        System.out.println("location: "+this.getLocation());
        System.out.println("location: "+this.getLocationOnScreen());
    	size.width  = Math.min( Math.round(screen.width *d), size.width );
    	size.height = Math.min( Math.round(screen.height*d), size.height);
        System.out.println("size: "+size);
        setSize(size);
		setLocationToScreenCenter(size, screen);
        System.out.println("location: "+this.getLocation());
        System.out.println("location: "+this.getLocationOnScreen());
	}
    
	@Override public void windowOpened(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowClosing(WindowEvent e) {
        if (closeListener!=null) closeListener.windowClosing(e);
    }

    public interface CloseListener {
        public void windowClosing(WindowEvent e);
    }

}
