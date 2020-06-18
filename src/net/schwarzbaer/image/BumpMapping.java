package net.schwarzbaer.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;
import java.util.function.BiFunction;

public class BumpMapping { // bumpmapping

	private static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}
	
	private Shading shading;
	private NormalFunction normalFunction;
	private ImageCache<BufferedImage> imageCache;
	private OverSampling overSampling = OverSampling.None;
	private NormalCache normalCache = null;
	private boolean cacheNormals;
	
	public BumpMapping(boolean cacheImage, boolean cacheNormals) {
		this.cacheNormals = cacheNormals;
		imageCache = !cacheImage?null:new ImageCache<>(this::renderImage_uncached);
	}

	public void setNormalMap(Normal[][] normalMap) {
		setNormalFunction( new NormalMapFunction(normalMap,false) );
//		setNormalFunction((x_,y_,width,height)->{
//			int x = (int) Math.round(x_);
//			int y = (int) Math.round(y_);
//			if (x<0 || x>=normalMap   .length) return new Normal(0,0,1);
//			if (y<0 || y>=normalMap[x].length) return new Normal(0,0,1);
//			return normalMap[x][y];
//		});
	}
	
	public static class NormalMapFunction implements NormalFunction {
		
		private Normal[][] normalMap;
		private boolean forceNormalCreation;
		private boolean centered;
		
		public NormalMapFunction(Normal[][] normalMap, boolean centered) {
			this.normalMap = normalMap;
			this.centered = centered;
			forceNormalCreation = true;
		}
		@Override
		public Normal getNormal(double x, double y, double width, double height) {
			int mapWidth  = normalMap    .length;
			int xi = (int) Math.round(x + (centered ? (mapWidth -width )/2 : 0));
			if (xi<0 || xi>=mapWidth ) return forceNormalCreation ? new Normal(0,0,1) : null;
			
			int mapHeight = normalMap[xi].length;
			int yi = (int) Math.round(y + (centered ? (mapHeight-height)/2 : 0));
			if (yi<0 || yi>=mapHeight) return forceNormalCreation ? new Normal(0,0,1) : null;
			
			return normalMap[xi][yi];
		}
		@Override public void forceNormalCreation(boolean forceNormalCreation) {
			this.forceNormalCreation = forceNormalCreation;
		}
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
	
	public BumpMapping setNormalFunction(NormalFunction normalFunction) {
		this.normalFunction = normalFunction;
		resetImageCache();
		resetNormalCache();
		return this;
	}
	public NormalFunction getNormalFunction() {
		return normalFunction;
	}
	public void setSun(double x, double y, double z) {
		shading.setSun(x,y,z);
		resetImageCache();
	}
//	public void getSun(Normal sunOut) {
//		sunOut.x = shading.sun.x;
//		sunOut.y = shading.sun.y;
//		sunOut.z = shading.sun.z;
//	}
	public void setShading(Shading shading) {
		this.shading = shading;
		resetImageCache();
	}
	public Shading getShading() { return shading; }
	
	public void setOverSampling(OverSampling overSampling) {
		this.overSampling = overSampling;
		resetImageCache();
		resetNormalCache();
	}
	public OverSampling getOverSampling() {
		return overSampling;
	}
	public void reset() {
		resetImageCache();
		resetNormalCache();
	}
	
	private void resetImageCache() {
		if (imageCache!=null) imageCache.resetImage();
	}
	
	private void resetNormalCache() {
		normalCache = null;
	}

	
	public BufferedImage renderImage(int width, int height) {
		if (imageCache!=null) return imageCache.getImage(width, height);
		return renderImage_uncached(width, height);
	}
	
	public BufferedImage renderImage_uncached(int width, int height) { return renderImage_uncached(width, height, null); }
	public BufferedImage renderImage_uncached(int width, int height, RenderProgressListener listener) {
		
		if (normalCache==null) {
			if (cacheNormals) normalCache = new NormalCache(width, height, overSampling, (x,y)->normalFunction.getNormal(x,y,width,height));
			else              normalCache = new NormalCache.Dummy(                       (x,y)->normalFunction.getNormal(x,y,width,height));
		}
		
		PixelRenderer pixelRenderer = new PixelRenderer(overSampling,1,normalCache,
			b       -> normalFunction.forceNormalCreation(b),
			(x,y,n) -> shading.getColor(x,y,width,height,n)
		);
		
		BufferedImage image = renderImage(1, width, height, pixelRenderer, listener);
		normalCache.setFixed();
		
		return image;
	}
	public BufferedImage renderImage_uncached(int width, int height, float scale) { return renderImage_uncached(width, height, scale, null); }
	public BufferedImage renderImage_uncached(int width, int height, float scale, RenderProgressListener listener) {
		PixelRenderer pixelRenderer = new PixelRenderer(overSampling, 1/scale,
			(d1,d2,d3,x,y  ) -> normalFunction.getNormal(x,y,width,height),
			b                -> normalFunction.forceNormalCreation(b),
			(         x,y,n) -> shading       .getColor (x,y,width,height,n)
		);
		
		return renderImage(scale, Math.round(width *scale), Math.round(height*scale), pixelRenderer, listener);
	}

	private BufferedImage renderImage(float scale, int scaledWidth, int scaledHeight, PixelRenderer pixelRenderer, RenderProgressListener listener) {
		if (listener!=null) listener.setSize(scaledWidth, scaledHeight);
		BufferedImage image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		for (int pixX=0; pixX<scaledWidth; pixX++)
			for (int pixY=0; pixY<scaledHeight; pixY++) {
				int[] color = pixelRenderer.computeColor(pixX,pixY,pixX/scale,pixY/scale);
				if (color!=null) raster.setPixel(pixX, pixY, color);
				if (listener!=null) listener.wasRendered(pixX, pixY);
			}
		
		return image;
	}
	
	public interface RenderProgressListener {
		void setSize(int width, int height);
		void wasRendered(int x, int y);
	}
	
	private static class NormalCache implements PixelRenderer.NormalSource {
		
		public static class Dummy extends NormalCache {
			Dummy(NormalSource normalSource) { super(0,0,null, normalSource); }
			@Override public Normal getNormal(int pixX, int pixY, int spIndex, double x, double y) { return normalSource.getNormal(x, y); }
		}

		protected final NormalSource normalSource;
		private boolean isFixed;
		private Normal[][][] cache;

		NormalCache(int width, int height, OverSampling overSampling, NormalSource normalSource) {
			this.normalSource = normalSource;
			this.isFixed = false;
			int n = overSampling==null || overSampling==OverSampling.None ? 1 : overSampling.samplingPoints.length;
			cache = new Normal[n][width][height];
			for (Normal[][] arr1:cache)
				for (Normal[] arr2:arr1)
					Arrays.fill(arr2, null);
		}

		public void setFixed() { isFixed = true; }

		@Override
		public Normal getNormal(int pixX, int pixY, int spIndex, double x, double y) {
			Normal n;
			if (!isFixed) cache[spIndex][pixX][pixY] = n = normalSource.getNormal(x, y);
			else          n = cache[spIndex][pixX][pixY];
			return n;
		}
		
		public interface NormalSource { Normal getNormal(double x,double y); }
	}
	
	private static class PixelRenderer {
		
		public interface NormalSource       { Normal getNormal(int pixX, int pixY, int spIndex, double x,double y); }
		public interface NormalSourceSwitch { void forceNormalCreation(boolean b); }
		public interface ColorSource        { int[]  getColor(double x,double y, Normal n); }
		public interface Source extends ColorSource, NormalSource, NormalSourceSwitch {}

		private final NormalSource normalSource;
		private final NormalSourceSwitch normalSourceSwitch;
		private final ColorSource colorSource;
		private final OverSampling overSampling;
		private final double pixWidth;

		@SuppressWarnings("unused")
		PixelRenderer(OverSampling overSampling, double pixWidth, Source source) { this(overSampling, pixWidth, source,source,source); }
		PixelRenderer(OverSampling overSampling, double pixWidth, NormalSource normalSource, NormalSourceSwitch normalSourceSwitch, ColorSource colorSource) {
			this.overSampling = overSampling;
			this.pixWidth = pixWidth;
			this.normalSource = normalSource;
			this.normalSourceSwitch = normalSourceSwitch;
			this.colorSource = colorSource;
		}
	
		public int[] computeColor(int pixX, int pixY, double x, double y) {
			
			if (overSampling==null || overSampling==OverSampling.None || overSampling.samplingPoints.length==0)
				return colorSource.getColor(x,y,normalSource.getNormal(pixX, pixY, 0, x,y));
			
			boolean miss = false;
			boolean hit  = false;
			Normal[] normals = new Normal[overSampling.samplingPoints.length];
			for (int i=0; i<normals.length; i++) {
				OverSampling.SamplingPoint sp = overSampling.samplingPoints[i];
				normals[i] = getNormal(pixX, pixY, i, x, y, sp);
				if (normals[i]==null) {
					if (!miss && hit) normalSourceSwitch.forceNormalCreation(true);
					miss = true;
				} else {
					if (!hit && miss) normalSourceSwitch.forceNormalCreation(true);
					hit = true;
				}
			}
			if (hit && miss) {
				// normalSourceSwitch.forceNormalCreation is already set
				for (int i=0; i<normals.length; i++) {
					OverSampling.SamplingPoint sp = overSampling.samplingPoints[i];
					normals[i] = getNormal(pixX, pixY, i, x,y, sp );
					Assert(normals[i]!=null);
				}
				normalSourceSwitch.forceNormalCreation(false);
			}
			
			if (miss && !hit)
				return null;
			
			int[] sumColor = null;
			for (int i=0; i<overSampling.samplingPoints.length; i++) {
				OverSampling.SamplingPoint sp = overSampling.samplingPoints[i];
				int[] color = getColor( x,y, sp, normals[i] );
				sumColor = add(sumColor,color);
			}
			return div(sumColor,overSampling.samplingPoints.length);
		}
		
		private int[] getColor(double x, double y, OverSampling.SamplingPoint sp, Normal n) {
			return colorSource.getColor(
				x+sp.x*pixWidth,
				y+sp.y*pixWidth,
				n
			);
		}
		
		private Normal getNormal(int pixX, int pixY, int spIndex, double x, double y, OverSampling.SamplingPoint sp) {
			return normalSource.getNormal(
				pixX, pixY, spIndex, 
				x+sp.x*pixWidth,
				y+sp.y*pixWidth
			);
		}
		
		private int[] div(int[] color, int divisor) {
			Assert(color!=null);
			for (int i=0; i<color.length; i++)
				color[i] /= divisor;
			return color;
		}
		
		private int[] add(int[] sumColor, int[] color) {
			Assert(color!=null);
			Assert(color.length>0);
			if (sumColor==null) {
				sumColor = new int[color.length];
				Arrays.fill(sumColor,0);
			}
			Assert(sumColor.length == color.length);
			for (int i=0; i<sumColor.length; i++)
				sumColor[i] += color[i];
			return sumColor;
		}
	}

	public enum OverSampling {
		None         ("None (1x)"),
		_2x_Diagonal1("2x (diagonal1)"  , new SamplingPoint(-0.25,-0.25), new SamplingPoint(0.25,0.25)),
		_2x_Diagonal2("2x (diagonal2)"  , new SamplingPoint(-0.25,0.25), new SamplingPoint(0.25,-0.25)),
		_4x_Square   ("4x (square)"     , new SamplingPoint(-0.25,-0.25), new SamplingPoint(0.25,0.25), new SamplingPoint(-0.25,0.25), new SamplingPoint(0.25,-0.25)),
		_5x_Cross    ("5x (cross,\"x\")", new SamplingPoint(0,0), new SamplingPoint(-0.3,-0.3), new SamplingPoint(0.3,0.3), new SamplingPoint(-0.3,0.3), new SamplingPoint(0.3,-0.3)),
		_9x_Square   ("9x (square)"     , new SamplingPoint(0,0), new SamplingPoint(0,0.33), new SamplingPoint(0,-0.33), new SamplingPoint(0.33,0), new SamplingPoint(-0.33,0), new SamplingPoint(0.33,0.33), new SamplingPoint(0.33,-0.33), new SamplingPoint(-0.33,0.33), new SamplingPoint(-0.33,-0.33)),
		;
		
		private final String label;
		private final SamplingPoint[] samplingPoints;
		OverSampling(String label, SamplingPoint... samplingPoints) {
			this.label = label;
			this.samplingPoints = samplingPoints;
		}
		@Override
		public String toString() {
			return label;
		}
	
		private static class SamplingPoint {
			final double x,y;
			private SamplingPoint(double x, double y) { this.x = x; this.y = y;
			}
		}
	}

	public static abstract class Shading {
		protected int[] color;
		protected Normal sun;
		
		private Shading(Normal sun) {
			this.sun = sun.normalize();
			this.color = new int[4];
		}
		public Shading(Shading shading) {
			this(shading.sun);
		}

		public void setSun(double x, double y, double z) {
			sun = new Normal(x,y,z).normalize();
		}

		public abstract int[] getColor(double x, double y, double width, double height, Normal normal);
		
		public static Shading clone(Shading shading) {
			if (shading==null) return null;
			if (shading instanceof NormalImage      ) return new NormalImage      ((NormalImage      ) shading);
			if (shading instanceof MixedShading     ) return new MixedShading     ((MixedShading     ) shading);
			if (shading instanceof MaterialShading  ) return new MaterialShading  ((MaterialShading  ) shading);
			if (shading instanceof GUISurfaceShading) return new GUISurfaceShading((GUISurfaceShading) shading);
			Assert(false);
			return null;
		}

		public static class MixedShading extends Shading {
			
			private final Shading[] shadings;
			private Indexer indexer;
			
			public MixedShading(Indexer indexer, Shading...shadings) {
				super(new Normal(0,0,1));
				this.indexer = indexer;
				this.shadings = shadings;
				Assert(indexer!=null);
				Assert(shadings!=null);
				Assert(shadings.length>0);
			}
			public MixedShading(MixedShading shading) {
				super(shading);
				Assert(shading.indexer!=null);
				Assert(shading.shadings!=null);
				Assert(shading.shadings.length>0);
				indexer = shading.indexer;
				shadings = new Shading[shading.shadings.length];
				for (int i=0; i<shadings.length; i++)
					shadings[i] = Shading.clone(shading.shadings[i]);
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
			public NormalImage(NormalImage shading) {
				super(shading);
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
			public MaterialShading(MaterialShading shading) {
				this(shading.sun,shading.materialColor,shading.ambientIntensity,shading.phongExp,shading.withReflection,shading.reflectionIntensity);
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
			
			public GUISurfaceShading(GUISurfaceShading shading) {
				this(shading.sun,shading.highlightColor,shading.faceColor,shading.shadowColor);
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
		
		public interface Cart extends Indexer {
			@Override public default int getIndex(double x, double y, double width, double height) {
				return getIndex(x, y);
			}
			public int getIndex(double x, double y);
		}
		public interface Polar extends Indexer {
			@Override public default int getIndex(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width/2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return getIndex(w,r);
			}
			public int getIndex(double w, double r);
		}
	}
	
	public interface Colorizer {
		
		public Color getColor(double x, double y, double width, double height);
		
		public interface Cart extends Colorizer {
			@Override public default Color getColor(double x, double y, double width, double height) {
				return getColor(x, y);
			}
			public Color getColor(double x, double y);
		}
		public interface Polar extends Colorizer {
			@Override public default Color getColor(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width /2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return getColor(w, r);
			}
			public Color getColor(double w, double r);
		}
	}
	
	public interface Filter {
		
		public boolean passesFilter(double x, double y, double width, double height);
		
		public interface Cart extends Filter {
			@Override public default boolean passesFilter(double x, double y, double width, double height) {
				return passesFilter(x, y);
			}
			public boolean passesFilter(double x, double y);
		}
		public interface Polar extends Filter {
			@Override public default boolean passesFilter(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width /2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return passesFilter(w,r);
			}
			public boolean passesFilter(double w, double r);
		}
	}

	public static interface NormalFunction {
		public Normal getNormal(double x, double y, double width, double height);
		public void forceNormalCreation(boolean force);
		
		public static class Simple implements NormalFunction {
			public interface Fcn { public Normal getNormal(double x, double y, double width, double height); }
			private Fcn fcn;
			public Simple(Fcn fcn) {
				this.fcn = fcn;
				Assert(this.fcn!=null);
			}
			@Override public Normal getNormal(double x, double y, double width, double height) {
				Normal normal = fcn.getNormal(x, y, width, height);
				Assert(normal!=null);
				return normal;
			}
			@Override public void forceNormalCreation(boolean force) {}
			
		}
		public static interface Cart extends NormalFunction {
			@Override public default Normal  getNormal     (double x, double y, double width, double height) { return getNormal     (x, y); }
			public Normal getNormal(double x, double y);
		}
		public static interface Polar extends NormalFunction {
			@Override public default Normal getNormal(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width /2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return getNormal(w,r);
			}
			public Normal getNormal(double w, double r);
			
			public static class Simple implements Polar {
				public interface Fcn { public Normal getNormal(double w, double r); }
				private Fcn fcn;
				public Simple (Fcn fcn) {
					this.fcn = fcn;
					Assert(this.fcn!=null);
				}
				@Override public Normal getNormal(double w, double r) {
					Normal normal = fcn.getNormal(w,r);
					Assert(normal!=null);
					return normal;
				}
				@Override public void forceNormalCreation(boolean force) {}
				
			}
			public static abstract class AbstractNormalFunctionPolar<MyClass extends AbstractNormalFunctionPolar<MyClass>> implements Polar {
				
				private Colorizer.Polar colorizer;
				private ExtraNormalFunction.Polar extras;
				private boolean forceNormalCreation;
				private boolean showExtrasOnly;
			
				public AbstractNormalFunctionPolar() {
					this.colorizer = null;
					extras = null;
					forceNormalCreation = false;
				}
				protected abstract MyClass getThis(); // return this;
			
				public MyClass setColorizer(Colorizer.Polar colorizer) {
					this.colorizer = colorizer;
					Assert(this.colorizer!=null);
					return getThis();
				}
				
				@Override
				public void forceNormalCreation(boolean forceNormalCreation) {
					this.forceNormalCreation = forceNormalCreation;
				}
				public void showExtrasOnly(boolean showExtrasOnly) {
					this.showExtrasOnly = showExtrasOnly;
				}
				
				public MyClass setExtras(ExtraNormalFunction.Polar extras) {
					this.extras = extras;
					Assert(this.extras!=null);
					return getThis();
				}
			
				@Override
				public Normal getNormal(double w, double r) {
					boolean showAll = !showExtrasOnly;
					
					Normal n = null;
					Normal en = null;
					
					if (extras!=null)
						en = extras.getNormal(w,r);
					
					if (en!=null || showAll || forceNormalCreation)
						n = getBaseNormal(w, r);
					
					if (en!=null)
						n = ExtraNormalFunction.merge( n, en );
					
					if (forceNormalCreation && n==null)
						n = new Normal(0,0,1);
					
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
					return n0.toNormalInXZ().normalize().rotateZ(w);
				}
			}
		}
	}
	
	public interface ExtraNormalFunction {

		public static Normal merge(Normal n, Normal en) {
			if (en==null) return  n;
			if ( n==null) return en;
			double wZ = Math.atan2(n.y, n.x);
			 n =  n.rotateZ(-wZ);
			en = en.rotateZ(-wZ);
			double wY = Math.atan2(n.x, n.z);
			en = en.rotateY(wY);
			en = en.rotateZ(wZ);
			return en;
		}
		
		public Normal  getNormal     (double x, double y, double width, double height);
		public boolean isInsideBounds(double x, double y, double width, double height);
		
		public interface Cart extends ExtraNormalFunction {
			@Override public default Normal  getNormal     (double x, double y, double width, double height) { return getNormal     (x, y); }
			@Override public default boolean isInsideBounds(double x, double y, double width, double height) { return isInsideBounds(x, y); }
			public Normal  getNormal     (double x, double y);
			public boolean isInsideBounds(double x, double y);
			
			public static abstract class AbstractGroup<ElementType extends Cart> implements Cart {
			
				protected final Vector<ElementType> elements;
				public AbstractGroup(ElementType[] elements) {
					this.elements = new Vector<>();
					add(elements);
				}
			
				public void add(ElementType element) {
					if (element!=null)
						elements.add(element);
					
				}
				public void add(ElementType[] elements) {
					if (elements!=null)
						for (ElementType el:elements)
							add(el);
				}
				
				@Override
				public boolean isInsideBounds(double x, double y) {
					for (ElementType el:elements)
						if (el.isInsideBounds(x, y))
							return true;
					return false;
				}
			}
			
			public static class MergeGroup extends AbstractGroup<ProfileXYbasedLineElement> {
			
				public MergeGroup(ProfileXYbasedLineElement...elements) {
					super(elements);
				}
			
				@Override
				public Normal getNormal(double x, double y) {
					ProfileXYbasedLineElement.Distance d0 = null;
					ProfileXYbasedLineElement el0 = null;
					for (ProfileXYbasedLineElement el:elements) {
						ProfileXYbasedLineElement.Distance d = el.getDistance(x,y);
						if (d!=null && (d0==null || d0.r>d.r)) { d0=d; el0 = el; }
					}
					if (el0==null || d0==null) return null;
					return el0.getNormal(d0);
				}
			}

			public static abstract class ProfileXYbasedLineElement implements Cart {
				
				protected final ProfileXY profile;
			
				public ProfileXYbasedLineElement(ProfileXY profile) {
					this.profile = profile;
				}
				
				public abstract Distance getDistance(double x, double y);
			
				@Override
				public Normal getNormal(double x, double y) {
					return getNormal(getDistance(x, y));
				}
			
				public Normal getNormal(Distance distance) {
					if (distance==null) return null;
					
					NormalXY n0 = profile.getNormal(distance.r);
					if (n0==null) return null;
					
					return n0.toNormalInXZ().normalize().rotateZ(distance.w);
				}
				
				public static class Distance {
					public final double r,w;
					private Distance(double r, double w) {
						this.r = r;
						this.w = w;
						Assert(r>=0);
					}
					public static double computeW(double xC, double yC, double x, double y) {
						double localX = x-xC;
						double localY = y-yC;
						return Math.atan2(localY, localX);
					}
					public static double computeR(double xC, double yC, double x, double y) {
						double localX = x-xC;
						double localY = y-yC;
						return Math.sqrt(localX*localX+localY*localY);
					}
					public static Distance compute(double xC, double yC, double x, double y) {
						double localX = x-xC;
						double localY = y-yC;
						double localR = Math.sqrt(localX*localX+localY*localY);
						double localW = Math.atan2(localY, localX);
						return new Distance(localR,localW);
					}
				}
				
				public static class LineGroup extends ProfileXYbasedLineElement {
			
					protected final Vector<ProfileXYbasedLineElement> elements;
					public LineGroup(ProfileXY profile) {
						super(profile);
						this.elements = new Vector<>();
					}
					
					public void addLine(double x1, double y1, double x2, double y2) {
						elements.add(new Line(x1,y1,x2,y2,profile));
					}
					
					public void addArc(double xC, double yC, double r, double aStart, double aEnd) {
						elements.add(new Arc(xC,yC, r,aStart,aEnd, profile));
					}
					
					@Override
					public Distance getDistance(double x, double y) {
						Distance d0 = null;
						for (ProfileXYbasedLineElement el:elements) {
							Distance d = el.getDistance(x,y);
							if (d!=null && (d0==null || d0.r>d.r)) d0=d;
						}
						return d0;
					}
			
					@Override
					public boolean isInsideBounds(double x, double y) {
						for (ProfileXYbasedLineElement el:elements)
							if (el.isInsideBounds(x, y))
								return true;
						return false;
					}
				}
				
				public static class Line extends ProfileXYbasedLineElement {
			
					private final double x1, y1, x2, y2;
					private final double length;
					private final double angle;
			
					public Line(double x1, double y1, double x2, double y2, ProfileXY profile) {
						super(profile);
						this.x1 = x1;
						this.y1 = y1;
						this.x2 = x2;
						this.y2 = y2;
						length = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
						Assert(length>0);
						angle = Math.atan2(y2-y1, x2-x1);
					}
			
					@Override
					public Distance getDistance(double x, double y) {
						double f = ((x2-x1)*(x-x1)+(y2-y1)*(y-y1))/length/length; // cos(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|² -> (x1,y1) ..f.. (x2,y2)
						if (f>1) {
							// after (x2,y2)
							Distance d = Distance.compute(x2,y2,x,y);
							if (d.r>profile.maxR) return null;
							return d;
						}
						if (f<0) {
							// before (x1,y1)
							Distance d = Distance.compute(x1,y1,x,y);
							if (d.r>profile.maxR) return null;
							return d;
						}
						// between (x1,y1) and (x2,y2)
						double r = ((x2-x1)*(y-y1)-(y2-y1)*(x-x1))/length; // sin(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|  =  sin(a)*|x-x1,y-y1|  =  r
						if (Math.abs(r)>profile.maxR) return null;
						if (r>0) return new Distance( r, angle+Math.PI/2);
						else     return new Distance(-r, angle-Math.PI/2);
					}
			
					@Override
					public boolean isInsideBounds(double x, double y) {
						double r = ((x2-x1)*(y-y1)-(y2-y1)*(x-x1))/length; // sin(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|  =  sin(a)*|x-x1,y-y1|  =  r
						if (Math.abs(r)>profile.maxR) return false;
						
						double s = ((x2-x1)*(x-x1)+(y2-y1)*(y-y1))/length; // cos(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|  =  cos(a)*|x-x1,y-y1|  =  s
						return -profile.maxR<=s && s<=length+profile.maxR;
					}
				}
				
				public static class Arc extends ProfileXYbasedLineElement {
			
					private final double xC,yC,r,aStart,aEnd, xS,yS,xE,yE;
			
					public Arc(double xC, double yC, double r, double aStart, double aEnd, ProfileXY profile) {
						super(profile);
						this.xC = xC;
						this.yC = yC;
						this.r = r;
						this.aStart = aStart;
						this.aEnd = aEnd;
						xS = r*Math.cos(this.aStart);
						yS = r*Math.sin(this.aStart);
						xE = r*Math.cos(this.aEnd);
						yE = r*Math.sin(this.aEnd);
					}
			
					@Override
					public Distance getDistance(double x, double y) {
						Distance dC = Distance.compute(xC,yC,x,y);
						if (Math.abs(dC.r-r)>profile.maxR) return null;
						
						if (isInsideAngleRange(aStart, aEnd, dC.w)) {
							if (dC.r>r) return new Distance(dC.r-r,  dC.w);
							return             new Distance(r-dC.r, -dC.w);
						}
						Distance dS = Distance.compute(xS,yS,x,y);
						Distance dE = Distance.compute(xE,yE,x,y);
						if (dS.r<dE.r) {
							if (dS.r<=profile.maxR) return dS;
						} else {
							if (dE.r<=profile.maxR) return dE;
						}
						return null;
					}
			
					@Override
					public boolean isInsideBounds(double x, double y) {
						Distance dC = Distance.compute(xC,yC,x,y);
						if (Math.abs(dC.r-r)>profile.maxR) return false;
						if (isInsideAngleRange(aStart, aEnd, dC.w)) return true;
						return Distance.computeR(xS,yS,x,y)<=profile.maxR || Distance.computeR(xE,yE,x,y)<=profile.maxR;
					}
				}
			}
		}
		public interface Polar extends ExtraNormalFunction {
			@Override public default Normal getNormal(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width /2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return getNormal(w,r);
			}
			@Override public default boolean isInsideBounds(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width /2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return isInsideBounds(w,r);
			}
			public Normal  getNormal     (double w, double r);
			public boolean isInsideBounds(double w, double r);
			
			public static class Stencil implements Polar {
				
				private final Filter.Polar filter;
				private final Polar extra;
			
				public Stencil(Filter.Polar filter, Polar extra) {
					this.filter = filter;
					this.extra = extra;
					Assert(this.filter!=null);
					Assert(this.extra!=null);
				}
			
				@Override
				public boolean isInsideBounds(double w, double r) {
					return filter.passesFilter(w,r);
				}
			
				@Override
				public Normal getNormal(double w, double r) {
					if (filter.passesFilter(w,r))
						return extra.getNormal(w,r);
					return null;
				}
			}
			public static class Group implements Polar {
				
				private final Vector<Polar> elements;
				
				public Group(Polar... elements) {
					this.elements = new Vector<>();
					add(elements);
				}
				public void add(Polar... elements) {
					if (elements!=null)
						for (Polar el:elements)
							if (el!=null) this.elements.add(el);
				}
			
				@Override
				public boolean isInsideBounds(double w, double r) {
					for (Polar el:elements)
						if (el.isInsideBounds(w, r))
							return true;
					return false;
				}
				
				@Override
				public Normal getNormal(double w, double r) {
					for (Polar el:elements) {
						if (!el.isInsideBounds(w,r)) continue;
						Normal en = el.getNormal(w,r);
						if (en!=null) return en;
					}
					return null;
				}
			}
			public static class Rotated implements Polar {
				
				private final double anglePos;
				private final Polar extra;
			
				public Rotated(double anglePosDegree, Polar extraNormalizedAtXaxis) {
					this.anglePos = anglePosDegree/180.0*Math.PI;
					this.extra = extraNormalizedAtXaxis;
					Assert(Double.isFinite(this.anglePos));
					Assert(this.extra!=null);
				}
			
				@Override
				public boolean isInsideBounds(double w, double r) {
					return extra.isInsideBounds(w-anglePos, r);
				}
			
				@Override
				public Normal getNormal(double w, double r) {
					if (!isInsideBounds(w,r)) return null;
					Normal en = extra.getNormal(w-anglePos,r);
					if (en==null) return null;
					return en.rotateZ(anglePos);
				}
			}
			public static class Bounds {
				
				final double minW,maxW,minR,maxR;
				
				private Bounds() {
					this(0,FULL_CIRCLE,0,Double.POSITIVE_INFINITY);
				}
				private Bounds(double minW, double maxW, double minR, double maxR) {
					this.minW = minW;
					this.maxW = maxW;
					this.minR = minR;
					this.maxR = maxR;
					Assert(Double.isFinite(this.minW));
					Assert(Double.isFinite(this.maxW));
					Assert(this.minW<=this.maxW);
					Assert(Double.isFinite(this.minR));
					Assert(!Double.isNaN(this.maxR));
					Assert(0<=this.minR);
					Assert(this.minR<=this.maxR);
				}
				public boolean isInside(double w, double r) {
					if (r<minR) return false;
					if (r>maxR) return false;
					return isInsideAngleRange(minW, maxW, w);
				}
				public Bounds rotate(double w) {
					return new Bounds(minW+w, maxW+w, minR, maxR);
				}
			}
			public static class LineOnX implements Polar {
			
				private final double minR;
				private final double maxR;
				private final ProfileXY profile;
				private final Bounds bounds;
			
				public LineOnX(double minR, double maxR, ProfileXY profile) {
					this.minR = minR;
					this.maxR = maxR;
					this.profile = profile;
					Assert(this.profile!=null);
					Assert(Double.isFinite(this.minR));
					Assert(Double.isFinite(this.maxR));
					Assert(0<=this.minR);
					Assert(this.minR<=this.maxR);
					double maxProfileR = profile.maxR;
					double w = Math.asin(maxProfileR/this.minR);
					bounds = new Bounds(-w,w,this.minR-maxProfileR,this.maxR+maxProfileR);
				}
				
				@Override
				public boolean isInsideBounds(double w, double r) {
					return bounds.isInside(w, r);
				}
			
				@Override
				public Normal getNormal(double w, double r) {
					//if (!isInsideBounds(w,r)) return null;
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
					return n0.toNormalInXZ().normalize().rotateZ(local_w);
				}
			}
		}
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
		
		public Normal rotateY(double w) {
			return new Normal(
				x*Math.cos(w)+z*Math.sin(w),
				y,
				-x*Math.sin(w)+z*Math.cos(w),
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
		@Override public String toString() {
			return String.format(Locale.ENGLISH, "Normal[%1.5f,%1.5f,%1.5f%s]", x, y, z, color==null?"":String.format(",0x%08X", color.getRGB()) );
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
		
		public Normal toNormalInXZ() { return new Normal( x,0,y, color ); }
		
		@Override public String toString() {
			return String.format(Locale.ENGLISH, "NormalXY[%1.5f,%1.5f,%1.5f%s]", x, y, color==null?"":String.format(",0x%08X", color.getRGB()) );
		}
	}
	
	private static final double FULL_CIRCLE = 2*Math.PI;
	public static boolean isInsideAngleRange(double minW, double maxW, double w) {
		Assert(Double.isFinite(minW));
		Assert(Double.isFinite(maxW));
		Assert(minW<=maxW);
		
		double wDiff = w-minW;
		if (wDiff<0 || FULL_CIRCLE<wDiff) w -= Math.floor(wDiff/FULL_CIRCLE)*FULL_CIRCLE;
		Assert(minW<=w);
		Assert(w<=minW+FULL_CIRCLE);
		
		return w<=maxW;
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
				//System.out.printf(Locale.ENGLISH,"RoundBlend.prepareValues() -> a1:%6.2f° a2:%6.2f°%n", a1/Math.PI*180, a2/Math.PI*180); 
				if (a1==a2) { linearBlend=true; /*System.out.println("linearBlend");*/ return; } else linearBlend=false;
				if (a1<a2) { a1 += Math.PI; a2 += Math.PI; f0=-1; } else f0=1;
				//System.out.printf(Locale.ENGLISH,"RoundBlend.prepareValues() -> cos(a1):%1.5f cos(a2):%1.5f%n", Math.cos(a1), Math.cos(a2)); 
				
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
