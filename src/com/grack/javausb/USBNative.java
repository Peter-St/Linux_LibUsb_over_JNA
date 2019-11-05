package com.grack.javausb;

import java.nio.ByteBuffer;

import com.grack.javausb.jna.LibUSBXNative;
import com.grack.javausb.jna.libusb_config_descriptor;
import com.grack.javausb.jna.libusb_device_descriptor;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Rather than everyone calling LibUSB from all over, we go through a number of
 * nice methods in one central place.
 */
public class USBNative {

    private static final LibUSBXNative USB = LibUSBXNative.INSTANCE;

    static void cleanup(libusb_config_descriptor descriptor) {
        USB.libusb_free_config_descriptor(descriptor.getPointer());
    }

    static Pointer openLibrary() throws USBException {
        PointerByReference ctxPtr = new PointerByReference();
        nativeCall(USB.libusb_init(ctxPtr));
        return ctxPtr.getValue();
    }

    private static int nativeCall(int response) throws USBException {
        if (response < 0) {
            throw new USBException(response);
        }
        return response;
    }

    static String getStringDescriptionAscii(Pointer dev, byte index) throws USBException {
        byte[] s = new byte[4096];
        nativeCall(USB.libusb_get_string_descriptor_ascii(dev, index, s, s.length));
        return Native.toString(s, "ASCII");
    }

    static Pointer openDevice(Pointer dev) throws USBException {
        PointerByReference handle = new PointerByReference();
        nativeCall(USB.libusb_open(dev, handle));
        return handle.getValue();
    }

    static void closeDevice(Pointer handle) {
        USB.libusb_close(handle);
    }

    static void unrefDevice(Pointer dev) {
        USB.libusb_unref_device(dev);
    }

    static int get_bus_number(Pointer usb_device) {
        return USB.libusb_get_bus_number(usb_device);
    }

    static int get_address(Pointer usb_device) {
        return USB.libusb_get_device_address(usb_device);
    }

    static int libusb_set_interface_alt_setting(Pointer dev_handle, int interface_number, int alternate_setting) {
        return USB.libusb_set_interface_alt_setting(dev_handle, interface_number, alternate_setting);
    }

    static int libusb_release_interface(Pointer dev_handle, int interface_number) {
        return USB.libusb_release_interface(dev_handle, interface_number);
    }

    static libusb_config_descriptor getConfigDescriptor(Pointer dev, int index) throws USBException {
        PointerByReference config = new PointerByReference();
        nativeCall(USB.libusb_get_config_descriptor(dev, index, config));
        libusb_config_descriptor descriptor = new libusb_config_descriptor(config.getValue());
        return descriptor;
    }

    static int bulkTransfer(Pointer handle, int endpoint, byte[] b, int off, int len) throws USBException {
        IntByReference transferred = new IntByReference();
        if (off == 0) {
            nativeCall(USB.libusb_bulk_transfer(handle, (byte) endpoint, b, len, transferred, 1000));
        } else {
            ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
            nativeCall(USB.libusb_bulk_transfer(handle, (byte) endpoint, buffer, len, transferred, 1000));
        }
        return transferred.getValue();
    }

    static int kernel_driver_active(Pointer handle, int iface) throws USBException {
        return nativeCall(USB.libusb_kernel_driver_active(handle, iface));
    }

    static int detach_kernel_driver(Pointer handle, int iface) throws USBException {
        return nativeCall(USB.libusb_detach_kernel_driver(handle, iface));
    }

    static int claimInterface(Pointer handle, int iface) throws USBException {
        return nativeCall(USB.libusb_claim_interface(handle, iface));
    }

    static void setConfiguration(Pointer handle, int configuration) throws USBException {
        nativeCall(USB.libusb_set_configuration(handle, configuration));
    }

    static int sendControlTransfer(Pointer handle, int requestType, int request, int value, int index, byte[] buffer, int off, int len)
            throws USBException {
        // TODO: This should be a parameter?
        int timeout = 1000;
        return nativeCall(USB.libusb_control_transfer(handle, (byte) requestType, (byte) request, (short) value, (short) index,
                ByteBuffer.wrap(buffer, off, len), (short) len, timeout));
    }

