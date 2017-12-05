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
	private final int columnCount;
	private BufferedImage images;
	
	public IconSource(int iconWidth, int iconHeight) {
		this(iconWidth,iconHeight,-1);
	}
	
	public IconSource(int iconWidth, int iconHeight, int columnCount) {
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.columnCount = columnCount;
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
		return getIcon(getIconIndexInImage(key));
	}

	public Icon getIcon(int indexInImage) {
		if (indexInImage<0) return null;
		int x,y;
		if (columnCount<=0) {
			x = indexInImage*iconWidth;
			y = 0;
		} else {
			x = (indexInImage%columnCount)*iconWidth;
			y = (indexInImage/columnCount)*iconHeight;
		}
		return new ImageIcon(images.getSubimage( x,y, iconWidth,iconHeight ));
	}
	
	private enum Dummy {}
	
	public static class IndexOnlyIconSource extends IconSource<Dummy> {
		
		public IndexOnlyIconSource(int iconWidth, int iconHeight) { super(iconWidth, iconHeight); }
		public IndexOnlyIconSource(int iconWidth, int iconHeight, int columnCount) { super(iconWidth, iconHeight, columnCount); }

		@Override public Icon getIcon(Dummy key) {
		 	throw new IllegalArgumentException("Only indexed access allowed");
		}
		@Override protected int getIconIndexInImage(Dummy key) {
		 	throw new IllegalArgumentException("Only indexed access allowed");
		}
	}
}
