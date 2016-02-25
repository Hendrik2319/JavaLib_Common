package net.schwarzbaer.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public abstract class IconSource<E extends Enum<E>> {
	
	private final EnumMap<E,Icon> icons;
	private final int iconWidth;
	private final int iconHeight;
	
	public IconSource(Class<E> keyType, int iconWidth, int iconHeight) {
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.icons = new EnumMap<E,Icon>(keyType);
	}
	
	public void readIconsFromResource(E[] availableKeys, String resourcePath) {
//		String resourcePath = "/Toolbar.png";
		InputStream stream = getClass().getResourceAsStream(resourcePath);
		if (stream==null) {
			System.err.printf("IconSource: Can't open resource stream \"%s\".\r\n",resourcePath);
			return;
		}
		BufferedImage images;
		try { images = ImageIO.read(stream); }
		catch (IOException e) {
			System.err.printf("IconSource: IOException while reading icon image file from resource \"%s\".",resourcePath);
			return;
		}
		
		icons.clear();
		
		for (E key:availableKeys) {
			int index = getIconIndexInImage(key);
			BufferedImage subimage = images.getSubimage( index*iconWidth,0, iconWidth,iconHeight );
			icons.put(key, new ImageIcon(subimage));
		}
	}
	
	protected abstract int getIconIndexInImage(E key);
	
	public Icon getIcon(E key) {
		return icons.get(key);
	}
}
