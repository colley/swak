package com.swak.license;

import org.apache.commons.collections4.CollectionUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractServerInfo {

    public List<String> getIpAddress() {
        List<InetAddress> inetAddresses = getLocalAllInetAddress();
        if (CollectionUtils.isNotEmpty(inetAddresses)) {
            return inetAddresses.stream().map(InetAddress::getHostAddress).distinct().map(String::toLowerCase).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<String> getMacAddress() {
        List<InetAddress> inetAddresses = getLocalAllInetAddress();
        if (CollectionUtils.isNotEmpty(inetAddresses)) {
            return inetAddresses.stream().map(this::getMacByInetAddress)
                    .filter(item -> Objects.nonNull(item)).distinct().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 获取当前服务器所有符合条件的InetAddress
     */
    protected List<InetAddress> getLocalAllInetAddress() {
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
    protected String getMacByInetAddress(InetAddress inetAddress) {
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

    public boolean checkIpAddress(List<String> expectedList, List<String> serverList) {
        if(CollectionUtils.isEmpty(expectedList)){
            return true;
        }
        if(CollectionUtils.isEmpty(serverList)){
            return false;
        }
        for (String expected : expectedList) {
            if (serverList.contains(expected.trim())) {
                return true;
            }
        }
        return false;
    }
}
