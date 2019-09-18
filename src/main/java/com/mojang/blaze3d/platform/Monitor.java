package com.mojang.blaze3d.platform;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;

@OnlyIn(Dist.CLIENT)
public final class Monitor {
    private final long monitor;
    private final List<VideoMode> videoModes;
    private VideoMode currentMode;
    private int x;
    private int y;

    public Monitor(long param0) {
        this.monitor = param0;
        this.videoModes = Lists.newArrayList();
        this.refreshVideoModes();
    }

    public void refreshVideoModes() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        this.videoModes.clear();
        Buffer var0 = GLFW.glfwGetVideoModes(this.monitor);

        for(int var1 = var0.limit() - 1; var1 >= 0; --var1) {
            var0.position(var1);
            VideoMode var2 = new VideoMode(var0);
            if (var2.getRedBits() >= 8 && var2.getGreenBits() >= 8 && var2.getBlueBits() >= 8) {
                this.videoModes.add(var2);
            }
        }

        int[] var3 = new int[1];
        int[] var4 = new int[1];
        GLFW.glfwGetMonitorPos(this.monitor, var3, var4);
        this.x = var3[0];
        this.y = var4[0];
        GLFWVidMode var5 = GLFW.glfwGetVideoMode(this.monitor);
        this.currentMode = new VideoMode(var5);
    }

    public VideoMode getPreferredVidMode(Optional<VideoMode> param0) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        if (param0.isPresent()) {
            VideoMode var0 = param0.get();

            for(VideoMode var1 : this.videoModes) {
                if (var1.equals(var0)) {
                    return var1;
                }
            }
        }

        return this.getCurrentMode();
    }

    public int getVideoModeIndex(VideoMode param0) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return this.videoModes.indexOf(param0);
    }

    public VideoMode getCurrentMode() {
        return this.currentMode;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public VideoMode getMode(int param0) {
        return this.videoModes.get(param0);
    }

    public int getModeCount() {
        return this.videoModes.size();
    }

    public long getMonitor() {
        return this.monitor;
    }

    @Override
    public String toString() {
        return String.format("Monitor[%s %sx%s %s]", this.monitor, this.x, this.y, this.currentMode);
    }
}
