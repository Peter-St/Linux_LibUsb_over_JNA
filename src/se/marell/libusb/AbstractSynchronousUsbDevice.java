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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractSynchronousUsbDevice {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected static final int DEFAULT_RW_TIMEOUT_MS = 100;
    protected UsbSystem us;
    protected int deviceNumber;
    protected UsbDevice device;

    public AbstractSynchronousUsbDevice(UsbSystem us, int deviceNumber) {
        this.us = us;
        this.deviceNumber = deviceNumber;
    }

    protected UsbDevice getUsbDevice(int vendor, int product, int deviceNumber) throws LibUsbException {
        List<UsbDevice> devices = us.visitUsbDevices(new VendorProductVisitor(vendor, product, deviceNumber));
        if (devices.isEmpty()) {
            return null; // Device not found
        }
        device = devices.get(0);
        try {
            device.open();
            log.trace("device opened");
        } catch (LibUsbException e) {
            log.warn("open failed:" + e.getMessage());
            return null;
        }

        try {
            device.detach_kernel_driver(0);
            log.trace("kernel driver detached");
        } catch (LibUsbException e) {
            log.warn("detach_kernel_driver failed:" + e.getMessage() + ". Ignored.");
        }

        try {
            device.claim_interface(0);
            log.trace("claim_interface succeeded");
        } catch (LibUsbException e) {
            log.warn("claim_interface failed:" + e.getMessage());
            return null;
        }

        return device;
    }

    public int getDeviceNumber() {
        return deviceNumber;
    }
}
