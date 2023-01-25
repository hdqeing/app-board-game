package nowhere2gopp.preset;

import java.io.*;
import java.util.*;

/**
 * Function {@link #equals(Object)} and {@link #hashCode()} respect site order (tuple)
 */
public class SiteTuple extends SitePair implements Comparable<SiteTuple>{
    // ------------------------------------------------------
    public SiteTuple(final Site first, final Site second) {
        super(first, second);
    }

    public SiteTuple(final SitePair site) {
        super(site);
    }

    // ------------------------------------------------------
    public static SiteTuple parse(final String string) {
        Site s[] = SitePair.parse(string, '(', ')');

        return new SiteTuple(s[0], s[1]);
    }

    // ------------------------------------------------------

    /** respect order */
    public int hashCode() {
        return getFirst().hashCode() * SitePair.BASE + getSecond().hashCode();
    }

    /** respect order */
    public boolean equals(final Object o) {
        if (o == null) return false;

        if (!(o instanceof SiteTuple)) return false;

        SiteTuple t = (SiteTuple)o;
        return getFirst().equals(t.getFirst()) && getSecond().equals(t.getSecond());
    }

    public String toString() {
        return "(" + getFirst().toString() + "," + getSecond().toString() + ")";
    }

    // ------------------------------------------------------
    public int compareTo(final SiteTuple s) {
        return compareByHash(s);
    }

    // ------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
