package net.schwarzbaer.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.BiFunction;

public class BumpMapping {

	private static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}
	
	private Shading shading;
	private NormalFunction normalFunction;
	private ImageCache<BufferedImage> imageCache;

	public BumpMapping(Normal sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunction.Cart getNormal) {
		this(false);
		setShading(new Shading.GUISurfaceShading(sun, highlightColor, faceColor, lowlightColor));
		setNormalFunction(getNormal);
	}
	public BumpMapping(Normal sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunction.Polar getNormal) {
		this(false);
		setShading(new Shading.GUISurfaceShading(sun, highlightColor, faceColor, lowlightColor));
		setNormalFunction(getNormal);
	}
	public BumpMapping(boolean cachedImage) {
		imageCache = !cachedImage?null:new ImageCache<>(this::renderImage_uncached);
	}

	public void setNormalMap(Normal[][] normalMap) {
		setNormalFunction((x_,y_,width,height)->{
			int x = (int) Math.round(x_);
			int y = (int) Math.round(y_);
			if (x<0 || x>=normalMap   .length) return new Normal(0,0,1);
			if (y<0 || y>=normalMap[x].length) return new Normal(0,0,1);
			return normalMap[x][y];
		});
	}
	
	public void setHeightMap(float[][] heightMap, double cornerScale) {
		setHeightMap(heightMap,null,cornerScale);
	}
	public void setHeightMap(float[][] heightMap, Color[][] colorMap, double cornerScale) {
		int width = heightMap.length;
		int height = heightMap[0].length;;
		Normal[][] normalMap = new Normal[width][height];
		for (int x1=0; x1<width; ++x1)
			for (int y1=0; y1<height; ++y1) {
				MutableNormal base = new MutableNormal(0,0,0,colorMap==null?null:colorMap[x1][y1]);
				addNormal(base,computeNormal(heightMap,x1,y1,+1, 0),1); 
				addNormal(base,computeNormal(heightMap,x1,y1, 0,+1),1); 
				addNormal(base,computeNormal(heightMap,x1,y1,-1, 0),1); 
				addNormal(base,computeNormal(heightMap,x1,y1, 0,-1),1);
				if (cornerScale>0) {
					addNormal(base,computeNormal(heightMap,x1,y1,+1,-1),cornerScale);
					addNormal(base,computeNormal(heightMap,x1,y1,+1,+1),cornerScale); 
					addNormal(base,computeNormal(heightMap,x1,y1,-1,+1),cornerScale); 
					addNormal(base,computeNormal(heightMap,x1,y1,-1,-1),cornerScale); 
				}
				normalMap[x1][y1] = base.toNormal().normalize();
			}
		setNormalMap(normalMap);
	}
	private void addNormal(MutableNormal base, Normal n, double scale) {
		if (n != null) {
			base.x += n.x*scale;
			base.y += n.y*scale;
			base.z += n.z*scale;
		}
	}
	private Normal computeNormal(float[][] heightMap, int x, int y, int dx, int dy) {
		if (x+dx<0 || x+dx>=heightMap   .length) return null;
		if (y+dy<0 || y+dy>=heightMap[0].length) return null;
		float dh = heightMap[x][y]-heightMap[x+dx][y+dy];
		if ( (dx!=0) && (dy!=0) ) {
			double w = Math.atan2(dy, dx);
			double r = Math.sqrt(dx*dx+dy*dy);
			return new Normal(dh,0,r).normalize().rotateZ(w);
		}
		if (dx!=0) return new Normal(dh*dx,0,Math.abs(dx)).normalize();
		if (dy!=0) return new Normal(0,dh*dy,Math.abs(dy)).normalize();
		return null;
	}
	
	public BumpMapping setNormalFunction(NormalFunction.Cart normalFunction) {
		return setNormalFunction(NormalFunction.convert(normalFunction));
	}
	public BumpMapping setNormalFunction(NormalFunction.Polar normalFunction) {
		return setNormalFunction(NormalFunction.convert(normalFunction));
	}
	public BumpMapping setNormalFunction(NormalFunction normalFunction) {
		this.normalFunction = normalFunction;
		if (imageCache!=null) imageCache.resetImage();
		return this;
	}
	public void setSun(double x, double y, double z) {
		shading.setSun(x,y,z);
		if (imageCache!=null) imageCache.resetImage();
	}
