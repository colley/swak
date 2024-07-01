
package com.swak.core.spectator.metrics;

import com.swak.core.spectator.api.*;
import com.swak.core.spectator.api.histogram.PercentileTimer;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Monitors {

    private static final Registry registry = Spectator.globalRegistry();

    public static final String NO_DOMAIN = "NO_DOMAIN";

    private static final Map<String, Map<Map<String, String>, Counter>> counters =
            new ConcurrentHashMap<>();

    private static final Map<String, Map<Map<String, String>, PercentileTimer>> timers =
            new ConcurrentHashMap<>();

    private static final Map<String, Map<Map<String, String>, Gauge>> gauges =
            new ConcurrentHashMap<>();

    private static final Map<String, Map<Map<String, String>, DistributionSummary>>
            distributionSummaries = new ConcurrentHashMap<>();

    public static final String classQualifier = "WorkflowMonitor";

    private Monitors() {}

    /**
     * Increment a counter that is used to measure the rate at which some event is occurring.
     * Consider a simple queue, counters would be used to measure things like the rate at which
     * items are being inserted and removed.
     *
     * @param className
     * @param name
     * @param additionalTags
     */
    private static void counter(String className, String name, String... additionalTags) {
        getCounter(className, name, additionalTags).increment();
    }

    /**
     * Set a gauge is a handle to get the current value. Typical examples for gauges would be the
     * size of a queue or number of threads in the running state. Since gauges are sampled, there is
     * no information about what might have occurred between samples.
     *
     * @param name
     * @param measurement
     * @param additionalTags
     */
    private static void gauge(
            String className, String name, long measurement, String... additionalTags) {
        getGauge(className, name, additionalTags).set(measurement);
    }

    /**
     * Records a value for an event as a distribution summary. Unlike a gauge, this is sampled
     * multiple times during a minute or everytime a new value is recorded.
     *
     * @param className
     * @param name
     * @param additionalTags
     */
    private static void distributionSummary(
            String className, String name, long value, String... additionalTags) {
        getDistributionSummary(className, name, additionalTags).record(value);
    }

    private static Timer getTimer(String className, String name, String... additionalTags) {
        Map<String, String> tags = toMap(className, additionalTags);
        return timers.computeIfAbsent(name, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(
                        tags,
                        t -> {
                            Id id = registry.createId(name, tags);
                            return PercentileTimer.get(registry, id);
                        });
    }

    private static Counter getCounter(String className, String name, String... additionalTags) {
        Map<String, String> tags = toMap(className, additionalTags);

        return counters.computeIfAbsent(name, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(
                        tags,
                        t -> {
                            Id id = registry.createId(name, tags);
                            return registry.counter(id);
                        });
    }

    private static Gauge getGauge(String className, String name, String... additionalTags) {
        Map<String, String> tags = toMap(className, additionalTags);

        return gauges.computeIfAbsent(name, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(
                        tags,
                        t -> {
                            Id id = registry.createId(name, tags);
                            return registry.gauge(id);
                        });
    }

    private static DistributionSummary getDistributionSummary(
            String className, String name, String... additionalTags) {
        Map<String, String> tags = toMap(className, additionalTags);

        return distributionSummaries
                .computeIfAbsent(name, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(
                        tags,
                        t -> {
                            Id id = registry.createId(name, tags);
                            return registry.distributionSummary(id);
                        });
    }

    private static Map<String, String> toMap(String className, String... additionalTags) {
        Map<String, String> tags = new HashMap<>();
        tags.put("class", className);
        for (int j = 0; j < additionalTags.length - 1; j++) {
            String tk = additionalTags[j];
            String tv = "" + additionalTags[j + 1];
            if (!tv.isEmpty()) {
                tags.put(tk, tv);
            }
            j++;
        }
        return tags;
    }

    /**
     * @param className Name of the class
     * @param methodName Method name
     */
    public static void error(String className, String methodName) {
        getCounter(className, "workflow_server_error", "methodName", methodName).increment();
    }

    public static void recordGauge(String name, long count) {
        gauge(classQualifier, name, count);
    }

    public static void recordCounter(String name, long count, String... additionalTags) {
        getCounter(classQualifier, name, additionalTags).increment(count);
    }

    public static void recordQueueWaitTime(String taskType, long queueWaitTime) {
        getTimer(classQualifier, "task_queue_wait", "taskType", taskType)
                .record(queueWaitTime, TimeUnit.MILLISECONDS);
    }


    public static void recordWorkflowDecisionTime(long duration) {
        getTimer(classQualifier, "workflow_decision").record(duration, TimeUnit.MILLISECONDS);
    }

    public static void recordTaskPollError(String taskType, String exception) {
        recordTaskPollError(taskType, NO_DOMAIN, exception);
    }

    public static void recordTaskPollError(String taskType, String domain, String exception) {
        counter(
                classQualifier,
                "task_poll_error",
                "taskType",
                taskType,
                "domain",
                domain,
                "exception",
                exception);
    }

    public static void recordTaskPoll(String taskType) {
        counter(classQualifier, "task_poll", "taskType", taskType);
    }

    public static void recordTaskPollCount(String taskType, int count) {
        recordTaskPollCount(taskType, NO_DOMAIN, count);
    }

    public static void recordTaskPollCount(String taskType, String domain, int count) {
        getCounter(classQualifier, "task_poll_count", "taskType", taskType, "domain", domain)
                .increment(count);
    }

    public static void recordQueueDepth(String taskType, long size, String ownerApp) {
        gauge(
                classQualifier,
                "task_queue_depth",
                size,
                "taskType",
                taskType,
                "ownerApp",
                StringUtils.defaultIfBlank(ownerApp, "unknown"));
    }

    public static void recordTaskInProgress(String taskType, long size, String ownerApp) {
        gauge(
                classQualifier,
                "task_in_progress",
                size,
                "taskType",
                taskType,
                "ownerApp",
                StringUtils.defaultIfBlank(ownerApp, "unknown"));
    }

    public static void recordRunningWorkflows(long count, String name, String ownerApp) {
        gauge(
                classQualifier,
                "workflow_running",
                count,
                "workflowName",
                name,
                "ownerApp",
                StringUtils.defaultIfBlank(ownerApp, "unknown"));
    }

    public static void recordNumTasksInWorkflow(long count, String name, String version) {
        distributionSummary(
                classQualifier,
                "tasks_in_workflow",
                count,
                "workflowName",
                name,
                "version",
                version);
    }

    public static void recordTaskTimeout(String taskType) {
        counter(classQualifier, "task_timeout", "taskType", taskType);
    }

    public static void recordTaskResponseTimeout(String taskType) {
        counter(classQualifier, "task_response_timeout", "taskType", taskType);
    }

    public static void recordTaskPendingTime(String taskType, String workflowType, long duration) {
        gauge(
                classQualifier,
                "task_pending_time",
                duration,
                "workflowName",
                workflowType,
                "taskType",
                taskType);
    }


    public static void recordEventQueueMessagesProcessed(
            String queueType, String queueName, int count) {
        getCounter(
                        classQualifier,
                        "event_queue_messages_processed",
                        "queueType",
                        queueType,
                        "queueName",
                        queueName)
                .increment(count);
    }

    public static void recordObservableQMessageReceivedErrors(String queueType) {
        counter(classQualifier, "observable_queue_error", "queueType", queueType);
    }

    public static void recordEventQueueMessagesHandled(String queueType, String queueName) {
        counter(
                classQualifier,
                "event_queue_messages_handled",
                "queueType",
                queueType,
                "queueName",
                queueName);
    }

    public static void recordEventQueueMessagesError(String queueType, String queueName) {
        counter(
                classQualifier,
                "event_queue_messages_error",
                "queueType",
                queueType,
                "queueName",
                queueName);
    }

    public static void recordEventExecutionSuccess(String event, String handler, String action) {
        counter(
                classQualifier,
                "event_execution_success",
                "event",
                event,
                "handler",
                handler,
                "action",
                action);
    }

    public static void recordEventExecutionError(
            String event, String handler, String action, String exceptionClazz) {
        counter(
                classQualifier,
                "event_execution_error",
                "event",
                event,
                "handler",
                handler,
                "action",
                action,
                "exception",
                exceptionClazz);
    }

    public static void recordEventActionError(String action, String entityName, String event) {
        counter(
                classQualifier,
                "event_action_error",
                "action",
                action,
                "entityName",
                entityName,
                "event",
                event);
    }

    public static void recordExternalPayloadStorageUsage(
            String name, String operation, String payloadType) {
        counter(
                classQualifier,
                "external_payload_storage_usage",
                "name",
                name,
                "operation",
                operation,
                "payloadType",
                payloadType);
    }

    public static void recordDaoError(String dao, String action) {
        counter(classQualifier, "dao_errors", "dao", dao, "action", action);
    }


    public static void recordESIndexTime(String action, String docType, long val) {
        getTimer(Monitors.classQualifier, action, "docType", docType)
                .record(val, TimeUnit.MILLISECONDS);
    }

    public static void recordWorkerQueueSize(String queueType, int val) {
        gauge(Monitors.classQualifier, "indexing_worker_queue", val, "queueType", queueType);
    }

    public static void recordDiscardedIndexingCount(String queueType) {
        counter(Monitors.classQualifier, "discarded_index_count", "queueType", queueType);
    }

    public static void recordAcquireLockUnsuccessful() {
        counter(classQualifier, "acquire_lock_unsuccessful");
    }

    public static void recordAcquireLockFailure(String exceptionClassName) {
        counter(classQualifier, "acquire_lock_failure", "exceptionType", exceptionClassName);
    }


    public static void recordArchivalDelayQueueSize(int val) {
        gauge(classQualifier, "workflow_archival_delay_queue_size", val);
    }

    public static void recordDiscardedArchivalCount() {
        counter(classQualifier, "discarded_archival_count");
    }

    public static void recordSystemTaskWorkerPollingLimited(String queueName) {
        counter(classQualifier, "system_task_worker_polling_limited", "queueName", queueName);
    }

    public static void recordEventQueuePollSize(String queueType, int val) {
        gauge(Monitors.classQualifier, "event_queue_poll", val, "queueType", queueType);
    }

    public static void recordQueueMessageRepushFromRepairService(String queueName) {
        counter(classQualifier, "queue_message_repushed", "queueName", queueName);
    }

    public static void recordTaskExecLogSize(int val) {
        gauge(classQualifier, "task_exec_log_size", val);
    }
}
