/*
 * Copyright (c) 2011 Daniel Marell
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package se.marell.libusb;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.nio.ByteBuffer;
import se.marell.libusb.jna.LibUsb;
import se.marell.libusb.jna.LibUsb.Libusb_transfer;
import se.marell.libusb.jna.LibUsb.Libusb_transfer_cb_fn;
import se.marell.libusb.jna.libusb_device_descriptor;
import se.marell.libusb.jna.libusb_error;

/**
 * Represents an USB device. libusb-operations operating on a specific USB
 * device is collected here. When you receive an USB device, it is opened. It is
 * your responsibility to close it when you are done with it. You may re-open a
 * closed device.
 */
public class UsbDevice {

    private Pointer usb_device;
    private Pointer dev_handle;
    //private Pointer context;
    private LibUsb usb;
    private libusb_device_descriptor descriptor;

    public UsbDevice(LibUsb usb, Pointer usb_device) throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException {
        this.usb = usb;
        this.usb_device = usb_device;
        descriptor = get_descriptor();
    }

    /**
     * Get usb_device pointer.
     *
     * @return usb_device pointer
     */
    public Pointer get_usb_device() {
        return usb_device;
    }

    /**
     * Get usb_handle pointer.
     *
     * @return usb_handle pointer or null if device is not open
     */
    public Pointer get_dev_handle() {
        return dev_handle;
    }


    /**
     * Get the number of the bus that a device is connected to.
     *
     * @return the bus number
     */
    public int get_bus_number() {
        return usb.libusb_get_bus_number(usb_device);
    }

    /**
     * Get the address of the device on the bus it is connected to.
     *
     * @return the device address
     */
    public int get_address() {
        return usb.libusb_get_device_address(usb_device);
    }

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
     * @param endpoint address of the endpoint in question
     * @return the maximum packet size which can be sent/received on this
     * endpoint
     * @throws LibUsbNotFoundException if the endpoint does not exist
     * @throws LibUsbOtherException if another USB error occurred
     */
    public int get_max_iso_packet_size(int endpoint) throws LibUsbNotFoundException,
            LibUsbOtherException {
        int rc = usb.libusb_get_max_iso_packet_size(usb_device, endpoint);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        return rc;
    }

    /**
     * Increment the reference count of a device.
     */
    public void ref_device() {
        usb.libusb_ref_device(usb_device);
    }

    /**
     * Decrement the reference count of a device. If the decrement operation
     * causes the reference count to reach zero, the device shall be destroyed.
     */
    public void unref_device() {
        usb.libusb_unref_device(usb_device);
    }

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
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbPermissionException if the user has insufficient
     * permissions
     * @throws LibUsbOtherException if another USB error occurred
     */
    public int open() throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException {
        close(); // Make sure it's closed if we're reopening a device
        Pointer[] arr = new Pointer[1];
        int rc = usb.libusb_open(usb_device, arr);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_ACCESS:
                    throw new LibUsbPermissionException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        dev_handle = arr[0];
        return rc;
    }

    public void open_with_vendor_product(Pointer context) throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException {
        close(); // Make sure it's closed if we're reopening a device
        dev_handle = usb.libusb_open_device_with_vid_pid(context, getIdVendor(), getIdProduct());
    }

    public int init() throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException {
        Pointer[] arr = new Pointer[1];
        int rc = usb.libusb_init(arr);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_ACCESS:
                    throw new LibUsbPermissionException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        //context = arr[0];
        return rc;
    }

    /**
     * Close a device handle.
     *
     * Should be called on all open handles before your application exits.
     *
     * Internally, this function destroys the reference that was added by
     * open().
     *
     * This is a non-blocking function; no requests are sent over the bus.
     */
    public void close() {
        if (dev_handle != null) {
            usb.libusb_close(dev_handle);
            dev_handle = null;
        }
    }

    public void exit(Pointer context) {
        usb.libusb_exit(context);
    }

    /**
     * @return USB specification release number in binary-coded decimal. A value
     * of 0x0200 indicates USB 2.0, 0x0110 indicates USB 1.1, etc.
     */
    public short getBcdUSB() {
        return descriptor.bcdDevice;
    }

    /**
     * @return USB-IF class code for the device.
     */
    public byte getBDeviceClass() {
        return descriptor.bDeviceClass;
    }

