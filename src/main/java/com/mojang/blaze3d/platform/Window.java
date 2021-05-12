package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.main.SilentInitException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@OnlyIn(Dist.CLIENT)
public final class Window implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final GLFWErrorCallback defaultErrorCallback = GLFWErrorCallback.create(this::defaultErrorCallback);
    private final WindowEventHandler eventHandler;
    private final ScreenManager screenManager;
    private final long window;
    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;
    private Optional<VideoMode> preferredFullscreenVideoMode;
    private boolean fullscreen;
    private boolean actuallyFullscreen;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private int guiScaledWidth;
    private int guiScaledHeight;
    private double guiScale;
    private String errorSection = "";
    private boolean dirty;
    private int framerateLimit;
    private boolean vsync;

    public Window(WindowEventHandler param0, ScreenManager param1, DisplayData param2, @Nullable String param3, String param4) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        this.screenManager = param1;
        this.setBootErrorCallback();
        this.setErrorSection("Pre startup");
        this.eventHandler = param0;
        Optional<VideoMode> var0 = VideoMode.read(param3);
        if (var0.isPresent()) {
            this.preferredFullscreenVideoMode = var0;
        } else if (param2.fullscreenWidth.isPresent() && param2.fullscreenHeight.isPresent()) {
            this.preferredFullscreenVideoMode = Optional.of(new VideoMode(param2.fullscreenWidth.getAsInt(), param2.fullscreenHeight.getAsInt(), 8, 8, 8, 60));
        } else {
            this.preferredFullscreenVideoMode = Optional.empty();
        }

        this.actuallyFullscreen = this.fullscreen = param2.isFullscreen;
        Monitor var1 = param1.getMonitor(GLFW.glfwGetPrimaryMonitor());
        this.windowedWidth = this.width = param2.width > 0 ? param2.width : 1;
        this.windowedHeight = this.height = param2.height > 0 ? param2.height : 1;
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(139265, 196609);
        GLFW.glfwWindowHint(139275, 221185);
        GLFW.glfwWindowHint(139266, 3);
        GLFW.glfwWindowHint(139267, 2);
        GLFW.glfwWindowHint(139272, 204801);
        GLFW.glfwWindowHint(139270, 1);
        this.window = GLFW.glfwCreateWindow(this.width, this.height, param4, this.fullscreen && var1 != null ? var1.getMonitor() : 0L, 0L);
        if (var1 != null) {
            VideoMode var2 = var1.getPreferredVidMode(this.fullscreen ? this.preferredFullscreenVideoMode : Optional.empty());
            this.windowedX = this.x = var1.getX() + var2.getWidth() / 2 - this.width / 2;
            this.windowedY = this.y = var1.getY() + var2.getHeight() / 2 - this.height / 2;
        } else {
            int[] var3 = new int[1];
            int[] var4 = new int[1];
            GLFW.glfwGetWindowPos(this.window, var3, var4);
            this.windowedX = this.x = var3[0];
            this.windowedY = this.y = var4[0];
        }

        GLFW.glfwMakeContextCurrent(this.window);
        GL.createCapabilities();
        this.setMode();
        this.refreshFramebufferSize();
        GLFW.glfwSetFramebufferSizeCallback(this.window, this::onFramebufferResize);
        GLFW.glfwSetWindowPosCallback(this.window, this::onMove);
        GLFW.glfwSetWindowSizeCallback(this.window, this::onResize);
        GLFW.glfwSetWindowFocusCallback(this.window, this::onFocus);
        GLFW.glfwSetCursorEnterCallback(this.window, this::onEnter);
    }

    public int getRefreshRate() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GLX._getRefreshRate(this);
    }

    public boolean shouldClose() {
        return GLX._shouldClose(this);
    }

    public static void checkGlfwError(BiConsumer<Integer, String> param0) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);

        try (MemoryStack var0 = MemoryStack.stackPush()) {
            PointerBuffer var1 = var0.mallocPointer(1);
            int var2 = GLFW.glfwGetError(var1);
            if (var2 != 0) {
                long var3 = var1.get();
                String var4 = var3 == 0L ? "" : MemoryUtil.memUTF8(var3);
                param0.accept(var2, var4);
            }
        }

    }

    public void setIcon(InputStream param0, InputStream param1) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);

        try (MemoryStack var0 = MemoryStack.stackPush()) {
            if (param0 == null) {
                throw new FileNotFoundException("icons/icon_16x16.png");
            }

            if (param1 == null) {
                throw new FileNotFoundException("icons/icon_32x32.png");
            }

            IntBuffer var1 = var0.mallocInt(1);
            IntBuffer var2 = var0.mallocInt(1);
            IntBuffer var3 = var0.mallocInt(1);
            Buffer var4 = GLFWImage.mallocStack(2, var0);
            ByteBuffer var5 = this.readIconPixels(param0, var1, var2, var3);
            if (var5 == null) {
                throw new IllegalStateException("Could not load icon: " + STBImage.stbi_failure_reason());
            }

            var4.position(0);
            var4.width(var1.get(0));
            var4.height(var2.get(0));
            var4.pixels(var5);
            ByteBuffer var6 = this.readIconPixels(param1, var1, var2, var3);
            if (var6 == null) {
                throw new IllegalStateException("Could not load icon: " + STBImage.stbi_failure_reason());
            }

            var4.position(1);
            var4.width(var1.get(0));
            var4.height(var2.get(0));
            var4.pixels(var6);
            var4.position(0);
            GLFW.glfwSetWindowIcon(this.window, var4);
            STBImage.stbi_image_free(var5);
            STBImage.stbi_image_free(var6);
        } catch (IOException var12) {
            LOGGER.error("Couldn't set icon", (Throwable)var12);
        }

    }

    @Nullable
    private ByteBuffer readIconPixels(InputStream param0, IntBuffer param1, IntBuffer param2, IntBuffer param3) throws IOException {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        ByteBuffer var0 = null;

        ByteBuffer var6;
        try {
            var0 = TextureUtil.readResource(param0);
            var0.rewind();
            var6 = STBImage.stbi_load_from_memory(var0, param1, param2, param3, 0);
        } finally {
            if (var0 != null) {
                MemoryUtil.memFree(var0);
            }

        }

        return var6;
    }

    public void setErrorSection(String param0) {
        this.errorSection = param0;
    }

    private void setBootErrorCallback() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GLFW.glfwSetErrorCallback(Window::bootCrash);
    }

    private static void bootCrash(int param0, long param1) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        String var0 = "GLFW error " + param0 + ": " + MemoryUtil.memUTF8(param1);
        TinyFileDialogs.tinyfd_messageBox(
            "Minecraft", var0 + ".\n\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).", "ok", "error", false
        );
        throw new Window.WindowInitFailed(var0);
    }

    public void defaultErrorCallback(int param0x, long param1x) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        String var0x = MemoryUtil.memUTF8(param1x);
        LOGGER.error("########## GL ERROR ##########");
        LOGGER.error("@ {}", this.errorSection);
        LOGGER.error("{}: {}", param0x, var0x);
    }

    public void setDefaultErrorCallback() {
        GLFWErrorCallback var0 = GLFW.glfwSetErrorCallback(this.defaultErrorCallback);
        if (var0 != null) {
            var0.free();
        }

    }

    public void updateVsync(boolean param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.vsync = param0;
        GLFW.glfwSwapInterval(param0 ? 1 : 0);
    }

    @Override
    public void close() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Callbacks.glfwFreeCallbacks(this.window);
        this.defaultErrorCallback.close();
        GLFW.glfwDestroyWindow(this.window);
        GLFW.glfwTerminate();
    }

    private void onMove(long param0x, int param1x, int param2x) {
        this.x = param1x;
        this.y = param2x;
    }

    private void onFramebufferResize(long param0x, int param1x, int param2x) {
        if (param0x == this.window) {
            int var0x = this.getWidth();
            int var1x = this.getHeight();
            if (param1x != 0 && param2x != 0) {
                this.framebufferWidth = param1x;
                this.framebufferHeight = param2x;
                if (this.getWidth() != var0x || this.getHeight() != var1x) {
                    this.eventHandler.resizeDisplay();
                }

            }
        }
    }

    private void refreshFramebufferSize() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        int[] var0 = new int[1];
        int[] var1 = new int[1];
        GLFW.glfwGetFramebufferSize(this.window, var0, var1);
        this.framebufferWidth = var0[0];
        this.framebufferHeight = var1[0];
    }

    private void onResize(long param0x, int param1x, int param2x) {
        this.width = param1x;
        this.height = param2x;
    }

    private void onFocus(long param0x, boolean param1x) {
        if (param0x == this.window) {
            this.eventHandler.setWindowActive(param1x);
        }

    }

    private void onEnter(long param0x, boolean param1x) {
        if (param1x) {
            this.eventHandler.cursorEntered();
        }

    }

    public void setFramerateLimit(int param0) {
        this.framerateLimit = param0;
    }

    public int getFramerateLimit() {
        return this.framerateLimit;
    }

    public void updateDisplay() {
        RenderSystem.flipFrame(this.window);
        if (this.fullscreen != this.actuallyFullscreen) {
            this.actuallyFullscreen = this.fullscreen;
            this.updateFullscreen(this.vsync);
        }

    }

    public Optional<VideoMode> getPreferredFullscreenVideoMode() {
        return this.preferredFullscreenVideoMode;
    }

    public void setPreferredFullscreenVideoMode(Optional<VideoMode> param0) {
        boolean var0 = !param0.equals(this.preferredFullscreenVideoMode);
        this.preferredFullscreenVideoMode = param0;
        if (var0) {
            this.dirty = true;
        }

    }

    public void changeFullscreenVideoMode() {
        if (this.fullscreen && this.dirty) {
            this.dirty = false;
            this.setMode();
            this.eventHandler.resizeDisplay();
        }

    }

    private void setMode() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        boolean var0 = GLFW.glfwGetWindowMonitor(this.window) != 0L;
        if (this.fullscreen) {
            Monitor var1 = this.screenManager.findBestMonitor(this);
            if (var1 == null) {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                this.fullscreen = false;
            } else {
                VideoMode var2 = var1.getPreferredVidMode(this.preferredFullscreenVideoMode);
                if (!var0) {
                    this.windowedX = this.x;
                    this.windowedY = this.y;
                    this.windowedWidth = this.width;
                    this.windowedHeight = this.height;
                }

                this.x = 0;
                this.y = 0;
                this.width = var2.getWidth();
                this.height = var2.getHeight();
                GLFW.glfwSetWindowMonitor(this.window, var1.getMonitor(), this.x, this.y, this.width, this.height, var2.getRefreshRate());
            }
        } else {
            this.x = this.windowedX;
            this.y = this.windowedY;
            this.width = this.windowedWidth;
            this.height = this.windowedHeight;
            GLFW.glfwSetWindowMonitor(this.window, 0L, this.x, this.y, this.width, this.height, -1);
        }

    }

    public void toggleFullScreen() {
        this.fullscreen = !this.fullscreen;
    }

    public void setWindowed(int param0, int param1) {
        this.windowedWidth = param0;
        this.windowedHeight = param1;
        this.fullscreen = false;
        this.setMode();
    }

    private void updateFullscreen(boolean param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        try {
            this.setMode();
            this.eventHandler.resizeDisplay();
            this.updateVsync(param0);
            this.updateDisplay();
        } catch (Exception var3) {
            LOGGER.error("Couldn't toggle fullscreen", (Throwable)var3);
        }

    }

    public int calculateScale(int param0, boolean param1) {
        int var0 = 1;

        while(
            var0 != param0
                && var0 < this.framebufferWidth
                && var0 < this.framebufferHeight
                && this.framebufferWidth / (var0 + 1) >= 320
                && this.framebufferHeight / (var0 + 1) >= 240
        ) {
            ++var0;
        }

        if (param1 && var0 % 2 != 0) {
            ++var0;
        }

        return var0;
    }

    public void setGuiScale(double param0) {
        this.guiScale = param0;
        int var0 = (int)((double)this.framebufferWidth / param0);
        this.guiScaledWidth = (double)this.framebufferWidth / param0 > (double)var0 ? var0 + 1 : var0;
        int var1 = (int)((double)this.framebufferHeight / param0);
        this.guiScaledHeight = (double)this.framebufferHeight / param0 > (double)var1 ? var1 + 1 : var1;
    }

    public void setTitle(String param0) {
        GLFW.glfwSetWindowTitle(this.window, param0);
    }

    public long getWindow() {
        return this.window;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public int getWidth() {
        return this.framebufferWidth;
    }

    public int getHeight() {
        return this.framebufferHeight;
    }

    public void setWidth(int param0) {
        this.framebufferWidth = param0;
    }

    public void setHeight(int param0) {
        this.framebufferHeight = param0;
    }

    public int getScreenWidth() {
        return this.width;
    }

    public int getScreenHeight() {
        return this.height;
    }

    public int getGuiScaledWidth() {
        return this.guiScaledWidth;
    }

    public int getGuiScaledHeight() {
        return this.guiScaledHeight;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public double getGuiScale() {
        return this.guiScale;
    }

    @Nullable
    public Monitor findBestMonitor() {
        return this.screenManager.findBestMonitor(this);
    }

    public void updateRawMouseInput(boolean param0) {
        InputConstants.updateRawMouseInput(this.window, param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class WindowInitFailed extends SilentInitException {
        WindowInitFailed(String param0) {
            super(param0);
        }
    }
}
