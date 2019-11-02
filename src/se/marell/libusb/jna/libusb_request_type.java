/*
 * Created by Daniel Marell 2011-08-28 16:16
 */
package se.marell.libusb.jna;

public class libusb_request_type {
    /**
     * Standard
     */
    public static final int REQUEST_TYPE_STANDARD = 0x00 << 5;

    /**
     * Class
     */
    public static final int REQUEST_TYPE_CLASS = 0x01 << 5;

    /**
     * Vendor
     */
    public static final int REQUEST_TYPE_VENDOR = 0x02 << 5;

    /**
     * Reserved
     */
    public static final int REQUEST_TYPE_RESERVED = 0x03 << 5;
}
