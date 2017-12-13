package net.schwarzbaer.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public abstract class IconSource<E extends Enum<E>> {
	
	private final int iconWidth;
	private final int iconHeight;
	private final int columnCount;
	private BufferedImage images;
	private EnumMap<E, Icon>          enumIconCache;
	private EnumMap<E, BufferedImage> enumImageCache;
	private Icon[]          indexedIconCache;
	private BufferedImage[] indexedImageCache;
	
	public IconSource(int iconWidth, int iconHeight) {
		this(iconWidth,iconHeight,-1);
	}
	
	public IconSource(int iconWidth, int iconHeight, int columnCount) {
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.columnCount = columnCount;
		this.enumIconCache = null;
		this.enumImageCache = null;
		this.indexedIconCache = null;
		this.indexedImageCache = null;
		this.images = null;
	}
	
	private boolean exitsCache() {
		return enumIconCache!=null || indexedIconCache!=null || enumImageCache!=null || indexedImageCache!=null;
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
	
	public Icon getCachedIcon(E key) { return enumIconCache.get(key); }
	public void setCachedIcon(E key, Icon icon) { enumIconCache.put(key,icon); }
	public void cacheIcons(E[] keys) {
		if (keys.length==0) return;
		if (exitsCache()) System.err.println("Warning: Another cache already exists.");
		enumIconCache = new EnumMap<E,Icon>(keys[0].getDeclaringClass());
		for (E key:keys) enumIconCache.put(key, getIcon(key));
	}
	
	public BufferedImage getCachedImage(E key) { return enumImageCache.get(key); }
	public void setCachedImage(E key, BufferedImage image) { enumImageCache.put(key,image); }
	public void cacheImages(E[] keys) {
		if (keys.length==0) return;
		if (exitsCache()) System.err.println("Warning: Another cache already exists.");
		enumImageCache = new EnumMap<E,BufferedImage>(keys[0].getDeclaringClass());
		for (E key:keys) enumImageCache.put(key, getImage(key));
	}
	
	public Icon getCachedIcon(int key) { return indexedIconCache[key]; }
	public void setCachedIcon(int key, Icon icon) { indexedIconCache[key]=icon; }
	public void cacheIcons(int numberOfIcons) {
		if (numberOfIcons==0) return;
		if (exitsCache()) System.err.println("Warning: Another cache already exists.");
		indexedIconCache = new Icon[numberOfIcons];
		for (int i=0; i<numberOfIcons; ++i) indexedIconCache[i] = getIcon(i);
	}
	
	public BufferedImage getCachedImage(int key) { return indexedImageCache[key]; }
	public void setCachedImage(int key, BufferedImage image) { indexedImageCache[key]=image; }
	public void cacheImages(int numberOfImages) {
		if (numberOfImages==0) return;
		if (exitsCache()) System.err.println("Warning: Another cache already exists.");
		indexedImageCache = new BufferedImage[numberOfImages];
		for (int i=0; i<numberOfImages; ++i) indexedImageCache[i] = getImage(i);
	}

	protected abstract int getIconIndexInImage(E key);
	
	public Icon          getIcon (E key) { return getIcon (getIconIndexInImage(key)); }
	public BufferedImage getImage(E key) { return getImage(getIconIndexInImage(key)); }

	public Icon getIcon(int indexInImage) {
		BufferedImage subimage = getImage(indexInImage);
		if (subimage==null) return null;
		return new ImageIcon(subimage);
	}
	
	public BufferedImage getImage(int indexInImage) {
		if (indexInImage<0) return null;
		
		int x,y;
		if (columnCount<=0) {
			x = indexInImage*iconWidth;
			y = 0;
		} else {
			x = (indexInImage%columnCount)*iconWidth;
			y = (indexInImage/columnCount)*iconHeight;
		}
		
		return images.getSubimage( x,y, iconWidth,iconHeight );
	}
	
	public static Icon cutIcon(Icon icon, int x, int y, int w, int h) {
		if (!(icon instanceof ImageIcon)) return icon;
		ImageIcon imageIcon = (ImageIcon)icon;
		
		BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(),imageIcon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
		bufferedImage.getGraphics().drawImage(imageIcon.getImage(),0,0,null);
		
		return new ImageIcon(bufferedImage.getSubimage(x,y,w,h));
	}

	public static Icon combine(Icon icon1, Icon icon2) {
		if (!(icon1 instanceof ImageIcon)) return icon1;
		if (!(icon2 instanceof ImageIcon)) return icon1;
		ImageIcon imageIcon1 = (ImageIcon)icon1;
		ImageIcon imageIcon2 = (ImageIcon)icon2;
		
		BufferedImage bufferedImage = new BufferedImage(imageIcon1.getIconWidth(),imageIcon1.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufferedImage.getGraphics();
		g.drawImage(imageIcon1.getImage(),0,0,null);
		g.drawImage(imageIcon2.getImage(),0,0,null);
		
		return new ImageIcon(bufferedImage);
	}

	private enum Dummy {}
	
	public static class IndexOnlyIconSource extends IconSource<Dummy> {
		
		public IndexOnlyIconSource(int iconWidth, int iconHeight) { super(iconWidth, iconHeight); }
		public IndexOnlyIconSource(int iconWidth, int iconHeight, int columnCount) { super(iconWidth, iconHeight, columnCount); }

		@Override protected int getIconIndexInImage(Dummy key) {
		 	throw new IllegalArgumentException("Only indexed access allowed");
		}
	}
}
