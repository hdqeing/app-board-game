package nowhere2gopp.gamelogic;
import nowhere2gopp.preset.*;
import nowhere2gopp.gamelogic.*;
import java.util.*;
import java.io.*;

/**
 * This is a Wrapper class for the Site class from the package nowhere2gopp.preset.
 * It is utilized in the GameBoard to make it easier to find paths between Sites
 * and to keep track of neighbouring sites.
 * @author Benedikt W. Berg
 */
public class Node implements Serializable {
    /**
     * Site variable of the Wrapper
     */
    private Site site;

    /**
     * SiteColor Enum of the Wrapper
     */
    private SiteColor color;

    /**
     * Neighbour list of the Wrapper
     */
    private LinkedList<Node> neighbors;

    /**
     * Serialize ID of the Wrapper
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construktor that initializes the Site, SiteColor and the Neighbour List
     * @param s Site that is to be stored in this wrapper
     */
    public Node(Site s) {
        site = s;
        color = SiteColor.WHITE;
        neighbors = new LinkedList<>();
    }

    /**
     * Returns the stored Site of the Wrapper
     * @return Stored Site
     */
    public Site getSite() {
        return site;
    }

    /**
     * This methode changes the SiteColor of the Wrapper to White,
     * when this Node is not representing an agent.
     * e.g. SiteColor is not Red or Blue
     */
    public void paintWhite() {
        if ((color != SiteColor.RED) && (color != SiteColor.BLUE))
          color = SiteColor.WHITE;
    }

    /**
     * This methode changes the SiteColor of the Wrapper to Black,
     * when this Node is not representing an agent.
     * e.g. SiteColor is not Red or Blue
     */
    public void paintBlack() {
        if ((color != SiteColor.RED) && (color != SiteColor.BLUE))
          color = SiteColor.BLACK;
    }

    /**
     * This methode changes the SiteColor of the Wrapper to Red,
     * when this Node is not already representing an agent.
     * e.g. If this Node would be the blue agent then it can not be painted Red
     */
    public void paintRed() {
        if (color != SiteColor.BLUE)
          color = SiteColor.RED;
        else
          throw new IllegalStateException("Cannot paint " + SiteColor.RED + " because Node is " + color);
    }

    /**
     * This methode changes the SiteColor of the Wrapper to Blue,
     * when this Node is not already representing an agent.
     * e.g. If this Node would be the Red agent then it can not be painted Blue
     */
    public void paintBlue() {
        if (color != SiteColor.RED)
          color = SiteColor.BLUE;
        else
          throw new IllegalStateException("Cannot paint " + SiteColor.BLUE + " because Node is " + color);
    }

    /**
     * This methode changes the SiteColor of the Wrapper to White,
     * regardless which SiteColor it was previously.
     */
    public void turnWhite() {
        color = SiteColor.WHITE;
    }

    /**
     * Returns the stored SiteColor of the Wrapper
     * @return Stored SiteCOlor
     */
    public SiteColor getColor() {
        return color;
    }

    /**
     * Adds the passed Node to {@link #neighbors neighbors}, if it is not already in the list.
     * @param node of the type Node which should be added
     */
    public void addNeighbor(Node node) {
        if (!neighbors.contains(node))
          neighbors.add(node);
    }

    /**
     * Removes the passed Node from {@link #neighbors neighbors}, if it is in the list.
     * @param node of the type Node which should be removed
     */
    public void removeNeighbor(Node node) {
        if (neighbors.contains(node))
          neighbors.remove(node);
    }

    /**
     * Returns the Neighbour list with the Specified SiteColor.
     * It will always return a empty list if do not choose to pass
     * SiteColor.WHITE or SiteColor.NONE to this method.,
     * @param color of the Sites which should be returned
     * @return list of specified neighbours
     */
    public LinkedList<Node>getNeighbors(SiteColor color) {
        LinkedList<Node> list = new LinkedList<>();
        if (color == SiteColor.NONE)
          return neighbors;
        else {
            for (Node node : neighbors) {
                if (node.getColor() == color)
                  list.add(node);
            }
        }
        return list;
    }

    /**
     * This methode compares the calling Node object and the passed Object.
     * @param o Object which should be compared with the calling Node Object
     * @return If the passed Object is not an instance of Node than return false, else return (Sites equals)
     */
    public boolean equals(final Object o) {
        if (o == null)
          return false;
        if (!(o instanceof Node))
          return false;
        Node node = (Node)o;
        return site.equals(node.getSite());
    }

    /**
     * hashCode method of this Wrapper
     * @return returns the hashcode of the Site
     */
    public int hashCode() {
        return site.hashCode();
    }

    /**
     * hashCode method of this Wrapper
     * @return returns the toString of the Site
     */
    public String toString() {
        return site.toString();
    }
}
