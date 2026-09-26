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

public class TGBKDecoder extends TBufferedDecoder {
    public TGBKDecoder(TCharset cs) {
        super(cs, 0.5f, 1);
    }

    @Override
    protected TCoderResult arrayDecode(byte[] inArray, int inPos, int inSize, char[] outArray, int outPos, int outSize,
            Controller controller) {
        TCoderResult result = null;
        while (inPos < inSize && outPos < outSize) {
            int lead = inArray[inPos] & 0xff;
            if (lead < 0x80) {
                outArray[outPos++] = (char) lead;
                inPos++;
            } else if (lead == 0x80 || lead == 0xff) {
                outArray[outPos++] = (char) decodeSingle(lead);
                inPos++;
            } else if (inPos + 1 >= inSize) {
                if (!controller.hasMoreInput(2)) {
                    result = TCoderResult.UNDERFLOW;
                }
                break;
            } else {
                int trail = inArray[inPos + 1] & 0xff;
                if (trail < 0x40 || trail == 0x7f || trail == 0xff) {
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
            }
        }
        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);
        return result;
    }

    @JSBody(params = "value", script = ""
            + "const decoded = new TextDecoder('gbk').decode(new Uint8Array([value]));"
            + "return decoded.charCodeAt(0);")
    static native int decodeSingle(int value);

    @JSBody(params = { "lead", "trail" }, script = ""
            + "const decoded = new TextDecoder('gbk').decode(new Uint8Array([lead, trail]));"
            + "return decoded.length === 1 ? decoded.charCodeAt(0) : -1;")
    static native int decodePair(int lead, int trail);
}
