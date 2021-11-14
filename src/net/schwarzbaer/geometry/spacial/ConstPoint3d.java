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

	public ConstPoint3d sub(ConstPoint3d p) { return new ConstPoint3d(x-p.x, y-p.y, z-p.z); }
	public ConstPoint3d add(ConstPoint3d p) { return new ConstPoint3d(x+p.x, y+p.y, z+p.z); }
	public ConstPoint3d mul(double f      ) { return new ConstPoint3d(x*f, y*f, z*f); }

	public ConstPoint3d normalize() {
		if (isOrigin()) return this;
		return mul(1/getDistance(new ConstPoint3d(0,0,0)));
	}

	public boolean isOrigin() {
		return x==0 && y==0 && z==0;
	}
}
