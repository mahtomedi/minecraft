package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFWErrorCallbackI;

@OnlyIn(Dist.CLIENT)
public class RenderSystem {
    private static final ConcurrentLinkedQueue<Runnable> recordingQueue = Queues.newConcurrentLinkedQueue();
    private static Thread clientThread;
    private static Thread renderThread;

    public static void initClientThread() {
        if (clientThread == null && renderThread != Thread.currentThread()) {
            clientThread = Thread.currentThread();
        } else {
            throw new IllegalStateException("Could not initialize tick thread");
        }
    }

    public static boolean isOnClientThread() {
        return clientThread == Thread.currentThread();
    }

    public static void initRenderThread() {
        if (renderThread == null && clientThread != Thread.currentThread()) {
            renderThread = Thread.currentThread();
        } else {
            throw new IllegalStateException("Could not initialize render thread");
        }
    }

    public static boolean isOnRenderThread() {
        return renderThread == Thread.currentThread();
    }

    public static void pushLightingAttributes() {
        GlStateManager._pushLightingAttributes();
    }

    public static void pushTextureAttributes() {
        GlStateManager._pushTextureAttributes();
    }

    public static void popAttributes() {
        GlStateManager._popAttributes();
    }

    public static void disableAlphaTest() {
        GlStateManager._disableAlphaTest();
    }

    public static void enableAlphaTest() {
        GlStateManager._enableAlphaTest();
    }

    public static void alphaFunc(int param0, float param1) {
        GlStateManager._alphaFunc(param0, param1);
    }

    public static void enableLighting() {
        GlStateManager._enableLighting();
    }

    public static void disableLighting() {
        GlStateManager._disableLighting();
    }

    public static void enableLight(int param0) {
        GlStateManager._enableLight(param0);
    }

    public static void disableLight(int param0) {
        GlStateManager._disableLight(param0);
    }

    public static void enableColorMaterial() {
        GlStateManager._enableColorMaterial();
    }

    public static void disableColorMaterial() {
        GlStateManager._disableColorMaterial();
    }

    public static void colorMaterial(int param0, int param1) {
        GlStateManager._colorMaterial(param0, param1);
    }

    public static void light(int param0, int param1, FloatBuffer param2) {
        GlStateManager._light(param0, param1, param2);
    }

    public static void lightModel(int param0, FloatBuffer param1) {
        GlStateManager._lightModel(param0, param1);
    }

    public static void normal3f(float param0, float param1, float param2) {
        GlStateManager._normal3f(param0, param1, param2);
    }

    public static void disableDepthTest() {
        GlStateManager._disableDepthTest();
    }

    public static void enableDepthTest() {
        GlStateManager._enableDepthTest();
    }

    public static void depthFunc(int param0) {
        GlStateManager._depthFunc(param0);
    }

    public static void depthMask(boolean param0) {
        GlStateManager._depthMask(param0);
    }

    public static void enableBlend() {
        GlStateManager._enableBlend();
    }

    public static void disableBlend() {
        GlStateManager._disableBlend();
    }

    public static void blendFunc(GlStateManager.SourceFactor param0, GlStateManager.DestFactor param1) {
        GlStateManager._blendFunc(param0.value, param1.value);
    }

    public static void blendFunc(int param0, int param1) {
        GlStateManager._blendFunc(param0, param1);
    }

    public static void blendFuncSeparate(
        GlStateManager.SourceFactor param0, GlStateManager.DestFactor param1, GlStateManager.SourceFactor param2, GlStateManager.DestFactor param3
    ) {
        GlStateManager._blendFuncSeparate(param0.value, param1.value, param2.value, param3.value);
    }

    public static void blendFuncSeparate(int param0, int param1, int param2, int param3) {
        GlStateManager._blendFuncSeparate(param0, param1, param2, param3);
    }

    public static void blendEquation(int param0) {
        GlStateManager._blendEquation(param0);
    }

    public static void setupSolidRenderingTextureCombine(int param0) {
        GlStateManager._setupSolidRenderingTextureCombine(param0);
    }

