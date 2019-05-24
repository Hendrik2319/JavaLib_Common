package net.schwarzbaer.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.schwarzbaer.gui.ColorSlider.ColorChangeListener;
import net.schwarzbaer.gui.ColorSlider.SliderType;

public final class HSColorChooser {
	
	public static final StandardDialog.Position PARENT_CENTER   = StandardDialog.Position.PARENT_CENTER;
	public static final StandardDialog.Position LEFT_OF_PARENT  = StandardDialog.Position.LEFT_OF_PARENT;
	public static final StandardDialog.Position ABOVE_PARENT    = StandardDialog.Position.ABOVE_PARENT;
	public static final StandardDialog.Position RIGHT_OF_PARENT = StandardDialog.Position.RIGHT_OF_PARENT;
	public static final StandardDialog.Position BELOW_PARENT    = StandardDialog.Position.BELOW_PARENT;
	
	public static Color showDialog(Window parent, String title, Color color, StandardDialog.Position position) {
		StandardDialog dlgFenster = new StandardDialog(parent, title);
		MainPanel mainPanel = new MainPanel(color, dlgFenster, null);
		dlgFenster.createGUI(mainPanel);
		dlgFenster.setSizeAsMinSize();
		dlgFenster.showDialog(position);
		return mainPanel.newColor;
	}
	
	public static MainPanel createPanel(Color color, ColorReceiver colorReceiver) {
		return new MainPanel(color, null, colorReceiver);
	}
	
	public interface ColorReceiver {
		public void colorChanged(Color color);
	}

	public static class MainPanel extends JPanel implements ActionListener, ColorChangeListener {
		private static final long serialVersionUID = 1065328530214691959L;
		
		private Disabler<ActionCommands> disabler;
		private JLabel oldColorField;
		private JLabel colForegrField;
		private JLabel colBackgrFieldB;
		private JLabel colForegrFieldW;
		private JLabel colBackgrFieldW;
		private ColorCompSlider sliderR;
		private ColorCompSlider sliderG;
		private ColorCompSlider sliderB;
		private ColorCompSlider sliderH;
		private ColorCompSlider sliderS;
		private ColorCompSlider sliderV;
		private ColorCompSlider sliderDual;
		private Color oldColor;
		private Color newColor;
		
		private Color[] userdefinedColors = new Color[] { null,null,null,null,null,null,null,null }; 
		private ColorButton[] userColorButtons; 
		
		private ColorReceiver colorReceiver;
		private StandardDialog dialog;
	
		private MainPanel(Color color, StandardDialog dlgFenster, ColorReceiver colorReceiver) {
			super(new BorderLayout(3,3));
			this.dialog = dlgFenster;
			this.colorReceiver = colorReceiver;
			createPanel();
			setInitialColor(color);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			disabler.setEnableAll(enabled);
		}

		public void setInitialColor(Color color) {
			setColor(color);
			setOldColor(color);
		}
		
		private void setOldColor(Color color) {
			this.oldColor = color;
			oldColorField.setBackground(color);
		}
	
		private void setColor(Color color) {
			if (colorReceiver!=null) colorReceiver.colorChanged(color);
			this.newColor = color;
			colForegrField.setForeground(color);
			colForegrFieldW.setForeground(color);
			colBackgrFieldB.setBackground(color);
			colBackgrFieldW.setBackground(color);
			int blue  = color.getBlue();
			int green = color.getGreen();
			int red   = color.getRed();
			float[] hsb = Color.RGBtoHSB(red, green, blue, null);
			sliderR.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			sliderG.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			sliderB.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			sliderH.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			sliderS.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			sliderV.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			sliderDual.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
		}
		
		private static enum ActionCommands {
			other,
			SetDual2RG, SetDual2GB, SetDual2RB,
			SetDual2HS, SetDual2SB, SetDual2HB,
			Ok, Cancel, ResetColor, SetUserColor, ReadUserColor,
		}
		
