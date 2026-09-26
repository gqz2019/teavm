/*
 *  Copyright 2026 Legato contributors.
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
package org.teavm.classlib.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
public class JsoupClasslibCompatibilityTest {
    @Test
    public void charsetSupportDistinguishesUnknownAndIllegalNames() {
        assertTrue(Charset.isSupported("UTF-8"));
        assertFalse(Charset.isSupported("NO_SUCH_CHARSET"));
        try {
            Charset.isSupported("!BAD");
        } catch (IllegalCharsetNameException expected) {
            return;
        }
        throw new AssertionError("Illegal charset name was accepted");
    }

    @Test
    public void sharedEmptyListClearIsNoOp() {
        Collections.emptyList().clear();
        assertTrue(Collections.emptyList().isEmpty());
    }

    @Test
    public void bufferedReaderSupportsMarkAndIdempotentClose() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("A"));
        assertTrue(reader.markSupported());
        reader.close();
        reader.close();
        try {
            reader.read();
        } catch (IOException expected) {
            return;
        }
        throw new AssertionError("Read after close was accepted");
    }

    @Test
    public void socketTimeoutKeepsJavaInheritanceAndMessage() {
        SocketTimeoutException error = new SocketTimeoutException("Read timeout");
        assertTrue(error instanceof InterruptedIOException);
        assertEquals("Read timeout", error.getMessage());
        assertNull(new SocketTimeoutException().getMessage());
    }
}
