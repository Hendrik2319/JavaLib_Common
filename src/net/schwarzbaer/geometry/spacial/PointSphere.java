package net.schwarzbaer.geometry.spacial;

public class PointSphere extends ConstSphere {
	
	private final double pointDensity_perSqU;
	private ConstPoint3d[] points;

	public PointSphere(ConstPoint3d center, double radius, double pointDensity_perSqU) {
		super(center, radius);
		this.pointDensity_perSqU = pointDensity_perSqU;
		generatePoints();
	}

	public void generatePoints() {
		double A = 4*Math.PI*this.radius*this.radius;
		long n = Math.round( A*this.pointDensity_perSqU );
		points = new ConstPoint3d[(int) n];
		for (int i=0; i<points.length; i++) {
			double angle  = Math.random()*Math.PI*2;
			double height = (Math.random()*2-1)*this.radius;
			double rh = Math.sqrt(this.radius*this.radius-height*height);
			
			double x = Math.cos(angle)*rh;
			double y = Math.sin(angle)*rh;
			double z = height;
			points[i] = new ConstPoint3d(x, y, z);
		}
	}

}
