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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.Activator;
import org.eclipse.tracecompass.incubator.internal.inandout.core.config.InAndOutConfigurationSource;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;
import org.eclipse.tracecompass.tmf.core.config.TmfConfiguration;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfConfigurationException;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model of Segment specifiers
 */
@NonNullByDefault
public class SegmentSpecifierConfiguration implements ITmfConfiguration {
    @Expose
    @SerializedName(value = "name")
    private String fName;
    @Expose
    @SerializedName(value = "description")
    private @Nullable String fDescription;
    @Expose
    @SerializedName(value = "specifiers")
    private @Nullable List<SegmentSpecifier> fSpecifiers;
    transient private @Nullable String fId;
    transient private Map<String, Object> fParameters;

    SegmentSpecifierConfiguration() {
        fName = ""; //$NON-NLS-1$
        fDescription = ""; //$NON-NLS-1$
        fSpecifiers = Collections.emptyList();
        fId = null;
        fParameters = Collections.emptyMap();
    }

    public @Nullable List<SegmentSpecifier> getSpecifiers() {
        return fSpecifiers;
    }

    public static SegmentSpecifierConfiguration fromJsonString(String json) throws TmfConfigurationException {
        try {
            return new Gson().fromJson(json, SegmentSpecifierConfiguration.class);
        } catch (JsonSyntaxException e) {
            Activator.getInstance().logError(e.getMessage(), e);
            throw new TmfConfigurationException("Can't parse json. ", e);
        }
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public String getId() {
        String id = fId;
        if (id == null) {
            id = toUuidString();
        }
        return id;
    }

    @Override
    public String getDescription() {
        String description = fDescription;
        if (description == null) {
            description = "TODO";
            fDescription = description;
        }
        return description;
    }

    @Override
    public String getSourceTypeId() {
        return InAndOutConfigurationSource.IN_AND_OUT_CONFIG_SOURCE_TYPE_ID;
    }

    @Override
    public Map<String, Object> getParameters() {
        return fParameters;
    }

    void setParameter(String json) {
        fParameters = ImmutableMap.of(TmfConfiguration.JSON_STRING_KEY, json);
    }

    private String toUuidString() {
        StringBuilder paramBuilder = new StringBuilder();
        for (Entry<String, Object> entry : fParameters.entrySet()) {
            paramBuilder.append(entry.getKey())
            .append("=") //$NON-NLS-1$
            .append(entry.getValue());
        }
        String inputStr = new StringBuilder()
                .append("fName=").append(fName) //$NON-NLS-1$
                .append("fParameters=").append(paramBuilder.toString()).toString(); //$NON-NLS-1$
        return UUID.nameUUIDFromBytes(Objects.requireNonNull(inputStr.getBytes(Charset.defaultCharset()))).toString();
    }
}
