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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.Activator;
import org.eclipse.tracecompass.incubator.internal.inandout.core.analysis.SegmentSpecifier;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigParamDescriptor;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSource;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSourceType;
import org.eclipse.tracecompass.tmf.core.config.ITmfExperimentConfigSource;
import org.eclipse.tracecompass.tmf.core.config.TmfConfigParamDescriptor;
import org.eclipse.tracecompass.tmf.core.config.TmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.TmfConfigurationSourceType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfConfigurationException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of {@link ITmfConfigurationSource} for managing InAndOut configuration.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("null")
public class InAndOutConfigurationSource implements ITmfExperimentConfigSource {
    private static final ITmfConfigurationSourceType CONFIG_SOURCE_TYPE;

    public static final String IN_AND_OUT_CONFIG_SOURCE_TYPE_ID = "org.eclipse.tracecompass.incubator.internal.inandout.core.config"; //$NON-NLS-1$
    private static final String NAME_KEY = "name"; //$NON-NLS-1$
    private static final String NAME = "In And Out Analysis"; //$NON-NLS-1$
    private static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$
    public static final String DESCRIPTION = "Configure In And Out analysis using file description";
    private static final String LABEL_KEY = SegmentSpecifier.LABEL_KEY;
    private static final String LABEL_DESCRIPTION = SegmentSpecifier.LABEL_DESCRIPTION;

    private static final String IN_REGEX_KEY = SegmentSpecifier.IN_REGEX_KEY;
    private static final String IN_REGEX_DESCRIPTION = SegmentSpecifier.IN_REGEX_DESCRIPTION;

    private static final String OUT_REGEX_KEY = SegmentSpecifier.OUT_REGEX_KEY;
    private static final String OUT_REGEX_DESCRIPTION = SegmentSpecifier.OUT_REGEX_DESCRIPTION;

    private static final String CONTEXT_IN_REGEX_KEY = SegmentSpecifier.CONTEXT_IN_REGEX_KEY;
    private static final String CONTEXT_IN_REGEX_DESCRIPTION = SegmentSpecifier.CONTEXT_IN_REGEX_DESCRIPTION;

    private static final String CONTEXT_OUT_REGEX_KEY = SegmentSpecifier.CONTEXT_OUT_REGEX_KEY;
    private static final String CONTEXT_OUT_REGEX_DESCRIPTION = SegmentSpecifier.CONTEXT_OUT_REGEX_DESCRIPTION;

    private static final String CLASSIFIER_KEY = SegmentSpecifier.CONTEXT_IN_REGEX_KEY;
    private static final String CLASSIFIER_DESCRIPTION = SegmentSpecifier.CLASSIFIER_DESCRIPTION;

    private Map<String, ITmfConfiguration> fConfigurations = new ConcurrentHashMap<>();
//    private Map<String, List<IDataProviderDescriptor>> fDescriptors = new ConcurrentHashMap<>();
    private final JsonUtils fJsonUtil = new JsonUtils(IN_AND_OUT_CONFIG_SOURCE_TYPE_ID);

