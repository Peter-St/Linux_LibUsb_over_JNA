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

import com.sun.jna.Structure;

/**
 *
 * @author peter
 */
@Structure.FieldOrder({"length", "actual_length", "status"})
public final class libusb_iso_packet_descriptor extends Structure {

    public int length;
    public int actual_length;
    public int status;

    @Override
    public int fieldOffset(String field) {
        return super.fieldOffset(field);
    }
}
