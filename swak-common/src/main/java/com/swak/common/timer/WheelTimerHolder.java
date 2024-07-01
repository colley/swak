package com.swak.common.timer;

import com.swak.common.util.NamedThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * WheelTimerHolder.java
 * model_param_type
 * @author ColleyMa
 * @version 19-9-6 上午11:28
 */
public final class WheelTimerHolder {
	
	private WheelTimerHolder() {
		 throw new UnsupportedOperationException("It's prohibited to create instances of the class.");
	}
	
	private static HashedWheelTimer monitorWheel;

	private static HashedWheelTimer lockWatchWheel;

	private static HashedWheelTimer refreshableWheel;

	private static Object lock = new Object();

	public static HashedWheelTimer refreshableWheel() {
		if (refreshableWheel == null) {
			synchronized (lock) {
				if (refreshableWheel == null) {
					refreshableWheel = new HashedWheelTimer(new NamedThreadFactory("refreshableWheel", true), 1,
							TimeUnit.SECONDS);
				}
			}
		}
		return refreshableWheel;
	}

	public static HashedWheelTimer monitorWheel() {
		if (monitorWheel == null) {
			synchronized (lock) {
				if (monitorWheel == null) {
					monitorWheel = new HashedWheelTimer(new NamedThreadFactory("monitorWheel", true), 1,
							TimeUnit.SECONDS);
				}
			}
		}
		return monitorWheel;
	}

	public static HashedWheelTimer lockWatchWheel() {
		if (lockWatchWheel == null) {
			synchronized (lock) {
				if (lockWatchWheel == null) {
					lockWatchWheel = new HashedWheelTimer(new NamedThreadFactory("lockWatchWheel", true), 100,
							TimeUnit.MILLISECONDS, 1024);
				}
			}
		}
		return lockWatchWheel;
	}
}
