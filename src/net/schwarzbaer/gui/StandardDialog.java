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
	
	private Window parent; 
	
	public StandardDialog( Window parent, String title ) {
		this(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
	}
	
	public StandardDialog( Window parent, String title, ModalityType modality ) {
		super( parent, title, modality );
		this.parent = parent;
	}
    
    public void createGUI( JComponent contentPane ) {
    	createGUI( contentPane, null, null );
	}
    
    public void createGUI( JComponent contentPane, Position position ) {
    	createGUI( contentPane, position, null );
    }
    
    public void createGUI( JComponent contentPane, Dimension preferredSize ) {
    	createGUI( contentPane, null, preferredSize );
    }
    
    public void createGUI( JComponent contentPane, Position position, Dimension preferredSize ) {
        setContentPane( contentPane );
        if (preferredSize!=null) setPreferredSize(preferredSize);
        pack();
        if (position==null) setPosition(Position.PARENT_CENTER);
        else                setPosition(position);
        
    }

	private void setPosition(Position position) {
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

    public void showDialog(Position position) {
    	setPosition(position);
        setVisible( true );
    }

    public void showDialog() {
        setVisible( true );
    }

    public void closeDialog() {
        setVisible( false );
    }

	public static enum Position {
		PARENT_CENTER,
		LEFT_OF_PARENT,
		ABOVE_PARENT,
		RIGHT_OF_PARENT,
		BELOW_PARENT
	}
    
}
