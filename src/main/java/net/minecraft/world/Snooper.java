package net.minecraft.world;

import com.google.common.collect.Maps;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Snooper {
    private final Map<String, Object> fixedData = Maps.newHashMap();
    private final Map<String, Object> dynamicData = Maps.newHashMap();
    private final String token = UUID.randomUUID().toString();
    private final URL url;
    private final SnooperPopulator populator;
    private final Timer timer = new Timer("Snooper Timer", true);
    private final Object lock = new Object();
    private final long startupTime;
    private boolean started;

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

    public boolean isStarted() {
        return this.started;
    }

    public void interrupt() {
        this.timer.cancel();
    }

    @OnlyIn(Dist.CLIENT)
    public String getToken() {
        return this.token;
    }

    public long getStartupTime() {
        return this.startupTime;
    }
}
