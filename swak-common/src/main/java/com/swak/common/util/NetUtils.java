
package com.swak.common.util;

import com.swak.common.exception.SwakException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NetUtils 获取IP等信息
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public final class NetUtils {

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
    public static String getLocalhost() {
        InetAddress localhost = getLocalInetAddress();
        if (null != localhost) {
            return localhost.getHostAddress();
        }
        return null;
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

    public static InetAddress getLocalInetAddress() {
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

    public static List<String> getIpAddress() {
        List<InetAddress> inetAddresses = getLocalAllInetAddress();
        if (CollectionUtils.isNotEmpty(inetAddresses)) {
            return inetAddresses.stream().map(InetAddress::getHostAddress).distinct().map(String::toLowerCase).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<String> getMacAddress() {
        List<InetAddress> inetAddresses = getLocalAllInetAddress();
        if (CollectionUtils.isNotEmpty(inetAddresses)) {
            return inetAddresses.stream().map(NetUtils::getMacByInetAddress).filter(item -> Objects.nonNull(item)).distinct().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 获取当前服务器所有符合条件的InetAddress
     */
    public static List<InetAddress> getLocalAllInetAddress() {
        try {
            List<InetAddress> result = new ArrayList<>(4);
            // 遍历所有的网络接口
            for (Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) networkInterfaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration ineptAddresses = iface.getInetAddresses(); ineptAddresses.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) ineptAddresses.nextElement();
                    //排除LoopbackAddress、SiteLocalAddress、LinkLocalAddress、MulticastAddress类型的IP地址
                    if (!inetAddr.isLoopbackAddress() /*&& !inetAddr.isSiteLocalAddress()*/
                            && !inetAddr.isLinkLocalAddress() && !inetAddr.isMulticastAddress()) {
                        result.add(inetAddr);
                    }
                }
            }
            return result;
        } catch (SocketException e) {
        }
        return Collections.emptyList();
    }

    /**
     * 获取某个网络接口的Mac地址
     */
    public static String getMacByInetAddress(InetAddress inetAddress) {
        try {
            byte[] hardwareAddress = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
            if (hardwareAddress == null) {
                return null;
            }
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < hardwareAddress.length; i++) {
                if (i != 0) {
                    stringBuffer.append("-");
                }
                //将十六进制byte转化为字符串
                String temp = Integer.toHexString(hardwareAddress[i] & 0xff);
                if (temp.length() == 1) {
                    stringBuffer.append("0" + temp);
                } else {
                    stringBuffer.append(temp);
                }
            }
            return stringBuffer.toString().toUpperCase();
        } catch (SocketException e) {

        }
        return null;
    }
}
