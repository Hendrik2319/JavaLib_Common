package net.schwarzbaer.image;

import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Comparator;

public class ImageSimilarity<ImageID> {
	
	private RasterSource<ImageID> rasterSource;
	
	public ImageSimilarity(RasterSource<ImageID> rasterSource) {
		this.rasterSource = rasterSource;
	}
	
	public static <ImageID> int[] computeOrder(ImageID baseImageID, ImageID[] imageIDs, RasterSource<ImageID> rasterSource) {
		return new ImageSimilarity<ImageID>(rasterSource).computeOrder(baseImageID, imageIDs);
	}
	
	public int[] computeOrder(ImageID baseImageID, ImageID[] imageIDs) {
		ComparableImage baseImage = new ComparableImage(-1,rasterSource.createRaster(baseImageID,0xFFFFFF,256,256));
		
		ComparableImage[] images = new ComparableImage[imageIDs.length];
		for (int i=0; i<imageIDs.length; i++) {
			images[i] = new ComparableImage(i,rasterSource.createRaster(imageIDs[i],0xFFFFFF,256,256));
			images[i].similarity = images[i].computeSimilarityTo(baseImage);
		}
		
		int[] sortedIndexes = Arrays.stream(images)
				.sorted(Comparator.comparing(img->Double.isNaN(img.similarity) ? null : img.similarity, Comparator.nullsLast(Comparator.naturalOrder())))
				.mapToInt(img->img.index)
				.toArray();
		return sortedIndexes;
	}
	
	private static class ComparableImage {
		final WritableRaster raster;
		final int index;
		double similarity;

		private ComparableImage(int index, WritableRaster raster) {
			this.raster = raster;
			this.index = index;
			this.similarity = Double.NaN;
		}

		private double computeSimilarityTo(ComparableImage other) {
			if (this .raster==null) return Double.NaN;
			if (other.raster==null) return Double.NaN;
			
			Rectangle bounds      = this .raster.getBounds();
			Rectangle otherBounds = other.raster.getBounds();
			if (!bounds.equals(otherBounds))
				throw new IllegalArgumentException();
			
			double[] p1 = new double[4];
			double[] p2 = new double[4];
			double similarity = 0;
			for (int x=bounds.x; x<bounds.width+bounds.x; x++)
				for (int y=bounds.y; y<bounds.height+bounds.y; y++) {
					this .raster.getPixel(x, y, p1);
					other.raster.getPixel(x, y, p2);
					similarity += (float) Math.sqrt( (p1[0]-p2[0])*(p1[0]-p2[0]) + (p1[1]-p2[1])*(p1[1]-p2[1]) + (p1[2]-p2[2])*(p1[2]-p2[2]) );
				}
			return similarity;
		}
	}
	
	public interface RasterSource<ImageID> {
		WritableRaster createRaster(ImageID image, int backgroundColor, int width, int height);
	}
}