    static {
        ImmutableList.Builder<ITmfConfigParamDescriptor> list = new ImmutableList.Builder<>();

        TmfConfigParamDescriptor.Builder descriptorBuilder = new TmfConfigParamDescriptor.Builder()
                .setKeyName(LABEL_KEY)
                .setDescription(LABEL_DESCRIPTION)
                .setIsRequired(true);
        list.add(descriptorBuilder.build());

        descriptorBuilder = new TmfConfigParamDescriptor.Builder()
                .setKeyName(IN_REGEX_KEY)
                .setDescription(IN_REGEX_DESCRIPTION)
                .setIsRequired(true);
        list.add(descriptorBuilder.build());

        descriptorBuilder = new TmfConfigParamDescriptor.Builder()
                .setKeyName(OUT_REGEX_KEY)
                .setDescription(OUT_REGEX_DESCRIPTION)
                .setIsRequired(true);
        list.add(descriptorBuilder.build());

        descriptorBuilder = new TmfConfigParamDescriptor.Builder()
                .setKeyName(CONTEXT_IN_REGEX_KEY)
                .setDescription(CONTEXT_IN_REGEX_DESCRIPTION)
                .setIsRequired(true);
        list.add(descriptorBuilder.build());

        descriptorBuilder = new TmfConfigParamDescriptor.Builder()
                .setKeyName(CONTEXT_OUT_REGEX_KEY)
                .setDescription(CONTEXT_OUT_REGEX_DESCRIPTION)
                .setIsRequired(true);
        list.add(descriptorBuilder.build());

        descriptorBuilder = new TmfConfigParamDescriptor.Builder()
                .setKeyName(CLASSIFIER_KEY)
                .setDescription(CLASSIFIER_DESCRIPTION)
                .setIsRequired(true);
        list.add(descriptorBuilder.build());

        CONFIG_SOURCE_TYPE = new TmfConfigurationSourceType.Builder()
                .setId(IN_AND_OUT_CONFIG_SOURCE_TYPE_ID)
                .setDescription(DESCRIPTION)
                .setName(NAME)
                .setConfigParamDescriptors(list.build()).build();
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
    public ITmfConfiguration create(Map<String, Object> parameters) throws TmfConfigurationException {
        Object labelObj = parameters.get(LABEL_KEY);
        if (!(labelObj instanceof String)) {
            throw new TmfConfigurationException("No label provided or not a string"); //$NON-NLS-1$
        }
        String label = labelObj.toString();

        Object inRegExObj = parameters.get(IN_REGEX_KEY);
        if (!(inRegExObj instanceof String)) {
            throw new TmfConfigurationException("No inRegEx provided or not a string"); //$NON-NLS-1$
        }
        String inRegex = inRegExObj.toString();

        Object outRegExObj = parameters.get(OUT_REGEX_KEY);
        if (!(outRegExObj instanceof String)) {
            throw new TmfConfigurationException("No outRegEx provided or not a string"); //$NON-NLS-1$
        }
        String outRegex = outRegExObj.toString();

        Object contextInRegExObj = parameters.get(CONTEXT_IN_REGEX_KEY);
        if (!(contextInRegExObj instanceof String)) {
            throw new TmfConfigurationException("No contextInRegEx provided or not a string"); //$NON-NLS-1$
        }
        String contextInRegEx = contextInRegExObj.toString();

        Object contextOutRegExObj = parameters.get(CONTEXT_OUT_REGEX_KEY);
        if (!(contextOutRegExObj instanceof String)) {
            throw new TmfConfigurationException("No inRegEx provided or not a string"); //$NON-NLS-1$
        }
        String contextOutRegEx = contextOutRegExObj.toString();

        Object classifierObj = parameters.get(CLASSIFIER_KEY);
        if (!(classifierObj instanceof String)) {
            throw new TmfConfigurationException("No classifier provided or not a string"); //$NON-NLS-1$
        }
        String classifier = classifierObj.toString();

        Object nameObj = parameters.get(NAME_KEY);
        String name = "InAndOut: " + label; //$NON-NLS-1$
        if (nameObj instanceof String) {
            name = nameObj.toString();
        }

        String description = DESCRIPTION +": " + label;
        Object descriptionObj = parameters.get(DESCRIPTION_KEY);
        if (descriptionObj instanceof String) {
            description = descriptionObj.toString();
        }

        Map<String, String> map = new HashMap<>();
        map.put(LABEL_KEY, label);
        map.put(IN_REGEX_KEY, inRegex);
        map.put(OUT_REGEX_KEY, outRegex);
        map.put(CONTEXT_IN_REGEX_KEY, contextInRegEx);
        map.put(CONTEXT_OUT_REGEX_KEY, contextOutRegEx);
        map.put(CLASSIFIER_KEY, classifier);
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

    @Override
    public @Nullable ITmfConfiguration get(String id) {
        return fConfigurations.get(id);
    }

    @Override
    public ITmfConfiguration update(String id, Map<String, Object> parameters) throws TmfConfigurationException {
        ITmfConfiguration config = fConfigurations.get(id);
        if (config == null) {
            throw new TmfConfigurationException("No such configuration with ID: " + id); //$NON-NLS-1$
        }
        throw new TmfConfigurationException("Update configuration not supported!"); //$NON-NLS-1$
    }

    @Override
    public @Nullable ITmfConfiguration remove(String id) {
        ITmfConfiguration config = fConfigurations.remove(id);
        if (config != null) {
            fJsonUtil.deleteConfiguration(config);
        }
        return config;
    }

    @Override
    public List<ITmfConfiguration> getConfigurations() {
        return ImmutableList.copyOf(fConfigurations.values());
    }

    @Override
    public boolean contains(String id) {
        return fConfigurations.containsKey(id);
    }

    @Override
    public void dispose() {
        fConfigurations.clear();
        TmfSignalManager.deregister(this);
    }

    @Override
    public ITmfConfiguration applyConfiguration(String configId, ITmfTrace trace) throws TmfConfigurationException {
        ITmfConfiguration config = fConfigurations.get(configId);
        if (config == null) {
            throw new TmfConfigurationException("No such configuration with ID: " + configId); //$NON-NLS-1$
        }
        appendConfigId(trace, config);
//        InAndOutModuleSource.notifyModuleChange();
        for (ITmfTrace tr : TmfTraceManager.getTraceSetWithExperiment(trace)) {
            tr.refreshAnalysisModules();
        }
        // FIXME: config is applied to all traces in the experiment and should be per trace
//        fDescriptors.put(configId, getDescriptors(trace, configId));
        return config;
    }

    @Override
    public void removeConfiguration(String configId, ITmfTrace trace) throws TmfConfigurationException {
        ITmfConfiguration config = fConfigurations.get(configId);
        if (config == null) {
            throw new TmfConfigurationException("No such configuration with ID: " + configId); //$NON-NLS-1$
        }

        List<IDataProviderDescriptor> list = fDescriptors.remove(configId);
        if (list == null || list.isEmpty()) {
            list = Collections.emptyList();
        }
//        IDataProviderFactory factory = DataProviderManager.getInstance().getFactory(CustomInAndOutDataProviderFactory.PROVIDER_ID);
//        if (factory instanceof CustomInAndOutDataProviderFactory) {
//            ((CustomInAndOutDataProviderFactory) factory).handleConfigurationDeleted(trace, configId);
//        }
        removeConfiguration(trace, config);

        // Reset analysis module source
//        InAndOutModuleSource.notifyModuleChange();
//        for (ITmfTrace tr : TmfTraceManager.getTraceSetWithExperiment(trace)) {
//            tr.refreshAnalysisModules();
//        }
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

//    private static List<IDataProviderDescriptor> getDescriptors(ITmfTrace trace, String configId) {
//        IDataProviderFactory factory = DataProviderManager.getInstance().getFactory(CustomInAndOutDataProviderFactory.PROVIDER_ID);
//        List<IDataProviderDescriptor> descriptors = new ArrayList<>();
//        if (factory != null) {
//            for (IDataProviderDescriptor desc : factory.getDescriptors(trace)) {
//                if (desc.getId().contains(configId)) {
//                    descriptors.add(desc);
//                }
//            }
//        }
//        return descriptors;
//    }
}
