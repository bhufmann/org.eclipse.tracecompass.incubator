/**********************************************************************
 * Copyright (c) 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.incubator.internal.ros.core.analysis.tasks;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.incubator.internal.ros.core.trace.layout.IRosEventLayout;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisEventRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfCompositeAnalysisRequirement;

import com.google.common.collect.ImmutableSet;

/**
 * Analysis requirement implementation for ROS Tasks analysis
 *
 * @author Christophe Bedard
 */
@NonNullByDefault
public class RosTasksAnalysisRequirement extends TmfCompositeAnalysisRequirement {

    /**
     * Constructor
     *
     * @param layout
     *            the event layout
     */
    public RosTasksAnalysisRequirement(IRosEventLayout layout) {
        super(getSubRequirements(layout), PriorityLevel.MANDATORY);
    }

    private static Collection<TmfAbstractAnalysisRequirement> getSubRequirements(IRosEventLayout layout) {
        // Requirement on task_start event
        TmfAnalysisEventRequirement taskStartReq = new TmfAnalysisEventRequirement(
                ImmutableSet.of(checkNotNull(layout.eventTaskStart())),
                PriorityLevel.MANDATORY);

        return ImmutableSet.of(taskStartReq);
    }
}
