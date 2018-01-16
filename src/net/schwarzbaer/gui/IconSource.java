package net.schwarzbaer.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconSource<E extends Enum<E>> {
	
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
	
	public CachedIcons<E> cacheIcons(E[] keys) {
		return new CachedIcons<E>(this,keys);
	}
	
	public CachedImages<E> cacheImages(E[] keys) {
		return new CachedImages<E>(this,keys);
	}
	
	public CachedIndexedIcons cacheIcons(int numberOfIcons) {
		return new CachedIndexedIcons(this,numberOfIcons);
	}
	
	public CachedIndexedImages cacheImages(int numberOfImages) {
		return new CachedIndexedImages(this,numberOfImages);
	}

	protected int getIconIndexInImage(E key) {
		if (key!=null) return key.ordinal();
	 	throw new IllegalArgumentException("Unknown icon key: "+key);
	}
	
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
	
	public static class CachedImages<E extends Enum<E>> {
		private EnumMap<E, BufferedImage> enumImageCache;
		public CachedImages(IconSource<E> iconSource, E[] keys) {
			enumImageCache = new EnumMap<E,BufferedImage>(keys[0].getDeclaringClass());
			for (E key:keys) enumImageCache.put(key, iconSource.getImage(key));
		}
		public BufferedImage getCachedImage(E key) { return enumImageCache.get(key); }
		public void setCachedImage(E key, BufferedImage image) { enumImageCache.put(key, image); }
	}
	
	public static class CachedIcons<E extends Enum<E>> {
		private EnumMap<E, Icon> enumIconCache;
		public CachedIcons(IconSource<E> iconSource, E[] keys) {
			enumIconCache = new EnumMap<E,Icon>(keys[0].getDeclaringClass());
			for (E key:keys) enumIconCache.put(key, iconSource.getIcon(key));
		}
		public Icon getCachedIcon(E key) { return enumIconCache.get(key); }
		public void setCachedIcon(E key, Icon icon) { enumIconCache.put(key, icon); }
	}

	public static class CachedIndexedImages {
		private BufferedImage[] cache;

		public CachedIndexedImages(IconSource<?> iconSource, int numberOfImages) {
			cache = new BufferedImage[numberOfImages];
			for (int i=0; i<numberOfImages; ++i) cache[i] = iconSource.getImage(i);
		}
		public BufferedImage getCachedImage(int key) { return cache[key]; }
		public void setCachedImage(int key, BufferedImage image) { cache[key]=image; }
	}
	
	public static class CachedIndexedIcons {
		private Icon[] cache;

		public CachedIndexedIcons(IconSource<?> iconSource, int numberOfIcons) {
			cache = new Icon[numberOfIcons];
			for (int i=0; i<numberOfIcons; ++i) cache[i] = iconSource.getIcon(i);
		}
		public Icon getCachedIcon(int key) { return cache[key]; }
		public void setCachedIcon(int key, Icon icon) { cache[key]=icon; }
	}
}
