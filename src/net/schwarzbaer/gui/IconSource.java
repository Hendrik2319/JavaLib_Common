package net.schwarzbaer.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public abstract class IconSource<E extends Enum<E>> {
	
	private final int iconWidth;
	private final int iconHeight;
	private BufferedImage images;
	
	public IconSource(int iconWidth, int iconHeight) {
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.images = null;
	}
	
	public void readIconsFromResource(String resourcePath) {
//		String resourcePath = "/Toolbar.png";
		InputStream stream = getClass().getResourceAsStream(resourcePath);
		if (stream==null) {
			System.err.printf("IconSource: Can't open resource stream \"%s\".\r\n",resourcePath);
			return;
		}
		try { images = ImageIO.read(stream); }
		catch (IOException e) {
			System.err.printf("IconSource: IOException while reading icon image file from resource \"%s\".",resourcePath);
			return;
		}
	}
	
	protected abstract int getIconIndexInImage(E key);
	
	public Icon getIcon(E key) {
		return new ImageIcon(images.getSubimage( getIconIndexInImage(key)*iconWidth,0, iconWidth,iconHeight ));
	}
}
