/**********************************************************************
 * Copyright (c) 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.incubator.internal.ros.ui.views.queues;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tracecompass.incubator.internal.ros.core.analysis.AbstractRosStateProvider;
import org.eclipse.tracecompass.incubator.internal.ros.ui.views.AbstractRosPresentationProvider;
import org.eclipse.tracecompass.incubator.internal.ros.ui.views.Messages;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

import com.google.common.collect.ImmutableMap;

/**
 * ROS Queues presentation provider
 *
 * @author Christophe Bedard
 */
public class RosQueuesPresentationProvider extends AbstractRosPresentationProvider {

    /**
     * Constructor
     */
    public RosQueuesPresentationProvider() {
        // Nothing to do
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        String entryName = entry.getName();
        if (StringUtils.isNumeric(entryName)) {
            return Messages.AbstractRosPresentationProvider_QueuePosition;
        }
        return null;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        String label = event.getLabel();
        String entryName = event.getEntry().getName();
        String nodeName = Messages.AbstractRosPresentationProvider_Unknown;
        String topicName = Messages.AbstractRosPresentationProvider_Unknown;
        if (StringUtils.isNumeric(entryName)) {
            topicName = event.getEntry().getParent().getParent().getName();
            nodeName = event.getEntry().getParent().getParent().getParent().getParent().getName();
            builder.put(Messages.AbstractRosPresentationProvider_QueueReference, label);
        } else if (entryName.equals(AbstractRosStateProvider.QUEUE)) {
            topicName = event.getEntry().getParent().getName();
            nodeName = event.getEntry().getParent().getParent().getParent().getName();
            builder.put(Messages.AbstractRosPresentationProvider_QueueSize, label);
        } else if (entryName.equals(AbstractRosStateProvider.SUBSCRIBER_MESSAGE_PROCESSING)) {
            topicName = event.getEntry().getParent().getName();
            nodeName = event.getEntry().getParent().getParent().getParent().getName();
            builder.put(Messages.AbstractRosPresentationProvider_QueueReference, label);
        } else if (entryName.equals(AbstractRosStateProvider.DROPS)) {
            topicName = event.getEntry().getParent().getName();
            nodeName = event.getEntry().getParent().getParent().getParent().getName();
            builder.put(Messages.AbstractRosPresentationProvider_QueueReference, label);
        } else if (entryName.equals(AbstractRosStateProvider.CALLBACKS)) {
            nodeName = event.getEntry().getParent().getParent().getName();
            builder.put(Messages.AbstractRosPresentationProvider_QueueReference, label);
        }
        builder.put(Messages.AbstractRosPresentationProvider_TopicName, topicName);
        builder.put(Messages.AbstractRosPresentationProvider_NodeName, nodeName);
        return builder.build();
    }
}
