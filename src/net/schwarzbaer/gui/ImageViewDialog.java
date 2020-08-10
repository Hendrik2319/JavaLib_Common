package net.schwarzbaer.gui;

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
}
