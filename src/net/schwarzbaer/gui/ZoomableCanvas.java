package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

import javax.swing.event.MouseInputAdapter;

public abstract class ZoomableCanvas<VS extends ZoomableCanvas.ViewState> extends Canvas {
	private static final long serialVersionUID = -1282219829667604150L;

	private Point panStart;
	protected VS viewState;

	private Axes verticalAxes;
	private Axes horizontalAxes;
	private Scale mapScale;

	private boolean withTopAxis;

	private boolean withRightAxis;

	private boolean withBottomAxis;

	private boolean withLeftAxis;
	
	protected ZoomableCanvas() { this(null,false,false,false,false,false); }
	protected ZoomableCanvas(Color COLOR_AXIS, boolean withMapScale) { this(COLOR_AXIS,withMapScale,false,false,false,false); }
	protected ZoomableCanvas(Color COLOR_AXIS, boolean withMapScale, boolean withTopAxis, boolean withRightAxis, boolean withBottomAxis, boolean withLeftAxis) {
		this.withTopAxis = withTopAxis;
		this.withRightAxis = withRightAxis;
		this.withBottomAxis = withBottomAxis;
		this.withLeftAxis = withLeftAxis;
		panStart = null;
		viewState = createViewState();
		mapScale       = !withMapScale ? null : new Scale(viewState, COLOR_AXIS, "u");
		verticalAxes   = !this.withLeftAxis && !this.withRightAxis ? null : new Axes (viewState, true , COLOR_AXIS);
		horizontalAxes = !this.withTopAxis && !this.withBottomAxis ? null : new Axes (viewState, false, COLOR_AXIS);
		
		MouseInputAdapter mouse = new MouseInputAdapter() {
			@Override public void mousePressed   (MouseEvent e) { if (e.getButton()==MouseEvent.BUTTON1) startPan  (e.getPoint()); }
			@Override public void mouseDragged   (MouseEvent e) { proceedPan(e.getPoint());  }
			@Override public void mouseReleased  (MouseEvent e) { if (e.getButton()==MouseEvent.BUTTON1) stopPan   (e.getPoint());  }
			@Override public void mouseWheelMoved(MouseWheelEvent e) { zoom(e.getPoint(),e.getPreciseWheelRotation()); }
			
			@Override public void mouseEntered(MouseEvent e) { ZoomableCanvas.this.mouseEntered(e); }
			@Override public void mouseMoved  (MouseEvent e) { ZoomableCanvas.this.mouseMoved  (e); }
			@Override public void mouseExited (MouseEvent e) { ZoomableCanvas.this.mouseExited (e); }
			@Override public void mouseClicked(MouseEvent e) { ZoomableCanvas.this.mouseClicked(e); }
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		addMouseWheelListener(mouse);
	}
	protected abstract VS createViewState();
	
	protected void mouseEntered(MouseEvent e) {}
	protected void mouseMoved  (MouseEvent e) {}
	protected void mouseExited (MouseEvent e) {}
	protected void mouseClicked(MouseEvent e) {}

	public void reset() {
		if (viewState.reset()) {
			updateAxes();
			updateMapScale();
		}
		repaint();
	}

	public void update() {
		if (!viewState.isOk())
			reset();
		repaint();
	}

	protected static Rectangle2D getBounds(Graphics2D g2, Font font, String str) {
		return font.getStringBounds(str==null?"":str, g2.getFontRenderContext());
	}

	protected static Rectangle2D getBounds(Graphics2D g2, String str) {
		return g2.getFontMetrics().getStringBounds(str==null?"":str, g2);
	}

	private Point sub(Point p1, Point p2) {
		return new Point(p1.x-p2.x,p1.y-p2.y);
	}

	private void startPan(Point point) {
		panStart = point;
		viewState.tempPanOffset = new Point();
		repaint();
	}

	private void proceedPan(Point point) {
		if (panStart != null)
			viewState.tempPanOffset = sub(point,panStart);
		repaint();
	}

	private void stopPan(Point point) {
		if (panStart!=null)
			if (viewState.pan(sub(point,panStart))) {
				updateAxes();
			}
		
		panStart = null;
		viewState.tempPanOffset = null;
		repaint();
	}

	private void zoom(Point point, double preciseWheelRotation) {
		float f = (float) Math.pow(1.1f, preciseWheelRotation);
		if (viewState.zoom(point,f)) {
			updateAxes();
			updateMapScale();
			repaint();
		}
	}

	private void updateMapScale() {
		if (mapScale!=null) mapScale.update();
	}

	private void updateAxes() {
		if (viewState.isOk()) {
			if (verticalAxes  !=null)   verticalAxes.updateTicks();
			if (horizontalAxes!=null) horizontalAxes.updateTicks();
		}
	}
	
	protected void drawMapDecoration(Graphics2D g2, int x, int y, int width, int height) {
		if (  withLeftAxis)   verticalAxes.drawAxis ( g2, x+5       , y+20, height-40, true  );
		if ( withRightAxis)   verticalAxes.drawAxis ( g2, x+width -5, y+20, height-40, false );
		if (   withTopAxis) horizontalAxes.drawAxis ( g2, y+5       , x+20, width -40, true  );
		if (withBottomAxis) horizontalAxes.drawAxis ( g2, y+height-5, x+20, width -40, false );
		if (mapScale!=null) mapScale      .drawScale( g2, x+width-110, y+height-50, 60,15 );
	}

	public static class MapLatLong {
		
		public Float latitude;
		public Float longitude;
		
		public MapLatLong() {
			this.latitude = null;
			this.longitude = null;
		}
		
		public MapLatLong(float latitude, float longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
	
		public MapLatLong(MapLatLong other) {
			this.latitude = other.latitude;
			this.longitude = other.longitude;
		}
	
		@Override
		public String toString() {
			return String.format("MapLatLong [latitude=%s, longitude=%s]", latitude, longitude);
		}
	
		public void setMin(MapLatLong location) {
			if (location.latitude!=null) {
				if (latitude==null) latitude = location.latitude;
				else latitude = Math.min( latitude, location.latitude );
			}
			if (location.longitude!=null) {
				if (longitude==null) longitude = location.longitude;
				else longitude = Math.min( longitude, location.longitude );
			}
		}
	
		public void setMax(MapLatLong location) {
			if (location.latitude!=null) {
				if (latitude==null) latitude = location.latitude;
				else latitude = Math.max( latitude, location.latitude );
			}
			if (location.longitude!=null) {
				if (longitude==null) longitude = location.longitude;
				else longitude = Math.max( longitude, location.longitude );
			}
		}
		
	}

	public static abstract class ViewState {
		
		private ZoomableCanvas<?> canvas;
		
		Point tempPanOffset;
		protected MapLatLong center;
		private float scalePixelPerLength;
		private float scaleLengthPerAngleLat;
		private float scaleLengthPerAngleLong;
		private float lowerZoomLimit;


		protected ViewState(ZoomableCanvas<?> canvas, float lowerZoomLimit) {
			this.canvas = canvas;
			this.lowerZoomLimit = lowerZoomLimit;
			tempPanOffset = null;
			clearValues();
		}
		
		protected void clearValues() {
			center = null;
			scaleLengthPerAngleLat  = Float.NaN;
			scaleLengthPerAngleLong = Float.NaN;
			scalePixelPerLength     = Float.NaN;
		}

		public boolean haveScalePixelPerLength() {
			return !Float.isNaN(scalePixelPerLength);
		}
		
		public Integer convertLength_LengthToScreen(Float length_u) {
			if (length_u==null || Float.isNaN(length_u)) return null;
			return Math.round( length_u * scalePixelPerLength );
		}
	
		public boolean isOk() {
			return center!=null && !Float.isNaN(scalePixelPerLength) && !Float.isNaN(scaleLengthPerAngleLat) && !Float.isNaN(scaleLengthPerAngleLong);
		}
		
		protected abstract float computeScaleLengthPerAngle_Latitude();
		protected abstract float computeScaleLengthPerAngle_Longitude();
		protected abstract void  determineMinMax(MapLatLong min, MapLatLong max);
		
		protected boolean reset() {
			MapLatLong min = new MapLatLong();
			MapLatLong max = new MapLatLong();
			determineMinMax(min, max);
			
			if (min.latitude==null || min.longitude==null || max.latitude==null || max.longitude==null ) {
				clearValues();
				return false;
			}
			
			center = new MapLatLong( (min.latitude+max.latitude)/2, (min.longitude+max.longitude)/2 );
			
			updateScaleLengthPerAngle();
			float neededHeight = (max.latitude -min.latitude )*scaleLengthPerAngleLat;
			float neededWidth  = (max.longitude-min.longitude)*scaleLengthPerAngleLong;
			if (neededHeight==0 || neededWidth==0) {
				clearValues();
				return false;
			}
			
			float scalePixelPerLengthLat  = (canvas.height-30) / neededHeight;
			float scalePixelPerLengthLong = (canvas.width -30) / neededWidth;
			scalePixelPerLength = Math.min(scalePixelPerLengthLat, scalePixelPerLengthLong);
			
			return true;
		}

		boolean zoom(Point point, float f) {
			if (!isOk()) return false;
			
			MapLatLong centerOld = new MapLatLong(center);
			MapLatLong location = convertScreenToAngle(point);
			
			if (scalePixelPerLength*f < lowerZoomLimit) return false;
			
			scalePixelPerLength *= f;
			center.latitude  = (centerOld.latitude  - location.latitude ) / f + location.latitude;
			center.longitude = (centerOld.longitude - location.longitude) * (float) (Math.cos(centerOld.latitude/180*Math.PI) / Math.cos(center.latitude/180*Math.PI) ) / f + location.longitude;
			updateScaleLengthPerAngle();
			
			return true;
		}
	
		boolean pan(Point offsetOnScreen) {
			if (!isOk()) return false;
			
			center.latitude  -= convertLength_ScreenToAngle_Lat (-offsetOnScreen.y);
			center.longitude -= convertLength_ScreenToAngle_Long( offsetOnScreen.x);
			updateScaleLengthPerAngle();
			
			return true;
		}
	
		private void updateScaleLengthPerAngle() {
			scaleLengthPerAngleLat  = computeScaleLengthPerAngle_Latitude();
			scaleLengthPerAngleLong = computeScaleLengthPerAngle_Longitude();
		}

		public Point convertPos_AngleToScreen(MapLatLong location) {
			if (location.latitude==null || location.longitude==null) return null;
			return new Point(
				convertPos_AngleToScreen_Long(location.longitude),
				convertPos_AngleToScreen_Lat (location.latitude )
			);
		}
		public Integer convertPos_AngleToScreen_Long(float longitude) {
			float x = canvas.width /2f + convertLength_AngleToScreen_Long(longitude - center.longitude);
			if (tempPanOffset!=null) x += tempPanOffset.x;
			return Math.round(x);
		}
		public Integer convertPos_AngleToScreen_Lat (float latitude) {
			float y = canvas.height/2f - convertLength_AngleToScreen_Lat (latitude  - center.latitude );
			if (tempPanOffset!=null) y += tempPanOffset.y;
			return Math.round(y);
		}
		private float convertLength_AngleToScreen_Long(float length_a) { return length_a * scaleLengthPerAngleLong * scalePixelPerLength; }
		private float convertLength_AngleToScreen_Lat (float length_a) { return length_a * scaleLengthPerAngleLat  * scalePixelPerLength; }
		
		public MapLatLong convertScreenToAngle(Point point) {
			return new MapLatLong(
				convertPos_ScreenToAngle_Lat (point.y),
				convertPos_ScreenToAngle_Long(point.x)
			);
		}
		public Float convertPos_ScreenToAngle_Long(int x) {
			if (tempPanOffset!=null) x -= tempPanOffset.x;
			return center.longitude + convertLength_ScreenToAngle_Long(x - canvas.width /2f);
		}
		public Float convertPos_ScreenToAngle_Lat(int y) {
			if (tempPanOffset!=null) y -= tempPanOffset.y;
			return center.latitude  - convertLength_ScreenToAngle_Lat(y - canvas.height/2f);
		}
		public float convertLength_ScreenToAngle_Long(float length_px) { return length_px / scalePixelPerLength / scaleLengthPerAngleLong; }
		public float convertLength_ScreenToAngle_Lat (float length_px) { return length_px / scalePixelPerLength / scaleLengthPerAngleLat ; }
	}

	public static class Axes {
		private static final int minMinorTickUnitLength_px = 7;
		private static final int majorTickLength_px = 10;
		private static final int minorTickLength_px = 4;
		
		private Float majorTickUnit_a = null;
		private Float minorTickUnit_a = null;
		private Integer minorTickCount = null;
		private int precision = 1;
		
		private boolean isVertical;
		private ViewState viewState;
		private Color axisColor;
		
		public Axes(ViewState viewState, boolean isVertical, Color axisColor) {
			this.viewState = viewState;
			this.isVertical = isVertical;
			this.axisColor = axisColor;
		}
		
		public String toString(float angle) {
			return String.format(Locale.ENGLISH, "%1."+precision+"f", angle);
		}
		
		public void updateTicks() {
			float minMinorTickUnitLength_a;
			if (isVertical) minMinorTickUnitLength_a = viewState.convertLength_ScreenToAngle_Lat (minMinorTickUnitLength_px);
			else            minMinorTickUnitLength_a = viewState.convertLength_ScreenToAngle_Long(minMinorTickUnitLength_px);
			
			majorTickUnit_a = 1f;
			minorTickCount = 5; // minorTickUnit_a = 0.2
			precision = 0;
			while (majorTickUnit_a/10 > minMinorTickUnitLength_a) {
				
				if (majorTickUnit_a/10 > minMinorTickUnitLength_a) {
					majorTickUnit_a /= 2; // majorTickUnit_a = 0.5
					minorTickCount = 5;   // minorTickUnit_a = 0.1
					precision += 1;
				}
				
				if (majorTickUnit_a/10 > minMinorTickUnitLength_a) {
					majorTickUnit_a /= 2.5f; // majorTickUnit_a = 0.2
					minorTickCount = 4;      // minorTickUnit_a = 0.05
				}
				
				if (majorTickUnit_a/10 > minMinorTickUnitLength_a) {
					majorTickUnit_a /= 2; // majorTickUnit_a = 0.1
					minorTickCount = 5;   // minorTickUnit_a = 0.02
				}
			};
			while (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
				
				if (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
					majorTickUnit_a *= 2; // majorTickUnit_a = 2
					minorTickCount = 4;   // minorTickUnit_a = 0.5
				}
				
				if (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
					majorTickUnit_a *= 2.5f; // majorTickUnit_a = 5
					minorTickCount = 5;      // minorTickUnit_a = 1
				}
				
				if (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
					majorTickUnit_a *= 2; // majorTickUnit_a = 10
					minorTickCount = 5;   // minorTickUnit_a = 2
				}
			};
			minorTickUnit_a = majorTickUnit_a/minorTickCount;
		}
	
		public void drawAxis(Graphics2D g2, int c0, int c1, int width1, boolean labelsRightBottom) {
			//   isVertical:  c0 = x, c1 = y, width1 = height
			// ! isVertical:  c0 = y, c1 = x, width1 = width
			if (width1<0) return; // display area too small
			
			float minAngle_a,maxAngle_a,angleWidth_a;
			if (isVertical) minAngle_a = viewState.convertPos_ScreenToAngle_Lat (c1);
			else            minAngle_a = viewState.convertPos_ScreenToAngle_Long(c1);
			if (isVertical) maxAngle_a = viewState.convertPos_ScreenToAngle_Lat (c1+width1);
			else            maxAngle_a = viewState.convertPos_ScreenToAngle_Long(c1+width1);
			
			if (maxAngle_a<minAngle_a) {
				angleWidth_a = minAngle_a; // angleWidth_a  used as temp. storage
				minAngle_a = maxAngle_a;
				maxAngle_a = angleWidth_a;
			}
			angleWidth_a = maxAngle_a-minAngle_a;
			
			
			float firstMajorTick_a = (float) Math.ceil(minAngle_a / majorTickUnit_a) * majorTickUnit_a;
			
			g2.setPaint(axisColor);
			if (isVertical) g2.drawLine(c0, c1, c0, c1+width1);
			else            g2.drawLine(c1, c0, c1+width1, c0);
			
			for (int j=1; minAngle_a < firstMajorTick_a-j*minorTickUnit_a; j++)
				drawMinorTick( g2, c0, firstMajorTick_a - j*minorTickUnit_a, labelsRightBottom );
			
			for (int i=0; firstMajorTick_a+i*majorTickUnit_a < maxAngle_a; i++) {
				float majorTick_a = firstMajorTick_a + i*majorTickUnit_a;
				drawMajorTick( g2, c0, majorTick_a, labelsRightBottom );
				for (int j=1; j<minorTickCount && majorTick_a + j*minorTickUnit_a < maxAngle_a; j++)
					drawMinorTick( g2, c0, majorTick_a + j*minorTickUnit_a, labelsRightBottom );
			}
		}
	
		private void drawMajorTick(Graphics2D g2, int c0, float angle, boolean labelsRightBottom) {
			//   isVertical:  c0 = x, c1 = y, width1 = height
			// ! isVertical:  c0 = y, c1 = x, width1 = width
			int c1;
			if (isVertical) c1 = viewState.convertPos_AngleToScreen_Lat (angle);
			else            c1 = viewState.convertPos_AngleToScreen_Long(angle);
			
			int halfTick = majorTickLength_px/2;
			int tickLeft  = halfTick;
			int tickRight = halfTick;
			if (labelsRightBottom) tickLeft = 0;
			else                   tickRight = 0;
			if (isVertical) g2.drawLine(c0-tickLeft, c1, c0+tickRight, c1);
			else            g2.drawLine(c1, c0-tickLeft, c1, c0+tickRight);
			
			String label = toString(angle);
			Rectangle2D bounds = g2.getFontMetrics().getStringBounds(label, g2);
			
			if (isVertical) {
				if (labelsRightBottom) g2.drawString(label, (float)(c0-bounds.getX()+halfTick+4                  ), (float)(c1-bounds.getY()-bounds.getHeight()/2));
				else                   g2.drawString(label, (float)(c0-bounds.getX()-halfTick-4-bounds.getWidth()), (float)(c1-bounds.getY()-bounds.getHeight()/2));
			} else {
				if (labelsRightBottom) g2.drawString(label, (float)(c1-bounds.getX()-bounds.getWidth()/2), (float)(c0-bounds.getY()+halfTick+4                   ));
				else                   g2.drawString(label, (float)(c1-bounds.getX()-bounds.getWidth()/2), (float)(c0-bounds.getY()-halfTick-4-bounds.getHeight()));
			}
		}
	
		private void drawMinorTick(Graphics2D g2, int c0, float angle, boolean labelsRightBottom) {
			//   isVertical:  c0 = x, c1 = y, width1 = height
			// ! isVertical:  c0 = y, c1 = x, width1 = width
			int c1;
			if (isVertical) c1 = viewState.convertPos_AngleToScreen_Lat (angle);
			else            c1 = viewState.convertPos_AngleToScreen_Long(angle);
			
			int tickLeft  = minorTickLength_px/2;
			int tickRight = minorTickLength_px/2;
			if (labelsRightBottom) tickLeft  = 0;
			else                   tickRight = 0;
			if (isVertical) g2.drawLine(c0-tickLeft, c1, c0+tickRight, c1);
			else            g2.drawLine(c1, c0-tickLeft, c1, c0+tickRight);
		}
	}

	public static class Scale {
		
		private static final int minScaleLength_px = 50;
	
		private float scaleLength_u;
		private int   scaleLength_px;
		private Color scaleColor;
		private String unit;
		private ViewState viewState;
	
		public Scale(ViewState viewState, Color scaleColor, String unit) {
			this.viewState = viewState;
			this.scaleColor = scaleColor;
			this.unit = unit;
			scaleLength_px = minScaleLength_px;
			scaleLength_u = 1;
		}

		public void update() {
			if (!viewState.haveScalePixelPerLength()) return;
			
			scaleLength_u = 1;
			
			if (( viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) )
				while ( viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) {
					float base = scaleLength_u;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u = 1.5f*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u =    2*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u =    3*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u =    4*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u =    5*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u =    6*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u =    8*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u) < minScaleLength_px) scaleLength_u =   10*base;
				}
			else
				while ( viewState.convertLength_LengthToScreen(scaleLength_u*0.80f) > minScaleLength_px) {
					float base = scaleLength_u;
					if (viewState.convertLength_LengthToScreen(base*0.80f) > minScaleLength_px) scaleLength_u = base*0.80f;
					if (viewState.convertLength_LengthToScreen(base*0.60f) > minScaleLength_px) scaleLength_u = base*0.60f;
					if (viewState.convertLength_LengthToScreen(base*0.50f) > minScaleLength_px) scaleLength_u = base*0.50f;
					if (viewState.convertLength_LengthToScreen(base*0.40f) > minScaleLength_px) scaleLength_u = base*0.40f;
					if (viewState.convertLength_LengthToScreen(base*0.30f) > minScaleLength_px) scaleLength_u = base*0.30f;
					if (viewState.convertLength_LengthToScreen(base*0.20f) > minScaleLength_px) scaleLength_u = base*0.20f;
					if (viewState.convertLength_LengthToScreen(base*0.15f) > minScaleLength_px) scaleLength_u = base*0.15f;
					if (viewState.convertLength_LengthToScreen(base*0.10f) > minScaleLength_px) scaleLength_u = base*0.10f;
				}
			scaleLength_px = viewState.convertLength_LengthToScreen(scaleLength_u);
		}
		
		private String getScaleLengthStr() {
			float f = scaleLength_u;
			if (f<0.002) return String.format(Locale.ENGLISH, "%1.5f%s", f, unit);
			if (f<0.02 ) return String.format(Locale.ENGLISH, "%1.4f%s", f, unit);
			if (f<0.2  ) return String.format(Locale.ENGLISH, "%1.3f%s", f, unit);
			if (f<2    ) return String.format(Locale.ENGLISH, "%1.2f%s", f, unit);
			if (f<1000 ) return String.format(Locale.ENGLISH, "%1.0f%s", f, unit);
			f /= 1000;
			if (f<2    ) return String.format(Locale.ENGLISH, "%1.1fk%s", f, unit);
			if (f<1000 ) return String.format(Locale.ENGLISH, "%1.0fk%s", f, unit);
			f /= 1000;
			if (f<2    ) return String.format(Locale.ENGLISH, "%1.1fM%s", f, unit);
			else         return String.format(Locale.ENGLISH, "%1.0fM%s", f, unit);
		}
		
		public void drawScale(Graphics2D g2, int x, int y, int w, int h) {
			//g2.setColor(Color.RED);
			//g2.drawRect(x, y, w, h);
			
			g2.setColor(scaleColor);
			
			g2.drawLine(x+w, y  , x+w, y+h);
			g2.drawLine(x+w, y+h, x+w-scaleLength_px, y+h);
			g2.drawLine(x+w-scaleLength_px, y+h, x+w-scaleLength_px, y);
			
			String str = getScaleLengthStr();
			Rectangle2D bounds = getBounds(g2, str);
			
			g2.drawString( str, (float)(x+w-bounds.getX()-bounds.getWidth()-3), (float)(y+h-bounds.getY()-bounds.getHeight()-3) );
		}
	}
}
