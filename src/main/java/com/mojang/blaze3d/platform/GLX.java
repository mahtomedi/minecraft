package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWNativeGLX;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class GLX {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static String cpuInfo;

    public static String getOpenGLVersionString() {
        RenderSystem.assertOnRenderThread();
        return GLFW.glfwGetCurrentContext() == 0L
            ? "NO CONTEXT"
            : GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    public static int _getRefreshRate(Window param0) {
        RenderSystem.assertOnRenderThread();
        long var0 = GLFW.glfwGetWindowMonitor(param0.getWindow());
        if (var0 == 0L) {
            var0 = GLFW.glfwGetPrimaryMonitor();
        }

        GLFWVidMode var1 = var0 == 0L ? null : GLFW.glfwGetVideoMode(var0);
        return var1 == null ? 0 : var1.refreshRate();
    }

    public static String _getLWJGLVersion() {
        RenderSystem.assertInInitPhase();
        return Version.getVersion();
    }

    public static LongSupplier _initGlfw() {
        RenderSystem.assertInInitPhase();
        GLFWNativeGLX.setPath(GL.getFunctionProvider());
        Window.checkGlfwError((param0, param1) -> {
            throw new IllegalStateException(String.format(Locale.ROOT, "GLFW error before init: [0x%X]%s", param0, param1));
        });
        List<String> var0 = Lists.newArrayList();
        GLFWErrorCallback var1 = GLFW.glfwSetErrorCallback((param1, param2) -> {
            String var0x = param2 == 0L ? "" : MemoryUtil.memUTF8(param2);
            var0.add(String.format(Locale.ROOT, "GLFW error during init: [0x%X]%s", param1, var0x));
        });
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
        RenderSystem.assertInInitPhase();
        GLFWErrorCallback var0 = GLFW.glfwSetErrorCallback(param0);
        if (var0 != null) {
            var0.free();
        }

    }

    public static boolean _shouldClose(Window param0) {
        return GLFW.glfwWindowShouldClose(param0.getWindow());
    }

    public static void _init(int param0, boolean param1) {
        RenderSystem.assertInInitPhase();

        try {
            CentralProcessor var0 = new SystemInfo().getHardware().getProcessor();
            cpuInfo = String.format(Locale.ROOT, "%dx %s", var0.getLogicalProcessorCount(), var0.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
        } catch (Throwable var3) {
        }

        GlDebug.enableDebugCallback(param0, param1);
    }

    public static String _getCpuInfo() {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void _renderCrosshair(int param0, boolean param1, boolean param2, boolean param3) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator var0 = RenderSystem.renderThreadTesselator();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.lineWidth(4.0F);
        var1.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        if (param1) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
            var1.vertex((double)param0, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
        }

        if (param2) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
            var1.vertex(0.0, (double)param0, 0.0).color(0, 0, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        if (param3) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
            var1.vertex(0.0, 0.0, (double)param0).color(0, 0, 0, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
        }

        var0.end();
        RenderSystem.lineWidth(2.0F);
        var1.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        if (param1) {
            var1.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
            var1.vertex((double)param0, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
        }

        if (param2) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
            var1.vertex(0.0, (double)param0, 0.0).color(0, 255, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        if (param3) {
            var1.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
            var1.vertex(0.0, 0.0, (double)param0).color(127, 127, 255, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
        }

        var0.end();
        RenderSystem.lineWidth(1.0F);
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
    }

    public static <T> T make(Supplier<T> param0) {
        return param0.get();
    }

    public static <T> T make(T param0, Consumer<T> param1) {
        param1.accept(param0);
        return param0;
    }
}
