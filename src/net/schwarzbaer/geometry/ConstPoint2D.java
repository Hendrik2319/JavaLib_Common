package net.schwarzbaer.geometry;

public class ConstPoint2D {
	public final double x,y;

	public ConstPoint2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getDistance(ConstPoint2D p) {
		return Math.sqrt( (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y) );
	}
	
	public static double getSinOfAngle(ConstPoint2D center, ConstPoint2D p1, ConstPoint2D p2) {
		// |aXb| = |a| * |b| * sin(angle(a,b))
		// (z of |aXb|) = ax*by - ay*bx
		double dx1 = p1.x-center.x; // a = p1-center
		double dy1 = p1.y-center.y;
		double dx2 = p2.x-center.x; // b = p2-center
		double dy2 = p2.y-center.y;
		double l1 = Math.sqrt(dx1*dx1+dy1*dy1);
		double l2 = Math.sqrt(dx2*dx2+dy2*dy2);
		return (dx1*dy2 - dy1*dx2) / l1 / l2;
	}
}
