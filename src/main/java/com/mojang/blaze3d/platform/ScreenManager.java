package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;

@OnlyIn(Dist.CLIENT)
public class ScreenManager {
    private final Long2ObjectMap<Monitor> monitors = new Long2ObjectOpenHashMap<>();
    private final MonitorCreator monitorCreator;

    public ScreenManager(MonitorCreator param0) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
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
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param1 == 262145) {
            this.monitors.put(param0x, this.monitorCreator.createMonitor(param0x));
        } else if (param1 == 262146) {
            this.monitors.remove(param0x);
        }

    }

    @Nullable
    public Monitor getMonitor(long param0) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
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

            for(Monitor var7 : this.monitors.values()) {
                int var8 = var7.getX();
                int var9 = var8 + var7.getCurrentMode().getWidth();
                int var10 = var7.getY();
                int var11 = var10 + var7.getCurrentMode().getHeight();
                int var12 = clamp(var1, var8, var9);
                int var13 = clamp(var2, var8, var9);
                int var14 = clamp(var3, var10, var11);
                int var15 = clamp(var4, var10, var11);
                int var16 = Math.max(0, var13 - var12);
                int var17 = Math.max(0, var15 - var14);
                int var18 = var16 * var17;
                if (var18 > var5) {
                    var6 = var7;
                    var5 = var18;
                }
            }

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
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GLFWMonitorCallback var0 = GLFW.glfwSetMonitorCallback(null);
        if (var0 != null) {
            var0.free();
        }

    }
}
