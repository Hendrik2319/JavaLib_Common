/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.schwarzbaer.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author hscholtz
 */
public class StandardDialog {

    private JDialog dlg;
    
    public void createGUI( Window parent, String title, JPanel contentPane ) {
        dlg = new JDialog( parent, title, Dialog.ModalityType.APPLICATION_MODAL );
        dlg.setContentPane( contentPane );
        dlg.pack();
        Rectangle r = parent.getBounds();
        Dimension d = dlg.getSize();
        dlg.setLocation(
            (r.width -d.width )/2+r.x,
            (r.height-d.height)/2+r.y
        );
    }

    public void setSizeAsMinSize() {
        Dimension d = dlg.getSize();
        dlg.setMinimumSize(d);
    }

    public void showDialog() {
        this.dlg.setVisible( true );
    }

    public void closeDialog() {
        this.dlg.setVisible( false );
    }
}
