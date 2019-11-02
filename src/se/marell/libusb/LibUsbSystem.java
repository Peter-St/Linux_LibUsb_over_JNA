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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.marell.libusb.jna.LibUsb;
import se.marell.libusb.jna.libusb_error;

import java.util.ArrayList;
import java.util.List;

public class LibUsbSystem implements UsbSystem {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private Pointer context;
    private LibUsb usb = LibUsb.INSTANCE;
    
    

    public LibUsbSystem(boolean setContext) {
        if (setContext) {
            Pointer[] p = new Pointer[1];
            usb.libusb_init(p);
            context = p[0];
        } else {
            usb.libusb_init(null);
        }
    }

    /**
     * Set message verbosity.
     * 
     * Level 0: no messages ever printed by the library (default)
     * Level 1: error messages are printed to stderr
     * Level 2: warning and error messages are printed to stderr
     * Level 3: informational messages are printed to stdout, warning and error messages are printed to stderr
     * The default level is 0, which means no messages are ever printed. If you choose to increase the message
     * verbosity level, ensure that your application does not close the stdout/stderr file descriptors.
     * 
     * You are advised to set level 3. libusb is conservative with its message logging and most of the time, will
     * only log messages that explain error conditions and other oddities. This will help you debug your software.
     * 
     * If the LIBUSB_DEBUG environment variable was set when libusb was initialized, this function does nothing:
     * the message verbosity is fixed to the value in the environment variable.
     * 
     * If libusb was compiled without any message logging, this function does nothing: you'll never get any messages.
     * 
     * If libusb was compiled with verbose debug message logging, this function does nothing: you'll always get
     * messages from all levels.
     *
     * @param setContext true if a non-null context shall be used in libusb_init
     * @param debugLevel Debug level: 0=none ... 3=most
     */
    public LibUsbSystem(boolean setContext, int debugLevel) {
        this(setContext);
        usb.libusb_set_debug(context, debugLevel);
    }
    
    

    @Override
    public List<UsbDevice> visitUsbDevices(UsbDeviceVisitor visitor) throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException {
        Pointer[] pa = new Pointer[1];
        int rc = usb.libusb_get_device_list(context, pa);
        if (rc <= 0) {
            switch (rc) {
                case libusb_error.ERROR_NO_MEM:
                    throw new OutOfMemoryError("ERROR_NO_MEM when calling libusb_get_device_list");
                default:
                    throw new LibUsbOtherException(rc);
            }
        }
        Pointer device_list = pa[0];
        List<UsbDevice> devices = new ArrayList<UsbDevice>();
        Pointer[] parr = device_list.getPointerArray(0);
        log.debug("Found " + parr.length + " devices");

        for (Pointer usb_device : parr) {
            devices.add(new UsbDevice(usb, usb_device));
        }

        List<UsbDevice> targetDevices = visitor.visitDevices(devices);

        // unref all other devices
        for (UsbDevice d : devices) {
            if (targetDevices == null) break;
            if (!targetDevices.contains(d)) {
                usb.libusb_unref_device(d.get_usb_device());
            }
        }

        // Free the device list itself
        usb.libusb_free_device_list(context, device_list, 0);

        return targetDevices;
    }

    @Override
    public void cleanup() {
        usb.libusb_exit(context);
    }

    @Override
    public LibUsb getLibUsb() {
        return usb;
    }
    
    @Override
    public Pointer getContext() {
        return context;
    }
    
    
    
}
