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
@SuppressWarnings("serial")
public class StandardMainWindow extends JFrame implements WindowListener {

    private CloseListener closeListener = null;

    public StandardMainWindow(String title, CloseListener closeListener) throws HeadlessException {
        super(title);
        this.closeListener = closeListener;
    }
    public StandardMainWindow(String title) throws HeadlessException { super(title); }
    public StandardMainWindow(            ) throws HeadlessException { super(     ); }

    public void startGUI( JComponent contentPane ) {
        startGUI( contentPane, null );
    }
    public void startGUI( JComponent contentPane, int width, int height ) {
        startGUI( contentPane, new Dimension( width, height ) );
    }
    public void startGUI( JComponent contentPane, Dimension d ) {
        if (closeListener==null) setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        else                     setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        addWindowListener(this);
        setContentPane( contentPane );
        pack();
        Rectangle r = getGraphicsConfiguration().getBounds();
        if (d!=null) setSize(d); else d = getSize();
        setLocation(
            (r.width -d.width )/2+r.x,
            (r.height-d.height)/2+r.y
        );
        setVisible( true );
    }

    public void setSizeAsMinSize() {
        Dimension d = this.getSize();
        this.setMinimumSize(d);
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
