package net.schwarzbaer.geometry.spacial;

public class ConstSphere {
	protected final ConstPoint3d center;
	protected final double radius;
	
	public ConstSphere(ConstPoint3d center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public boolean isInside(ConstPoint3d p) {
		return radius*radius > center.getSquaredDistance(p);
	}

}
