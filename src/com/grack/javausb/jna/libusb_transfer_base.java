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
package com.grack.javausb.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 *
 * @author peter
 */
@Structure.FieldOrder({"dev_handle", "flags", "endpoint", "type", "timeout",
        "status", "length", "actual_length",
        "callback", "user_data", "buffer",
        "num_iso_packets"})
    public class libusb_transfer_base extends Structure {

        public Pointer dev_handle;

        public byte flags;

        public byte endpoint;

        public byte type;

        public int timeout;

        public int status;

        public int length;

        public int actual_length;

        public LibUSBXNative.Libusb_transfer_cb_fn callback;

        public Pointer user_data;

        public Pointer buffer;

        public int num_iso_packets;

        @Override
        public int fieldOffset(String field) {
            return super.fieldOffset(field);
        }

    }