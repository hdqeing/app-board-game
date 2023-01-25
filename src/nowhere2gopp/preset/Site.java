package nowhere2gopp.preset;

import java.io.Serializable;

public class Site implements Serializable, Comparable<Site>{
    /** max number of columns/rows, base for hash code */
    public static final int BASE = 11;

    /** max hash code plus 1 */
    public static final int NEXT_HASH = BASE * BASE;

    // -----------------------------------------------------
    private int column;
    private int row;

    // -----------------------------------------------------
    public Site(final int column, final int row) {
        if ((column < 0) || (column >= BASE) || (row < 0) || (row >= BASE)) throw new SiteFormatException("format: 0 <= column/row <= " + (BASE - 1));

        this.column = column;
        this.row    = row;
    }

    // ------------------------------------------------------
    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int[] toArray() {
        return new int[] { column, row };
    }

    // -----------------------------------------------------
    public int hashCode() {
        return column * BASE + row;
    }

    public boolean equals(final Object o) {
        if (o == null) return false;

        if (!(o instanceof Site)) return false;

        Site s = (Site)o;
        return column == s.getColumn() && row == s.getRow();
    }

    public String toString() {
        return "(" + column + "," + row + ")";
    }

    // ------------------------------------------------------
    public int compareTo(final Site s) {
        // correct if hash code is unique only
        if (s == null) return -1;

        if (equals(s) || (hashCode() == s.hashCode())) return 0;

        if (hashCode() < s.hashCode()) return -1;

        return 1;
    }

    // static ==============================================

    // -----------------------------------------------------
    public static Site parse(final String string) {
        if (string == null) throw new SiteFormatException("cannot parse empty string!");

        String msg = "cannot parse string \""
                     + string
                     + "\"! correct format is: (COLUMN,ROW)";

        String str = string.trim();

        if (str.equals("")) throw new SiteFormatException("cannot parse empty string!");

        if (!str.startsWith("(") || !str.endsWith(")")) throw new SiteFormatException(msg);

        String[] parts = str.substring(1, str.length() - 1).split(",");

        if (parts.length != 2) throw new SiteFormatException(msg);

        try {
            return new Site(Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException e) {
            throw new SiteFormatException("wrong number format! ", e);
        }
    }

    // ------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
