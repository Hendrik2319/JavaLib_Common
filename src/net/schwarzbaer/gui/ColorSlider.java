package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public final class ColorSlider extends Canvas implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -9018785440106166371L;
	public static final int VERTICAL   = 1; 
	public static final int HORIZONTAL = 2; 
	public static final int DUAL       = 3;
	public static final int COMP_RED = 1;
	public static final int COMP_GRN = 2;
	public static final int COMP_BLU = 3;
	public static final int COMP_HUE = 4;
	public static final int COMP_SAT = 5;
	public static final int COMP_BRT = 6;
	
	private final ColorChangeListener colorChangeListener;
	private final int type;
	private int colorCompH; 
	private int colorCompV;
	
	private int colorRED;
	private int colorGRN;
	private int colorBLU;
	private float colorHUE;
	private float colorSAT;
	private float colorBRT;
	public ColorSlider(int type, Color color, int colorComp, ColorChangeListener colorChangeListener) {
		this(type,color,colorComp, colorComp, colorChangeListener);
	}

	public ColorSlider(int type, Color color, int colorCompH, int colorCompV, ColorChangeListener colorChangeListener) {
		switch (type) {
		case HORIZONTAL: setPreferredSize(128, 20); break;
		case DUAL      : setPreferredSize(128, 128); break;
		case VERTICAL  :
		default: setPreferredSize(20, 128);
			type = VERTICAL;
			break;
		}
		this.type = type;
		setColor(color);
		setColorComps(colorCompH, colorCompV);
		this.colorChangeListener = colorChangeListener;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void setColorComps(int colorCompH, int colorCompV) {
		this.colorCompH = colorCompH;
		this.colorCompV = colorCompV;
	}

	@Override
	protected void paintCanvas(Graphics g, int width, int height) {
		switch (type) {
		case VERTICAL  : paintV(g,width,height); break;
		case HORIZONTAL: paintH(g,width,height); break;
		case DUAL      : paintD(g,width,height); break;
		default:
			g.setColor(Color.GREEN);
			g.fillRect(0, 0, width, height);
		}
	}

	private void paintV(Graphics g, int width, int height) {
		for (int y=0; y<height; y++) {
			g.setColor(getColor(colorCompV,height-1,y,0,null,null));
			g.drawLine(3,y,width-4,y);
		}
		int y = Math.round((1-getValue(colorCompV))*(height-1));
		g.setColor(Color.BLACK);
		g.drawLine(0,y,width-1,y);
	}

	private void paintH(Graphics g, int width, int height) {
		for (int x=0; x<width; x++) {
			g.setColor(getColor(colorCompH,0,x,width-1,null,null));
			g.drawLine(x,3,x,height-4);
		}
		int x = Math.round(getValue(colorCompH)*(width-1));
		g.setColor(Color.BLACK);
		g.drawLine(x,0,x,height-1);
	}

	private void paintD(Graphics g, int width, int height) {
		float[] hsb = new float[3];  
		for (int x=3; x<width-3; x++) {
			Color colorV = getColor(colorCompH,3,x,width-4,null,null);
			Color.RGBtoHSB(colorV.getRed(), colorV.getGreen(), colorV.getBlue(), hsb);
			for (int y=3; y<height-3; y++) {
				g.setColor(getColor(colorCompV,height-4,y,3,colorV,hsb));
				g.drawLine(x,y,x,y);
			}
		}
		int x = Math.round(   getValue(colorCompH) *(width -7))+3;
		int y = Math.round((1-getValue(colorCompV))*(height-7))+3;
		g.setColor(Color.BLACK);
//		g.drawOval(arg0, arg1, arg2, arg3)
		g.drawOval(x-3, y-3, 6, 6);
	}

	private Color getColor(int colorComp, int minV, int v, int maxV, Color baseColor, float[] hsb) {
		float f = (v-minV)/(float)(maxV-minV);
		if ( (colorComp==COMP_RED) || (colorComp==COMP_GRN) || (colorComp==COMP_BLU) ) {
			int RGBval = Math.round(f*255);
			if (RGBval>255) RGBval=255;
			
			if (baseColor!=null)
				switch (colorComp) {
				case COMP_RED: return new Color(     RGBval       ,baseColor.getGreen(),baseColor.getBlue());
				case COMP_GRN: return new Color(baseColor.getRed(),       RGBval       ,baseColor.getBlue());
				case COMP_BLU: return new Color(baseColor.getRed(),baseColor.getGreen(),      RGBval       );
				}
			else
				switch (colorComp) {
				case COMP_RED: return new Color( RGBval ,colorGRN,colorBLU);
				case COMP_GRN: return new Color(colorRED, RGBval ,colorBLU);
				case COMP_BLU: return new Color(colorRED,colorGRN, RGBval );
				}
		} else {
			if (hsb!=null) {
				switch (colorComp) {
				case COMP_HUE: return Color.getHSBColor(  f   ,hsb[1],hsb[2]);
				case COMP_SAT: return Color.getHSBColor(hsb[0],  f   ,hsb[2]);
				case COMP_BRT: return Color.getHSBColor(hsb[0],hsb[1],  f   );
				}
			} else
				switch (colorComp) {
				case COMP_HUE: return Color.getHSBColor(   f    , colorSAT, colorBRT);
				case COMP_SAT: return Color.getHSBColor(colorHUE,    f    , colorBRT);
				case COMP_BRT: return Color.getHSBColor(colorHUE, colorSAT,    f    );
				}
		}
		return null;
	}
	
	private float getValue(int colorComp) {
		switch (colorComp) {
		case COMP_RED: return (colorRED/255f);
		case COMP_GRN: return (colorGRN/255f);
		case COMP_BLU: return (colorBLU/255f);
		case COMP_HUE: return colorHUE;
		case COMP_SAT: return colorSAT;
		case COMP_BRT: return colorBRT;
		}
		return Float.NaN;
	}
	private void setValue( int colorComp, float f ) {
		switch (colorComp) {
		case COMP_RED: colorRED = Math.round(f*255); updateHSB(); break;
		case COMP_GRN: colorGRN = Math.round(f*255); updateHSB(); break;
		case COMP_BLU: colorBLU = Math.round(f*255); updateHSB(); break;
		case COMP_HUE: colorHUE = f; updateRGB(); break;
		case COMP_SAT: colorSAT = f; updateRGB(); break;
		case COMP_BRT: colorBRT = f; updateRGB(); break;
		}
	}
	
	public void setValues(int red, int green, int blue, float h, float s, float b) {
		setColor(red, green, blue, h, s, b);
		repaint();
	}

	public void setColor(int red, int green, int blue, float h, float s,
			float b) {
		this.colorRED = red;
		this.colorGRN = green;
		this.colorBLU = blue;
		this.colorHUE = h;
		this.colorSAT = s;
		this.colorBRT = b;
	}

	public void setColor(Color color) {
		this.colorRED = color.getRed();
		this.colorGRN = color.getGreen();
		this.colorBLU = color.getBlue();
		updateHSB();
	}

	private void updateHSB() {
		float[] hsb = Color.RGBtoHSB(colorRED, colorGRN, colorBLU, null);
		this.colorHUE = hsb[0];
		this.colorSAT = hsb[1];
		this.colorBRT = hsb[2];
	}
	
	private void updateRGB() {
		int rgb = Color.HSBtoRGB(colorHUE, colorSAT, colorBRT);
		this.colorRED = (rgb>>16)&255;
		this.colorGRN = (rgb>> 8)&255;
		this.colorBLU = (rgb>> 0)&255;
	}

	private Color getColor() {
		return new Color(colorRED,colorGRN,colorBLU);
	}

	private void userChangedValue(int x, int y) {
		if (type==DUAL) {
			if ( (x<3) || (width -3<=x) ) return;
			if ( (y<3) || (height-3<=y) ) return;
			setValue( colorCompH, (x-3)/(float)(width-7) );
			setValue( colorCompV, (height-4-y)/(float)(height-7) );
		} else {
			switch (type) {
			case HORIZONTAL:
				if ( (x<0) || (width <=x) ) return;
				setValue( colorCompH, (x)/(float)(width-1) );
				break;
			case VERTICAL  :
				if ( (y<0) || (height<=y) ) return;
				setValue( colorCompV, (height-1-y)/(float)(height-1) );
				break;
			}
		}
		colorChangeListener.colorChanged( getColor() );
	}

	@Override public void mouseDragged (MouseEvent e) { userChangedValue(e.getX(),e.getY()); }
	@Override public void mouseMoved   (MouseEvent e) {}
	@Override public void mouseClicked (MouseEvent e) {}
	@Override public void mouseEntered (MouseEvent e) {}
	@Override public void mouseExited  (MouseEvent e) {}
	@Override public void mousePressed (MouseEvent e) { userChangedValue(e.getX(),e.getY()); }
	@Override public void mouseReleased(MouseEvent e) { userChangedValue(e.getX(),e.getY()); }


	public static interface ColorChangeListener {
		public void colorChanged( Color color );
	}
}
