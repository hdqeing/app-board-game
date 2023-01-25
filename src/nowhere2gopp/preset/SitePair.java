package nowhere2gopp.preset;

import java.io.*;
import java.util.*;

abstract public class SitePair implements Serializable {
    /** max number of sites, base for hash code */
    public static final int BASE = Site.NEXT_HASH;

    /** max hash code plus 1 */
    public static final int NEXT_HASH = BASE * BASE;

    // ------------------------------------------------------
    public SitePair(final Site first, final Site second) {
        site = new Site[] { first, second };
    }

    public SitePair(final SitePair site) {
        this(site.getFirst(), site.getSecond());
    }

    // ------------------------------------------------------
    public Site getFirst() {
        return site[0];
    }

    public Site getSecond() {
        return site[1];
    }

    public Site[] toArray() {
        return new Site[] { getFirst(), getSecond() };
    }

    // ------------------------------------------------------
    abstract public boolean equals(final Object o);

    abstract public int     hashCode();

    public String           toString() {
        return "[" + getFirst().toString() + "," + getSecond().toString() + "]";
    }

    // ------------------------------------------------------
    protected int compareByHash(final SitePair s) {
        if (s == null) return -1;

        if (equals(s) || (hashCode() == s.hashCode())) return 0;

        if (hashCode() < s.hashCode()) return -1;

        return 1;
    }

    // ------------------------------------------------------
    private Site[] site;

    // static ==============================================

    // ------------------------------------------------------
    protected static Site[] parse(final String string, final char open, final char close) {
        if (string == null) throw new SiteFormatException("cannot parse empty string!");

        String msg = "cannot parse string \""
                     + string
                     + "\"! correct format is: "
                     + open + "(C0,R0),(C1,R1)" + close;

        String str = string.trim();

        if (str.equals("")) throw new SitePairFormatException("cannot parse empty string");

        if (!str.startsWith("" + open) || !str.endsWith("" + close)) throw new SitePairFormatException(msg);

        // the splitting needs to be merged afterwards
        String[] parts = str.substring(1, str.length() - 1).split(",");

        if (parts.length != 4) throw new SitePairFormatException(msg);

        try {
            // Merge splitted substrings accordingly
            return new Site[] { Site.parse(parts[0] + ',' + parts[1]),
                                Site.parse(parts[2] + ',' + parts[3]) };
        } catch (SiteFormatException e) {
            throw new SitePairFormatException("wrong site format! ", e);
        }
    }

    // ------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
