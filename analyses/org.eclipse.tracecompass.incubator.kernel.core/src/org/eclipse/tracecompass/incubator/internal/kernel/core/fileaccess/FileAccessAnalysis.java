/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.kernel.core.fileaccess;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * File access anaylysis
 *
 * @author Matthew Khouzam
 */
public class FileAccessAnalysis extends TmfStateSystemAnalysisModule {

    /**
     * ID
     */
    public static final String ID = "org.eclipse.tracecompass.incubator.internal.kernel.core.fileacess"; //$NON-NLS-1$

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        if (trace instanceof IKernelTrace) {
            return new FileAccessStateProvider((IKernelTrace) trace);
        }
        throw new IllegalStateException("Trace " + trace + "(" + (trace == null ? "null" : trace.getClass().getCanonicalName()) + ")" + " is not of the type IKernelTrace."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    @Override
    public String getId() {
        return ID;
    }
}
