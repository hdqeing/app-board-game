package nowhere2gopp.preset;

import java.io.*;
import java.util.*;

/**
 * Function {@link #equals(Object)} and {@link #hashCode()} ignore site order (set)
 */
public class SiteSet extends SitePair implements Comparable<SiteSet>{
    // ------------------------------------------------------
    public SiteSet(final Site first, final Site second) {
        super(first, second);
    }

    public SiteSet(final SitePair site) {
        super(site);
    }

    // ------------------------------------------------------

    /** ignore order */
    public int hashCode() {
        Site[] s = toSortedArray();

        return s[0].hashCode() * SitePair.BASE + s[1].hashCode();
    }

    /** ignore order */
    public boolean equals(final Object o) {
        if (o == null) return false;

        if (!(o instanceof SiteSet)) return false;

        Site[] s = toSortedArray();
        Site[] t = ((SiteSet)o).toSortedArray();
        return s[0].equals(t[0]) && s[1].equals(t[1]);
    }

    public String toString() {
        return "{" + getFirst().toString() + "," + getSecond().toString() + "}";
    }

    // ------------------------------------------------------
    public int compareTo(final SiteSet s) {
        return compareByHash(s);
    }

    // ------------------------------------------------------
    protected Site[] toSortedArray() {
        Site[] s = new Site[2];

        if (getFirst().compareTo(getSecond()) < 0) {
            s[0] = getFirst();
            s[1] = getSecond();
        }
        else {
            s[0] = getSecond();
            s[1] = getFirst();
        }
        return s;
    }

    // static ==============================================

    // ------------------------------------------------------
    public static SiteSet parse(final String string) {
        Site s[] = SitePair.parse(string, '{', '}');

        return new SiteSet(s[0], s[1]);
    }

    // ------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
