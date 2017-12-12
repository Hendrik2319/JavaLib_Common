package net.schwarzbaer.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 *
 * @author hscholtz
 */
public abstract class Canvas extends JComponent {
	private static final long serialVersionUID = 1936784818314303929L;
	
	protected boolean withDebugOutput;
	protected int width;
    protected int height;
    protected int preferredWidth;
    protected int preferredHeight;

    protected Canvas() {}
    
    public Canvas( int preferredWidth, int preferredHeight ) {
        this.width = -1;
        this.height = -1;
        this.preferredWidth  = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.withDebugOutput = false;
    }
    protected void sizeChanged( int width, int height ) {}
    protected abstract void paintCanvas(Graphics g, int width, int height );

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintCanvas( g, width, height );
    }
    
    public void setPreferredSize( int preferredWidth, int preferredHeight ) {
    	if (withDebugOutput) System.out.printf("setPreferredSize( %d, %d )\r\n", preferredWidth,preferredHeight);
        this.preferredWidth  = preferredWidth;
        this.preferredHeight = preferredHeight;
    }
    @Override
	public void setPreferredSize(Dimension prefSize) {
    	if (withDebugOutput) System.out.printf("setPreferredSize( %s )\r\n", prefSize);
		super.setPreferredSize(prefSize);
        this.preferredWidth  = prefSize.width;
        this.preferredHeight = prefSize.height;
	}

	@Override public Dimension getPreferredSize() {
    	if (withDebugOutput) System.out.printf("getPreferredSize() -> ( %d, %d )\r\n", preferredWidth,preferredHeight);
		return new Dimension( preferredWidth, preferredHeight );
	}
	
    @Override public void setBounds(int x, int y, int width, int height) {
    	if (withDebugOutput) System.out.printf("setBounds( %d, %d, %d, %d )\r\n", x, y, width, height);
    	super.setBounds( x, y, width, height );
    	this.width = width; this.height = height;
    	sizeChanged( width, height );
    }
    @Override public void setBounds(Rectangle r) {
    	if (withDebugOutput) System.out.printf("setBounds( %s )\r\n", r);
    	super.setBounds( r );
    	this.width = r.width; this.height = r.height;
    	sizeChanged( width, height );
    }
    @Override public void setSize(Dimension d) {
    	if (withDebugOutput) System.out.printf("setSize( %s )\r\n", d);
    	super.setSize( d );
    	this.width = d.width; this.height = d.height;
    	sizeChanged( width, height );
    }
    @Override public void setSize(int width, int height) {
    	if (withDebugOutput) System.out.printf("setSize( %d, %d )\r\n", width, height);
    	super.setSize( width, height );
    	this.width =   width; this.height =   height;
    	sizeChanged( width, height );
    }
}
