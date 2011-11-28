package net.schwarzbaer.gui;

import javax.swing.DefaultBoundedRangeModel;

public class AutoDownScroller extends DefaultBoundedRangeModel {
	private static final long serialVersionUID = 6952749920749411231L;

	@Override
	public void setRangeProperties( int newValue, int newExtent, int newMin, int newMax, boolean adjusting ) {
		super.setRangeProperties( newValue, newExtent, newMin, newMax, adjusting );
//		System.out.println( "min:"+newMin + " val:"+newValue + " val+ext:"+(newValue+newExtent) + " max:"+newMax + " adj:"+adjusting );
		
		if (!adjusting)  {
			int val = this.getValue();
			int ext = this.getExtent();
			int max = this.getMaximum();
			if (val != max - ext)
				super.setValue(max - ext);
		}
	}
}
