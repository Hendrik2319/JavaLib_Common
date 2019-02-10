package net.schwarzbaer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;

public class ImageGridPanel extends JPanel {
	private static final long serialVersionUID = -189481388341606323L;
	private static Color   COLOR_BACKGROUND = null;
	private static Color   COLOR_BACKGROUND_SELECTED = null;
	private static Color   COLOR_BACKGROUND_PRESELECTED = null;
	private static Color[] COLOR_BACKGROUND_MARKED = null;
	private static Color   COLOR_FOREGROUND = null;
	private static Color   COLOR_FOREGROUND_SELECTED = null;
	
	private Vector<ImageGridPanel.SelectionListener> selectionListeners;
	private Vector<ImageGridPanel.RightClickListener> rightClickListener;
	private int cols;
	public int selectedIndex;
	public Vector<ImageGridPanel.ImageLabel> imageLabels;
	
	public ImageGridPanel(int cols, String preselectedImageID, Iterable<ImageData> images) {
		super(new GridLayout(0,cols,0,0));
		this.cols = cols;
		this.selectionListeners = new Vector<>();
		this.rightClickListener = new Vector<>();
		this.imageLabels = new Vector<>();
		
		ImageLabel.defaultFont = new JLabel().getFont();
		JTextArea dummy = new JTextArea();
		COLOR_BACKGROUND = dummy.getBackground();
		COLOR_FOREGROUND = dummy.getForeground();
		COLOR_BACKGROUND_SELECTED = dummy.getSelectionColor();
		COLOR_FOREGROUND_SELECTED = dummy.getSelectedTextColor();
		COLOR_BACKGROUND_PRESELECTED = brighter(COLOR_BACKGROUND_SELECTED,0.7f);
		COLOR_BACKGROUND_MARKED = new Color[] { Color.LIGHT_GRAY, new Color(0xDCB9F2) };
		
		createImageLabels(preselectedImageID,images,null);
		
		//setBorder(BorderFactory.createEtchedBorder());
		setBackground(COLOR_BACKGROUND);
	}
	
	public static Color brighter(Color color, float fraction) {
		// fraction==0.0:  same color
		// fraction==1.0:  WHITE
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		r = Math.min(255, Math.round(255-(255-r)*(1-fraction)));
		g = Math.min(255, Math.round(255-(255-g)*(1-fraction)));
		b = Math.min(255, Math.round(255-(255-b)*(1-fraction)));
		return new Color(r,g,b);
	}
	
	public static class ImageData {
		public String ID;
		public String name;
		public BufferedImage image;
		public ImageData(String iD, String name, BufferedImage image) {
			ID = iD;
			this.name = name;
			this.image = image;
		}
	}

	public void resetImages(Iterable<ImageData> images) {
		String selectedImageID = getSelectedImageID();
		removeAll();
		createImageLabels(selectedImageID,images,null);
	}

	protected String getSelectedImageID() {
		String selectedImageID = null;
		if (selectedIndex>=0)
			selectedImageID = imageLabels.get(selectedIndex).ID;
		return selectedImageID;
	}

	protected void createImageLabels(String preselectedImageID, Iterable<ImageData> images, Consumer<Integer> indexOutput) {
		selectedIndex = -1;
		imageLabels.clear();
		if (images==null) return;
		
		int index = 0;
		for (ImageData imageData : images) {
			if (imageData.image!=null) {
				boolean isSelected = imageData.ID.equals(preselectedImageID);
				if (isSelected) selectedIndex=index;
				ImageGridPanel.ImageLabel imageLabel = new ImageLabel(this,imageData.ID,imageData.name,index,imageData.image,isSelected);
				imageLabels.add(imageLabel);
				add(imageLabel);
				++index;
				if (indexOutput!=null)
					indexOutput.accept(index);
			}
		}
	}

	public void setImageName(int index, String newName) {
		imageLabels.get(index).changeName(newName);
	}

	public void    addSelectionListener( ImageGridPanel.SelectionListener l ) { selectionListeners.   add(l); }
	public void removeSelectionListener( ImageGridPanel.SelectionListener l ) { selectionListeners.remove(l); }
	
	protected void setSelectedImage(String ID, int index) {
		if (selectedIndex>=0)
			imageLabels.get(selectedIndex).setSelected(false,false);
		
		selectedIndex=index;
		for (ImageGridPanel.SelectionListener l:selectionListeners)
			l.imageWasSelected(ID);
		
		if (selectedIndex>=0)
			imageLabels.get(selectedIndex).setSelected(true,true);
	}

	public static interface SelectionListener {
		public void imageWasSelected(String ID);
	}
	
