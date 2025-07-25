/*******************************************************************************
 * Copyright (c) 2018, 2024 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.OutputConfigurationQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.ErrorResponseImpl;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.webapp.TraceServerConfiguration;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.webapp.WebApplication;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.DataProviderDescriptorStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.ExperimentModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TraceModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.webapp.TestWebApplication;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.ImmutableList;

/**
 * Rest server test abstract class, handles starting the server in test mode,
 * getting the correct client, and closing the traces and server once the test
 * is complete
 *
 * @author Loic Prieur-Drevon
 */
@SuppressWarnings("null")
public abstract class RestServerTest {
    private static final String ERROR_CODE_STR = ", error code=";
    private static final String SERVER = "http://localhost:8378/tsp/api"; //$NON-NLS-1$
    private static final WebApplication fWebApp = new TestWebApplication(new TraceServerConfiguration(TraceServerConfiguration.TEST_PORT, false, null, null));
    private static final Bundle TEST_BUNDLE = Platform.getBundle("org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests");
    private static final String CONFIG_FOLDER_NAME = "config";

    private static final String CLOCK_OFFSET_KEY = "clock_offset"; //$NON-NLS-1$
    private static final String CLOCK_SCALE_KEY = "clock_scale"; //$NON-NLS-1$
    private static final String DOMAIN_KEY = "domain"; //$NON-NLS-1$
    private static final String HOSTNAME_KEY = "hostname"; //$NON-NLS-1$
    private static final String HOST_ID_KEY = "host ID"; //$NON-NLS-1$
    private static final String KERNEL_RELEASE_KEY = "kernel_release";
    private static final String KERNEL_VERSION_KEY = "kernel_version";
    private static final String SYSNAME_KEY = "sysname";
    private static final String TRACER_MAJOR_KEY = "tracer_major"; //$NON-NLS-1$
    private static final String TRACER_MINOR_KEY = "tracer_minor"; //$NON-NLS-1$
    private static final String TRACER_NAME_KEY = "tracer_name"; //$NON-NLS-1$
    private static final String TRACER_PATCHLEVEL_KEY = "tracer_patchlevel";

    /**
     * No parameter string
     */
    protected static final String NO_PARAMETERS = "no-parameters";

    /**
     * Invalid experiment UUID
     */
    protected static final String INVALID_EXP_UUID = "unknown.experiment.id";

    /**
     * Unknown experiment UUID
     */
    protected static final String UNKNOWN_EXP_UUID = UUID.nameUUIDFromBytes(Objects.requireNonNull(INVALID_EXP_UUID.getBytes(Charset.defaultCharset()))).toString();

    /**
     * Unknown data provider ID
     */
    protected static final String UNKNOWN_DP_ID = "unknown.dp.id";

    /**
     * Callstack data provider ID
     */
    protected static final String CALL_STACK_DATAPROVIDER_ID = "org.eclipse.tracecompass.internal.analysis.profiling.callstack.provider.CallStackDataProvider";

    /**
     * Requested times key
     */
    protected static final String REQUESTED_TIMES_KEY = "requested_times";

    /**
     * Traces endpoint path (relative to application).
     */
    public static final String TRACES = "traces";
    /**
     * Experiments endpoint path (relative to application).
     */
    public static final String EXPERIMENTS = "experiments";
    /**
     * Bookmarks endpoint path (relative to application).
     */
    public static final String BOOKMARKS = "bookmarks";

    /**
     * Outputs path segment
     */
    public static final String OUTPUTS_PATH = "outputs";

    /**
     * Marker sets path segment
     */
    public static final String MARKER_SETS = "markerSets";

    /**
     * Tree path segment
     */
    public static final String TREE_PATH = "tree";

    /**
     * Data Tree path segment
     */
    public static final String DATATREE_PATH = "data";

    /**
     * Time Graph path segment
     */
    public static final String TIMEGRAPH_PATH = "timeGraph";

    /**
     * XY path segment
     */
    public static final String XY_PATH = "XY";

