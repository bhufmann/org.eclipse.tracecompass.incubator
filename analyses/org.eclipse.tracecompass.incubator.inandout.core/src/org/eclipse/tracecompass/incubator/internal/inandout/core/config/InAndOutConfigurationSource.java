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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.Activator;
import org.eclipse.tracecompass.incubator.internal.inandout.core.analysis.InAndOutAnalysisModule;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
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
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of {@link ITmfConfigurationSource} for managing InAndOut configuration.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("null")
public class InAndOutConfigurationSource extends TmfComponent implements ITmfExperimentConfigSource {
    private static final ITmfConfigurationSourceType CONFIG_SOURCE_TYPE;

    private static final String IN_AND_OUT_DIRECTORY = "inandout"; //$NON-NLS-1$

    private static final String IN_AND_OUT_CONFIG_SOURCE_TYPE_ID = "org.eclipse.tracecompass.incubator.internal.inandout.core.config"; //$NON-NLS-1$
    private static final String NAME = "In And Out Analysis"; //$NON-NLS-1$
    private static final String DESCRIPTION = "Configure In And Out analysis using file description"; //$NON-NLS-1$
    private static final String PATH_KEY = "path"; //$NON-NLS-1$
    private static final String PATH_DESCRIPTION = "Path to InAndOut analysis file"; //$NON-NLS-1$

    private Map<String, ITmfConfiguration> fConfigurations = new ConcurrentHashMap<>();

    static {
        ImmutableList.Builder<ITmfConfigParamDescriptor> list = new ImmutableList.Builder<>();

        TmfConfigParamDescriptor.Builder descriptorBuilder = new TmfConfigParamDescriptor.Builder()
                .setKeyName(PATH_KEY)
                .setDescription(PATH_DESCRIPTION)
                .setIsRequired(false);
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
        super("InAndOutConfigurationSource"); //$NON-NLS-1$
        for (Entry<@NonNull String, @NonNull File> entry : listFiles().entrySet()) {
            ITmfConfiguration config = createConfiguration(entry.getValue());
            fConfigurations.put(config.getId(), config);
        }
    }

    @Override
    public ITmfConfigurationSourceType getConfigurationSourceType() {
        return CONFIG_SOURCE_TYPE;
    }

    @Override
    public ITmfConfiguration create(Map<String, Object> parameters) throws TmfConfigurationException {
        return createOrUpdateJson(parameters);
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
        return createOrUpdateJson(parameters);
    }

    @Override
    public @Nullable ITmfConfiguration remove(String id) {
        ITmfConfiguration config = fConfigurations.remove(id);
        if (config != null) {
            deleteFile(getInAndOutFile(config.getId()));
            return config;
        }
        return null;
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
    }

    @Override
    public ITmfConfiguration applyConfiguration(String configId, ITmfTrace trace) throws TmfConfigurationException {
        ITmfConfiguration config = fConfigurations.get(configId);
        if (config == null) {
            throw new TmfConfigurationException("No such configuration with ID: " + configId); //$NON-NLS-1$
        }

        // TODO retrieve data provider descriptors
        List<IDataProviderDescriptor> descriptors = new ArrayList<>();
        File toFile = getTraceInAndOutFile(trace);

        File fromFile = getInAndOutFile(config.getId());
        if (!fromFile.exists()) {
            throw new TmfConfigurationException("InAndOut configuration doesn't exist"); //$NON-NLS-1$
        }
        IStatus status = copyFile(fromFile, toFile);
        String statusMessage = status.getMessage();
        String message = statusMessage != null? statusMessage : "Failed to update json analysis configuration"; //$NON-NLS-1$
        if (status.getException() != null) {
            throw new TmfConfigurationException(message, status.getException());
        }
        TmfConfiguration.Builder builder = new TmfConfiguration.Builder();
            builder.setId(config.getId())
                .setName(config.getName())
                .setDescription(config.getDescription())
                .setSourceTypeId(config.getSourceTypeId())
                .setParameters(config.getParameters())
                .setDataProviderDescriptors(descriptors);
        config = builder.build();
        return config;
    }

