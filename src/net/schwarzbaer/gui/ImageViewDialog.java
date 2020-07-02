package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class ImageViewDialog extends JDialog {
	private static final long serialVersionUID = 2981906616002170627L;

	public ImageViewDialog(JFrame parent, BufferedImage image, String title, int width, int height) {
		super(parent,title,ModalityType.APPLICATION_MODAL);
		ImageView imageView = new ImageView(image,width,height);
		setContentPane(imageView);
		pack();
		setLocationRelativeTo(parent);
		imageView.reset();
	}
	
	private static class ImageView extends ZoomableCanvas<ImageView.ViewState> {
		private static final long serialVersionUID = 4779060880687788367L;
		private static final Color COLOR_AXIS = new Color(0x70000000,true);
		//private static final Color COLOR_BACKGROUND = Color.WHITE;
		
		private BufferedImage image;
		
		public ImageView(BufferedImage image, int width, int height) {
			this.image = image;
			setPreferredSize(width, height);
			activateMapScale(COLOR_AXIS, "px");
			activateAxes(COLOR_AXIS, true,true,true,true);
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
