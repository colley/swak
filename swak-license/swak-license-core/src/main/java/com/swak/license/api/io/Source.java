/*
 * Copyright © 2017 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swak.license.api.io;

import java.io.InputStream;

/**
 * An abstraction for safe access to some {@linkplain InputStream input stream}.
 *
 * @author Christian Schlichtherle
 */
@FunctionalInterface
public interface Source extends GenSource<InputStream> {

    /**
     * Returns a source which applies the given filter to this source.
     *
     * @param f the filter to apply to this source.
     */
    default Source map(InputFilter f) {
        return f.source(this);
    }
}
