package net.schwarzbaer.image.bumpmapping;

import java.awt.Color;

import net.schwarzbaer.image.bumpmapping.BumpMapping.Colorizer;
import net.schwarzbaer.image.bumpmapping.BumpMapping.MutableNormal;
import net.schwarzbaer.image.bumpmapping.BumpMapping.Normal;
import net.schwarzbaer.image.bumpmapping.BumpMapping.NormalXY;

public interface NormalFunction {
	public Normal getNormal(double x, double y, double width, double height);
	public void forceNormalCreation(boolean force);
	
	public static class Simple implements NormalFunction {
		public interface Fcn { public Normal getNormal(double x, double y, double width, double height); }
		private Fcn fcn;
		public Simple(Fcn fcn) {
			this.fcn = fcn;
			Debug.Assert(this.fcn!=null);
		}
		@Override public Normal getNormal(double x, double y, double width, double height) {
			Normal normal = fcn.getNormal(x, y, width, height);
			Debug.Assert(normal!=null);
			return normal;
		}
		@Override public void forceNormalCreation(boolean force) {}
		
	}
	
	public static class NormalMap implements NormalFunction {
		
		private Normal[][] normalMap;
		private boolean forceNormalCreation;
		private boolean centered;
		
		public NormalMap(Normal[][] normalMap, boolean centered) {
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
		
		public static NormalMap createFromHeightMap(float[][] heightMap, double cornerScale) {
			return createFromHeightMap(heightMap,null,cornerScale);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, Color[][] colorMap, double cornerScale) {
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
			return new NormalMap(normalMap, false);
		}
		private static void addNormal(MutableNormal base, Normal n, double scale) {
			if (n != null) {
				base.x += n.x*scale;
				base.y += n.y*scale;
				base.z += n.z*scale;
			}
		}
		private static Normal computeNormal(float[][] heightMap, int x, int y, int dx, int dy) {
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
				Debug.Assert(this.fcn!=null);
			}
			@Override public Normal getNormal(double w, double r) {
				Normal normal = fcn.getNormal(w,r);
				Debug.Assert(normal!=null);
				return normal;
			}
			@Override public void forceNormalCreation(boolean force) {}
			
		}
		public static abstract class AbstractPolar<MyClass extends AbstractPolar<MyClass>> implements Polar {
			
			private Colorizer.Polar colorizer;
			private ExtraNormalFunction.Polar extras;
			private boolean forceNormalCreation;
			private boolean showExtrasOnly;
		
			public AbstractPolar() {
				this.colorizer = null;
				extras = null;
				forceNormalCreation = false;
			}
			protected abstract MyClass getThis(); // return this;
		
			public MyClass setColorizer(Colorizer.Polar colorizer) {
				this.colorizer = colorizer;
				Debug.Assert(this.colorizer!=null);
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
				Debug.Assert(this.extras!=null);
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
		public static class RotatedProfile extends AbstractPolar<RotatedProfile> {
			
			private ProfileXY profile;
		
			public RotatedProfile(ProfileXY profile) {
				this.profile = profile;
				Debug.Assert(this.profile!=null);
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