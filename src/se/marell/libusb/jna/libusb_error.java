/*
 * Created by Daniel Marell 2011-08-28 10:18
 */
package se.marell.libusb.jna;

public class libusb_error {
    /**
     * Success (no error)
     */
    public static final int SUCCESS = 0;

    /**
     * Input/output error
     */
    public static final int ERROR_IO = -1;

    /**
     * Invalid parameter
     */
    public static final int ERROR_INVALID_PARAM = -2;

    /**
     * Access denied (insufficient permissions)
     */
    public static final int ERROR_ACCESS = -3;

    /**
     * No such device (it may have been disconnected)
     */
    public static final int ERROR_NO_DEVICE = -4;

    /**
     * Entity not found
     */
    public static final int ERROR_NOT_FOUND = -5;

    /**
     * Resource busy
     */
    public static final int ERROR_BUSY = -6;

    /**
     * Operation timed out
     */
    public static final int ERROR_TIMEOUT = -7;

    /**
     * Overflow
     */
    public static final int ERROR_OVERFLOW = -8;

    /**
     * Pipe error
     */
    public static final int ERROR_PIPE = -9;

    /**
     * System call interrupted (perhaps due to signal)
     */
    public static final int ERROR_INTERRUPTED = -10;

    /**
     * Insufficient memory
     */
    public static final int ERROR_NO_MEM = -11;

    /**
     * Operation not supported or unimplemented on this platform
     */
    public static final int ERROR_NOT_SUPPORTED = -12;

    /**
     * Other error
     */
    public static final int ERROR_OTHER = -99;

    /**
     * Get text for error code.
     *
     * @param c Error code
     * @return Text string
     */
    public static String getText(int c) {
        switch (c) {
            case SUCCESS:
                return "SUCCESS";
            case ERROR_IO:
                return "ERROR_IO";
            case ERROR_INVALID_PARAM:
                return "ERROR_INVALID_PARAM";
            case ERROR_ACCESS:
                return "ERROR_ACCESS";
            case ERROR_NO_DEVICE:
                return "ERROR_NO_DEVICE";
            case ERROR_NOT_FOUND:
                return "ERROR_NOT_FOUND";
            case ERROR_BUSY:
                return "ERROR_BUSY";
            case ERROR_TIMEOUT:
                return "ERROR_TIMEOUT";
            case ERROR_OVERFLOW:
                return "ERROR_OVERFLOW";
            case ERROR_PIPE:
                return "ERROR_PIPE";
            case ERROR_INTERRUPTED:
                return "ERROR_INTERRUPTED";
            case ERROR_NO_MEM:
                return "ERROR_NO_MEM";
            case ERROR_NOT_SUPPORTED:
                return "ERROR_NOT_SUPPORTED";
            case ERROR_OTHER:
                return "ERROR_OTHER";
            default:
                return "?(" + c + ")";
        }
    }
}