package net.schwarzbaer.system;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;


public final class System {

	public static void listSystemPropertiesSorted(PrintStream out) {
		Properties properties = java.lang.System.getProperties();
		Vector<Object> keySet = new Vector<Object>(properties.keySet());
		Collections.sort(keySet,new Comparator<Object>(){
			@Override public int compare(Object o1, Object o2) { return o1.toString().compareTo(o2.toString()); }
		});
		for (int i=0; i<keySet.size(); i++) {
			out.println(String.format("%s=%s", toString(keySet.get(i)),replace(toString(properties.get(keySet.get(i))))));
		}
	}

	private static String replace(String string) {
		for (int i=0; i<32; i++)
			string = string.replace(""+(char)i, String.format("%%%02X", i));
		return string;
	}

	public static String toString(Object object) {
		return (object==null?"<null>":object.toString());
	}

}