    /**
     * XY series path segment
     */
    public static final String XY_SERIES_PATH = "xy";

    /**
     * States path segment
     */
    public static final String STATE_PATH = "states";

    /**
     * Arrows path segment
     */
    public static final String ARROWS_PATH = "arrows";

    /**
     * Tooltip path segment
     */
    public static final String TOOLTIP_PATH = "tooltip";

    /**
     * Table path segment
     */
    public static final String TABLE_PATH = "table";

    /**
     * Styles path segment
     */
    public static final String STYLES_PATH = "style";

    /**
     * Annotation path segment
     */
    public static final String ANNOTATIONS_PATH = "annotations";

    /**
     * Column path segment
     */
    public static final String TABLE_COLUMN_PATH = "columns";

    /**
     * Lines path segment
     */
    public static final String TABLE_LINE_PATH = "lines";

    /**
     * ConfigTypes path
     */
    public static final String DP_CONFIG_TYPES_PATH = "configTypes";

    /**
     * <b>name</b> constant
     */
    public static final String NAME = "name";
    /**
     * <b>path</b> constant
     */
    public static final String URI = "uri";

    /**
     * <b>typeID</b> constant
     */
    public static final String TYPE_ID = "typeID";

    /**
     * Configuration service root path
     */
    public static final String CONFIG_PATH = "config";

    /**
     * Configuration types path segment
     */
    public static final String TYPES_PATH = "types";

    /**
     * Configuration instances path segment
     */
    public static final String CONFIG_INSTANCES_PATH = "configs";

    /**
     * Filename with valid json configuration
     */
    public static final String VALID_JSON_FILENAME = "custom-execution-analysis.json";

    private static final GenericType<Set<TraceModelStub>> TRACE_MODEL_SET_TYPE = new GenericType<>() {
    };
    private static final GenericType<Set<ExperimentModelStub>> EXPERIMENT_MODEL_SET_TYPE = new GenericType<>() {
    };
    private static final GenericType<Set<DataProviderDescriptorStub>> DATAPROVIDER_DESCR_MODEL_SET_TYPE = new GenericType<>() {
    };

    /**
     * Callstack data provider descriptor
     */
    protected static final DataProviderDescriptorStub EXPECTED_CALLSTACK_PROVIDER_DESCRIPTOR = new DataProviderDescriptorStub(
            null,
            CALL_STACK_DATAPROVIDER_ID,
            "Flame Chart",
            "Show a call stack over time",
            ProviderType.TIME_GRAPH.name(),
            null,
            null);

    /**
     * {@link TraceModelStub} to represent the object returned by the server for
     * {@link CtfTestTrace#CONTEXT_SWITCHES_UST}.
     */
    protected static TraceModelStub sfContextSwitchesUstStub;

    /**
     * {@link TraceModelStub} to represent the object returned by the server for
     * {@link CtfTestTrace#CONTEXT_SWITCHES_UST} without metadata initialized.
     */
    protected static TraceModelStub sfContextSwitchesUstNotInitializedStub;

    /**
     * The name used when posting the trace.
     */
    protected static final String CONTEXT_SWITCHES_UST_NAME = "ust";

    /**
     * The properties of the trace.
     */
    protected static final Map<String, String> CONTEXT_SWITCHES_UST_PROPERTIES = new HashMap<>(Map.ofEntries(
            Map.entry(HOSTNAME_KEY, "\"qemu1\""),
            Map.entry(CLOCK_OFFSET_KEY, "1450192743562703624"),
            Map.entry(DOMAIN_KEY, "\"ust\""),
            Map.entry(HOST_ID_KEY, "\"40b6dd3a-c130-431e-92ef-8c4dafe14627\""),
            Map.entry(TRACER_NAME_KEY, "\"lttng-ust\""),
            Map.entry(CLOCK_SCALE_KEY, "1.0"),
            Map.entry(TRACER_MAJOR_KEY, "2"),
            Map.entry(TRACER_MINOR_KEY, "6")
        ));

