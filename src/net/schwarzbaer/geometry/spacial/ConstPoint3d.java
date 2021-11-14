package net.schwarzbaer.geometry.spacial;

public class ConstPoint3d {
	public final double x,y,z;

	public ConstPoint3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getDistance(ConstPoint3d p) {
		return Math.sqrt( getSquaredDistance(p) );
	}

	public double getSquaredDistance(ConstPoint3d p) {
		return (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y) + (z-p.z)*(z-p.z);
	}

}
