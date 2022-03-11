package com.lesofn.appengine.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author sofn
 */
@Slf4j
public class IPUtils {

    public static final Pattern IP_PATTERN = Pattern.compile("((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

    public static boolean isIp(String in) {
        return in != null && IP_PATTERN.matcher(in).matches();
    }

    /**
     * 获取本机所有ip
     *
     * @return map key为网卡名 value为对应ip
     */
    public static Map<String, String> getLocalIps() {
        try {
            Map<String, String> result = new HashMap<String, String>();
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                String name = ni.getName();
                String ip = "";
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress address = ips.nextElement();
                    if (address instanceof Inet4Address) {
                        ip = address.getHostAddress();
                        break;
                    }
                }
                result.put(name, ip);
            }
            return result;
        } catch (SocketException e) {
            log.error("getLocalIP error", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取服务器ip 判断规则 eth0 > eth1 > ... ethN > wlan > lo
     */
    public static String getLocalIp() {

        Map<String, String> ips = getLocalIps();
        List<String> faceNames = new ArrayList<String>(ips.keySet());
        Collections.sort(faceNames);

        for (String name : faceNames) {
            if ("lo".equals(name)) {
                continue;
            }
            String ip = ips.get(name);
            if (!StringUtils.isBlank(ip)) {
                return ip;
            }
        }
        return "127.0.0.1";
    }

    private static String localIp = null;

    /**
     * 只获取一次ip
     */
    public static String getSingleLocalIp() {
        if (localIp == null) {
            localIp = getLocalIp();
        }
        return localIp;
    }

    private static final int MIN_USER_PORT_NUMBER = 1024;
    private static final int MAX_USER_PORT_NUMBER = 65536;

    /**
     * 随机返回可用端口
     *
     * @return 端口号
     */
    public static int randomAvailablePort() {
        int port;
        do {
            port = (int) ((MAX_USER_PORT_NUMBER - MIN_USER_PORT_NUMBER) * Math.random()) + MIN_USER_PORT_NUMBER;
        } while (!availablePort(port));
        return port;
    }

    /**
     * 检测该端口是否可用 <br/>
     * 端口必须大于 0 小于 {@value #MAX_USER_PORT_NUMBER}
     *
     * @param port 端口号
     * @return 是否可用
     */
    public static boolean availablePort(int port) {
        if (port < 0 || port > MAX_USER_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // ignore
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    public static String getRealIpAddr(HttpServletRequest request) {
        // just for test.
        if (request == null) {
            return "127.0.0.1";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        log.debug("x-forwarded-for:" + request.getHeader("x-forwarded-for") +
                ",Proxy-Client-IP:" + request.getHeader("Proxy-Client-IP") +
                ",WL-Proxy-Client-IP:" + request.getHeader("WL-Proxy-Client-IP") +
                ",ip:" + ip);
        if (ip != null) {
            // 为了解决移动网关的问题。移动网关过来的请求，第二个ip是真实ip
            int idx = ip.indexOf(",");
            if (idx > 0 && idx < ip.length()) {
                ip = (ip.substring(ip.indexOf(",") + 1)).trim();
            }
        }
        return ip != null ? ip.trim() : "127.0.0.1";
    }

    private static long[][] intranet_ip_ranges = new long[][]{
            {ipToInt("10.0.0.0"), ipToInt("10.255.255.255")},
            {ipToInt("172.16.0.0"), ipToInt("172.31.255.255")},
            {ipToInt("192.168.0.0"), ipToInt("192.168.255.255")}
    };

    /**
     * 是否为内网ip A类 10.0.0.0-10.255.255.255 B类 172.16.0.0-172.31.255.255 C类
     * 192.168.0.0-192.168.255.255 不包括回环ip
     *
     * @param ip 需要判断的ip地址
     * @return 是否是内网ip
     */
    public static boolean isIntranetIP(String ip) {
        if (!isIp(ip)) {
            return false;
        }
        long ipNum = ipToInt(ip);
        for (long[] range : intranet_ip_ranges) {
            if (ipNum >= range[0] && ipNum <= range[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert IP to Int.
     *
     * @param address   ip地址
     * @param isSegment true IP segment, false full IP.
     * @return 数字
     */
    public static int ipToInt(final String address, final boolean isSegment) {
        final String[] addressBytes = address.split("\\.");
        int length = addressBytes.length;
        int ip = 0;
        if (length >= 3) {
            try {
                for (int i = 0; i < 3; i++) {
                    ip <<= 8;
                    ip |= Integer.parseInt(addressBytes[i]);
                }
                ip <<= 8;
                if (!isSegment && length != 3) {
                    ip |= Integer.parseInt(addressBytes[3]);
                }
            } catch (Exception e) {
                log.warn("Warn ipToInt address is wrong: address=" + address);
            }
        }

        return ip;
    }

    /**
     * 将ip转化为数字，并且保持ip的大小顺序不变 如 ipToInt("10.75.0.1") > ipToInt("10.75.0.0")
     * 如果ip不合法则返回 0
     *
     * @param addr ip地址
     * @return 数字
     */
    public static int ipToInt(final String addr) {
        return ipToInt(addr, false);
    }
}
