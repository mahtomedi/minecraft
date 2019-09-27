package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;

@OnlyIn(Dist.CLIENT)
public class RenderSystem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
    private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
    public static final float DEFAULTALPHACUTOFF = 0.1F;
    private static boolean isReplayingQueue;
    private static Thread gameThread;
    private static Thread renderThread;
    private static int MAX_SUPPORTED_TEXTURE_SIZE = -1;
    private static boolean isInInit;
    private static double lastDrawTime = Double.MIN_VALUE;

    public static void initRenderThread() {
        if (renderThread == null && gameThread != Thread.currentThread()) {
            renderThread = Thread.currentThread();
        } else {
            throw new IllegalStateException("Could not initialize render thread");
        }
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static boolean isOnRenderThreadOrInit() {
        return isInInit || isOnRenderThread();
    }

    public static void initGameThread(boolean param0) {
        boolean var0 = renderThread == Thread.currentThread();
        if (gameThread == null && renderThread != null && var0 != param0) {
            gameThread = Thread.currentThread();
        } else {
            throw new IllegalStateException("Could not initialize tick thread");
        }
    }

    public static boolean isOnGameThread() {
        return true;
    }

    public static boolean isOnGameThreadOrInit() {
        return isInInit || isOnGameThread();
    }

    public static void assertThread(Supplier<Boolean> param0) {
        if (!param0.get()) {
            throw new IllegalStateException("Rendersystem called from wrong thread");
        }
    }

    public static boolean isInInitPhase() {
        return true;
    }

    public static void recordRenderCall(RenderCall param0) {
        recordingQueue.add(param0);
    }

    public static void flipFrame(long param0) {
        GLFW.glfwPollEvents();
        replayQueue();
        Tesselator.getInstance().getBuilder().clear();
        GLFW.glfwSwapBuffers(param0);
        GLFW.glfwPollEvents();
    }

    public static void replayQueue() {
        isReplayingQueue = true;

        while(!recordingQueue.isEmpty()) {
            RenderCall var0 = recordingQueue.poll();
            var0.execute();
        }

        isReplayingQueue = false;
    }

    public static void limitDisplayFPS(int param0) {
        double var0 = lastDrawTime + 1.0 / (double)param0;

        double var1;
        for(var1 = GLFW.glfwGetTime(); var1 < var0; var1 = GLFW.glfwGetTime()) {
            GLFW.glfwWaitEventsTimeout(var0 - var1);
        }

        lastDrawTime = var1;
    }

    public static void pushLightingAttributes() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushLightingAttributes();
    }

    public static void pushTextureAttributes() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushTextureAttributes();
    }

    public static void popAttributes() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._popAttributes();
    }

    public static void disableAlphaTest() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableAlphaTest();
    }

    public static void enableAlphaTest() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableAlphaTest();
    }

    public static void alphaFunc(int param0, float param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._alphaFunc(param0, param1);
    }

    public static void enableLighting() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableLighting();
    }

    public static void disableLighting() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableLighting();
    }

    public static void enableColorMaterial() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableColorMaterial();
    }

    public static void disableColorMaterial() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableColorMaterial();
    }

    public static void colorMaterial(int param0, int param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._colorMaterial(param0, param1);
    }

    public static void normal3f(float param0, float param1, float param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._normal3f(param0, param1, param2);
    }

    public static void disableDepthTest() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableDepthTest();
    }

    public static void enableDepthTest() {
        assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._enableDepthTest();
    }

    public static void depthFunc(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._depthFunc(param0);
    }

    public static void depthMask(boolean param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._depthMask(param0);
    }

    public static void enableBlend() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableBlend();
    }

    public static void disableBlend() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableBlend();
    }

    public static void blendFunc(GlStateManager.SourceFactor param0, GlStateManager.DestFactor param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFunc(param0.value, param1.value);
    }

    public static void blendFunc(int param0, int param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFunc(param0, param1);
    }

    public static void blendFuncSeparate(
        GlStateManager.SourceFactor param0, GlStateManager.DestFactor param1, GlStateManager.SourceFactor param2, GlStateManager.DestFactor param3
    ) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFuncSeparate(param0.value, param1.value, param2.value, param3.value);
    }

    public static void blendFuncSeparate(int param0, int param1, int param2, int param3) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFuncSeparate(param0, param1, param2, param3);
    }

    public static void blendEquation(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendEquation(param0);
    }

    public static void blendColor(float param0, float param1, float param2, float param3) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendColor(param0, param1, param2, param3);
    }

    public static void enableFog() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableFog();
    }

    public static void disableFog() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableFog();
    }

    public static void fogMode(GlStateManager.FogMode param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogMode(param0.value);
    }

    public static void fogMode(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogMode(param0);
    }

    public static void fogDensity(float param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogDensity(param0);
    }

    public static void fogStart(float param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogStart(param0);
    }

    public static void fogEnd(float param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogEnd(param0);
    }

    public static void fog(int param0, FloatBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fog(param0, param1);
    }

    public static void fogi(int param0, int param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogi(param0, param1);
    }

    public static void enableCull() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableCull();
    }

    public static void cullFace(GlStateManager.CullFace param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._cullFace(param0.value);
    }

    public static void cullFace(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._cullFace(param0);
    }

    public static void polygonMode(int param0, int param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._polygonMode(param0, param1);
    }

    public static void enablePolygonOffset() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enablePolygonOffset();
    }

    public static void disablePolygonOffset() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disablePolygonOffset();
    }

    public static void enableLineOffset() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableLineOffset();
    }

    public static void disableLineOffset() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableLineOffset();
    }

    public static void polygonOffset(float param0, float param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._polygonOffset(param0, param1);
    }

    public static void enableColorLogicOp() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableColorLogicOp();
    }

    public static void disableColorLogicOp() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableColorLogicOp();
    }

    public static void logicOp(GlStateManager.LogicOp param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._logicOp(param0.value);
    }

    public static void activeTexture(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._activeTexture(param0);
    }

    public static void enableTexture() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableTexture();
    }

    public static void disableTexture() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableTexture();
    }

    public static void texParameter(int param0, int param1, int param2) {
        GlStateManager._texParameter(param0, param1, param2);
    }

    public static void deleteTexture(int param0) {
        assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._deleteTexture(param0);
    }

    public static void bindTexture(int param0) {
        GlStateManager._bindTexture(param0);
    }

    public static void shadeModel(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._shadeModel(param0);
    }

    public static void enableRescaleNormal() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableRescaleNormal();
    }

    public static void disableRescaleNormal() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableRescaleNormal();
    }

    public static void viewport(int param0, int param1, int param2, int param3) {
        assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._viewport(param0, param1, param2, param3);
    }

    public static void colorMask(boolean param0, boolean param1, boolean param2, boolean param3) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._colorMask(param0, param1, param2, param3);
    }

    public static void stencilFunc(int param0, int param1, int param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._stencilFunc(param0, param1, param2);
    }

    public static void stencilMask(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._stencilMask(param0);
    }

    public static void stencilOp(int param0, int param1, int param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._stencilOp(param0, param1, param2);
    }

    public static void clearDepth(double param0) {
        assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._clearDepth(param0);
    }

    public static void clearColor(float param0, float param1, float param2, float param3) {
        assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._clearColor(param0, param1, param2, param3);
    }

    public static void clearStencil(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._clearStencil(param0);
    }

    public static void clear(int param0, boolean param1) {
        assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._clear(param0, param1);
    }

    public static void matrixMode(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._matrixMode(param0);
    }

    public static void loadIdentity() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._loadIdentity();
    }

    public static void pushMatrix() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushMatrix();
    }

    public static void popMatrix() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._popMatrix();
    }

    public static void ortho(double param0, double param1, double param2, double param3, double param4, double param5) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._ortho(param0, param1, param2, param3, param4, param5);
    }

    public static void rotatef(float param0, float param1, float param2, float param3) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._rotatef(param0, param1, param2, param3);
    }

    public static void scalef(float param0, float param1, float param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._scalef(param0, param1, param2);
    }

    public static void scaled(double param0, double param1, double param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._scaled(param0, param1, param2);
    }

    public static void translatef(float param0, float param1, float param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._translatef(param0, param1, param2);
    }

    public static void translated(double param0, double param1, double param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._translated(param0, param1, param2);
    }

    public static void multMatrix(Matrix4f param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._multMatrix(param0);
    }

    public static void color4f(float param0, float param1, float param2, float param3) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._color4f(param0, param1, param2, param3);
    }

    public static void color3f(float param0, float param1, float param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._color4f(param0, param1, param2, 1.0F);
    }

    public static void clearCurrentColor() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._clearCurrentColor();
    }

    public static void drawArrays(int param0, int param1, int param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._drawArrays(param0, param1, param2);
    }

    public static void lineWidth(float param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._lineWidth(param0);
    }

    public static void pixelStore(int param0, int param1) {
        assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._pixelStore(param0, param1);
    }

    public static void pixelTransfer(int param0, float param1) {
        GlStateManager._pixelTransfer(param0, param1);
    }

    public static void readPixels(int param0, int param1, int param2, int param3, int param4, int param5, ByteBuffer param6) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._readPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static void getString(int param0, Consumer<String> param1) {
        assertThread(RenderSystem::isOnGameThread);
        param1.accept(GlStateManager._getString(param0));
    }

    public static String getBackendDescription() {
        assertThread(RenderSystem::isInInitPhase);
        return String.format("LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        assertThread(RenderSystem::isInInitPhase);
        return GLX.getOpenGLVersionString();
    }

    public static LongSupplier initBackendSystem() {
        assertThread(RenderSystem::isInInitPhase);
        return GLX._initGlfw();
    }

    public static void initRenderer(int param0, boolean param1) {
        assertThread(RenderSystem::isInInitPhase);
        GLX._init(param0, param1);
    }

    public static void setErrorCallback(GLFWErrorCallbackI param0) {
        assertThread(RenderSystem::isInInitPhase);
        GLX._setGlfwErrorCallback(param0);
    }

    public static void renderCrosshair(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GLX._renderCrosshair(param0, true, true, true);
    }

    public static void setupNvFogDistance() {
        assertThread(RenderSystem::isOnGameThread);
        GLX._setupNvFogDistance();
    }

    public static void glMultiTexCoord2f(int param0, float param1, float param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glMultiTexCoord2f(param0, param1, param2);
    }

    public static String getCapsString() {
        assertThread(RenderSystem::isOnGameThread);
        return GLX._getCapsString();
    }

    public static void setupDefaultState(int param0, int param1, int param2, int param3) {
        assertThread(RenderSystem::isInInitPhase);
        GlStateManager._enableTexture();
        GlStateManager._shadeModel(7425);
        GlStateManager._clearDepth(1.0);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(515);
        GlStateManager._enableAlphaTest();
        GlStateManager._alphaFunc(516, 0.1F);
        GlStateManager._cullFace(GlStateManager.CullFace.BACK.value);
        GlStateManager._matrixMode(5889);
        GlStateManager._loadIdentity();
        GlStateManager._matrixMode(5888);
        GlStateManager._viewport(param0, param1, param2, param3);
    }

    public static int maxSupportedTextureSize() {
        assertThread(RenderSystem::isInInitPhase);
        if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
            for(int var0 = 16384; var0 > 0; var0 >>= 1) {
                GlStateManager._texImage2D(32868, 0, 6408, var0, var0, 0, 6408, 5121, null);
                int var1 = GlStateManager._getTexLevelParameter(32868, 0, 4096);
                if (var1 != 0) {
                    MAX_SUPPORTED_TEXTURE_SIZE = var0;
                    return var0;
                }
            }

            MAX_SUPPORTED_TEXTURE_SIZE = Mth.clamp(GlStateManager._getInteger(3379), 1024, 16384);
            LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", MAX_SUPPORTED_TEXTURE_SIZE);
        }

        return MAX_SUPPORTED_TEXTURE_SIZE;
    }

    public static void glBindBuffer(int param0, Supplier<Integer> param1) {
        GlStateManager._glBindBuffer(param0, param1.get());
    }

    public static void glBufferData(int param0, ByteBuffer param1, int param2) {
        assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._glBufferData(param0, param1, param2);
    }

    public static void glDeleteBuffers(int param0) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glDeleteBuffers(param0);
    }

    public static void glUniform1i(int param0, int param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform1i(param0, param1);
    }

    public static void glUniform1(int param0, IntBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform1(param0, param1);
    }

    public static void glUniform2(int param0, IntBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform2(param0, param1);
    }

    public static void glUniform3(int param0, IntBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform3(param0, param1);
    }

    public static void glUniform4(int param0, IntBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform4(param0, param1);
    }

    public static void glUniform1(int param0, FloatBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform1(param0, param1);
    }

    public static void glUniform2(int param0, FloatBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform2(param0, param1);
    }

    public static void glUniform3(int param0, FloatBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform3(param0, param1);
    }

    public static void glUniform4(int param0, FloatBuffer param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform4(param0, param1);
    }

    public static void glUniformMatrix2(int param0, boolean param1, FloatBuffer param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniformMatrix2(param0, param1, param2);
    }

    public static void glUniformMatrix3(int param0, boolean param1, FloatBuffer param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniformMatrix3(param0, param1, param2);
    }

    public static void glUniformMatrix4(int param0, boolean param1, FloatBuffer param2) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniformMatrix4(param0, param1, param2);
    }

    public static void setupOverlayColor(IntSupplier param0, int param1) {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupOverlayColor(param0.getAsInt(), param1);
    }

    public static void teardownOverlayColor() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager.teardownOverlayColor();
    }

    public static void setupLevelDiffuseLighting() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupLevelDiffuseLighting();
    }

    public static void setupGuiDiffuseLighting() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupGuiDiffuseLighting();
    }

    public static void mulTextureByProjModelView() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager.mulTextureByProjModelView();
    }

    public static void setupEndPortalTexGen() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupEndPortalTexGen();
    }

    public static void clearTexGen() {
        assertThread(RenderSystem::isOnGameThread);
        GlStateManager.clearTexGen();
    }

    public static void beginInitialization() {
        isInInit = true;
    }

    public static void finishInitialization() {
        isInInit = false;
        if (!recordingQueue.isEmpty()) {
            replayQueue();
        }

        if (!recordingQueue.isEmpty()) {
            throw new IllegalStateException("Recorded to render queue during initialization");
        }
    }

    public static void glGenBuffers(Consumer<Integer> param0) {
        if (!isOnRenderThread()) {
            recordRenderCall(() -> param0.accept(GlStateManager._glGenBuffers()));
        } else {
            param0.accept(GlStateManager._glGenBuffers());
        }

    }

    public static Tesselator renderThreadTesselator() {
        assertThread(RenderSystem::isOnRenderThread);
        return RENDER_THREAD_TESSELATOR;
    }

    public static void defaultBlendFunc() {
        blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
    }

    public static void defaultAlphaFunc() {
        alphaFunc(516, 0.1F);
    }
}
