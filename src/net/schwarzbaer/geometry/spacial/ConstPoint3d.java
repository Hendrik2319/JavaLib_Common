package net.schwarzbaer.geometry.spacial;

public class ConstPoint3d {
	public final double x,y,z;

	public ConstPoint3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getDistance       (ConstPoint3d p) { return getDistance       (p.x, p.y, p.z); }
	public double getSquaredDistance(ConstPoint3d p) { return getSquaredDistance(p.x, p.y, p.z); }
	public double getDistance       (double x, double y, double z) { return Math.sqrt( getSquaredDistance(x,y,z) ); }
	public double getSquaredDistance(double x, double y, double z) { return (this.x-x)*(this.x-x) + (this.y-y)*(this.y-y) + (this.z-z)*(this.z-z); }

	public ConstPoint3d add(ConstPoint3d p) { return add(p.x,p.y,p.z); }
	public ConstPoint3d sub(ConstPoint3d p) { return sub(p.x,p.y,p.z); }
	public ConstPoint3d add(double x, double y, double z) { return new ConstPoint3d(this.x+x, this.y+y, this.z+z); }
	public ConstPoint3d sub(double x, double y, double z) { return new ConstPoint3d(this.x-x, this.y-y, this.z-z); }
	public ConstPoint3d mul(double f) { return new ConstPoint3d(x*f, y*f, z*f); }

	public ConstPoint3d normalize() {
		if (isOrigin()) return this;
		return mul(1/getDistance(0,0,0));
	}

	public boolean isOrigin() {
		return x==0 && y==0 && z==0;
	}
}
