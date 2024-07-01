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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A filter output stream which just {@linkplain #flush() flushes} the underlying output stream on any call to its
 * {@link #close()} method.
 *
 * @author Christian Schlichtherle
 */
public final class UncloseableOutputStream extends FilterOutputStream {

    public UncloseableOutputStream(OutputStream out) { super(out); }

    @Override
    public void close() throws IOException { out.flush(); }
}
