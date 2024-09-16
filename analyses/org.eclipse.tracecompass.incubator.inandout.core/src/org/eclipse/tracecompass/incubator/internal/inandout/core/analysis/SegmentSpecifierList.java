/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.inandout.core.analysis;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.inandout.core.Activator;
import org.eclipse.tracecompass.tmf.core.config.ITmfConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Model of Segment specifiers
 */
@NonNullByDefault
public class SegmentSpecifierList {

    private @Nullable List<SegmentSpecifier> fSpecifiers;
    private ITmfConfiguration fConfig;

    SegmentSpecifierList(ITmfConfiguration config) {
        fConfig = config;
        fSpecifiers = read(config);
    }

    public @Nullable List<SegmentSpecifier> getSpecifiers() {
        return fSpecifiers;
    }

    public ITmfConfiguration getConfiguration() {
        return fConfig;
    }

    public static List<SegmentSpecifier> read(ITmfConfiguration config) {
        Type listType = new TypeToken<ArrayList<SegmentSpecifier>>() {
        }.getType();
        List<SegmentSpecifier> specifiers = Collections.emptyList();

        Object json = config.getParameters().get("json");
        if (!(json instanceof String) ) {
            return specifiers;
        }
        try {
            List<SegmentSpecifier> list = new Gson().fromJson((String) json, listType);
            if (list != null) {
                specifiers = list;
            }
        } catch (JsonSyntaxException e) {
            Activator.getInstance().logError(e.getMessage(), e);
        }
        return specifiers;
    }
}
