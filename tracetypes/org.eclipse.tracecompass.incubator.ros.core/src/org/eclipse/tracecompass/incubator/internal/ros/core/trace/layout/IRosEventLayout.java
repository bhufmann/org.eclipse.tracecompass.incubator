/**********************************************************************
 * Copyright (c) 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.incubator.internal.ros.core.trace.layout;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * This interface represents the events layout as defined in the tracetools ROS
 * package for tracing.
 * <p>
 * This could eventually support different versions of tracetools in case they
 * have different event definitions.
 *
 * @author Christophe Bedard
 */
@SuppressWarnings("javadoc")
@NonNullByDefault
public interface IRosEventLayout {

    public static String PROVIDER_NAME = "roscpp:"; //$NON-NLS-1$

    /**
     * The default layout
     *
     * @return the default layout
     */
    public static IRosEventLayout getDefault() {
        return Ros021EventLayout.getInstance();
    }

    /**
     * Get all event names defined here
     *
     * @return the event names
     */
    public default Collection<String> getEventNames() {
        Collection<String> eventNames = Lists.newArrayList();
        Iterables.filter(Arrays.asList(IRosEventLayout.class.getMethods()),
                method -> method.getName().startsWith("event")) //$NON-NLS-1$
                .forEach(eventMethod -> {
                    try {
                        eventNames.add(checkNotNull((String) eventMethod.invoke(this)));
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    }
                });
        return ImmutableList.copyOf(checkNotNull(eventNames));
    }

    // ------------------------------------------------------------------------
    // Events
    // ------------------------------------------------------------------------

    /**
     * <code>init_node</code>,
     * when a node is initialized
     */
    String eventInitNode();

    /**
     * <code>shutdown_node</code>,
     * when a node is shut down
     */
    String eventShutdownNode();

    /**
     * <code>subscriber_callback_added</code>,
     * when a subscriber callback is added
     */
    String eventSubCallbackAdded();

    /**
     * <code>callback_start</code>,
     * before the subscriber's callback is called
     */
    String eventCallbackStart();

    /**
     * <code>subscriber_callback_start</code>,
     * when the subscriber's callback is called
     */
    String eventSubCallbackStart();

    /**
     * <code>subscriber_callback_end</code>,
     * when the subscriber's callback has finished
     */
    String eventSubCallbackEnd();

    /**
     * <code>callback_end</code>,
     * after the subscriber's callback has finished
     */
    String eventCallbackEnd();

    /**
     * <code>subscription_message_queued</code>,
     * when a message is queued on the subscriber's side, after reception
     */
    String eventSubMsgQueued();

    /**
     * <code>subscription_message_dropped</code>,
     * when a message is dropped on the subscriber's side, usually right after a
     * queued message makes the queue go over its allowed size
     */
    String eventSubMsgDropped();

    /**
     * <code>publisher_message_queued</code>,
     * when a message is queued on the publisher's side, before transmission
     */
    String eventPubMsgQueued();

    /**
     * <code>publisher_link_handle_message</code>,
     * when ?
     */
    String eventPubLinkHandleMsg();

    /**
     * <code>task_start</code>,
     * when a one-time task or setup has started
     */
    String eventTaskStart();

    /**
     * <code>timer_added</code>,
     * when a timer is created and added in a node
     */
    String eventTimerAdded();

    /**
     * <code>timer_scheduled</code>,
     * when a timer is scheduled to
     */
    String eventTimerScheduled();

    /**
     * <code>new_connection</code>,
     * when ?
     */
    String eventNewConnection();

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    String fieldNodeName();
    String fieldRosCppVersion();
    String fieldQueueRef();
    String fieldCallbackRef();
    String fieldTypeInfo();
    String fieldDataType();
    String fieldSourceName();
    String fieldQueueSize();
    String fieldTopic();
    String fieldBufferRef();
    String fieldDataRef();
    String fieldTracingId();
    String fieldMsgRef();
    String fieldReceiptTimeSec();
    String fieldReceiptTimeNsec();
    String fieldTaskName();
    String fieldFunctionName();
    String fieldPeriodSec();
    String fieldPeriodNsec();
    String fieldCallbackQueueCbRef();
    String fieldLocalHostport();
    String fieldRemoteHostport();
    String fieldChannelRef();
    String fieldChannelType();
    String fieldName();

    // Context fields
    String contextVpid();
    String contextVtid();
    String contextProcname();
}
