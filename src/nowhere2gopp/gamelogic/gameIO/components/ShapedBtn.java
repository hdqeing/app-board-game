package nowhere2gopp.gamelogic.gameIO.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.JButton;

/**
 * This class provides an extended JButton thats graphically represented by a
 * drawn polygon and contains an Element of the type of your choice.
 *
 * @author Marvin Sommer
 * @version 0.1
 */

public class ShapedBtn<E> extends JButton {
    /**
     * Contains the polygon to be drawn as the button.
     */
    private Shape poly;

    /**
     * Contains the color the polygon is to be drawn in.
     */
    private Color color;

    /**
     * Contains the width of our Button.
     */
    private int width;

    /**
     * Contains the height of our Button.
     */
    private int height;

    /**
     * Contains a String to be writton onto the Button for debugging purposes.
     */
    private String txt = "";

    /**
     * Contains the element the Button represents.
     */
    private E element;

    /**
     * Sets the Text to be shown on the Button.
     *
     * @param text Text to be shown.
     */
    public void setText(String text) {
        this.txt = text;
        repaint();
    }

    /**
     * Returns the Text thats written on the Button.
     *
     * @return Text thats written on the Button
     */
    public String getText() {
        return txt;
    }

    /**
     * Calls the overloaded {@link #setShape(Shape poly,int width,int height)
     * setShape} with the poly parameter and diameter int as width and height.
     *
     * @param poly     Shape to be drawn.
     * @param diameter x and y Dimension
     */
    public void setShape(Shape poly, int diameter) {
        setShape(poly, diameter, diameter);
    }

    /**
     * Sets the shape to be drawn with different x and y Dimensions and initiates a
     * repaint of the Button.
     *
     * @param poly   shape to be drawn
     * @param width  width of the Button
     * @param height height of the Button
     */
    public void setShape(Shape poly, int width, int height) {
        this.poly = poly;
        this.width = width;
        this.height = height;
        repaint();
    }

    /**
     * Constructor that calls an overloaded {@link #ShapedBtn(int size) Constructor}
     * with size 100.
     */
    public ShapedBtn() {
        this(100);
    }

    /**
     * Constructor that takes an int parameter to call an overloaded
     * {@link #ShapedBtn(int width,int height) Constructor} with size as width and
     * height.
     *
     * @param size x and y dimension of shape
     */
    public ShapedBtn(int size) {
        this(size, size);
    }

    /**
     * Overloaded constructor which takes a width and height as parameters. Calls an
     * overloaded {@link #ShapedBtn(Shape,int,int,Color) Constructor}
     * with a default Circular shape, the parameters width and height and the Color
     * dark gray as default color.
     *
     * @param width  width to use
     * @param height height to use
     */
    public ShapedBtn(int width, int height) {
        this(new Ellipse2D.Float(0, 0, 100, 100), width, height, Color.DARK_GRAY);
    }

    /**
     * Overloaded constructor which takes a width, height and a shape as parameters.
     * Calls an overloaded {@link #ShapedBtn(Shape,int,Color)
     * Constructor} with size as width and height and the Color dark gray as default
     * color.
     *
     * @param poly shape to draw
     * @param size x and y dimension of shape
     */
    public ShapedBtn(Shape poly, int size) {
        this(poly, size, Color.DARK_GRAY);
    }

    /**
     * Overloaded constructor which takes a width, height and a shape as parameters.
     * Calls an overloaded
     * {@link #ShapedBtn(Shape poly,int width,int height,Color col) Constructor}
     * with size as width and height.
     *
     * @param poly shape to draw
     * @param size x and y dimension of shape
     * @param col  color to draw the shape in
     */
    public ShapedBtn(Shape poly, int size, Color col) {
        this(poly, size, size, col);
    }

    /**
     * Constructor that sets the shape, width, height and color of this Button.
     *
     * @param poly   shape to draw
     * @param width  x and y dimension of shape
     * @param height x and y dimension of shape
     * @param col    color to draw the shape in
     */
    public ShapedBtn(Shape poly, int width, int height, Color col) {
        this.poly = poly;
        this.width = width;
        this.height = height;
        setColor(col);
        setContentAreaFilled(false);
    }

    /**
     * Overrides the getPreferredSize function
     *
     * @return returns width and height as Dimension instance
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.width, this.height);
    }

    /**
     * Sets the element thats to be represented by this Button
     *
     * @param o element to be represented
     */
    public void setElement(E o) {
        this.element = o;
    }

    /**
     * returns the element thats to be represented by this Button
     *
     * @return returns element thats to be represented
     */
    public E getElement() {
        return this.element;
    }

    /**
     * Sets color to draw the shape in and initiates a repaint.
     *
     * @param color color to draw the shape in.
     */
    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    /**
     * Gets the current color of our Button
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Gets the String representation of the element thats to be represented by this
     * Button
     *
     * @return String representation of the element thats to be represented
     */
    public String getElementString() {
        String result = element.toString();

        return result;
    }

    /**
     * Overrides the paintComponent function to draw the polygon in the desired
     * color.
     *
     * @param g Graphics instance is filled in when a repaint is called.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (poly instanceof Polygon) { // If we draw a Polygon we draw the edges in Black (Links are passed as Polygons, Sites arent)
            float thickness = 2;
            Stroke oldStroke = g2d.getStroke();
            g2d.fillPolygon((Polygon) poly);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon((Polygon) poly);
            g2d.setStroke(oldStroke);
        } else // If its not a Polygon we dont draw edges but only fill the shape
            g2d.fill(poly);

        if (!(this.txt == "")) { // If txt is not empty write it onto the Button
            g2d.setColor(Color.YELLOW);
            g2d.drawString(txt, width / 8, width / 2);
        }
        g2d.dispose();
    }

    /**
     * Overrides the contains function thats called by the actionlistener, makes clicks
     * only register when drawn polygon is clicked
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return returns if click was in polygon
     */
    @Override
    public boolean contains(int x, int y) {
        return poly.contains(x, y);
    }
}