		private void createPanel() {
			disabler = new Disabler<ActionCommands>();
			disabler.setCareFor(ActionCommands.values());
			
			JPanel buttonPanel = new JPanel(new GridLayout(1,0,3,3));
			if (dialog!=null) {
				buttonPanel.add(GUI.createButton("OK", ActionCommands.Ok, disabler, this));
				buttonPanel.add(GUI.createButton("Cancel", ActionCommands.Cancel, disabler, this));
			}
			buttonPanel.add(GUI.createButton("Reset", ActionCommands.ResetColor, disabler, this));
			
			JPanel rgbPanel = new JPanel(new GridLayout(1,0,3,3));
			rgbPanel.setBorder(BorderFactory.createTitledBorder(""));
			rgbPanel.add(createSliderPanel( "R",sliderR = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_RED,this) ));
			rgbPanel.add(createSliderPanel( "G",sliderG = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_GRN,this) ));
			rgbPanel.add(createSliderPanel( "B",sliderB = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_BLU,this) ));
			
			JPanel hsvPanel = new JPanel(new GridLayout(1,0,3,3));
			hsvPanel.setBorder(BorderFactory.createTitledBorder(""));
			hsvPanel.add(createSliderPanel( "H",sliderH = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_HUE,this) ));
			hsvPanel.add(createSliderPanel( "S",sliderS = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_SAT,this) ));
			hsvPanel.add(createSliderPanel( "V",sliderV = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_BRT,this) ));
			
			JPanel singleSliderPanel = new JPanel(new GridLayout(1,0,3,3));
			singleSliderPanel.add(rgbPanel);
			singleSliderPanel.add(hsvPanel);
			
			ButtonGroup buttonGroup_panelType = new ButtonGroup();
			JPanel dualTypePanel = new JPanel(new GridLayout(1,0,3,3));
			dualTypePanel.add(GUI.createRadioButton("RG", ActionCommands.SetDual2RG, disabler, this, buttonGroup_panelType, true, true));
			dualTypePanel.add(GUI.createRadioButton("GB", ActionCommands.SetDual2GB, disabler, this, buttonGroup_panelType, true, true));
			dualTypePanel.add(GUI.createRadioButton("RB", ActionCommands.SetDual2RB, disabler, this, buttonGroup_panelType, true, true));
			dualTypePanel.add(GUI.createRadioButton("HS", ActionCommands.SetDual2HS, disabler, this, buttonGroup_panelType, true, true));
			dualTypePanel.add(GUI.createRadioButton("SB", ActionCommands.SetDual2SB, disabler, this, buttonGroup_panelType, true, true));
			dualTypePanel.add(GUI.createRadioButton("HB", ActionCommands.SetDual2HB, disabler, this, buttonGroup_panelType, true, true));
			
			JPanel dualSliderPanel = new JPanel(new BorderLayout(3,3));
			dualSliderPanel.setBorder(BorderFactory.createTitledBorder(""));
			dualSliderPanel.add(sliderDual = new ColorCompSlider(SliderType.DUAL,Color.YELLOW,ColorCompSlider.COMP_RED,ColorCompSlider.COMP_GRN,this), BorderLayout.CENTER);
			dualSliderPanel.add(dualTypePanel, BorderLayout.SOUTH);
			disabler.add(ActionCommands.other, sliderDual);
			
			userColorButtons = new ColorButton[userdefinedColors.length];
			ButtonGroup buttonGroup_colorList = new ButtonGroup();
			JPanel userColorListPanel = new JPanel(new GridLayout(1,0,3,3));
			//userColorListPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			Dimension prefSize = new Dimension(20,20);
			for (int i=0; i<userdefinedColors.length; i++) {
				userColorListPanel.add( userColorButtons[i] = createColorButton(userdefinedColors[i], buttonGroup_colorList, prefSize) );
			}
			
			JPanel userColorSetButtonPanel = new JPanel(new GridLayout(1,0,3,3));
			userColorSetButtonPanel.add(GUI.createButton("set" , ActionCommands.SetUserColor , disabler, this));
			userColorSetButtonPanel.add(GUI.createButton("read", ActionCommands.ReadUserColor, disabler, this));
			
			JPanel userColorPanel = GUI.createLeftAlignedPanel(userColorSetButtonPanel,userColorListPanel,3);
			userColorPanel.setBorder(BorderFactory.createTitledBorder(""));
			
			oldColorField = new JLabel("   ");
			oldColorField.setBorder(BorderFactory.createTitledBorder(""));
			oldColorField.setOpaque(true);
			oldColorField.setPreferredSize(new Dimension(30,15));
			
			JPanel examplePanel = new JPanel(new GridLayout(1,0,3,3));
			examplePanel.setBorder(BorderFactory.createTitledBorder(""));
			examplePanel.add(colForegrField  = new JLabel("Text",SwingConstants.CENTER)); colForegrField .setOpaque(false);
			examplePanel.add(colForegrFieldW = new JLabel("Text",SwingConstants.CENTER)); colForegrFieldW.setOpaque(true ); colForegrFieldW.setBackground(Color.WHITE);
			examplePanel.add(colBackgrFieldB = new JLabel("Text",SwingConstants.CENTER)); colBackgrFieldB.setOpaque(true );
			examplePanel.add(colBackgrFieldW = new JLabel("Text",SwingConstants.CENTER)); colBackgrFieldW.setOpaque(true ); colBackgrFieldW.setForeground(Color.WHITE);
			
			JPanel lowerButtonPanel = new JPanel( new BorderLayout(3,3) );
			lowerButtonPanel.add(oldColorField, BorderLayout.WEST);
			lowerButtonPanel.add(examplePanel, BorderLayout.CENTER);
			lowerButtonPanel.add(buttonPanel, BorderLayout.EAST);
			
			JPanel lowerPanel = new JPanel(new GridLayout(0,1,3,3));
			lowerPanel.add(userColorPanel);
			lowerPanel.add(lowerButtonPanel);
			
			setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
			add(dualSliderPanel,BorderLayout.CENTER);
			add(singleSliderPanel,BorderLayout.EAST);
			add(lowerPanel,BorderLayout.SOUTH);
		}

		private ColorButton createColorButton(Color color, ButtonGroup buttonGroup, Dimension prefSize) {
			ColorButton colorButton = new ColorButton(color);
			colorButton.setPreferredSize(prefSize);
			buttonGroup.add(colorButton);
			disabler.add(ActionCommands.other, colorButton);
			return colorButton;
		}
	
		private JPanel createSliderPanel(String title, ColorCompSlider colorSlider) {
	//		JPanel panel = new JPanel(new BorderLayout(3,3));
	//		panel.add(new JLabel("+",JLabel.CENTER),BorderLayout.NORTH);
	//		panel.add(new JLabel("-",JLabel.CENTER),BorderLayout.SOUTH);
	//		panel.add(colorSlider,BorderLayout.CENTER);
	//		return GUI.createTopAlignedPanel(new JLabel(title,JLabel.CENTER), panel, 3);
			disabler.add(ActionCommands.other, colorSlider);
			return GUI.createTopAlignedPanel(new JLabel(title,JLabel.CENTER), colorSlider, 3);
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			ActionCommands actionCommand;
			try { actionCommand = ActionCommands.valueOf(e.getActionCommand()); }
			catch (Exception e1) { e1.printStackTrace(); return; }
			
			switch(actionCommand) {
			case Ok:
				if (dialog!=null) {
					dialog.closeDialog();
				}
				break;
			case Cancel:
				if (dialog!=null) {
					newColor = null;
					dialog.closeDialog();
				}
				break;
			case ResetColor:
				setColor(oldColor);
				break;
			case SetUserColor: {
				int i = getSelectedColorButton();
				if (i>=0) {
					userdefinedColors[i] = newColor;
					userColorButtons[i].setColor(newColor);
				}
			} break;
			case ReadUserColor: {
				int i = getSelectedColorButton();
				if ( (i>=0) && (userdefinedColors[i]!=null) ) {
					setColor(userdefinedColors[i]);
					userColorButtons[i].setColor(newColor);
				}
			} break;
			case SetDual2RG: sliderDual.setColorComps(ColorCompSlider.COMP_RED,ColorCompSlider.COMP_GRN); sliderDual.repaint(); break;
			case SetDual2GB: sliderDual.setColorComps(ColorCompSlider.COMP_GRN,ColorCompSlider.COMP_BLU); sliderDual.repaint(); break;
			case SetDual2RB: sliderDual.setColorComps(ColorCompSlider.COMP_RED,ColorCompSlider.COMP_BLU); sliderDual.repaint(); break;
			case SetDual2HS: sliderDual.setColorComps(ColorCompSlider.COMP_HUE,ColorCompSlider.COMP_SAT); sliderDual.repaint(); break;
			case SetDual2SB: sliderDual.setColorComps(ColorCompSlider.COMP_SAT,ColorCompSlider.COMP_BRT); sliderDual.repaint(); break;
			case SetDual2HB: sliderDual.setColorComps(ColorCompSlider.COMP_HUE,ColorCompSlider.COMP_BRT); sliderDual.repaint(); break;
			
			case other:break;
			}
		}
	
		private int getSelectedColorButton() {
			for (int i=0; i<userColorButtons.length; i++) {
				if (userColorButtons[i].isSelected()) return i;
			}
			return -1;
		}
	
		@Override
		public void colorChanged(Color color, float f) {
			setColor(color);
		}
	
		@Override
		public void colorChanged(Color color, float fH, float fV) {
			setColor(color);
		}
	}
	
	private static class ColorButton extends JToggleButton {
		private static final long serialVersionUID = 7759027446832437901L;
		
		private ColorButtonIcon icon;

		public ColorButton(Color color) {
			icon = new ColorButtonIcon(color);
			setIcon(icon);
		}
		
		public void setColor(Color color) {
			icon.setColor(color);
			repaint();
		}

		@Override public void setBounds(int x, int y, int width, int height) { icon.setMastersSize(  width,   height); super.setBounds(x, y, width, height); }
		@Override public void setBounds(Rectangle r                        ) { icon.setMastersSize(r.width, r.height); super.setBounds(r                  ); }
		@Override public void setSize  (Dimension d                        ) { icon.setMastersSize(d.width, d.height); super.setSize  (d                  ); }
		@Override public void setSize  (              int width, int height) { icon.setMastersSize(  width,   height); super.setSize  (      width, height); }

		private static class ColorButtonIcon implements Icon {

			private static final Color BORDER_COLOR = new Color(128,128,128);
			private int fakeWidth;
			private int fakeHeight;
			private int realWidth;
			private int realHeight;
			private Color color;

			public ColorButtonIcon(Color color) {
				this.color = color;
				this.realWidth = 10;
				this.realHeight = 10;
				this.fakeWidth = 10;
				this.fakeHeight = 10;
			}

			public void setColor(Color color) {
				this.color = color;
			}

			public void setMastersSize(int masterWidth, int masterHeight) {
				this.realWidth  = masterWidth -8;
				this.realHeight = masterHeight-8;
			}

			@Override public int getIconWidth () { return fakeWidth; }
			@Override public int getIconHeight() { return fakeHeight; }

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				int realX = x+(fakeWidth -realWidth )/2;
				int realY = y+(fakeHeight-realHeight)/2;
				if (color != null) {
					g.setColor(color);
					g.fillRect(realX, realY, realWidth, realHeight);
				}
				g.setColor(BORDER_COLOR);
				g.drawRect(realX, realY, realWidth-1, realHeight-1);
			}
			
		}
	}
}