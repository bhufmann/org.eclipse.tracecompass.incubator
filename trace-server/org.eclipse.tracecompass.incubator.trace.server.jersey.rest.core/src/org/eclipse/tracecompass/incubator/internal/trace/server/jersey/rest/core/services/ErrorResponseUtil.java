/*******************************************************************************
 * Copyright (c) 2025 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Error response utility class
 *
 * @author Bernd Hufmann
 */
public class ErrorResponseUtil {

    private ErrorResponseUtil() {
    }

    /**
     * Create a new error response
     * @param status
     *            the http status
     * @param message
     *            the error message
     * @return the error response
     */
    public static Response newErrorResponse(Status status, String message) {
        return Response.status(status).entity(new ErrorResponseImpl(message)).build();
    }
}
