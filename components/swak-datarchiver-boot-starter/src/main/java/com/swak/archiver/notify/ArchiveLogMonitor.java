
package com.swak.archiver.notify;

import lombok.extern.slf4j.Slf4j;

/**
 * ArchiveLogMonitor.java
 *
 * @author ColleyMa
 * @version 19-5-28 下午8:34
 */
@Slf4j
public class ArchiveLogMonitor extends AbstractArchiveMonitor {
	@Override
	public void _monitor(String monitorLog) {
		log.warn(monitorLog);
	}
}
