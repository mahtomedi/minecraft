package com.mojang.blaze3d;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class Blaze3D {
    public static void process(RenderPipeline param0, float param1) {
        ConcurrentLinkedQueue<RenderCall> var0 = param0.getRecordingQueue();
    }

    public static void render(RenderPipeline param0, float param1) {
        ConcurrentLinkedQueue<RenderCall> var0 = param0.getProcessedQueue();
    }

    public static void youJustLostTheGame() {
        MemoryUtil.memSet(0L, 0, 1L);
    }

    public static double getTime() {
        return GLFW.glfwGetTime();
    }
}
