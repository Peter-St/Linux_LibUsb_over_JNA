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
package humer.peter.jna_libusb_project;

import com.grack.javausb.USB;
import com.grack.javausb.USBConfiguration;
import com.grack.javausb.USBDevice;
import com.grack.javausb.USBEndpoint;
import com.grack.javausb.USBException;
import com.grack.javausb.USBInterface;
import com.grack.javausb.USBInterfaceDescriptor;
import com.grack.javausb.USBNative;
import com.grack.javausb.USBOpenDevice;
import com.grack.javausb.USBTransferType;
import com.grack.javausb.jna.LibUSBXNative;
import com.grack.javausb.jna.libusb_device_descriptor;
import com.grack.javausb.jna.libusb_transfer;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.CallbackReference;
import com.sun.jna.Native;
import java.io.IOException;
import static java.lang.Integer.toHexString;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author peter
 */
public class Main extends javax.swing.JFrame {

    // Device Variables
    private ArrayList<Request> xfers = new ArrayList<>();
    private static final byte endpointadress = (byte) 0x81;
    private static final int CAM_STREAMING_INTERFACE_NUM = 1;
    private static final int CAM_CONTROL_INTERFACE_NUM = 0;
    public static int CAM_FORMAT_INDEX = 1;   // MJPEG // YUV // bFormatIndex: 1 = uncompressed
    public static int CAM_FRAME_INDEX = 5; // bFrameIndex: 1 = 640 x 360;       2 = 176 x 144;     3 =    320 x 240;      4 = 352 x 288;     5 = 640 x 480;
    public static int CAM_FRAME_INTERVAL = 666666; // 333333 YUV = 30 fps // 666666 YUV = 15 fps
    public static int PACKETS_PER_REQUEST = 4;
    public static int MAX_PACKET_SIZE = 3072;
    public static int ACTIVE_URBS = 8;
    public static int ALT_SETTING = 7; // 7 = 3*1024 bytes packet size // 6 = 3*896 // 5 = 2*1024 // 4 = 2*768 // 3 = 1x 1024 // 2 = 1x 512 // 1 = 128 //
    // ADDITIONAL CONFIGURATION

    // USB codes:
// Request types (bmRequestType):
    private static final int RT_STANDARD_INTERFACE_SET = 0x01;
    private static final byte RT_CLASS_INTERFACE_SET = 0x21;
    private static final int RT_CLASS_INTERFACE_GET = 0xA1;
    // Video interface subclass codes:
    private static final int SC_VIDEOCONTROL = 0x01;
    private static final int SC_VIDEOSTREAMING = 0x02;
    // Standard request codes:
    private static final int SET_INTERFACE = 0x0b;
    // Video class-specific request codes:
    private static final byte SET_CUR = 0x01;
    private static final int GET_CUR = 0x81;
    // VideoControl interface control selectors (CS):
    private static final int VC_REQUEST_ERROR_CODE_CONTROL = 0x02;
    // VideoStreaming interface control selectors (CS):
    private static final int VS_PROBE_CONTROL = 0x01;
    private static final int VS_COMMIT_CONTROL = 0x02;
    private static final int VS_STILL_PROBE_CONTROL = 0x03;
    private static final int VS_STILL_COMMIT_CONTROL = 0x04;
    private static final int VS_STREAM_ERROR_CODE_CONTROL = 0x06;
    private static final int VS_STILL_IMAGE_TRIGGER_CONTROL = 0x05;

    private int signal = 0;
    private boolean stopTransmission;

    // Values for debug
    ArrayList<String> logArray = new ArrayList<>(512);
    private int packetCnt = 0;
    private int packet0Cnt = 0;
    private int packet12Cnt = 0;
    private int packetDataCnt = 0;
    private int packetHdr8Ccnt = 0;
    private int packetErrorCnt = 0;
    private int frameCnt = 0;
    private long time0 = System.currentTimeMillis();
    private int frameLen = 0;
    private int requestCnt = 0;

    // LibUsb Values
    private static final int LIBUSB_TRANSFER_COMPLETED = 0;
    private static int position_of_libusb_transfer_usercontext;
    private static int vendorID;
    private static int productID;

    private static int FOXLINK_USB_VID = 0x05c8;
    private static int FOXLINK_USB_PID = 0x0233;

