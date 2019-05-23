package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class ColorSlider extends Canvas implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 6525911479150907865L;
	
	private   final ColorChangeListener colorChangeListener;
	protected final SliderType type;
	protected final ColorSliderModel model;
	
	public ColorSlider(SliderType type, Colorizer colorizer, float f, ColorChangeListener colorChangeListener ) {
		this(type, new SimpleColorSliderModel(f, colorizer), colorChangeListener);
		if (type==SliderType.DUAL)
			throw new UnsupportedOperationException("A ColorSlider based on a Colorizer can't be a dual slider."); 
	}
	public ColorSlider(SliderType type, ColorSliderModel model, ColorChangeListener colorChangeListener) {
		this.colorChangeListener = colorChangeListener;
		this.type = type;
		this.model = model;
		switch (type) {
		case VERTICAL  : setPreferredSize(20, 128); break;
		case HORIZONTAL: setPreferredSize(128, 20); break;
		case DUAL      : setPreferredSize(128, 128); break;
		}
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	public void setValue( float f ) { model.setValue(f); repaint(); }
	public void setValue( float fH, float fV ) { model.setValue(fH, fV); repaint(); }
	
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
		if (!isEnabled()) {
			g.setColor(Color.GRAY);
			g.fillRect(3, 0, width-7, height);
		} else
			for (int y=0; y<height; y++) {
				g.setColor(model.calcColor(calcFraction(height-1,y,0)));
				g.drawLine(3,y,width-4,y);
			}
		int y = Math.round((1-model.getValue())*(height-1));
		g.setColor(isEnabled()?Color.BLACK:Color.DARK_GRAY);
		g.drawLine(0,y,width-1,y);
	}

	private void paintH(Graphics g, int width, int height) {
		if (!isEnabled()) {
			g.setColor(Color.GRAY);
			g.fillRect(0, 3, width, height-7);
		} else
			for (int x=0; x<width; x++) {
				g.setColor(model.calcColor(calcFraction(0,x,width-1)));
				g.drawLine(x,3,x,height-4);
			}
		int x = Math.round(model.getValue()*(width-1));
		g.setColor(isEnabled()?Color.BLACK:Color.DARK_GRAY);
		g.drawLine(x,0,x,height-1);
	}

	private void paintD(Graphics g, int width, int height) {
		if (!isEnabled()) {
			g.setColor(Color.GRAY);
			g.fillRect(3, 3, width-6, height-6);
		} else
			for (int x=3; x<width-3; x++) {
				model.prepareColorH(calcFraction(3,x,width-4));
				for (int y=3; y<height-3; y++) {
					g.setColor(model.calcColorVFromPreparedColor(calcFraction(height-4,y,3)));
					g.drawLine(x,y,x,y);
				}
			}
		int x = Math.round(   model.getValueH() *(width -7))+3;
		int y = Math.round((1-model.getValueV())*(height-7))+3;
		g.setColor(isEnabled()?Color.BLACK:Color.DARK_GRAY);
		g.drawOval(x-3, y-3, 6, 6);
	}

	private float calcFraction(int minV, int v, int maxV) {
		return (v-minV)/(float)(maxV-minV);
	}
	
	private void userChangedValue(int x, int y) {
		float f; float fH; float fV;
		switch (type) {
		case HORIZONTAL:
			if (x<0) x=0;
			if (width<=x) x=width-1;
			model.setValue( f = calcFraction(0,x,width-1) );
			colorChangeListener.colorChanged( model.getColor(), f );
			break;
		case VERTICAL:
			if (y<0) y=0;
			if (height<=y) y=height-1;
			model.setValue( f = calcFraction(height-1,y,0) );
			colorChangeListener.colorChanged( model.getColor(), f );
			break;
		case DUAL:
			if (x<3) x=3;
			if (y<3) y=3;
			if (width -3<=x) x=width -4;
			if (height-3<=y) y=height-4;
			model.setValue( fH = calcFraction(3,x,width-4), fV = calcFraction(height-4,y,3) );
			colorChangeListener.colorChanged( model.getColor(), fH, fV );
			break;
		}
		repaint();
	}
	
	@Override public void mouseDragged (MouseEvent e) { if (isEnabled()) userChangedValue(e.getX(),e.getY()); }
	@Override public void mouseMoved   (MouseEvent e) {}
	@Override public void mouseClicked (MouseEvent e) {}
	@Override public void mouseEntered (MouseEvent e) {}
	@Override public void mouseExited  (MouseEvent e) {}
	@Override public void mousePressed (MouseEvent e) { if (isEnabled()) userChangedValue(e.getX(),e.getY()); }
	@Override public void mouseReleased(MouseEvent e) { if (isEnabled()) userChangedValue(e.getX(),e.getY()); }


	public static interface ColorChangeListener {
		public void colorChanged( Color color, float f );
		public void colorChanged( Color color, float fH, float fV );
	}
	
	public static interface ColorSliderModel {
		public Color getColor();
		public Color calcColor(float f);
		public void prepareColorH(float f);
		public Color calcColorVFromPreparedColor(float f);
		public void setValue( float f );
		public void setValue( float fH,float fV );
		public float getValue();
		public float getValueH();
		public float getValueV();
	}
	
	public static enum SliderType {
		HORIZONTAL, VERTICAL, DUAL
	}
	
	private static class SimpleColorSliderModel implements ColorSliderModel {
		
		private float fraction;
		private Colorizer colorizer;

		public SimpleColorSliderModel(float fraction, Colorizer colorizer) {
			this.fraction = fraction;
			this.colorizer = colorizer;
		}

		@Override public void  setValue(float f) { fraction = f; }
		@Override public Color getColor() { return calcColor(fraction); }
		@Override public float getValue() { return fraction; }
		
		
		@Override public void  prepareColorH(float f) { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }
		@Override public Color calcColorVFromPreparedColor(float f) { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }
		@Override public void  setValue(float fH, float fV) { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider.");  }
		@Override public float getValueH() { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }
		@Override public float getValueV() { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }

		@Override
		public Color calcColor(float f) {
			return colorizer.calcColor(f);
		}
		
	}
	
	public static interface Colorizer {

		Color calcColor(float f);
		
	}
}
