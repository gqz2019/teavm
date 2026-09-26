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

public class TGBKCharset extends TCharset {
    public TGBKCharset() {
        super("GBK", new String[] { "CP936", "windows-936" });
    }

    @Override
    public boolean contains(TCharset cs) {
        return cs == this || cs == TStandardCharsets.US_ASCII;
    }

    @Override
    public TCharsetDecoder newDecoder() {
        return new TGBKDecoder(this);
    }

    @Override
    public TCharsetEncoder newEncoder() {
        return new TGBKEncoder(this);
    }
}
