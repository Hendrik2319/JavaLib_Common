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

/**
 *
 * @author hscholtz
 */
public class StandardDialog extends JDialog {
	private static final long serialVersionUID = -2236026007551538954L;

	private Window parent; 
	
	public StandardDialog( Window parent, String title ) {
		super( parent, title, Dialog.ModalityType.APPLICATION_MODAL );
		this.parent = parent;
	}
    
    public void createGUI( JComponent contentPane ) {
        setContentPane( contentPane );
        pack();
        Rectangle r = parent.getBounds();
        Dimension d = getSize();
        setLocation(
            (r.width -d.width )/2+r.x,
            (r.height-d.height)/2+r.y
        );
    }

    public void setSizeAsMinSize() {
        Dimension d = getSize();
        setMinimumSize(d);
    }

    public void showDialog() {
        setVisible( true );
    }

    public void closeDialog() {
        setVisible( false );
    }
}
