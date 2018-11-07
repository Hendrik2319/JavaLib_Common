package net.schwarzbaer.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconSource<E extends Enum<E>> {
	
	private final int offsetX;
	private final int offsetY;
	private final int iconWidth;
	private final int iconHeight;
	private final int columnCount;
	private BufferedImage images;
	private int[] iconOffsetX;
	private int[] iconWidths;
	
	public IconSource(int iconWidth, int iconHeight) {
		this(iconWidth,iconHeight,-1);
	}
	
	public IconSource(int iconWidth, int iconHeight, int columnCount) {
		this(0,0,iconWidth,iconHeight,columnCount);
	}
	
	public IconSource(int offsetX, int offsetY, int iconWidth, int iconHeight) {
		this(offsetX,offsetY,iconWidth,iconHeight,-1);
	}
	
	public IconSource(int iconHeight, Function<E,Integer> getIconWidth, E[] keys) {
		this(0,0,iconHeight,getIconWidth,keys);
	}
	
	public IconSource(int offsetX, int offsetY, int iconHeight, Function<E,Integer> getIconWidth, E[] keys) {
		this(offsetX,offsetY,-1,iconHeight,-1);
		
		this.iconOffsetX = new int[keys.length];
		this.iconWidths  = new int[keys.length];
		
		iconOffsetX[0] = 0;
		for (int i=0; i<iconOffsetX.length; i++) {
			iconWidths[i] = getIconWidth.apply(keys[i]);
			if (i+1<iconOffsetX.length)
				iconOffsetX[i+1] = iconOffsetX[i]+iconWidths[i];
		}
	}
	
	public IconSource(int offsetX, int offsetY, int iconWidth, int iconHeight, int columnCount) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.columnCount = columnCount;
		this.images = null;
		
		this.iconOffsetX = null;
		this.iconWidths = null;
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
		return key.ordinal();
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
		
		int x,y,localIconWidth = iconWidth;
		if (iconWidths!=null && iconOffsetX!=null) {
			localIconWidth = iconWidths[indexInImage];
			x = iconOffsetX[indexInImage];
			y = 0;
		} else if (columnCount<=0) {
			x = indexInImage*iconWidth;
			y = 0;
		} else {
			x = (indexInImage%columnCount)*iconWidth;
			y = (indexInImage/columnCount)*iconHeight;
		}
		
		return images.getSubimage( offsetX+x,offsetY+y, localIconWidth,iconHeight );
	}
	
	public static Icon cutIcon(Icon icon, int x, int y, int w, int h) {
		if (!(icon instanceof ImageIcon)) return icon;
		ImageIcon imageIcon = (ImageIcon)icon;
		
		BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(),imageIcon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
		bufferedImage.getGraphics().drawImage(imageIcon.getImage(),0,0,null);
		
		return new ImageIcon(bufferedImage.getSubimage(x,y,w,h));
	}

	public static Icon combine(Icon icon1, Icon icon2) {
		if (icon1==null) return icon2;
		if (icon2==null) return icon1;
//		if (!(icon1 instanceof ImageIcon)) throw new IllegalArgumentException("First icon parameter is not an instance of ImageIcon.");
//		if (!(icon2 instanceof ImageIcon)) throw new IllegalArgumentException("Second icon parameter is not an instance of ImageIcon.");
//		ImageIcon imageIcon1 = (ImageIcon)icon1;
//		ImageIcon imageIcon2 = (ImageIcon)icon2;
		
		int width  = Math.max( icon1.getIconWidth (), icon2.getIconWidth () );
		int height = Math.max( icon1.getIconHeight(), icon2.getIconHeight() );
		BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufferedImage.getGraphics();
		icon1.paintIcon(null, g, 0,0);
		icon2.paintIcon(null, g, 0,0);
//		g.drawImage(imageIcon1.getImage(),0,0,null);
//		g.drawImage(imageIcon2.getImage(),0,0,null);
		
		return new ImageIcon(bufferedImage);
	}

	public static Icon setSideBySide(Icon icon1, Icon icon2) {
		if (icon1==null) return icon2;
		if (icon2==null) return icon1;
		
		int width  = icon1.getIconWidth() + icon2.getIconWidth();
		int height = Math.max( icon1.getIconHeight(), icon2.getIconHeight() );
		BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufferedImage.getGraphics();
		icon1.paintIcon(null, g, 0,0);
		icon2.paintIcon(null, g, icon1.getIconWidth(),0);
		
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
