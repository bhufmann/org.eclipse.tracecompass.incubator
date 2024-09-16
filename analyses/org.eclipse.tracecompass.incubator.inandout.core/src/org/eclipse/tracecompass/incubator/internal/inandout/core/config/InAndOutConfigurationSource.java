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
import java.util.HashMap;
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
import org.eclipse.tracecompass.incubator.internal.inandout.core.analysis.InAndOutAnalysisModuleSource;
import org.eclipse.tracecompass.incubator.internal.inandout.core.analysis.SegmentSpecifierList;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSource;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSourceType;
import org.eclipse.tracecompass.tmf.core.config.ITmfDataProviderConfigSource;
import org.eclipse.tracecompass.tmf.core.config.ITmfDataProviderSource;
import org.eclipse.tracecompass.tmf.core.config.TmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.TmfConfigurationSourceType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
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
import com.google.common.collect.ImmutableList;
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
    private static final String NAME_KEY = "name"; //$NON-NLS-1$
    private static final String NAME = "In And Out Analysis"; //$NON-NLS-1$
    private static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$
    public static final String DESCRIPTION = "Configure In And Out analysis using file description";

    private static final Table<String, ITmfTrace, ITmfConfiguration> fTmfConfigurationTable = HashBasedTable.create();

//    private Map<String, ITmfConfiguration> fConfigurations = new ConcurrentHashMap<>();
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
        refresh();
        TmfSignalManager.register(this);
    }

    /**
     * Refresh the configuration source by reading the global storage
     */
    public void refresh() {
        for (@NonNull ITmfConfiguration config : fJsonUtil.readConfigurations()) {
            fConfigurations.put(config.getId(), config);
        }
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

        Object labelObj = parameters.get(NAME);
        if (!(labelObj instanceof String)) {
            throw new TmfConfigurationException("No name json input provided or not a string"); //$NON-NLS-1$
        }
        String label = labelObj.toString();

        Object nameObj = parameters.get(NAME_KEY);
        String name = "InAndOut"; //$NON-NLS-1$
        if (nameObj instanceof String) {
            name = nameObj.toString();
        }

        String description = DESCRIPTION;
        Object descriptionObj = parameters.get(DESCRIPTION_KEY);
        if (descriptionObj instanceof String) {
            description = descriptionObj.toString();
        }

        Map<String, String> map = new HashMap<>();
        map.put(LABEL_KEY, label);
        TmfConfiguration.Builder builder = new TmfConfiguration.Builder()
                .setName(name)
                .setDescription(description.toString())
                .setSourceTypeId(IN_AND_OUT_CONFIG_SOURCE_TYPE_ID)
                .setParameters(map);
        ITmfConfiguration config = builder.build();

        if (fConfigurations.containsKey(config.getId())) {
            throw new TmfConfigurationException("Configuration already existis with label: " + label); //$NON-NLS-1$
        }
        fJsonUtil.writeConfiguration(config);
        fConfigurations.put(config.getId(), config);
        return config;
    }

//    @Override
//    public @Nullable ITmfConfiguration get(String id) {
//        return fConfigurations.get(id);
//    }

//    @Override
//    public ITmfConfiguration update(String id, Map<String, Object> parameters) throws TmfConfigurationException {
//        ITmfConfiguration config = fConfigurations.get(id);
//        if (config == null) {
//            throw new TmfConfigurationException("No such configuration with ID: " + id); //$NON-NLS-1$
//        }
//        throw new TmfConfigurationException("Update configuration not supported!"); //$NON-NLS-1$
//    }

//    @Override
//    public @Nullable ITmfConfiguration remove(String id) {
//        ITmfConfiguration config = fConfigurations.remove(id);
//        if (config != null) {
//            fJsonUtil.deleteConfiguration(config);
//        }
//        return config;
//    }
//
//    @Override
//    public List<ITmfConfiguration> getConfigurations() {
//        return ImmutableList.copyOf(fConfigurations.values());
//    }
//
//    @Override
//    public boolean contains(String id) {
//        return fConfigurations.containsKey(id);
//    }

    @Override
    public void dispose() {
        fConfigurations.clear();
        TmfSignalManager.deregister(this);
    }

