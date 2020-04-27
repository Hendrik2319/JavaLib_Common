package net.schwarzbaer.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.event.MouseInputAdapter;

public abstract class ZoomableCanvas<VS extends ZoomableCanvas.ViewState> extends Canvas {
	private static final long serialVersionUID = -1282219829667604150L;

	private Point panStart = null;
	protected VS viewState;
	
	protected ZoomableCanvas() {
		viewState = createViewState();
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
	protected abstract void updateAfterFinalPan();
	protected abstract void updateAfterZoom();
	
	protected void mouseEntered(MouseEvent e) {}
	protected void mouseMoved  (MouseEvent e) {}
	protected void mouseExited (MouseEvent e) {}
	protected void mouseClicked(MouseEvent e) {}

	protected Point sub(Point p1, Point p2) {
		return new Point(p1.x-p2.x,p1.y-p2.y);
	}

	protected void startPan(Point point) {
		panStart = point;
		viewState.tempPanOffset = new Point();
		repaint();
	}

	protected void proceedPan(Point point) {
		if (panStart != null)
			viewState.tempPanOffset = sub(point,panStart);
		repaint();
	}

	protected void stopPan(Point point) {
		if (panStart!=null)
			if (viewState.pan_valueChecked(sub(point,panStart))) {
				updateAfterFinalPan();
			}
		
		panStart = null;
		viewState.tempPanOffset = null;
		repaint();
	}

	protected void zoom(Point point, double preciseWheelRotation) {
		float f = (float) Math.pow(1.1f, preciseWheelRotation);
		if (viewState.zoom_valueChecked(point,f)) {
			updateAfterZoom();
			repaint();
		}
	}

	protected static abstract class ViewState {
		protected Point tempPanOffset = null;

		protected abstract boolean isOk();
		protected abstract boolean pan(Point offsetOnScreen);
		protected abstract boolean zoom(Point point, float f);
		
		protected boolean pan_valueChecked(Point offsetOnScreen) {
			if (!isOk()) return false;
			return pan(offsetOnScreen);
		}

		protected boolean zoom_valueChecked(Point point, float f) {
			if (!isOk()) return false;
			return zoom(point,f);
		}
	}
}
