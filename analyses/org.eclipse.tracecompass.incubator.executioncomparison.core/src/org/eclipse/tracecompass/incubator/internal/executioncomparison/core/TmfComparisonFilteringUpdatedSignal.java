/*******************************************************************************
 * Copyright (c) 2024 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.executioncomparison.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * Signal to notify that the comparison filtering has been updated.
 *
 * @author Fateme Faraji Daneshgar and Vlad Arama
 */
public class TmfComparisonFilteringUpdatedSignal extends TmfSignal {

    private final ITmfTimestamp fBeginTimeA;
    private final ITmfTimestamp fEndTimeA;
    private final ITmfTimestamp fBeginTimeB;
    private final ITmfTimestamp fEndTimeB;
    private @Nullable String fStatistic;
    private @Nullable List<String> fTraceListA;
    private @Nullable List<String> fTraceListB;

    /**
     * Constructor
     *
     * @param source
     *            the class that has generated the signal
     * @param beginA
     *            the start time in event density chartA
     * @param endA
     *            the end time in event density chartA
     * @param beginB
     *            the start time in event density chartB
     * @param endB
     *            the end time in event density chartA
     * @param statistic
     *            the statistic that will be represented in the flame graph
     * @param traceListA
     *            the list of traces in group A
     * @param traceListB
     *            the list of traces in group B
     */
    public TmfComparisonFilteringUpdatedSignal(Object source, ITmfTimestamp beginA, ITmfTimestamp endA, ITmfTimestamp beginB, ITmfTimestamp endB, @Nullable String statistic, @Nullable List<String> traceListA, @Nullable List<String> traceListB) {
        super(source);
        fBeginTimeA = beginA;
        fEndTimeA = endA;
        fBeginTimeB = beginB;
        fEndTimeB = endB;
        fStatistic = statistic;
        fTraceListA = traceListA;
        fTraceListB = traceListB;
    }

    /**
     * Constructor
     *
     * @param source
     *            the class that has generated the signal
     * @param statistic
     *            the statistic that will be represented in the flame graph
     * @param traceListA
     *            the list of traces in group A
     * @param traceListB
     *            the list of traces in group B
     */
    public TmfComparisonFilteringUpdatedSignal(Object source, @Nullable String statistic, @Nullable List<String> traceListA, @Nullable List<String> traceListB) {
        this(source, TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH, TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH, statistic, traceListA, traceListB);
    }

    /**
     * Get beginning of the timestamp in Group A
     *
     * @return The beginning of the timestamp of the selection in GroupA
     */
    public ITmfTimestamp getBeginTimeA() {
        return fBeginTimeA;
    }

    /**
     * Get the end of the timestamp in Group A
     *
     * @return The end of the timestamp of the selection in GroupA
     */
    public ITmfTimestamp getEndTimeA() {
        return fEndTimeA;
    }

    /**
     * Get beginning of the timestamp in Group B
     *
     * @return The beginning of the timestamp of the selection in GroupB
     */
    public ITmfTimestamp getBeginTimeB() {
        return fBeginTimeB;
    }

    /**
     * Get the end of the timestamp in Group B
     *
     * @return The end of the timestamp of the selection in GroupB
     */
    public ITmfTimestamp getEndTimeB() {
        return fEndTimeB;
    }

    /**
     * Get the statistic type
     *
     * @return the type of statistic that will be represented in flame graph
     */
    public @Nullable String getStatistic() {
        return fStatistic;
    }

    /**
     * Get the list of traces in Group A
     *
     * @return the list of traces in Group A
     */
    public @Nullable List<String> getTraceListA() {
        return fTraceListA;
    }

    /**
     * Get the list of traces in Group B
     *
     * @return the list of traces in Group B
     */
    public @Nullable List<String> getTraceListB() {
        return fTraceListB;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" ["); //$NON-NLS-1$
        if (!TmfTimestamp.BIG_BANG.equals(fBeginTimeA)) {
            sb.append(fBeginTimeA.toString());
            if (!fBeginTimeA.equals(fEndTimeA)) {
                sb.append('-');
                sb.append(fEndTimeA.toString());
            }
        }
        if (!TmfTimestamp.BIG_BANG.equals(fBeginTimeB)) {
            sb.append(fBeginTimeB.toString());
            if (!fBeginTimeB.equals(fEndTimeB)) {
                sb.append('-');
                sb.append(fEndTimeB.toString());
            }
        }
        if (fStatistic != null) {
            sb.append(fStatistic);
        }

        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }

}
