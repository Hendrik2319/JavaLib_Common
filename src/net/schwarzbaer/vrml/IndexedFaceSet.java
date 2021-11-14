package net.schwarzbaer.vrml;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Vector;

import net.schwarzbaer.geometry.spacial.ConstPoint3d;

public class IndexedFaceSet extends PointBasedSet {
	
	private final Vector<Integer> faces;

	public IndexedFaceSet(String pointCoordFormat, boolean optimizePointSet) {
		super(pointCoordFormat, optimizePointSet);
		faces = new Vector<>();
	}

	public void addFace(ConstPoint3d... points) {
		for (ConstPoint3d p : points) {
			int pIndex = addPoint(p);
			faces.add(pIndex);
		}
		faces.add(-1);
	}
	public void addFace(int... indexes) {
		for (int i : indexes)
			faces.add(i);
		faces.add(-1);
	}
	
	public void writeToVRML(PrintWriter out, Color lineColor, double transparency) {
		Iterable<String> it = ()->faces.stream().map(i->i.toString()).iterator();
		out.println("Shape {");
		out.printf (Locale.ENGLISH,
					"	appearance Appearance { material Material { emissiveColor %1.3f %1.3f %1.3f transparency %1.3f } }%n",
					lineColor.getRed()/255f, lineColor.getGreen()/255f, lineColor.getBlue()/255f, transparency);
		out.println("	geometry IndexedFaceSet {");
		out.println("		solid FALSE");
		out.println("		coord Coordinate { point [ "+String.join(", ", points)+" ] }");
		out.println("		coordIndex [ "+String.join(" ", it)+" ]");
		out.println("	}");
		out.println("}");
	}
}
