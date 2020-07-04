package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class ImageViewDialog extends JDialog {
	private static final long serialVersionUID = 2981906616002170627L;

	public ImageViewDialog(JFrame parent, BufferedImage image, String title, int width, int height) {
		super(parent,title,ModalityType.APPLICATION_MODAL);
		ImageView imageView = new ImageView(image,width,height);
		ContextMenu contextMenu = new ContextMenu(imageView);
		imageView.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3) {
					contextMenu.show(imageView, e.getX(), e.getY());
				}
			}
		});
		setContentPane(imageView);
		pack();
		setLocationRelativeTo(parent);
		imageView.reset();
	}
	
	private static class ContextMenu extends JPopupMenu{
		private static final long serialVersionUID = 4090306246829034171L;

		public ContextMenu(ImageView imageView) {
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
		}

		private JMenuItem createMenuItem(String title, ActionListener al) {
			JMenuItem comp = new JMenuItem(title);
			if (al!=null) comp.addActionListener(al);
			return comp;
		}
		
	}

	private static class ImageView extends ZoomableCanvas<ImageView.ViewState> {
		private static final long serialVersionUID = 4779060880687788367L;
		private static final Color COLOR_AXIS = new Color(0x70000000,true);
		//private static final Color COLOR_BACKGROUND = Color.WHITE;
		
		private BufferedImage image;
		
		public ImageView(BufferedImage image, int width, int height) {
			this.image = image;
			setPreferredSize(width, height);
			activateMapScale(COLOR_AXIS, "px", true);
			activateAxes(COLOR_AXIS, true,true,true,true);
		}
		
		public void setZoom(float zoom) {
			float currentZoom = viewState.convertLength_LengthToScreenF(1f);
			addZoom(new Point(width/2,height/2), zoom/currentZoom);
		}

		@Override
		protected void paintCanvas(Graphics g, int x, int y, int width, int height) {
			//g.setColor(COLOR_BACKGROUND);
			//g.fillRect(x, y, width, height);
			
			if (g instanceof Graphics2D) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setClip(x, y, width, height);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				
				int imageX      = viewState.convertPos_AngleToScreen_LongX(0);
				int imageY      = viewState.convertPos_AngleToScreen_LatY (0);
				int imageWidth  = viewState.convertPos_AngleToScreen_LongX(image.getWidth ()) - imageX;
				int imageHeight = viewState.convertPos_AngleToScreen_LatY (image.getHeight()) - imageY;
				
				g2.setColor(COLOR_AXIS);
				g2.drawLine(imageX, y, imageX, y+height);
				g2.drawLine(x, imageY, x+width, imageY);
				g2.drawLine(imageX+imageWidth, y, imageX+imageWidth, y+height);
				g2.drawLine(x, imageY+imageHeight, x+width, imageY+imageHeight);
				
				g2.drawImage(image, x+imageX, y+imageY, imageWidth+1, imageHeight+1, null);
				
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
				max.longitude_x = (float) image.getWidth();
				max.latitude_y  = (float) image.getHeight();
			}
		}
	}
}
