package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Vector;
import java.util.function.Function;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class Tables {

	public static class SimplifiedColumnConfig {
		public String name;
		public int minWidth;
		public int maxWidth;
		public int prefWidth;
		public int currentWidth;
		public Class<?> columnClass;
		
		SimplifiedColumnConfig() {
			this("",String.class,-1,-1,-1,-1);
		}
		public SimplifiedColumnConfig(String name, Class<?> columnClass, int minWidth, int maxWidth, int prefWidth, int currentWidth) {
			this.name = name;
			this.columnClass = columnClass;
			this.minWidth = minWidth;
			this.maxWidth = maxWidth;
			this.prefWidth = prefWidth;
			this.currentWidth = currentWidth;
		}
	}

	public static interface SimplifiedColumnIDInterface {
		public SimplifiedColumnConfig getColumnConfig();
	}

	public static abstract class SimplifiedTableModel<ColumnID extends Enum<ColumnID> & SimplifiedColumnIDInterface> implements TableModel {
		
		protected ColumnID[] columns;
		private Vector<TableModelListener> tableModelListeners;
	
		protected SimplifiedTableModel(ColumnID[] columns) {
			this.columns = columns;
			tableModelListeners = new Vector<>();
		}
	
		@Override public void addTableModelListener(TableModelListener l) { tableModelListeners.add(l); }
		@Override public void removeTableModelListener(TableModelListener l) { tableModelListeners.remove(l); }
		
		protected void fireTableModelEvent(TableModelEvent e) {
			for (TableModelListener tml:tableModelListeners)
				tml.tableChanged(e);
		}
		protected void fireTableColumnUpdate(int columnIndex) {
			if (getRowCount()>0)
				fireTableModelEvent(new TableModelEvent(this, 0, getRowCount()-1, columnIndex, TableModelEvent.UPDATE));
		}
		protected void fireTableCellUpdate(int rowIndex, int columnIndex) {
			fireTableModelEvent(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
		}
		protected void fireTableRowAdded(int rowIndex) {
			fireTableModelEvent(new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
		}
		protected void fireTableUpdate() {
			fireTableModelEvent(new TableModelEvent(this));
		}
		protected void fireTableStructureUpdate() {
			fireTableModelEvent(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
		}
		
		public void initiateColumnUpdate(ColumnID columnID) {
			int columnIndex = getColumn( columnID );
			if (columnIndex>=0) fireTableColumnUpdate(columnIndex);
		}

		@Override public abstract int getRowCount();
		public abstract Object getValueAt(int rowIndex, int columnIndex, ColumnID columnID);
		
		public int getUnsortedRowsCount() { return 0; }
		
		protected ColumnID getColumnID(int columnIndex) {
			if (columnIndex<0) return null;
			if (columnIndex<columns.length) return columns[columnIndex];
			return null;
		}
		public int getColumn( ColumnID columnID ) {
			for (int i=0; i<columns.length; ++i)
				if (columns[i]==columnID)
					return i;
			return -1;
		}
		
		@Override public int getColumnCount() { return columns.length; }
		
		@Override
		public String getColumnName(int columnIndex) {
			ColumnID columnID = getColumnID(columnIndex);
			if (columnID==null) return null;
			return columnID.getColumnConfig().name; //getName();
		}
	
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			ColumnID columnID = getColumnID(columnIndex);
			if (columnID==null) return null;
			return columnID.getColumnConfig().columnClass; //getColumnClass();
		}
	
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex<0) return null;
			if (rowIndex>=getRowCount()) return null;
			ColumnID columnID = getColumnID(columnIndex);
			if (columnID==null) return null;
			return getValueAt(rowIndex, columnIndex, columnID);
		}
	
		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (rowIndex<0) return false;
			if (rowIndex>=getRowCount()) return false;
			ColumnID columnID = getColumnID(columnIndex);
			if (columnID==null) return false;
			return isCellEditable(rowIndex, columnIndex, columnID);
		}
		protected boolean isCellEditable(int rowIndex, int columnIndex, ColumnID columnID) { return false; }
	
		@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (rowIndex<0) return;
			if (rowIndex>=getRowCount()) return;
			ColumnID columnID = getColumnID(columnIndex);
			if (columnID==null) return;
			setValueAt(aValue, rowIndex, columnIndex, columnID);
		}
		protected void setValueAt(Object aValue, int rowIndex, int columnIndex, ColumnID columnID) {}
	
		public void setColumnWidths(JTable table) {
			TableColumnModel columnModel = table.getColumnModel();
			for (int i=0; i<columnModel.getColumnCount(); ++i) {
				ColumnID columnID = getColumnID(i);
				if (columnID!=null) {
					SimplifiedColumnConfig config = columnID.getColumnConfig();
					setColumnWidth(columnModel.getColumn(i), config.minWidth, config.maxWidth, config.prefWidth, config.currentWidth);
				}
			}
		}
	
		private void setColumnWidth(TableColumn column, int min, int max, int preferred, int width) {
			if (min>=0) column.setMinWidth(min);
			if (max>=0) column.setMinWidth(max);
			if (preferred>=0) column.setPreferredWidth(preferred);
			if (width    >=0) column.setWidth(width);
		}
	}
	
	public static class ComboboxCellEditor<T> extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 8936989376730045132L;

		private Object currentValue;
		private T[] values;
		private ListCellRenderer<? super T> renderer;
		
		public ComboboxCellEditor(T[] values) {
			this.values = values;
			this.currentValue = null;
			this.renderer = null;
		}
		
		public void addValue(T newValue) {
			stopCellEditing();
			values = Arrays.copyOf(values, values.length+1);
			values[values.length-1] = newValue;
		}

		public void setValues(T[] newValues) {
			stopCellEditing();
			values = newValues;
		}

		public void setRenderer(ListCellRenderer<? super T> renderer) {
			this.renderer = renderer;
		}

		public void setRenderer(Function<Object,String> converter) {
			this.renderer = new NonStringRenderer<T>(converter);
		}
		
		@Override
		public Object getCellEditorValue() {
			return currentValue;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.currentValue = value;
			
			JComboBox<T> cmbbx = new JComboBox<T>(values);
			if (renderer!=null) cmbbx.setRenderer(renderer);
			cmbbx.setSelectedItem(currentValue);
			cmbbx.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
			cmbbx.addActionListener(e->{
				currentValue = cmbbx.getSelectedItem();
				fireEditingStopped();
			});
			
			return cmbbx;
		}
		
	}
	
	public static class NonStringRenderer<T> implements ListCellRenderer<T>, TableCellRenderer {
		
		private RendererComponent comp;
		private Function<Object, String> converter;
		
		public NonStringRenderer(Function<Object,String> converter) {
			this.converter = converter;
			this.comp = new RendererComponent();
			comp.setPreferredSize(new Dimension(1,16));
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Color bgColor   = isSelected ? table.getSelectionBackground() : table.getBackground();
			Color textColor = isSelected ? table.getSelectionForeground() : table.getForeground();
			comp.set(converter.apply(value),bgColor,textColor);
			return comp;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
			Color bgColor   = isSelected ? list.getSelectionBackground() : list.getBackground();
			Color textColor = isSelected ? list.getSelectionForeground() : list.getForeground();
			comp.set(converter.apply(value),bgColor,textColor);
			return comp;
		}

		public static class RendererComponent extends LabelRendererComponent {
			private static final long serialVersionUID = 6214683201455907406L;

			private RendererComponent() {
				setOpaque(true);
			}

			public void set(String value, Color bgColor, Color textColor) {
				setBackground(bgColor);
				setForeground(textColor);
				setText(value==null?"":value);
			}
		}
	}

	private static class LabelRendererComponent extends JLabel {
		private static final long serialVersionUID = -4524101782848184348L;
		
		@Override public void revalidate() {}
		@Override public void invalidate() {}
		@Override public void validate() {}
		@Override public void repaint(long tm, int x, int y, int width, int height) {}
		@Override public void repaint(Rectangle r) {}
		@Override public void repaint() {}
		@Override public void repaint(long tm) {}
		@Override public void repaint(int x, int y, int width, int height) {}

		@Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
		@Override public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
		@Override public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
		@Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
		@Override public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
		@Override public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
		@Override public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
		@Override public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
		@Override public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
	}


}
