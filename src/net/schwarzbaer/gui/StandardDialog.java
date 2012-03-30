/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.schwarzbaer.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JDialog;

public class StandardDialog extends JDialog {
	private static final long serialVersionUID = -2236026007551538954L;

	public static final int PARENT_CENTER   = 0;
	public static final int LEFT_OF_PARENT  = 1;
	public static final int ABOVE_PARENT    = 2;
	public static final int RIGHT_OF_PARENT = 3;
	public static final int BELOW_PARENT    = 4;
	
	private Window parent; 
	
	public StandardDialog( Window parent, String title ) {
		this(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
	}
	
	public StandardDialog( Window parent, String title, ModalityType modality ) {
		super( parent, title, modality );
		this.parent = parent;
	}
    
    public void createGUI( JComponent contentPane ) {
    	createGUI( contentPane, PARENT_CENTER );
	}
    
    public void createGUI( JComponent contentPane, int position ) {
        setContentPane( contentPane );
        pack();
        setPosition(position);
    }

	private void setPosition(int position) {
		Rectangle p = parent.getBounds();
        Dimension d = getSize();
        int dist = 3;
        switch (position) {
        case LEFT_OF_PARENT:
            if (p.height>d.height) this.setSize(d.width, p.height);
            setLocation( p.x-d.width-dist, p.y );
            break;
        case RIGHT_OF_PARENT:
            if (p.height>d.height) this.setSize(d.width, p.height);
            setLocation( p.x+p.width+dist, p.y );
            break;
        case ABOVE_PARENT:
            if (p.width>d.width) this.setSize(p.width, d.height);
            setLocation( p.x, p.y-d.height-dist );
            break;
        case BELOW_PARENT:
            if (p.width>d.width) this.setSize(p.width, d.height);
            setLocation( p.x, p.y+p.height+dist );
            break;
        case PARENT_CENTER:
        default:
            setLocation(
                    (p.width -d.width )/2+p.x,
                    (p.height-d.height)/2+p.y
                );
        }
	}

    public void setSizeAsMinSize() {
        Dimension d = getSize();
        setMinimumSize(d);
    }

    public void showDialog(int position) {
    	setPosition(position);
        setVisible( true );
    }

    public void showDialog() {
        setVisible( true );
    }

    public void closeDialog() {
        setVisible( false );
    }
}
