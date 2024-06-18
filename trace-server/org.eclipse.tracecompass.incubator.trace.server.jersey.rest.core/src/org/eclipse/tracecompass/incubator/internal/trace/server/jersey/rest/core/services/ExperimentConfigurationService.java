/*******************************************************************************
 * Copyright (c) 2023 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.CFG_CONFIG_ID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.CFG_TYPE_ID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.DT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.EXP_UUID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_SUCH_TRACE;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSource;
import org.eclipse.tracecompass.tmf.core.config.ITmfExperimentConfigSource;
import org.eclipse.tracecompass.tmf.core.config.TmfConfigurationSourceManager;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfConfigurationException;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Service to query the {@link ITmfExperimentConfigSource}s
 */
@Path("/experiments/{expUUID}/config")
public class ExperimentConfigurationService {
    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(ExperimentConfigurationService.class);

    private final TmfConfigurationSourceManager fConfigSourceManager = TmfConfigurationSourceManager.getInstance();

    /**
     * @param expUUID
     * @param typeId
     * @param queryParameters
     * @return
     */
    @POST
    @Path("/types/{typeId}/configs")
    @Tag(name = DT)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
        public Response applyConfiguration(
                @PathParam("expUUID") UUID expUUID,
                @PathParam("typeId") String typeId,
                QueryParameters queryParameters) {

        Map<String, Object> params = queryParameters.getParameters();
        try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "DataProviderService#applyConfiguration") //$NON-NLS-1$
                .setCategory(typeId).build()) {
            TmfExperiment experiment = ExperimentManagerService.getExperimentByUUID(expUUID);
            if (experiment == null) {
                return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
            }

            ITmfConfigurationSource configurationSource = fConfigSourceManager.getConfigurationSource(typeId);
            if (!(configurationSource instanceof ITmfExperimentConfigSource)) {
                return Response.status(Status.NOT_FOUND).entity("Configuration source type doesn't exist").build(); //$NON-NLS-1$
            }

            Object configId = params.get("configId"); //$NON-NLS-1$
            if (!(configId instanceof String)) {
                // No config for existing configuration is passed.
                // Try to create a new one.
                try {
                    ITmfConfiguration config = configurationSource.create(params);
                    configId = config.getId();
                } catch (TmfConfigurationException e) {
                    return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
//                    return Response.status(Status.NOT_FOUND).entity("Configuration instance doesn't exist for type " + typeId).build(); //$NON-NLS-1$
                }
            }
            ITmfConfiguration config = ((ITmfExperimentConfigSource) configurationSource).applyConfiguration((String) configId, experiment);
            return Response.ok(config).build();
        } catch (TmfConfigurationException e) {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * DELETE a configuration by type and instance id
     *
     * @param typeId
     *            the configuration source type ID
     * @param configId
     *            the configuration instance ID
     * @return status and the deleted configuration instance, if successful
     */
    @DELETE
    @Path("/types/{typeId}/configs/{configId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a configuration instance of a given configuration source type", responses = {
            @ApiResponse(responseCode = "200", description = "The configuration instance was successfully deleted", content = @Content(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Configuration.class))),
            @ApiResponse(responseCode = "404", description = EndpointConstants.NO_SUCH_CONFIGURATION, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal trace-server error while trying to delete configuration instance", content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response deleteConfiguration(@Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID, @Parameter(description = CFG_TYPE_ID) @PathParam("typeId") String typeId,
            @Parameter(description = CFG_CONFIG_ID) @PathParam("configId") String configId) {
        TmfExperiment experiment = ExperimentManagerService.getExperimentByUUID(expUUID);
        if (experiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }
        ITmfConfigurationSource configurationSource = fConfigSourceManager.getConfigurationSource(typeId);
        if (!(configurationSource instanceof ITmfExperimentConfigSource)) {
            return Response.status(Status.NOT_FOUND).entity("Configuration source type doesn't exist").build(); //$NON-NLS-1$
        }

        if (configId == null || !configurationSource.contains(configId)) {
            return Response.status(Status.NOT_FOUND).entity("Configuration instance doesn't exist for type " + typeId).build(); //$NON-NLS-1$
        }
        try {
            ((ITmfExperimentConfigSource) configurationSource).removeConfiguration(configId, experiment);
        } catch (TmfConfigurationException e) {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }
}
