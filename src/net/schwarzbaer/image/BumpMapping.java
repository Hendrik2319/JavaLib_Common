package net.schwarzbaer.image;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class BumpMapping {

	private static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}
	
	private Shading shading;
	private TransferFunction normalFunction;
	private ImageCache<Image> imageCache;

	public BumpMapping(Vector3D sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunctionCart getNormal) {
		this(false);
		setShading(new Shading.GUISurfaceShading(sun, highlightColor, faceColor, lowlightColor));
		setNormalFunction(getNormal);
	}
	public BumpMapping(Vector3D sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunctionPolar getNormal) {
		this(false);
		setShading(new Shading.GUISurfaceShading(sun, highlightColor, faceColor, lowlightColor));
		setNormalFunction(getNormal);
	}
	public BumpMapping(boolean cachedImage) {
		imageCache = !cachedImage?null:new ImageCache<Image>(this::renderImageUncached);
	}

	public void setNormalFunction(NormalFunctionCart normalFunction) {
		setNormalFunction((x,y,width,height)->{
			return normalFunction.getNormal(x,y);
		});
	}
	public void setNormalFunction(NormalFunctionPolar normalFunction) {
		setNormalFunction((x,y,width,height)->{
			double y1 = y-height/2.0;
			double x1 = x-width/2.0;
			double w = Math.atan2(y1,x1);
			double r = Math.sqrt(x1*x1+y1*y1);
			return normalFunction.getNormal(w,r);
		});
	}
	private void setNormalFunction(TransferFunction normalFunction) {
		this.normalFunction = normalFunction;
		if (imageCache!=null) imageCache.resetImage();
	}
	public void setSun(double x, double y, double z) {
		shading.setSun(x,y,z);
		if (imageCache!=null) imageCache.resetImage();
	}
	public void setShading(Shading shading) {
		this.shading = shading;
		if (imageCache!=null) imageCache.resetImage();
	}
	public void resetImage() {
		if (imageCache!=null) imageCache.resetImage();
	}

	
	public Image renderImage(int width, int height) {
		if (imageCache!=null) return imageCache.getImage(width, height);
		return renderImageUncached(width, height);
	}
	public Image renderImageUncached(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		for (int x=0; x<width; x++)
			for (int y=0; y<height; y++) {
				raster.setPixel(x, y, shading.getColor(normalFunction.getNormal(x,y,width,height)));
			}
		return image;
	}
	
	public static abstract class Shading {
		protected int[] color;
		protected Vector3D sun;
		
		private Shading(Vector3D sun) {
			this.sun = sun.normalize();
			this.color = new int[4];
		}
		
		public void setSun(double x, double y, double z) {
			sun = new Vector3D(x,y,z).normalize();
		}

		public abstract int[] getColor(Vector3D normal);
		
		public static class NormalImage extends Shading {
			
			public NormalImage() {
				super(new Vector3D(0,0,1));
			}

			@Override
			public int[] getColor(Vector3D normal) {
				color[0] = (int) Math.round(((normal.x+1)/2)*255); Assert(0<=color[0] && color[0]<=255);
				color[1] = (int) Math.round(((normal.y+1)/2)*255); Assert(0<=color[1] && color[1]<=255);
				color[2] = (int) Math.round(((normal.z+1)/2)*255); Assert(0<=color[2] && color[2]<=255);
				color[3] = 255;
				return color;
			}
			
		}
		
		public static class MaterialShading extends Shading {
			
			private Color materialColor;
			private double minIntensity;
			private double phongExp;
			private Vector3D maxRefl;

			public MaterialShading(Vector3D sun, Color materialColor, double minIntensity, double phongExp) {
				super(sun);
				this.materialColor = materialColor;
				this.minIntensity = Math.max(0,Math.min(minIntensity,1));
				this.phongExp = Math.max(0,phongExp);
				maxRefl = new Vector3D(0,0,1).add(sun).normalize();
			}
			
			public Color getMaterialColor() { return materialColor; }
			public void  setMaterialColor(Color color) { this.materialColor = color; }

			public double getMinIntensity() { return minIntensity; }
			public double getPhongExp    () { return phongExp    ; }
			public void setMinIntensity(double minIntensity) { this.minIntensity = minIntensity; }
			public void setPhongExp    (double phongExp    ) { this.phongExp     = phongExp    ; }

			@Override
			public void setSun(double x, double y, double z) {
				super.setSun(x, y, z);
				maxRefl = new Vector3D(0,0,1).add(sun).normalize();
				//System.out.println("maxRefl = "+maxRefl);
			}
			
			@Override
			public int[] getColor(Vector3D normal) {
				color[3] = 255;
				double intensityDiff = Math.max( minIntensity, getF(sun,normal) );
				double intensityRefl = getF(maxRefl,normal);
				intensityRefl = Math.max(0,Math.min(intensityRefl,1));
				intensityRefl = Math.pow(intensityRefl,phongExp);
				
				color[0] = (int) Math.round(materialColor.getRed  ()*intensityDiff);
				color[1] = (int) Math.round(materialColor.getGreen()*intensityDiff);
				color[2] = (int) Math.round(materialColor.getBlue ()*intensityDiff);
				
				color[0] = (int) Math.round(255-(255-color[0])*(1-intensityRefl));
				color[1] = (int) Math.round(255-(255-color[1])*(1-intensityRefl));
				color[2] = (int) Math.round(255-(255-color[2])*(1-intensityRefl));
				
				return color;
			}
			
			private double getF(Vector3D v1, Vector3D v2) {
				return Math.max(0,v1.dotP(v2));
			}
			
		}
		
		public static class GUISurfaceShading extends Shading {
			private Color highlightColor;
			private Color faceColor;
			private Color shadowColor;
			private double faceF;
			
			public GUISurfaceShading(Vector3D sun, Color highlightColor, Color faceColor, Color shadowColor) {
				super(sun);
				this.highlightColor = highlightColor;
				this.faceColor = faceColor;
				this.shadowColor = shadowColor;
				this.faceF = getF(new Vector3D(0,0,1));
			}
			
			public Color getHighlightColor() { return highlightColor; }
			public Color getFaceColor     () { return faceColor; }
			public Color getShadowColor   () { return shadowColor; }

			public void setHighlightColor(Color color) { this.highlightColor = color; }
			public void setFaceColor     (Color color) { this.faceColor      = color; }
			public void setShadowColor   (Color color) { this.shadowColor    = color; }

			@Override
			public void setSun(double x, double y, double z) {
				super.setSun(x, y, z);
				faceF = getF(new Vector3D(0,0,1));
			}

			@Override
			public int[] getColor(Vector3D normal) {
				color[3] = 255;
				double f = getF(normal);
				if (faceF<=f && f<=1) {
					f = (f-faceF)/(1-faceF);
					color[0] = (int) Math.round(highlightColor.getRed  ()*f + faceColor.getRed  ()*(1-f));
					color[1] = (int) Math.round(highlightColor.getGreen()*f + faceColor.getGreen()*(1-f));
					color[2] = (int) Math.round(highlightColor.getBlue ()*f + faceColor.getBlue ()*(1-f));
				} else if (0<=f && f<faceF) {
					f = f/faceF;
					color[0] = (int) Math.round(faceColor.getRed  ()*f + shadowColor.getRed  ()*(1-f));
					color[1] = (int) Math.round(faceColor.getGreen()*f + shadowColor.getGreen()*(1-f));
					color[2] = (int) Math.round(faceColor.getBlue ()*f + shadowColor.getBlue ()*(1-f));
				} else {
					color[0] = 255;
					color[1] = 0;
					color[2] = 0;
				}
				return color;
			}
			
			private double getF(Vector3D normal) {
				return Math.max(0,sun.dotP(normal));
			}
		}
	}
	
	public static interface NormalFunctionCart {
		public Vector3D getNormal(int x, int y);
	}
	public static interface NormalFunctionPolar {
		public Vector3D getNormal(double w, double r);
	}
	private static interface TransferFunction {
		public Vector3D getNormal(int x, int y, int width, int height);
	}
	
	public static class Vector3D {
		public double x,y,z;

		public Vector3D() { this(0,0,0); }
		public Vector3D(double x, double y, double z) { set(x,y,z); }
		
		public Vector3D add(Vector3D v) {
			return new Vector3D(x+v.x,y+v.y,z+v.z);
		}
		public void set(double x, double y, double z) {
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
		
		public static Vector3D blend(double f, double fmin, double fmax, Vector3D vmin, Vector3D vmax) {
			f = (f-fmin)/(fmax-fmin); 
			return new Vector3D(
					vmax.x*f+vmin.x*(1-f),
					vmax.y*f+vmin.y*(1-f),
					vmax.z*f+vmin.z*(1-f)
				);
		}
		@Override
		public String toString() {
			return "Vector3D [x=" + x + ", y=" + y + ", z=" + z + "]";
		}
		
	}
}