//	public void getSun(Normal sunOut) {
//		sunOut.x = shading.sun.x;
//		sunOut.y = shading.sun.y;
//		sunOut.z = shading.sun.z;
//	}
	public void setShading(Shading shading) {
		this.shading = shading;
		if (imageCache!=null) imageCache.resetImage();
	}
	public void resetImage() {
		if (imageCache!=null) imageCache.resetImage();
	}

	
	public BufferedImage renderImage(int width, int height) {
		if (imageCache!=null) return imageCache.getImage(width, height);
		return renderImage_uncached(width, height);
	}
	public BufferedImage renderImage_uncached(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		for (int x=0; x<width; x++)
			for (int y=0; y<height; y++) {
				raster.setPixel(x, y, shading.getColor(x,y,width,height,normalFunction.getNormal(x,y,width,height)));
			}
		return image;
	}
	public BufferedImage renderScaledImage_uncached(int width, int height, float scale) {
		int scaledWidth  = Math.round(width *scale);
		int scaledHeight = Math.round(height*scale);
		BufferedImage image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		for (int x=0; x<scaledWidth; x++)
			for (int y=0; y<scaledHeight; y++) {
				raster.setPixel(x, y, shading.getColor(x/scale,y/scale,width,height,normalFunction.getNormal(x/scale,y/scale,width,height)));
			}
		return image;
	}
	
	public static abstract class Shading {
		protected int[] color;
		protected Normal sun;
		
		private Shading(Normal sun) {
			this.sun = sun.normalize();
			this.color = new int[4];
		}
		
		public void setSun(double x, double y, double z) {
			sun = new Normal(x,y,z).normalize();
		}

		public abstract int[] getColor(double x, double y, double width, double height, Normal normal);
		
		public static class MixedShading extends Shading {
			
			private final Shading[] shadings;
			private Indexer indexer;
			
			public MixedShading(Indexer.Cart  indexer, Shading...shadings) { this(Indexer.convert(indexer),shadings); }
			public MixedShading(Indexer.Polar indexer, Shading...shadings) { this(Indexer.convert(indexer),shadings); }
			public MixedShading(Indexer      indexer, Shading...shadings) {
				super(new Normal(0,0,1));
				this.indexer = indexer;
				this.shadings = shadings;
				Assert(indexer!=null);
				Assert(shadings!=null);
				Assert(shadings.length>0);
			}

			@Override
			public void setSun(double x, double y, double z) {
				super.setSun(x, y, z);
				for (Shading sh:shadings)
					sh.setSun(x, y, z);
			}
			
			@Override
			public int[] getColor(double x, double y, double width, double height, Normal normal) {
				int i = indexer.getIndex(x, y, width, height);
				Assert(0<=i);
				Assert(i<shadings.length);
				return shadings[i].getColor(x, y, width, height, normal);
			}
		}
		
		public static class NormalImage extends Shading {
			
			public NormalImage() {
				super(new Normal(0,0,1));
			}

			@Override
			public int[] getColor(double x, double y, double width, double height, Normal normal) {
				color[0] = (int) Math.round(((normal.x+1)/2)*255); Assert(0<=color[0] && color[0]<=255);
				color[1] = (int) Math.round(((normal.y+1)/2)*255); Assert(0<=color[1] && color[1]<=255);
				color[2] = (int) Math.round(((normal.z+1)/2)*255); Assert(0<=color[2] && color[2]<=255);
				color[3] = 255;
				return color;
			}
			
		}
		
		public static class MaterialShading extends Shading {
			
			private Color materialColor;
			private double ambientIntensity;
			private double phongExp;
			private Normal maxSunRefl;
			private boolean withReflection;
			private double reflectionIntensity;

			public MaterialShading(Normal sun, Color materialColor, double ambientIntensity, double phongExp, boolean withReflection, double reflectionIntensity) {
				super(sun);
				this.materialColor = materialColor;
				this.ambientIntensity = Math.max(0,Math.min(ambientIntensity,1));
				this.phongExp = Math.max(0,phongExp);
				this.withReflection = withReflection;
				this.reflectionIntensity = Math.max(0,Math.min(reflectionIntensity,1));
				updateMaxSunRefl();
			}
			
			public Color getMaterialColor() { return materialColor; }
			public void  setMaterialColor(Color color) { this.materialColor = color; }

			public double  getAmbientIntensity() { return ambientIntensity   ; }
			public double  getPhongExp        () { return phongExp           ; }
			public boolean getReflection      () { return withReflection     ; }
			public double  getReflIntensity   () { return reflectionIntensity; }
			public void setAmbientIntensity(double  ambientIntensity   ) { this.ambientIntensity    = Math.max(0,Math.min(ambientIntensity,1)); }
			public void setPhongExp        (double  phongExp           ) { this.phongExp            = Math.max(0,phongExp); }
			public void setReflection      (boolean withReflection     ) { this.withReflection      = withReflection  ; }
			public void setReflIntensity   (double  reflectionIntensity) { this.reflectionIntensity = Math.max(0,Math.min(reflectionIntensity,1)); }

			@Override
			public void setSun(double x, double y, double z) {
				super.setSun(x, y, z);
				updateMaxSunRefl();
				//System.out.println("maxRefl = "+maxRefl);
			}

			private void updateMaxSunRefl() {
				maxSunRefl = new Normal(0,0,1).add(sun).normalize();
			}
			
			@Override
			public int[] getColor(double x, double y, double width, double height, Normal normal) {
				
				Color c = normal.color==null?materialColor:normal.color;
				double diffuseIntensity = getF(sun,normal);
				double intensity = mul_inverse(ambientIntensity, diffuseIntensity);
				color[0] = (int) Math.round(c.getRed  ()*intensity);
				color[1] = (int) Math.round(c.getGreen()*intensity);
				color[2] = (int) Math.round(c.getBlue ()*intensity);
				
				if (withReflection) {
					//add( getReflectedLandscape(Math.atan2(normal.z,normal.y)), 0.8 );
					Color c2 = getReflectedLandscape(Math.atan2(normal.z,normal.y));
					desaturate(reflectionIntensity);
					//c2 = brighter(c2, reflectionIntensity);
					mul_inverse( c2 );
					//intensityDiff = 1;
				}
				
				double intensityRefl = getF(maxSunRefl,normal);
				intensityRefl = Math.max(0,Math.min(intensityRefl,1));
				intensityRefl = Math.pow(intensityRefl,phongExp);
				brighter(intensityRefl);
				
				color[3] = 255;
				return color;
			}

			private int brighter(int c, double f) {
				return (int) Math.round(255-(255-c)*(1-f));
			}
			@SuppressWarnings("unused")
			private Color brighter(Color c, double f) {
				int r = brighter(c.getRed  (),f);
				int g = brighter(c.getGreen(),f);
				int b = brighter(c.getBlue (),f);
				return new Color(r, g, b);
			}
			private void brighter(double f) {
				color[0] = brighter(color[0],f);
				color[1] = brighter(color[1],f);
				color[2] = brighter(color[2],f);
			}
			
			private double mul_inverse(double f1, double f2) {
				f1 = Math.max(0,Math.min(f1,1));
				f2 = Math.max(0,Math.min(f2,1));
				return 1-(1-f1)*(1-f2);
			}
			
			private void desaturate(double f) {
				int r = color[0];
				int g = color[1];
				int b = color[2];
				int gray = (r+g+b)/3;
				color[0] = (int) Math.floor( r*(1-f) + gray*f );
				color[1] = (int) Math.floor( g*(1-f) + gray*f );
				color[2] = (int) Math.floor( b*(1-f) + gray*f );
			}

			@SuppressWarnings("unused")
			private void add(Color c, float scale) {
				color[0] = Math.min( 255, (int) Math.floor((c.getRed  ()+color[0])*scale) );
				color[1] = Math.min( 255, (int) Math.floor((c.getGreen()+color[1])*scale) );
				color[2] = Math.min( 255, (int) Math.floor((c.getBlue ()+color[2])*scale) );
			}
			
			private void mul_inverse(Color c) {
				color[0] = (int) Math.floor( mul_inverse(c.getRed  ()/255f,color[0]/255f)*255 );
				color[1] = (int) Math.floor( mul_inverse(c.getGreen()/255f,color[1]/255f)*255 );
				color[2] = (int) Math.floor( mul_inverse(c.getBlue ()/255f,color[2]/255f)*255 );
			}

			private Color getReflectedLandscape(double angle) {
				// angle == 0    --> down
				// angle == PI/2 --> to viewer
				// angle == PI   --> up
				double f;
				Color minC,maxC;
				if (angle<Math.PI/2) {
					f = Math.max(0, angle)/(Math.PI/2);
//					minC = new Color(0x442c16);
//					maxC = new Color(0x202020);
					minC = new Color(0xb57652);
					maxC = new Color(0xc7c7c7);
				} else {
					f = (Math.min(Math.PI, angle)-Math.PI/2)/(Math.PI/2);
//					minC = new Color(0x202020);
//					maxC = new Color(0x002d4c);
					minC = new Color(0xc7c7c7);
					maxC = new Color(0x60c7ff);
				}
				
				int r = (int)(Math.floor( (minC.getRed  ()*(1-f)) + (maxC.getRed  ()*f))*reflectionIntensity );
				int g = (int)(Math.floor( (minC.getGreen()*(1-f)) + (maxC.getGreen()*f))*reflectionIntensity );
				int b = (int)(Math.floor( (minC.getBlue ()*(1-f)) + (maxC.getBlue ()*f))*reflectionIntensity );
				return new Color(r, g, b);
			}

			private double getF(Normal v1, Normal v2) {
				return Math.max(0,v1.dotP(v2));
			}
			
		}
		
		public static class GUISurfaceShading extends Shading {
			private Color highlightColor;
			private Color faceColor;
			private Color shadowColor;
			private double faceF;
			
			public GUISurfaceShading(Normal sun, Color highlightColor, Color faceColor, Color shadowColor) {
				super(sun);
				this.highlightColor = highlightColor;
				this.faceColor = faceColor;
				this.shadowColor = shadowColor;
				this.faceF = getAbsF(new Normal(0,0,1));
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
				faceF = getAbsF(new Normal(0,0,1));
			}

			@Override
			public int[] getColor(double x, double y, double width, double height, Normal normal) {
				color[3] = 255;
				double f1 = getF(normal);
				double f = Math.max(0,f1);
				
				if ( (faceF<f && f<=1) || (faceF>0 && f==faceF)) {
					if (faceF==1) f = 0;
					else f = (f-faceF)/(1-faceF);
					color[0] = (int) Math.round(highlightColor.getRed  ()*f + faceColor.getRed  ()*(1-f));
					color[1] = (int) Math.round(highlightColor.getGreen()*f + faceColor.getGreen()*(1-f));
					color[2] = (int) Math.round(highlightColor.getBlue ()*f + faceColor.getBlue ()*(1-f));
					
				} else if ( (0<=f && f<faceF) || (faceF==0 && f==faceF) ) {
					if (faceF==0) { if (f1==0) f=1; else f=0; }
					else f = f/faceF;
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
			
			private double getAbsF(Normal normal) {
				return Math.max(0,getF(normal));
			}

			private double getF(Normal normal) {
				return sun.dotP(normal);
			}
		}
	}
	
	public interface Indexer {
		
		public int getIndex(double x, double y, double width, double height);
		
		static Indexer convert(Cart indexer) {
			return (x,y,width,height)->{
				return indexer.getIndex(x,y);
			};
		}
		static Indexer convert(Polar indexer) {
			return (x,y,width,height)->{
				double y1 = y-height/2.0;
				double x1 = x-width/2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return indexer.getIndex(w,r);
			};
		}
		public interface Cart  { public int getIndex(double x, double y); }
		public interface Polar { public int getIndex(double w, double r); }
	}
	
	public interface Colorizer {
		
		public Color getColor(double x, double y, double width, double height);
		
		static Colorizer convert(Cart colorizer) {
			return (x,y,width,height)->{
				return colorizer.getColor(x,y);
			};
		}
		static Colorizer convert(Polar colorizer) {
			return (x,y,width,height)->{
				double y1 = y-height/2.0;
				double x1 = x-width/2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return colorizer.getColor(w,r);
			};
		}
		
		public interface Cart  { public Color getColor(double x, double y); }
		public interface Polar { public Color getColor(double w, double r); }
	}

	public static interface NormalFunction {
		public Normal getNormal(double x, double y, double width, double height);
		
		static NormalFunction convert(Cart normalFunction) {
			return (x,y,width,height)->{
				return normalFunction.getNormal(x,y);
			};
		}
		static NormalFunction convert(Polar normalFunction) {
			return (x,y,width,height)->{
				double y1 = y-height/2.0;
				double x1 = x-width/2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return normalFunction.getNormal(w,r);
			};
		}
		
		public static interface Cart  { public Normal getNormal(double x, double y); }
		public static interface Polar { public Normal getNormal(double w, double r); }
	}
	
	public static class MutableNormal {
		public double x,y,z;
		public final Color color;
		public MutableNormal(Normal n) { this(n.x,n.y,n.z,n.color); }
		public MutableNormal(double x, double y, double z, Color color) { this.color=color; this.x=x; this.y=y; this.z=z; }
		public Normal toNormal() { return new Normal( x,y,z, color ); }
	}
	
	public static class Normal {
		public final double x,y,z;
		public final Color color;

		public Normal() { this(0,0,0); }
		public Normal(Normal n) { this(n.x,n.y,n.z,n.color); }
		public Normal(Normal n, Color color) { this(n.x,n.y,n.z,color); }
		public Normal(double x, double y, double z) { this(x,y,z,null); }
		public Normal(double x, double y, double z, Color color) { this.color=color; this.x=x; this.y=y; this.z=z; }
		
		public Normal add(Normal v) {
			return new Normal(x+v.x,y+v.y,z+v.z,color);
		}
		public double dotP(Normal v) {
			return x*v.x+y*v.y+z*v.z;
		}
		public Normal normalize() { return mul(1/getLength()); }
		public Normal mul(double d) { return new Normal(x*d,y*d,z*d,color); }
		public double getLength() { return Math.sqrt(x*x+y*y+z*z); }
		
		public Normal rotateZ(double w) {
			return new Normal(
				x*Math.cos(w)-y*Math.sin(w),
				x*Math.sin(w)+y*Math.cos(w),
				z,
				color
			);
		}
		
		public static Normal blend(double f, double fmin, double fmax, Normal vmin, Normal vmax) {
			f = (f-fmin)/(fmax-fmin); 
			return new Normal(
					vmax.x*f+vmin.x*(1-f),
					vmax.y*f+vmin.y*(1-f),
					vmax.z*f+vmin.z*(1-f)
				);
		}
		@Override
		public String toString() {
			return "Normal [x=" + x + ", y=" + y + ", z=" + z + "]";
		}
		
	}
	
	public static class NormalXY {
		public final double x,y;
		public final Color color;
		
		public NormalXY() { this(0,0,null); }
		public NormalXY(NormalXY n) { this(n.x,n.y,n.color); }
		public NormalXY(NormalXY n, Color color) { this(n.x,n.y,color); }
		public NormalXY(double x, double y) { this(x,y,null); }
		public NormalXY(double x, double y, Color color) { this.x=x; this.y=y; this.color=color; }
		
		public static NormalXY blend(double f, double fmin, double fmax, NormalXY vmin, NormalXY vmax) {
			f = (f-fmin)/(fmax-fmin); 
			return new NormalXY(
					vmax.x*f+vmin.x*(1-f),
					vmax.y*f+vmin.y*(1-f)
				);
		}
		
		public NormalXY normalize()   { return mul(1/getLength()); }
		public NormalXY mul(double d) { return new NormalXY(x*d,y*d,color); }
		public double   getLength()   { return Math.sqrt(x*x+y*y); }
		
		public Normal toNormal() { return new Normal( x,0,y, color ); }
	}
	
	public interface ExtraNormalFunctionPolar {
		Normal getNormal(double w, double r);
		
		@SuppressWarnings("unused")
		public static Normal merge(Normal n, Normal en) {
			if (en==null) return  n;
			if ( n==null) return en;
			double wZ = Math.atan2(n.y, n.x);
			Normal  n1 =  n.rotateZ(wZ);
			Normal en1 = en.rotateZ(wZ);
			
			// TODO: merge en & n
			return en;
		}

		public static class Group implements ExtraNormalFunctionPolar {
			
			private ExtraNormalFunctionPolar[] elements;
			public Group(ExtraNormalFunctionPolar... elements) {
				this.elements = elements;
				Assert(this.elements!=null);
				for (ExtraNormalFunctionPolar el:this.elements)
					Assert(el!=null);
			}

			@Override
			public Normal getNormal(double w, double r) {
				for (ExtraNormalFunctionPolar el:this.elements) {
					Normal en = el.getNormal(w,r);
					if (en!=null) return en;
				}
				return null;
			}
		}
		
		public static class Rotated implements ExtraNormalFunctionPolar {
			
			private double anglePos;
			private ExtraNormalFunctionPolar extra;

			public Rotated(double anglePosDegree, ExtraNormalFunctionPolar extraNormalizedAtXaxis) {
				this.anglePos = -anglePosDegree/180.0*Math.PI;
				this.extra = extraNormalizedAtXaxis;
				Assert(Double.isFinite(this.anglePos));
				Assert(this.extra!=null);
			}

			@Override
			public Normal getNormal(double w, double r) {
				Normal en = extra.getNormal(w-anglePos,r);
				if (en==null) return null;
				return en.rotateZ(anglePos);
			}
		}
		
		public static class LineOnX implements ExtraNormalFunctionPolar {

			private double minR;
			private double maxR;
			private ProfileXY profile;

			public LineOnX(double minR, double maxR, ProfileXY profile) {
				this.minR = minR;
				this.maxR = maxR;
				this.profile = profile;
				Assert(Double.isFinite(this.minR));
				Assert(Double.isFinite(this.maxR));
				Assert(this.profile!=null);
			}
			
			@Override
			public Normal getNormal(double w, double r) {
				double x = r*Math.cos(w);
				double y = r*Math.sin(w);
				double maxProfileR = profile.maxR;
				if (x < minR-maxProfileR) return null;
				if (x > maxR+maxProfileR) return null;
				if (y <     -maxProfileR) return null;
				if (y >      maxProfileR) return null;
				
				double local_r;
				double local_w;
				if (x < minR) {
					local_r = Math.sqrt( y*y + (x-minR)*(x-minR) );
					local_w = Math.atan2(y, x-minR);
					
				} else if (x > maxR) {
					local_r = Math.sqrt( y*y + (x-maxR)*(x-maxR) );
					local_w = Math.atan2(y, x-maxR);
					
				} else if (y>0) {
					local_r = y;
					local_w = Math.PI/2;
					
				} else {
					local_r = -y;
					local_w = -Math.PI/2;
				}
				
				NormalXY n0 = profile.getNormal(local_r);
				if (n0==null) return null; 
				return n0.toNormal().normalize().rotateZ(local_w);
			}
		}
	}
	
	public static abstract class AbstractNormalFunctionPolar<MyClass extends AbstractNormalFunctionPolar<MyClass>> implements NormalFunction.Polar {
		
		private Colorizer.Polar colorizer;
		private ExtraNormalFunctionPolar extras;

		public AbstractNormalFunctionPolar() {
			this.colorizer = null;
			extras = null;
		}
		protected abstract MyClass getThis(); // return this;

		public MyClass setColorizer(Colorizer.Polar colorizer) {
			this.colorizer = colorizer;
			Assert(this.colorizer!=null);
			return getThis();
		}
		
		public MyClass setExtras(ExtraNormalFunctionPolar extras) {
			this.extras = extras;
			Assert(this.extras!=null);
			return getThis();
		}

		@Override
		public Normal getNormal(double w, double r) {
			Normal n = getBaseNormal(w, r);
			if (extras!=null)
				n = ExtraNormalFunctionPolar.merge( n, extras.getNormal(w,r) ); 
			if (n!=null && colorizer!=null) {
				Color color = colorizer.getColor(w,r);
				if (color!=null)
					return new Normal(n,color);
			}
			return n;
		}
		
		protected abstract Normal getBaseNormal(double w, double r);
	}
	
	public static class RotatedProfile extends AbstractNormalFunctionPolar<RotatedProfile> {
		
		private ProfileXY profile;

		public RotatedProfile(ProfileXY profile) {
			this.profile = profile;
			Assert(this.profile!=null);
		}
		@Override protected RotatedProfile getThis() { return this; }

		@Override
		protected Normal getBaseNormal(double w, double r) {
			NormalXY n0 = profile.getNormal(r);
			if (n0==null) return null; 
			return n0.toNormal().normalize().rotateZ(w);
		}
	}
	
	public static abstract class ProfileXY {
		
		public final double minR; // inclusive
		public final double maxR; // exclusive

		protected ProfileXY(double minR, double maxR) {
			this.minR = minR;
			this.maxR = maxR;
			Assert(!Double.isNaN(minR));
			Assert(!Double.isNaN(maxR));
			Assert(minR<=maxR);
		}
		
		public boolean contains(double r) {
			return minR<=r && r<maxR;
		}

		protected abstract NormalXY getNormal(double r);
		
		
		public static class Constant extends ProfileXY {

			public static NormalXY computeNormal(double minR, double maxR, double heightAtMinR, double heightAtMaxR) {
				Assert(Double.isFinite(minR));
				Assert(Double.isFinite(maxR));
				Assert(minR<=maxR);
				Assert(Double.isFinite(heightAtMinR));
				Assert(Double.isFinite(heightAtMaxR));
				return new NormalXY(heightAtMinR-heightAtMaxR,maxR-minR).normalize();
			}

			private final NormalXY constN;

			public Constant(double minR, double maxR) { this(minR, maxR, new NormalXY(0,1)); }
			public Constant(double minR, double maxR, double heightAtMinR, double heightAtMaxR) { this(minR, maxR, computeNormal(minR, maxR, heightAtMinR, heightAtMaxR)); }
			public Constant(double minR, double maxR, NormalXY constN) {
				super(minR, maxR);
				this.constN = constN;
			}

			@Override
			protected NormalXY getNormal(double r) {
				return constN;
			}
		}
		
		public static class RoundBlend extends ProfileXY {

			private NormalXY normalAtMinR;
			private NormalXY normalAtMaxR;
			private boolean linearBlend;
			private double f1;
			private double f2;
			private int f0;

			public RoundBlend(double minR, double maxR, NormalXY normalAtMinR, NormalXY normalAtMaxR) {
				super(minR, maxR);
				this.normalAtMinR = normalAtMinR;
				this.normalAtMaxR = normalAtMaxR;
				Assert(this.normalAtMinR!=null);
				Assert(this.normalAtMaxR!=null);
				Assert(0<=this.normalAtMinR.y);
				Assert(0<=this.normalAtMaxR.y);
				prepareValues();
			}

			private void prepareValues() {
				double a1 = Math.atan2(this.normalAtMinR.y, this.normalAtMinR.x);
				double a2 = Math.atan2(this.normalAtMaxR.y, this.normalAtMaxR.x);
				Assert(a1<=Math.PI);
				Assert(0<=a1);
				Assert(a2<=Math.PI);
				Assert(0<=a2);
				System.out.printf(Locale.ENGLISH,"RoundBlend.prepareValues() -> a1:%6.2f� a2:%6.2f�%n", a1/Math.PI*180, a2/Math.PI*180); 
				if (a1==a2) { linearBlend=true; System.out.println("linearBlend"); return; } else linearBlend=false;
				if (a1<a2) { a1 += Math.PI; a2 += Math.PI; f0=-1; } else f0=1;
				System.out.printf(Locale.ENGLISH,"RoundBlend.prepareValues() -> cos(a1):%1.5f cos(a2):%1.5f%n", Math.cos(a1), Math.cos(a2)); 
				
				double R = (maxR-minR)/(Math.cos(a2)-Math.cos(a1));
				f1 = R*Math.cos(a1) - minR;
				f2 = R*R;
				// x = r + R*Math.cos(a1) - minR;
				// y = Math.sqrt( R*R - x*x );
			}

			@Override
			protected NormalXY getNormal(double r) {
				if (linearBlend)
					return NormalXY.blend(r,minR,maxR,normalAtMinR,normalAtMaxR);
				// x = r + R*Math.cos(a1) - minR;
				// y = Math.sqrt( R*R - x*x );
				double x = f0*(r + f1);
				double y = Math.sqrt( f2 - x*x );
				return new NormalXY(x,y);
			}
		}
		
		public static class LinearBlend extends ProfileXY {

			private final NormalXY normalAtMinR;
			private final NormalXY normalAtMaxR;

			public LinearBlend(double minR, double maxR, NormalXY normalAtMinR, NormalXY normalAtMaxR) {
				super(minR, maxR);
				this.normalAtMinR = normalAtMinR;
				this.normalAtMaxR = normalAtMaxR;
				Assert(this.normalAtMinR!=null);
				Assert(this.normalAtMaxR!=null);
			}

			@Override
			protected NormalXY getNormal(double r) {
				return NormalXY.blend(r,minR,maxR,normalAtMinR,normalAtMaxR);
			}
			
		}
		
		public static class Group extends ProfileXY {

			private static double getR(ProfileXY[] children, BiFunction<Double,Double,Double> compare) {
				Assert(children!=null);
				Assert(children.length>0);
				Assert(children[0]!=null);
				
				double r = children[0].minR;
				for (ProfileXY child:children) {
					Assert(child!=null);
					r = compare.apply(compare.apply(r, child.minR), child.maxR);
				}
				return r;
			}

			private static double getMaxR(ProfileXY[] children) {
				return getR(children,Math::max);
			}

			private static double getMinR(ProfileXY[] children) {
				return getR(children,Math::min);
			}

			private ProfileXY[] children;

			public Group(ProfileXY... children) { this(getMinR(children), getMaxR(children), children); }
			public Group(double minR, double maxR, ProfileXY... children) {
				super(minR, maxR);
				setGroup(children);
			}
			
			public void setGroup(ProfileXY... children) {
				Assert(children!=null);
				for (ProfileXY child:children)
					Assert(child!=null);
				this.children = children;
			}
			
			public boolean hasGaps() {
				if (children.length==0) return minR<maxR;
				if (minR==maxR) return false;
				
				Arrays.sort(children,Comparator.<ProfileXY,Double>comparing(fcn->fcn.minR).thenComparing(fcn->fcn.maxR));
				
				int first = -1;
				for (int i=0; i<children.length; i++)
					if (children[i].contains(minR)) {
						first = i;
						break;
					}
				if (first == -1) return true;
				
				// [r0,r1) [r1,r2) ...
				// [0.5,1.0) [0.8,1.3) [1.2,1.5) ...
				// -->  child[n].contains( child[n-1].maxR )
				//  &&  child[first].contains( minR )
				//  &&  maxR <= child[last].maxR
				double r = minR;
				for (int i=first; i<children.length; i++) {
					if (!children[i].contains(r)) return true;
					r = children[i].maxR;
				}
				
				return r<maxR;
			}

			@Override
			protected NormalXY getNormal(double r) {
				for (ProfileXY child:children)
					if (child.contains(r))
						return child.getNormal(r);
				return null;
			}
			
		}
		
	}
}
