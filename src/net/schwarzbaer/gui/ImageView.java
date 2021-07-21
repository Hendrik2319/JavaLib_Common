package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class ImageView extends ZoomableCanvas<ImageView.ViewState> {
	private static final long serialVersionUID = 4779060880687788367L;
	private static final Color COLOR_AXIS = new Color(0x70000000,true);
	//private static final Color COLOR_BACKGROUND = Color.WHITE;
	
	private BufferedImage image;
	private Color bgColor;
	private boolean useInterpolation;
	private boolean useBetterInterpolation;
	private final BetterInterpolation betterInterpolation;
	
	public ImageView(int width, int height) { this(null, width, height); }
	public ImageView(BufferedImage image, int width, int height) {
		this.image = image;
		bgColor = null;
		useInterpolation = true;
		useBetterInterpolation = false;
		setPreferredSize(width, height);
		activateMapScale(COLOR_AXIS, "px", true);
		activateAxes(COLOR_AXIS, true,true,true,true);
		
		betterInterpolation = new BetterInterpolation(this::repaint);
		addZoomListener(this::updateBetterInterpolation);
		
		ContextMenu contextMenu = new ContextMenu(this);
		addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3)
					contextMenu.show(ImageView.this, e.getX(), e.getY());
			}
		});
	}
	
	private void updateBetterInterpolation() {
		if (useBetterInterpolation && this.image!=null && viewState.isOk()) {
			int imageX      = viewState.convertPos_AngleToScreen_LongX(0);
			int imageY      = viewState.convertPos_AngleToScreen_LatY (0);
			int imageWidth  = viewState.convertPos_AngleToScreen_LongX(this.image.getWidth ()) - imageX;
			int imageHeight = viewState.convertPos_AngleToScreen_LatY (this.image.getHeight()) - imageY;
			if (imageWidth<this.image.getWidth() || imageHeight<this.image.getHeight())
				betterInterpolation.recomputeImage(this.image, imageWidth, imageHeight);
		}
	}
	
	private static class BetterInterpolation {
		
		private final Runnable repaint;
		private final ExecutorService scheduler;
		private Future<BufferedImage> runningTask;
		
		BetterInterpolation(Runnable repaint) {
			this.repaint = repaint;
			if (this.repaint==null) throw new IllegalArgumentException();
			scheduler = Executors.newSingleThreadExecutor();
			runningTask = null;
		}

		public synchronized void recomputeImage(BufferedImage image, int imageWidth, int imageHeight) {
			if (runningTask!=null && !runningTask.isCancelled() && runningTask.isDone()) {
				runningTask.cancel(true);
			}
			runningTask = scheduler.submit(()->{
				BufferedImage newImage = computeScaledImage(image, imageWidth, imageHeight);
				SwingUtilities.invokeLater(repaint);
				return newImage;
			});
			
		}

		public synchronized BufferedImage getResult() {
			if (runningTask==null) return null;
			if (runningTask.isCancelled()) return null;
			if (!runningTask.isDone()) return null;
			
			try { return runningTask.get(); }
			catch (InterruptedException e) { System.err.printf("InterruptedException: %s%n", e.getMessage()); }
			catch (ExecutionException   e) { System.err.printf("ExecutionException: %s%n"  , e.getMessage()); }
			return null;
		}

		private BufferedImage computeScaledImage(BufferedImage image, int imageWidth, int imageHeight) {
			//x = 1;
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public boolean useInterpolation      () { return useInterpolation      ; }
	public boolean useBetterInterpolation() { return useBetterInterpolation; }
	
	public void useInterpolation      (boolean useInterpolation) {
		this.useInterpolation = useInterpolation;
		useBetterInterpolation = useInterpolation && useBetterInterpolation;
		repaint();
	}
	
	public void useBetterInterpolation(boolean useBetterInterpolation) {
		this.useBetterInterpolation = useInterpolation && useBetterInterpolation;
		repaint();
		//System.out.println("useBetterInterpolation: "+useBetterInterpolation);
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
		reset();
		updateBetterInterpolation();
	}

	public void setZoom(float zoom) {
		float currentZoom = viewState.convertLength_LengthToScreenF(1f);
		addZoom(new Point(width/2,height/2), zoom/currentZoom);
	}
	
	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
		repaint();
	}
	
	@Override
	protected void paintCanvas(Graphics g, int x, int y, int width, int height) {
		//g.setColor(COLOR_BACKGROUND);
		//g.fillRect(x, y, width, height);
		
		if (g instanceof Graphics2D && viewState.isOk()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setClip(x, y, width, height);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			if (image!=null) {
				int imageX      = viewState.convertPos_AngleToScreen_LongX(0);
				int imageY      = viewState.convertPos_AngleToScreen_LatY (0);
				int imageWidth  = viewState.convertPos_AngleToScreen_LongX(image.getWidth ()) - imageX;
				int imageHeight = viewState.convertPos_AngleToScreen_LatY (image.getHeight()) - imageY;
				
				if (bgColor!=null) {
					g2.setColor(bgColor);
					g2.fillRect(x+imageX, y+imageY, imageWidth, imageHeight);
				}
				
				g2.setColor(COLOR_AXIS);
				g2.drawLine(x+imageX, y, x+imageX, y+height);
				g2.drawLine(x, y+imageY, x+width, y+imageY);
				g2.drawLine(x+imageX+imageWidth-1, y, x+imageX+imageWidth-1, y+height);
				g2.drawLine(x, y+imageY+imageHeight-1, x+width, y+imageY+imageHeight-1);
				
				Object interpolationValue = null;
				if (useInterpolation && imageWidth<image.getWidth()) {
					interpolationValue = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
					if (useBetterInterpolation) {
						BufferedImage scaledImage = betterInterpolation.getResult();
						if (scaledImage != null) {
							g2.drawImage(scaledImage, x+imageX, y+imageY, null);
							interpolationValue = null;
						}
					}
				} else {
					interpolationValue = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
				}
				
				if (interpolationValue!=null) {
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationValue);
					g2.drawImage(image, x+imageX, y+imageY, imageWidth, imageHeight, null);
				}
			}
			
			drawMapDecoration(g2, x, y, width, height);
		}
	}
	
	@Override
	protected ViewState createViewState() {
		return new ViewState(this);
	}
	class ViewState extends ZoomableCanvas.ViewState {
		
		ViewState(ZoomableCanvas<?> canvas) {
			super(canvas,0.1f);
			setPlainMapSurface();
			setVertAxisDownPositive(true);
			//debug_showChanges_scalePixelPerLength = true;
		}

		@Override
		protected void determineMinMax(MapLatLong min, MapLatLong max) {
			min.longitude_x = (float) 0;
			min.latitude_y  = (float) 0;
			max.longitude_x = (float) (image==null ? 100 : image.getWidth ());
			max.latitude_y  = (float) (image==null ? 100 : image.getHeight());
		}
	}
	
	private static class ContextMenu extends JPopupMenu{
		private static final long serialVersionUID = 4090306246829034171L;
		private JCheckBoxMenuItem chkbxBetterInterpolation;
		private ImageView imageView;

		public ContextMenu(ImageView imageView) {
			this.imageView = imageView;
			chkbxBetterInterpolation = createCheckBoxMenuItem("Better Interpolation", imageView.useBetterInterpolation(), b -> {
				imageView.useBetterInterpolation(b);
			});
			JCheckBoxMenuItem chkbxInterpolation = createCheckBoxMenuItem("Interpolation", imageView.useInterpolation(), b -> {
				imageView.useInterpolation(b);
				chkbxBetterInterpolation.setEnabled(b);
			});
			
			add(createMenuItem("10%",e->imageView.setZoom(0.10f)));
			add(createMenuItem("25%",e->imageView.setZoom(0.25f)));
			add(createMenuItem("50%",e->imageView.setZoom(0.50f)));
			add(createMenuItem("75%",e->imageView.setZoom(0.75f)));
			addSeparator();
			add(createMenuItem("100%",e->imageView.setZoom(1)));
			addSeparator();
			add(createMenuItem("150%",e->imageView.setZoom(1.5f)));
			add(createMenuItem("200%",e->imageView.setZoom(2.0f)));
			add(createMenuItem("300%",e->imageView.setZoom(3.0f)));
			add(createMenuItem("400%",e->imageView.setZoom(4.0f)));
			add(createMenuItem("600%",e->imageView.setZoom(6.0f)));
			addSeparator();
			add(createSetBgColorMenuItem(imageView,Color.BLACK  , "Set Background to Black"));
			add(createSetBgColorMenuItem(imageView,Color.WHITE  , "Set Background to White"));
			add(createSetBgColorMenuItem(imageView,Color.MAGENTA, "Set Background to Magenta"));
			add(createSetBgColorMenuItem(imageView,Color.GREEN  , "Set Background to Green"));
			add(createSetBgColorMenuItem(imageView,null         , "Remove Background Color"));
			addSeparator();
			add(chkbxInterpolation);
			add(chkbxBetterInterpolation);
		}

		@Override
		public void show(Component invoker, int x, int y) {
			chkbxBetterInterpolation.setSelected(imageView.useBetterInterpolation());
			super.show(invoker, x, y);
		}

		private JMenuItem createMenuItem(String title, ActionListener al) {
			JMenuItem comp = new JMenuItem(title);
			if (al!=null) comp.addActionListener(al);
			return comp;
		}

		private JCheckBoxMenuItem createCheckBoxMenuItem(String title, boolean selected, Consumer<Boolean> setValue) {
			JCheckBoxMenuItem comp = new JCheckBoxMenuItem(title,selected);
			if (setValue!=null) comp.addActionListener(e->{
				setValue.accept(comp.isSelected());
			});
			return comp;
		}

		private JMenuItem createSetBgColorMenuItem(ImageView imageView, Color color, String title) {
			JMenuItem comp = createMenuItem(title, e->imageView.setBgColor(color));
			comp.setIcon(new ColorIcon(color,32,16,3));
			return comp;
		}

		private static class ColorIcon implements Icon {
		
			private final Color color;
			private final int width;
			private final int height;
			private final int cornerRadius;
		
			public ColorIcon(Color color, int width, int height, int cornerRadius) {
				this.color = color;
				this.width = width;
				this.height = height;
				this.cornerRadius = cornerRadius;
			}
			@Override public int getIconWidth () { return width;  }
			@Override public int getIconHeight() { return height; }
		
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				if (g instanceof Graphics2D) {
					Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					if (color==null) {
						g2.setColor(Color.BLACK);
						g2.drawRoundRect(x, y, width-1, height-1, cornerRadius*2, cornerRadius*2);
					} else {
						g2.setColor(color);
						g2.fillRoundRect(x, y, width, height, cornerRadius*2, cornerRadius*2);
					}
				} else {
					if (color==null) {
						g.setColor(Color.BLACK);
						g.drawRoundRect(x, y, width-1, height-1, cornerRadius*2, cornerRadius*2);
					} else {
						g.setColor(color);
						g.fillRoundRect(x, y, width, height, cornerRadius*2, cornerRadius*2);
					}
				}
			}
		
		
		}
		
	}
}