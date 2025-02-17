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
package com.swak.license.api.io.spi;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * A filter input stream which ignores any call to its {@link #close()} method.
 *
 * @author Christian Schlichtherle
 */
public final class UncloseableInputStream extends FilterInputStream {

    public UncloseableInputStream(InputStream in) { super(in); }

    @Override
    public void close() { }
}
