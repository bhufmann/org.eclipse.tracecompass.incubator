/*******************************************************************************
 * Copyright (c) 2023 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.incubator.internal.inandout.core.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.Activator;
import org.eclipse.tracecompass.incubator.internal.inandout.core.analysis.InAndOutAnalysisModule;
import org.eclipse.tracecompass.incubator.internal.inandout.core.analysis.SegmentSpecifierConfiguration;
import org.eclipse.tracecompass.internal.analysis.profiling.core.instrumented.FlameChartDataProvider;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSourceType;
import org.eclipse.tracecompass.tmf.core.config.ITmfDataProviderConfigSource;
import org.eclipse.tracecompass.tmf.core.config.ITmfDataProviderSource;
import org.eclipse.tracecompass.tmf.core.config.TmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.TmfConfigurationSourceType;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfConfigurationException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.osgi.framework.Bundle;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Implementation of {@link ITmfDataProviderConfigSource} for managing InAndOut configuration.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("null")
public class InAndOutConfigurationSource implements ITmfDataProviderConfigSource, ITmfDataProviderSource {
    private static final ITmfConfigurationSourceType CONFIG_SOURCE_TYPE;

    public static final String IN_AND_OUT_CONFIG_SOURCE_TYPE_ID = "org.eclipse.tracecompass.incubator.internal.inandout.core.config"; //$NON-NLS-1$
    private static final String NAME = "In And Out Analysis"; //$NON-NLS-1$
    public static final String DESCRIPTION = "Configure In And Out analysis using file description";

    private static final Table<String, ITmfTrace, SegmentSpecifierConfiguration> fTmfConfigurationTable = HashBasedTable.create();

    private final JsonUtils fJsonUtil = new JsonUtils(IN_AND_OUT_CONFIG_SOURCE_TYPE_ID);

    static {

        Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
        IPath defaultPath = new org.eclipse.core.runtime.Path("schema/in-and-out-analysis.json");  //$NON-NLS-1$
        URL url = FileLocator.find(bundle, defaultPath, null);
        File schemaFile = null;
        try {
            schemaFile = new File(FileLocator.toFileURL(url).toURI());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        CONFIG_SOURCE_TYPE = new TmfConfigurationSourceType.Builder()
                .setId(IN_AND_OUT_CONFIG_SOURCE_TYPE_ID)
                .setDescription(DESCRIPTION)
                .setName(NAME)
                .setSchemaFile(schemaFile)
                .build();
    }

    /**
     * Default Constructor
     */
    public InAndOutConfigurationSource() {
        TmfSignalManager.register(this);
    }

    @Override
    public ITmfConfigurationSourceType getConfigurationSourceType() {
        return CONFIG_SOURCE_TYPE;
    }

    @Override
    public ITmfConfiguration create(Map<String, Object> parameters, ITmfTrace trace, String srcDataProviderId) throws TmfConfigurationException {
        Object json = parameters.get(TmfConfiguration.JSON_STRING_KEY);
        if (!(json instanceof String)) {
            throw new TmfConfigurationException("No name json input provided or not a string");
        }
        SegmentSpecifierConfiguration config = SegmentSpecifierConfiguration.fromJsonString((String) json);
        if (fTmfConfigurationTable.contains(config.getId(), trace)) {
            throw new TmfConfigurationException("Configuration already existis with label: " + config.getName()); //$NON-NLS-1$
        }
        fTmfConfigurationTable.put(config.getId(), trace, config);
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
                fJsonUtil.writeConfiguration(tr, config, srcDataProviderId);
            }
        } else {
            fJsonUtil.writeConfiguration(trace, config, srcDataProviderId);
        }
        applyConfiguration(trace, config, srcDataProviderId);
        return config;
    }

    @Override
    public void dispose() {
        fTmfConfigurationTable.clear();
        TmfSignalManager.deregister(this);
    }

    @Override
    public @Nullable ITmfConfiguration remove(String configId, ITmfTrace trace, String srcDataProviderId) {
        ITmfConfiguration config = fTmfConfigurationTable.get(configId, trace);
        if (config == null) {
            return null;
        }

        Iterator<InAndOutAnalysisModule> csModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, InAndOutAnalysisModule.class).iterator();
        while (csModules.hasNext()) {
            InAndOutAnalysisModule csModule = csModules.next();
            SegmentSpecifierConfiguration specifiers = csModule.getSeSpecifierList();

            if (specifiers != null && specifiers.getId().equals(configId)) {
                try {
                    removeConfiguration(trace, config);
                } catch (TmfConfigurationException e) {
                    Activator.getInstance().logError("Error remove configurations for trace " + trace.getName(), e); //$NON-NLS-1$
                }
                csModule.clearPersistentData();
            }
        }
        return config;
    }

    /**
     * Signal handler for opened trace signal. Will populate trace
     * configurations
     *
     * @param signal
     *            the signal to handle
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (trace == null) {
            return;
        }
        try {
            Set<String> configIds = getConfigIds(trace);
            for (String configId : configIds) {
                SegmentSpecifierConfiguration config = fTmfConfigurationTable.get(configId, trace);
                if (config != null) {
                    applyConfiguration(trace, config);
                }
            }
        } catch (TmfConfigurationException e) {
            Activator.getInstance().logError("Error applying configurations for trace " + trace.getName(), e); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------
    private void applyConfiguration(ITmfTrace trace, SegmentSpecifierConfiguration config) throws TmfConfigurationException {
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
                applyConfiguration(tr, config);
            }
        }
        InAndOutAnalysisModule module = new InAndOutAnalysisModule(config);
        trace.addAnalysisModule(module);
    }

    private void removeConfiguration(ITmfTrace trace, ITmfConfiguration config) throws TmfConfigurationException {
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
                removeConfiguration(tr, config);
            }
        }
        fJsonUtil.deleteFromTrace(config, trace);
    }

    private Set<String> getConfigIds(ITmfTrace trace) throws TmfConfigurationException {
        Set<String> ret = new HashSet<>();
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
                ret.addAll(getConfigIds(tr));
            }
            return ret;
        }
        return fJsonUtil.getTraceConfigIds(trace);
    }

    @Override
    public boolean appliesToDataProvider(@NonNull String dataProviderId) {
        return dataProviderId.contains(InAndOutAnalysisModule.ID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull List<@NonNull ITmfConfiguration> getConfigurations(@NonNull String srcDataProviderId, @NonNull ITmfTrace trace) {
        return (List<@NonNull ITmfConfiguration>) fTmfConfigurationTable.get(srcDataProviderId, trace);
    }

    @Override
    public @NonNull List<@NonNull IDataProviderDescriptor> getDataProviderDescriptors(@NonNull String srcDataProviderId, @NonNull ITmfTrace trace, @NonNull String configId) throws TmfConfigurationException {

        IDataProviderFactory factory = DataProviderManager.getInstance().getFactory(FlameChartDataProvider.ID);
        List<IDataProviderDescriptor> descriptors = new ArrayList<>();
        if (factory != null) {
            for (IDataProviderDescriptor desc : factory.getDescriptors(trace)) {
                if (desc.getId().contains(configId)) {
                    descriptors.add(desc);
                }
            }
        }

        factory = DataProviderManager.getInstance().getFactory(SegmentStoreStatisticsDataProvider.ID);
        if (factory != null) {
            for (IDataProviderDescriptor desc : factory.getDescriptors(trace)) {
                if (desc.getId().contains(configId)) {
                    descriptors.add(desc);
                }
            }
        }
        return descriptors;
    }



}