//    @Override
//    public ITmfConfiguration create(Map<String, Object> parameters, ITmfTrace trace, String srcDataProviderId) throws TmfConfigurationException {
//
//
//        ITmfConfiguration config = fConfigurations.get(configId);
//        if (config == null) {
//            throw new TmfConfigurationException("No such configuration with ID: " + configId); //$NON-NLS-1$
//        }
//        appendConfigId(trace, config);
//        InAndOutAnalysisModuleSource.notifyModuleChange();
//        for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
//            tr.refreshAnalysisModules();
//        }
//        // FIXME: config is applied to all traces in the experiment and should be per trace
//        return config;
//    }

    @Override
    @Nullable ITmfConfiguration remove(String id, ITmfTrace trace, String srcDataProviderId) {
        ITmfConfiguration config = fConfigurations.get(configId);
        if (config == null) {
            throw new TmfConfigurationException("No such configuration with ID: " + configId); //$NON-NLS-1$
        }

        Iterator<InAndOutAnalysisModule> csModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, InAndOutAnalysisModule.class).iterator();
        while (csModules.hasNext()) {
            InAndOutAnalysisModule csModule = csModules.next();
            SegmentSpecifierList specifiers = csModule.getSeSpecifierList();

            if (specifiers != null && specifiers.getConfiguration().getId().equals(configId)) {
//                ITmfTreeDataProvider<?> dp = DataProviderManager.getInstance().getExistingDataProvider(trace, generateDpId(csModule.getId()), ITmfTreeDataProvider.class);
//                if (dp != null) {
//                    DataProviderManager.getInstance().removeDataProvider(trace, dp);
//                    dp.dispose();
//                }
                csModule.clearPersistentData();
            }
        }

        removeConfiguration(trace, config);
        // Reset analysis module source
        InAndOutAnalysisModuleSource.notifyModuleChange();
        for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
            tr.refreshAnalysisModules();
        }
    }

    /**
     * Checks if given config is applied to a given trace
     *
     * @param trace
     *            The trace to check
     * @param configId
     *            The config id to check
     * @return true if it applies else false
     */
    public boolean appliesToTrace(ITmfTrace trace, String configId) {
        return fJsonUtil.getTraceConfigIds(trace).contains(configId);
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
                ITmfConfiguration config = fConfigurations.get(configId);
                if (config != null) {
                    applyConfiguration(configId, trace);
                }
            }
        } catch (TmfConfigurationException e) {
            Activator.getInstance().logError("Error applying configurations for trace " + trace.getName(), e); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------
    private void appendConfigId(ITmfTrace trace, ITmfConfiguration config) throws TmfConfigurationException {
        // FIXME: config is applied to all traces in the experiment and should be per trace
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
                appendConfigId(tr, config);
            }
        }
        fJsonUtil.copyToTrace(config, trace);
    }

    private void removeConfiguration(ITmfTrace trace, ITmfConfiguration config) throws TmfConfigurationException {
        // FIXME: config is applied to all traces in the experiment and should be per trace
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
                removeConfiguration(tr, config);
            }
        }
        fJsonUtil.deleteFromTrace(config, trace);
    }

    private Set<String> getConfigIds(ITmfTrace trace) throws TmfConfigurationException {
        Set<String> ret = new HashSet<>();
        // FIXME: config is applied to all traces in the experiment and should be per trace
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace tr : TmfTraceManager.getTraceSet(trace)) {
                ret.addAll(getConfigIds(tr));
            }
            return ret;
        }
        return fJsonUtil.getTraceConfigIds(trace);
    }

    @Override
    public @NonNull List<@NonNull IDataProviderDescriptor> getDataProviderDescriptors(@NonNull String srcDataProviderId, @NonNull ITmfTrace trace, @NonNull String configId) throws TmfConfigurationException {
        // TODO Auto-generated method stub
        return null;
    }
}