    /**
     * @return USB-IF subclass code for the device, qualified by the
     * bDeviceClass value.
     */
    public byte getBDeviceSubClass() {
        return descriptor.bDeviceSubClass;
    }

    /**
     * @return USB-IF protocol code for the device, qualified by the
     * bDeviceClass and bDeviceSubClass values.
     */
    public byte getBDeviceProtocol() {
        return descriptor.bDeviceProtocol;
    }

    /**
     * @return Maximum packet size for endpoint 0.
     */
    public byte getBMaxPacketSize0() {
        return descriptor.bMaxPacketSize0;
    }

    /**
     * @return USB-IF vendor ID.
     */
    public short getIdVendor() {
        return descriptor.idVendor;
    }

    /**
     * @return USB-IF product ID.
     */
    public short getIdProduct() {
        return descriptor.idProduct;
    }

    /**
     * @return Device release number in binary-coded decimal.
     */
    public short getBcdDevice() {
        return descriptor.bcdDevice;
    }

    /**
     * @return String describing manufacturer.
     */
    public String getManufacturer() {
        return get_string_ascii(descriptor.iManufacturer);
    }

    /**
     * @return String describing product.
     */
    public String getProduct() {
        return get_string_ascii(descriptor.iProduct);
    }

    /**
     * @return String containing device serial number.
     */
    public String getSerialNumber() {
        return get_string_ascii(descriptor.iSerialNumber);
    }

    /**
     * @return Number of possible configurations.
     */
    public byte getBNumConfigurations() {
        return descriptor.bNumConfigurations;
    }

    /**
     * Get the USB device descriptor for a given device.
     *
     * This is a non-blocking function; the device descriptor is cached in
     * memory.
     *
     * @return the descriptor
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbPermissionException if the user has insufficient
     * permissions
     * @throws LibUsbOtherException if another USB error occurred
     */
    private libusb_device_descriptor get_descriptor() throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException {
        libusb_device_descriptor[] arr = new libusb_device_descriptor[1];
        int rc = usb.libusb_get_device_descriptor(usb_device, arr);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_ACCESS:
                    throw new LibUsbPermissionException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        return arr[0];
    }

    /**
     * Retrieve a string descriptor in C style ASCII.
     *
     * Wrapper around libusb_get_string_descriptor(). Uses the first language
     * supported by the device.
     *
     * @param desc_index the index of the descriptor to retrieve
     * @return Text string or null
     */
    public String get_string_ascii(byte desc_index) {
        byte[] data = new byte[256];
        usb.libusb_get_string_descriptor_ascii(dev_handle, desc_index, data, data.length);

        // Convert C string to Java String
        int len = 0;
        while (data[len] != 0) {
            ++len;
        }
        return new String(data, 0, len);
    }

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
     * This function will return a value of 0 if the device is in unconfigured
     * state.
     *
     * @return bConfigurationValue
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbPermissionException if the user has insufficient
     * permissions
     * @throws LibUsbOtherException if another USB error occurred
     */
    public int get_configuration() throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException {
        int[] arr = new int[1];
        int rc = usb.libusb_get_configuration(dev_handle, arr);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_ACCESS:
                    throw new LibUsbPermissionException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        return arr[0];
    }

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
     * @param configuration the bConfigurationValue of the configuration you
     * wish to activate, or -1 if you wish to put the device in unconfigured
     * state
     * @throws LibUsbNotFoundException if the requested configuration does not
     * exist
     * @throws LibUsbBusyException if interfaces are currently claimed
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void set_configuration(int configuration) throws LibUsbNotFoundException,
            LibUsbBusyException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_set_configuration(dev_handle, configuration);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException("configuration=" + configuration);
                case libusb_error.ERROR_BUSY:
                    throw new LibUsbBusyException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

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
     * @param interface_number the bInterfaceNumber of the interface you wish to
     * claim
     * @throws LibUsbNotFoundException if the requested interface does not exist
     * @throws LibUsbBusyException if another program or driver has claimed the
     * interface
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void claim_interface(int interface_number) throws LibUsbNotFoundException,
            LibUsbBusyException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_claim_interface(dev_handle, interface_number);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException();
                case libusb_error.ERROR_BUSY:
                    throw new LibUsbBusyException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