    public static void tearDownSolidRenderingTextureCombine() {
        GlStateManager._tearDownSolidRenderingTextureCombine();
    }

    public static void enableFog() {
        GlStateManager._enableFog();
    }

    public static void disableFog() {
        GlStateManager._disableFog();
    }

    public static void fogMode(GlStateManager.FogMode param0) {
        GlStateManager._fogMode(param0.value);
    }

    public static void fogMode(int param0) {
        GlStateManager._fogMode(param0);
    }

    public static void fogDensity(float param0) {
        GlStateManager._fogDensity(param0);
    }

    public static void fogStart(float param0) {
        GlStateManager._fogStart(param0);
    }

    public static void fogEnd(float param0) {
        GlStateManager._fogEnd(param0);
    }

    public static void fog(int param0, FloatBuffer param1) {
        GlStateManager._fog(param0, param1);
    }

    public static void fogi(int param0, int param1) {
        GlStateManager._fogi(param0, param1);
    }

    public static void enableCull() {
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        GlStateManager._disableCull();
    }

    public static void cullFace(GlStateManager.CullFace param0) {
        GlStateManager._cullFace(param0.value);
    }

    public static void cullFace(int param0) {
        GlStateManager._cullFace(param0);
    }

    public static void polygonMode(int param0, int param1) {
        GlStateManager._polygonMode(param0, param1);
    }

    public static void enablePolygonOffset() {
        GlStateManager._enablePolygonOffset();
    }

    public static void disablePolygonOffset() {
        GlStateManager._disablePolygonOffset();
    }

    public static void enableLineOffset() {
        GlStateManager._enableLineOffset();
    }

    public static void disableLineOffset() {
        GlStateManager._disableLineOffset();
    }

    public static void polygonOffset(float param0, float param1) {
        GlStateManager._polygonOffset(param0, param1);
    }

    public static void enableColorLogicOp() {
        GlStateManager._enableColorLogicOp();
    }

    public static void disableColorLogicOp() {
        GlStateManager._disableColorLogicOp();
    }

    public static void logicOp(GlStateManager.LogicOp param0) {
        GlStateManager._logicOp(param0.value);
    }

    public static void logicOp(int param0) {
        GlStateManager._logicOp(param0);
    }

    public static void enableTexGen(GlStateManager.TexGen param0) {
        GlStateManager._enableTexGen(param0);
    }

    public static void disableTexGen(GlStateManager.TexGen param0) {
        GlStateManager._disableTexGen(param0);
    }

    public static void texGenMode(GlStateManager.TexGen param0, int param1) {
        GlStateManager._texGenMode(param0, param1);
    }

    public static void texGenParam(GlStateManager.TexGen param0, int param1, FloatBuffer param2) {
        GlStateManager._texGenParam(param0, param1, param2);
    }

    public static void activeTexture(int param0) {
        GlStateManager._activeTexture(param0);
    }

    public static void enableTexture() {
        GlStateManager._enableTexture();
    }

    public static void disableTexture() {
        GlStateManager._disableTexture();
    }

    public static void texEnv(int param0, int param1, FloatBuffer param2) {
        GlStateManager._texEnv(param0, param1, param2);
    }

    public static void texEnv(int param0, int param1, int param2) {
        GlStateManager._texEnv(param0, param1, param2);
    }

    public static void texEnv(int param0, int param1, float param2) {
        GlStateManager._texEnv(param0, param1, param2);
    }

    public static void texParameter(int param0, int param1, float param2) {
        GlStateManager._texParameter(param0, param1, param2);
    }

    public static void texParameter(int param0, int param1, int param2) {
        GlStateManager._texParameter(param0, param1, param2);
    }

    public static int getTexLevelParameter(int param0, int param1, int param2) {
        return GlStateManager._getTexLevelParameter(param0, param1, param2);
    }

    public static int genTexture() {
        return GlStateManager._genTexture();
    }

    public static void deleteTexture(int param0) {
        GlStateManager._deleteTexture(param0);
    }

    public static void bindTexture(int param0) {
        GlStateManager._bindTexture(param0);
    }

