/*******************************************************************************
 * Copyright (c) 2024 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.incubator.internal.inandout.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.Activator;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.TmfConfiguration;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfConfigurationException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Class containing some utilities for the json configurations files
 * TODO should be in Trace Compass open source
 */
public final class JsonUtils {

    /** File extension */
    private static final String JSON_EXTENSION = "json"; //$NON-NLS-1$

    private final String fSubFolderName;

    /**
     * Constructor
     *
     * @param rootDirectoryName
     *            the name of folder under global / trace storage for
     *            configuration files
     */
    public JsonUtils(String rootDirectoryName) {
        fSubFolderName = rootDirectoryName;
    }

    /**
     * Validate the config file input as JSON
     *
     * @param file
     *            file to validate
     * @return True if the config file validates as JSON
     */
    public static IStatus validate(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            new Gson().fromJson(bufferedReader, JsonObject.class);

        } catch (FileNotFoundException e) {
            String error = "File not found"; //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (IOException e) {
            String error = "IO Exception"; //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (JsonSyntaxException e) {
            String error = "Json Syntax Errror"; //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (JsonIOException e) {
            String error = "JSON IO Exception"; //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);

        }
        return Status.OK_STATUS;
    }

    /**
     * Delete configuration from trace supplementary folder
     *
     * @param config
     *            The configuration to delete
     * @param trace
     *            The trace
     * @throws TmfConfigurationException
     *             if an error occurs
     */
    public void deleteFromTrace(ITmfConfiguration config, ITmfTrace trace) throws TmfConfigurationException {
        IPath path = getTraceRootFolder(trace);
        File dir = path.toFile();
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new TmfConfigurationException("Error creating supplementary folder: " + dir.getName()); //$NON-NLS-1$
            }
        }
        if (!dir.isDirectory()) {
            throw new TmfConfigurationException("Supplementary folder is not a folder: " + dir.getName()); //$NON-NLS-1$
        }
        File toFile = path.addTrailingSeparator().append(config.getId()).addFileExtension(JSON_EXTENSION).toFile();
        deleteFile(toFile);
    }

    /**
     * Read all configurations from a single trace
     *
     * @param trace
     *            The trace the configuration should be read
     * @return a list of configurations
     */
    public List<ITmfConfiguration> readConfigurations(ITmfTrace trace) {
        IPath supplPath = getTraceRootFolder(trace);
        File folder = supplPath.toFile();
        return readConfigurations(folder);
    }

    /**
     * Get all the configIds applied for a trace
     *
     * @param trace
     *            The trace
     * @return a set of configuration ids
     */
    public synchronized Set<String> getTraceConfigIds(ITmfTrace trace) {
        IPath supplPath = getTraceRootFolder(trace);
        File folder = supplPath.toFile();
        Set<String> configIds = new HashSet<>();
        if ((folder.isDirectory() && folder.exists())) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    IPath path = new Path(file.getName());
                    if (path.getFileExtension().equals(JSON_EXTENSION)) {
                        configIds.add(path.removeFileExtension().lastSegment());
                    }
                }
            } else {
                Activator.getInstance().logError("IO error" + " " + folder.getPath()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return configIds;
    }

    /**
     * Write a single configuration to global storage
     *
     * @param config
     *            The configuration to write
     * @throws TmfConfigurationException
     *             if an error occurred
     */
    public void writeConfiguration(ITmfTrace trace, ITmfConfiguration config, String srcDataProviderId) throws TmfConfigurationException {
        IPath pathToFiles = getTraceRootFolder(trace, srcDataProviderId);
        File file = pathToFiles.addTrailingSeparator().append(config.getId()).addFileExtension(JSON_EXTENSION).toFile();
        try (Writer writer = new FileWriter(file)) {
            writer.append(new Gson().toJson(config));
        } catch (IOException e) {
            throw new TmfConfigurationException("Error writing configuration.", e); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("null")
    private IPath getTraceRootFolder(ITmfTrace trace, String srcDataProviderId) {
        String supplFolder = TmfTraceManager.getSupplementaryFileDir(trace);
        IPath supplPath = new Path(supplFolder);
        supplPath = supplPath.addTrailingSeparator().append(fSubFolderName);
        return supplPath;
    }

    private static List<ITmfConfiguration> readConfigurations(File folder) {
        List<ITmfConfiguration> files = new ArrayList<>();
        if ((folder.isDirectory() && folder.exists())) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    IPath path = new Path(file.getName());
                    if (path.getFileExtension().equals(JSON_EXTENSION)) {
                        try {
                            ITmfConfiguration config = readConfiguration(file);
                            if (config != null) {
                                files.add(config);
                            }
                        } catch (TmfConfigurationException e) {
                            Activator.getInstance().logError("Error reading configurations " + " " + folder.getPath() + ", " + e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                    }
                }
            } else {
                Activator.getInstance().logError("IO error " + folder.getPath()); //$NON-NLS-1$
            }
        }
        return files;
    }

    private static @Nullable ITmfConfiguration readConfiguration(File file) throws TmfConfigurationException {
        ITmfConfiguration config = null;
        Type listType = new TypeToken<TmfConfiguration>() {
        }.getType();
        try (Reader reader = new FileReader(file)) {
            config = new Gson().fromJson(reader, listType);
        } catch (IOException e) {
            throw new TmfConfigurationException("Error writing configuration.", e); //$NON-NLS-1$
        }
        return config;
    }

    private static void deleteFile(File file) {
        if (file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                Activator.getInstance().logError("Error deleting In-And-Out File " + file.getAbsolutePath(), e); //$NON-NLS-1$
            }
        }
    }
}
