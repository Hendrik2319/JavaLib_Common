package net.schwarzbaer.image.alphachar;

import java.util.Vector;

public interface Form {
	
	static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}
	
	double[] getValues();
	Form setValues(double[] values);
	
	public static class PolyLine implements Form {
		
		private final Vector<Point> points;
		public PolyLine(double xStart, double yStart) {
			points = new Vector<>();
			points.add(new Point(xStart,yStart));
		}
		
		public PolyLine add(double x, double y) {
			points.add(new Point(x,y));
			return this;
		}
		
		public Line[] toLineArray() {
			if (points.size()<=1) return new Line[0];
			Line[] lines = new Line[points.size()-1];
			for (int i=1; i<points.size(); ++i) {
				Point p1 = points.get(i-1);
				Point p2 = points.get(i);
				lines[i-1] = new Line(p1.x, p1.y, p2.x, p2.y);
			}
			return lines;
		}
		
		@Override public double[] getValues() { throw new UnsupportedOperationException(); }
		@Override public PolyLine setValues(double[] values) { throw new UnsupportedOperationException(); }

		private static class Point {
			double x,y;
			private Point(double x, double y) { this.x = x; this.y = y; }
		}
	}
	
	public static class Line implements Form {
		private double x1, y1, x2, y2;
		public Line() { this(0,0,0,0); }
		public Line(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		@Override public double[] getValues() {
			return new double[] { x1, y1, x2, y2 };
		}
		@Override public Line setValues(double[] values) {
			Assert(values.length==4);
			this.x1 = values[0];
			this.y1 = values[1];
			this.x2 = values[2];
			this.y2 = values[3];
			return this;
		}
	}
	
	public static class Arc implements Form {
		private double xC,yC,r,aStart,aEnd;
		public Arc() { this(0,0,0,0,0); }
		public Arc(double xC, double yC, double r, double aStart, double aEnd) {
			this.xC     = xC;
			this.yC     = yC;
			this.r      = r;
			this.aStart = aStart;
			this.aEnd   = aEnd;
		}
		@Override public double[] getValues() {
			return new double[] { xC,yC,r,aStart,aEnd };
		}
		@Override public Arc setValues(double[] values) {
			Assert(values.length==5);
			this.xC     = values[0];
			this.yC     = values[1];
			this.r      = values[2];
			this.aStart = values[3];
			this.aEnd   = values[4];
			return this;
		}
	}
}
