package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class Tables {
	
	public static class SimplifiedRowSorter extends RowSorter<SimplifiedTableModel<?>> {

		protected SimplifiedTableModel<?> model;
		private LinkedList<RowSorter.SortKey> keys;
		private Integer[] modelRowIndexes;
		private int[] viewRowIndexes;
		private Vector<RowSorterListener> listeners;

		public SimplifiedRowSorter(SimplifiedTableModel<?> model) {
			this.model = model;
			this.keys = new LinkedList<RowSorter.SortKey>();
			this.modelRowIndexes = null;
			this.viewRowIndexes = null;
			this.listeners = new Vector<>();
		}
		
		public void    addListener(RowSorterListener listener) { listeners.   add(listener); }
		public void removeListener(RowSorterListener listener) { listeners.remove(listener); }
		private void notifyListeners() {
			for (RowSorterListener listener:listeners)
				listener.sortingChangedByUser();
		}
		
		public interface RowSorterListener {
			public void sortingChangedByUser();
		}
		
		public void setModel(SimplifiedTableModel<?> model) {
			this.model = model;
			this.keys = new LinkedList<RowSorter.SortKey>();
			sort();
		}

		@Override public SimplifiedTableModel<?> getModel() { return model; }

		private void log(String format, Object... values) {
			//System.out.printf(String.format("[%08X:%s] ", this.hashCode(), name)+format+"\r\n",values);
		}

		private static String toString(List<? extends RowSorter.SortKey> keys) {
			if (keys==null) return "<null>";
			String str = "";
			for (RowSorter.SortKey key:keys) {
				if (!str.isEmpty()) str+=", ";
				str+=key.getColumn()+":"+key.getSortOrder();
			}
			if (!str.isEmpty()) str = "[ "+str+" ]";
			return str;
		}
		
		private void sort() {
			
			synchronized (this) {
				if (model==null) {
					this.modelRowIndexes = null;
					this.viewRowIndexes = null;
					return;
				}
				
				log("sort() -> %s",toString(keys));
				
				int rowCount = getModelRowCount();
				if (modelRowIndexes==null || modelRowIndexes.length!=rowCount)
					modelRowIndexes = new Integer[rowCount];
				
				for (int i=0; i<modelRowIndexes.length; ++i)
					modelRowIndexes[i] = i;
				
				Comparator<Integer> comparator = null;
				
				int unsortedRows = model.getUnsortedRowsCount();
				if (0<unsortedRows)
					comparator = Comparator.comparingInt((Integer row)->(row<unsortedRows?row:unsortedRows));
				
				for (SortKey key:keys) {
					SortOrder sortOrder = key.getSortOrder();
					if (sortOrder==SortOrder.UNSORTED) continue;
					int column = key.getColumn();
					
					if      (model.hasSpecialSorting(column)              ) comparator = addComparator(comparator,sortOrder,model.getSpecialSorting(column,sortOrder));
					else if (isNewClass(model.getColumnClass(column))     ) comparator = addComparatorForNewClass(comparator,sortOrder,column);
					else if (model.getColumnClass(column) == Boolean.class) comparator = addComparator(comparator,sortOrder,(Integer row)->(Boolean)model.getValueAt(row,column));
					else if (model.getColumnClass(column) == String .class) comparator = addComparator(comparator,sortOrder,(Integer row)->(String )model.getValueAt(row,column));
					else if (model.getColumnClass(column) == Long   .class) comparator = addComparator(comparator,sortOrder,(Integer row)->(Long   )model.getValueAt(row,column));
					else if (model.getColumnClass(column) == Integer.class) comparator = addComparator(comparator,sortOrder,(Integer row)->(Integer)model.getValueAt(row,column));
					else if (model.getColumnClass(column) == Double .class) comparator = addComparator(comparator,sortOrder,(Integer row)->(Double )model.getValueAt(row,column));
					else if (model.getColumnClass(column) == Float  .class) comparator = addComparator(comparator,sortOrder,(Integer row)->(Float  )model.getValueAt(row,column));
					else comparator = addComparator(comparator,sortOrder,
								(Integer row)->{
									Object object = model.getValueAt(row,column);
									if (object==null) return null;
									return object.toString();
								});
				}
				
				if (comparator!=null)
					Arrays.sort(modelRowIndexes, comparator);
				
				if (viewRowIndexes==null || viewRowIndexes.length!=rowCount)
					viewRowIndexes = new int[rowCount];
				for (int i=0; i<viewRowIndexes.length; ++i) viewRowIndexes[i] = -1;
				for (int i=0; i<modelRowIndexes.length; ++i) viewRowIndexes[modelRowIndexes[i]] = i;
			}
			
			fireSortOrderChanged();
		}
		
		protected boolean isNewClass(Class<?> columnClass) { return false; }
		protected Comparator<Integer> addComparatorForNewClass(Comparator<Integer> comparator, SortOrder sortOrder, int column) { return comparator; }

		protected Comparator<Integer> addComparator(Comparator<Integer> comp, SortOrder sortOrder, Comparator<Integer> specialSorting) {
			if (sortOrder==SortOrder.DESCENDING) {
				if (comp==null) comp = specialSorting;
				else            comp = comp.reversed().thenComparing(specialSorting);
				return comp.reversed();
			} else {
				if (comp==null) comp = specialSorting;
				else            comp = comp.thenComparing(specialSorting);
				return comp;
			}
		}

		protected <U extends Comparable<? super U>> Comparator<Integer> addComparator(Comparator<Integer> comp, SortOrder sortOrder, Function<? super Integer,? extends U> keyExtractor) {
			if (sortOrder==SortOrder.DESCENDING) {
				if (comp==null) comp = Comparator     .<Integer,U>comparing(keyExtractor,Comparator.<U>nullsFirst(Comparator.<U>naturalOrder()));
				else            comp = comp.reversed().    <U>thenComparing(keyExtractor,Comparator.<U>nullsFirst(Comparator.<U>naturalOrder()));
				return comp.reversed();
			} else {
				if (comp==null) comp = Comparator     .<Integer,U>comparing(keyExtractor,Comparator.<U>nullsLast(Comparator.<U>naturalOrder()));
				else            comp = comp           .    <U>thenComparing(keyExtractor,Comparator.<U>nullsLast(Comparator.<U>naturalOrder()));
				return comp;
			}
		}
		
		
		@Override
		public void toggleSortOrder(int column) {
			RemovePred pred = new RemovePred(column);
			keys.removeIf(pred);
			if (pred.oldSortOrder == SortOrder.ASCENDING)
				keys.addFirst(new SortKey(column, SortOrder.DESCENDING));
			else
				keys.addFirst(new SortKey(column, SortOrder.ASCENDING));
			log("toggleSortOrder( %d )", column);
			sort();
			notifyListeners();
		}

		private static class RemovePred implements Predicate<SortKey> {
			private int column;
			private SortOrder oldSortOrder;
			public RemovePred(int column) {
				this.column = column;
				this.oldSortOrder = SortOrder.UNSORTED;
			}
			@Override public boolean test(SortKey k) {
				if (k.getColumn()==column) {
					oldSortOrder = k.getSortOrder();
					return true;
				}
				return false;
			}
		}

		@Override
		public void setSortKeys(List<? extends RowSorter.SortKey> keys) {
			if (keys==null) this.keys = new LinkedList<RowSorter.SortKey>();
			else            this.keys = new LinkedList<RowSorter.SortKey>(keys);
			log("setSortKeys( %s )",toString(this.keys));
		}

		@Override
		public List<? extends RowSorter.SortKey> getSortKeys() {
			//log("getSortKeys()");
			return keys;
		}

		@Override
		public synchronized int convertRowIndexToModel(int index) {
			if (modelRowIndexes==null) return index;
			if (index<0) return -1;
			if (index>=modelRowIndexes.length) return -1;
			return modelRowIndexes[index];
		}

		@Override
		public synchronized int convertRowIndexToView(int index) {
			if (viewRowIndexes==null) return index;
			if (index<0) return -1;
			if (index>=viewRowIndexes.length) return -1;
			return viewRowIndexes[index];
		}

		@Override public int getViewRowCount() { return getModelRowCount(); }
		@Override public int getModelRowCount() { if (model==null) return 0; return model.getRowCount(); }

		@Override public void modelStructureChanged() { log("modelStructureChanged()"); sort(); }
		@Override public void allRowsChanged() { log("allRowsChanged()"); sort(); }
		@Override public void rowsInserted(int firstRow, int endRow) { log("rowsInserted( %d, %d )", firstRow, endRow); sort(); }
		@Override public void rowsDeleted(int firstRow, int endRow) { log("rowsDeleted( %d, %d )", firstRow, endRow); sort(); }
		@Override public void rowsUpdated(int firstRow, int endRow) { log("rowsUpdated( %d, %d )", firstRow, endRow); sort(); }
		@Override public void rowsUpdated(int firstRow, int endRow, int column) { log("rowsUpdated( %d, %d, %d )", firstRow, endRow, column); sort();
		}
		
	}

	public static class SimplifiedColumnConfig {
		public String name;
		public int minWidth;
		public int maxWidth;
		public int prefWidth;
		public int currentWidth;
		public Class<?> columnClass;
		public boolean hasSpecialSorting;
		
		public SimplifiedColumnConfig() {
			this("",String.class,-1,-1,-1,-1,false);
		}
		public SimplifiedColumnConfig(String name, Class<?> columnClass, int minWidth, int maxWidth, int prefWidth, int currentWidth) {
			this(name, columnClass, minWidth, maxWidth, prefWidth, currentWidth, false);
		}
		public SimplifiedColumnConfig(String name, Class<?> columnClass, int minWidth, int maxWidth, int prefWidth, int currentWidth, boolean hasSpecialSorting) {
			this.name = name;
			this.columnClass = columnClass;
			this.minWidth = minWidth;
			this.maxWidth = maxWidth;
			this.prefWidth = prefWidth;
			this.currentWidth = currentWidth;
			this.hasSpecialSorting = hasSpecialSorting;
		}
	}

	public static interface SimplifiedColumnIDInterface {
		public SimplifiedColumnConfig getColumnConfig();
	}

	public static abstract class SimplifiedTableModel<ColumnID extends Enum<ColumnID> & SimplifiedColumnIDInterface> implements TableModel {
		
		protected ColumnID[] columns;
		private Vector<TableModelListener> tableModelListeners;
		protected JTable table = null;
	
		protected SimplifiedTableModel(ColumnID[] columns) {
			this.columns = columns;
			tableModelListeners = new Vector<>();
		}
	
		public void setTable(JTable table) {
			this.table = table;
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
		protected void fireTableCellEvent(int rowIndex, int columnIndex, int type) {
			fireTableModelEvent(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, type));
		}
		protected void fireTableCellUpdate(int rowIndex, int columnIndex) { fireTableCellEvent(rowIndex, columnIndex, TableModelEvent.UPDATE); }
		protected void fireTableRowAdded  (int rowIndex) { fireTableCellEvent(rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT); }
		protected void fireTableRowRemoved(int rowIndex) { fireTableCellEvent(rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE); }
		protected void fireTableRowUpdate (int rowIndex) { fireTableCellEvent(rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE); }
		
		protected void fireTableRowsEvent(int firstRowIndex, int lastRowIndex, int type) {
			fireTableModelEvent(new TableModelEvent(this, firstRowIndex, lastRowIndex, TableModelEvent.ALL_COLUMNS, type));
		}
		protected void fireTableRowsAdded  (int firstRowIndex, int lastRowIndex) { fireTableRowsEvent(firstRowIndex, lastRowIndex, TableModelEvent.INSERT); }
		protected void fireTableRowsRemoved(int firstRowIndex, int lastRowIndex) { fireTableRowsEvent(firstRowIndex, lastRowIndex, TableModelEvent.DELETE); }
		protected void fireTableRowsUpdate (int firstRowIndex, int lastRowIndex) { fireTableRowsEvent(firstRowIndex, lastRowIndex, TableModelEvent.UPDATE); }
		
		public void fireTableUpdate() {
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
			if (max>=0) column.setMaxWidth(max);
			if (preferred>=0) column.setPreferredWidth(preferred);
			if (width    >=0) column.setWidth(width);
		}

		public boolean hasSpecialSorting(int columnIndex) {
			ColumnID columnID = getColumnID(columnIndex);
			if (columnID==null) return false;
			return columnID.getColumnConfig().hasSpecialSorting;
		}

		public Comparator<Integer> getSpecialSorting(int columnIndex, SortOrder sortOrder) {
			ColumnID columnID = getColumnID(columnIndex);
			if (columnID==null) return null;
			return getSpecialSorting(columnID,sortOrder);
		}

		protected Comparator<Integer> getSpecialSorting(ColumnID columnID, SortOrder sortOrder) {
			return null;
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
			comp.set(converter.apply(value),bgColor,textColor,hasFocus);
			return comp;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean hasFocus) {
			Color bgColor   = isSelected ? list.getSelectionBackground() : null; //list.getBackground();
			Color textColor = isSelected ? list.getSelectionForeground() : list.getForeground();
			comp.set(converter.apply(value),bgColor,textColor,hasFocus);
			return comp;
		}

		public static class RendererComponent extends LabelRendererComponent {
			private static final long serialVersionUID = 6214683201455907406L;
			
			private static final Border DASHED_BORDER = BorderFactory.createDashedBorder(Color.BLACK, 1, 1);
			private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(1,1,1,1);
			
			private RendererComponent() {
				//setOpaque(true);
			}

			public void set(String value, Color bgColor, Color textColor, boolean hasFocus) {
				setBorder(!hasFocus?EMPTY_BORDER:DASHED_BORDER);
				setOpaque(bgColor!=null);
				setBackground(bgColor);
				setForeground(textColor);
				setText(value==null?"":value);
			}
		}
	}

	public static class CheckBoxRendererComponent extends JCheckBox {
		private static final long serialVersionUID = -1094682628853018055L;

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

	public static class LabelRendererComponent extends JLabel {
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
