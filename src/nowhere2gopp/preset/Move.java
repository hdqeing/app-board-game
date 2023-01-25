package nowhere2gopp.preset;

import java.io.Serializable;

public class Move implements Serializable {
    /** max number of site pairs, base for hash code */
    public static final int BASE = SitePair.NEXT_HASH;

    /** max hash code plus 1 */
    public static final int NEXT_HASH = BASE * BASE;

    // ------------------------------------------------------
    public Move(final MoveType type) {
        if ((type != MoveType.Surrender) && (type != MoveType.End)) throw new IllegalArgumentException("constructors for surrender/end moves only");

        this.type = type;
    }

    public Move(final SiteTuple agent, final SiteSet link) {
        type = MoveType.AgentLink;

        this.agent = agent;
        links      = new SiteSet[] { link };
    }

    public Move(final SiteSet one, final SiteSet other) {
        type = MoveType.LinkLink;

        links = new SiteSet[] { one, other };
    }

    // ------------------------------------------------------
    public MoveType getType() {
        return type;
    }

    public SiteTuple getAgent() {
        if (type != MoveType.AgentLink) throw new IllegalStateException("cannot be called on type " + type);
        return agent;
    }

    public SiteSet getLink() {
        if (type != MoveType.AgentLink) throw new IllegalStateException("cannot be called on type " + type);
        return links[0];
    }

    public SiteSet getOneLink() {
        if (type != MoveType.LinkLink) throw new IllegalStateException("cannot be called on type " + type);
        return links[0];
    }

    public SiteSet getOtherLink() {
        if (type != MoveType.LinkLink) throw new IllegalStateException("cannot be called on type " + type);
        return links[1];
    }

    public SitePair[] toArray() {
        switch (type) {
        case AgentLink:
            return new SitePair[] { agent, links[0] };

        case LinkLink:
            return new SitePair[] { links[0], links[1] };
        }
        throw new IllegalStateException("cannot be called on type " + type);
    }

    // ------------------------------------------------------
    public boolean equals(Object o) {
        if (o == null) return false;

        if (!(o instanceof Move)) return false;

        Move m = (Move)o;

        if (type != m.type) return false;

        switch (type) {
        case AgentLink:
            return agent.equals(m.getAgent()) && links[0].equals(m.getLink());

        case LinkLink:

            // ignore order
            boolean same = links[0].equals(m.getOneLink()) && links[1].equals(m.getOtherLink());
            boolean swap = links[0].equals(m.getOtherLink()) && links[1].equals(m.getOneLink());
            return same || swap;
        }

        // type is already the same
        return true;
    }

    public int hashCode() {
        if (type == MoveType.Surrender) return -2;

        if (type == MoveType.End) return -1;

        switch (type) {
        case Surrender:
            return -2;

        case End:
            return -1;

        case AgentLink:
            return getAgent().hashCode() * BASE + getLink().hashCode();

        case LinkLink:

            // ignore order
            SiteSet[] ss = toSortedLinkArray();
            return ss[0].hashCode() * BASE + ss[1].hashCode();

        default:
            throw new IllegalStateException("unknown type " + type);
        }

        // unreachable
    }

    public String toString() {
        SitePair[] sp = toArray();
        return sp[0] + "+" + sp[1];
    }

    // ------------------------------------------------------
    protected SiteSet[] toSortedLinkArray() {
        if (type != MoveType.LinkLink) throw new IllegalStateException("cannot be called on type " + type);

        SiteSet[] ss = new SiteSet[2];

        if (getOneLink().compareTo(getOtherLink()) < 0) {
            ss[0] = getOneLink();
            ss[1] = getOtherLink();
        } else {
            ss[0] = getOtherLink();
            ss[1] = getOneLink();
        }
        return ss;
    }

    // ------------------------------------------------------
    private MoveType type;
    private SiteTuple agent;
    private SiteSet[] links;

    // static ==============================================

    // ------------------------------------------------------
    public static Move parse(final String string) {
        if (string == null) throw new SiteFormatException("cannot parse empty string!");

        String msg = "cannot parse string \"" + string + "\"! correct formats are:\n"
                     + "agent+link> ((C0,R0),(C1,R1))+{(C2,R2),(C3,R3)}\n"
                     + "link+link> {(C0,R0),(C1,R1)}+{(C2,R2),(C3,R3)}";

        String str = string.trim().toLowerCase();

        if (str.equals("")) throw new MoveFormatException("cannot parse empty string");

        if (str.startsWith("end")) return new Move(MoveType.End);

        if (str.startsWith("surrender")) return new Move(MoveType.Surrender);

        MoveType t;

        if (str.startsWith("(")) t = MoveType.AgentLink;
        else if (str.startsWith("{")) t = MoveType.LinkLink;
        else throw new SitePairFormatException(msg);

        String[] parts = str.substring(0, str.length()).split("\\+");

        if (parts.length != 2) throw new SitePairFormatException(msg);

        try {
            if (t == MoveType.AgentLink) {
                return new Move(SiteTuple.parse(parts[0]), SiteSet.parse(parts[1]));
            } else {
                // MoveType.LinkLink
                return new Move(SiteSet.parse(parts[0]), SiteSet.parse(parts[1]));
            }
        } catch (SitePairFormatException e) {
            throw new MoveFormatException("wrong site pair format! ", e);
        }

        // unreachable
    }

    // ------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
