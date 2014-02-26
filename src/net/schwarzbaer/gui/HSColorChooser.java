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

import net.schwarzbaer.gui.ColorSlider.ColorChangeListener;
import net.schwarzbaer.gui.ColorSlider.SliderType;


public final class HSColorChooser implements ActionListener, ColorChangeListener {
	
	public static final StandardDialog.Position PARENT_CENTER   = StandardDialog.Position.PARENT_CENTER;
	public static final StandardDialog.Position LEFT_OF_PARENT  = StandardDialog.Position.LEFT_OF_PARENT;
	public static final StandardDialog.Position ABOVE_PARENT    = StandardDialog.Position.ABOVE_PARENT;
	public static final StandardDialog.Position RIGHT_OF_PARENT = StandardDialog.Position.RIGHT_OF_PARENT;
	public static final StandardDialog.Position BELOW_PARENT    = StandardDialog.Position.BELOW_PARENT;
	
	private static Color[] userdefinedColors = new Color[] { null,null,null,null,null,null,null,null }; 
	private ColorButton[] userColorButtons; 
	
	public static Color showDialog(Window parent, String title, Color color, StandardDialog.Position position) {
		HSColorChooser dialog = new HSColorChooser();
		dialog.createGUI(parent,title);
		return dialog.showDialog(color, position);
	}

	private StandardDialog dlgFenster;
	private JLabel newColorField;
	private JLabel oldColorField;
	private ColorCompSlider sliderR;
	private ColorCompSlider sliderG;
	private ColorCompSlider sliderB;
	private ColorCompSlider sliderH;
	private ColorCompSlider sliderS;
	private ColorCompSlider sliderV;
	private ColorCompSlider sliderDual;
	private Color oldColor;
	private Color newColor;

	private Color showDialog(Color color, StandardDialog.Position position) {
		setColor(color);
		this.oldColor = color;
		oldColorField.setBackground(this.oldColor);
		dlgFenster.showDialog(position);
		return newColor;
	}

	private void setColor(Color color) {
		this.newColor = color;
		newColorField.setBackground(color);
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

	private void createGUI(Window parent, String title) {
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,0,3,3));
		buttonPanel.add(GUI.createButton("OK", "ok", this));
		buttonPanel.add(GUI.createButton("Abbrechen", "abbrechen", this));
		buttonPanel.add(GUI.createButton("Zurück", "zurücksetzen", this));
		
		JPanel rgbPanel = new JPanel(new GridLayout(1,0,3,3));
		rgbPanel.setBorder(BorderFactory.createTitledBorder(""));
		rgbPanel.add(createSliderPanel( "R",sliderR = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_RED,this) ));
		rgbPanel.add(createSliderPanel( "G",sliderG = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_GRN,this) ));
		rgbPanel.add(createSliderPanel( "B",sliderB = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_BLU,this) ));
		
		JPanel hsvPanel = new JPanel(new GridLayout(1,0,3,3));
		hsvPanel.setBorder(BorderFactory.createTitledBorder(""));
		hsvPanel.add(createSliderPanel( "H",sliderH = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_HUE,this) ));
		hsvPanel.add(createSliderPanel( "S",sliderS = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_SAT,this) ));
		hsvPanel.add(createSliderPanel( "B",sliderV = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,ColorCompSlider.COMP_BRT,this) ));
		
		JPanel singleSliderPanel = new JPanel(new GridLayout(1,0,3,3));
		singleSliderPanel.add(rgbPanel);
		singleSliderPanel.add(hsvPanel);
		
		ButtonGroup buttonGroup_panelType = new ButtonGroup();
		JPanel dualTypePanel = new JPanel(new GridLayout(1,0,3,3));
		dualTypePanel.add(GUI.createRadioButton("RG", "dualRG", this, buttonGroup_panelType, true, true));
		dualTypePanel.add(GUI.createRadioButton("GB", "dualGB", this, buttonGroup_panelType, true, true));
		dualTypePanel.add(GUI.createRadioButton("RB", "dualRB", this, buttonGroup_panelType, true, true));
		dualTypePanel.add(GUI.createRadioButton("HS", "dualHS", this, buttonGroup_panelType, true, true));
		dualTypePanel.add(GUI.createRadioButton("SB", "dualSB", this, buttonGroup_panelType, true, true));
		dualTypePanel.add(GUI.createRadioButton("HB", "dualHB", this, buttonGroup_panelType, true, true));
		
		JPanel dualSliderPanel = new JPanel(new BorderLayout(3,3));
