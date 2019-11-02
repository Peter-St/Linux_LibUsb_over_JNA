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

import java.util.Arrays;

/**
 * Driver for USB mouse. It is tested with a Microsoft Basic Optical Mouse v2.0.
 * It listens to mouse events and returns every time there is a message, hanging waiting for next message.
 * 
 * The driver communicates with the USB board using synchronous commands.
 * 
 * Run in a separate thread in a simple loop:
 * <pre>
 * forever {
 *  usbdev.poll
 *  usbdev.get inputs
 * }
 * </pre>
 */
public class SynchronousUsbMouse extends AbstractSynchronousUsbDevice {
    private UsbDevice device;
    private boolean[] buttons = new boolean[8];
    private int x;
    private int y;
    private int wheel;
    private byte[] dataBuffer = new byte[4];
    private int readTimeout = DEFAULT_RW_TIMEOUT_MS;
    private int vendorId;
    private int productId;

    /**
     * @param us           The UsbSystem
     * @param vendorId     Vendor id
     * @param productId    Product id
     * @param deviceNumber 0 for first board, 1 for 2nd etc.
     */
    public SynchronousUsbMouse(UsbSystem us, int vendorId, int productId, int deviceNumber) {
        super(us, deviceNumber);
        this.vendorId = vendorId;
        this.productId = productId;
    }

    public boolean getButton(int n) {
        return buttons[n];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWheel() {
        return wheel;
    }

    /**
     * Hangs waiting for input from mouse. If case of some kind of error, return false.
     * Try to reconnect if device is disconnected.
     *
     * @return true if device is connected and data is available
     */
    public boolean poll() {
        if (device == null) {
            try {
                device = getUsbDevice(vendorId, productId, deviceNumber);
            } catch (LibUsbException e) {
                return false;
            }
            if (device == null) {
                return false;
            }
        }

        Arrays.fill(dataBuffer, (byte) 0);
        while (true) {
            try {
                device.interrupt_read(0x81, dataBuffer, DEFAULT_RW_TIMEOUT_MS);
                break;
            } catch (LibUsbTimeoutException e) {
                // nop, read again
            } catch (LibUsbException e) {
                log.info("read failed:" + e.getClass().getSimpleName() + ":" + e.getMessage());
                device.close();
                device = null;
                return false;
            }
        }

        // Unpack byte-array from mouse to variables
        byte b = dataBuffer[0];
        for (int i = 0; i < 8; ++i) {
            buttons[i] = (b & (1 << i)) != 0;
        }
        x = dataBuffer[1];
        y = dataBuffer[2];
        wheel = dataBuffer[3];

        return true;
    }

    /**
     * Get current read timeout in ms.
     *
     * @return timeout in ms, 0=no timeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Set new read timeout in ms.
     *
     * @param readTimeout Timeout in ms, 0=no timeout
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
