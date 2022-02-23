package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ScreenManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Long2ObjectMap<Monitor> monitors = new Long2ObjectOpenHashMap<>();
    private final MonitorCreator monitorCreator;

    public ScreenManager(MonitorCreator param0) {
        RenderSystem.assertInInitPhase();
        this.monitorCreator = param0;
        GLFW.glfwSetMonitorCallback(this::onMonitorChange);
        PointerBuffer var0 = GLFW.glfwGetMonitors();
        if (var0 != null) {
            for(int var1 = 0; var1 < var0.limit(); ++var1) {
                long var2 = var0.get(var1);
                this.monitors.put(var2, param0.createMonitor(var2));
            }
        }

    }

    private void onMonitorChange(long param0x, int param1) {
        RenderSystem.assertOnRenderThread();
        if (param1 == 262145) {
            this.monitors.put(param0x, this.monitorCreator.createMonitor(param0x));
            LOGGER.debug("Monitor {} connected. Current monitors: {}", param0x, this.monitors);
        } else if (param1 == 262146) {
            this.monitors.remove(param0x);
            LOGGER.debug("Monitor {} disconnected. Current monitors: {}", param0x, this.monitors);
        }

    }

    @Nullable
    public Monitor getMonitor(long param0) {
        RenderSystem.assertInInitPhase();
        return this.monitors.get(param0);
    }

    @Nullable
    public Monitor findBestMonitor(Window param0) {
        long var0 = GLFW.glfwGetWindowMonitor(param0.getWindow());
        if (var0 != 0L) {
            return this.getMonitor(var0);
        } else {
            int var1 = param0.getX();
            int var2 = var1 + param0.getScreenWidth();
            int var3 = param0.getY();
            int var4 = var3 + param0.getScreenHeight();
            int var5 = -1;
            Monitor var6 = null;
            long var7 = GLFW.glfwGetPrimaryMonitor();
            LOGGER.debug("Selecting monitor - primary: {}, current monitors: {}", var7, this.monitors);

            for(Monitor var8 : this.monitors.values()) {
                int var9 = var8.getX();
                int var10 = var9 + var8.getCurrentMode().getWidth();
                int var11 = var8.getY();
                int var12 = var11 + var8.getCurrentMode().getHeight();
                int var13 = clamp(var1, var9, var10);
                int var14 = clamp(var2, var9, var10);
                int var15 = clamp(var3, var11, var12);
                int var16 = clamp(var4, var11, var12);
                int var17 = Math.max(0, var14 - var13);
                int var18 = Math.max(0, var16 - var15);
                int var19 = var17 * var18;
                if (var19 > var5) {
                    var6 = var8;
                    var5 = var19;
                } else if (var19 == var5 && var7 == var8.getMonitor()) {
                    LOGGER.debug("Primary monitor {} is preferred to monitor {}", var8, var6);
                    var6 = var8;
                }
            }

            LOGGER.debug("Selected monitor: {}", var6);
            return var6;
        }
    }

    public static int clamp(int param0, int param1, int param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    public void shutdown() {
        RenderSystem.assertOnRenderThread();
        GLFWMonitorCallback var0 = GLFW.glfwSetMonitorCallback(null);
        if (var0 != null) {
            var0.free();
        }

    }
}
