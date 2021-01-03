package net.schwarzbaer.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class ImageViewDialog extends JDialog {
	private static final long serialVersionUID = 2981906616002170627L;
	private ImageView imageView;

	public ImageViewDialog(JFrame parent, BufferedImage image, String title, int width, int height) {
		this(parent, image, title, width, height, false);
	}
	public ImageViewDialog(JFrame parent, BufferedImage image, String title, int width, int height, boolean exitOnESC) {
		super(parent,title,ModalityType.APPLICATION_MODAL);
		imageView = new ImageView(image,width,height);
		setContentPane(imageView);
		pack();
		setLocationRelativeTo(parent);
		imageView.reset();
		if (exitOnESC) {
			KeyAdapter keyAdapter = new KeyAdapter() {
				@Override public void keyPressed(KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ESCAPE)
						ImageViewDialog.this.setVisible(false);
				}
			};
			addKeyListener(keyAdapter);
			imageView.addKeyListener(keyAdapter);
		}
	}

	public void setImage(BufferedImage image) {
		imageView.setImage(image);
		imageView.reset();
	}
}
