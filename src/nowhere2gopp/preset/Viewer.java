package nowhere2gopp.preset;

import java.util.Collection;

public interface Viewer {
    PlayerColor        getTurn();
    int                getSize();
    Status             getStatus();

    /** site blocked by the agent of given color */
    Site               getAgent(final PlayerColor color);

    /** unbroken links */
    Collection<SiteSet>getLinks();
}