    public static void texImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, @Nullable IntBuffer param8) {
        GlStateManager._texImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void texSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, long param8) {
        GlStateManager._texSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void copyTexSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        GlStateManager._copyTexSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void getTexImage(int param0, int param1, int param2, int param3, long param4) {
        GlStateManager._getTexImage(param0, param1, param2, param3, param4);
    }

    public static void enableNormalize() {
        GlStateManager._enableNormalize();
    }

    public static void disableNormalize() {
        GlStateManager._disableNormalize();
    }

    public static void shadeModel(int param0) {
        GlStateManager._shadeModel(param0);
    }

    public static void enableRescaleNormal() {
        GlStateManager._enableRescaleNormal();
    }

    public static void disableRescaleNormal() {
        GlStateManager._disableRescaleNormal();
    }

    public static void viewport(int param0, int param1, int param2, int param3) {
        GlStateManager._viewport(param0, param1, param2, param3);
    }

    public static void colorMask(boolean param0, boolean param1, boolean param2, boolean param3) {
        GlStateManager._colorMask(param0, param1, param2, param3);
    }

    public static void stencilFunc(int param0, int param1, int param2) {
        GlStateManager._stencilFunc(param0, param1, param2);
    }

    public static void stencilMask(int param0) {
        GlStateManager._stencilMask(param0);
    }

    public static void stencilOp(int param0, int param1, int param2) {
        GlStateManager._stencilOp(param0, param1, param2);
    }

    public static void clearDepth(double param0) {
        GlStateManager._clearDepth(param0);
    }

    public static void clearColor(float param0, float param1, float param2, float param3) {
        GlStateManager._clearColor(param0, param1, param2, param3);
    }

    public static void clearStencil(int param0) {
        GlStateManager._clearStencil(param0);
    }

    public static void clear(int param0, boolean param1) {
        GlStateManager._clear(param0, param1);
    }

    public static void matrixMode(int param0) {
        GlStateManager._matrixMode(param0);
    }

    public static void loadIdentity() {
        GlStateManager._loadIdentity();
    }

    public static void pushMatrix() {
        GlStateManager._pushMatrix();
    }

    public static void popMatrix() {
        GlStateManager._popMatrix();
    }

    public static void getMatrix(int param0, FloatBuffer param1) {
        GlStateManager._getMatrix(param0, param1);
    }

    public static Matrix4f getMatrix4f(int param0) {
        return GlStateManager._getMatrix4f(param0);
    }

    public static void ortho(double param0, double param1, double param2, double param3, double param4, double param5) {
        GlStateManager._ortho(param0, param1, param2, param3, param4, param5);
    }

    public static void rotatef(float param0, float param1, float param2, float param3) {
        GlStateManager._rotatef(param0, param1, param2, param3);
    }

    public static void rotated(double param0, double param1, double param2, double param3) {
        GlStateManager._rotated(param0, param1, param2, param3);
    }

    public static void scalef(float param0, float param1, float param2) {
        GlStateManager._scalef(param0, param1, param2);
    }

    public static void scaled(double param0, double param1, double param2) {
        GlStateManager._scaled(param0, param1, param2);
    }

    public static void translatef(float param0, float param1, float param2) {
        GlStateManager._translatef(param0, param1, param2);
    }

    public static void translated(double param0, double param1, double param2) {
        GlStateManager._translated(param0, param1, param2);
    }

    public static void multMatrix(FloatBuffer param0) {
        GlStateManager._multMatrix(param0);
    }

    public static void multMatrix(Matrix4f param0) {
        GlStateManager._multMatrix(param0);
    }

    public static void color4f(float param0, float param1, float param2, float param3) {
        GlStateManager._color4f(param0, param1, param2, param3);
    }

    public static void color3f(float param0, float param1, float param2) {
        GlStateManager._color4f(param0, param1, param2, 1.0F);
    }

    public static void texCoord2f(float param0, float param1) {
        GlStateManager._texCoord2f(param0, param1);
    }

    public static void vertex3f(float param0, float param1, float param2) {
        GlStateManager._vertex3f(param0, param1, param2);
    }

    public static void clearCurrentColor() {
        GlStateManager._clearCurrentColor();
    }

    public static void normalPointer(int param0, int param1, int param2) {
        GlStateManager._normalPointer(param0, param1, param2);
    }

    public static void normalPointer(int param0, int param1, ByteBuffer param2) {
        GlStateManager._normalPointer(param0, param1, param2);
    }

    public static void texCoordPointer(int param0, int param1, int param2, int param3) {
        GlStateManager._texCoordPointer(param0, param1, param2, param3);
    }

    public static void texCoordPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GlStateManager._texCoordPointer(param0, param1, param2, param3);
    }

    public static void vertexPointer(int param0, int param1, int param2, int param3) {
        GlStateManager._vertexPointer(param0, param1, param2, param3);
    }

    public static void vertexPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GlStateManager._vertexPointer(param0, param1, param2, param3);
    }

    public static void colorPointer(int param0, int param1, int param2, int param3) {
        GlStateManager._colorPointer(param0, param1, param2, param3);
    }

    public static void colorPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GlStateManager._colorPointer(param0, param1, param2, param3);
    }

    public static void disableClientState(int param0) {
        GlStateManager._disableClientState(param0);
    }

    public static void enableClientState(int param0) {
        GlStateManager._enableClientState(param0);
    }

    public static void begin(int param0) {
        GlStateManager._begin(param0);
    }

    public static void end() {
        GlStateManager._end();
    }

    public static void drawArrays(int param0, int param1, int param2) {
        GlStateManager._drawArrays(param0, param1, param2);
    }

    public static void lineWidth(float param0) {
        GlStateManager._lineWidth(param0);
    }

    public static void callList(int param0) {
        GlStateManager._callList(param0);
    }

    public static void deleteLists(int param0, int param1) {
        GlStateManager._deleteLists(param0, param1);
    }

    public static void newList(int param0, int param1) {
        GlStateManager._newList(param0, param1);
    }

    public static void endList() {
        GlStateManager._endList();
    }

    public static int genLists(int param0) {
        return GlStateManager._genLists(param0);
    }

    public static void pixelStore(int param0, int param1) {
        GlStateManager._pixelStore(param0, param1);
    }

    public static void pixelTransfer(int param0, float param1) {
        GlStateManager._pixelTransfer(param0, param1);
    }

    public static void readPixels(int param0, int param1, int param2, int param3, int param4, int param5, ByteBuffer param6) {
        GlStateManager._readPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static void readPixels(int param0, int param1, int param2, int param3, int param4, int param5, long param6) {
        GlStateManager._readPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static int getError() {
        return GlStateManager._getError();
    }

    public static String getString(int param0) {
        return GlStateManager._getString(param0);
    }

    public static void getInteger(int param0, IntBuffer param1) {
        GlStateManager._getInteger(param0, param1);
    }

    public static int getInteger(int param0) {
        return GlStateManager._getInteger(param0);
    }

    public static String getBackendDescription() {
        return String.format("LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        return GLX.getOpenGLVersionString();
    }

    public static LongSupplier initBackendSystem() {
        return GLX._initGlfw();
    }

    public static void initRenderer(int param0, boolean param1) {
        GLX._init(param0, param1);
    }

    public static void setErrorCallback(GLFWErrorCallbackI param0) {
        GLX._setGlfwErrorCallback(param0);
    }

    public static void pollEvents() {
        GLX._pollEvents();
    }

    public static void glClientActiveTexture(int param0) {
        GlStateManager._glClientActiveTexture(param0);
    }

    public static void renderCrosshair(int param0) {
        GLX._renderCrosshair(param0, true, true, true);
    }

    public static void setupNvFogDistance() {
        GLX._setupNvFogDistance();
    }

    public static void glMultiTexCoord2f(int param0, float param1, float param2) {
        GlStateManager._glMultiTexCoord2f(param0, param1, param2);
    }

    public static String getCapsString() {
        return GLX._getCapsString();
    }
}
