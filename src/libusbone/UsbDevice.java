/*
 * Copyright 2019 peter.
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
package libusbone;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 *
 * @author peter
 */
public class UsbDevice {

    private static LibusboneLibrary usb;
    private static LibusboneLibrary.libusb_context context;
    private static LibusboneLibrary.libusb_device_handle dev_handle;
    private static LibusboneLibrary.libusb_device usb_device;
    public static libusb_device_descriptor descriptor;
    //private libusb_device_descriptor descriptor;

    public UsbDevice(LibusboneLibrary lib, LibusboneLibrary.libusb_context libUsbContext, LibusboneLibrary.libusb_device libusbDevice) {

        this.usb = lib;
        this.context = libUsbContext;
        this.usb_device = libusbDevice;
        descriptor = getDeviceDescriptor(lib, libusbDevice);
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
        if (dev_handle == null) {
            return "";
        }
        usb.libusb_get_string_descriptor_ascii(dev_handle, desc_index, data, data.length);

        // Convert C string to Java String
        int len = 0;
        while (data[len] != 0) {
            ++len;
        }
        return new String(data, 0, len);
    }

    private static libusb_device_descriptor getDeviceDescriptor(
            LibusboneLibrary lib, LibusboneLibrary.libusb_device libusbDevice) {
        libusb_device_descriptor desc = new libusb_device_descriptor();
        usb.libusb_get_device_descriptor(libusbDevice, desc);
        //System.out.println(toHexString(desc.idVendor) + " "
        //        + toHexString(desc.idProduct) + " num conf="
        //        + desc.bNumConfigurations);
        return desc;
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

    public void close() {

        System.out.println("About to exit context");
        usb.libusb_exit(context);
        System.out.println("bye");
    }

    public static void LIBUSB_open(boolean withContext) {
        if (withContext) {
            dev_handle = usb.libusb_open_device_with_vid_pid(context, descriptor.idVendor, descriptor.idProduct);
        } else {
            dev_handle = usb.libusb_open_device_with_vid_pid(null, descriptor.idVendor, descriptor.idProduct);

        }

    }
    
    /**
     * Claim an interface on a given device handle.
     * 
     * You must claim the interface you wish to use before you can perform I/O on any of its endpoints.
     * 
     * It is legal to attempt to claim an already-claimed interface, in which case libusb just returns
     * 0 without doing anything.
     * 
     * Claiming of interfaces is a purely logical operation; it does not cause any requests to be sent
     * over the bus. Interface claiming is used to instruct the underlying operating system that your
     * application wishes to take ownership of the interface.
     * 
     * This is a non-blocking function.
     *
     * @param interface_number the bInterfaceNumber of the interface you wish to claim
     * @throws LibUsbNotFoundException if the requested interface does not exist
     * @throws LibUsbBusyException     if another program or driver has claimed the interface
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public int claim_interface(int interface_number)  {
        return usb.libusb_claim_interface(dev_handle, interface_number);
    }

    /**
     * Release an interface previously claimed with libusb_claim_interface().
     * 
     * You should release all claimed interfaces before closing a device handle.
     * 
     * This is a blocking function. A SET_INTERFACE control request will be sent to the device,
     * resetting interface state to the first alternate setting.
     *
     * @param interface_number the bInterfaceNumber of the previously-claimed interface
     * @throws LibUsbNotFoundException if the interface was not claimed
     * @throws LibUsbBusyException     if another program or driver has claimed the interface
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public int release_interface(int interface_number)  {
        return usb.libusb_release_interface(dev_handle, interface_number);
        
    }

    /**
     * Activate an alternate setting for an interface.
     * 
     * The interface must have been previously claimed with libusb_claim_interface().
     * 
     * You should always use this function rather than formulating your own SET_INTERFACE control request.
     * This is because the underlying operating system needs to know when such changes happen.
     * 
     * This is a blocking function.
     *
     * @param interface_number  the bInterfaceNumber of the previously-claimed interface
     * @param alternate_setting the bAlternateSetting of the alternate setting to activate
     * @throws LibUsbNotFoundException if the interface was not claimed, or the requested alternate setting does
     *                                 not exist
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public int set_interface_alt_setting(int interface_number, int alternate_setting)  {
        return usb.libusb_set_interface_alt_setting(dev_handle, interface_number, alternate_setting);
    }

    /**
     * Clear the halt/stall condition for an endpoint.
     * 
     * Endpoints with halt status are unable to receive or transmit data until the halt condition is stalled.
     * 
     * You should cancel all pending transfers before attempting to clear the halt condition.
     * 
     * This is a blocking function.
     *
     * @param endpoint the endpoint to clear halt status
     * @throws LibUsbNotFoundException if the endpoint does not exist
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public int clear_halt(byte endpoint) {
        return usb.libusb_clear_halt(dev_handle, endpoint);
        
    }
    
     /**
     * Perform a USB port reset to reinitialize a device.
     * 
     * The system will attempt to restore the previous configuration and alternate settings
     * after the reset has completed.
     * 
     * If the reset fails, the descriptors change, or the previous state cannot be restored,
     * the device will appear to be disconnected and reconnected. This means that the device
     * handle is no longer valid (you should close it) and rediscover the device. A return
     * code of LIBUSB_ERROR_NOT_FOUND indicates when this is the case.
     * 
     * This is a blocking function which usually incurs a noticeable delay.
     *
     * @throws LibUsbNotFoundException if re-enumeration is required, or if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public int reset_device() {
        return usb.libusb_reset_device(dev_handle);
    }
    /**
     * Determine if a kernel driver is active on an interface.
     * 
     * If a kernel driver is active, you cannot claim the interface, and libusb will be unable to perform I/O.
     *
     * @param interface_number the interface to check
     * @return true if kernel driver is active
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public int kernel_driver_active(int interface_number) {
        return usb.libusb_kernel_driver_active(dev_handle, interface_number);
        
    }

    /**
     * Detach a kernel driver from an interface.
     * 
     * If successful, you will then be able to claim the interface and perform I/O.
     *
     * @param interface_number the interface to detach the driver from
     * @throws LibUsbInvalidParameterException if the interface does not exist
     * @throws LibUsbNotFoundException         if no kernel driver was active
     * @throws LibUsbNoDeviceException         if the device has been disconnected
     * @throws LibUsbOtherException            if another USB error occurred
     */
    
    public int detach_kernel_driver(int interface_number) {
        return usb.libusb_detach_kernel_driver(dev_handle, interface_number);
    }

    /**
     * Re-attach an interface's kernel driver, which was previously detached using libusb_detach_kernel_driver().
     *
     * @param interface_number the interface to attach the driver from
     * @throws LibUsbInvalidParameterException if the interface does not exist
     * @throws LibUsbNotFoundException         if no kernel driver was active
     * @throws LibUsbBusyException             if the driver cannot be attached because the interface is claimed by a
     *                                         program or driver
     * @throws LibUsbNoDeviceException         if the device has been disconnected
     * @throws LibUsbOtherException            if another USB error occurred
     */
    public int attach_kernel_driver(int interface_number)  {
        return usb.libusb_attach_kernel_driver(dev_handle, interface_number);
        
    }

    /**
     * Perform a USB control write.
     * 
     * The direction of the transfer is inferred from the bmRequestType field of the setup packet.
     * 
     * The wValue, wIndex and wLength fields values should be given in host-endian byte order.
     *
     * @param bmRequestType the request type field for the setup packet
     * @param bRequest      the request field for the setup packet
     * @param wValue        the value field for the setup packet
     * @param wIndex        the index field for the setup packet
     * @param data          data buffer to send
     * @param wLength       the length field for the setup packet. The data buffer should be at least this size.
     * @param timeout       timeout (in milliseconds) that this function should wait before giving up due to no response
     *                      being received. For an unlimited timeout, use value 0.
     * @throws LibUsbTimeoutException      if the transfer timed out
     * @throws LibUsbPipeException         if the control request was not supported by the device
     * @throws LibUsbNoDeviceException     if the device has been disconnected
     * @throws LibUsbTransmissionException if all data could not be sent
     * @throws LibUsbOtherException        if another USB error occurred
     */
    public int control_write(byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data,
                              short wLength, int timeout) {
        return usb.libusb_control_transfer(dev_handle, bmRequestType, bRequest, wValue, wIndex, data, wLength, timeout);
        
    }

    /**
     * Perform a USB control read.
     * 
     * The direction of the transfer is inferred from the bmRequestType field of the setup packet.
     * 
     * The wValue, wIndex and wLength fields values should be given in host-endian byte order.
     *
     * @param bmRequestType the request type field for the setup packet
     * @param bRequest      the request field for the setup packet
     * @param wValue        the value field for the setup packet
     * @param wIndex        the index field for the setup packet
     * @param data          a suitably-sized data buffer for input
     * @param wLength       the length field for the setup packet. The data buffer should be at least this size.
     * @param timeout       timeout (in milliseconds) that this function should wait before giving up due to no response
     *                      being received. For an unlimited timeout, use value 0.
     * @return the number of bytes actually transferred
     * @throws LibUsbTimeoutException  if the transfer timed out
     * @throws LibUsbPipeException     if the control request was not supported by the device
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public int control_read(byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data,
                            short wLength, int timeout) {
        return usb.libusb_control_transfer(dev_handle, bmRequestType, bRequest, wValue, wIndex, data, (short) data.length, timeout);
        
    }

    /**
     * Perform a USB bulk write.
     * 
     * The direction of the transfer is inferred from the direction bits of the endpoint address.
     * 
     * Check transferred bytes when dealing with a timeout error code. libusb may have to split your transfer
     * into a number of chunks to satisfy underlying O/S requirements, meaning that the timeout may expire
     * after the first few chunks have completed. libusb is careful not to lose any data that may have been
     * transferred; do not assume that timeout conditions indicate a complete lack of I/O.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data     a suitably-sized data buffer for either input or output (depending on endpoint)
     * @param timeout  timeout (in milliseconds) that this function should wait before giving up due to no
     *                 response being received. For an unlimited timeout, use value 0.
     * @throws LibUsbTimeoutException      if the transfer timed out
     * @throws LibUsbPipeException         if the endpoint halted
     * @throws LibUsbNoDeviceException     if the device has been disconnected
     * @throws LibUsbTransmissionException if all data could not be sent
     * @throws LibUsbOtherException        if another USB error occurred
     */
    public int bulk_write(int endpoint, byte[] data, int timeout) {
        //int[] transferred = new int[1];
        IntByReference transferred = new IntByReference();
        int rc = usb.libusb_bulk_transfer(dev_handle, (byte) endpoint, data, data.length, transferred, timeout);
        if (transferred.getValue() != data.length) {
            return -1;
        }
        return 0;
        
    }

    /**
     * Perform a USB bulk read.
     * 
     * The direction of the transfer is inferred from the direction bits of the endpoint address.
     * 
     * The length field indicates the maximum length of data you are expecting to receive.
     * If less data arrives than expected, this function will return that data, so be sure to check the
     * return value.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data     a suitably-sized data buffer for either input or output (depending on endpoint)
     * @param timeout  timeout (in milliseconds) that this function should wait before giving up due to no
     *                 response being received. For an unlimited timeout, use value 0.
     * @return Number of bytes received
     * @throws LibUsbTimeoutException  if the transfer timed out
     * @throws LibUsbPipeException     if the endpoint halted
     * @throws LibUsbOverflowException if the device offered more data, see Packets and overflows
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public IntByReference bulk_read(int endpoint, byte[] data, int timeout) {
        //int[] transferred = new int[1];
        IntByReference transferred = new IntByReference();
        int rc = usb.libusb_bulk_transfer(dev_handle, (byte) endpoint, data, data.length, transferred, timeout);
        if (rc < 0) {
            return null;
        }
        return transferred;
    }

    /**
     * Perform a USB interrupt write.
     * 
     * The direction of the transfer is inferred from the direction bits of the endpoint address.
     * 
     * You should also check the transferred parameter for interrupt writes. Not all of the data may have been written.
     * 
     * Also check transferred when dealing with a timeout error code. libusb may have to split your transfer into
     * a number of chunks to satisfy underlying O/S requirements, meaning that the timeout may expire after the
     * first few chunks have completed. libusb is careful not to lose any data that may have been transferred;
     * do not assume that timeout conditions indicate a complete lack of I/O.
     * 
     * The default endpoint bInterval value is used as the polling interval.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data     a suitably-sized data buffer for output
     * @param timeout  timeout (in milliseconds) that this function should wait before giving up due to no response
     *                 being received. For an unlimited timeout, use value 0.
     * @throws LibUsbTimeoutException      if the transfer timed out
     * @throws LibUsbPipeException         if the endpoint halted
     * @throws LibUsbNoDeviceException     if the device has been disconnected
     * @throws LibUsbTransmissionException if all data could not be sent
     * @throws LibUsbOtherException        if another USB error occurred
     */
    public int interrupt_write(int endpoint, byte[] data, int timeout) {
        //int[] transferred = new int[1];
        IntByReference transferred = new IntByReference();
        int rc = usb.libusb_interrupt_transfer(dev_handle, (byte) endpoint, data, data.length, transferred, timeout);
        if (transferred.getValue() != data.length) {
            return -1;
        }
        return 0;
    }

    /**
     * Perform a USB interrupt read.
     * 
     * The direction of the transfer is inferred from the direction bits of the endpoint address.
     * 
     * The length field indicates the maximum length of data you are expecting to receive.
     * If less data arrives than expected, this function will return that data, so be sure to check the return value.
     * 
     * Also check transferred when dealing with a timeout error code. libusb may have to split your transfer into
     * a number of chunks to satisfy underlying O/S requirements, meaning that the timeout may expire after the
     * first few chunks have completed. libusb is careful not to lose any data that may have been transferred;
     * do not assume that timeout conditions indicate a complete lack of I/O.
     * 
     * The default endpoint bInterval value is used as the polling interval.
     *
     * @param endpoint the address of a valid endpoint to communicate with
     * @param data     a suitably-sized data buffer for input
     * @param timeout  timeout (in milliseconds) that this function should wait before giving up due to no response
     *                 being received. For an unlimited timeout, use value 0.
     * @return Number of bytes received
     * @throws LibUsbTimeoutException  if the transfer timed out
     * @throws LibUsbPipeException     if the endpoint halted
     * @throws LibUsbOverflowException if the device offered more data, see Packets and overflows
     * @throws LibUsbNoDeviceException if the device has been disconnected
     * @throws LibUsbOtherException    if another USB error occurred
     */
    public IntByReference interrupt_read(int endpoint, byte[] data, int timeout) {
        //int[] transferred = new int[1];
        IntByReference a = new IntByReference();
        int rc = usb.libusb_interrupt_transfer(dev_handle, (byte) endpoint, data, data.length, a, timeout);
        if (rc == 0) return a;
        else return null;
    }

    @Override
    public String toString() {
        return "UsbDevice {" +
                " bus_number=" + get_bus_number() +
                " address=" + get_address() +
                "}";
    }

}
