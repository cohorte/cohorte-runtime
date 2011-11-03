/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Client, a Java client extension to JSON-RPC-Java
 * (C) Copyright CodeBistro 2007, Sasha Ovsankin <sasha at codebistro dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jabsorb.ng.client;

/**
 * Exception created from the JSON-RPC error response
 */
public class ErrorResponse extends ClientError {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    private static String formatMessage(final Integer code,
            final String message, final String trace) {

        StringBuilder resultBuilder = new StringBuilder();
        if (code == null) {
            resultBuilder.append("JSONRPC error: ");
        } else {
            resultBuilder.append("JSONRPC error code ").append(code)
                    .append(": ");
        }

        if (message != null) {
            resultBuilder.append("\nCaused by ").append(message);
        }
        if (trace != null) {
            resultBuilder.append("\nTrace :\n").append(trace);
        }
        return resultBuilder.toString();
    }

    private String trace;

    public ErrorResponse(final Integer code, final String message,
            final String trace) {

        super(ErrorResponse.formatMessage(code, message, trace));
        this.trace = trace;
    }

    public String getTrace() {

        return trace;
    }
}
