package net.schwarzbaer.image;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class BumpMapping {
	
	private Vector3D sun;
	private Color highlightColor;
	private Color faceColor;
	private Color lowlightColor;
	private TransferFunction getNormal;
	private double faceF;

	public BumpMapping(Vector3D sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunction getNormal) {
		this(sun, highlightColor, faceColor, lowlightColor, (x,y,width,height)->getNormal.getNormal(x,y));
	}
	public BumpMapping(Vector3D sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunctionPolar getNormal) {
		this(sun, highlightColor, faceColor, lowlightColor, (x,y,width,height)->{
			double y1 = y-height/2.0;
			double x1 = x-width/2.0;
			double w = Math.atan2(y1,x1);
			double r = Math.sqrt(x1*x1+y1*y1);
			return getNormal.getNormal(w,r);
		});
	}
	private BumpMapping(Vector3D sun, Color highlightColor, Color faceColor, Color lowlightColor, TransferFunction getNormal) {
		this.sun = sun.normalize();
		this.highlightColor = highlightColor;
		this.faceColor = faceColor;
		this.lowlightColor = lowlightColor;
		this.getNormal = getNormal;
		this.faceF = getF(new Vector3D(0,0,1));
	}

	public Image renderImage(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		int[] color = new int[4];
		color[3] = 255;
		for (int x=0; x<width; x++)
			for (int y=0; y<height; y++) {
				double f = getF(getNormal.getNormal(x,y,width,height));
				if (faceF<=f && f<=1) {
					f = (f-faceF)/(1-faceF);
					color[0] = (int) Math.round(highlightColor.getRed  ()*f + faceColor.getRed  ()*(1-f));
					color[1] = (int) Math.round(highlightColor.getGreen()*f + faceColor.getGreen()*(1-f));
					color[2] = (int) Math.round(highlightColor.getBlue ()*f + faceColor.getBlue ()*(1-f));
				} else if (0<=f && f<faceF) {
					f = f/faceF;
					color[0] = (int) Math.round(faceColor.getRed  ()*f + lowlightColor.getRed  ()*(1-f));
					color[1] = (int) Math.round(faceColor.getGreen()*f + lowlightColor.getGreen()*(1-f));
					color[2] = (int) Math.round(faceColor.getBlue ()*f + lowlightColor.getBlue ()*(1-f));
				} else {
					color[0] = 255;
					color[1] = 0;
					color[2] = 0;
				}
				raster.setPixel(x, y, color);
			}
		return image;
	}
	private double getF(Vector3D normal) {
		return Math.max(0,sun.dotP(normal));
	}
	
	public static interface NormalFunction {
		public Vector3D getNormal(int x, int y);
	}
	public static interface NormalFunctionPolar {
		public Vector3D getNormal(double w, double r);
	}
	private static interface TransferFunction {
		public Vector3D getNormal(int x, int y, int width, int height);
	}
	
	public static class Vector3D {
		double x,y,z;

		public Vector3D() { this(0,0,0); }
		public Vector3D(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public double dotP(Vector3D v) {
			return x*v.x+y*v.y+z*v.z;
		}
		public Vector3D normalize() { return mul(1/getLength()); }
		public Vector3D mul(double d) { return new Vector3D(x*d,y*d,z*d); }
		public double getLength() { return Math.sqrt(x*x+y*y+z*z); }
		
		public Vector3D rotateZ(double w) {
			return new Vector3D(
					x*Math.cos(w)-y*Math.sin(w),
					x*Math.sin(w)+y*Math.cos(w),
					z
			);
		}
		
	}
}
