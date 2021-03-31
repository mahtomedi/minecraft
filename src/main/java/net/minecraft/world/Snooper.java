package net.minecraft.world;

import com.google.common.collect.Maps;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;

public class Snooper {
    private static final String POLL_HOST = "http://snoop.minecraft.net/";
    private static final long DATA_SEND_FREQUENCY = 900000L;
    private static final int SNOOPER_VERSION = 2;
    private final Map<String, Object> fixedData = Maps.newHashMap();
    private final Map<String, Object> dynamicData = Maps.newHashMap();
    private final String token = UUID.randomUUID().toString();
    private final URL url;
    private final SnooperPopulator populator;
    private final Timer timer = new Timer("Snooper Timer", true);
    private final Object lock = new Object();
    private final long startupTime;
    private boolean started;
    private int count;

    public Snooper(String param0, SnooperPopulator param1, long param2) {
        try {
            this.url = new URL("http://snoop.minecraft.net/" + param0 + "?version=" + 2);
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException();
        }

        this.populator = param1;
        this.startupTime = param2;
    }

    public void start() {
        if (!this.started) {
        }

    }

    private void populateFixedData() {
        this.setJvmArgs();
        this.setDynamicData("snooper_token", this.token);
        this.setFixedData("snooper_token", this.token);
        this.setFixedData("os_name", System.getProperty("os.name"));
        this.setFixedData("os_version", System.getProperty("os.version"));
        this.setFixedData("os_architecture", System.getProperty("os.arch"));
        this.setFixedData("java_version", System.getProperty("java.version"));
        this.setDynamicData("version", SharedConstants.getCurrentVersion().getId());
        this.populator.populateSnooperInitial(this);
    }

    private void setJvmArgs() {
        int[] var0 = new int[]{0};
        Util.getVmArguments().forEach(param1 -> this.setDynamicData("jvm_arg[" + var0[0]++ + "]", param1));
        this.setDynamicData("jvm_args", var0[0]);
    }

    public void prepare() {
        this.setFixedData("memory_total", Runtime.getRuntime().totalMemory());
        this.setFixedData("memory_max", Runtime.getRuntime().maxMemory());
        this.setFixedData("memory_free", Runtime.getRuntime().freeMemory());
        this.setFixedData("cpu_cores", Runtime.getRuntime().availableProcessors());
        this.populator.populateSnooper(this);
    }

    public void setDynamicData(String param0, Object param1) {
        synchronized(this.lock) {
            this.dynamicData.put(param0, param1);
        }
    }

    public void setFixedData(String param0, Object param1) {
        synchronized(this.lock) {
            this.fixedData.put(param0, param1);
        }
    }

    public Map<String, String> getValues() {
        Map<String, String> var0 = Maps.newLinkedHashMap();
        synchronized(this.lock) {
            this.prepare();

            for(Entry<String, Object> var1 : this.fixedData.entrySet()) {
                var0.put(var1.getKey(), var1.getValue().toString());
            }

            for(Entry<String, Object> var2 : this.dynamicData.entrySet()) {
                var0.put(var2.getKey(), var2.getValue().toString());
            }

            return var0;
        }
    }

    public boolean isStarted() {
        return this.started;
    }

    public void interrupt() {
        this.timer.cancel();
    }

    public String getToken() {
        return this.token;
    }

    public long getStartupTime() {
        return this.startupTime;
    }
}