    static int sendControlTransfer(Pointer handle, byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data,
            short wLength, int timeout)
            throws USBException {
        // TODO: This should be a parameter?
        return USB.libusb_control_transfer(handle, bmRequestType, bRequest, wValue, wIndex,
                data, wLength, timeout);
    }

    static Pointer alloc_transfer(int numPackets) throws USBException {

        return USB.libusb_alloc_transfer(numPackets);
    }

    static void freeLibrary(Pointer ctx) {
        USB.libusb_exit(ctx);
    }

    static libusb_device_descriptor getDeviceDescriptor(Pointer dev) throws USBException {
        libusb_device_descriptor[] desc = new libusb_device_descriptor[1];
        nativeCall(USB.libusb_get_device_descriptor(dev, desc));
        return desc[0];
    }

    static Pointer[] getDeviceHandles(Pointer ctx) {
        // libusb_device*** list
        final PointerByReference list = new PointerByReference();
        final int count = USB.libusb_get_device_list(ctx, list);
        Pointer[] pointers = list.getValue().getPointerArray(0, count);
        USB.libusb_free_device_list(list.getValue(), false);
        return pointers;
    }

    static int submitTransfer(Pointer transfer) {
        return USB.libusb_submit_transfer(transfer);
    }

    static int handle_events(Pointer context) {
        return USB.libusb_handle_events(context);
    }
    
    static Pointer open_device_with_vid_pid(Pointer context, int vendor_id, int product_id) {
        return USB.libusb_open_device_with_vid_pid(context, vendor_id, product_id);
    }

    public static enum libusb_transfer_type {
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

    /**
     * \ingroup asyncio<br>
     * Transfer status codes<br>
     * <i>native declaration : /usr/include/limits.h:716</i><br>
     * enum values
     */
    public static interface libusb_transfer_status {

        /**
         * Transfer completed without error. Note that this does not
         * indicate<br>
         * that the entire amount of requested data was transferred.<br>
         * Transfer completed without error. Note that this does not
         * indicate<br>
         * that the entire amount of requested data was transferred.<br>
         * <i>native declaration : /usr/include/limits.h:719</i>
         */
        public static final int LIBUSB_TRANSFER_COMPLETED = 0;
        /**
         * Transfer failed<br>
         * Transfer failed<br>
         * <i>native declaration : /usr/include/limits.h:722</i>
         */
        public static final int LIBUSB_TRANSFER_ERROR = 1;
        /**
         * Transfer timed out<br>
         * Transfer timed out<br>
         * <i>native declaration : /usr/include/limits.h:725</i>
         */
        public static final int LIBUSB_TRANSFER_TIMED_OUT = 2;
        /**
         * Transfer was cancelled<br>
         * Transfer was cancelled<br>
         * <i>native declaration : /usr/include/limits.h:728</i>
         */
        public static final int LIBUSB_TRANSFER_CANCELLED = 3;
        /**
         * For bulk/interrupt endpoints: halt condition detected (endpoint<br>
         * stalled). For control endpoints: control request not supported.<br>
         * For bulk/interrupt endpoints: halt condition detected (endpoint<br>
         * stalled). For control endpoints: control request not supported.<br>
         * <i>native declaration : /usr/include/limits.h:732</i>
         */
        public static final int LIBUSB_TRANSFER_STALL = 4;
        /**
         * Device was disconnected<br>
         * Device was disconnected<br>
         * <i>native declaration : /usr/include/limits.h:735</i>
         */
        public static final int LIBUSB_TRANSFER_NO_DEVICE = 5;
        /**
         * Device sent more data than requested<br>
         * Device sent more data than requested<br>
         * <i>native declaration : /usr/include/limits.h:738</i>
         */
        public static final int LIBUSB_TRANSFER_OVERFLOW = 6;
    }

}
