
package net.schwarzbaer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;

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

    public static JButton createButton( String title, String commandStr, ActionListener actionListener ) {
        JButton button = new JButton( title );
        button.addActionListener(actionListener);
        button.setActionCommand( commandStr );
        return button;
    }

    public static JButton createButton( String title, String commandStr, ActionListener actionListener, Icon icon ) {
        JButton btn = createButton( title, commandStr, actionListener );
        btn.setIcon(icon);
        return btn;
	}

	public static JButton createButton(String title, String commandStr, ActionListener actionListener, boolean enabled) {
        JButton btn = createButton( title, commandStr, actionListener );
        btn.setEnabled(enabled);
        return btn;
	}

	public static JButton createButton( String title, String commandStr, ActionListener actionListener, String toolTipText ) {
	    JButton btn = createButton( title, commandStr, actionListener );
	    btn.setToolTipText( toolTipText );
	    return btn;
	}

	public static JToggleButton createToggleButton( String title, String commandStr, ActionListener actionListener ) {
    	JToggleButton button = new JToggleButton( title );
        button.addActionListener(actionListener);
        button.setActionCommand( commandStr );
        return button;
    }

	public static JRadioButton createRadioButton( String title, String commandStr, ActionListener actionListener, ButtonGroup buttonGroup, boolean isSelected, boolean isEnabled ) {
    	JRadioButton button = new JRadioButton( title );
        button.addActionListener(actionListener);
        button.setActionCommand( commandStr );
        button.setSelected(isSelected);
        button.setEnabled(isEnabled);
        buttonGroup.add(button);
        return button;
    }
	
	public static <E> JComboBox<E> createComboBox_Gen( ComboBoxModel<E> comboBoxModel, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox_Gen( new JComboBox<E>( comboBoxModel ), commandStr, enabled, actionListener);
    }

	public static <E> JComboBox<E> createComboBox_Gen( E[] items, int selected, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox_Gen( new JComboBox<E>( items ), selected, commandStr, enabled, actionListener);
    }
	
	private static <E> JComboBox<E> setComboBox_Gen(JComboBox<E> cmbBx, int selected, String commandStr, boolean enabled, ActionListener actionListener) {
		cmbBx.setSelectedIndex(selected);
		return setComboBox_Gen( cmbBx, commandStr, enabled, actionListener);
    }

	private static <E> JComboBox<E> setComboBox_Gen(JComboBox<E> cmbBx, String commandStr, boolean enabled, ActionListener actionListener) {
		cmbBx.setActionCommand(commandStr);
        cmbBx.addActionListener(actionListener);
        cmbBx.setEnabled(enabled);
        return cmbBx;
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static JComboBox createComboBox( ComboBoxModel comboBoxModel, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox( new JComboBox( comboBoxModel ), commandStr, enabled, actionListener);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static JComboBox createComboBox( Object[] items, int selected, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox( new JComboBox( items ), selected, commandStr, enabled, actionListener);
    }

	@SuppressWarnings("rawtypes")
	private static JComboBox setComboBox(JComboBox cmbBx, int selected, String commandStr, boolean enabled, ActionListener actionListener) {
		cmbBx.setSelectedIndex(selected);
		return setComboBox( cmbBx, commandStr, enabled, actionListener);
    }

	@SuppressWarnings("rawtypes")
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

    public static JPanel    createTopAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.NORTH); }
	public static JPanel createBottomAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.SOUTH); }
    public static JPanel  createRightAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.EAST); }
	public static JPanel   createLeftAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.WEST); }

    public static JPanel    createTopAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.NORTH); }
	public static JPanel createBottomAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.SOUTH); }
    public static JPanel  createRightAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.EAST); }
	public static JPanel   createLeftAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.WEST); }

    public static JPanel    createTopAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.NORTH, spacing); }
	public static JPanel createBottomAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.SOUTH, spacing); }
    public static JPanel  createRightAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.EAST,  spacing); }
	public static JPanel   createLeftAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.WEST,  spacing); }

	private static JPanel createAlignedPanel(Component comp, String layoutPosition) {
		return createAlignedPanel(comp,new JLabel(),layoutPosition);
	}
	private static JPanel createAlignedPanel(Component comp, Component center_comp, String layoutPosition) {
		return createAlignedPanel(comp,center_comp,layoutPosition,0);
	}
	private static JPanel createAlignedPanel(Component comp, Component center_comp, String layoutPosition, int spacing) {
		JPanel panel = new JPanel( new BorderLayout(spacing,spacing) );
		panel.add(center_comp, BorderLayout.CENTER);
		panel.add(comp, layoutPosition);
		return panel;
	}

	public static JPanel createLeftRightAlignedPanel(Component leftComp, Component rightComp) {
		JPanel panel = new JPanel( new BorderLayout() );
		panel.add(leftComp, BorderLayout.WEST);
		panel.add(new JLabel(), BorderLayout.CENTER);
		panel.add(rightComp, BorderLayout.EAST);
		return panel;
	}

	public static JPanel createTitlePanel(String title, Component comp) {
		JPanel panel = new JPanel( new GridLayout(1,0,3,3) );
		panel.setBorder(BorderFactory.createTitledBorder(title));
		panel.add(comp);
		return panel;
	}

	public static JTextField createOutputTextField() {
        JTextField textfield = new JTextField();
        textfield.setEditable(false);
        return textfield;
    }
    
    private static class TextFieldFocusActionListener implements FocusListener, ActionListener {
    	private JTextField textfield;
    	private String commandStr;
    	private ActionListener actionListener;
		private boolean fireActionPerformed;
    	
	    public TextFieldFocusActionListener(JTextField textfield, String commandStr, ActionListener actionListener) {
	    	this.textfield = textfield;
			this.commandStr = commandStr;
			this.actionListener = actionListener;
			fireActionPerformed = false;
		}
		@Override public synchronized void focusGained(FocusEvent e) {
			fireActionPerformed = true;
		}
		@Override public synchronized void focusLost(FocusEvent e) {
			if (fireActionPerformed) {
				fireActionPerformed = false;
				actionListener.actionPerformed( new ActionEvent( textfield,ActionEvent.ACTION_PERFORMED,commandStr ) );
			}
		}
		@Override public synchronized void actionPerformed(ActionEvent e) {
			if (fireActionPerformed) {
				fireActionPerformed = false;
				actionListener.actionPerformed(e);
			}
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
        JTextField textfield = new JTextField();
        if (focusListener==null) {
            TextFieldFocusActionListener listener = new TextFieldFocusActionListener(textfield,commandStr,actionListener);
        	focusListener  = listener;
        	actionListener = listener;
        }
        textfield.addFocusListener ( focusListener  );
        textfield.addActionListener( actionListener );
        textfield.setActionCommand( commandStr );
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
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setText(value);
        textfield.setColumns( columns );
        return textfield;
    }

//	public static class JTextField_HS extends JTextField {
//		private static final long serialVersionUID = -1107252015179183026L;
//		
//		private String commandStr;
//
//        public JTextField_HS() {
//            super();
//        }
//
//        public JTextField_HS( String str ) {
//            super(str);
//        }
//
//        @Override
//        public void setActionCommand(String commandStr) {
//            super.setActionCommand(commandStr);
//            this.commandStr = commandStr;
//        }
//
//        public String getActionCommand() {
//            return commandStr;
//        }
//    }

//    public static class JTextField_HS_FocusListener implements FocusListener {
//
//        private ActionListener actionListener;
//        private FocusActionFlag focusActionFlag;
//
//        public JTextField_HS_FocusListener( ActionListener actionListener ) {
//            this( actionListener, null );
//        }
//
//        public JTextField_HS_FocusListener( ActionListener actionListener, FocusActionFlag focusActionFlag ) {
//            this.actionListener = actionListener;
//            this.focusActionFlag = focusActionFlag;
//        }
//
//        @Override public void focusGained(FocusEvent e) {}
//        @Override public void focusLost(FocusEvent e) {
//            if ( (focusActionFlag!=null) && !focusActionFlag.isFocusActionAllowedNow()) return;
//            if ( e.getComponent() instanceof JTextField_HS ) {
//                JTextField_HS txtf = (JTextField_HS)e.getComponent();
//                actionListener.actionPerformed( new ActionEvent( txtf, ActionEvent.ACTION_PERFORMED, txtf.getActionCommand() ) );
//            }
//        }
//
//        public static interface FocusActionFlag {
//            public boolean isFocusActionAllowedNow();
//        }
//    }
    
	public static final int ALIGNMENT_TOP    = -1;
	public static final int ALIGNMENT_CENTER =  0;
	public static final int ALIGNMENT_BOTTOM =  1;
	public static final int ALIGNMENT_LEFT   = -1;
	public static final int ALIGNMENT_RIGHT  =  1;
	
	public static void drawString(Graphics g, String str, int x, int y, int hAlign, int vAlign) {
		Rectangle2D b = g.getFontMetrics().getStringBounds(str, g);
		switch (hAlign) {
		case ALIGNMENT_LEFT  : x -= (int)Math.round( b.getMinX()               ); break;
		case ALIGNMENT_CENTER: x -= (int)Math.round((b.getMinX()+b.getMaxX())/2); break;
		case ALIGNMENT_RIGHT : x -= (int)Math.round(             b.getMaxX()   ); break;
		}
		switch (vAlign) {
		case ALIGNMENT_TOP   : y -= (int)Math.round( b.getMinY()               ); break;
		case ALIGNMENT_CENTER: y -= (int)Math.round((b.getMinY()+b.getMaxY())/2); break;
		case ALIGNMENT_BOTTOM: y -= (int)Math.round(             b.getMaxY()   ); break;
		}
		g.drawString(str, x, y);
	}

	public static void listSystemPropertiesSorted(PrintStream out) {
		Properties properties = System.getProperties();
		Vector<Object> keySet = new Vector<Object>(properties.keySet());
		Collections.sort(keySet,new Comparator<Object>(){
			@Override public int compare(Object o1, Object o2) { return o1.toString().compareTo(o2.toString()); }
		});
		for (int i=0; i<keySet.size(); i++) {
			out.println(String.format("%s=%s", toString(keySet.get(i)),toString(properties.get(keySet.get(i)))));
		}
	}

	private static String toString(Object object) {
		return (object==null?"<null>":object.toString());
	}

	public static void makeAutoScroll(JScrollPane scrollPane) {
		new AutoScrollModel().makeAutoScroll(scrollPane);
	}
	
	public static JScrollPane createAutoScrollPanel(JTextArea output, boolean editable, int width, int height) {
		JScrollPane scrollPanel = createAutoScrollPanel(output,editable);
		scrollPanel.getViewport().setPreferredSize(new Dimension(width,height));
		return scrollPanel;
	}
	
	public static JScrollPane createAutoScrollPanel(JTextArea output, boolean editable) {
		output.setEditable(editable);
		return createAutoScrollPanel(output);
	}
	
	public static JScrollPane createAutoScrollPanel(JTextArea output) {
		
		JScrollPane scrollPane = new JScrollPane( output );
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		makeAutoScroll(scrollPane);
		
		return scrollPane; 
	}
	
	private static class AutoScrollModel extends DefaultBoundedRangeModel implements MouseListener, ActionListener {
		private static final long serialVersionUID = 583924172439445131L;
		
		private boolean autoScroll = true;
		private JPopupMenu contextMenu;
		private JScrollBar verticalScrollBar;

		public void makeAutoScroll(JScrollPane scrollPane) {
			JCheckBoxMenuItem item = new JCheckBoxMenuItem("autoscroll",autoScroll);
			item.addActionListener(this);
			item.setActionCommand("autoscroll");
			contextMenu = new JPopupMenu();
			contextMenu.add(item);
			
			this.verticalScrollBar = scrollPane.getVerticalScrollBar();
			verticalScrollBar.setModel(this);
			verticalScrollBar.addMouseListener(this);
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if ("autoscroll".equals(e.getActionCommand())) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem)e.getSource();
				setAutoScroll(source.isSelected());
			}
		}

		public void setAutoScroll(boolean autoScroll) {
			this.autoScroll = autoScroll;
			if (this.autoScroll) checkSetting();
		}

		@Override
		public void setRangeProperties( int newValue, int newExtent, int newMin, int newMax, boolean adjusting ) {
			super.setRangeProperties( newValue, newExtent, newMin, newMax, adjusting );
//			System.out.println( "min:"+newMin + " val:"+newValue + " val+ext:"+(newValue+newExtent) + " max:"+newMax + " adj:"+adjusting );
			
			if (autoScroll && !adjusting) checkSetting();
		}

		private void checkSetting() {
			int val = this.getValue();
			int ext = this.getExtent();
			int max = this.getMaximum();
			if (val != max - ext)
				super.setValue(max - ext);
		}

		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				contextMenu.show(verticalScrollBar, e.getX(),e.getY());
			}
		}

//		@Override
//		public void setValue(int val) {
//			super.setValue(val);
//			System.out.println( "val:"+val );
//		}
		
	}

	public static Icon getFileIcon(File file) {
		return FileSystemView.getFileSystemView().getSystemIcon( file );
	}
}
