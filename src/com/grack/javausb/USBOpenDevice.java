package com.grack.javausb;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.sun.jna.Pointer;

public class USBOpenDevice implements AutoCloseable, Closeable {

    private USBDevice device;
    private Pointer handle;
    private USB usb;
    private FinalizerReference finalizer;

    private static final Logger logger = Logger.getLogger(USBDevice.class.getName());

    public USBOpenDevice(USB usb, USBDevice device, Pointer handle) {
        this.usb = usb;
        this.device = device;
        this.handle = handle;

        finalizer = usb.trackFinalizer(this, new LibUSBOpenDeviceFinalizer(handle));
    }

    private static class LibUSBOpenDeviceFinalizer implements Finalizer {

        private Pointer handle;

        public LibUSBOpenDeviceFinalizer(Pointer handle) {
            this.handle = handle;
        }

        @Override
        public void cleanup() {
            logger.info("Cleanup: open device");
            USBNative.closeDevice(handle);
        }
    }

    public String getStringDescriptionAscii(int index) throws USBException {
        return USBNative.getStringDescriptionAscii(handle, (byte) index);
    }

    public String manufacturer() throws USBException {
         if (device.getDescriptor().iManufacturer == 0) {
            return null;
        }

        return USBNative.getStringDescriptionAscii(handle, device.getDescriptor().iManufacturer);
    }

    public String product() throws USBException {
        if (device.getDescriptor().iProduct == 0) {
            return null;
        }

        return USBNative.getStringDescriptionAscii(handle, device.getDescriptor().iProduct);
    }

    public String serialNumber() throws USBException {
        if (device.getDescriptor().iSerialNumber == 0) {
            return null;
        }

        return USBNative.getStringDescriptionAscii(handle, device.getDescriptor().iSerialNumber);
    }

    public InputStream openBulkReadEndpoint(USBEndpoint endpoint) {
        Preconditions.checkArgument(endpoint.direction() == USBEndpointDirection.IN);
        return new USBBulkEndpointInputStream(handle, endpoint.address());
    }

    public OutputStream openBulkWriteEndpoint(USBEndpoint endpoint) {
        Preconditions.checkArgument(endpoint.direction() == USBEndpointDirection.OUT);
        return new USBBulkEndpointOutputStream(handle, endpoint.address());
    }

    @Override
    public synchronized void close() {
        if (finalizer != null) {
            usb.forceFinalization(finalizer);
            finalizer = null;
            handle = null;
            usb = null;
        }
    }

    public void activate(USBConfiguration config) throws USBException {
        USBNative.setConfiguration(handle, config.number());
    }

    public boolean kernel_driver_active(USBInterface config) throws USBException {

        int rc = USBNative.kernel_driver_active(handle, config.number());
        if (rc < 0) {
            throw new USBException(rc);
        } else if (rc == 1) {
            return true;
        } else {
            return false;
        }

    }

    public boolean detach_kernel_driver(USBInterface config) throws USBException {
        int rc = USBNative.detach_kernel_driver(handle, config.number());
        if (rc < 0) {
            throw new USBException(rc);
        } else if (rc == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean claim(USBInterface config) throws USBException {
        int rc = USBNative.claimInterface(handle, config.number());
        if (rc < 0) {
            throw new USBException(rc);
        } else if (rc == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean set_interface_alt_setting(int interface_number, int alternate_setting) throws USBException {
        int rc = USBNative.libusb_set_interface_alt_setting(handle, interface_number, alternate_setting);
        if (rc < 0) {
            throw new USBException(rc);
        } else if (rc == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean release_interface(USBInterface config) throws USBException {
        int rc = USBNative.libusb_release_interface(handle, config.number());
        if (rc < 0) {
            throw new USBException(rc);
        } else if (rc == 0) {
            return true;
        } else {
            return false;
        }
    }

    public int sendControlTransfer(int requestType, byte request, int value, int index, byte[] buffer)
            throws USBException {
        return sendControlTransfer(requestType, request, value, index, buffer, 0, buffer.length);
    }

    public int sendControlTransfer(int requestType, byte request, int value, int index, byte[] buffer, int offset, int length)
            throws USBException {
        return USBNative.sendControlTransfer(handle, requestType, request, value, index, buffer, offset, length);
    }
    
    public int sendControlTransfer(byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data,
                            short wLength, int timeout)  throws USBException  {
        return USBNative.sendControlTransfer(handle, bmRequestType, bRequest, wValue, wIndex, data, (short) data.length, timeout);
        
    }
}
