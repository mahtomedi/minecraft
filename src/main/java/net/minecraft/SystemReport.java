package net.minecraft;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;

public class SystemReport {
    public static final long BYTES_PER_MEBIBYTE = 1048576L;
    private static final long ONE_GIGA = 1000000000L;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String OPERATING_SYSTEM = System.getProperty("os.name")
        + " ("
        + System.getProperty("os.arch")
        + ") version "
        + System.getProperty("os.version");
    private static final String JAVA_VERSION = System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
    private static final String JAVA_VM_VERSION = System.getProperty("java.vm.name")
        + " ("
        + System.getProperty("java.vm.info")
        + "), "
        + System.getProperty("java.vm.vendor");
    private final Map<String, String> entries = Maps.newLinkedHashMap();

    public SystemReport() {
        this.setDetail("Minecraft Version", SharedConstants.getCurrentVersion().getName());
        this.setDetail("Minecraft Version ID", SharedConstants.getCurrentVersion().getId());
        this.setDetail("Operating System", OPERATING_SYSTEM);
        this.setDetail("Java Version", JAVA_VERSION);
        this.setDetail("Java VM Version", JAVA_VM_VERSION);
        this.setDetail("Memory", () -> {
            Runtime var0 = Runtime.getRuntime();
            long var1 = var0.maxMemory();
            long var2 = var0.totalMemory();
            long var3 = var0.freeMemory();
            long var4 = var1 / 1048576L;
            long var5 = var2 / 1048576L;
            long var6 = var3 / 1048576L;
            return var3 + " bytes (" + var6 + " MiB) / " + var2 + " bytes (" + var5 + " MiB) up to " + var1 + " bytes (" + var4 + " MiB)";
        });
        this.setDetail("CPUs", () -> String.valueOf(Runtime.getRuntime().availableProcessors()));
        this.ignoreErrors("hardware", () -> this.putHardware(new SystemInfo()));
        this.setDetail("JVM Flags", () -> {
            List<String> var0 = Util.getVmArguments().collect(Collectors.toList());
            return String.format("%d total; %s", var0.size(), String.join(" ", var0));
        });
    }

    public void setDetail(String param0, String param1) {
        this.entries.put(param0, param1);
    }

    public void setDetail(String param0, Supplier<String> param1) {
        try {
            this.setDetail(param0, param1.get());
        } catch (Exception var4) {
            LOGGER.warn("Failed to get system info for {}", param0, var4);
            this.setDetail(param0, "ERR");
        }

    }

    private void putHardware(SystemInfo param0) {
        HardwareAbstractionLayer var0 = param0.getHardware();
        this.ignoreErrors("processor", () -> this.putProcessor(var0.getProcessor()));
        this.ignoreErrors("graphics", () -> this.putGraphics(var0.getGraphicsCards()));
        this.ignoreErrors("memory", () -> this.putMemory(var0.getMemory()));
    }

    private void ignoreErrors(String param0, Runnable param1) {
        try {
            param1.run();
        } catch (Throwable var4) {
            LOGGER.warn("Failed retrieving info for group {}", param0, var4);
        }

    }

    private void putPhysicalMemory(List<PhysicalMemory> param0) {
        int var0 = 0;

        for(PhysicalMemory var1 : param0) {
            String var2 = String.format("Memory slot #%d ", var0++);
            this.setDetail(var2 + "capacity (MB)", () -> String.format("%.2f", (float)var1.getCapacity() / 1048576.0F));
            this.setDetail(var2 + "clockSpeed (GHz)", () -> String.format("%.2f", (float)var1.getClockSpeed() / 1.0E9F));
            this.setDetail(var2 + "type", var1::getMemoryType);
        }

    }

    private void putVirtualMemory(VirtualMemory param0) {
        this.setDetail("Virtual memory max (MB)", () -> String.format("%.2f", (float)param0.getVirtualMax() / 1048576.0F));
        this.setDetail("Virtual memory used (MB)", () -> String.format("%.2f", (float)param0.getVirtualInUse() / 1048576.0F));
        this.setDetail("Swap memory total (MB)", () -> String.format("%.2f", (float)param0.getSwapTotal() / 1048576.0F));
        this.setDetail("Swap memory used (MB)", () -> String.format("%.2f", (float)param0.getSwapUsed() / 1048576.0F));
    }

    private void putMemory(GlobalMemory param0) {
        this.ignoreErrors("physical memory", () -> this.putPhysicalMemory(param0.getPhysicalMemory()));
        this.ignoreErrors("virtual memory", () -> this.putVirtualMemory(param0.getVirtualMemory()));
    }

    private void putGraphics(List<GraphicsCard> param0) {
        int var0 = 0;

        for(GraphicsCard var1 : param0) {
            String var2 = String.format("Graphics card #%d ", var0++);
            this.setDetail(var2 + "name", var1::getName);
            this.setDetail(var2 + "vendor", var1::getVendor);
            this.setDetail(var2 + "VRAM (MB)", () -> String.format("%.2f", (float)var1.getVRam() / 1048576.0F));
            this.setDetail(var2 + "deviceId", var1::getDeviceId);
            this.setDetail(var2 + "versionInfo", var1::getVersionInfo);
        }

    }

    private void putProcessor(CentralProcessor param0) {
        ProcessorIdentifier var0 = param0.getProcessorIdentifier();
        this.setDetail("Processor Vendor", var0::getVendor);
        this.setDetail("Processor Name", var0::getName);
        this.setDetail("Identifier", var0::getIdentifier);
        this.setDetail("Microarchitecture", var0::getMicroarchitecture);
        this.setDetail("Frequency (GHz)", () -> String.format("%.2f", (float)var0.getVendorFreq() / 1.0E9F));
        this.setDetail("Number of physical packages", () -> String.valueOf(param0.getPhysicalPackageCount()));
        this.setDetail("Number of physical CPUs", () -> String.valueOf(param0.getPhysicalProcessorCount()));
        this.setDetail("Number of logical CPUs", () -> String.valueOf(param0.getLogicalProcessorCount()));
    }

    public void appendToCrashReportString(StringBuilder param0) {
        param0.append("-- ").append("System Details").append(" --\n");
        param0.append("Details:");
        this.entries.forEach((param1, param2) -> {
            param0.append("\n\t");
            param0.append(param1);
            param0.append(": ");
            param0.append(param2);
        });
    }

    public String toLineSeparatedString() {
        return this.entries
            .entrySet()
            .stream()
            .map(param0 -> (String)param0.getKey() + ": " + (String)param0.getValue())
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
