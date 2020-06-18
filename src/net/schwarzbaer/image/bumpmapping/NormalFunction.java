package net.schwarzbaer.image.bumpmapping;

import java.awt.Color;

import net.schwarzbaer.image.bumpmapping.BumpMapping.Colorizer;
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
		public static class RotatedProfile extends AbstractNormalFunctionPolar<RotatedProfile> {
			
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