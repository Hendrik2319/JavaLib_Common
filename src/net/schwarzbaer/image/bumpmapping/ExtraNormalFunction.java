package net.schwarzbaer.image.bumpmapping;

import java.util.Vector;

import net.schwarzbaer.image.bumpmapping.BumpMapping.Filter;
import net.schwarzbaer.image.bumpmapping.BumpMapping.Normal;
import net.schwarzbaer.image.bumpmapping.BumpMapping.NormalXY;

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
					Debug.Assert(r>=0);
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
					Debug.Assert(length>0);
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
					
					if (BumpMapping.isInsideAngleRange(aStart, aEnd, dC.w)) {
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
					if (BumpMapping.isInsideAngleRange(aStart, aEnd, dC.w)) return true;
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
				Debug.Assert(this.filter!=null);
				Debug.Assert(this.extra!=null);
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
				Debug.Assert(Double.isFinite(this.anglePos));
				Debug.Assert(this.extra!=null);
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
				this(0,BumpMapping.FULL_CIRCLE,0,Double.POSITIVE_INFINITY);
			}
			private Bounds(double minW, double maxW, double minR, double maxR) {
				this.minW = minW;
				this.maxW = maxW;
				this.minR = minR;
				this.maxR = maxR;
				Debug.Assert(Double.isFinite(this.minW));
				Debug.Assert(Double.isFinite(this.maxW));
				Debug.Assert(this.minW<=this.maxW);
				Debug.Assert(Double.isFinite(this.minR));
				Debug.Assert(!Double.isNaN(this.maxR));
				Debug.Assert(0<=this.minR);
				Debug.Assert(this.minR<=this.maxR);
			}
			public boolean isInside(double w, double r) {
				if (r<minR) return false;
				if (r>maxR) return false;
				return BumpMapping.isInsideAngleRange(minW, maxW, w);
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
				Debug.Assert(this.profile!=null);
				Debug.Assert(Double.isFinite(this.minR));
				Debug.Assert(Double.isFinite(this.maxR));
				Debug.Assert(0<=this.minR);
				Debug.Assert(this.minR<=this.maxR);
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