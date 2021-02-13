package net.schwarzbaer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CellEditorListener;
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

	public static abstract class SimplifiedTableModel<ColumnID extends SimplifiedColumnIDInterface> implements TableModel {
		
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
		
		public void forEachColum(BiConsumer<ColumnID,TableColumn> action) {
			for (int i=0; i<columns.length; ++i) {
				TableColumn column = null;
				if (table!=null) {
					int columnV = table.convertColumnIndexToView(i);
					column = table.getColumnModel().getColumn(columnV);
				}
				action.accept(columns[i],column);
			}
		}
		
		public ColumnID getColumnID(int columnIndex) {
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
		
		public int getSumOfPrefColumnWidths() {
			int sum = 0;
			for (ColumnID columnID:columns)
				sum += columnID.getColumnConfig().prefWidth;
			return sum;
		}
		
		public void setColumnWidths(JTable table) {
			TableColumnModel columnModel = table.getColumnModel();
			for (int i=0; i<columnModel.getColumnCount(); ++i) {
				ColumnID columnID = getColumnID(table.convertColumnIndexToModel(i));
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
	
	public static abstract class PopupTableCellEditorAndRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
		private static final long serialVersionUID = 4397569745226506250L;
		
		private LabelRendererComponent renderComp;
		private EditorComp editorComp;
		private JPanel popupContent;
		private Popup popup;
		private JTable table;
		private Component contentComp = null;
		private Component tableViewport;
		
		public PopupTableCellEditorAndRenderer(JTable table, Component tableViewport) {
			this.table = table;
			this.tableViewport = tableViewport;
			renderComp = new LabelRendererComponent();
			editorComp = new EditorComp();
			popup = null;
			
			JPanel center = new JPanel();
			center.setPreferredSize(new Dimension(100,150));
			center.setBorder(BorderFactory.createLoweredBevelBorder());
			center.setBackground(Color.CYAN);
			center.setOpaque(true);
			
			JButton btnSet = new JButton("Set");
			btnSet.addActionListener(e->{
				stopSelection();
				fireEditingStopped();
				deactivatePopup();
			});
			
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(e->{
				fireEditingCanceled();
				deactivatePopup();
			});
			
			popupContent = new JPanel(new GridBagLayout());
			popupContent.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 1; c.weighty = 0; c.gridy = 1;
			c.weightx = 1;
			c.gridx = 0; popupContent.add(new JLabel(),c);
			c.weightx = 0;
			c.gridx = 1; popupContent.add(btnSet,c);
			c.gridx = 2; popupContent.add(btnCancel,c);
			
			setContent(center);
		}

//		@Override
//		public boolean stopCellEditing() {
//			System.out.println("stopCellEditing() START");
//			boolean b = super.stopCellEditing();
//			System.out.println("stopCellEditing() END");
//			return b;
//		}
//		@Override
//		public void cancelCellEditing() {
//			System.out.println("cancelCellEditing() START");
//			super.cancelCellEditing();
//			System.out.println("cancelCellEditing() END");
//		}

		@Override
		protected void fireEditingStopped() {
//			System.out.println("fireEditingStopped() START");
			cancelSelection();
			super.fireEditingStopped();
//			System.out.println("fireEditingStopped() END");
		}
		@Override
		protected void fireEditingCanceled() {
//			System.out.println("fireEditingCanceled() START");
			cancelSelection();
			super.fireEditingCanceled();
//			System.out.println("fireEditingCanceled() END");
		}

		private GridBagConstraints setContent(Component content) {
			if (contentComp!=null) popupContent.remove(contentComp);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 3; c.weighty = 1; c.gridy = 0;
			c.weightx = 1;
			c.gridx = 0; popupContent.add(content,c);
			contentComp = content;
//			popupContent.repaint();
//			popupContent.revalidate();
//			content.revalidate();
			return c;
		}
		
		private static final Border DASHED_BORDER = BorderFactory.createDashedBorder(Color.BLACK, 1, 1);
		private static final Border EMPTY_BORDER  = BorderFactory.createEmptyBorder(1,1,1,1);

		private class EditorComp extends JLabel {
			private static final long serialVersionUID = 2786411238866454826L;
			private boolean activateOnPaint = false;
			private int rowM = -1;
			private int columnM = -1;

			EditorComp() {
				addMouseListener(new MouseListener() {
					@Override public void mouseReleased(MouseEvent e) {}
					@Override public void mousePressed (MouseEvent e) {}
					@Override public void mouseExited  (MouseEvent e) {}
					@Override public void mouseEntered (MouseEvent e) {}
					@Override public void mouseClicked (MouseEvent e) { activatePopup(rowM, columnM); }
				});
//				addFocusListener(new FocusListener() {
//					@Override public void focusLost  (FocusEvent e) { show("FocusListener","focusLost  "); }
//					@Override public void focusGained(FocusEvent e) { show("FocusListener","focusGained"); }
//				});
//				addComponentListener(new ComponentListener() {
//					@Override public void componentShown  (ComponentEvent e) { show("ComponentListener","componentShown  "); }
//					@Override public void componentResized(ComponentEvent e) { show("ComponentListener","componentResized"); }
//					@Override public void componentMoved  (ComponentEvent e) { show("ComponentListener","componentMoved  "); }
//					@Override public void componentHidden (ComponentEvent e) { show("ComponentListener","componentHidden "); }
//				});
				addAncestorListener(new AncestorListener() {
					@Override public void ancestorRemoved(AncestorEvent event) { show("AncestorListener","ancestorRemoved"); deactivatePopup(); }
					@Override public void ancestorMoved  (AncestorEvent event) { show("AncestorListener","ancestorMoved  "); }
					@Override public void ancestorAdded  (AncestorEvent event) { show("AncestorListener","ancestorAdded  "); }
				});
//				addHierarchyListener(new HierarchyListener() {
//					@Override public void hierarchyChanged(HierarchyEvent e) {
//						String extra = "";
//						switch(e.getID()) {
//						case HierarchyEvent.ANCESTOR_RESIZED: extra += ".ANCESTOR_RESIZED";
//						}
//						show("HierarchyListener","hierarchyChanged"+extra);
//					}
//				});
				addHierarchyBoundsListener(new HierarchyBoundsListener() {
					@Override public void ancestorResized(HierarchyEvent e) { show("HierarchyBoundsListener","ancestorResized"); deactivatePopup(); }
					@Override public void ancestorMoved  (HierarchyEvent e) { show("HierarchyBoundsListener","ancestorMoved  "); deactivatePopup(); }
				});
//				addPropertyChangeListener(new PropertyChangeListener() {
//					@Override public void propertyChange(PropertyChangeEvent evt) { show("PropertyChangeListener","propertyChange"); }
//				});
			}
			
			private void show(String str1, String str2) {
				//System.out.printf("EditorComp.%-24s.%-20s: %s%n", str1, str2, isVisible() ? "Visible" : "");
			}
			
			public void set(int rowM, int columnM) {
				this.rowM = rowM;
				this.columnM = columnM;
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (activateOnPaint) {
					activatePopup(rowM,columnM);
					activateOnPaint = false;
				}
			}

			public void activateOnPaint(boolean activateOnPaint) {
				this.activateOnPaint = activateOnPaint;
			}
		}
		
		protected abstract String getValueStr(int rowM, int columnM);
		protected abstract Component getSelectorPanel(int rowM, int columnM, SelectionChangeListener listener);
		protected abstract void copyCurrentSelectionToModel();
		protected abstract void stopSelection();
		protected abstract void cancelSelection();

		public interface SelectionChangeListener {
			void selectionChanged(String newValueStr);
		}

		private void activatePopup(int rowM, int columnM) {
//			System.out.printf("activatePopup%n");
			
			if (popup!=null) {
//				System.out.printf("   Popup already exists%n");
				return;
			}
			
			if (editorComp.isVisible()) {
				Point p = computePopupPos();
				popup = PopupFactory.getSharedInstance().getPopup(table/*renderComp*/, popupContent, p.x, p.y);
				popup.show();
			}
		}

		private Point computePopupPos() {
//			System.out.printf("table       :  VisibleRect: %s%n", table.getVisibleRect());
			Point tp = tableViewport.getLocationOnScreen();
			//int tw = table.getWidth();
			int th = tableViewport.getHeight();
			
			Point ep = editorComp.getLocationOnScreen();
			//int ew = editorComp.getWidth();
			int eh = editorComp.getHeight();
			
			Point pp = new Point(ep.x, ep.y+eh+2);
			//int pw = popupContent.getWidth();
			int ph = popupContent.getHeight();
			
//			System.out.printf("table       :  x:%d  y:%d  h:%d%n", tp.x, tp.y, th);
//			System.out.printf("editorComp  :  x:%d  y:%d  h:%d%n", ep.x, ep.y, eh);
//			System.out.printf("popupContent:  x:%d  y:%d  h:%d%n", pp.x, pp.y, ph);
			
			if (ph<th && pp.y+ph>=tp.y+th && ep.y-2-ph>tp.y)
				pp = new Point(ep.x, ep.y-2-ph);
			
			return pp;
		}

		private void deactivatePopup() {
//			System.out.printf("deactivatePopup:%n");
			if (popup!=null) {
				popup.hide();
				popup = null;
			}
		}

		@Override public Object getCellEditorValue() {
			copyCurrentSelectionToModel();
			return null;
		}

		@Override public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			int rowM    = table.convertRowIndexToModel   (row   );
			int columnM = table.convertColumnIndexToModel(column);
			configure(renderComp,getValueStr(rowM, columnM), table, isSelected, hasFocus);
			return renderComp;
		}
		@Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			int rowM    = table.convertRowIndexToModel   (row   );
			int columnM = table.convertColumnIndexToModel(column);
			configure(editorComp,getValueStr(rowM, columnM), table, isSelected, false);
			setContent(getSelectorPanel(rowM,columnM,editorComp::setText));
			editorComp.set(rowM, columnM);
			editorComp.activateOnPaint(true);
			return editorComp;
		}

		public void configure(JLabel comp, String valueStr, JTable table, boolean isSelected, boolean hasFocus) {
			comp.setText(valueStr);
			comp.setBorder(hasFocus ? DASHED_BORDER : EMPTY_BORDER);
			comp.setOpaque(true);
			comp.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			comp.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		}
	}
	
	public static class ComboboxCellEditor<T> extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 8936989376730045132L;

		private Object currentValue;
		protected Vector<T> valueVector;
		protected T[] valueArray;
		private ListCellRenderer<? super T> renderer;
		private Supplier<Vector<T>> volatileValueSource;
		
		public ComboboxCellEditor(Supplier<Vector<T>> volatileValueSource) {
			this(null,null,volatileValueSource);
			if (volatileValueSource==null) throw new IllegalArgumentException("Parameter \"volatileValueSource\" must not be null.");
		}
		public ComboboxCellEditor(Vector<T> values) {
			this(values,null,null);
			if (values==null) throw new IllegalArgumentException("Parameter \"values\" must not be null.");
		}
		public ComboboxCellEditor(T[] values) {
			this(null,values,null);
			if (values==null) throw new IllegalArgumentException("Parameter \"values\" must not be null.");
		}
		private ComboboxCellEditor(Vector<T> valueVector, T[] valueArray, Supplier<Vector<T>> volatileValueSource) {
			this.valueVector = valueVector;
			this.valueArray = valueArray;
			this.volatileValueSource = volatileValueSource;
			if (valueVector==null && valueArray==null && volatileValueSource==null) throw new IllegalArgumentException();
			this.currentValue = null;
			this.renderer = null;
		}
		public void addValue(T newValue) {
			if (volatileValueSource!=null) throw new UnsupportedOperationException();
			stopCellEditing();
			if (valueArray!=null) {
				valueArray = Arrays.copyOf(valueArray, valueArray.length+1);
				valueArray[valueArray.length-1] = newValue;
			}
			if (valueVector!=null)
				valueVector.add(newValue);
		}

		public void setValues(T[] newValues) {
			if (volatileValueSource!=null) throw new UnsupportedOperationException();
			stopCellEditing();
			valueArray = newValues;
			valueVector = null;
		}

		public void setValues(Vector<T> newValues) {
			if (volatileValueSource!=null) throw new UnsupportedOperationException();
			stopCellEditing();
			valueArray = null;
			valueVector = newValues;
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
		
		protected void updateAtEditStart(int rowM, int columnM) {}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.currentValue = value;
			
			int rowM    = table.convertRowIndexToModel(row);
			int columnM = table.convertColumnIndexToModel(column);
			updateAtEditStart(rowM,columnM);
			
			JComboBox<T> cmbbx;
			if      (valueArray         !=null) cmbbx = new JComboBox<T>(valueArray);
			else if (valueVector        !=null) cmbbx = new JComboBox<T>(valueVector);
			else if (volatileValueSource!=null) cmbbx = new JComboBox<T>(volatileValueSource.get());
			else                        cmbbx = null;
			
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
	
	public static abstract class IconTextRenderer<T> implements ListCellRenderer<T>, TableCellRenderer {
		
		private final LabelRendererComponent comp;

		public IconTextRenderer() {
			this(new Dimension(1,16));
		}
		public IconTextRenderer(int width, int height) {
			this(new Dimension(width, height));
		}
		public IconTextRenderer(Dimension defaultSize) {
			comp = new LabelRendererComponent();
			comp.setPreferredSize(defaultSize);
		}
		
		protected abstract String convertToStr (Object value);
		protected abstract Icon   convertToIcon(Object value);
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			comp.configureAsTableCellRendererComponent(table, convertToIcon(value), convertToStr(value), isSelected, hasFocus);
			return comp;
		}
		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean hasFocus) {
			comp.configureAsListCellRendererComponent(list, convertToIcon(value), convertToStr(value), index, isSelected, hasFocus);
			return comp;
		}
	}
	
	public static class NonStringRenderer<T> implements ListCellRenderer<T>, TableCellRenderer {
		
		private final LabelRendererComponent comp;
		private final Function<Object, String> converter;
		
		public NonStringRenderer(Function<Object,String> converter) {
			this.converter = converter;
			this.comp = new LabelRendererComponent();
			comp.setPreferredSize(new Dimension(1,16));
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			String valueStr = converter.apply(value);
			if (valueStr==null) valueStr = "";
			comp.configureAsTableCellRendererComponent(table, null, valueStr, isSelected, hasFocus);
			return comp;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean hasFocus) {
			String valueStr = converter.apply(value);
			if (valueStr==null) valueStr = "";
			comp.configureAsListCellRendererComponent(list, null, valueStr, index, isSelected, hasFocus);
			return comp;
		}
	}

	public interface RendererConfigurator {
		static final Border EMPTY_BORDER  = BorderFactory.createEmptyBorder(1,1,1,1);
		static final Border DASHED_BORDER = BorderFactory.createDashedBorder(Color.BLACK);
		
		default void configureAsTableCRC(JTable table, boolean isSelected, boolean hasFocus) {
			configureAsTableCRC(table, isSelected, hasFocus, null, null);
		}
		default void configureAsTableCRC(JTable table, boolean isSelected, boolean hasFocus, Supplier<Color> getCustomForeground, Supplier<Color> getCustomBackground) {
			setBorder(hasFocus ? DASHED_BORDER : EMPTY_BORDER);
			setOpaque(true);
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				Color background = getCustomBackground==null ? null : getCustomBackground.get();
				Color foreground = getCustomForeground==null ? null : getCustomForeground.get();
				setBackground(background==null ? table.getBackground() : background);
				setForeground(foreground==null ? table.getForeground() : foreground);
			}
		}
		default void configureAsListCRC(JList<?> list, int index, boolean isSelected, boolean hasFocus) {
			configureAsListCRC(list, index, isSelected, hasFocus, null, null);
		}
		default void configureAsListCRC(JList<?> list, int index, boolean isSelected, boolean hasFocus, Supplier<Color> getCustomForeground, Supplier<Color> getCustomBackground) {
			if (index<0) {
				setOpaque(false);
				setForeground(list.getForeground());
				setBorder(isSelected ? DASHED_BORDER : EMPTY_BORDER);
			} else {
				setBorder(hasFocus ? DASHED_BORDER : EMPTY_BORDER);
				setOpaque(true);
				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					Color background = getCustomBackground==null ? null : getCustomBackground.get();
					Color foreground = getCustomForeground==null ? null : getCustomForeground.get();
					setBackground(background==null ? list.getBackground() : background);
					setForeground(foreground==null ? list.getForeground() : foreground);
				}
			}
		}
	
		void setBorder(Border border);
		void setOpaque(boolean isOpaque);
		void setForeground(Color color);
		void setBackground(Color color);
		
		static RendererConfigurator create(JComponent rendererComp) {
			return new RendererConfigurator() {
				@Override public void setBorder    (Border  border  ) { rendererComp.setBorder    (border  ); }
				@Override public void setOpaque    (boolean isOpaque) { rendererComp.setOpaque    (isOpaque); }
				@Override public void setForeground(Color   color   ) { rendererComp.setForeground(color   ); }
				@Override public void setBackground(Color   color   ) { rendererComp.setBackground(color   ); }
			};
		}
		static RendererConfigurator create(Consumer<Border> setBorder, Consumer<Boolean> setOpaque, Consumer<Color> setForeground, Consumer<Color> setBackground) {
			return new RendererConfigurator() {
				@Override public void setBorder    (Border  border  ) { setBorder    .accept(border  ); }
				@Override public void setOpaque    (boolean isOpaque) { setOpaque    .accept(isOpaque); }
				@Override public void setForeground(Color   color   ) { setForeground.accept(color   ); }
				@Override public void setBackground(Color   color   ) { setBackground.accept(color   ); }
			};
		}
	}

	public static class CheckBoxRendererComponent extends JCheckBox {
		private static final long serialVersionUID = -1094682628853018055L;
		private RendererConfigurator rendConf;
		
		public CheckBoxRendererComponent() {
			rendConf = RendererConfigurator.create(this);
		}
		
		public void configureAsTableCellRendererComponent(JTable table, boolean isChecked, String valueStr, boolean isSelected, boolean hasFocus) {
			configureAsTableCellRendererComponent(table, isChecked, valueStr, isSelected, hasFocus, null, null);
		}
		public void configureAsTableCellRendererComponent(JTable table, boolean isChecked, String valueStr, boolean isSelected, boolean hasFocus, Supplier<Color> getCustomForeground, Supplier<Color> getCustomBackground) {
			rendConf.configureAsTableCRC(table, isSelected, hasFocus, getCustomForeground, getCustomBackground);
			setSelected(isChecked);
			setText(valueStr);
		}
		
		public void configureAsListCellRendererComponent(JList<?> list, boolean isChecked, String valueStr, int index, boolean isSelected, boolean hasFocus) {
			configureAsListCellRendererComponent(list, isChecked, valueStr, index, isSelected, hasFocus, null, null);
		}
		public void configureAsListCellRendererComponent(JList<?> list, boolean isChecked, String valueStr, int index, boolean isSelected, boolean hasFocus, Supplier<Color> getCustomForeground, Supplier<Color> getCustomBackground) {
			rendConf.configureAsListCRC(list, index, isSelected, hasFocus, getCustomForeground, getCustomBackground);
			setSelected(isChecked);
			setText(valueStr);
		}

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
		private RendererConfigurator rendConf;
		
		public LabelRendererComponent() {
			rendConf = RendererConfigurator.create(this);
		}
		
		public void configureAsTableCellRendererComponent(JTable table, Icon icon, String valueStr, boolean isSelected, boolean hasFocus) {
			configureAsTableCellRendererComponent(table, icon, valueStr, isSelected, hasFocus, null, null);
		}
		public void configureAsTableCellRendererComponent(JTable table, Icon icon, String valueStr, boolean isSelected, boolean hasFocus, Supplier<Color> getCustomBackground, Supplier<Color> getCustomForeground) {
			rendConf.configureAsTableCRC(table, isSelected, hasFocus, getCustomForeground, getCustomBackground);
			setIcon(icon);
			setText(valueStr);
		}
		
		public void configureAsListCellRendererComponent(JList<?> list, Icon icon, String valueStr, int index, boolean isSelected, boolean hasFocus) {
			configureAsListCellRendererComponent(list, icon, valueStr, index, isSelected, hasFocus, null, null);
		}
		public void configureAsListCellRendererComponent(JList<?> list, Icon icon, String valueStr, int index, boolean isSelected, boolean hasFocus, Supplier<Color> getCustomBackground, Supplier<Color> getCustomForeground) {
			rendConf.configureAsListCRC(list, index, isSelected, hasFocus, getCustomForeground, getCustomBackground);
			setIcon(icon);
			setText(valueStr);
		}

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

	public static class CellwiseCellEditor implements TableCellEditor {
		
		public interface CellEditorSelector {
			TableCellEditor getCellEditor(int row, int column);
		}
		
		private TableCellEditor currentCellEditor;
		private CellEditorSelector cellEditorSelector;

		public CellwiseCellEditor(CellEditorSelector cellEditorSelector) {
			this.cellEditorSelector = cellEditorSelector;
			currentCellEditor = null;
		}

		@Override public boolean isCellEditable  (EventObject anEvent) { return true; }
		@Override public boolean shouldSelectCell(EventObject anEvent) { return true; }

		@Override public Object getCellEditorValue() {
			if (currentCellEditor!=null)
				return currentCellEditor.getCellEditorValue();
			return null;
		}

		@Override public boolean stopCellEditing() {
			if (currentCellEditor!=null)
				return currentCellEditor.stopCellEditing();
			return false;
		}
		@Override public void cancelCellEditing() {
			if (currentCellEditor!=null)
				currentCellEditor.cancelCellEditing();
		}

		@Override public void addCellEditorListener(CellEditorListener l) {
			if (currentCellEditor!=null)
				currentCellEditor.addCellEditorListener(l);
		}
		@Override public void removeCellEditorListener(CellEditorListener l) {
			if (currentCellEditor!=null)
				currentCellEditor.removeCellEditorListener(l);
		}

		@Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			currentCellEditor = cellEditorSelector.getCellEditor(row, column);
			return currentCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}

}
