package com.lesofn.archsmith.server.admin.service.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

/**
 * 服务器监控服务 (基于 Oshi)
 *
 * @author sofn
 */
@Slf4j
@Service
@ConditionalOnProperty(
        name = "arch-smith.monitor.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ServerMonitorService {

    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public Map<String, Object> getServerInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            OperatingSystem os = si.getOperatingSystem();

            result.put("cpu", getCpuInfo(hal.getProcessor()));
            result.put("memory", getMemoryInfo(hal.getMemory()));
            result.put("jvm", getJvmInfo());
            result.put("os", getOsInfo(os));
            result.put("disks", getDiskInfo(os));
        } catch (Exception e) {
            log.error("获取服务器监控信息失败", e);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> getCpuInfo(CentralProcessor processor) {
        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("name", processor.getProcessorIdentifier().getName());
        cpu.put("physicalCount", processor.getPhysicalProcessorCount());
        cpu.put("logicalCount", processor.getLogicalProcessorCount());

        // CPU使用率
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        long[] ticks = processor.getSystemCpuLoadTicks();

        long user =
                ticks[CentralProcessor.TickType.USER.getIndex()]
                        - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice =
                ticks[CentralProcessor.TickType.NICE.getIndex()]
                        - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys =
                ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                        - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle =
                ticks[CentralProcessor.TickType.IDLE.getIndex()]
                        - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait =
                ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                        - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq =
                ticks[CentralProcessor.TickType.IRQ.getIndex()]
                        - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq =
                ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                        - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long total = user + nice + sys + idle + iowait + irq + softirq;

        cpu.put("userUsage", total > 0 ? Double.parseDouble(DF.format(100.0 * user / total)) : 0);
        cpu.put("sysUsage", total > 0 ? Double.parseDouble(DF.format(100.0 * sys / total)) : 0);
        cpu.put(
                "usage",
                total > 0 ? Double.parseDouble(DF.format(100.0 * (total - idle) / total)) : 0);
        cpu.put("idle", total > 0 ? Double.parseDouble(DF.format(100.0 * idle / total)) : 0);
        return cpu;
    }

    private Map<String, Object> getMemoryInfo(GlobalMemory memory) {
        Map<String, Object> mem = new LinkedHashMap<>();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        mem.put("total", formatBytes(total));
        mem.put("used", formatBytes(used));
        mem.put("available", formatBytes(available));
        mem.put("usage", Double.parseDouble(DF.format(100.0 * used / total)));
        return mem;
    }

    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvm = new LinkedHashMap<>();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();

        jvm.put("heapMax", formatBytes(heapMax));
        jvm.put("heapUsed", formatBytes(heapUsed));
        jvm.put(
                "heapUsage",
                heapMax > 0 ? Double.parseDouble(DF.format(100.0 * heapUsed / heapMax)) : 0);
        jvm.put("nonHeapUsed", formatBytes(nonHeapUsed));
        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("javaVendor", System.getProperty("java.vendor"));
        jvm.put("javaHome", System.getProperty("java.home"));
        jvm.put("vmName", runtimeMXBean.getVmName());

        // 运行时间
        long uptime = runtimeMXBean.getUptime();
        Duration duration = Duration.ofMillis(uptime);
        jvm.put("startTime", Instant.ofEpochMilli(runtimeMXBean.getStartTime()).toString());
        jvm.put(
                "uptime",
                String.format(
                        "%d天%d小时%d分钟",
                        duration.toDays(), duration.toHoursPart(), duration.toMinutesPart()));
        return jvm;
    }

    private Map<String, Object> getOsInfo(OperatingSystem os) {
        Map<String, Object> osInfo = new LinkedHashMap<>();
        osInfo.put("name", os.toString());
        osInfo.put("arch", System.getProperty("os.arch"));
        try {
            InetAddress addr = InetAddress.getLocalHost();
            osInfo.put("hostName", addr.getHostName());
            osInfo.put("hostAddress", addr.getHostAddress());
        } catch (Exception e) {
            osInfo.put("hostName", "unknown");
            osInfo.put("hostAddress", "unknown");
        }
        osInfo.put("processCount", os.getProcessCount());
        osInfo.put("threadCount", os.getThreadCount());
        return osInfo;
    }

    private List<Map<String, Object>> getDiskInfo(OperatingSystem os) {
        List<Map<String, Object>> disks = new ArrayList<>();
        FileSystem fileSystem = os.getFileSystem();
        for (OSFileStore store : fileSystem.getFileStores()) {
            long total = store.getTotalSpace();
            long usable = store.getUsableSpace();
            long used = total - usable;
            if (total <= 0) continue;

            Map<String, Object> disk = new LinkedHashMap<>();
            disk.put("name", store.getName());
            disk.put("mount", store.getMount());
            disk.put("type", store.getType());
            disk.put("total", formatBytes(total));
            disk.put("used", formatBytes(used));
            disk.put("available", formatBytes(usable));
            disk.put("usage", Double.parseDouble(DF.format(100.0 * used / total)));
            disks.add(disk);
        }
        return disks;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return DF.format(bytes / 1024.0) + " KB";
        if (bytes < 1024L * 1024 * 1024) return DF.format(bytes / (1024.0 * 1024)) + " MB";
        return DF.format(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }
}
