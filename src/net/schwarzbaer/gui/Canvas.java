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
	
	protected int width;
    protected int height;
    protected int preferredWidth;
    protected int preferredHeight;

    public Canvas( int preferredWidth, int preferredHeight ) {
        this.width = -1;
        this.height = -1;
        this.preferredWidth  = preferredWidth;
        this.preferredHeight = preferredHeight;
    }

    protected void sizeChanged( int width, int height ) {}
    protected abstract void paintCanvas(Graphics g, int width, int height );

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintCanvas( g, width, height );
    }

    @Override public Dimension getPreferredSize() { return new Dimension( preferredWidth, preferredHeight ); }
    @Override public void setBounds(int x, int y, int width, int height) { super.setBounds( x, y, width, height ); this.width = width; this.height = height; sizeChanged( width, height ); }
    @Override public void setBounds(Rectangle r)         { super.setBounds( r );           this.width = r.width; this.height = r.height; sizeChanged( width, height ); }
    @Override public void setSize(Dimension d)           { super.setSize( d );             this.width = d.width; this.height = d.height; sizeChanged( width, height ); }
    @Override public void setSize(int width, int height) { super.setSize( width, height ); this.width =   width; this.height =   height; sizeChanged( width, height ); }
}
