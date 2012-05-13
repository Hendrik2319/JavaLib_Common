/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.schwarzbaer.gui;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author Hendrik
 */
public class StandardMainWindow extends JFrame implements WindowListener {
	private static final long serialVersionUID = -6515512014217265169L;
	
	private final CloseListener closeListener;
	private final boolean disposeOnClose;

    public StandardMainWindow(String title, CloseListener closeListener, boolean disposeOnClose ) throws HeadlessException {
        super(title);
        this.closeListener = closeListener;
		this.disposeOnClose = disposeOnClose;
    }
    public StandardMainWindow(String title, CloseListener closeListener ) throws HeadlessException { this(title,closeListener,false); }
    public StandardMainWindow(String title, boolean disposeOnClose      ) throws HeadlessException { this(title,null,disposeOnClose); }
    public StandardMainWindow(String title) throws HeadlessException { this(title,null); }
    public StandardMainWindow(            ) throws HeadlessException { this("",null); }

    public void startGUI( JComponent contentPane ) {
        startGUI( contentPane, null );
    }
    public void startGUI( JComponent contentPane, int width, int height ) {
        startGUI( contentPane, new Dimension( width, height ) );
    }
    public void startGUI( JComponent contentPane, Dimension size ) {
    	prepareGUI(contentPane);
        finishGUI(size);
    }
    public void prepareGUI(JComponent contentPane) {
		if (closeListener!=null) setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
	    if (disposeOnClose)      setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	    else                     setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	    addWindowListener(this);
	    setContentPane( contentPane );
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
    
	public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
        if (closeListener!=null) closeListener.windowClosing(e);
    }

    public interface CloseListener {
        public void windowClosing(WindowEvent e);
    }

}
