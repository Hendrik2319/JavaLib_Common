package net.schwarzbaer.gui;

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
}
