package nowhere2gopp.preset;

public interface Playable extends Viewable {
    /** make a move */
    void make(final Move move) throws IllegalStateException;
}
