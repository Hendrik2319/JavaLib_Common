package net.schwarzbaer.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ValueListOutput extends Vector<ValueListOutput.Entry> {
	private static final long serialVersionUID = -5898390765518030500L;

	public void add(int indentLevel, String label, int     value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, long    value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, float   value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, double  value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, boolean value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, Integer value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, Long    value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, Float   value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, Double  value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, Boolean value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, String  value) { add(indentLevel, label, (Style)null, value); }
	public void add(int indentLevel, String label, Style style, int     value) { add(indentLevel, label, style, "%d", value); }
	public void add(int indentLevel, String label, Style style, long    value) { add(indentLevel, label, style, "%d", value); }
	public void add(int indentLevel, String label, Style style, float   value) { add(indentLevel, label, style, "%f", value); }
	public void add(int indentLevel, String label, Style style, double  value) { add(indentLevel, label, style, "%f", value); }
	public void add(int indentLevel, String label, Style style, boolean value) { add(indentLevel, label, style, "%s", value); }
	public void add(int indentLevel, String label, Style style, Integer value) { if (value==null) add(indentLevel, label, style, "<null> (%s)", "Integer"); else add(indentLevel, label, style, "%d", value); }
	public void add(int indentLevel, String label, Style style, Long    value) { if (value==null) add(indentLevel, label, style, "<null> (%s)", "Long"   ); else add(indentLevel, label, style, "%d", value); }
	public void add(int indentLevel, String label, Style style, Float   value) { if (value==null) add(indentLevel, label, style, "<null> (%s)", "Float"  ); else add(indentLevel, label, style, "%f", value); }
	public void add(int indentLevel, String label, Style style, Double  value) { if (value==null) add(indentLevel, label, style, "<null> (%s)", "Double" ); else add(indentLevel, label, style, "%f", value); }
	public void add(int indentLevel, String label, Style style, Boolean value) { if (value==null) add(indentLevel, label, style, "<null> (%s)", "Boolean"); else add(indentLevel, label, style, "%s", value); }
	public void add(int indentLevel, String label, Style style, String  value) { if (value==null) add(indentLevel, label, style, "<null> (%s)", "String" ); else add(indentLevel, label, style, "\"%s\"", value); }
	
	public void addEmptyLine() { add(null); }
	
	public void add(int indentLevel, String label, Style style, String format, Object... args) {
		add(new Entry(indentLevel, label, style, format, args));
	}
	public void add(int indentLevel, String label, String format, Object... args) {
		add(new Entry(indentLevel, label, null, format, args));
	}
	public void add(int indentLevel, String label) {
		add(new Entry(indentLevel, label));
	}

	public String generateOutput() {
		return generateOutput("");
	}
	
	public String generateOutput(String baseIndent) {
		StringBuilderOutput out = new StringBuilderOutput();
		generateOutput(baseIndent, out);
		return out.toString();
	}
	
	public void generateOutput(String baseIndent, StyledDocument doc, String styleNamesPrefix) {
		generateOutput(baseIndent, new StyledDocumentOutput(doc, styleNamesPrefix, null));
	}
	
	public void generateOutput(String baseIndent, StyledDocument doc, String styleNamesPrefix, int fontSize) {
		generateOutput(baseIndent, new StyledDocumentOutput(doc, styleNamesPrefix, fontSize));
	}
	
	public void generateOutput(String baseIndent, OutputTarget out) {
		HashMap<Integer,Integer> labelLengths = new HashMap<>();
		for (Entry entry:this)
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
		
		out.prepareOutput(this);
		for (Entry entry:this)
			if (entry == null)
				out.appendEmptyLine();
			else {
				String spacer = entry.valueStr.isEmpty() ? "" : entry.label.isEmpty() ? "  " : ": ";
				String indent = indents.get(entry.indentLevel);
				int labelLength = labelLengths.get(entry.indentLevel);
				String labelFormat = labelLength==0 ? "%s" : "%-"+labelLength+"s";
				out.appendLine(String.format("%s%s"+labelFormat+"%s", baseIndent, indent, entry.label, spacer), entry.style, entry.valueStr);
			}
		
	}
	
	interface OutputTarget {
		void appendEmptyLine();
		void prepareOutput(Vector<Entry> entries);
		void appendLine(String prefix, Style style, String valueStr);
	}
	
	static class StringBuilderOutput implements OutputTarget {
		private final StringBuilder sb;
		StringBuilderOutput() {
			this.sb = new StringBuilder();;
		}
		@Override public void prepareOutput(Vector<Entry> entries) {}
		@Override public void appendEmptyLine() {
			sb.append(String.format("%n"));
		}
		@Override public void appendLine(String prefix, Style style, String valueStr) {
			sb.append(String.format("%s%s%n", prefix, valueStr));
		}
		@Override public String toString() {
			return sb.toString();
		}
	}
	
	static class StyledDocumentOutput implements OutputTarget {

		private final StyledDocument doc;
		private final String styleNamesPrefix;
		private javax.swing.text.Style mainStyle;
		private HashMap<Style,javax.swing.text.Style> subStyles;
		private final Integer fontSize;

		public StyledDocumentOutput(StyledDocument doc, String styleNamesPrefix, Integer fontSize) {
			this.doc = doc;
			this.styleNamesPrefix = styleNamesPrefix;
			this.fontSize = fontSize;
			mainStyle = null;
			subStyles = null;
		}

		@Override public void prepareOutput(Vector<Entry> entries) {
			HashSet<Style> styles = new HashSet<>();
			for (Entry entry : entries)
				if (entry.style!=null)
					styles.add(entry.style);
			
			mainStyle = doc.addStyle(styleNamesPrefix+".Main", null);
			StyleConstants.setFontFamily(mainStyle, "Monospaced");
			if (fontSize!=null) StyleConstants.setFontSize(mainStyle, fontSize);
			subStyles = new HashMap<>();
			
			Vector<Style> list = new Vector<>(styles);
			for (int i=0; i<list.size(); i++) {
				Style style = list.get(i);
				String styleName = String.format("%s.SubStyle.%s", styleNamesPrefix, style.getID());
				//System.out.printf("Add Style \"%s\"%n", styleName);
				javax.swing.text.Style subStyle = doc.addStyle(styleName, mainStyle);
				style.setValuesTo(subStyle);
				subStyles.put(style, subStyle);
			}
		}

		@Override public void appendEmptyLine() {
			append(String.format("%n"), mainStyle);
		}

		@Override public void appendLine(String prefix, Style style, String valueStr) {
			append(prefix, mainStyle);
			javax.swing.text.Style subStyle = subStyles.get(style);
			append(String.format("%s%n", valueStr), subStyle==null ? mainStyle : subStyle);
		}

		private void append(String text, javax.swing.text.Style style) {
			try {
				doc.insertString(doc.getLength(), text, style); }
			catch (BadLocationException e) {
				System.err.printf("[ValueListOutput.StyledDocumentOutput] BadLocationException while inserting Strings into StyledDocument: %s%n", e.getMessage());
				//e.printStackTrace();
			}
		}
		
	}
	
	public static class Style {
		public static final Style BOLD   = new Style(true, false);
		public static final Style ITALIC = new Style(false, true);
		
		public final Color color;
		public final boolean isBold;
		public final boolean isItalic;
		
		public Style(Color color) {
			this(color, false, false);
		}
		public Style(boolean isBold, boolean isItalic) {
			this(null, isBold, isItalic);
		}
		public Style(Color color, boolean isBold, boolean isItalic) {
			this.color = color;
			this.isBold = isBold;
			this.isItalic = isItalic;
		}
		
		public String getID() {
			return String.format("%s:%s:%s", color==null ? "--------" : String.format("%08X", color.getRGB()), isBold ? "B" : "-", isItalic ? "I" : "-");
		}
		
		public void setValuesTo(javax.swing.text.Style subStyle) {
			StyleConstants.setBold  (subStyle, isBold);
			StyleConstants.setItalic(subStyle, isItalic);
			if (color!=null) StyleConstants.setForeground(subStyle, color);
		}
		
		@Override
		public int hashCode() {
			int hashCode = 0;
			if (isBold  ) hashCode ^= 0xF0F0F0F0;
			if (isItalic) hashCode ^= 0x0F0F0F0F;
			if (color!=null) hashCode ^= color.getRGB();
			return hashCode;
		}
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Style)) return false;
			Style other = (Style) obj;
			if (other.isBold!=this.isBold) return false;
			if (other.isItalic!=this.isItalic) return false;
			if (other.color==null && this.color==null) return true;
			if (other.color==null || this.color==null) return false;
			return other.color.getRGB()==this.color.getRGB();
		}
		
	}
	

	static class Entry {
		final int indentLevel;
		final String label;
		final String valueStr;
		final Style style;
		
		Entry(int indentLevel, String label, Style style, String format, Object[] args) {
			this.indentLevel = indentLevel;
			this.style = style;
			this.label = label==null ? "" : label.trim();
			this.valueStr = String.format(Locale.ENGLISH, format, args);
		}
		Entry(int indentLevel, String label) {
			this(indentLevel, label, null, "", new Object[0]);
		}
	}
	
}