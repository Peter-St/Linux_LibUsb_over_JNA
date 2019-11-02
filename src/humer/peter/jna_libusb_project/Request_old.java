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
package com.humer.peter.jna_libusb_project;

import com.sun.jna.CallbackReference;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import java.io.IOException;
import se.marell.libusb.UsbDevice;
import se.marell.libusb.jna.LibUsb;
import se.marell.libusb.jna.LibUsb.Libusb_transfer;

/**
 *
 * @author peter
 */
public class Request_old extends Libusb_transfer {

    public boolean initialized;
    public boolean queued;
    public Memory buffer;
    private UsbDevice camDevice;
    private static int position_of_libusb_transfer_usercontext;
    private static int PACKETS_PER_REQUEST;
    private static int MAX_PACKET_SIZE;

    public Request_old(UsbDevice dev, Pointer nativePointer, int maxPacketsPerRequest, int maxSize) {
        super(maxPacketsPerRequest, maxSize, nativePointer);
        this.camDevice = dev;
        this.PACKETS_PER_REQUEST = maxPacketsPerRequest;
        this.MAX_PACKET_SIZE = maxSize;
        buffer = new Memory(maxPacketsPerRequest * maxSize);
    }

    public int initialize(byte addr, int usercontext, Pointer callback) {
        if (queued) {
            throw new IllegalStateException();
        }

        position_of_libusb_transfer_usercontext = setUserContext(usercontext);
        setBuffer(buffer);
        setDev_handle(camDevice.get_dev_handle());
        setCallback(callback);
        setNumberOfPackets(PACKETS_PER_REQUEST);
        setEndpoint(addr);
        setTransferLength(PACKETS_PER_REQUEST * MAX_PACKET_SIZE);
        setType(LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS.getValue());
        setTimeout(20);
        for (int packetNo = 0; packetNo < PACKETS_PER_REQUEST; packetNo++) {
            setPacketLength(packetNo, MAX_PACKET_SIZE);
            setPacketActualLength(packetNo, 0);
            setPacketStatus(packetNo, -1);
        }

        initialized = true;

        return position_of_libusb_transfer_usercontext;
    }

    public void submit() throws IOException {
        if (!initialized || queued) {
            throw new IllegalStateException();
        }
        initialized = false;
        int rc = camDevice.submitTransfer(getNativeLibusbAddr());
        if (rc == 0) {
            log("  --  The return of the Transfer is = " + rc + "  -  " + LibUsb.libusb_error.fromNative((byte) rc));
        } else {
            logError("  --  The return of the Transfer is = " + rc + "  -  " + LibUsb.libusb_error.fromNative((byte) rc));
            throw new IllegalStateException("!! Submit xfer failed !! ");
        }
        queued = true;
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private void logError(String msg) {
        System.out.println("\033[31m" + msg + "\033[0m");
    }

    public void getPacketDataRequest(int packetNo, byte[] buf, int len) {
        if (packetNo < 0 || packetNo >= PACKETS_PER_REQUEST || len > MAX_PACKET_SIZE) {
            throw new IllegalArgumentException();
        }
        buffer.read(packetNo * MAX_PACKET_SIZE, buf, 0, len);
    }
    
    public void setUrbPointer(Pointer urbPointer) {
        setNativeLibusbAddr(urbPointer);
    }

}