    /**
     * {@link TraceModelStub} to represent the object returned by the server for
     * {@link CtfTestTrace#CONTEXT_SWITCHES_KERNEL}.
     */
    protected static TraceModelStub sfContextSwitchesKernelStub;

    /**
     * {@link TraceModelStub} to represent the object returned by the server for
     * {@link CtfTestTrace#CONTEXT_SWITCHES_KERNEL} without metadata initialized.
     */
    protected static TraceModelStub sfContextSwitchesKernelNotInitializedStub;

    /**
     * The name used when posting the trace.
     */
    protected static final String CONTEXT_SWITCHES_KERNEL_NAME = "kernel";

    /**
     * The properties of the trace.
     */
    protected static final Map<String, String> CONTEXT_SWITCHES_KERNEL_PROPERTIES = new HashMap<>(Map.ofEntries(
            Map.entry(HOSTNAME_KEY, "\"qemu1\""),
            Map.entry(KERNEL_VERSION_KEY, "\"#1 SMP PREEMPT Sat Dec 12 14:52:43 CET 2015\""),
            Map.entry(TRACER_PATCHLEVEL_KEY, "3"),
            Map.entry(CLOCK_OFFSET_KEY, "1450192747804379176"),
            Map.entry(DOMAIN_KEY, "\"kernel\""),
            Map.entry(SYSNAME_KEY, "\"Linux\""),
            Map.entry(HOST_ID_KEY, "\"40b6dd3a-c130-431e-92ef-8c4dafe14627\""),
            Map.entry(KERNEL_RELEASE_KEY, "\"4.1.13-WR8.0.0.0_standard\""),
            Map.entry(TRACER_NAME_KEY, "\"lttng-modules\""),
            Map.entry(CLOCK_SCALE_KEY, "1.0"),
            Map.entry(TRACER_MAJOR_KEY, "2"),
            Map.entry(TRACER_MINOR_KEY, "6")
        ));

    /**
     * {@link TraceModelStub} to represent the object returned by the server for
     * {@link CtfTestTrace#ARM_64_BIT_HEADER}, with the same name as {@link #sfContextSwitchesKernelStub}
     */
    protected static TraceModelStub sfArm64KernelStub;

    /**
     * {@link TraceModelStub} to represent the object returned by the server for
     * {@link CtfTestTrace#ARM_64_BIT_HEADER} without metadata initialized.
     */
    protected static TraceModelStub sfArm64KernelNotIntitialzedStub;

    /**
     * The name used when posting the trace.
     */
    protected static final String ARM_64_KERNEL_NAME = "kernel";

    /**
     * The properties of the trace.
     */
    protected static final Map<String, String> ARM_64_KERNEL_PROPERTIES = new HashMap<>(Map.ofEntries(
            Map.entry(HOSTNAME_KEY, "\"lager\""),
            Map.entry(KERNEL_VERSION_KEY, "\"#6 SMP PREEMPT Wed Oct 1 17:07:11 CEST 2014\""),
            Map.entry(TRACER_PATCHLEVEL_KEY, "0"),
            Map.entry(CLOCK_OFFSET_KEY, "1412663327522716450"),
            Map.entry(DOMAIN_KEY, "\"kernel\""),
            Map.entry(SYSNAME_KEY, "\"Linux\""),
            Map.entry(HOST_ID_KEY, "\"5a71a43c-1390-4365-9baf-111c565e78c3\""),
            Map.entry(KERNEL_RELEASE_KEY, "\"3.10.31-ltsi\""),
            Map.entry(CLOCK_SCALE_KEY, "1.0"),
            Map.entry(TRACER_NAME_KEY, "\"lttng-modules\""),
            Map.entry(TRACER_MAJOR_KEY, "2"),
            Map.entry(TRACER_MINOR_KEY, "5")
        ));

    /**
     * Expected toString() of all data providers for this experiment
     */
    protected static List<DataProviderDescriptorStub> sfExpectedDataProviderDescriptorStub = null;