    /**
     * Release an interface previously claimed with libusb_claim_interface().
     *
     * You should release all claimed interfaces before closing a device handle.
     *
     * This is a blocking function. A SET_INTERFACE control request will be sent
     * to the device, resetting interface state to the first alternate setting.
     *
     * @param interface_number the bInterfaceNumber of the previously-claimed
     * interface
     * @throws LibUsbNotFoundException if the interface was not claimed
     * @throws LibUsbBusyException if another program or driver has claimed the
     * interface
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void release_interface(int interface_number) throws LibUsbNotFoundException,
            LibUsbBusyException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_release_interface(dev_handle, interface_number);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException("interface_number=" + interface_number);
                case libusb_error.ERROR_BUSY:
                    throw new LibUsbBusyException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

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
     * @param interface_number the bInterfaceNumber of the previously-claimed
     * interface
     * @param alternate_setting the bAlternateSetting of the alternate setting
     * to activate
     * @throws LibUsbNotFoundException if the interface was not claimed, or the
     * requested alternate setting does not exist
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void set_interface_alt_setting(int interface_number, int alternate_setting) throws LibUsbNotFoundException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_set_interface_alt_setting(dev_handle, interface_number, alternate_setting);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException("interface_number=" + interface_number + ",alternate_setting=" + alternate_setting);
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

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
     * @param endpoint the endpoint to clear halt status
     * @throws LibUsbNotFoundException if the endpoint does not exist
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void clear_halt(byte endpoint) throws LibUsbNotFoundException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_clear_halt(dev_handle, endpoint);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException("endpoint=" + endpoint);
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

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
     * @throws LibUsbNotFoundException if re-enumeration is required, or if the
     * device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void reset_device() throws LibUsbNotFoundException,
            LibUsbOtherException {
        int rc = usb.libusb_reset_device(dev_handle);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

    /**
     * Determine if a kernel driver is active on an interface.
     *
     * If a kernel driver is active, you cannot claim the interface, and libusb
     * will be unable to perform I/O.
     *
     * @param interface_number the interface to check
     * @return true if kernel driver is active
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public boolean kernel_driver_active(int interface_number) throws LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_kernel_driver_active(dev_handle, interface_number);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        return rc == 1;
    }

    /**
     * Detach a kernel driver from an interface.
     *
     * If successful, you will then be able to claim the interface and perform
     * I/O.
     *
     * @param interface_number the interface to detach the driver from
     * @throws LibUsbInvalidParameterException if the interface does not exist
     * @throws LibUsbNotFoundException if no kernel driver was active
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void detach_kernel_driver(int interface_number) throws LibUsbInvalidParameterException,
            LibUsbNotFoundException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_detach_kernel_driver(dev_handle, interface_number);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_INVALID_PARAM:
                    throw new LibUsbInvalidParameterException("interface_number=" + interface_number);
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

    /**
     * Re-attach an interface's kernel driver, which was previously detached
     * using libusb_detach_kernel_driver().
     *
     * @param interface_number the interface to attach the driver from
     * @throws LibUsbInvalidParameterException if the interface does not exist
     * @throws LibUsbNotFoundException if no kernel driver was active
     * @throws LibUsbBusyException if the driver cannot be attached because the
     * interface is claimed by a program or driver
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void attach_kernel_driver(int interface_number) throws LibUsbInvalidParameterException,
            LibUsbNotFoundException,
            LibUsbBusyException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_attach_kernel_driver(dev_handle, interface_number);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_INVALID_PARAM:
                    throw new LibUsbInvalidParameterException("interface_number=" + interface_number);
                case libusb_error.ERROR_NOT_FOUND:
                    throw new LibUsbNotFoundException();
                case libusb_error.ERROR_BUSY:
                    throw new LibUsbBusyException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

    /**
     * Perform a USB control write.
     *
     * The direction of the transfer is inferred from the bmRequestType field of
     * the setup packet.
     *
     * The wValue, wIndex and wLength fields values should be given in
     * host-endian byte order.
     *
     * @param bmRequestType the request type field for the setup packet
     * @param bRequest the request field for the setup packet
     * @param wValue the value field for the setup packet
     * @param wIndex the index field for the setup packet
     * @param data data buffer to send
     * @param wLength the length field for the setup packet. The data buffer
     * should be at least this size.
     * @param timeout timeout (in milliseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @throws LibUsbTimeoutException if the transfer timed out
     * @throws LibUsbPipeException if the control request was not supported by
     * the device
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbTransmissionException if all data could not be sent
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void control_write(byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data,
            short wLength, int timeout) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbNoDeviceException,
            LibUsbTransmissionException,
            LibUsbOtherException {
        int rc = usb.libusb_control_transfer(dev_handle, bmRequestType, bRequest, wValue, wIndex, data, wLength, timeout);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_TIMEOUT:
                    throw new LibUsbTimeoutException();
                case libusb_error.ERROR_PIPE:
                    throw new LibUsbPipeException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        } else {
            if (rc != data.length) {
                throw new LibUsbTransmissionException("Transferred " + rc + " bytes of " + data.length);
            }
        }
    }

    /**
     * Perform a USB control read.
     *
     * The direction of the transfer is inferred from the bmRequestType field of
     * the setup packet.
     *
     * The wValue, wIndex and wLength fields values should be given in
     * host-endian byte order.
     *
     * @param bmRequestType the request type field for the setup packet
     * @param bRequest the request field for the setup packet
     * @param wValue the value field for the setup packet
     * @param wIndex the index field for the setup packet
     * @param data a suitably-sized data buffer for input
     * @param wLength the length field for the setup packet. The data buffer
     * should be at least this size.
     * @param timeout timeout (in milliseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @return the number of bytes actually transferred
     * @throws LibUsbTimeoutException if the transfer timed out
     * @throws LibUsbPipeException if the control request was not supported by
     * the device
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public int control_read(byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data,
            short wLength, int timeout) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int rc = usb.libusb_control_transfer(dev_handle, bmRequestType, bRequest, wValue, wIndex, data, (short) data.length, timeout);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_TIMEOUT:
                    throw new LibUsbTimeoutException();
                case libusb_error.ERROR_PIPE:
                    throw new LibUsbPipeException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        return rc;
    }

    /**
     * Perform a USB bulk write.
     *
     * The direction of the transfer is inferred from the direction bits of the
     * endpoint address.
     *
     * Check transferred bytes when dealing with a timeout error code. libusb
     * may have to split your transfer into a number of chunks to satisfy
     * underlying O/S requirements, meaning that the timeout may expire after
     * the first few chunks have completed. libusb is careful not to lose any
     * data that may have been transferred; do not assume that timeout
     * conditions indicate a complete lack of I/O.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data a suitably-sized data buffer for either input or output
     * (depending on endpoint)
     * @param timeout timeout (in milliseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @throws LibUsbTimeoutException if the transfer timed out
     * @throws LibUsbPipeException if the endpoint halted
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbTransmissionException if all data could not be sent
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void bulk_write(int endpoint, byte[] data, int timeout) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbNoDeviceException,
            LibUsbTransmissionException,
            LibUsbOtherException {
        int[] transferred = new int[1];
        int rc = usb.libusb_bulk_transfer(dev_handle, (byte) endpoint, data, data.length, transferred, timeout);
        if (transferred[0] != data.length) {
            throw new LibUsbTransmissionException("Transferred " + transferred[0] + " bytes of " + data.length);
        }
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_TIMEOUT:
                    throw new LibUsbTimeoutException(transferred[0]);
                case libusb_error.ERROR_PIPE:
                    throw new LibUsbPipeException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

    /**
     * Perform a USB bulk read.
     *
     * The direction of the transfer is inferred from the direction bits of the
     * endpoint address.
     *
     * The length field indicates the maximum length of data you are expecting
     * to receive. If less data arrives than expected, this function will return
     * that data, so be sure to check the return value.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data a suitably-sized data buffer for either input or output
     * (depending on endpoint)
     * @param timeout timeout (in milliseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @return Number of bytes received
     * @throws LibUsbTimeoutException if the transfer timed out
     * @throws LibUsbPipeException if the endpoint halted
     * @throws LibUsbOverflowException if the device offered more data, see
     * Packets and overflows
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public int bulk_read(int endpoint, byte[] data, int timeout) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbOverflowException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int[] transferred = new int[1];
        int rc = usb.libusb_bulk_transfer(dev_handle, (byte) endpoint, data, data.length, transferred, timeout);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_TIMEOUT:
                    throw new LibUsbTimeoutException(transferred[0]);
                case libusb_error.ERROR_PIPE:
                    throw new LibUsbPipeException();
                case libusb_error.ERROR_OVERFLOW:
                    throw new LibUsbOverflowException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        return transferred[0];
    }

    /**
     * Perform a USB interrupt write.
     *
     * The direction of the transfer is inferred from the direction bits of the
     * endpoint address.
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
     * The default endpoint bInterval value is used as the polling interval.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data a suitably-sized data buffer for output
     * @param timeout timeout (in milliseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @throws LibUsbTimeoutException if the transfer timed out
     * @throws LibUsbPipeException if the endpoint halted
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbTransmissionException if all data could not be sent
     * @throws LibUsbOtherException if another USB error occurred
     */
    public void interrupt_write(int endpoint, byte[] data, int timeout) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbNoDeviceException,
            LibUsbTransmissionException,
            LibUsbOtherException {
        int[] transferred = new int[1];
        int rc = usb.libusb_interrupt_transfer(dev_handle, (byte) endpoint, data, data.length, transferred, timeout);
        if (transferred[0] != data.length) {
            throw new LibUsbTransmissionException("Transferred " + transferred[0] + " bytes of " + data.length);
        }
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_TIMEOUT:
                    throw new LibUsbTimeoutException();
                case libusb_error.ERROR_PIPE:
                    throw new LibUsbPipeException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
    }

