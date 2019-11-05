package com.grack.javausb.jna;

import com.sun.jna.Callback;
import java.nio.ByteBuffer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface LibUSBXNative extends Library {

    LibUSBXNative INSTANCE = (LibUSBXNative) Native.load("usb-1.0", LibUSBXNative.class);

    // int LIBUSB_CALL libusb_init(libusb_context **ctx);
    int libusb_init(PointerByReference ctx);

    // void LIBUSB_CALL libusb_set_debug(libusb_context *ctx, int level);
    void libusb_set_debug(Pointer ctx, int level);

    // void LIBUSB_CALL libusb_exit(libusb_context *ctx);
    void libusb_exit(Pointer ctx);

    // ssize_t LIBUSB_CALL libusb_get_device_list(libusb_context *ctx,
    // libusb_device ***list);
    int libusb_get_device_list(Pointer ctx, PointerByReference list);

    // void LIBUSB_CALL libusb_free_device_list(libusb_device **list, int
    // unref_devices);
    void libusb_free_device_list(Pointer list, boolean unref_devices);

    // void LIBUSB_CALL libusb_unref_device(libusb_device *dev);
    void libusb_unref_device(Pointer dev);

    // int LIBUSB_CALL libusb_get_device_descriptor(libusb_device *dev, struct
    // libusb_device_descriptor *desc);
    int libusb_get_device_descriptor(Pointer dev, libusb_device_descriptor[] desc);

    // int LIBUSB_CALL libusb_open(libusb_device *dev, libusb_device_handle
    // **handle);
    int libusb_open(Pointer dev, PointerByReference handle);

    int libusb_get_bus_number(Pointer usb_device);

    int libusb_get_device_address(Pointer usb_device);

    int libusb_get_max_packet_size(Pointer usb_device, int endpoint);

    int libusb_get_max_iso_packet_size(Pointer usb_device, int endpoint);

    // int LIBUSB_CALL libusb_get_config_descriptor(libusb_device *dev, uint8_t
    // config_index, struct libusb_config_descriptor **config);
    int libusb_get_config_descriptor(Pointer dev, int config_index, PointerByReference config);

    // void LIBUSB_CALL libusb_free_config_descriptor( struct
    // libusb_config_descriptor *config);
    void libusb_free_config_descriptor(Pointer config);

    // int LIBUSB_CALL libusb_set_configuration(libusb_device_handle *dev, int configuration);
    int libusb_set_configuration(Pointer handle, int configuration);

    int libusb_kernel_driver_active(Pointer dev_handle, int interface_number);

    int libusb_detach_kernel_driver(Pointer dev_handle, int interface_number);

    // int LIBUSB_CALL libusb_claim_interface(libusb_device_handle *dev, int interface_number);
    int libusb_claim_interface(Pointer handle, int interface_number);

    int libusb_set_interface_alt_setting(Pointer dev_handle, int interface_number, int alternate_setting);

    int libusb_release_interface(Pointer dev_handle, int interface_number);

    int libusb_reset_device(Pointer dev_handle);

    // int LIBUSB_CALL libusb_control_transfer(libusb_device_handle *dev_handle,
    // uint8_t request_type, uint8_t bRequest, uint16_t wValue, uint16_t wIndex,
    // unsigned char *data, uint16_t wLength, unsigned int timeout);
    int libusb_control_transfer(Pointer dev_handle, byte bmRequestType, byte bRequest, short wValue, short wIndex, ByteBuffer data,
            short wLength, int timeout);

    int libusb_control_transfer(Pointer dev_handle, byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data, short wLength, int timeout);

    // int LIBUSB_CALL libusb_bulk_transfer ( struct libusb_device_handle *
    // dev_handle, unsigned char endpoint, unsigned char * data, int length, int *
    // transferred, unsigned int timeout)
    int libusb_bulk_transfer(Pointer handle, byte endpoint, ByteBuffer data, int length, IntByReference transferred, int timeout);

    int libusb_bulk_transfer(Pointer handle, byte endpoint, byte[] data, int length, IntByReference transferred, int timeout);

    // void LIBUSB_CALL libusb_close(libusb_device_handle *dev_handle);
    void libusb_close(Pointer handle);

    // int LIBUSB_CALL libusb_get_string_descriptor_ascii(libusb_device_handle
    // *dev, uint8_t desc_index, unsigned char *data, int length);
    int libusb_get_string_descriptor_ascii(Pointer dev, byte desc_index, byte[] data, int length);

    Pointer libusb_alloc_transfer(int iso_packets);

    interface Libusb_transfer_cb_fn extends Callback {

        void invoke(Pointer transfer);
    }

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

        //private ByteBuffer urbBuf;
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
            //libusbSize = libusbBaseSize + maxPackets * packetDescSize;
            this.MAX_PACKET_SIZE = maxPacketSize;
            //int urbSize = libusbBaseSize + maxPackets * packetDescSize;
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

        /*
        public void setNativeLibusbAddr(Pointer adress) {
            libusbBuf = adress;
            urbBuf = libusbBuf.getByteBuffer(0, libusbBaseSize + maxPackets * packetDescSize);
        }
         */
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

}
