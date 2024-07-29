
package com.swak.common.util;

import com.swak.common.exception.SwakException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.util.Enumeration;
import java.util.LinkedHashSet;

/**
 *
 * NetUtils 获取IP等信息
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public final class NetUtils {
	public static String localhostName;

	/**
	 * 获取本机网卡IP地址，这个地址为所有网卡中非回路地址的第一个<br>
	 * 如果获取失败调用 {@link InetAddress#getLocalHost()}方法获取。<br>
	 * 此方法不会抛出异常，获取失败将返回{@code null}<br>
	 * <p>
	 * 参考：http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
	 *
	 * @return 本机网卡IP地址，获取失败返回{@code null}
	 * @since 3.0.7
	 */
	public static String getLocalhostStr() {
		InetAddress localhost = getLocalhost();
		if (null != localhost) {
			return localhost.getHostAddress();
		}
		return null;
	}

	/**
	 * 获取本机网卡IP地址，规则如下：
	 *
	 * <pre>
	 * 1. 查找所有网卡地址，必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv4地址
	 * 2. 如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址
	 * </pre>
	 * <p>
	 * 此方法不会抛出异常，获取失败将返回{@code null}<br>
	 * <p>
	 * 见：https://github.com/dromara/hutool/issues/428
	 *
	 * @return 本机网卡IP地址，获取失败返回{@code null}
	 * @since 3.0.1
	 */
	public static InetAddress getLocalhost() {
		final LinkedHashSet<InetAddress> localAddressList = localAddressList(address -> {
			// 非loopback地址，指127.*.*.*的地址
			return false == address.isLoopbackAddress()
					// 需为IPV4地址
					&& address instanceof Inet4Address;
		});

		if (CollectionUtils.isNotEmpty(localAddressList)) {
			InetAddress address2 = null;
			for (InetAddress inetAddress : localAddressList) {
				if (false == inetAddress.isSiteLocalAddress()) {
					// 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
					return inetAddress;
				} else if (null == address2) {
					address2 = inetAddress;
				}
			}

			if (null != address2) {
				return address2;
			}
		}

		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// ignore
		}

		return null;
	}

	/**
	 * 获得本机MAC地址
	 *
	 * @return 本机MAC地址
	 */
	public static String getLocalMacAddress() {
		return getMacAddress(getLocalhost());
	}

	/**
	 * 获得指定地址信息中的MAC地址，使用分隔符“-”
	 *
	 * @param inetAddress {@link InetAddress}
	 * @return MAC地址，用-分隔
	 */
	public static String getMacAddress(InetAddress inetAddress) {
		return getMacAddress(inetAddress, "-");
	}

	/**
	 * 获得指定地址信息中的MAC地址
	 *
	 * @param inetAddress {@link InetAddress}
	 * @param separator   分隔符，推荐使用“-”或者“:”
	 * @return MAC地址，用-分隔
	 */
	public static String getMacAddress(InetAddress inetAddress, String separator) {
		if (null == inetAddress) {
			return null;
		}

		final byte[] mac = getHardwareAddress(inetAddress);
		if (null != mac) {
			final StringBuilder sb = new StringBuilder();
			String s;
			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					sb.append(separator);
				}
				// 字节转换为整数
				s = Integer.toHexString(mac[i] & 0xFF);
				sb.append(s.length() == 1 ? 0 + s : s);
			}
			return sb.toString();
		}

		return null;
	}

	/**
	 * 获得指定地址信息中的硬件地址
	 *
	 * @param inetAddress {@link InetAddress}
	 * @return 硬件地址
	 * @since 5.7.3
	 */
	public static byte[] getHardwareAddress(InetAddress inetAddress) {
		if (null == inetAddress) {
			return null;
		}

		try {
			final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
			if (null != networkInterface) {
				return networkInterface.getHardwareAddress();
			}
		} catch (SocketException e) {
			throw new SwakException(e);
		}
		return null;
	}

	/**
	 * 获得本机物理地址
	 *
	 * @return 本机物理地址
	 * @since 5.7.3
	 */
	public static byte[] getLocalHardwareAddress() {
		return getHardwareAddress(getLocalhost());
	}

	/**
	 * 获取主机名称，一次获取会缓存名称<br>
	 * 注意此方法会触发反向DNS解析，导致阻塞，阻塞时间取决于网络！
	 *
	 * @return 主机名称
	 * @since 5.4.4
	 */
	public static String getLocalHostName() {
		if (StringUtils.isNotBlank(localhostName)) {
			return localhostName;
		}

		final InetAddress localhost = getLocalhost();

		if (null != localhost) {
			String name = localhost.getHostName();
			if (StringUtils.isEmpty(name)) {
				name = localhost.getHostAddress();
			}
			localhostName = name;
		}

		return localhostName;
	}

	/**
	 * 获取所有满足过滤条件的本地IP地址对象
	 *
	 * @param addressFilter 过滤器，null表示不过滤，获取所有地址
	 * @return 过滤后的地址对象列表
	 * @since 4.5.17
	 */
	public static LinkedHashSet<InetAddress> localAddressList(Filter<InetAddress> addressFilter) {
		return localAddressList(null, addressFilter);
	}

	/**
	 * 获取所有满足过滤条件的本地IP地址对象
	 *
	 * @param addressFilter          过滤器，null表示不过滤，获取所有地址
	 * @param networkInterfaceFilter 过滤器，null表示不过滤，获取所有网卡
	 * @return 过滤后的地址对象列表
	 */
	public static LinkedHashSet<InetAddress> localAddressList(Filter<NetworkInterface> networkInterfaceFilter, Filter<InetAddress> addressFilter) {
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			throw new SwakException(e);
		}

		if (networkInterfaces == null) {
			throw new SwakException("Get network interface error!");
		}

		final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces.nextElement();
			if (networkInterfaceFilter != null && false == networkInterfaceFilter.accept(networkInterface)) {
				continue;
			}
			final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				final InetAddress inetAddress = inetAddresses.nextElement();
				if (inetAddress != null && (null == addressFilter || addressFilter.accept(inetAddress))) {
					ipSet.add(inetAddress);
				}
			}
		}

		return ipSet;
	}

}
