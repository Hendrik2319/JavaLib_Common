
package net.schwarzbaer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

/**
 * 
 * @author Hendrik
 */
public class GUI {

    public static void addLabelAndField( JPanel labelPanel, JPanel fieldPanel, String label, JComponent field ) {
        labelPanel.add( new JLabel(label) );
        fieldPanel.add( field );
    }
    
    public static JPanel createLabelAndFieldPanel( String labelStr, Component comp ) {
        JPanel panel = new JPanel( new BorderLayout( 3,3 ) );
        panel.add( new JLabel( labelStr ), BorderLayout.WEST );
        panel.add( comp, BorderLayout.CENTER );
        return panel;
    }

    public static JMenu createMenu( String title, int mnemonic, boolean enabled ) {
        JMenu menu = new JMenu( title );
        menu.setMnemonic( mnemonic );
        menu.setEnabled( enabled );
        return menu;
    }

    public static JMenuItem createMenuItem(String title, String commandStr, int mnemonic, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem( title );
        menuItem.setActionCommand(commandStr);
        menuItem.addActionListener( actionListener );
        menuItem.setMnemonic( mnemonic );
        return menuItem;
    }

    public static JMenuItem createMenuItem(String title, String commandStr, int mnemonic, ActionListener actionListener, int accKey, int accMask ) {
        JMenuItem menuItem = createMenuItem( title, commandStr, mnemonic, actionListener );
        menuItem.setAccelerator( KeyStroke.getKeyStroke( accKey, accMask ) );
        return menuItem;
    }

    public static JButton createButton( String title, String commandStr, ActionListener actionListener, String toolTipText ) {
        JButton btn = createButton( title, commandStr, actionListener );
        btn.setToolTipText( toolTipText );
        return btn;
    }

    public static JButton createButton( String title, String commandStr, ActionListener actionListener ) {
        JButton button = new JButton( title );
        button.addActionListener(actionListener);
        button.setActionCommand( commandStr );
        return button;
    }

    public static JToggleButton createToggleButton( String title, String commandStr, ActionListener actionListener ) {
    	JToggleButton button = new JToggleButton( title );
        button.addActionListener(actionListener);
        button.setActionCommand( commandStr );
        return button;
    }

    public static JComboBox createComboBox( String[] items, String commandStr, boolean enabled, ActionListener actionListener ) {
        JComboBox cmbBx = new JComboBox( items );
        cmbBx.setActionCommand(commandStr);
        cmbBx.addActionListener(actionListener);
        cmbBx.setEnabled(enabled);
        return cmbBx;
    }

    public static JCheckBox createCheckBox( String title, boolean preselected, String commandStr, int alignment, boolean enabled, ActionListener actionListener ) {
        JCheckBox checkBox = new JCheckBox(title,preselected);
        checkBox.setActionCommand( commandStr );
        checkBox.addActionListener( actionListener );
        checkBox.setHorizontalTextPosition( alignment );
        checkBox.setEnabled(enabled);
        return checkBox;
    }

    public static JTextField createOutputTextField() {
        JTextField textfield = new JTextField();
        textfield.setEditable(false);
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener ) {
        JTextField textfield = new JTextField();
        textfield.addActionListener( actionListener );
        textfield.setActionCommand( commandStr );
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value ) {
        JTextField textfield = createTextField( commandStr, actionListener );
        textfield.setText(value);
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, int columns ) {
        JTextField textfield = createTextField( commandStr, actionListener, value );
        textfield.setColumns(columns);
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener,               boolean editable,              FocusListener focusListener ) {
        JTextField_HS textfield = new JTextField_HS();
        textfield.addActionListener( actionListener );
        textfield.setActionCommand( commandStr );
        if (focusListener!=null) textfield.addFocusListener( focusListener );
        textfield.setEditable(editable);
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, boolean editable,              FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setText(value);
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener,               boolean editable, int columns, FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setColumns( columns );
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, boolean editable, int columns, FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, columns, focusListener );
        textfield.setText(value);
        return textfield;
    }

    @SuppressWarnings("serial")
	public static class JTextField_HS extends JTextField {

        private String commandStr;

        public JTextField_HS() {
            super();
        }

        public JTextField_HS( String str ) {
            super(str);
        }

        @Override
        public void setActionCommand(String commandStr) {
            super.setActionCommand(commandStr);
            this.commandStr = commandStr;
        }

        public String getActionCommand() {
            return commandStr;
        }
    }

    public static class JTextField_HS_FocusListener implements FocusListener {

        private ActionListener actionListener;
        private FocusActionFlag focusActionFlag;

        public JTextField_HS_FocusListener( ActionListener actionListener ) {
            this( actionListener, null );
        }

        public JTextField_HS_FocusListener( ActionListener actionListener, FocusActionFlag focusActionFlag ) {
            this.actionListener = actionListener;
            this.focusActionFlag = focusActionFlag;
        }

        public void focusGained(FocusEvent e) {}
        public void focusLost(FocusEvent e) {
            if ( (focusActionFlag!=null) && !focusActionFlag.isFocusActionAllowedNow()) return;
            if ( e.getComponent() instanceof JTextField_HS ) {
                JTextField_HS txtf = (JTextField_HS)e.getComponent();
                actionListener.actionPerformed( new ActionEvent( txtf, ActionEvent.ACTION_PERFORMED, txtf.getActionCommand() ) );
            }
        }

        public static interface FocusActionFlag {
            public boolean isFocusActionAllowedNow();
        }
    }
}
