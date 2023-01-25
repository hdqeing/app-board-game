package nowhere2gopp.preset;

public class SitePairFormatException extends IllegalArgumentException {
    public SitePairFormatException(String msg) {
        super(msg);
    }

    public SitePairFormatException(String msg, Throwable e) {
        super(msg, e);
    }

    // ------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
