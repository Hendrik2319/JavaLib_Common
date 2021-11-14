package net.schwarzbaer.vrml;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Consumer;

public class VrmlTools {
	
	public static void writeVRML(File file, Consumer<PrintWriter> writeContent) {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			
			writeHeader(out);
			writeContent.accept(out);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void writeHeader(PrintWriter out) { writeHeader(out, 0.6, 0.7, 0.8); }
	public static void writeHeader(PrintWriter out, Color sky) { writeHeader(out, sky.getRed()/255f, sky.getGreen()/255f, sky.getBlue()/255f); }
	public static void writeHeader(PrintWriter out, double skyR, double skyG, double skyB) {
		out.println("#VRML V2.0 utf8");
		out.println();
		out.printf (Locale.ENGLISH, "Background { skyColor %1.3f %1.3f %1.3f }%n", skyR, skyG, skyB);
		out.println();
	}

	public static String toString(Color c) {
		return String.format(Locale.ENGLISH, "%1.3f %1.3f %1.3f", c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f);
	}
}
