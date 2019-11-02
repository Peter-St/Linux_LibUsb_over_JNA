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

import com.sun.jna.Pointer;
import se.marell.libusb.jna.LibUsb;

import java.util.List;

/**
 * This API represents an object-oriented layer on top of the naked JNA libusb-api functions.
 */
public interface UsbSystem {
    /**
     * Get native library object.
     *
     * @return Native library object
     */
    LibUsb getLibUsb();

    interface UsbDeviceVisitor {
        /**
         * @param allDevices list of USB devices currently attached to the system
         * @return UsbDevice you want to open. You are expected to call close() on the returned object when you are done with it.
         */
        List<UsbDevice> visitDevices(List<UsbDevice> allDevices);
        
    }

    /**
     * Find and select USB device(s).
     *
     * @param visitor Implemented by caller used by this method to identify USB-device to open and return
     * @return List of USB devices of interest
     * @throws LibUsbNoDeviceException   if the device has been disconnected
     * @throws LibUsbPermissionException if the user has insufficient permissions
     * @throws LibUsbOtherException      if another USB error occurred
     */
    List<UsbDevice> visitUsbDevices(UsbDeviceVisitor visitor) throws LibUsbNoDeviceException,
            LibUsbPermissionException,
            LibUsbOtherException;
    
    
    
    
    
    /**
     * Deinitialize libusb.
     * 
     * Should be called after closing all open devices and before your application terminates.
     */
    void cleanup();
    
    Pointer getContext();
}
