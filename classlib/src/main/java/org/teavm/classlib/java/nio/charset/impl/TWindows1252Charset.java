/*
 *  Copyright 2026 TeaVM contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.nio.charset.impl;

import org.teavm.classlib.java.nio.charset.TCharset;
import org.teavm.classlib.java.nio.charset.TCharsetDecoder;
import org.teavm.classlib.java.nio.charset.TCharsetEncoder;
import org.teavm.classlib.java.nio.charset.TStandardCharsets;

public class TWindows1252Charset extends TCharset {
    private static final char[] C1 = {
            '\u20ac', '\u0081', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021',
            '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\u008d', '\u017d', '\u008f',
            '\u0090', '\u2018', '\u2019', '\u201c', '\u201d', '\u2022', '\u2013', '\u2014',
            '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\u009d', '\u017e', '\u0178' };

    public TWindows1252Charset() {
        super("windows-1252", new String[] { "cp1252", "windows-1252" });
    }

    static char decodeByte(int value) {
        return value >= 0x80 && value < 0xa0 ? C1[value - 0x80] : (char) value;
    }

    static int encodeChar(char value) {
        if (value < 0x80 || value >= 0xa0 && value <= 0xff) {
            return value;
        }
        for (int index = 0; index < C1.length; index++) {
            if (C1[index] == value) {
                return index + 0x80;
            }
        }
        return -1;
    }

    @Override
    public boolean contains(TCharset cs) {
        return cs == this || cs == TStandardCharsets.US_ASCII;
    }

    @Override
    public TCharsetDecoder newDecoder() {
        return new TWindows1252Decoder(this);
    }

    @Override
    public TCharsetEncoder newEncoder() {
        return new TWindows1252Encoder(this);
    }
}
