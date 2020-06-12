package net.schwarzbaer.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;

public class BumpMapping {

	private static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}
	
	private Shading shading;
	private NormalFunction normalFunction;
	private ImageCache<BufferedImage> imageCache;

	public BumpMapping(Normal sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunctionCart getNormal) {
		this(false);
		setShading(new Shading.GUISurfaceShading(sun, highlightColor, faceColor, lowlightColor));
		setNormalFunction(getNormal);
	}
	public BumpMapping(Normal sun, Color highlightColor, Color faceColor, Color lowlightColor, NormalFunctionPolar getNormal) {
		this(false);
		setShading(new Shading.GUISurfaceShading(sun, highlightColor, faceColor, lowlightColor));
		setNormalFunction(getNormal);
	}
	public BumpMapping(boolean cachedImage) {
		imageCache = !cachedImage?null:new ImageCache<>(this::renderImageUncached);
	}

	public void setNormalMap(Normal[][] normalMap) {
		setNormalFunction((x,y,width,height)->{
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
	public void setNormalFunction(NormalFunction normalFunction) {
		this.normalFunction = normalFunction;
		if (imageCache!=null) imageCache.resetImage();
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
		return renderImageUncached(width, height);
	}
	public BufferedImage renderImageUncached(int width, int height) {
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
		protected Normal sun;
		
		private Shading(Normal sun) {
			this.sun = sun.normalize();
			this.color = new int[4];
		}
		
		public void setSun(double x, double y, double z) {
			sun = new Normal(x,y,z).normalize();
		}

		public abstract int[] getColor(Normal normal);
		
		public static class NormalImage extends Shading {
			
			public NormalImage() {
				super(new Normal(0,0,1));
			}

			@Override
			public int[] getColor(Normal normal) {
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
			private Normal maxRefl;

			public MaterialShading(Normal sun, Color materialColor, double minIntensity, double phongExp) {
				super(sun);
				this.materialColor = materialColor;
				this.minIntensity = Math.max(0,Math.min(minIntensity,1));
				this.phongExp = Math.max(0,phongExp);
				maxRefl = new Normal(0,0,1).add(sun).normalize();
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
				maxRefl = new Normal(0,0,1).add(sun).normalize();
				//System.out.println("maxRefl = "+maxRefl);
			}
			
			@Override
			public int[] getColor(Normal normal) {
				color[3] = 255;
				double intensityDiff = Math.max( minIntensity, getF(sun,normal) );
				double intensityRefl = getF(maxRefl,normal);
				intensityRefl = Math.max(0,Math.min(intensityRefl,1));
				intensityRefl = Math.pow(intensityRefl,phongExp);
				
				Color c = normal.color==null?materialColor:normal.color;
				color[0] = (int) Math.round(c.getRed  ()*intensityDiff);
				color[1] = (int) Math.round(c.getGreen()*intensityDiff);
				color[2] = (int) Math.round(c.getBlue ()*intensityDiff);
				
				color[0] = (int) Math.round(255-(255-color[0])*(1-intensityRefl));
				color[1] = (int) Math.round(255-(255-color[1])*(1-intensityRefl));
				color[2] = (int) Math.round(255-(255-color[2])*(1-intensityRefl));
				
				return color;
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
			public int[] getColor(Normal normal) {
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
	
	public interface Colorizer {
		public Color getColor(int x, int y, int width, int height);
	}
	public interface ColorizerCart {
		public Color getColor(int x, int y);
	}
	public interface ColorizerPolar {
		public Color getColor(double w, double r);
	}

	public static interface NormalFunctionCart {
		public Normal getNormal(int x, int y);
	}
	public static interface NormalFunctionPolar {
		public Normal getNormal(double w, double r);
	}
	public static interface NormalFunction {
		public Normal getNormal(int x, int y, int width, int height);
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
			return "Vector3D [x=" + x + ", y=" + y + ", z=" + z + "]";
		}
		
	}
	
	public static abstract class ConstructivePolarNormalFunction implements NormalFunctionPolar {
		
		public final double minR; // inclusive
		public final double maxR; // exclusive
		private ColorizerPolar colorizer;

		ConstructivePolarNormalFunction(double minR, double maxR) {
			this.minR = minR;
			this.maxR = maxR;
			Assert(!Double.isNaN(minR));
			Assert(!Double.isNaN(maxR));
			Assert(minR<=maxR);
			this.colorizer = null;
		}
		
		public ConstructivePolarNormalFunction setColorizer(ColorizerPolar colorizer) {
			this.colorizer = colorizer;
			return this;
		}
		
		public boolean contains(double r) {
			return minR<=r && r<maxR;
		}

		@Override
		public Normal getNormal(double w, double r) {
			Normal n = getBaseNormal(r);
			if (n!=null) n = n.normalize().rotateZ(w);
			if (colorizer != null) {
				Color color = colorizer.getColor(w,r);
				if (color!=null)
					return new Normal(n,color);
			}
			return n;
		}
		
		protected abstract Normal getBaseNormal(double r);
		
		
		public static class Constant extends ConstructivePolarNormalFunction {

			public static Normal computeNormal(double minR, double maxR, double heightAtMinR, double heightAtMaxR) {
				Assert(Double.isFinite(minR));
				Assert(Double.isFinite(maxR));
				Assert(minR<=maxR);
				Assert(Double.isFinite(heightAtMinR));
				Assert(Double.isFinite(heightAtMaxR));
				return new Normal(heightAtMinR-heightAtMaxR,0,maxR-minR).normalize();
			}

			private final Normal constN;

			Constant(double minR, double maxR) {
				this(minR, maxR, new Normal(0,0,1));
			}
			Constant(double minR, double maxR, double heightAtMinR, double heightAtMaxR) {
				this(minR, maxR, computeNormal(minR, maxR, heightAtMinR, heightAtMaxR));
			}
			Constant(double minR, double maxR, Normal constN) {
				super(minR, maxR);
				this.constN = constN;
			}

			@Override
			protected Normal getBaseNormal(double r) {
				return constN;
			}
		}
		
		public static class Linear extends ConstructivePolarNormalFunction {

			private final Normal normalAtMinR;
			private final Normal normalAtMaxR;

			Linear(double minR, double maxR, Normal normalAtMinR, Normal normalAtMaxR) {
				super(minR, maxR);
				this.normalAtMinR = normalAtMinR;
				this.normalAtMaxR = normalAtMaxR;
				Assert(this.normalAtMinR!=null);
				Assert(this.normalAtMaxR!=null);
			}

			@Override
			protected Normal getBaseNormal(double r) {
				return Normal.blend(r,minR,maxR,normalAtMinR,normalAtMaxR);
			}
			
		}
		
		public static class Group extends ConstructivePolarNormalFunction {

			private static double getR(ConstructivePolarNormalFunction[] children, BiFunction<Double,Double,Double> compare) {
				Assert(children!=null);
				Assert(children.length>0);
				Assert(children[0]!=null);
				
				double r = children[0].minR;
				for (ConstructivePolarNormalFunction child:children) {
					Assert(child!=null);
					r = compare.apply(compare.apply(r, child.minR), child.maxR);
				}
				return r;
			}

			private static double getMaxR(ConstructivePolarNormalFunction[] children) {
				return getR(children,Math::max);
			}

			private static double getMinR(ConstructivePolarNormalFunction[] children) {
				return getR(children,Math::min);
			}

			private ConstructivePolarNormalFunction[] children;

			Group(double minR, double maxR) {
				this(minR, maxR, null);
			}
			Group(ConstructivePolarNormalFunction[] children) {
				this(getMinR(children), getMaxR(children), children);
			}
			Group(double minR, double maxR, ConstructivePolarNormalFunction[] children) {
				super(minR, maxR);
				setGroup(children);
			}
			
			public void setGroup(ConstructivePolarNormalFunction[] children) {
				this.children = children;
				if (this.children!=null) {
					Assert(this.children.length>0);
					for (ConstructivePolarNormalFunction child:children)
						Assert(child!=null);
				}
			}
			
			public boolean hasGaps() {
				if (children==null) return minR<maxR;
				if (minR==maxR) return false;
				
				Arrays.sort(children,Comparator.<ConstructivePolarNormalFunction,Double>comparing(fcn->fcn.minR).thenComparing(fcn->fcn.maxR));
				
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
			protected Normal getBaseNormal(double r) {
				if (children==null) return null;
				for (ConstructivePolarNormalFunction child:children)
					if (child.contains(r))
						return child.getBaseNormal(r);
				return null;
			}
			
		}
		
	}
}
