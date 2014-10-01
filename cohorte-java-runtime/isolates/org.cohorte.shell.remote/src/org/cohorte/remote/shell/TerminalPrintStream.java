/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cohorte.remote.shell;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Class implementing a <tt>TerminalPrintStream</tt>.
 */
class TerminalPrintStream extends PrintStream {

    /** Close stream flag */
    private volatile boolean pIsClosed = false;

    /** The client session owner */
    private final RemoteShellService pShellService;

    /**
     * Constructs a new instance wrapping the given <tt>OutputStream</tt>.
     *
     * @param aOwner
     *            The client session owner
     * @param aOutputStream
     *            the <tt>OutputStream</tt> to be wrapped.
     */
    public TerminalPrintStream(final RemoteShellService aOwner,
            final OutputStream aOutputStream) {

        super(aOutputStream);
        pShellService = aOwner;
    }

    /**
     * Closes the stream
     */
    @Override
    public void close() {

        pIsClosed = true;
        super.close();
    }

    /**
     * Flushes the output
     */
    @Override
    public void flush() {

        if (out != null) {
            try {
                out.flush();
            } catch (final Exception ex) {
                if (!pIsClosed) {
                    pShellService.error("TerminalPrintStream::flush()", ex);
                }
            }
        }
    }

    /**
     * Simple print
     *
     * @param aString
     *            String to print
     */
    @Override
    public void print(final String aString) {

        if (out != null) {
            try {
                // Send the string as bytes
                final byte[] bytes = aString.getBytes();
                out.write(bytes, 0, bytes.length);
                flush();

            } catch (final Exception ex) {
                if (!pIsClosed) {
                    pShellService.error("TerminalPrintStream::print()", ex);
                }
            }
        }
    }

    /**
     * Simple print with a new line
     *
     * @param aString
     *            String to print
     */
    @Override
    public void println(final String aString) {

        print(aString + "\r\n");
    }
}
