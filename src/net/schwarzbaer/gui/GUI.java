
package net.schwarzbaer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * 
 * @author Hendrik
 */
public class GUI {

	public static void setSystemLookAndFeel() {
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
		catch (ClassNotFoundException e) {}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (UnsupportedLookAndFeelException e) {}
	}
	
    public static void addButtonToPanelAndButtonGroup( JPanel buttonPanel, ButtonGroup buttonGroup, AbstractButton btn ) {
    	buttonPanel.add(btn);
    	buttonGroup.add(btn);
    }

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
    
    public static JMenuItem createMenuItem( String title, String commandStr, ActionListener actionListener, boolean enabled, String keyStrokeStr ) {
    	JMenuItem menuItem = createMenuItem(title, commandStr, actionListener, enabled);
    	menuItem.setAccelerator( KeyStroke.getKeyStroke( keyStrokeStr ) );
    	return menuItem;
    }
    
    public static JMenuItem createMenuItem( String title, String commandStr, ActionListener actionListener, boolean enabled ) {
    	JMenuItem menuItem = new JMenuItem( title );
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand( commandStr );
        menuItem.setEnabled(enabled);
        return menuItem;
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

    public static JComboBox createComboBox( ComboBoxModel comboBoxModel, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox( new JComboBox( comboBoxModel ), commandStr, enabled, actionListener);
    }

    public static JComboBox createComboBox( String[] items, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox( new JComboBox( items ), commandStr, enabled, actionListener);
    }

	private static JComboBox setComboBox(JComboBox cmbBx, String commandStr, boolean enabled, ActionListener actionListener) {
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
    
    private static class TextFieldFocusListener implements FocusListener {
    	private JTextField textfield;
    	private String commandStr;
    	private ActionListener actionListener;
    	
	    public TextFieldFocusListener(JTextField textfield, String commandStr, ActionListener actionListener) {
	    	this.textfield = textfield;
			this.commandStr = commandStr;
			this.actionListener = actionListener;
		}
		@Override public void focusGained(FocusEvent e) {}
		@Override public void focusLost(FocusEvent e) {
			actionListener.actionPerformed( new ActionEvent( textfield,ActionEvent.ACTION_PERFORMED,commandStr ) );
		}

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
        if (focusListener!=null)
        	textfield.addFocusListener( focusListener );
        else
        	textfield.addFocusListener( new TextFieldFocusListener(textfield,commandStr,actionListener) );
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
