/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.opentracing.core.event;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages
 *
 * @author Katherine Nadeau
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.incubator.internal.opentracing.core.event.messages"; //$NON-NLS-1$
    /**
     * Tags
     */
    public static @Nullable String OpenTracingAspects_Tags;
    /**
     * Tags Description
     */
    public static @Nullable String OpenTracingAspects_TagsD;
    /**
     * ID
     */
    public static @Nullable String OpenTracingAspects_SpanId;
    /**
     * ID Description
     */
    public static @Nullable String OpenTracingAspects_SpanIdD;
    /**
     * Name
     */
    public static @Nullable String OpenTracingAspects_Name;
    /**
     * Name Description
     */
    public static @Nullable String OpenTracingAspects_NameD;
    /**
     * Duration
     */
    public static @Nullable String OpenTracingAspects_Duration;
    /**
     * Duration Description
     */
    public static @Nullable String OpenTracingAspects_DurationD;
    /**
     * Process Id
     */
    public static @Nullable String OpenTracingAspects_Pid;
    /**
     * Process Id Description
     */
    public static @Nullable String OpenTracingAspects_PidD;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}