    /**
     * Perform a USB interrupt read.
     *
     * The direction of the transfer is inferred from the direction bits of the
     * endpoint address.
     *
     * The length field indicates the maximum length of data you are expecting
     * to receive. If less data arrives than expected, this function will return
     * that data, so be sure to check the return value.
     *
     * Also check transferred when dealing with a timeout error code. libusb may
     * have to split your transfer into a number of chunks to satisfy underlying
     * O/S requirements, meaning that the timeout may expire after the first few
     * chunks have completed. libusb is careful not to lose any data that may
     * have been transferred; do not assume that timeout conditions indicate a
     * complete lack of I/O.
     *
     * The default endpoint bInterval value is used as the polling interval.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data a suitably-sized data buffer for input
     * @param timeout timeout (in milliseconds) that this function should wait
     * before giving up due to no response being received. For an unlimited
     * timeout, use value 0.
     * @return Number of bytes received
     * @throws LibUsbTimeoutException if the transfer timed out
     * @throws LibUsbPipeException if the endpoint halted
     * @throws LibUsbOverflowException if the device offered more data, see
     * Packets and overflows
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException if another USB error occurred
     */
    public int interrupt_read(int endpoint, byte[] data, int timeout) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbOverflowException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int[] transferred = new int[1];
        int rc = usb.libusb_interrupt_transfer(dev_handle, (byte) endpoint, data, data.length, transferred, timeout);
        if (rc < 0) {
            switch (rc) {
                case libusb_error.ERROR_TIMEOUT:
                    throw new LibUsbTimeoutException(transferred[0]);
                case libusb_error.ERROR_PIPE:
                    throw new LibUsbPipeException();
                case libusb_error.ERROR_OVERFLOW:
                    throw new LibUsbOverflowException();
                case libusb_error.ERROR_NO_DEVICE:
                    throw new LibUsbNoDeviceException();
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        return transferred[0];
    }

    @Override
    public String toString() {
        return "UsbDevice {"
                + " bus_number=" + get_bus_number()
                + " address=" + get_address()
                + "}";
    }

    /*
    
    public int submitTransfer (Pointer transferPointer) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbOverflowException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        int ret = usb.libusb_submit_transfer(transferPointer);
        return ret;
    }
     */
    public Pointer allocateTransfer(int numPackets) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbOverflowException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        return usb.libusb_alloc_transfer(numPackets);
    }

    public void setIsoPacketsLenght(Pointer transfer,
            int length) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbOverflowException,
            LibUsbNoDeviceException,
            LibUsbOtherException {

        usb.libusb_set_iso_packet_lengths(transfer, length);

    }

    public int handle_events(Pointer context) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbOverflowException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        return usb.libusb_handle_events(context);
    }

    public void free_transfer(Pointer transfer) throws LibUsbTimeoutException,
            LibUsbPipeException,
            LibUsbOverflowException,
            LibUsbNoDeviceException,
            LibUsbOtherException {
        usb.libusb_free_transfer(transfer);
    }

    public int submitTransfer(Pointer transfer) {
        int ret = usb.libusb_submit_transfer(transfer);
        return ret;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Pointer errorName (int error) {
        
        return usb.libusb_error_name(error);
    }
    
    public Pointer strError ( String error) {
        
        return usb.libusb_strerror(error);
    }

}
