package org.eclipse.tracecompass.incubator.internal.inandout.core.analysis;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.Activator;
import org.eclipse.tracecompass.incubator.internal.inandout.core.config.InAndOutConfigurationSource;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfigurationSource;
import org.eclipse.tracecompass.tmf.core.config.TmfConfigurationSourceManager;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.project.model.ITmfPropertiesProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;

public class InAndOutAnalysisHelper implements IAnalysisModuleHelper, ITmfPropertiesProvider {

    private static final String ICON_ANALYSIS = "/icons/inandout.png"; //$NON-NLS-1$

    private final SegmentSpecifierConfiguration fSpecifiers;
    private final String fId;

    /**
     * Constructor
     *
     * @param config
     *
     * @param name
     *            TODO
     * @param type
     *            TODO
     */
    public InAndOutAnalysisHelper(ITmfConfiguration config) {
        fId =  InAndOutAnalysisModule.ID + config.getId();
        fSpecifiers = new SegmentSpecifierConfiguration(config);
    }

    @Override
    public String getId() {
        return fId;
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public boolean appliesToExperiment() {
        return false;
    }

    @Override
    public String getHelpText(ITmfTrace trace) {
        return getHelpText();
    }

    @Override
    public String getIcon() {
        return ICON_ANALYSIS;
    }

    @Override
    public Bundle getBundle() {
        return Activator.getInstance().getBundle();
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceClass) {
        return true;
    }

    @Override
    public Iterable<Class<? extends ITmfTrace>> getValidTraceTypes() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        return Collections.emptySet();
    }

    /**
     * Sets the trace to the analysis module
     *
     * @param analysisModule
     *            the analysis module
     * @param trace
     *            the trace to set
     * @return returns the updated analysis module or null trace setting is not successful
     * @throws TmfAnalysisException if an exception happens when setting the trace
     */
    protected IAnalysisModule setTrace(@NonNull IAnalysisModule analysisModule, @NonNull ITmfTrace trace) throws TmfAnalysisException {
        IAnalysisModule module = analysisModule;
        if (module.setTrace(trace)) {
            TmfAnalysisManager.analysisModuleCreated(module);
        } else {
            /*
             * The analysis does not apply to the trace, dispose of the
             * module
             */
            module.dispose();
            module = null;
        }
        return module;
    }

    // ------------------------------------------------------------------------
    // ITmfPropertiesProvider
    // ------------------------------------------------------------------------
    @Override
    public @NonNull Map<@NonNull String, @NonNull String> getProperties() {
//        SegmentSpecifierList specifiers = fSpecifiers;
//        if (specifiers == null) {
            return Collections.emptyMap();
//        }
//        return ImmutableMap.copyOf(specifiers.getConfiguration().getParameters());
    }

    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder("Custom InAndOut Analysis Module");
        SegmentSpecifierConfiguration specifiers = fSpecifiers;
        if (specifiers != null) {
            builder.append('(')
                .append(specifiers.getConfiguration().getName())
                .append(')');

        }
        return builder.toString();
    }

    @Override
    public String getHelpText() {
        StringBuilder builder = new StringBuilder("Custom InAndOut Analysis Module");
        SegmentSpecifierConfiguration specifiers = fSpecifiers;
        if (specifiers != null) {
            builder.append("for configuration: ")
                .append(specifiers.getConfiguration().getName());
        }
        return builder.toString();
    }

    @Override
    public final @Nullable IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {
        SegmentSpecifierConfiguration specifiers = fSpecifiers;
        if (specifiers == null) {
            return null;
        }
        ITmfConfigurationSource configSource = TmfConfigurationSourceManager.getInstance().getConfigurationSource(InAndOutConfigurationSource.IN_AND_OUT_CONFIG_SOURCE_TYPE_ID);
        if (!(configSource instanceof InAndOutConfigurationSource)) {
            return null;
        }

        if (!((InAndOutConfigurationSource) configSource).appliesToTrace(trace, specifiers.getConfiguration().getId())) {
            return null;
        }

        IAnalysisModule module = new InAndOutAnalysisModule(specifiers);
        module.setName(getName());
        return setTrace(module, trace);
    }
}
