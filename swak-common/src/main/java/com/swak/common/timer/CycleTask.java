
package com.swak.common.timer;

import com.swak.common.exception.ThrowableWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  CycleTimerTask.java
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public abstract class CycleTask implements TimerTask {

	protected volatile AtomicBoolean cancel = new AtomicBoolean(false);
	private Long tick;
	private TimeUnit timeUnit;

	public CycleTask(){
	}

	public CycleTask(Long tick,TimeUnit timeUnit){
		this.tick = tick;
		this.timeUnit = timeUnit;
	}

	public CycleTask config(Integer tick, TimeUnit timeUnit,boolean isRun) {
		this.tick = tick.longValue();
		this.timeUnit = timeUnit;
		if(isRun){
			cancel.set(false);
		}else{
			cancel.set(true);
		}
		return this;
	}
	
	public CycleTask config(Long tick, TimeUnit timeUnit,boolean isRun) {
		this.tick = tick;
		this.timeUnit = timeUnit;
		if(isRun){
			cancel.set(false);
		}else{
			cancel.set(true);
		}
		return this;
	}
	
	protected abstract void  invoke() throws ThrowableWrapper; 

	static Long now() {
		return System.currentTimeMillis();
	}

	public void cancel() {
		this.cancel.set(true);
	}

	public boolean isCancelled() {
		return this.cancel.get();
	}

	public void start() {
		this.cancel.set(false);
	}

	@Override
	public void run(Timeout timeout) throws Exception {
		if(cancel.get()) {
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

		if(cancel.get()) {
			return;
		}

		Timer timer = timeout.timer();

		if (timer.isStop() || timeout.isCancelled()) {
			return;
		}

		timer.newTimeout(timeout.task(), tick, timeUnit);
	}
}
