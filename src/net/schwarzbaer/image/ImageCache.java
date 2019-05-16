package net.schwarzbaer.image;

import java.awt.Image;

public class ImageCache<I extends Image> {
	private I image;
	private int width;
	private int height;
	private ImageSource<I> imageSource;
	
	public ImageCache(ImageSource<I> imageSource) {
		this.imageSource = imageSource;
		this.image = null;
		this.width = 0;
		this.height = 0;
	}
	
	public static interface ImageSource<I> {
		public I getImage(int width, int height);
	}

	public I getImage(int width, int height) {
		if (image==null || this.width!=width || this.height!=height) {
			image = imageSource.getImage(width, height);
			this.width = width;
			this.height = height;
		}
		return image;
	}
}
