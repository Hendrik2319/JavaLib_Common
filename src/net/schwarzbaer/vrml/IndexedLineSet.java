package net.schwarzbaer.vrml;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

import net.schwarzbaer.geometry.spacial.ConstPoint3d;

public class IndexedLineSet {
	
	private final HashSet<String> pointSet;
	private final Vector<String> points;
	private final Vector<Integer> lines;
	private final String pointCoordFormat;

	public IndexedLineSet(String pointCoordFormat, boolean optimizePointSet) {
		this.pointCoordFormat = pointCoordFormat;
		points = new Vector<>();
		lines = new Vector<>();
		pointSet = !optimizePointSet ? null : new HashSet<>();
	}
	
	public int addPoint(ConstPoint3d p) {
		return addPoint(p.x, p.y, p.z);
	}
	public int addPoint(double x, double y, double z) {
		String str = String.format(Locale.ENGLISH, pointCoordFormat+" "+pointCoordFormat+" "+pointCoordFormat, x,y,z);
		int index;
		if (pointSet!=null && pointSet.contains(str))
			index = points.indexOf(str);
		else {
			index = points.size();
			points.add(str);
			if (pointSet!=null) pointSet.add(str);
		}
		return index;
	}
	
	public void addAxesCross(ConstPoint3d p, double d) {
		int p11 = addPoint(p.add( d,0,0));
		int p12 = addPoint(p.add(-d,0,0));
		int p21 = addPoint(p.add(0, d,0));
		int p22 = addPoint(p.add(0,-d,0));
		int p31 = addPoint(p.add(0,0, d));
		int p32 = addPoint(p.add(0,0,-d));
		addLine(p11,p12);
		addLine(p21,p22);
		addLine(p31,p32);
	}
	
	public void addLine(int pointIndex1, int pointIndex2) {
		lines.add(pointIndex1);
		lines.add(pointIndex2);
		lines.add(-1);
	}
	
	public void writeToVRML(PrintWriter out, Color lineColor) { writeToVRML(out, lineColor, 0); }
	public void writeToVRML(PrintWriter out, Color lineColor, double transparency) {
		Iterable<String> it = ()->lines.stream().map(i->i.toString()).iterator();
		out.println("Shape {");
		out.printf (Locale.ENGLISH,
					"	appearance Appearance { material Material { emissiveColor %1.3f %1.3f %1.3f transparency %1.3f } }%n",
					lineColor.getRed()/255f, lineColor.getGreen()/255f, lineColor.getBlue()/255f, transparency);
		out.println("	geometry IndexedLineSet {");
		out.println("		coord Coordinate { point [ "+String.join(", ", points)+" ] }");
		out.println("		coordIndex [ "+String.join(" ", it)+" ]");
		out.println("	}");
		out.println("}");
	}
}
