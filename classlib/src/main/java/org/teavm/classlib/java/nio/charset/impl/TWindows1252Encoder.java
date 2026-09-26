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
import org.teavm.classlib.java.nio.charset.TCoderResult;

public class TWindows1252Encoder extends TBufferedEncoder {
    public TWindows1252Encoder(TCharset cs) {
        super(cs, 1, 1, new byte[] { 0x1a });
    }

    @Override
    protected TCoderResult arrayEncode(char[] inArray, int inPos, int inSize, byte[] outArray, int outPos, int outSize,
            Controller controller) {
        TCoderResult result = null;
        while (inPos < inSize && outPos < outSize) {
            char value = inArray[inPos];
            if (Character.isSurrogate(value)) {
                if (Character.isHighSurrogate(value) && inPos + 1 >= inSize) {
                    if (!controller.hasMoreInput()) {
                        result = TCoderResult.UNDERFLOW;
                    }
                } else if (Character.isHighSurrogate(value) && Character.isLowSurrogate(inArray[inPos + 1])) {
                    result = TCoderResult.unmappableForLength(2);
                } else {
                    result = TCoderResult.malformedForLength(1);
                }
                break;
            }
            int encoded = TWindows1252Charset.encodeChar(value);
            if (encoded < 0) {
                result = TCoderResult.unmappableForLength(1);
                break;
            }
            outArray[outPos++] = (byte) encoded;
            inPos++;
        }
        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);
        return result;
    }
}
