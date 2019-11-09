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

import com.google.common.collect.ImmutableList;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.List;

/**
 *
 * @author peter
 */
@Structure.FieldOrder({"length", "actual_length", "status"})
public class libusb_iso_packet_descriptor_structure extends Structure {

    public static class ByReference extends libusb_iso_packet_descriptor_structure implements Structure.ByReference {
    }

    public libusb_iso_packet_descriptor_structure() {
    }

    public libusb_iso_packet_descriptor_structure(Pointer p) {
        super(p);
        read();
    }

    public libusb_iso_packet_descriptor_structure[] toArray(int size) {
        return (libusb_iso_packet_descriptor_structure[]) super.toArray(size);
    }

    @Override
    protected List<String> getFieldOrder() {
        return ImmutableList.of("length", "actual_length", "status");
    }

    public int length;
    public int actual_length;
    public int status;

}
