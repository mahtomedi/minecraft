package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import oshi.SystemInfo;
import oshi.hardware.Processor;

@OnlyIn(Dist.CLIENT)
public class GLX {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String capsString = "";
    private static String cpuInfo;
    private static final Map<Integer, String> LOOKUP_MAP = make(Maps.newHashMap(), param0 -> {
        param0.put(0, "No error");
        param0.put(1280, "Enum parameter is invalid for this function");
        param0.put(1281, "Parameter is invalid for this function");
        param0.put(1282, "Current state is invalid for this function");
        param0.put(1283, "Stack overflow");
        param0.put(1284, "Stack underflow");
        param0.put(1285, "Out of memory");
        param0.put(1286, "Operation on incomplete framebuffer");
        param0.put(1286, "Operation on incomplete framebuffer");
    });

    public static String getOpenGLVersionString() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GLFW.glfwGetCurrentContext() == 0L
            ? "NO CONTEXT"
            : GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    public static int _getRefreshRate(Window param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        long var0 = GLFW.glfwGetWindowMonitor(param0.getWindow());
        if (var0 == 0L) {
            var0 = GLFW.glfwGetPrimaryMonitor();
        }

        GLFWVidMode var1 = var0 == 0L ? null : GLFW.glfwGetVideoMode(var0);
        return var1 == null ? 0 : var1.refreshRate();
    }

    public static String _getLWJGLVersion() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return Version.getVersion();
    }

    public static LongSupplier _initGlfw() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        Window.checkGlfwError((param0, param1) -> {
            throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", param0, param1));
        });
        List<String> var0 = Lists.newArrayList();
        GLFWErrorCallback var1 = GLFW.glfwSetErrorCallback((param1, param2) -> var0.add(String.format("GLFW error during init: [0x%X]%s", param1, param2)));
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(var0));
        } else {
            LongSupplier var2 = () -> (long)(GLFW.glfwGetTime() * 1.0E9);

            for(String var3 : var0) {
                LOGGER.error("GLFW error collected during initialization: {}", var3);
            }

            RenderSystem.setErrorCallback(var1);
            return var2;
        }
    }

    public static void _setGlfwErrorCallback(GLFWErrorCallbackI param0) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GLFWErrorCallback var0 = GLFW.glfwSetErrorCallback(param0);
        if (var0 != null) {
            var0.free();
        }

    }

    public static boolean _shouldClose(Window param0) {
        return GLFW.glfwWindowShouldClose(param0.getWindow());
    }

    public static void _pollEvents() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GLFW.glfwPollEvents();
    }

    public static void _setupNvFogDistance() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (GL.getCapabilities().GL_NV_fog_distance) {
            GlStateManager._fogi(34138, 34139);
        }

    }

    public static void _init(int param0, boolean param1) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GLCapabilities var0 = GL.getCapabilities();
        capsString = "Using framebuffer using " + GlStateManager._init_fbo(var0);

        try {
            Processor[] var1 = new SystemInfo().getHardware().getProcessors();
            cpuInfo = String.format("%dx %s", var1.length, var1[0]).replaceAll("\\s+", " ");
        } catch (Throwable var4) {
        }

        GlDebug.enableDebugCallback(param0, param1);
    }

    public static String _getCapsString() {
        return capsString;
    }

    public static String _getCpuInfo() {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void _renderCrosshair(int param0, boolean param1, boolean param2, boolean param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._disableTexture();
        GlStateManager._depthMask(false);
        Tesselator var0 = RenderSystem.renderThreadTesselator();
        BufferBuilder var1 = var0.getBuilder();
        GL11.glLineWidth(4.0F);
        var1.begin(1, DefaultVertexFormat.POSITION_COLOR);
        if (param1) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            var1.vertex((double)param0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
        }

        if (param2) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            var1.vertex(0.0, (double)param0, 0.0).color(0, 0, 0, 255).endVertex();
        }

        if (param3) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            var1.vertex(0.0, 0.0, (double)param0).color(0, 0, 0, 255).endVertex();
        }

        var0.end();
        GL11.glLineWidth(2.0F);
        var1.begin(1, DefaultVertexFormat.POSITION_COLOR);
        if (param1) {
            var1.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
            var1.vertex((double)param0, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
        }

        if (param2) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).endVertex();
            var1.vertex(0.0, (double)param0, 0.0).color(0, 255, 0, 255).endVertex();
        }

        if (param3) {
            var1.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).endVertex();
            var1.vertex(0.0, 0.0, (double)param0).color(127, 127, 255, 255).endVertex();
        }

        var0.end();
        GL11.glLineWidth(1.0F);
        GlStateManager._depthMask(true);
        GlStateManager._enableTexture();
    }

    public static String getErrorString(int param0) {
        return LOOKUP_MAP.get(param0);
    }

    public static <T> T make(Supplier<T> param0) {
        return param0.get();
    }

    public static <T> T make(T param0, Consumer<T> param1) {
        param1.accept(param0);
        return param0;
    }
}
