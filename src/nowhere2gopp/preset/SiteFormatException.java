package nowhere2gopp.preset;

public class SiteFormatException extends IllegalArgumentException {
    public SiteFormatException(String msg) {
        super(msg);
    }

    public SiteFormatException(String msg, Throwable e) {
        super(msg, e);
    }

    // ------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
