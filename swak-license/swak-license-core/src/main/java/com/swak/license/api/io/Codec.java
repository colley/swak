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


import com.swak.license.api.io.function.XSupplier;

import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * Consumes input and output stream {@linkplain Socket sockets} in order to decode and encode object graphs.
 *
 * @author Christian Schlichtherle
 */
public interface Codec {

    /**
     * Returns an encoder which writes object graphs to the given output stream socket.
     */
    Encoder encoder(Socket<OutputStream> output);

    /**
     * Returns an encoder which writes object graphs to the given sink.
     */
    default Encoder encoder(Sink sink) {
        return encoder(sink.output());
    }

    /**
     * Returns a decoder which reads object graphs from the given input stream socket.
     */
    Decoder decoder(Socket<InputStream> input);

    /**
     * Returns a decoder which reads object graphs from the given source.
     */
    default Decoder decoder(Source source) {
        return decoder(source.input());
    }

    /**
     * Returns a deep clone of the given object by encoding it to a temporary store obtained from the given supplier and
     * decoding it again.
     */
    default <T> T clone(final T t, final XSupplier<Store> storeSupplier) throws Exception {
        final Socket<CompositeClosable<Store>> s = () -> CompositeClosable.of(storeSupplier.get(), Store::deleteIfExists);
        return s.apply(cc -> connect(cc.get()).clone(t));
    }

    /**
     * Connects this codec to the given store.
     */
    default ConnectedCodec connect(Store store) {
        return Internal.connect(this, requireNonNull(store));
    }

    /**
     * Returns a codec which applies the given filter to this codec.
     *
     * @param f the filter to apply to this codec.
     */
    default Codec map(Filter f) {
        return f.codec(this);
    }
}
