
package com.swak.core.spectator.api;

import java.util.Collection;
import java.util.Iterator;

/** Counter implementation for the composite registry. */
final class CompositeGauge extends CompositeMeter<Gauge> implements Gauge {

  /** Create a new instance. */
  CompositeGauge(Id id, Collection<Gauge> gauges) {
    super(id, gauges);
  }

  @Override public void set(double v) {
    for (Gauge g : meters) {
      g.set(v);
    }
  }

  @Override public double value() {
    Iterator<Gauge> it = meters.iterator();
    return it.hasNext() ? it.next().value() : Double.NaN;
  }
}
