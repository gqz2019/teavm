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
import org.teavm.jso.JSBody;

public class TShiftJISDecoder extends TBufferedDecoder {
    public TShiftJISDecoder(TCharset cs) {
        super(cs, 0.5f, 1);
    }

    @Override
    protected TCoderResult arrayDecode(byte[] inArray, int inPos, int inSize, char[] outArray, int outPos, int outSize,
            Controller controller) {
        TCoderResult result = null;
        while (inPos < inSize && outPos < outSize) {
            int lead = inArray[inPos] & 0xff;
            if (lead < 0x80) {
                int decoded = lead;
                if (lead == 0x1a) {
                    decoded = 0x1c;
                } else if (lead == 0x1c) {
                    decoded = 0x7f;
                } else if (lead == 0x7f) {
                    decoded = 0x1a;
                }
                outArray[outPos++] = (char) decoded;
                inPos++;
            } else if (lead >= 0xa1 && lead <= 0xdf) {
                outArray[outPos++] = (char) (0xff61 + lead - 0xa1);
                inPos++;
            } else if (lead >= 0x81 && lead <= 0x9f || lead >= 0xe0 && lead <= 0xfc) {
                if (inPos + 1 >= inSize) {
                    if (!controller.hasMoreInput(2)) {
                        result = TCoderResult.UNDERFLOW;
                    }
                    break;
                }
                int trail = inArray[inPos + 1] & 0xff;
                if (trail < 0x40 || trail == 0x7f || trail > 0xfc) {
                    result = TCoderResult.malformedForLength(1);
                    break;
                }
                int decoded = decodePair(lead, trail);
                if (decoded < 0 || decoded == 0xfffd) {
                    result = TCoderResult.unmappableForLength(2);
                    break;
                }
                outArray[outPos++] = (char) decoded;
                inPos += 2;
            } else {
                result = TCoderResult.malformedForLength(1);
                break;
            }
        }
        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);
        return result;
    }

    @JSBody(params = { "lead", "trail" }, script = ""
            + "const decoded = new TextDecoder('shift_jis').decode(new Uint8Array([lead, trail]));"
            + "return decoded.length === 1 ? decoded.charCodeAt(0) : -1;")
    static native int decodePair(int lead, int trail);
}