	public void    addRightClickListener( ImageGridPanel.RightClickListener l ) { rightClickListener.   add(l); }
	public void removeRightClickListener( ImageGridPanel.RightClickListener l ) { rightClickListener.remove(l); }
	
	protected void processRightClick(String ID, int index, Component source, int x, int y) {
		for (ImageGridPanel.RightClickListener l:rightClickListener)
			l.imageWasRightClicked(ID, index, source, x, y);
	}

	public static interface RightClickListener {
		public void imageWasRightClicked(String ID, int index, Component source, int x, int y);
	}
	
	public void scrollToPreselectedImage(JScrollPane imageScrollPane) {
		if (selectedIndex>=0) {
			int row = selectedIndex/cols;
			int rowCount = Math.round((float)Math.ceil(imageLabels.size()/(double)cols));
			//System.out.printf("Row %d/%d was preselected\r\n",row,rowCount);
			
			JScrollBar scrollBar = imageScrollPane.getVerticalScrollBar();
			int val = scrollBar.getValue();
			int max = scrollBar.getMaximum();
			int min = scrollBar.getMinimum();
			int ext = scrollBar.getVisibleAmount();
			//System.out.printf("VerticalScrollBar is at %d..%d(%d)..%d \r\n",min,val,ext,max);
			
			int h = (max-min)/rowCount;
			//System.out.printf("h = %d \r\n",h);
			val = row*h - (ext-h)/2 + min;
			//System.out.printf("val = %d \r\n",val);
			val = Math.max(min,val);
			val = Math.min(max-ext,val);
			
			scrollBar.setValue(val);
			//System.out.printf("VerticalScrollBar set to %d..%d(%d)..%d \r\n",min,val,ext,max);
		}
	}

	public static class ImageLabel extends JPanel {
		private static final long serialVersionUID = 4629632101041946456L;
		public static Font defaultFont;

		private JTextArea textArea;
		private boolean isSelected;
		private int markerIndex;
		public String ID;

		public ImageLabel(ImageGridPanel parent, String ID, String name, int index, BufferedImage image, boolean isSelected) {
			super(new BorderLayout(3,3));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			this.ID = ID;
			this.isSelected = isSelected;
			this.markerIndex = 0;
			
			textArea = new JTextArea(name);
			textArea.setPreferredSize(new Dimension(100,60));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(false);
			textArea.setEditable(false);
			textArea.setFont(defaultFont);
			textArea.setBackground(null);
			MouseListener[] mouseListeners = textArea.getMouseListeners();
			MouseMotionListener[] mouseMotionListeners = textArea.getMouseMotionListeners();
			for (MouseListener l:mouseListeners) textArea.removeMouseListener(l);
			for (MouseMotionListener l:mouseMotionListeners) textArea.removeMouseMotionListener(l);
			
			
			add(new JLabel(new ImageIcon(image)),BorderLayout.NORTH);
			add(textArea,BorderLayout.CENTER);
			
			MouseInputAdapter m = new MouseInputAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					if (e.getButton()==MouseEvent.BUTTON3) parent.processRightClick(ImageLabel.this.ID, index, ImageLabel.this, e.getX(), e.getY());
					else parent.setSelectedImage(ImageLabel.this.ID,index);
				}
				@Override public void mouseEntered(MouseEvent e) { setColors(true); }
				@Override public void mouseExited (MouseEvent e) { setColors(false); }
			};
			
			setColors(false);
			addMouseListener(m);
			addMouseMotionListener(m);
			textArea.addMouseListener(m);
			textArea.addMouseMotionListener(m);
		}
		
		public void changeName(String newName) {
			textArea.setText(newName);
		}

		public void setSelected(boolean isSelected, boolean hasFocus) {
			this.isSelected = isSelected;
			//SaveViewer.log_ln("Image: %s -> %sselected", name, isSelected?"":"not ");
			setColors(hasFocus);
			//repaint();
		}

		public void setMark(boolean isMarkedP1, boolean isMarkedP2) {
			this.markerIndex = isMarkedP1?1:isMarkedP2?2:0;
			setColors(false);
			//repaint();
		}

		private void setColors(boolean hasFocus) {
			if      (hasFocus     ) setBackground(COLOR_BACKGROUND_SELECTED);
			else if (isSelected   ) setBackground(COLOR_BACKGROUND_PRESELECTED);
			else if (markerIndex>0) setBackground(COLOR_BACKGROUND_MARKED[markerIndex-1]);
			else                    setBackground(COLOR_BACKGROUND);
			if (hasFocus) textArea.setForeground(COLOR_FOREGROUND_SELECTED);
			else          textArea.setForeground(COLOR_FOREGROUND);
		}
	
	}

}