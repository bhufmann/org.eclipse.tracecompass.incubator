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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error response class
 * @author Bernd Hufmann
 */
public class ErrorResponseImpl implements Serializable {

    private static final long serialVersionUID = -5823094821729001182L;

    private final String fMessage;

    /**
     * {@link JsonCreator} Constructor for final fields
     *
     * @param message
     *            The error message
     */
    @JsonCreator
    public ErrorResponseImpl(
            @JsonProperty("message") String message) {
        fMessage = message;
    }

    /**
     * Get the UUID
     *
     * @return the UUID
     */
    public String getMessage() {
        return fMessage;
    }
}
