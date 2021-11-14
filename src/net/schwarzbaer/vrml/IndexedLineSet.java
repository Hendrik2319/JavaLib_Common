package net.schwarzbaer.vrml;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Vector;

import net.schwarzbaer.geometry.spacial.ConstPoint3d;

public class IndexedLineSet extends PointBasedSet {
	
	private final Vector<Integer> lines;

	public IndexedLineSet(String pointCoordFormat, boolean optimizePointSet) {
		super(pointCoordFormat, optimizePointSet);
		lines = new Vector<>();
	}
	
	public void addAxesCross(ConstPoint3d p, double d) {
		addLine(p.add( d,0,0),p.add(-d,0,0));
		addLine(p.add(0, d,0),p.add(0,-d,0));
		addLine(p.add(0,0, d),p.add(0,0,-d));
	}
	
	public void addLine(ConstPoint3d... points) {
		for (ConstPoint3d p : points) {
			int pIndex = addPoint(p);
			lines.add(pIndex);
		}
		lines.add(-1);
	}
	
	public void addLine(int... indexes) {
		for (int i : indexes)
			lines.add(i);
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
