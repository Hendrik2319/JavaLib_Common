package net.schwarzbaer.gui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

public class ValueListOutput extends Vector<ValueListOutput.Entry> {
	private static final long serialVersionUID = -5898390765518030500L;

	public void add(int indentLevel, String label, int     value) { add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, long    value) { add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, float   value) { add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, double  value) { add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, boolean value) { add(indentLevel, label, "%s", value); }
	public void add(int indentLevel, String label, Integer value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Integer"); else add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, Long    value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Long"   ); else add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, Float   value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Float"  ); else add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, Double  value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Double" ); else add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, Boolean value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Boolean"); else add(indentLevel, label, "%s", value); }
	public void add(int indentLevel, String label, String  value) { if (value==null) add(indentLevel, label, "<null> (%s)", "String" ); else add(indentLevel, label, "\"%s\"", value); }
	
	public void addEmptyLine() { add(null); }
	
	public void add(int indentLevel, String label, String format, Object... args) {
		add(new Entry(indentLevel, label, format, args));
	}
	public void add(int indentLevel, String label) {
		add(new Entry(indentLevel, label, ""));
	}

	public String generateOutput() {
		return generateOutput("");
	}
	public String generateOutput(String baseIndent) {
		HashMap<Integer,Integer> labelLengths = new HashMap<>();
		for (ValueListOutput.Entry entry:this)
			if (entry!=null){
				Integer maxLength = labelLengths.get(entry.indentLevel);
				if (maxLength==null) maxLength=0;
				maxLength = Math.max(entry.label.length(), maxLength);
				labelLengths.put(entry.indentLevel,maxLength);
			}
		
		HashMap<Integer,String> indents = new HashMap<>();
		for (Integer indentLevel:labelLengths.keySet()) {
			String str = ""; int i=0;
			while (i<indentLevel) { str += "    "; i++; }
			indents.put(indentLevel, str);
		}
		
		StringBuilder sb = new StringBuilder();
		for (ValueListOutput.Entry entry:this)
			if (entry == null)
				sb.append(String.format("%n"));
			else {
				String spacer = entry.valueStr.isEmpty() ? "" : entry.label.isEmpty() ? "  " : ": ";
				String indent = indents.get(entry.indentLevel);
				int labelLength = labelLengths.get(entry.indentLevel);
				String labelFormat = labelLength==0 ? "%s" : "%-"+labelLength+"s";
				sb.append(String.format("%s%s"+labelFormat+"%s%s%n", baseIndent, indent, entry.label, spacer, entry.valueStr));
			}
		
		return sb.toString();
	}

	static class Entry {
		final int indentLevel;
		final private String label;
		final private String valueStr;
		public Entry(int indentLevel, String label, String format, Object... args) {
			this.indentLevel = indentLevel;
			this.label = label==null ? "" : label.trim();
			this.valueStr = String.format(Locale.ENGLISH, format, args);
		}
	}
	
}