//		dualSliderPanel.setBorder(BorderFactory.createTitledBorder(""));
		dualSliderPanel.add(GUI.createTitlePanel("", sliderDual = new ColorCompSlider(SliderType.DUAL,Color.YELLOW,ColorCompSlider.COMP_RED,ColorCompSlider.COMP_GRN,this)), BorderLayout.CENTER);
		dualSliderPanel.add(dualTypePanel, BorderLayout.SOUTH);
		
		JPanel examplePanel = new JPanel(new GridLayout(1,0,3,3));
		examplePanel.setBorder(BorderFactory.createTitledBorder(""));
		examplePanel.add(newColorField = new JLabel("   "));
		examplePanel.add(oldColorField = new JLabel("   "));
		newColorField.setOpaque(true);
		oldColorField.setOpaque(true);
		newColorField.setPreferredSize(new Dimension(30,15));
		oldColorField.setPreferredSize(new Dimension(30,15));
		
		userColorButtons = new ColorButton[userdefinedColors.length];
		ButtonGroup buttonGroup_colorList = new ButtonGroup();
		JPanel userColorListPanel = new JPanel(new GridLayout(1,0,3,3));
		//userColorListPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		for (int i=0; i<userdefinedColors.length; i++) {
			userColorButtons[i] = new ColorButton(userdefinedColors[i]);
			buttonGroup_colorList.add(userColorButtons[i]);
			userColorListPanel.add(userColorButtons[i]);
		}
		
		JPanel userColorButtonPanel = new JPanel(new GridLayout(1,0,3,3));
		userColorButtonPanel.add(GUI.createButton("set", "set user color", this));
		userColorButtonPanel.add(GUI.createButton("read", "read user color", this));
		
		JPanel userColorPanel = GUI.createLeftAlignedPanel(userColorButtonPanel,userColorListPanel,3);
		userColorPanel.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel lowerPanel = new JPanel(new GridLayout(0,1,3,3));
		lowerPanel.add(GUI.createRightAlignedPanel(buttonPanel,examplePanel));
		lowerPanel.add(userColorPanel);
		
		JPanel contentPane = new JPanel(new BorderLayout(3,3));
		contentPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		contentPane.add(dualSliderPanel,BorderLayout.CENTER);
		contentPane.add(singleSliderPanel,BorderLayout.EAST);
		contentPane.add(lowerPanel,BorderLayout.SOUTH);
		
		dlgFenster = new StandardDialog(parent, title);
		dlgFenster.createGUI(contentPane);
		dlgFenster.setSizeAsMinSize();
	}

	private JPanel createSliderPanel(String title, ColorCompSlider colorSlider) {
//		JPanel panel = new JPanel(new BorderLayout(3,3));
//		panel.add(new JLabel("+",JLabel.CENTER),BorderLayout.NORTH);
//		panel.add(new JLabel("-",JLabel.CENTER),BorderLayout.SOUTH);
//		panel.add(colorSlider,BorderLayout.CENTER);
//		return GUI.createTopAlignedPanel(new JLabel(title,JLabel.CENTER), panel, 3);
		return GUI.createTopAlignedPanel(new JLabel(title,JLabel.CENTER), colorSlider, 3);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("ok".equals(e.getActionCommand())) {
			dlgFenster.closeDialog();
			return;
		}
		if ("abbrechen".equals(e.getActionCommand())) {
			newColor = null;
			dlgFenster.closeDialog();
			return;
		}
		
		if ("zurücksetzen".equals(e.getActionCommand())) {
			setColor(oldColor);
			return;
		}
		if ("set user color".equals(e.getActionCommand())) {
			int i = getSelectedColorButton();
			if (i>=0) {
				userdefinedColors[i] = newColor;
				userColorButtons[i].setColor(newColor);
			}
			return;
		}
		if ("read user color".equals(e.getActionCommand())) {
			int i = getSelectedColorButton();
			if ( (i>=0) && (userdefinedColors[i]!=null) ) {
				setColor(userdefinedColors[i]);
				userColorButtons[i].setColor(newColor);
			}
			return;
		}
		if (e.getActionCommand().startsWith("dual")) {
			if ("dualRG".equals(e.getActionCommand())) sliderDual.setColorComps(ColorCompSlider.COMP_RED,ColorCompSlider.COMP_GRN); else
			if ("dualGB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorCompSlider.COMP_GRN,ColorCompSlider.COMP_BLU); else
			if ("dualRB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorCompSlider.COMP_RED,ColorCompSlider.COMP_BLU); else
			if ("dualHS".equals(e.getActionCommand())) sliderDual.setColorComps(ColorCompSlider.COMP_HUE,ColorCompSlider.COMP_SAT); else
			if ("dualSB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorCompSlider.COMP_SAT,ColorCompSlider.COMP_BRT); else
			if ("dualHB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorCompSlider.COMP_HUE,ColorCompSlider.COMP_BRT);
			sliderDual.repaint(); 
			return;
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
				this.realWidth = 5;
				this.realHeight = 5;
				this.fakeWidth = 5;
				this.fakeHeight = 5;
			}

			public void setColor(Color color) {
				this.color = color;
			}

			public void setMastersSize(int masterWidth, int masterHeight) {
				this.realWidth  = masterWidth -10;
				this.realHeight = masterHeight-10;
			}

			@Override public int getIconWidth () { return fakeWidth; }
			@Override public int getIconHeight() { return fakeHeight; }

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				int realX = x-(realWidth-fakeWidth)/2;
				int realY = y-(realHeight-fakeHeight)/2;
				if (color==null) {
					g.setColor(BORDER_COLOR);
					g.drawRect(realX, realY, realWidth, realHeight);
				} else {
					g.setColor(color);
					g.fillRect(realX, realY, realWidth, realHeight);
					g.setColor(BORDER_COLOR);
					g.drawRect(realX, realY, realWidth, realHeight);
				}
			}
			
		}
	}
}