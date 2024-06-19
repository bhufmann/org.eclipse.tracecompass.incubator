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

package org.eclipse.tracecompass.incubator.internal.inandout.core.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.config.InAndOutConfigurationSource;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSource;
import org.eclipse.tracecompass.tmf.core.config.TmfConfigurationSourceManager;

/**
 * Analysis module source to create InAndOutAnalysis module instances
 * @author Bernd Hufmann
 */
public class InAndOutAnalysisModuleSource implements IAnalysisModuleSource {

    private static @Nullable List<IAnalysisModuleHelper> fModules = null;

    /**
     * Constructor. It adds the new module listener to the analysis manager.
     */
    public InAndOutAnalysisModuleSource() {
        // Do nothing
    }

    @Override
    public synchronized Iterable<IAnalysisModuleHelper> getAnalysisModules() {
        List<IAnalysisModuleHelper> modules = fModules;
        if (modules == null) {
            modules = populateAnalysisModules();
            fModules = modules;
        }
        return modules;
    }

    private static List<IAnalysisModuleHelper> populateAnalysisModules() {
        List<IAnalysisModuleHelper> modules = new ArrayList<>();
        ITmfConfigurationSource configSource = TmfConfigurationSourceManager.getInstance().getConfigurationSource(InAndOutConfigurationSource.IN_AND_OUT_CONFIG_SOURCE_TYPE_ID);
        if (configSource != null) {
            List<ITmfConfiguration> configurations = configSource.getConfigurations();
            for (ITmfConfiguration config : configurations) {
                SegmentSpecifier inAndOutConfig = new SegmentSpecifier(config);
                IAnalysisModuleHelper helper = new InAndOutAnalysisHelper(inAndOutConfig);
                modules.add(helper);
            }
        }
        return modules;
    }

    /**
     * Notifies the main XML analysis module that the executable modules list
     * may have changed and needs to be refreshed.
     */
    public static void notifyModuleChange() {
        fModules = null;
        TmfAnalysisManager.refreshModules();
    }


}
