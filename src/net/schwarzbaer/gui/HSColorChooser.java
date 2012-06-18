package net.schwarzbaer.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.schwarzbaer.gui.ColorSlider.ColorChangeListener;


public final class HSColorChooser implements ActionListener, ColorChangeListener {

/**
 *
 * @author hscholtz
 */
	
	public static final StandardDialog.Position PARENT_CENTER   = StandardDialog.Position.PARENT_CENTER;
	public static final StandardDialog.Position LEFT_OF_PARENT  = StandardDialog.Position.LEFT_OF_PARENT;
	public static final StandardDialog.Position ABOVE_PARENT    = StandardDialog.Position.ABOVE_PARENT;
	public static final StandardDialog.Position RIGHT_OF_PARENT = StandardDialog.Position.RIGHT_OF_PARENT;
	public static final StandardDialog.Position BELOW_PARENT    = StandardDialog.Position.BELOW_PARENT;
	
	public static Color showDialog(Window parent, String title, Color color, StandardDialog.Position position) {
		HSColorChooser dialog = new HSColorChooser();
		dialog.createGUI(parent,title);
		return dialog.showDialog(color, position);
	}

	private StandardDialog dlgFenster;
	private JLabel newColorField;
	private JLabel oldColorField;
	private ColorSlider sliderR;
	private ColorSlider sliderG;
	private ColorSlider sliderB;
	private ColorSlider sliderH;
	private ColorSlider sliderS;
	private ColorSlider sliderV;
	private ColorSlider sliderDual;
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
		rgbPanel.add(createSliderPanel( "R",sliderR = new ColorSlider(ColorSlider.VERTICAL,Color.YELLOW,ColorSlider.COMP_RED,this) ));
		rgbPanel.add(createSliderPanel( "G",sliderG = new ColorSlider(ColorSlider.VERTICAL,Color.YELLOW,ColorSlider.COMP_GRN,this) ));
		rgbPanel.add(createSliderPanel( "B",sliderB = new ColorSlider(ColorSlider.VERTICAL,Color.YELLOW,ColorSlider.COMP_BLU,this) ));
		
		JPanel hsvPanel = new JPanel(new GridLayout(1,0,3,3));
		hsvPanel.setBorder(BorderFactory.createTitledBorder(""));
		hsvPanel.add(createSliderPanel( "H",sliderH = new ColorSlider(ColorSlider.VERTICAL,Color.YELLOW,ColorSlider.COMP_HUE,this) ));
		hsvPanel.add(createSliderPanel( "S",sliderS = new ColorSlider(ColorSlider.VERTICAL,Color.YELLOW,ColorSlider.COMP_SAT,this) ));
		hsvPanel.add(createSliderPanel( "B",sliderV = new ColorSlider(ColorSlider.VERTICAL,Color.YELLOW,ColorSlider.COMP_BRT,this) ));
		
		JPanel singleSliderPanel = new JPanel(new GridLayout(1,0,3,3));
		singleSliderPanel.add(rgbPanel);
		singleSliderPanel.add(hsvPanel);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		JPanel dualTypePanel = new JPanel(new GridLayout(1,0,3,3));
		dualTypePanel.add(GUI.createRadioButton("RG", "dualRG", this, buttonGroup, true, true));
		dualTypePanel.add(GUI.createRadioButton("GB", "dualGB", this, buttonGroup, true, true));
		dualTypePanel.add(GUI.createRadioButton("RB", "dualRB", this, buttonGroup, true, true));
		dualTypePanel.add(GUI.createRadioButton("HS", "dualHS", this, buttonGroup, true, true));
		dualTypePanel.add(GUI.createRadioButton("SB", "dualSB", this, buttonGroup, true, true));
		dualTypePanel.add(GUI.createRadioButton("HB", "dualHB", this, buttonGroup, true, true));
		
		JPanel dualSliderPanel = new JPanel(new BorderLayout(3,3));
//		dualSliderPanel.setBorder(BorderFactory.createTitledBorder(""));
		dualSliderPanel.add(GUI.createTitlePanel("", sliderDual = new ColorSlider(ColorSlider.DUAL,Color.YELLOW,ColorSlider.COMP_RED,ColorSlider.COMP_GRN,this)), BorderLayout.CENTER);
		dualSliderPanel.add(dualTypePanel, BorderLayout.SOUTH);
		
		JPanel examplePanel = new JPanel(new GridLayout(1,0,3,3));
		examplePanel.setBorder(BorderFactory.createTitledBorder(""));
		examplePanel.add(newColorField = new JLabel("   "));
		examplePanel.add(oldColorField = new JLabel("   "));
		newColorField.setOpaque(true);
		oldColorField.setOpaque(true);
		newColorField.setPreferredSize(new Dimension(30,15));
		oldColorField.setPreferredSize(new Dimension(30,15));
		
		JPanel contentPane = new JPanel(new BorderLayout(3,3));
		contentPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		contentPane.add(dualSliderPanel,BorderLayout.CENTER);
		contentPane.add(singleSliderPanel,BorderLayout.EAST);
		contentPane.add(GUI.createRightAlignedPanel(buttonPanel,examplePanel),BorderLayout.SOUTH);
		
		dlgFenster = new StandardDialog(parent, title);
		dlgFenster.createGUI(contentPane);
	}

	private JPanel createSliderPanel(String title, ColorSlider colorSlider) {
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
		if (e.getActionCommand().startsWith("dual")) {
			if ("dualRG".equals(e.getActionCommand())) sliderDual.setColorComps(ColorSlider.COMP_RED,ColorSlider.COMP_GRN); else
			if ("dualGB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorSlider.COMP_GRN,ColorSlider.COMP_BLU); else
			if ("dualRB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorSlider.COMP_RED,ColorSlider.COMP_BLU); else
			if ("dualHS".equals(e.getActionCommand())) sliderDual.setColorComps(ColorSlider.COMP_HUE,ColorSlider.COMP_SAT); else
			if ("dualSB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorSlider.COMP_SAT,ColorSlider.COMP_BRT); else
			if ("dualHB".equals(e.getActionCommand())) sliderDual.setColorComps(ColorSlider.COMP_HUE,ColorSlider.COMP_BRT);
			sliderDual.repaint(); 
			return;
		}
	}

	@Override
	public void colorChanged(Color color) {
		setColor(color);
	}

}