/*
 * Created by Daniel Marell 2011-08-28 16:12
 */
package se.marell.libusb.jna;

public class libusb_request_recipient {
  /**
   * Device.
   */
  public static final int RECIPIENT_DEVICE = 0;

  /**
   * Interface.
   */
  public static final int RECIPIENT_INTERFACE = 1;

  /**
   * Endpoint.
   */
  public static final int RECIPIENT_ENDPOINT = 2;

  /**
   * Other.
   */
  public static final int RECIPIENT_OTHER = 3;
}