    @Override
    public void removeConfiguration(String configId, ITmfTrace trace) throws TmfConfigurationException {
        ITmfConfiguration config = fConfigurations.get(configId);
        if (config == null) {
            throw new TmfConfigurationException("No such configuration with ID: " + configId); //$NON-NLS-1$
        }

        File toFile = getTraceInAndOutFile(trace);

        if (!toFile.exists()) {
            throw new TmfConfigurationException("InAndOut not configured for this trace"); //$NON-NLS-1$
        }

        deleteFile(toFile);

        // TODO: There must be a better way
        for (ITmfTrace tr: TmfTraceManager.getTraceSetWithExperiment(trace)) {
            TmfStateSystemAnalysisModule.getStateSystem(tr, InAndOutAnalysisModule.ID);
        }
        Iterable<InAndOutAnalysisModule> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, InAndOutAnalysisModule.class);
        for (InAndOutAnalysisModule module : modules) {
            if (module.getTrace() == trace) {
                module.clearPersistentData();
            }
        }
    }

    private ITmfConfiguration createOrUpdateJson(Map<String, Object> parameters) throws TmfConfigurationException {
        File fromFile = getFile(parameters);
        if (fromFile == null) {
            throw new TmfConfigurationException("Missing path"); //$NON-NLS-1$
        }
        File toFile = getInAndOutFile(fromFile.getName());
        ITmfConfiguration config = createConfiguration(fromFile);
        IStatus status = copyFile(fromFile, toFile);
        String statusMessage = status.getMessage();
        String message = statusMessage != null? statusMessage : "Failed to update json analysis configuration"; //$NON-NLS-1$
        if (status.getException() != null) {
            throw new TmfConfigurationException(message, status.getException());
        }
        fConfigurations.put(config.getId(), config);
        return config;
    }

    private static @Nullable File getFile(Map<String, Object> parameters) {
        String path = (String) parameters.get("path"); //$NON-NLS-1$
        if (path == null) {
            return null;
        }
        return new File(path);
    }

    private static String getName(String file) {
        return new Path(file).removeFileExtension().toString();
    }

    private static ITmfConfiguration createConfiguration(File file) {
        String id = file.getName();
        String name = getName(id);
        String description = "Configuration of In And Out analysis: "  + name; //$NON-NLS-1$
        TmfConfiguration.Builder builder = new TmfConfiguration.Builder()
                .setName(name)
                .setId(id)
                .setDescription(description.toString())
                .setSourceTypeId(IN_AND_OUT_CONFIG_SOURCE_TYPE_ID);
       return builder.build();
    }

    private static File getInAndOutFile(String fileName) {
        IPath path = Activator.getInstance().getStateLocation();
        path = path.addTrailingSeparator().append(IN_AND_OUT_DIRECTORY);
        File file = path.addTrailingSeparator().append(fileName).toFile();
        return file;
    }

    private static File getTraceInAndOutFile(ITmfTrace trace) {
        String folder = TmfTraceManager.getSupplementaryFileDir(trace);
        IPath path = new Path(folder);
        String fileName = InAndOutAnalysisModule.ID.concat(InAndOutAnalysisModule.JSON);
        File file = path.addTrailingSeparator().append(fileName).toFile();
        return file;
    }

    public static synchronized @NonNull Map<@NonNull String, @NonNull File> listFiles() {
        IPath pathToFolder = Activator.getInstance().getStateLocation();
        pathToFolder = pathToFolder.addTrailingSeparator().append(IN_AND_OUT_DIRECTORY);

        File folder = pathToFolder.toFile();

        if (!folder.exists()) {
            folder.mkdir();
        }
        Map<@NonNull String, @NonNull File> fileMap = new HashMap<>();
        if ((folder.isDirectory() && folder.exists())) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    IPath path = new Path(file.getName());
                    if (path.getFileExtension().equals("json")) {
                        fileMap.put(file.getName(), file);
                    }
                }
            } else {
                Activator.getInstance().logError("Error reading in-and-out file " + folder.getPath()); //$NON-NLS-1$
            }
        }
        return Collections.unmodifiableMap(fileMap);
    }

    private static IStatus copyFile(File fromFile, File toFile) {
        try {
            Files.copy(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String error = "Error copying file";  //$NON-NLS-1$
            Activator.getInstance().logError(error, e);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }
        return Status.OK_STATUS;
    }

    private static void deleteFile(File file) {
        if (file != null && file.exists()) {
            // TODO: Do we need to create a backup for global config?
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                Activator.getInstance().logError("Error deleting In-And-Out File " + file.getAbsolutePath(), e); //$NON-NLS-1$
            }
        }
    }
}
