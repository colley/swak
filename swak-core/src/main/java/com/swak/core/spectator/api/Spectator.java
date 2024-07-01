
package com.swak.core.spectator.api;

/**
 * Static factory used to access the main global registry.
 */
public final class Spectator {

  private static final CompositeRegistry COMPOSITE_REGISTRY = new CompositeRegistry(Clock.SYSTEM);
  /**
   * Returns the global composite registry. This method can be used for use-cases where it is
   * necessary to get a static reference to a registry. It will not do anything unless other
   * registries are added. Example:
   *
   * <pre>
   * class Main {
   *   public static void main(String[] args) {
   *     // This is the preferred usage and works well with DI libraries like guice. Setup a
   *     // registry and pass it in as needed.
   *     Registry registry = new DefaultRegistry();
   *     (new Example1(registry)).start();
   *
   *     // If it is desirable to get data from things using the global registry, then the
   *     // registry for the application can be added to the global context.
   *     Spectator.globalRegistry().add(registry);
   *     Example2 ex2 = new Example2();
   *     ex2.start();
   *
   *     // If the lifecycle is not the same as the jvm, then the registry should be removed
   *     // when shutting down.
   *     ex2.onShutdown(() -> Spectator.globalRegistry().remove(registry));
   *   }
   * }
   *
   * class Example1 {
   *   private final Counter c;
   *
   *   Example1(Registry registry) {
   *     c = registry.counter("example1");
   *   }
   *   ...
   * }
   *
   * class Example2 {
   *   private final Counter c;
   *
   *   Example2() {
   *     c = Spectator.globalRegistry().counter("example1");
   *   }
   *   ...
   * }
   * </pre>
   */
  public static CompositeRegistry globalRegistry() {
    return COMPOSITE_REGISTRY;
  }

  private Spectator() {
  }
}