    /**
     * Create the {@link TraceModelStub}s before running the tests
     *
     * @throws IOException
     *             if the URL could not be converted to a path
     */
    @BeforeClass
    public static void beforeTest() throws IOException {
        String contextSwitchesUstPath = FileLocator.toFileURL(CtfTestTrace.CONTEXT_SWITCHES_UST.getTraceURL()).getPath().replaceAll("/$", "");
        sfContextSwitchesUstNotInitializedStub = new TraceModelStub(CONTEXT_SWITCHES_UST_NAME, contextSwitchesUstPath, Collections.emptyMap());
        sfContextSwitchesUstStub = new TraceModelStub(CONTEXT_SWITCHES_UST_NAME, contextSwitchesUstPath, CONTEXT_SWITCHES_UST_PROPERTIES);

        String contextSwitchesKernelPath = FileLocator.toFileURL(CtfTestTrace.CONTEXT_SWITCHES_KERNEL.getTraceURL()).getPath().replaceAll("/$", "");
        sfContextSwitchesKernelNotInitializedStub = new TraceModelStub(CONTEXT_SWITCHES_KERNEL_NAME, contextSwitchesKernelPath, Collections.emptyMap());
        sfContextSwitchesKernelStub = new TraceModelStub(CONTEXT_SWITCHES_KERNEL_NAME, contextSwitchesKernelPath, CONTEXT_SWITCHES_KERNEL_PROPERTIES);

        String arm64Path = FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).getPath().replaceAll("/$", "");
        sfArm64KernelNotIntitialzedStub = new TraceModelStub(ARM_64_KERNEL_NAME, arm64Path, Collections.emptyMap());
        sfArm64KernelStub = new TraceModelStub(ARM_64_KERNEL_NAME, arm64Path, ARM_64_KERNEL_PROPERTIES);

