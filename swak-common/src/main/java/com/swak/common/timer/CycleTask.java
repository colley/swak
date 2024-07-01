
package com.swak.common.timer;

import com.swak.common.exception.ThrowableWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: CycleTimerTask.java
 * @author: colley.ma
 * @date: 2022/01/27
 */
@Slf4j
public abstract class CycleTask implements TimerTask {

	protected volatile boolean cancel = false;
	private Long tick;
	private TimeUnit timeUnit;

	private boolean runJob;

	public CycleTask config(Integer tick, TimeUnit timeUnit, boolean runJob) {
		this.tick = tick.longValue();
		this.timeUnit = timeUnit;
		this.runJob = runJob;
		return this;
	}
	
	public CycleTask config(Long tick, TimeUnit timeUnit, boolean runJob) {
		this.tick = tick;
		this.timeUnit = timeUnit;
		this.runJob = runJob;
		return this;
	}
	
	protected abstract void  invoke() throws ThrowableWrapper; 

	static Long now() {
		return System.currentTimeMillis();
	}

	public void cancel() {
		this.cancel = true;
	}

	@Override
	public void run(Timeout timeout) throws Exception {
		if (!runJob) {
			log.warn("[swak-timer] - CycleTask job is disabled status:{}", runJob);
			return;
		}
		if(cancel) {
			log.warn("[swak-timer] - CycleTask job is canceled");
			return;
		}
		try {
			invoke();
		} catch (Throwable e) {
			log.error("[swak-timer] - CycleTask error", e);
		}
		reput(timeout, tick);
	}

	private void reput(Timeout timeout, Long tick) {
		if ((timeout == null) || (tick == null) || (timeUnit == null)) {
			throw new IllegalArgumentException();
		}

		if (cancel) {
			return;
		}

		Timer timer = timeout.timer();

		if (timer.isStop() || timeout.isCancelled()) {
			return;
		}

		timer.newTimeout(timeout.task(), tick, timeUnit);
	}
}
