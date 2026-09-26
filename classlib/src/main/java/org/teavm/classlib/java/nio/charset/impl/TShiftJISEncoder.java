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

public class TShiftJISEncoder extends TBufferedEncoder {
    private static int[] mappings;

    public TShiftJISEncoder(TCharset cs) {
        super(cs, 1.5f, 2, new byte[] { (byte) 0xfc, (byte) 0xfc });
    }

    @Override
    protected TCoderResult arrayEncode(char[] inArray, int inPos, int inSize, byte[] outArray, int outPos, int outSize,
            Controller controller) {
        int[] map = mappings();
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
            int encoded = map[value];
            if (encoded == 0) {
                result = TCoderResult.unmappableForLength(1);
                break;
            }
            encoded--;
            int byteCount = encoded <= 0xff ? 1 : 2;
            if (outPos + byteCount > outSize) {
                if (!controller.hasMoreOutput(byteCount)) {
                    result = TCoderResult.OVERFLOW;
                }
                break;
            }
            if (byteCount == 2) {
                outArray[outPos++] = (byte) (encoded >> 8);
            }
            outArray[outPos++] = (byte) encoded;
            inPos++;
        }
        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);
        return result;
    }

    private static int[] mappings() {
        if (mappings == null) {
            int[] values = new int[Character.MAX_VALUE + 1];
            for (int value = 0; value < 0x80; value++) {
                values[value] = value + 1;
            }
            values[0x1a] = 0x7f + 1;
            values[0x1c] = 0x1a + 1;
            values[0x7f] = 0x1c + 1;
            for (int value = 0xa1; value <= 0xdf; value++) {
                values[0xff61 + value - 0xa1] = value + 1;
            }
            int[][] leadRanges = { { 0x81, 0x9f }, { 0xe0, 0xec }, { 0xef, 0xfc }, { 0xed, 0xee } };
            for (int[] range : leadRanges) {
                for (int lead = range[0]; lead <= range[1]; lead++) {
                    for (int trail = 0x40; trail <= 0xfc; trail++) {
                        if (trail == 0x7f) {
                            continue;
                        }
                        int decoded = TShiftJISDecoder.decodePair(lead, trail);
                        if (decoded >= 0 && decoded != 0xfffd && values[decoded] == 0) {
                            values[decoded] = (lead << 8 | trail) + 1;
                        }
                    }
                }
            }
            mappings = values;
        }
        return mappings;
    }
}
