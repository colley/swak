
/**
 * Primary interfaces for working with spectator. To get started, here is a small code sample:
 *
 * <pre>
 * Server s = new Server(new DefaultRegistry());
 *
 * class Server {
 *   private final Registry registry;
 *   private final Id requestCountId;
 *   private final Timer requestLatency;
 *   private final DistributionSummary responseSizes;
 *
 *   public Server(Registry registry) {
 *     this.registry = registry;
 *     requestCountId = registry.createId("server.requestCount");
 *     requestLatency = registry.timer("server.requestLatency");
 *     responseSizes = registry.distributionSummary("server.responseSizes");
 *     registry.gauge("server.numConnections", this, Server::getNumConnections);
 *   }
 *
 *   public Response handle(Request req) {
 *     final long s = System.nanoTime();
 *     try {
 *       Response res = doSomething(req);
 *
 *       final Id cntId = requestCountId
 *         .withTag("country", req.country())
 *         .withTag("status", res.status());
 *       registry.counter(cntId).increment();
 *
 *       responseSizes.record(res.body().size());
 *
 *       return res;
 *     } catch (Exception e) {
 *       final Id cntId = requestCountId
 *         .withTag("country", req.country())
 *         .withTag("status", "exception")
 *         .withTag("error", e.getClass().getSimpleName());
 *       registry.counter(cntId).increment();
 *       throw e;
 *     } finally {
 *       requestLatency.record(System.nanoTime() - s, TimeUnit.NANOSECONDS);
 *     }
 *   }
 *
 *   public int getNumConnections() {
 *     // however we determine the current number of connections on the server
 *   }
 * }
 * </pre>
 *
 * The main classes you will need to understand:
 *
 * <ul>
 *   <li>{@link com.swak.core.spectator.api.Spectator}: static entrypoint to access the registry.</li>
 *   <li>{@link com.swak.core.spectator.api.Registry}: registry class used to create meters.</li>
 *   <li>{@link com.swak.core.spectator.api.Counter}: meter type for measuring a rate of change.</li>
 *   <li>{@link com.swak.core.spectator.api.Timer}: meter type for measuring the time for many short
 *       events.</li>
 *   <li>{@link com.swak.core.spectator.api.LongTaskTimer}: meter type for measuring the time for a
 *       few long events.</li>
 *   <li>{@link com.swak.core.spectator.api.DistributionSummary}: meter type for measuring the sample
 *       distribution of some type of events.</li>
 * </ul>
 */
package com.swak.core.spectator.api;
