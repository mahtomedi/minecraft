package net.minecraft.util;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.annotation.Nullable;
import javax.management.MBeanServer;

public class HeapDumper {
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    @Nullable
    private static HotSpotDiagnosticMXBean hotspotMBean;

    private static HotSpotDiagnosticMXBean getHotspotMBean() {
        if (hotspotMBean == null) {
            try {
                MBeanServer var0 = ManagementFactory.getPlatformMBeanServer();
                hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(var0, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            } catch (IOException var11) {
                throw new RuntimeException(var11);
            }
        }

        return hotspotMBean;
    }

    public static void dumpHeap(String param0, boolean param1) {
        try {
            getHotspotMBean().dumpHeap(param0, param1);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }
}