    private boolean camerafound;
    private USBDevice camDevice;
    private USBOpenDevice openDevice;
    private static USBInterface controlInterface;
    private static USBInterface streamInterface;
    private Pointer ctx;

    // Color Text Output
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        usb_search = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        openCam = new javax.swing.JButton();
        cameraFound = new javax.swing.JLabel();
        closeCameraConnection = new javax.swing.JButton();
        startIsoTransfer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        usb_search.setText("Search for Usb Devices");
        usb_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usb_searchActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("LibUsb over Jna Sample");
        jScrollPane1.setViewportView(jTextArea1);

        openCam.setText("OpenCamera");
        openCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCamActionPerformed(evt);
            }
        });

        cameraFound.setText("No Camera");

        closeCameraConnection.setText("Close Camera Connection");
        closeCameraConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeCameraConnectionActionPerformed(evt);
            }
        });

        startIsoTransfer.setText("Start IsoTransfer");
        startIsoTransfer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startIsoTransferActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(usb_search)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(openCam)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                                .addComponent(startIsoTransfer))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(178, 178, 178)
                        .addComponent(cameraFound)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(closeCameraConnection)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usb_search)
                    .addComponent(openCam)
                    .addComponent(startIsoTransfer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeCameraConnection)
                .addGap(2, 2, 2)
                .addComponent(cameraFound)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void usb_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usb_searchActionPerformed

        StringBuilder sb = new StringBuilder();

        try {
            USB usb = new USB();
            //ctx = usb.getContext();
            //log("Context = \n" + ctx.toString());
            System.out.printf("%4s    %10s     %10s\n",
                    "Bus/Address",
                    "idVendor",
                    "idProduct");

            sb.append(String.format("%4s     %10s     %10s\n\n",
                    "Bus/Address",
                    "idVendor",
                    "idProduct"));

            for (USBDevice device : usb.devices()) {

                if (isFoxlink(device.getDescriptor())) {

                    camDevice = device;

                    System.out.printf("%03X/%03X         0x%04X        0x%04X    %4s\n",
                            device.get_bus_number(),
                            device.get_address(),
                            device.vendor(),
                            device.product(),
                            "   --->   Foxlink");

                    sb.append(String.format("%03X/%03X          0x%04X        0x%04X   %4s\n",
                            device.get_bus_number(),
                            device.get_address(),
                            device.vendor(),
                            device.product(),
                            "   --->   Foxlink"));

                } else {
                    System.out.printf("%03X/%03X         0x%04X       0x%04X\n",
                            device.get_bus_number(),
                            device.get_address(),
                            device.vendor(),
                            device.product());

                    sb.append(String.format("%03X/%03X          0x%04X        0x%04X\n",
                            device.get_bus_number(),
                            device.get_address(),
                            device.vendor(),
                            device.product()));
                }
            }
        } catch (USBException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (camDevice != null) {
            sb.append("Found the camera: Foxlink");
        }
        jTextArea1.setText(sb.toString());


    }//GEN-LAST:event_usb_searchActionPerformed

    private void openCamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCamActionPerformed

        StringBuilder sb = new StringBuilder();

        if (camDevice == null) {
            jTextArea1.setText("No Camera Found.\n\nRun 'search the camera' first. ");
            return;
        }

        for (USBConfiguration config : camDevice.configurations()) {

            try {
                //openDevice = camDevice.open();
                openDevice = camDevice.open_device_with_vid_pid();

                for (USBInterface iface : config.interfaces()) {
                    System.out.println(iface);
                    //sb.append(iface);
                    if (openDevice.kernel_driver_active(iface)) {
                        log("Kernel Driver on Interface " + iface.number() + " active");
                        sb.append("Kernel Driver on Interface " + iface.number() + " active\n");
                        if (openDevice.detach_kernel_driver(iface)) {
                            log("Kernel Driver on Interface " + iface.number() + " sucessful detached");
                            sb.append("Kernel Driver on Interface " + iface.number() + " sucessful detached\n");
                        }
                    }
                    if (openDevice.claim(iface)) {
                        logSucess("Interface " + iface.number() + " claimed !");
                        sb.append("Interface " + iface.number() + " claimed !\n");
                        for (USBInterfaceDescriptor ifaceDescriptor : iface.altSettings()) {
                            for (USBEndpoint endpoint : ifaceDescriptor.endpoints()) {
                                System.out.println(endpoint);
                                sb.append(endpoint + "\n");
                                if (endpoint.transferType() == USBTransferType.INTERRUPT) {
                                    controlInterface = iface;
                                } else if (endpoint.transferType() == USBTransferType.ISOCHRONOUS) {
                                    streamInterface = iface;
                                }
                            }
                        }
                    } else {
                        logError("Failed to Claim the interface " + iface.number());
                        sb.append("Failed to Claim the interface " + iface.number() + "\n");
                    }
                }
            } catch (USBException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            if (openDevice.set_interface_alt_setting(streamInterface.number(), 0)) {
                log("  -- Altsetting setted to 0");
                sb.append("  -- Altsetting setted to 0\n");
                controltransfer(sb);

            } else {
                log("  --  failed to set the Altsetting. ");
            }
        } catch (USBException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        jTextArea1.setText(sb.toString());


    }//GEN-LAST:event_openCamActionPerformed

    private void closeCameraConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeCameraConnectionActionPerformed
        closeConnection();
    }//GEN-LAST:event_closeCameraConnectionActionPerformed

    private void startIsoTransferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startIsoTransferActionPerformed

        StringBuilder sb = new StringBuilder();
        sb.append("");

        if (openDevice == null) {
            jTextArea1.setText("No Camera Found.\n\nRun 'search the camera', or 'Open the camera' first. ");

            return;
        }
        int i;

        try {
            openDevice.set_interface_alt_setting(streamInterface.number(), ALT_SETTING);
            log("Altsetting setted to: " + ALT_SETTING + "\n  -- Try to allocate the LibUsb Transfers.");
            log("  -- Starting with the submittion of the UsbRequestBlocks.");

            signal = 0;
            stopTransmission = false;

            for (i = 0; i < ACTIVE_URBS; i++) {

                libusb_transfer.ByReference xfer;

                xfer = new libusb_transfer.ByReference();

                xfer = openDevice.allocateTransfer(PACKETS_PER_REQUEST);

                //xfer.getPointer();
                Memory buffer;
                int bufSize = MAX_PACKET_SIZE * PACKETS_PER_REQUEST;

                buffer = new Memory(bufSize);

                //Pointer urbPointer = ;
                //req.setUrbPointer(camDevice.allocateTransfer(PACKETS_PER_REQUEST));
                xfer.buffer = buffer;
                xfer.callback = setTheCallbackFunction();
                xfer.dev_handle = openDevice.getDevHandle();
                xfer.endpoint = endpointadress;
                xfer.length = bufSize;
                xfer.type = USBNative.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS.getValue();
                xfer.timeout = 1000;
                xfer.user_data = null;
                xfer.num_iso_packets = PACKETS_PER_REQUEST;

                for (int j = 0; j < PACKETS_PER_REQUEST; j++) {
                    xfer.iso_packet_desc[j].length = MAX_PACKET_SIZE;
                    xfer.iso_packet_desc[j].status = -1;
                }
                //int rc = openDevice.submitTransfer(null);
                int rc = openDevice.submitTransfer(xfer);
                if (rc == 0) {
                    logSucess("Submitted UrbBlock Nr." + i);
                } else if (rc < 0) {
                    logError("Failed to Submit UrbBlock Nr. " + i);
                    throw new IOException();
                }
                //log("The Request size is " + req.getLibusbSize());

            }

            log("  -- All LibUsb Transfers sucessful submitted.");
            log("  -- Starting handle the events.");

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                @Override
                public void run() {
                    stopTransmission = true;
                }
            },
                    5000
            );

            do {
                openDevice.handle_events(camDevice.getContext());
                signal++;
                if (stopTransmission) {

                    break;
                }
            } while (signal < 1000);

            /*
            xfers = new ArrayList<>(ACTIVE_URBS);
            signal = 0;
            stopTransmission = false;

            for (i = 0; i < ACTIVE_URBS; i++) {

                Pointer urbPointer = openDevice.allocateTransfer(PACKETS_PER_REQUEST);
                //req.setUrbPointer(camDevice.allocateTransfer(PACKETS_PER_REQUEST));

                Request req = new Request(urbPointer, PACKETS_PER_REQUEST, MAX_PACKET_SIZE);

                position_of_libusb_transfer_usercontext = req.initialize(openDevice.getDevHandle(), endpointadress, i, CallbackReference.getFunctionPointer(setTheCallbackFunction()));
                //req.submit();
                int rc = openDevice.submitTransfer(urbPointer);
                if (rc == 0) {
                    logSucess("Submitted UrbBlock Nr." + i);
                } else if (rc < 0) {
                    logError("Failed to Submit UrbBlock Nr. " + i);
                    throw new IOException();
                }
                //log("The Request size is " + req.getLibusbSize());
                int status = urbPointer.getInt(req.getPosTransferStatus());
                log("Status = " + status);
                
                xfers.add(req);
            }

            log("  -- All LibUsb Transfers sucessful submitted.");
            log("  -- Starting handle the events.");

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                @Override
                public void run() {
                    stopTransmission = true;
                }
            },
                    5000
            );

            do {
                openDevice.handle_events(camDevice.getContext());
                signal++;
                if (stopTransmission) {

                    break;
                }
            } while (signal < 1000);
            
             */
        } catch (USBException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("requests=" + requestCnt + " packetCnt=" + packetCnt + " packetErrorCnt=" + packetErrorCnt + " packet0Cnt=" + packet0Cnt + ", packet12Cnt=" + packet12Cnt + ", packetDataCnt=" + packetDataCnt + " packetHdr8cCnt=" + packetHdr8Ccnt + " frameCnt=" + frameCnt);
        sb.append("requests=" + requestCnt + " packetCnt=" + packetCnt + " packetErrorCnt=" + packetErrorCnt + " packet0Cnt=" + packet0Cnt + ", packet12Cnt=" + packet12Cnt + ", packetDataCnt=" + packetDataCnt + " packetHdr8cCnt=" + packetHdr8Ccnt + " frameCnt=" + frameCnt);
        for (String s : logArray) {
            System.out.println(s);
            //sb.append(s);
        }

        jTextArea1.setText("Transfer sucessful --> look at your Output window (log)");

        closeConnection();
        log("  -- Exit.");
    }//GEN-LAST:event_startIsoTransferActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cameraFound;
    private javax.swing.JButton closeCameraConnection;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton openCam;
    private javax.swing.JButton startIsoTransfer;
    private javax.swing.JButton usb_search;
    // End of variables declaration//GEN-END:variables

    public void controltransfer(StringBuilder sb) throws Exception {
        final int timeout = 5000;
        int usedStreamingParmsLen;
        int len;
        byte[] streamingParms = new byte[26];
        short wLength = (short) streamingParms.length;

        streamingParms[0] = (byte) 0x01;
        streamingParms[2] = (byte) CAM_FORMAT_INDEX;                // bFormatIndex
        streamingParms[3] = (byte) CAM_FRAME_INDEX;                 // bFrameIndex
        packUsbInt(CAM_FRAME_INTERVAL, streamingParms, 4);         // dwFrameInterval
        log("Initial streaming parms: " + dumpStreamingParms(streamingParms));
        sb.append("Initial streaming parms: " + dumpStreamingParms(streamingParms) + "\n");
        len = openDevice.sendControlTransfer(RT_CLASS_INTERFACE_SET, SET_CUR, (short) (VS_PROBE_CONTROL << 8), (short) CAM_STREAMING_INTERFACE_NUM, streamingParms, wLength, timeout);
        if (len != streamingParms.length) {
            throw new Exception("Camera initialization failed. Streaming parms probe set failed, len=" + len + ".");
        }
        // for (int i = 0; i < streacontrol_readmingParms.length; i++) streamingParms[i] = 99;          // temp test
        len = openDevice.sendControlTransfer((byte) RT_CLASS_INTERFACE_GET, (byte) GET_CUR, (short) (VS_PROBE_CONTROL << 8), (short) CAM_STREAMING_INTERFACE_NUM, streamingParms, (short) streamingParms.length, timeout);
        if (len != streamingParms.length) {
            throw new Exception("Camera initialization failed. Streaming parms probe get failed, len=" + len + ".");
        }
        log("Probed streaming parms: " + dumpStreamingParms(streamingParms));
        sb.append("Probed streaming parms: " + dumpStreamingParms(streamingParms) + "\n");

        usedStreamingParmsLen = len;
        // log("Streaming parms length: " + usedStreamingParmsLen);
        len = openDevice.sendControlTransfer(RT_CLASS_INTERFACE_SET, SET_CUR, (short) (VS_COMMIT_CONTROL << 8), (short) CAM_STREAMING_INTERFACE_NUM, streamingParms, (short) usedStreamingParmsLen, timeout);
        if (len != streamingParms.length) {
            throw new Exception("Camera initialization failed. Streaming parms commit set failed.");
        }
        // for (int i = 0; i < streamingParms.length; i++) streamingParms[i] = 99;          // temp test
        len = openDevice.sendControlTransfer((byte) RT_CLASS_INTERFACE_GET, (byte) GET_CUR, (short) (VS_COMMIT_CONTROL << 8), (short) CAM_STREAMING_INTERFACE_NUM, streamingParms, (short) usedStreamingParmsLen, timeout);
        if (len != streamingParms.length) {
            throw new Exception("Camera initialization failed. Streaming parms commit get failed.");
        }
        log("Final streaming parms: " + dumpStreamingParms(streamingParms));
        sb.append("Final streaming parms: " + dumpStreamingParms(streamingParms) + "\n");

    }

    private String dumpStreamingParms(byte[] p) {
        StringBuilder s = new StringBuilder(128);
        s.append("hint=0x" + Integer.toHexString(unpackUsbUInt2(p, 0)));
        s.append(" format=" + (p[2] & 0xf));
        s.append(" frame=" + (p[3] & 0xf));
        s.append(" frameInterval=" + unpackUsbInt(p, 4));
        s.append(" keyFrameRate=" + unpackUsbUInt2(p, 8));
        s.append(" pFrameRate=" + unpackUsbUInt2(p, 10));
        s.append(" compQuality=" + unpackUsbUInt2(p, 12));
        s.append(" compWindowSize=" + unpackUsbUInt2(p, 14));
        s.append(" delay=" + unpackUsbUInt2(p, 16));
        s.append(" maxVideoFrameSize=" + unpackUsbInt(p, 18));
        s.append(" maxPayloadTransferSize=" + unpackUsbInt(p, 22));
        return s.toString();
    }

    private String dumpStillImageParms(byte[] p) {
        StringBuilder s = new StringBuilder(128);
        s.append("bFormatIndex=" + (p[0] & 0xff));
        s.append(" bFrameIndex=" + (p[1] & 0xff));
        s.append(" bCompressionIndex=" + (p[2] & 0xff));
        s.append(" maxVideoFrameSize=" + unpackUsbInt(p, 3));
        s.append(" maxPayloadTransferSize=" + unpackUsbInt(p, 7));
        return s.toString();
    }

    private static int unpackUsbInt(byte[] buf, int pos) {
        return unpackInt(buf, pos, false);
    }

    private static int unpackUsbUInt2(byte[] buf, int pos) {
        return ((buf[pos + 1] & 0xFF) << 8) | (buf[pos] & 0xFF);
    }

    private static void packUsbInt(int i, byte[] buf, int pos) {
        packInt(i, buf, pos, false);
    }

    private static void packInt(int i, byte[] buf, int pos, boolean bigEndian) {
        if (bigEndian) {
            buf[pos] = (byte) ((i >>> 24) & 0xFF);
            buf[pos + 1] = (byte) ((i >>> 16) & 0xFF);
            buf[pos + 2] = (byte) ((i >>> 8) & 0xFF);
            buf[pos + 3] = (byte) (i & 0xFF);
        } else {
            buf[pos] = (byte) (i & 0xFF);
            buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
            buf[pos + 2] = (byte) ((i >>> 16) & 0xFF);
            buf[pos + 3] = (byte) ((i >>> 24) & 0xFF);
        }
    }

    private static int unpackInt(byte[] buf, int pos, boolean bigEndian) {
        if (bigEndian) {
            return (buf[pos] << 24) | ((buf[pos + 1] & 0xFF) << 16) | ((buf[pos + 2] & 0xFF) << 8) | (buf[pos + 3] & 0xFF);
        } else {
            return (buf[pos + 3] << 24) | ((buf[pos + 2] & 0xFF) << 16) | ((buf[pos + 1] & 0xFF) << 8) | (buf[pos] & 0xFF);
        }
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private void logError(String msg) {
        System.out.println("\033[31m" + msg + "\033[0m");
    }

    private void logSucess(String msg) {
        System.out.println("\033[0;32m" + msg + "\033[0m");
    }

    private void closeConnection() {

        if (openDevice != null) {
            try {
                log("Start exiting the camera ...");
                openDevice.release_interface(controlInterface);
                openDevice.release_interface(streamInterface);
                openDevice.close();
                controlInterface = null;
                streamInterface = null;
                openDevice = null;

            } catch (USBException ex) {
                Logger.getLogger(Main.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }
        camDevice = null;
        xfers = null;
    }

    private static String hexDump(byte[] buf, int len) {
        StringBuilder s = new StringBuilder(len * 3);
        for (int p = 0; p < len; p++) {
            if (p > 0) {
                s.append(' ');
            }
            int v = buf[p] & 0xff;
            if (v < 16) {
                s.append('0');
            }
            s.append(Integer.toHexString(v));
        }
        return s.toString();
    }

    private static boolean isFoxlink(libusb_device_descriptor desc) {
        return desc.idVendor == FOXLINK_USB_VID
                && desc.idProduct == FOXLINK_USB_PID;
    }

    private LibUSBXNative.Libusb_transfer_cb_fn setTheCallbackFunction() {
        LibUSBXNative.Libusb_transfer_cb_fn callback = new LibUSBXNative.Libusb_transfer_cb_fn() {

            @Override
            public void invoke(libusb_transfer.ByReference transfer) {

                //log("signal = " + signal++);
                //log("Transfer Status = " + transfer.status);

                for (int packetNo = 0; packetNo < transfer.num_iso_packets; packetNo++) {
                    if (transfer.iso_packet_desc[packetNo].status == LIBUSB_TRANSFER_COMPLETED) {
                        packetCnt++;
                        int packetLen = transfer.iso_packet_desc[packetNo].actual_length;
                        if (packetLen == 0) {
                            packet0Cnt++;
                        }
                        if (packetLen == 12) {
                            packet12Cnt++;
                        }
                        if (packetLen == 0) {
                            continue;
                        }
                        StringBuilder logEntry = new StringBuilder(requestCnt + "/" + packetNo + " len=" + packetLen);
                        if (packetLen > 0) {
                            if (packetLen > MAX_PACKET_SIZE) {
                                //throw new Exception("packetLen > maxPacketSize");
                            }
                            byte[] data = new byte[MAX_PACKET_SIZE];

                            transfer.buffer.read(packetNo * MAX_PACKET_SIZE, data, 0, packetLen);

                            logEntry.append(" data=" + hexDump(data, Math.min(32, packetLen)));
                            int headerLen = data[0] & 0xff;

                            try {
                                if (headerLen < 2 || headerLen > packetLen) {
                                    //    skipFrames = 1;
                                }
                            } catch (Exception e) {
                                System.out.println("Invalid payload header length.");
                            }
                            int headerFlags = data[1] & 0xff;
                            if (headerFlags == 0x8c) {
                                packetHdr8Ccnt++;
                            }
                            // logEntry.append(" hdrLen=" + headerLen + " hdr[1]=0x" + Integer.toHexString(headerFlags));
                            int dataLen = packetLen - headerLen;
                            if (dataLen > 0) {
                                packetDataCnt++;
                            }
                            frameLen += dataLen;
                            if ((headerFlags & 0x40) != 0) {
                                logEntry.append(" *** Error ***");
                                packetErrorCnt++;
                            }
                            if ((headerFlags & 2) != 0) {
                                logEntry.append(" EOF frameLen=" + frameLen);
                                frameCnt++;
                                frameLen = 0;
                                if(frameCnt == 40) stopTransmission = true;
                            }
                        }
                        logArray.add(logEntry.toString());

                    }

                }

                try {

                    if (!stopTransmission && openDevice.submitTransfer(transfer) != 0) {
                        throw new IllegalStateException("!! Submit xfer failed !! " + Native.POINTER_SIZE);
                    }

                    //log("return");
                } catch (USBException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        return callback;
    }
}
