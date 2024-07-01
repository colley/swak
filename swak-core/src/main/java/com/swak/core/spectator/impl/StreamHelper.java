
package com.swak.core.spectator.impl;

import java.io.ByteArrayOutputStream;

/**
 * Caches ByteArrayOutputStream objects in a thread local to allow for reuse. This can be
 * useful to reduce the allocations for growing the buffer. It will hold onto the streams
 * so it should only be used for use-cases where the data written to the stream is bounded.
 *
 * <p><b>This class is an internal implementation detail only intended for use within spectator.
 * It is subject to change without notice.</b></p>
 */
public final class StreamHelper {

  private final ThreadLocal<ByteArrayOutputStream> streams;

  public StreamHelper() {
    streams = new ThreadLocal<>();
  }

  public ByteArrayOutputStream getOrCreateStream() {
    ByteArrayOutputStream baos = streams.get();
    if (baos == null) {
      baos = new ByteArrayOutputStream();
      streams.set(baos);
    } else {
      baos.reset();
    }
    return baos;
  }
}
