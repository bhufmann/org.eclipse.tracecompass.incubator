/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.callstack.core.instrumented;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.callstack.core.instrumented.ICalledFunction;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;

/**
 * @author Geneviève Bastien
 *
 */
public class FunctionTidAspect implements ISegmentAspect {

    /**
     * A symbol aspect
     */
    public static final ISegmentAspect TID_ASPECT = new FunctionTidAspect();

    @Override
    public String getName() {
        return String.valueOf(org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.Messages.AspectName_Tid);
    }

    @Override
    public String getHelpText() {
        return String.valueOf(org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.Messages.AspectHelpText_Tid);
    }

    @Override
    public @Nullable Comparator<?> getComparator() {
        return null;
    }

    @Override
    public @Nullable Object resolve(ISegment segment) {
        if (segment instanceof ICalledFunction) {
            return ((ICalledFunction) segment).getThreadId();
        }
        return null;
    }

}
