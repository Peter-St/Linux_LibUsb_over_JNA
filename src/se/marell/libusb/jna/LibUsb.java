/* 
* File changed by Peter Stoiber.
 */

 /*
 * Created by Daniel Marell 2011-08-28 10:19
 */
 /*
 * Copyright an Changed 2019 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.marell.libusb.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface LibUsb extends Library {

    public static final LibUsb INSTANCE = Native.load("usb-1.0", LibUsb.class);

    public enum libusb_transfer_type {
        LIBUSB_TRANSFER_TYPE_CONTROL((byte) 0),
        LIBUSB_TRANSFER_TYPE_ISOCHRONOUS((byte) 1),
        LIBUSB_TRANSFER_TYPE_BULK((byte) 2),
        LIBUSB_TRANSFER_TYPE_INTERRUPT((byte) 3),
        LIBUSB_TRANSFER_TYPE_BULK_STREAM((byte) 4);

        private final byte value;

        private libusb_transfer_type(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static libusb_transfer_type fromNative(byte value) {
            for (libusb_transfer_type val : libusb_transfer_type.values()) {
                if (val.getValue() == value) {
                    return val;
                }
            }
            return null;
        }
    }

    // LibUsb Error Codes:
    public enum libusb_error {
        LIBUSB_SUCCESS((byte) 0),
        LIBUSB_ERROR_IO((byte) -1),
        LIBUSB_ERROR_INVALID_PARAM((byte) -2),
        LIBUSB_ERROR_ACCESS((byte) -3),
        LIBUSB_ERROR_NO_DEVICE((byte) -4),
        LIBUSB_ERROR_NOT_FOUND((byte) -5),
        LIBUSB_ERROR_BUSY((byte) -6),
        LIBUSB_ERROR_TIMEOUT((byte) -7),
        LIBUSB_ERROR_OVERFLOW((byte) -8),
        LIBUSB_ERROR_PIPE((byte) -9),
        LIBUSB_ERROR_INTERRUPTED((byte) -10),
        LIBUSB_ERROR_NO_MEM((byte) -11),
        LIBUSB_ERROR_NOT_SUPPORTED((byte) -12),
        LIBUSB_ERROR_OTHER((byte) -99);

        private final byte value;

        private libusb_error(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static libusb_error fromNative(byte value) {
            for (libusb_error val : libusb_error.values()) {
                if (val.getValue() == value) {
                    return val;
                }
            }
            return null;
        }

    };

    public enum libusb_transfer_flags {
        LIBUSB_TRANSFER_SHORT_NOT_OK((byte) (1 << 0)),
        LIBUSB_TRANSFER_FREE_BUFFER((byte) (1 << 1)),
        LIBUSB_TRANSFER_FREE_TRANSFER((byte) (1 << 2)),
        LIBUSB_TRANSFER_ADD_ZERO_PACKET((byte) (1 << 3));

        private final byte value;

        private libusb_transfer_flags(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static libusb_transfer_flags fromNative(byte value) {
            for (libusb_transfer_flags val : libusb_transfer_flags.values()) {
                if (val.getValue() == value) {
                    return val;
                }
            }
            return null;
        }

    };

    /*
   Library initialization/deinitialization
     */
    /**
     * Set message verbosity.
     *
     * Level 0: no messages ever printed by the library (default) Level 1: error
     * messages are printed to stderr Level 2: warning and error messages are
     * printed to stderr Level 3: informational messages are printed to stdout,
     * warning and error messages are printed to stderr The default level is 0,
     * which means no messages are ever printed. If you choose to increase the
     * message verbosity level, ensure that your application does not close the
     * stdout/stderr file descriptors.
     *
     * You are advised to set level 3. libusb is conservative with its message
     * logging and most of the time, will only log messages that explain error
     * conditions and other oddities. This will help you debug your software.
     *
     * If the LIBUSB_DEBUG environment variable was set when libusb was
     * initialized, this function does nothing: the message verbosity is fixed
     * to the value in the environment variable.
     *
     * If libusb was compiled without any message logging, this function does
     * nothing: you'll never get any messages.
     *
     * If libusb was compiled with verbose debug message logging, this function
     * does nothing: you'll always get messages from all levels.
     *
     * @param context the context to operate on, or null for the default context
     * @param level debug level to set
     */
    void libusb_set_debug(Pointer context, int level);

    /**
     * Initialize libusb.
     *
     * This function must be called before calling any other libusb function.
     *
     * @param context Optional output location for context pointer. Only valid
     * on return code 0.
     * @return 0 on success, or a LIBUSB_ERROR code on failure
     */
    int libusb_init(Pointer[] context);

    /**
     * Deinitialize libusb.
     *
     * Should be called after closing all open devices and before your
     * application terminates.
     *
     * @param context the context to deinitialize, or null for the default
     * context
     */
    void libusb_exit(Pointer context);

    /*
   Device handling and enumeration
     */
    /**
     * Returns a list of USB devices currently attached to the system.
     *
     * This is your entry point into finding a USB device to operate.
     *
     * You are expected to unreference all the devices when you are done with
     * them, and then free the list with libusb_free_device_list(). Note that
     * libusb_free_device_list() can unref all the devices for you. Be careful
     * not to unreference a device you are about to open until after you have
     * opened it.
     *
     * This return value of this function indicates the number of devices in the
     * resultant list. The list is actually one element larger, as it is
     * NULL-terminated.
     *
     * @param context the context to operate on, or null for the default context
     * @param list output location for a list of devices. Must be later freed
     * with libusb_free_device_list().
     * @return the number of devices in the outputted list, or
     * LIBUSB_ERROR_NO_MEM on memory allocation failure.
     */
    int libusb_get_device_list(Pointer context, Pointer[] list);

    /**
     * Frees a list of devices previously discovered using
     * libusb_get_device_list().
     *
     * If the unref_devices parameter is set, the reference count of each device
     * in the list is decremented by 1.
     *
     * @param context the context to operate on, or null for the default context
     * @param list the list to free
     * @param unref_devices whether to unref the devices in the list
     */
    void libusb_free_device_list(Pointer context, Pointer list, int unref_devices);

    /**
     * Get the number of the bus that a device is connected to.
     *
     * @param usb_device a device
     * @return the bus number
     */
    int libusb_get_bus_number(Pointer usb_device);

    /**
     * Get the address of the device on the bus it is connected to.
     *
     * @param usb_device a device
     * @return the device address
     */
    int libusb_get_device_address(Pointer usb_device);

    /**
     * Convenience function to retrieve the wMaxPacketSize value for a
     * particular endpoint in the active device configuration.
     *
     * This function was originally intended to be of assistance when setting up
     * isochronous transfers, but a design mistake resulted in this function
     * instead. It simply returns the wMaxPacketSize value without considering
     * its contents. If you're dealing with isochronous transfers, you probably
     * want libusb_get_max_iso_packet_size() instead.
     *
     * @param usb_device a device
     * @param endpoint address of the endpoint in question
     * @return the wMaxPacketSize value LIBUSB_ERROR_NOT_FOUND if the endpoint
     * does not exist LIBUSB_ERROR_OTHER on other failure
     */
    int libusb_get_max_packet_size(Pointer usb_device, int endpoint);

    /**
     * Calculate the maximum packet size which a specific endpoint is capable is
     * sending or receiving in the duration of 1 microframe.
     *
     * Only the active configution is examined. The calculation is based on the
     * wMaxPacketSize field in the endpoint descriptor as described in section
     * 9.6.6 in the USB 2.0 specifications.
     *
     * If acting on an isochronous or interrupt endpoint, this function will
     * multiply the value found in bits 0:10 by the number of transactions per
     * microframe (determined by bits 11:12). Otherwise, this function just
     * returns the numeric value found in bits 0:10.
     *
     * This function is useful for setting up isochronous transfers, for example
     * you might pass the return value from this function to
     * libusb_set_iso_packet_lengths() in order to set the length field of every
     * isochronous packet in a transfer.
     *
     * Since v1.0.3.
     *
     * @param usb_device a device
     * @param endpoint address of the endpoint in question
     * @return the maximum packet size which can be sent/received on this
     * endpoint LIBUSB_ERROR_NOT_FOUND if the endpoint does not exist
     * LIBUSB_ERROR_OTHER on other failure
     */
    int libusb_get_max_iso_packet_size(Pointer usb_device, int endpoint);

    /**
     * Increment the reference count of a device.
     *
     * @param usb_device the device to reference
     * @return the same device
     */
    Pointer libusb_ref_device(Pointer usb_device);

    /**
     * Decrement the reference count of a device. If the decrement operation
     * causes the reference count to reach zero, the device shall be destroyed.
     *
     * @param usb_device the device to unreference
     */
    void libusb_unref_device(Pointer usb_device);

    /**
     * Open a device and obtain a device handle.
     *
     * A handle allows you to perform I/O on the device in question.
     *
     * Internally, this function adds a reference to the device and makes it
     * available to you through libusb_get_device(). This reference is removed
     * during libusb_close().
     *
     * This is a non-blocking function; no requests are sent over the bus.
     *
     * @param usb_device the device to open
     * @param dev_handle output location for the returned device handle pointer.
     * Only populated when the return code is 0.
     * @return 0 on success LIBUSB_ERROR_NO_MEM on memory allocation failure
     * LIBUSB_ERROR_ACCESS if the user has insufficient permissions
     * LIBUSB_ERROR_NO_DEVICE if the device has been disconnected another
     * LIBUSB_ERROR code on other failure
     */
    int libusb_open(Pointer usb_device, Pointer[] dev_handle);

    /**
     * Convenience function for finding a device with a particular
     * idVendor/idProduct combination.
     *
     * This function is intended for those scenarios where you are using libusb
     * to knock up a quick test application - it allows you to avoid calling
     * libusb_get_device_list() and worrying about traversing/freeing the list.
     *
     * This function has limitations and is hence not intended for use in real
     * applications: if multiple 7 devices have the same IDs it will only give
     * you the first one, etc.
     *
     * @param context the context to operate on, or null for the default context
     * @param vendor_id the idVendor value to search for
     * @param product_id the idProduct value to search for
     * @return a handle for the first found device, or null on error or if the
     * device could not be found.
     */
    Pointer libusb_open_device_with_vid_pid(Pointer context, int vendor_id, int product_id);

    /**
     * Close a device handle.
     *
     * Should be called on all open handles before your application exits.
     *
     * Internally, this function destroys the reference that was added by
     * libusb_open() on the given device.
     *
     * This is a non-blocking function; no requests are sent over the bus.
     *
     * @param dev_handle the handle to close
     */
    void libusb_close(Pointer dev_handle);

    /**
     * Get the underlying device for a handle.
     *
     * This function does not modify the reference count of the returned device,
     * so do not feel compelled to unreference it when you are done.
     *
     * @param dev_handle a device handle
     * @return the underlying device
     */
    Pointer libusb_get_device(Pointer dev_handle);

    /**
     * Determine the bConfigurationValue of the currently active configuration.
     *
     * You could formulate your own control request to obtain this information,
     * but this function has the advantage that it may be able to retrieve the
     * information from operating system caches (no I/O involved).
     *
     * If the OS does not cache this information, then this function will block
     * while a control transfer is submitted to retrieve the information.
     *
     * This function will return a value of 0 in the config output parameter if
     * the device is in unconfigured state.
     *
     * @param dev_handle a device handle
     * @param config output location for the bConfigurationValue of the active
     * configuration (only valid for return code 0)
     * @return 0 on success LIBUSB_ERROR_NO_DEVICE if the device has been
     * disconnected another LIBUSB_ERROR code on other failure
     */
    int libusb_get_configuration(Pointer dev_handle, int[] config);

    /**
     * Set the active configuration for a device.
     *
     * The operating system may or may not have already set an active
     * configuration on the device. It is up to your application to ensure the
     * correct configuration is selected before you attempt to claim interfaces
     * and perform other operations.
     *
     * If you call this function on a device already configured with the
     * selected configuration, then this function will act as a lightweight
     * device reset: it will issue a SET_CONFIGURATION request using the current
     * configuration, causing most USB-related device state to be reset
     * (altsetting reset to zero, endpoint halts cleared, toggles reset).
     *
     * You cannot change/reset configuration if your application has claimed
     * interfaces - you should free them with libusb_release_interface() first.
     * You cannot change/reset configuration if other applications or drivers
     * have claimed interfaces.
     *
     * A configuration value of -1 will put the device in unconfigured state.
     * The USB specifications state that a configuration value of 0 does this,
     * however buggy devices exist which actually have a configuration 0.
     *
     * You should always use this function rather than formulating your own
     * SET_CONFIGURATION control request. This is because the underlying
     * operating system needs to know when such changes happen.
     *
     * This is a blocking function.
     *
     * @param dev_handle a device handle
     * @param configuration the bConfigurationValue of the configuration you
     * wish to activate, or -1 if you wish to put the device in unconfigured
     * state
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if the requested
     * configuration does not exist LIBUSB_ERROR_BUSY if interfaces are
     * currently claimed LIBUSB_ERROR_NO_DEVICE if the device has been
     * disconnected another LIBUSB_ERROR code on other failure
     */
    int libusb_set_configuration(Pointer dev_handle, int configuration);

    /**
     * Claim an interface on a given device handle.
     *
     * You must claim the interface you wish to use before you can perform I/O
     * on any of its endpoints.
     *
     * It is legal to attempt to claim an already-claimed interface, in which
     * case libusb just returns 0 without doing anything.
     *
     * Claiming of interfaces is a purely logical operation; it does not cause
     * any requests to be sent over the bus. Interface claiming is used to
     * instruct the underlying operating system that your application wishes to
     * take ownership of the interface.
     *
     * This is a non-blocking function.
     *
     * @param dev_handle a device handle
     * @param interface_number the bInterfaceNumber of the interface you wish to
     * claim
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if the requested interface
     * does not exist LIBUSB_ERROR_BUSY if another program or driver has claimed
     * the interface LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
     * a LIBUSB_ERROR code on other failure
     */
    int libusb_claim_interface(Pointer dev_handle, int interface_number);

    /**
     * Release an interface previously claimed with libusb_claim_interface().
     *
     * You should release all claimed interfaces before closing a device handle.
     *
     * This is a blocking function. A SET_INTERFACE control request will be sent
     * to the device, resetting interface state to the first alternate setting.
     *
     * @param dev_handle a device handle
     * @param interface_number the bInterfaceNumber of the previously-claimed
     * interface
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if the interface was not
     * claimed LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
     * another LIBUSB_ERROR code on other failure
     */
    int libusb_release_interface(Pointer dev_handle, int interface_number);

    /**
     * Activate an alternate setting for an interface.
     *
     * The interface must have been previously claimed with
     * libusb_claim_interface().
     *
     * You should always use this function rather than formulating your own
     * SET_INTERFACE control request. This is because the underlying operating
     * system needs to know when such changes happen.
     *
     * This is a blocking function.
     *
     * @param dev_handle a device handle
     * @param interface_number the bInterfaceNumber of the previously-claimed
     * interface
     * @param alternate_setting the bAlternateSetting of the alternate setting
     * to activate
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if the interface was not
     * claimed, or the requested alternate setting does not exist
     * LIBUSB_ERROR_NO_DEVICE if the device has been disconnected another
     * LIBUSB_ERROR code on other failure
     */
    int libusb_set_interface_alt_setting(Pointer dev_handle, int interface_number, int alternate_setting);

    /**
     * Clear the halt/stall condition for an endpoint.
     *
     * Endpoints with halt status are unable to receive or transmit data until
     * the halt condition is stalled.
     *
     * You should cancel all pending transfers before attempting to clear the
     * halt condition.
     *
     * This is a blocking function.
     *
     * @param dev_handle a device handle
     * @param endpoint the endpoint to clear halt status
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if the endpoint does not
     * exist LIBUSB_ERROR_NO_DEVICE if the device has been disconnected another
     * LIBUSB_ERROR code on other failure
     */
    int libusb_clear_halt(Pointer dev_handle, byte endpoint);

    /**
     * Perform a USB port reset to reinitialize a device.
     *
     * The system will attempt to restore the previous configuration and
     * alternate settings after the reset has completed.
     *
     * If the reset fails, the descriptors change, or the previous state cannot
     * be restored, the device will appear to be disconnected and reconnected.
     * This means that the device handle is no longer valid (you should close
     * it) and rediscover the device. A return code of LIBUSB_ERROR_NOT_FOUND
     * indicates when this is the case.
     *
     * This is a blocking function which usually incurs a noticeable delay.
     *
     * @param dev_handle a handle of the device to reset
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if re-enumeration is
     * required, or if the device has been disconnected another LIBUSB_ERROR
     * code on other failure
     */
    int libusb_reset_device(Pointer dev_handle);

    /**
     * Determine if a kernel driver is active on an interface.
     *
     * If a kernel driver is active, you cannot claim the interface, and libusb
     * will be unable to perform I/O.
     *
     * @param dev_handle a device handle
     * @param interface_number the interface to check
     * @return 0 if no kernel driver is active 1 if a kernel driver is active
     * LIBUSB_ERROR_NO_DEVICE if the device has been disconnected another
     * LIBUSB_ERROR code on other failure
     */
    int libusb_kernel_driver_active(Pointer dev_handle, int interface_number);

    /**
     * Detach a kernel driver from an interface.
     *
     * If successful, you will then be able to claim the interface and perform
     * I/O.
     *
     * @param dev_handle a device handle
     * @param interface_number the interface to detach the driver from
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if no kernel driver was
     * active LIBUSB_ERROR_INVALID_PARAM if the interface does not exist
     * LIBUSB_ERROR_NO_DEVICE if the device has been disconnected another
     * LIBUSB_ERROR code on other failure
     */
    int libusb_detach_kernel_driver(Pointer dev_handle, int interface_number);

    /**
     * Re-attach an interface's kernel driver, which was previously detached
     * using libusb_detach_kernel_driver().
     *
     * @param dev_handle a device handle
     * @param interface_number the interface to attach the driver from
     * @return 0 on success LIBUSB_ERROR_NOT_FOUND if no kernel driver was
     * active LIBUSB_ERROR_INVALID_PARAM if the interface does not exist
     * LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
     * LIBUSB_ERROR_BUSY if the driver cannot be attached because the interface
     * is claimed by a program or driver another LIBUSB_ERROR code on other
     * failure
     */
    int libusb_attach_kernel_driver(Pointer dev_handle, int interface_number);

    /**
     * Get the USB device descriptor for a given device.
     *
     * This is a non-blocking function; the device descriptor is cached in
     * memory.
     *
     * @param usb_device the device
     * @param desc output location for the descriptor data
     * @return 0 on success or a LIBUSB_ERROR code on failure
     */
    int libusb_get_device_descriptor(Pointer usb_device, libusb_device_descriptor[] desc);

    /**
     * Retrieve a string descriptor in C style ASCII.
     *
     * Wrapper around libusb_get_string_descriptor(). Uses the first language
     * supported by the device.
     *
     * @param dev_handle a device handle
     * @param desc_index the index of the descriptor to retrieve
     * @param data output buffer for ASCII string descriptor
     * @param length size of data buffer
     * @return number of bytes returned in data, or LIBUSB_ERROR code on failure
     */
    int libusb_get_string_descriptor_ascii(Pointer dev_handle, byte desc_index, byte[] data, int length);

    /*
  Polling and timing:
  Todo: Implement
  int 	libusb_try_lock_events (libusb_context *ctx)
  void 	libusb_lock_events (libusb_context *ctx)
  void 	libusb_unlock_events (libusb_context *ctx)
  int 	libusb_event_handling_ok (libusb_context *ctx)
  int 	libusb_event_handler_active (libusb_context *ctx)
  void 	libusb_lock_event_waiters (libusb_context *ctx)
  void 	libusb_unlock_event_waiters (libusb_context *ctx)
  int 	libusb_wait_for_event (libusb_context *ctx, struct timeval *tv)
  int 	libusb_handle_events_timeout (libusb_context *ctx, struct timeval *tv)
  int 	libusb_handle_events (libusb_context *ctx)
  int 	libusb_handle_events_locked (libusb_context *ctx, struct timeval *tv)
  int 	libusb_pollfds_handle_timeouts (libusb_context *ctx)
  int 	libusb_get_next_timeout (libusb_context *ctx, struct timeval *tv)
  void 	libusb_set_pollfd_notifiers (libusb_context *ctx, libusb_pollfd_added_cb added_cb, libusb_pollfd_removed_cb removed_cb, void *user_data)
  struct libusb_pollfd ** 	libusb_get_pollfds (libusb_context *ctx)
     */

 /*
  Synchronous device I/O:
     */
    /**
     * Perform a USB control transfer.
     *
     * The direction of the transfer is inferred from the bmRequestType field of
     * the setup packet.
     *
     * The wValue, wIndex and wLength fields values should be given in
     * host-endian byte order.
     *
     * @param dev_handle a handle for the device to communicate with
     * @param bmRequestType the request type field for the setup packet
     * @param bRequest the request field for the setup packet
     * @param wValue the value field for the setup packet
     * @param wIndex the index field for the setup packet
     * @param data a suitably-sized data buffer for either input or output
     * (depending on direction bits within bmRequestType)
     * @param wLength the length field for the setup packet. The data buffer
     * should be at least this size.
     * @param timeout timeout (in millseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @return on success, the number of bytes actually transferred
     * LIBUSB_ERROR_TIMEOUT if the transfer timed out LIBUSB_ERROR_PIPE if the
     * control request was not supported by the device LIBUSB_ERROR_NO_DEVICE if
     * the device has been disconnected another LIBUSB_ERROR code on other
     * failures
     */
    int libusb_control_transfer(Pointer dev_handle, byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data, short wLength, int timeout);

    /**
     * Perform a USB bulk transfer.
     *
     * The direction of the transfer is inferred from the direction bits of the
     * endpoint address.
     *
     * For bulk reads, the length field indicates the maximum length of data you
     * are expecting to receive. If less data arrives than expected, this
     * function will return that data, so be sure to check the transferred
     * output parameter.
     *
     * You should also check the transferred parameter for bulk writes. Not all
     * of the data may have been written.
     *
     * Also check transferred when dealing with a timeout error code. libusb may
     * have to split your transfer into a number of chunks to satisfy underlying
     * O/S requirements, meaning that the timeout may expire after the first few
     * chunks have completed. libusb is careful not to lose any data that may
     * have been transferred; do not assume that timeout conditions indicate a
     * complete lack of I/O.
     *
     * @param dev_handle a handle for the device to communicate with
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data a suitably-sized data buffer for either input or output
     * (depending on endpoint)
     * @param length for bulk writes, the number of bytes from data to be sent.
     * for bulk reads, the maximum number of bytes to receive into the data
     * buffer.
     * @param transferred output location for the number of bytes actually
     * transferred.
     * @param timeout timeout (in millseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @return 0 on success (and populates transferred) LIBUSB_ERROR_TIMEOUT if
     * the transfer timed out (and populates transferred) LIBUSB_ERROR_PIPE if
     * the endpoint halted LIBUSB_ERROR_OVERFLOW if the device offered more
     * data, see Packets and overflows LIBUSB_ERROR_NO_DEVICE if the device has
     * been disconnected another LIBUSB_ERROR code on other failures
     */
    int libusb_bulk_transfer(Pointer dev_handle, byte endpoint, byte[] data, int length, int[] transferred, int timeout);

    /**
     * Perform a USB interrupt transfer.
     *
     * The direction of the transfer is inferred from the direction bits of the
     * endpoint address.
     *
     * For interrupt reads, the length field indicates the maximum length of
     * data you are expecting to receive. If less data arrives than expected,
     * this function will return that data, so be sure to check the transferred
     * output parameter.
     *
     * You should also check the transferred parameter for interrupt writes. Not
     * all of the data may have been written.
     *
     * Also check transferred when dealing with a timeout error code. libusb may
     * have to split your transfer into a number of chunks to satisfy underlying
     * O/S requirements, meaning that the timeout may expire after the first few
     * chunks have completed. libusb is careful not to lose any data that may
     * have been transferred; do not assume that timeout conditions indicate a
     * complete lack of I/O.
     *
     * The default endpoint bInterval value is used as the polling interval.*
     *
     * @param dev_handle a handle for the device to communicate with
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data a suitably-sized data buffer for either input or output
     * (depending on endpoint)
     * @param length for bulk writes, the number of bytes from data to be sent.
     * for bulk reads, the maximum number of bytes to receive into the data
     * buffer.
     * @param transferred output location for the number of bytes actually
     * transferred.
     * @param timeout timeout (in millseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @return 0 on success (and populates transferred) LIBUSB_ERROR_TIMEOUT if
     * the transfer timed out LIBUSB_ERROR_PIPE if the endpoint halted
     * LIBUSB_ERROR_OVERFLOW if the device offered more data, see Packets and
     * overflows LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
     * another LIBUSB_ERROR code on other error
     */
    int libusb_interrupt_transfer(Pointer dev_handle, byte endpoint, byte[] data, int length, int[] transferred, int timeout);

    /*
  Asynchronous device I/O.
  Todo: Implement
  struct libusb_transfer * 	libusb_alloc_transfer (int iso_packets)
  void 	libusb_free_transfer (struct libusb_transfer *transfer)
  int 	libusb_submit_transfer (struct libusb_transfer *transfer)
  int 	libusb_cancel_transfer (struct libusb_transfer *transfer)
  static unsigned char * 	libusb_control_transfer_get_data (struct libusb_transfer *transfer)
  static struct libusb_control_setup * 	libusb_control_transfer_get_setup (struct libusb_transfer *transfer)
  static void 	libusb_fill_control_setup (unsigned char *buffer, uint8_t bmRequestType, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength)
  static void 	libusb_fill_control_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char *buffer, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_fill_bulk_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char endpoint, unsigned char *buffer, int length, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_fill_interrupt_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char endpoint, unsigned char *buffer, int length, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_fill_iso_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char endpoint, unsigned char *buffer, int length, int num_iso_packets, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_set_iso_packet_lengths (struct libusb_transfer *transfer, unsigned int length)
  static unsigned char * 	libusb_get_iso_packet_buffer (struct libusb_transfer *transfer, unsigned int packet)
  static unsigned char * 	libusb_get_iso_packet_buffer_simple (struct libusb_transfer *transfer, unsigned int packet)
     */
    /**
     * \ingroup libusb_asyncio Transfer status codes
     */
    enum libusb_transfer_status {
        /**
         * Transfer completed without error. Note that this does not indicate
         * that the entire amount of requested data was transferred.
         */
        LIBUSB_TRANSFER_COMPLETED,
        /**
         * Transfer failed
         */
        LIBUSB_TRANSFER_ERROR,
        /**
         * Transfer timed out
         */
        LIBUSB_TRANSFER_TIMED_OUT,
        /**
         * Transfer was cancelled
         */
        LIBUSB_TRANSFER_CANCELLED,
        /**
         * For bulk/interrupt endpoints: halt condition detected (endpoint
         * stalled). For control endpoints: control request not supported.
         */
        LIBUSB_TRANSFER_STALL,
        /**
         * Device was disconnected
         */
        LIBUSB_TRANSFER_NO_DEVICE,
        /**
         * Device sent more data than requested
         */
        LIBUSB_TRANSFER_OVERFLOW,

        /* NB! Remember to update libusb_error_name()
	   when adding new status codes here. */
    };

    public static class Libusb_transfer {

        /**
         * Base size of native LibusbTransfer (without iso_frame_desc) in bytes
         */
        public static final int libusbBaseSize;

        /**
         * Size of struct libusb_iso_packet_desc
         */
        private static final int packetDescSize;

        public static final int libusb_transfer_devicehandle;
        public static final int libusb_transfer_flags;
        public static final int libusb_transfer_endpoint;
        public static final int libusb_transfer_type;
        public static final int libusb_transfer_timeout;
        public static final int libusb_transfer_status;
        public static final int libusb_transfer_length;
        public static final int libusb_transfer_actual_length;
        public static final int libusb_transfer_callback;
        public static final int libusb_transfer_usercontext;
        public static final int libusb_transfer_buffer;
        public static final int libusb_transfer_number_of_packets_stream_id;
        public static final int libusb_iso_packet_desc_length;
        public static final int libusb_iso_packet_desc_actual_length;
        public static final int libusb_iso_packet_desc_status;

        static {
            libusb_transfer_base base = new libusb_transfer_base();
            libusb_iso_packet_descriptor desc = new libusb_iso_packet_descriptor();
            libusbBaseSize = base.size();
            packetDescSize = desc.size();

            libusb_transfer_devicehandle = base.fieldOffset("dev_handle");
            libusb_transfer_flags = base.fieldOffset("flags");
            libusb_transfer_endpoint = base.fieldOffset("endpoint");
            libusb_transfer_type = base.fieldOffset("type");
            libusb_transfer_timeout = base.fieldOffset("timeout");
            libusb_transfer_status = base.fieldOffset("status");
            libusb_transfer_length = base.fieldOffset("length");
            libusb_transfer_actual_length = base.fieldOffset("actual_length");
            libusb_transfer_callback = base.fieldOffset("callback");
            libusb_transfer_usercontext = base.fieldOffset("user_data");
            libusb_transfer_buffer = base.fieldOffset("buffer");
            libusb_transfer_number_of_packets_stream_id = base.fieldOffset("num_iso_packets");
            libusb_iso_packet_desc_length = desc.fieldOffset("length");
            libusb_iso_packet_desc_actual_length = desc.fieldOffset("actual_length");
            libusb_iso_packet_desc_status = desc.fieldOffset("status");

        }

        private ByteBuffer urbBuf;
        private Pointer libusbBuf;
        private static int maxPackets;
        private static int MAX_PACKET_SIZE;
        public static int libusbSize;

        public Libusb_transfer(int maxPackets) {
            this.maxPackets = maxPackets;
            libusbSize = libusbBaseSize + maxPackets * packetDescSize;

            //libusbBuf = ByteBuffer.allocateDirect(libusbSize);
            //libusbBuf.order(ByteOrder.nativeOrder());
            //libusbBufPointer = Native.getDirectBufferPointer(libusbBuf);
        }

        public Libusb_transfer(int maxPackets, int maxPacketSize, Pointer nativePointer) {

            this.libusbBuf = nativePointer;
            this.maxPackets = maxPackets;
            libusbSize = libusbBaseSize + maxPackets * packetDescSize;
            this.MAX_PACKET_SIZE = maxPacketSize;
            int urbSize = libusbBaseSize + maxPackets * packetDescSize;
            //this.libusbBuf = libusbBufPointer.getByteBuffer(0, libusbSize);
            //urbBuf.order(ByteOrder.nativeOrder());
        }

        public int getMaxPackets() {
            return maxPackets;
        }

        public int getLibusbSize() {
            return libusbSize;
        }

        public int getMaxPacketSize() {
            return MAX_PACKET_SIZE;
        }

        public Pointer getNativeLibusbAddr() {
            return libusbBuf;
        }

        public void setNativeLibusbAddr(Pointer adress) {
            libusbBuf = adress;
            urbBuf = libusbBuf.getByteBuffer(0, libusbBaseSize + maxPackets * packetDescSize);
        }
        
        public static int getTransferStatus(Pointer urbPointer) {
            return urbPointer.getInt(libusb_transfer_status);
        }
        

        public void setDev_handle(Pointer handle) {
            if (Native.POINTER_SIZE == 4) {
                libusbBuf.setInt(libusb_transfer_devicehandle, (int) Pointer.nativeValue(handle));
            } else if (Native.POINTER_SIZE == 8) {
                libusbBuf.setLong(libusb_transfer_devicehandle, Pointer.nativeValue(handle));
            } else {
                throw new IllegalStateException("Unhandled Pointer Size: " + Native.POINTER_SIZE);
            }
            //  libusbBuf.put(libusb_transfer_devicehandle, handle);
        }

        public void setFlags(byte flags) {
            libusbBuf.setByte(libusb_transfer_flags, flags);
        }

        public void setEndpoint(byte endpoint) {
            libusbBuf.setByte(libusb_transfer_endpoint, endpoint);
        }

        public void setType(byte type) {
            libusbBuf.setByte(libusb_transfer_type, type);
        }

        public void setTimeout(int timeout) {
            libusbBuf.setInt(libusb_transfer_timeout, timeout);
        }

        public void setTransferLength(int bufferLength) {
            libusbBuf.setInt(libusb_transfer_length, bufferLength);
        }

        public int getTransferLength() {
            return libusbBuf.getInt(libusb_transfer_length);
        }

        public void setActualLength(int actualLength) {
            libusbBuf.setInt(libusb_transfer_actual_length, actualLength);
        }

        public int getTransferActualLength() {
            return libusbBuf.getInt(libusb_transfer_actual_length);
        }

        // Callback function
        public void setCallback(Pointer callback) {

            if (Native.POINTER_SIZE == 4) {
                libusbBuf.setInt(libusb_transfer_callback, (int) Pointer.nativeValue(callback));
            } else if (Native.POINTER_SIZE == 8) {
                libusbBuf.setLong(libusb_transfer_callback, Pointer.nativeValue(callback));
            } else {
                throw new IllegalStateException("Unhandled Pointer Size: " + Native.POINTER_SIZE);
            }
        }

        public int getUserContext() {
            if (Native.POINTER_SIZE == 4) {
                return libusbBuf.getInt(libusb_transfer_usercontext);
            } else if (Native.POINTER_SIZE == 8) {
                return (int) libusbBuf.getLong(libusb_transfer_usercontext);
            } else {
                throw new IllegalStateException("Unhandled Pointer Size: " + Native.POINTER_SIZE);
            }
        }

        public static int getUserContext(Pointer urbBufPointer) {
            return urbBufPointer.getInt(40);
        }

        public int setUserContext(int userContext) {

            if (Native.POINTER_SIZE == 4) {
                libusbBuf.setInt(libusb_transfer_usercontext, userContext);
            } else if (Native.POINTER_SIZE == 8) {
                libusbBuf.setLong(libusb_transfer_usercontext, userContext);
            } else {
                throw new IllegalStateException("Unhandled Pointer Size: " + Native.POINTER_SIZE);
            }
            return libusb_transfer_usercontext;
        }

        public void setBuffer(Pointer buffer) {
            if (Native.POINTER_SIZE == 4) {
                libusbBuf.setInt(libusb_transfer_buffer, (int) Pointer.nativeValue(buffer));
            } else if (Native.POINTER_SIZE == 8) {
                libusbBuf.setLong(libusb_transfer_buffer, Pointer.nativeValue(buffer));
            } else {
                throw new IllegalStateException("Unhandled Pointer Size: " + Native.POINTER_SIZE);
            }
        }

        /**
         * Used to retrieve data that has been received from the device.
         */
        public void getPacketData(int packetNo, byte[] buf, int len) {
            if (packetNo < 0 || packetNo >= maxPackets || len > MAX_PACKET_SIZE) {
                throw new IllegalArgumentException();
            }
            libusbBuf.read(packetNo * MAX_PACKET_SIZE, buf, 0, len);
        }

        public void setNumberOfPackets(int numberOfPackets) {
            libusbBuf.setInt(libusb_transfer_number_of_packets_stream_id, numberOfPackets);
        }

        public int getNumberOfPackets() {
            return libusbBuf.getInt(libusb_transfer_number_of_packets_stream_id);
        }

        public void setPacketLength(int packetNo, int length) {
            if (packetNo < 0 || packetNo >= maxPackets) {
                throw new IllegalArgumentException();
            }
            libusbBuf.setInt(libusbBaseSize + packetNo * packetDescSize + libusb_iso_packet_desc_length, length);    // für packetNo = 0 == urbBaseSize = 44 packetDescSize = 12 packetNo = 0
        }

        public int getPacketLength(int packetNo) {
            if (packetNo < 0 || packetNo >= maxPackets) {
                throw new IllegalArgumentException();
            }
            return libusbBuf.getInt(libusbBaseSize + packetNo * packetDescSize + libusb_iso_packet_desc_length);
        }

        public void setPacketActualLength(int packetNo, int actualLength) {
            if (packetNo < 0 || packetNo >= maxPackets) {
                throw new IllegalArgumentException();
            }
            libusbBuf.setInt(libusbBaseSize + packetNo * packetDescSize + libusb_iso_packet_desc_actual_length, actualLength);
        }

        public int getPacketActualLength(int packetNo) {
            if (packetNo < 0 || packetNo >= maxPackets) {
                throw new IllegalArgumentException();
            }
            return libusbBuf.getInt(libusbBaseSize + packetNo * packetDescSize + libusb_iso_packet_desc_actual_length);
        }

        public void setPacketStatus(int packetNo, int status) {
            if (packetNo < 0 || packetNo >= maxPackets) {
                throw new IllegalArgumentException();
            }
            libusbBuf.setInt(libusbBaseSize + packetNo * packetDescSize + libusb_iso_packet_desc_status, status);
        }

        public int getPacketStatus(int packetNo) {
            // System.out.println("sadcasdor = ");
            if (packetNo < 0 || packetNo >= maxPackets) {
                throw new IllegalArgumentException();
            }
            return libusbBuf.getInt(libusbBaseSize + packetNo * packetDescSize + libusb_iso_packet_desc_status);
            //System.out.println("Endpunktadresse vo");
        }

    }

    @Structure.FieldOrder({"dev_handle", "flags", "endpoint", "type", "timeout",
        "status", "length", "actual_length",
        "callback", "user_data", "buffer",
        "num_iso_packets"})
    public class libusb_transfer_base extends Structure {

        public Pointer dev_handle;

        public byte flags;

        public byte endpoint;

        public byte type;

        public int timeout;

        public int status;

        public int length;

        public int actual_length;

        public Libusb_transfer_cb_fn callback;

        public Pointer user_data;

        public Pointer buffer;

        public int num_iso_packets;

        @Override
        public int fieldOffset(String field) {
            return super.fieldOffset(field);
        }

    }

    @Structure.FieldOrder({"length", "actual_length", "status"})
    public static final class libusb_iso_packet_descriptor extends Structure {

        public int length;
        public int actual_length;
        public int status;

        @Override
        public int fieldOffset(String field) {
            return super.fieldOffset(field);
        }
    }

    Pointer libusb_alloc_transfer(int iso_packets);

    int libusb_submit_transfer(Pointer transferPointer);

    int libusb_handle_events(Pointer ctx);

    interface Libusb_transfer_cb_fn extends Callback {

        void invoke(Pointer transfer);

    }

    void libusb_fill_iso_transfer(Libusb_transfer transfer,
            Pointer dev_handle,
            byte endpoint,
            Pointer buffer,
            int length,
            int num_iso_packets,
            Libusb_transfer_cb_fn callback,
            Pointer user_data,
            int timeout
    );

    void libusb_set_iso_packet_lengths(Pointer transfer,
            int length
    );

    void libusb_free_transfer(Pointer transfer);
    
    Pointer libusb_error_name (int error);
    
    Pointer libusb_strerror (String name);

}
