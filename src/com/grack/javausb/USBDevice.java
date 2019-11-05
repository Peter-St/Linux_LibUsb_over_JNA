package com.grack.javausb;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.grack.javausb.jna.libusb_config_descriptor;
import com.grack.javausb.jna.libusb_device_descriptor;
import com.sun.jna.Pointer;

public class USBDevice {

    private Pointer dev;
    private USB usb;
    private libusb_device_descriptor descriptor;

    private static final Logger logger = Logger.getLogger(USBDevice.class.getName());

    USBDevice(USB usb, libusb_device_descriptor descriptor, Pointer dev) {
        this.usb = usb;
        this.descriptor = descriptor;
        this.dev = dev;

        usb.trackFinalizer(this, new LibUSBDeviceFinalizer(dev));
    }

    private static class LibUSBDeviceFinalizer implements Finalizer {

        private Pointer dev;

        public LibUSBDeviceFinalizer(Pointer dev) {
            this.dev = dev;
        }

        @Override
        public void cleanup() {
            logger.info("Cleanup: device");
            USBNative.unrefDevice(dev);
        }
    }

    public int vendor() {
        return descriptor.idVendor & 0xffff;
    }

    public int product() {
        return descriptor.idProduct & 0xffff;
    }

    public int numConfigurations() {
        return descriptor.bNumConfigurations;
    }
    
    public libusb_device_descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Get the number of the bus that a device is connected to.
     * @return the bus number
     */
    public int get_bus_number() {
        return USBNative.get_bus_number(dev);
    }

    /**
     * Get the address of the device on the bus it is connected to.
     * @return the device address
     */
    public int get_address() {
        return USBNative.get_bus_number(dev);
    }

    /**
     * Lists device configurations by sending requests to the device for each
     * individual descriptor. Each call to configurations returns a brand new
     * set of {@link USBConfiguration} objects.
     */
    public Iterable<USBConfiguration> configurations() {
        return new Iterable<USBConfiguration>() {
            @Override
            public Iterator<USBConfiguration> iterator() {
                List<USBConfiguration> configs = Lists.newArrayList();
                try {
                    for (int i = 0; i < descriptor.bNumConfigurations; i++) {
                        libusb_config_descriptor descriptor;
                        descriptor = USBNative.getConfigDescriptor(dev, i);
                        configs.add(new USBConfiguration(usb, USBDevice.this, descriptor));
                    }
                } catch (USBException e) {
                    throw new USBRuntimeException(e);
                }

                return configs.iterator();
            }
        };
    }

    /**
     * Opens a device.
     */
    public USBOpenDevice open() throws USBException {
        return new USBOpenDevice(usb, this, USBNative.openDevice(dev));
    }
    
    public USBOpenDevice open_device_with_vid_pid() {        
        return new USBOpenDevice(usb, this, USBNative.open_device_with_vid_pid(usb.getContext(), descriptor.idVendor, descriptor.idProduct));
    }

    public Pointer getContext() {
        return usb.getContext();
    }
    
    @Override
    public String toString() {
        return String.format("Device %04x:%04x", vendor(), product());
    }
}
