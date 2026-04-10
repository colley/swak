package com.swak.common.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: UUIDHexGenerator.java 
 *  UUID生产策略 IP+JVM+TIME>>32+TIME+COUNT
 * @author colley.ma
 * @since 2.4.0
 */
public class UUIDHexGenerator extends AbstractUUIDGenerator {
	public static final UUIDHexGenerator DEFAULT = new UUIDHexGenerator();
	private String sep = "";

	protected String format(final int interval) {
		final String formatted = Integer.toHexString(interval);
		final StringBuilder buf = new StringBuilder("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);
		return buf.toString();
	}

	protected String format(final short shortVal) {
		final String formatted = Integer.toHexString(shortVal);
		final StringBuilder buf = new StringBuilder("0000");
		buf.replace(4 - formatted.length(), 4, formatted);
		return buf.toString();
	}

	public Serializable generate(final Object obj) {
		return new StringBuilder(36).append(format(getIP())).append(sep).append(format(getJVM())).append(sep)
				.append(format(getHiTime())).append(sep).append(format(getLoTime())).append(sep)
				.append(format(getCount())).toString();
	}

	public void configure(final Properties params) {
		sep = params.getProperty("separator", "");
	}

	public static final String generator() {
		return String.valueOf(UUIDHexGenerator.DEFAULT.generate(null));
	}

	public static final String generator(final Object obj) {
		return String.valueOf(UUIDHexGenerator.DEFAULT.generate(obj));
	}

}

 abstract class AbstractUUIDGenerator {
	private static final int IP;
	static {
		int ipaddr;
		try {
			ipaddr = toInt(InetAddress.getLocalHost().getAddress());
		} catch (final Exception e) {
			ipaddr = 0;
		}
		IP = ipaddr;
	}

	public static int toInt(final byte[] bytes) {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result = (result << 8) - Byte.MIN_VALUE + bytes[i];
		}
		return result;
	}

	private static AtomicInteger counter = new AtomicInteger(0);
	private static final int JVM = (int) (System.currentTimeMillis() >>> 8);

	public AbstractUUIDGenerator() {
	}

	protected int getJVM() {
		return JVM;
	}

	protected  short getCount() {
		int countNum = counter.addAndGet(1);
		if (countNum > Short.MAX_VALUE || countNum < 0) {
			counter.set(0);
			return 0;
		}
		return (short) countNum;
	}

	protected int getIP() {
		return IP;
	}

	protected short getHiTime() {
		return (short) (System.currentTimeMillis() >>> 32);
	}

	protected int getLoTime() {
		return (int) System.currentTimeMillis();
	}
}