        ImmutableList.Builder<DataProviderDescriptorStub> b = ImmutableList.builder();
        b.add(new DataProviderDescriptorStub(null, "org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.scatter.dataprovider:org.eclipse.linuxtools.lttng2.ust.analysis.callstack",
                "LTTng-UST CallStack - Latency vs Time",
                "Show latencies provided by Analysis module: LTTng-UST CallStack",
                ProviderType.TREE_TIME_XY.name(), null, null));
        b.add(EXPECTED_CALLSTACK_PROVIDER_DESCRIPTOR);
        b.add(new DataProviderDescriptorStub(null,"org.eclipse.tracecompass.internal.tmf.core.histogram.HistogramDataProvider",
                "Histogram",
                "Show a histogram of number of events to time for a trace",
                ProviderType.TREE_TIME_XY.name(), null, null));
        sfExpectedDataProviderDescriptorStub = b.build();
    }

    /**
     * Start the Eclipse / Jetty Web server
     *
     * @throws Exception
     *             if there is a problem running this application.
     */
    @Before
    public void startServer() throws Exception {
        fWebApp.start();
    }

    /**
     * Stop the server once tests are finished, and close the traces
     */
    @After
    public void stopServer() {
        WebTarget application = getApplicationEndpoint();
        WebTarget experimentsTarget = application.path(EXPERIMENTS);
        for (ExperimentModelStub experiment: getExperiments(experimentsTarget)) {
            try (Response response = experimentsTarget.path(experiment.getUUID().toString()).request().delete()) {
                assertEquals(experiment, response.readEntity(ExperimentModelStub.class));
            }
        }
        WebTarget traceTarget = application.path(TRACES);
        for (TraceModelStub trace : getTraces(traceTarget)) {
            try (Response response = traceTarget.path(trace.getUUID().toString()).request().delete()) {
                assertEquals(trace, response.readEntity(TraceModelStub.class));
            }
        }
        assertEquals(Collections.emptySet(), getTraces(traceTarget));
        assertEquals(Collections.emptySet(), getExperiments(experimentsTarget));
        fWebApp.stop();
    }

    /**
     * Getter for the {@link WebTarget} for the application endpoint.
     *
     * @return the application endpoint {@link WebTarget}.
     */
    public static WebTarget getApplicationEndpoint() {
        Client client = ClientBuilder.newClient();
        client.register(JacksonJsonProvider.class);
        return client.target(SERVER);
    }

    /**
     * Get the {@link WebTarget} for the table columns endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getTableColumnsEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(TABLE_PATH)
                .path(dataProviderId)
                .path(TABLE_COLUMN_PATH);
    }

    /**
     * Get the {@link WebTarget} for the table lines endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getTableLinesEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(TABLE_PATH)
                .path(dataProviderId)
                .path(TABLE_LINE_PATH);
    }


    /**
     * Get the {@link WebTarget} for the data-tree tree endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getDataTreeEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(DATATREE_PATH)
                .path(dataProviderId)
                .path(TREE_PATH);
    }

    /**
     * Get the {@link WebTarget} for the time graph tree endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getTimeGraphTreeEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(TIMEGRAPH_PATH)
                .path(dataProviderId)
                .path(TREE_PATH);
    }

    /**
     * Get the {@link WebTarget} for the time graph state endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph state endpoint
     */
    public static WebTarget getTimeGraphStatesEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(TIMEGRAPH_PATH)
                .path(dataProviderId)
                .path(STATE_PATH);
    }

    /**
     * Get the {@link WebTarget} for the time graph data provider arrows endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getArrowsEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(TIMEGRAPH_PATH)
                .path(dataProviderId)
                .path(ARROWS_PATH);
    }

    /**
     * Get the {@link WebTarget} for the time graph tooltip endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tooltip endpoint
     */
    public static WebTarget getTimeGraphTooltipEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(TIMEGRAPH_PATH)
                .path(dataProviderId)
                .path(TOOLTIP_PATH);
    }

    /**
     * Get the {@link WebTarget} for the XY tree endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The XY tree endpoint
     */
    public static WebTarget getXYTreeEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(XY_PATH)
                .path(dataProviderId)
                .path(TREE_PATH);
    }

    /**
     * Get the {@link WebTarget} for the XY series endpoint.
     *
     * @param expUUID     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The XY series endpoint
     */
    public static WebTarget getXYSeriesEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(XY_PATH)
                .path(dataProviderId)
                .path(XY_SERIES_PATH);
    }

    /**
     * Get the {@link WebTarget} for the configTypes endpoint of a given data provider.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree configTypes endpoint
     */
    public static WebTarget getConfigEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(dataProviderId)
                .path(DP_CONFIG_TYPES_PATH);
    }

    /**
     * Get the {@link WebTarget} for data provide creation endpoint of a given data provider.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree configTypes endpoint
     */
    public static WebTarget getDpCreationEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(dataProviderId);
    }



    /**
     * Get the traces currently open on the server.
     *
     * @param traces
     *            traces endpoint on the server
     * @return list of currently open traces.
     */
    public static Set<TraceModelStub> getTraces(WebTarget traces) {
        return traces.request(MediaType.APPLICATION_JSON).get(TRACE_MODEL_SET_TYPE);
    }

    /**
     * Get the experiments currently open on the server.
     *
     * @param experiments
     *            experiment endpoint on the server.
     * @return list of currently open experiments.
     */
    public static Set<ExperimentModelStub> getExperiments(WebTarget experiments) {
        return experiments.request(MediaType.APPLICATION_JSON).get(EXPERIMENT_MODEL_SET_TYPE);
    }

    /**
     * Get a set of {@link DataProviderDescriptorStub}
     *
     * @param outputs
     *            {@link WebTarget} for the outputs endpoint
     * @return Set of {@link DataProviderDescriptorStub} given by the server
     */
    public static Set<DataProviderDescriptorStub> getDataProviderDescriptors(WebTarget outputs) {
        return outputs.request(MediaType.APPLICATION_JSON).get(DATAPROVIDER_DESCR_MODEL_SET_TYPE);
    }

    /**
     * Get the {@link WebTarget} for the experiment's marker sets
     *
     * @param expUUID
     *            Experiment UUID
     * @return marker sets model
     */
    public static WebTarget getMarkerSetsEndpoint(String expUUID) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(MARKER_SETS);
    }

    /**
     * Get the {@link WebTarget} for the data provider styles tree endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getStylesEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(dataProviderId)
                .path(STYLES_PATH);
    }


    /**
     * Get the {@link WebTarget} for the time graph data provider annotation categories endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @param markerSetId
     *            The marker set ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getAnnotationCategoriesEndpoint(String expUUID, String dataProviderId, String markerSetId) {
        WebTarget webTarget = getApplicationEndpoint()
                .path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(dataProviderId)
                .path(ANNOTATIONS_PATH);
        if (markerSetId != null) {
            webTarget = webTarget.queryParam("markerSetId", markerSetId);
        }
        return webTarget;
    }

    /**
     * Get the {@link WebTarget} for the time graph data provider annotation categories endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getAnnotationCategoriesEndpoint(String expUUID, String dataProviderId) {
        return getAnnotationCategoriesEndpoint(expUUID, dataProviderId, null);
    }

    /**
     * Get the {@link WebTarget} for the time graph data provider annotation endpoint.
     *
     * @param expUUID
     *            Experiment UUID
     * @param dataProviderId
     *            Data provider ID
     * @return The time graph tree endpoint
     */
    public static WebTarget getAnnotationEndpoint(String expUUID, String dataProviderId) {
        return getApplicationEndpoint().path(EXPERIMENTS)
                .path(expUUID)
                .path(OUTPUTS_PATH)
                .path(dataProviderId)
                .path(ANNOTATIONS_PATH);
    }

    /**
     * Post the trace from an expected {@link TraceModelStub}, ensure that the post
     * returned correctly and that the returned model was that of the expected stub.
     *
     * @param traces
     *            traces endpoint
     * @param stub
     *            expected trace stub
     * @return the resulting stub
     */
    public static TraceModelStub assertPost(WebTarget traces, TraceModelStub stub) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(NAME, stub.getName());
        parameters.put(URI, stub.getPath());
        try (Response response = traces.request().post(Entity.json(new QueryParameters(parameters , Collections.emptyList())))) {
            int code = response.getStatus();
            assertEquals("Failed to POST " + stub.getName() + ERROR_CODE_STR + code, 200, code);
            TraceModelStub result = response.readEntity(TraceModelStub.class);
            assertEquals(stub, result);
            return result;
        }
    }

    /**
     * Post an experiment from a list of {@link TraceModelStub}, ensure that the
     * post returned correctly and that the returned model was that of the
     * expected stub.
     *
     * @param name
     *            experiment name
     * @param traces
     *            traces to include in experiment
     * @return the resulting experiment stub
     */
    public static ExperimentModelStub assertPostExperiment(String name, TraceModelStub... traces) {
        WebTarget application = getApplicationEndpoint();
        List<String> traceUUIDs = new ArrayList<>();
        for (TraceModelStub trace : traces) {
            TraceModelStub traceStub = assertPost(application.path(TRACES), trace);
            traceUUIDs.add(traceStub.getUUID().toString());
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(NAME, name);
        parameters.put(TRACES, traceUUIDs);
        try (Response response = application.path(EXPERIMENTS).request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())))) {
            assertEquals("Failed to POST experiment " + name + ERROR_CODE_STR + response.getStatus(), 200, response.getStatus());
            return response.readEntity(ExperimentModelStub.class);
        }
    }

    /**
     * @param dpConfigEndpoint
     *            the dp config endpoint to create a derived data provider
     * @param configuration
     *            the configuration with input parameters to post
     * @return the derived data provider descriptor stub
     */
    public static DataProviderDescriptorStub assertDpPost(WebTarget dpConfigEndpoint, ITmfConfiguration configuration) {
        try (Response response = dpConfigEndpoint.request().post(Entity.json(
                new OutputConfigurationQueryParameters(configuration.getName(), configuration.getDescription(), configuration.getSourceTypeId(), configuration.getParameters())))) {
            int code = response.getStatus();
            assertEquals("Failed to POST " + configuration.getName() + ERROR_CODE_STR + code, 200, code);
            DataProviderDescriptorStub result = response.readEntity(DataProviderDescriptorStub.class);
            assertEquals(configuration.getName(), result.getConfiguration().getName());
            assertEquals(configuration.getDescription(), result.getConfiguration().getDescription());
            assertEquals(configuration.getSourceTypeId(), result.getConfiguration().getSourceTypeId());
            assertEquals(configuration.getParameters(), result.getConfiguration().getParameters());
            return result;
        }
    }

    /**
     * Request to create a derived DP but will cause errors
     *
     * @param dpConfigEndpoint
     *            the dp config endpoint to create a derived data provider
     * @param configuration
     *            the configuration with input parameters to post
     * @return error code
     */
    public static Response assertDpPostWithErrors(WebTarget dpConfigEndpoint, ITmfConfiguration configuration) {
        return dpConfigEndpoint.request().post(Entity.json(
                new OutputConfigurationQueryParameters(configuration.getName(), configuration.getDescription(), configuration.getSourceTypeId(), configuration.getParameters())));
    }

    /**
     * @param jsonFileName
     *            the json file to read in config folder
     * @return json parameters as Map<String, Object>
     * @throws IOException
     *             if such exception occurs
     * @throws URISyntaxException
     *             if such exception occurs
     */
    public static Map<String, Object> readParametersFromJson(String jsonFileName) throws IOException, URISyntaxException {
        IPath defaultPath = new org.eclipse.core.runtime.Path(CONFIG_FOLDER_NAME).append(jsonFileName);
        URL url = FileLocator.find(TEST_BUNDLE, defaultPath, null);
        File jsonFile = new File(FileLocator.toFileURL(url).toURI());
        try (InputStream inputStream = new FileInputStream(jsonFile)) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        }
    }

    /**
     * Interface to implement to resolve an endpoint based on experiment and data provider ID,
     * for example states, arrows etc.
     */
    protected interface IEndpointResolver {
        /**
         * Method to get endpoint
         * @param expUUID
         *          The experiment UUID
         * @param dataProviderId
         *          The data provider ID
         * @return the endpoint
         */
        WebTarget getEndpoint(String expUUID, String dataProviderId);
    }

    /**
     * Call method to execute common error test cases for a given endpoint.
     *
     * @param exp
     *            the experiment
     * @param resolver
     *            the endpoint resolver
     * @param dpId
     *            the data provider ID
     * @param hasParameters
     *            whether the endpoint requires parameters (to test empty parameter map)
     */
    protected static void executePostErrorTests (ExperimentModelStub exp, IEndpointResolver resolver, String dpId, boolean hasParameters) {
        // Invalid UUID string
        WebTarget endpoint = resolver.getEndpoint(INVALID_EXP_UUID, dpId);
        Map<String, Object> parameters = new HashMap<>();
        try (Response response = endpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())))) {
            assertNotNull(response);
            assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }

        // Unknown experiment
        endpoint = resolver.getEndpoint(UUID.randomUUID().toString(), dpId);
        try (Response response = endpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())))) {
            assertNotNull(response);
            assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals(EndpointConstants.NO_SUCH_TRACE, response.readEntity(ErrorResponseImpl.class).getTitle());
        }

        // Missing parameters
        endpoint = resolver.getEndpoint(exp.getUUID().toString(), dpId);
        try (Response response = endpoint.request().post(Entity.json(NO_PARAMETERS))) {
            assertNotNull(response);
            assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }

        if (hasParameters) {
            // Missing parameters
            endpoint = resolver.getEndpoint(exp.getUUID().toString(), dpId);
            try (Response response = endpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())))) {
                assertNotNull(response);
                assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            }
        }

        // Unknown data provider
        endpoint = resolver.getEndpoint(exp.getUUID().toString(), UNKNOWN_DP_ID);
        try (Response response = endpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())))) {
            assertNotNull(response);
            assertEquals(Status.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatus());
            assertEquals(EndpointConstants.NO_PROVIDER, response.readEntity(ErrorResponseImpl.class).getTitle());
        }
    }
}
