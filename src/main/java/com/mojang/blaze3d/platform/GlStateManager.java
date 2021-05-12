package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class GlStateManager {
    public static final int TEXTURE_COUNT = 12;
    private static final GlStateManager.BlendState BLEND = new GlStateManager.BlendState();
    private static final GlStateManager.DepthState DEPTH = new GlStateManager.DepthState();
    private static final GlStateManager.CullState CULL = new GlStateManager.CullState();
    private static final GlStateManager.PolygonOffsetState POLY_OFFSET = new GlStateManager.PolygonOffsetState();
    private static final GlStateManager.ColorLogicState COLOR_LOGIC = new GlStateManager.ColorLogicState();
    private static final GlStateManager.StencilState STENCIL = new GlStateManager.StencilState();
    private static final GlStateManager.ScissorState SCISSOR = new GlStateManager.ScissorState();
    private static int activeTexture;
    private static final GlStateManager.TextureState[] TEXTURES = IntStream.range(0, 12)
        .mapToObj(param0 -> new GlStateManager.TextureState())
        .toArray(param0 -> new GlStateManager.TextureState[param0]);
    private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();

    public static void _disableScissorTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        SCISSOR.mode.disable();
    }

    public static void _enableScissorTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        SCISSOR.mode.enable();
    }

    public static void _scissorBox(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL20.glScissor(param0, param1, param2, param3);
    }

    public static void _disableDepthTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        DEPTH.mode.disable();
    }

    public static void _enableDepthTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        DEPTH.mode.enable();
    }

    public static void _depthFunc(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (param0 != DEPTH.func) {
            DEPTH.func = param0;
            GL11.glDepthFunc(param0);
        }

    }

    public static void _depthMask(boolean param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != DEPTH.mask) {
            DEPTH.mask = param0;
            GL11.glDepthMask(param0);
        }

    }

    public static void _disableBlend() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        BLEND.mode.disable();
    }

    public static void _enableBlend() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        BLEND.mode.enable();
    }

    public static void _blendFunc(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != BLEND.srcRgb || param1 != BLEND.dstRgb) {
            BLEND.srcRgb = param0;
            BLEND.dstRgb = param1;
            GL11.glBlendFunc(param0, param1);
        }

    }

    public static void _blendFuncSeparate(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != BLEND.srcRgb || param1 != BLEND.dstRgb || param2 != BLEND.srcAlpha || param3 != BLEND.dstAlpha) {
            BLEND.srcRgb = param0;
            BLEND.dstRgb = param1;
            BLEND.srcAlpha = param2;
            BLEND.dstAlpha = param3;
            glBlendFuncSeparate(param0, param1, param2, param3);
        }

    }

    public static void _blendEquation(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL14.glBlendEquation(param0);
    }

    public static int glGetProgrami(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetProgrami(param0, param1);
    }

    public static void glAttachShader(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glAttachShader(param0, param1);
    }

    public static void glDeleteShader(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glDeleteShader(param0);
    }

    public static int glCreateShader(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glCreateShader(param0);
    }

    public static void glShaderSource(int param0, List<String> param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glShaderSource(param0, param1.toArray(new CharSequence[0]));
    }

    public static void glCompileShader(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glCompileShader(param0);
    }

    public static int glGetShaderi(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetShaderi(param0, param1);
    }

    public static void _glUseProgram(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUseProgram(param0);
    }

    public static int glCreateProgram() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glDeleteProgram(param0);
    }

    public static void glLinkProgram(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glLinkProgram(param0);
    }

    public static int _glGetUniformLocation(int param0, CharSequence param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetUniformLocation(param0, param1);
    }

    public static void _glUniform1(int param0, IntBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform1iv(param0, param1);
    }

    public static void _glUniform1i(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform1i(param0, param1);
    }

    public static void _glUniform1(int param0, FloatBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform1fv(param0, param1);
    }

    public static void _glUniform2(int param0, IntBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform2iv(param0, param1);
    }

    public static void _glUniform2(int param0, FloatBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform2fv(param0, param1);
    }

    public static void _glUniform3(int param0, IntBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform3iv(param0, param1);
    }

    public static void _glUniform3(int param0, FloatBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform3fv(param0, param1);
    }

    public static void _glUniform4(int param0, IntBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform4iv(param0, param1);
    }

    public static void _glUniform4(int param0, FloatBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform4fv(param0, param1);
    }

    public static void _glUniformMatrix2(int param0, boolean param1, FloatBuffer param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniformMatrix2fv(param0, param1, param2);
    }

    public static void _glUniformMatrix3(int param0, boolean param1, FloatBuffer param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniformMatrix3fv(param0, param1, param2);
    }

    public static void _glUniformMatrix4(int param0, boolean param1, FloatBuffer param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniformMatrix4fv(param0, param1, param2);
    }

    public static int _glGetAttribLocation(int param0, CharSequence param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetAttribLocation(param0, param1);
    }

    public static void _glBindAttribLocation(int param0, int param1, CharSequence param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glBindAttribLocation(param0, param1, param2);
    }

    public static int _glGenBuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL15.glGenBuffers();
    }

    public static int _glGenVertexArrays() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glGenVertexArrays();
    }

    public static void _glBindBuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBindBuffer(param0, param1);
    }

    public static void _glBindVertexArray(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBindVertexArray(param0);
    }

    public static void _glBufferData(int param0, ByteBuffer param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBufferData(param0, param1, param2);
    }

    public static void _glBufferData(int param0, long param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBufferData(param0, param1, param2);
    }

    @Nullable
    public static ByteBuffer _glMapBuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL15.glMapBuffer(param0, param1);
    }

    public static void _glUnmapBuffer(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glUnmapBuffer(param0);
    }

    public static void _glDeleteBuffers(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL15.glDeleteBuffers(param0);
    }

    public static void _glCopyTexSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL20.glCopyTexSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void _glDeleteVertexArrays(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL30.glDeleteVertexArrays(param0);
    }

    public static void _glBindFramebuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBindFramebuffer(param0, param1);
    }

    public static void _glBlitFrameBuffer(
        int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9
    ) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBlitFramebuffer(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void _glBindRenderbuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBindRenderbuffer(param0, param1);
    }

    public static void _glDeleteRenderbuffers(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glDeleteRenderbuffers(param0);
    }

    public static void _glDeleteFramebuffers(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glDeleteFramebuffers(param0);
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glGenFramebuffers();
    }

    public static int glGenRenderbuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glGenRenderbuffers();
    }

    public static void _glRenderbufferStorage(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glRenderbufferStorage(param0, param1, param2, param3);
    }

    public static void _glFramebufferRenderbuffer(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glFramebufferRenderbuffer(param0, param1, param2, param3);
    }

    public static int glCheckFramebufferStatus(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glCheckFramebufferStatus(param0);
    }

    public static void _glFramebufferTexture2D(int param0, int param1, int param2, int param3, int param4) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glFramebufferTexture2D(param0, param1, param2, param3, param4);
    }

    public static int getBoundFramebuffer() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return _getInteger(36006);
    }

    public static void glActiveTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glActiveTexture(param0);
    }

    public static void glBlendFuncSeparate(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL14.glBlendFuncSeparate(param0, param1, param2, param3);
    }

    public static String glGetShaderInfoLog(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetShaderInfoLog(param0, param1);
    }

    public static String glGetProgramInfoLog(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetProgramInfoLog(param0, param1);
    }

    public static void setupLevelDiffuseLighting(Vector3f param0, Vector3f param1, Matrix4f param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Vector4f var0 = new Vector4f(param0);
        var0.transform(param2);
        Vector4f var1 = new Vector4f(param1);
        var1.transform(param2);
        RenderSystem.setShaderLights(new Vector3f(var0), new Vector3f(var1));
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f param0, Vector3f param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        var0.multiply(Matrix4f.createScaleMatrix(1.0F, -1.0F, 1.0F));
        var0.multiply(Vector3f.YP.rotationDegrees(-22.5F));
        var0.multiply(Vector3f.XP.rotationDegrees(135.0F));
        setupLevelDiffuseLighting(param0, param1, var0);
    }

    public static void setupGui3DDiffuseLighting(Vector3f param0, Vector3f param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        var0.multiply(Vector3f.YP.rotationDegrees(62.0F));
        var0.multiply(Vector3f.XP.rotationDegrees(185.5F));
        var0.multiply(Vector3f.YP.rotationDegrees(-22.5F));
        var0.multiply(Vector3f.XP.rotationDegrees(135.0F));
        setupLevelDiffuseLighting(param0, param1, var0);
    }

    public static void _enableCull() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        CULL.enable.enable();
    }

    public static void _disableCull() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        CULL.enable.disable();
    }

    public static void _polygonMode(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPolygonMode(param0, param1);
    }

    public static void _enablePolygonOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        POLY_OFFSET.fill.enable();
    }

    public static void _disablePolygonOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        POLY_OFFSET.fill.disable();
    }

    public static void _polygonOffset(float param0, float param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != POLY_OFFSET.factor || param1 != POLY_OFFSET.units) {
            POLY_OFFSET.factor = param0;
            POLY_OFFSET.units = param1;
            GL11.glPolygonOffset(param0, param1);
        }

    }

    public static void _enableColorLogicOp() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        COLOR_LOGIC.enable.enable();
    }

    public static void _disableColorLogicOp() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        COLOR_LOGIC.enable.disable();
    }

    public static void _logicOp(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != COLOR_LOGIC.op) {
            COLOR_LOGIC.op = param0;
            GL11.glLogicOp(param0);
        }

    }

    public static void _activeTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (activeTexture != param0 - 33984) {
            activeTexture = param0 - 33984;
            glActiveTexture(param0);
        }

    }

    public static void _enableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        TEXTURES[activeTexture].enable = true;
    }

    public static void _disableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        TEXTURES[activeTexture].enable = false;
    }

    public static void _texParameter(int param0, int param1, float param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexParameterf(param0, param1, param2);
    }

    public static void _texParameter(int param0, int param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexParameteri(param0, param1, param2);
    }

    public static int _getTexLevelParameter(int param0, int param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return GL11.glGetTexLevelParameteri(param0, param1, param2);
    }

    public static int _genTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL11.glGenTextures();
    }

    public static void _genTextures(int[] param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glGenTextures(param0);
    }

    public static void _deleteTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glDeleteTextures(param0);

        for(GlStateManager.TextureState var0 : TEXTURES) {
            if (var0.binding == param0) {
                var0.binding = -1;
            }
        }

    }

    public static void _deleteTextures(int[] param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);

        for(GlStateManager.TextureState var0 : TEXTURES) {
            for(int var1 : param0) {
                if (var0.binding == var1) {
                    var0.binding = -1;
                }
            }
        }

        GL11.glDeleteTextures(param0);
    }

    public static void _bindTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (param0 != TEXTURES[activeTexture].binding) {
            TEXTURES[activeTexture].binding = param0;
            GL11.glBindTexture(3553, param0);
        }

    }

    public static int _getTextureId(int param0) {
        return param0 >= 0 && param0 < 12 && TEXTURES[param0].enable ? TEXTURES[param0].binding : 0;
    }

    public static int _getActiveTexture() {
        return activeTexture + 33984;
    }

    public static void _texImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, @Nullable IntBuffer param8) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void _texSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, long param8) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void _getTexImage(int param0, int param1, int param2, int param3, long param4) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glGetTexImage(param0, param1, param2, param3, param4);
    }

    public static void _viewport(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager.Viewport.INSTANCE.x = param0;
        GlStateManager.Viewport.INSTANCE.y = param1;
        GlStateManager.Viewport.INSTANCE.width = param2;
        GlStateManager.Viewport.INSTANCE.height = param3;
        GL11.glViewport(param0, param1, param2, param3);
    }

    public static void _colorMask(boolean param0, boolean param1, boolean param2, boolean param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != COLOR_MASK.red || param1 != COLOR_MASK.green || param2 != COLOR_MASK.blue || param3 != COLOR_MASK.alpha) {
            COLOR_MASK.red = param0;
            COLOR_MASK.green = param1;
            COLOR_MASK.blue = param2;
            COLOR_MASK.alpha = param3;
            GL11.glColorMask(param0, param1, param2, param3);
        }

    }

    public static void _stencilFunc(int param0, int param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != STENCIL.func.func || param0 != STENCIL.func.ref || param0 != STENCIL.func.mask) {
            STENCIL.func.func = param0;
            STENCIL.func.ref = param1;
            STENCIL.func.mask = param2;
            GL11.glStencilFunc(param0, param1, param2);
        }

    }

    public static void _stencilMask(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != STENCIL.mask) {
            STENCIL.mask = param0;
            GL11.glStencilMask(param0);
        }

    }

    public static void _stencilOp(int param0, int param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != STENCIL.fail || param1 != STENCIL.zfail || param2 != STENCIL.zpass) {
            STENCIL.fail = param0;
            STENCIL.zfail = param1;
            STENCIL.zpass = param2;
            GL11.glStencilOp(param0, param1, param2);
        }

    }

    public static void _clearDepth(double param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClearDepth(param0);
    }

    public static void _clearColor(float param0, float param1, float param2, float param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClearColor(param0, param1, param2, param3);
    }

    public static void _clearStencil(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glClearStencil(param0);
    }

    public static void _clear(int param0, boolean param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClear(param0);
        if (param1) {
            _getError();
        }

    }

    public static void _glDrawPixels(int param0, int param1, int param2, int param3, long param4) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDrawPixels(param0, param1, param2, param3, param4);
    }

    public static void _vertexAttribPointer(int param0, int param1, int param2, boolean param3, int param4, long param5) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glVertexAttribPointer(param0, param1, param2, param3, param4, param5);
    }

    public static void _vertexAttribIPointer(int param0, int param1, int param2, int param3, long param4) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL30.glVertexAttribIPointer(param0, param1, param2, param3, param4);
    }

    public static void _enableVertexAttribArray(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glEnableVertexAttribArray(param0);
    }

    public static void _disableVertexAttribArray(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glDisableVertexAttribArray(param0);
    }

    public static void _drawElements(int param0, int param1, int param2, long param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDrawElements(param0, param1, param2, param3);
    }

    public static void _pixelStore(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glPixelStorei(param0, param1);
    }

    public static void _readPixels(int param0, int param1, int param2, int param3, int param4, int param5, ByteBuffer param6) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glReadPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static void _readPixels(int param0, int param1, int param2, int param3, int param4, int param5, long param6) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glReadPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static int _getError() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL11.glGetError();
    }

    public static String _getString(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL11.glGetString(param0);
    }

    public static int _getInteger(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL11.glGetInteger(param0);
    }

    @OnlyIn(Dist.CLIENT)
    static class BlendState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3042);
        public int srcRgb = 1;
        public int dstRgb = 0;
        public int srcAlpha = 1;
        public int dstAlpha = 0;
    }

    @OnlyIn(Dist.CLIENT)
    static class BooleanState {
        private final int state;
        private boolean enabled;

        public BooleanState(int param0) {
            this.state = param0;
        }

        public void disable() {
            this.setEnabled(false);
        }

        public void enable() {
            this.setEnabled(true);
        }

        public void setEnabled(boolean param0) {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            if (param0 != this.enabled) {
                this.enabled = param0;
                if (param0) {
                    GL11.glEnable(this.state);
                } else {
                    GL11.glDisable(this.state);
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ColorLogicState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3058);
        public int op = 5379;
    }

    @OnlyIn(Dist.CLIENT)
    static class ColorMask {
        public boolean red = true;
        public boolean green = true;
        public boolean blue = true;
        public boolean alpha = true;
    }

    @OnlyIn(Dist.CLIENT)
    static class CullState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2884);
        public int mode = 1029;
    }

    @OnlyIn(Dist.CLIENT)
    static class DepthState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(2929);
        public boolean mask = true;
        public int func = 513;
    }

    @OnlyIn(Dist.CLIENT)
    @DontObfuscate
    public static enum DestFactor {
        CONSTANT_ALPHA(32771),
        CONSTANT_COLOR(32769),
        DST_ALPHA(772),
        DST_COLOR(774),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_CONSTANT_COLOR(32770),
        ONE_MINUS_DST_ALPHA(773),
        ONE_MINUS_DST_COLOR(775),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_SRC_COLOR(769),
        SRC_ALPHA(770),
        SRC_COLOR(768),
        ZERO(0);

        public final int value;

        private DestFactor(int param0) {
            this.value = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum LogicOp {
        AND(5377),
        AND_INVERTED(5380),
        AND_REVERSE(5378),
        CLEAR(5376),
        COPY(5379),
        COPY_INVERTED(5388),
        EQUIV(5385),
        INVERT(5386),
        NAND(5390),
        NOOP(5381),
        NOR(5384),
        OR(5383),
        OR_INVERTED(5389),
        OR_REVERSE(5387),
        SET(5391),
        XOR(5382);

        public final int value;

        private LogicOp(int param0) {
            this.value = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class PolygonOffsetState {
        public final GlStateManager.BooleanState fill = new GlStateManager.BooleanState(32823);
        public final GlStateManager.BooleanState line = new GlStateManager.BooleanState(10754);
        public float factor;
        public float units;
    }

    @OnlyIn(Dist.CLIENT)
    static class ScissorState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3089);
    }

    @OnlyIn(Dist.CLIENT)
    @DontObfuscate
    public static enum SourceFactor {
        CONSTANT_ALPHA(32771),
        CONSTANT_COLOR(32769),
        DST_ALPHA(772),
        DST_COLOR(774),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_CONSTANT_COLOR(32770),
        ONE_MINUS_DST_ALPHA(773),
        ONE_MINUS_DST_COLOR(775),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_SRC_COLOR(769),
        SRC_ALPHA(770),
        SRC_ALPHA_SATURATE(776),
        SRC_COLOR(768),
        ZERO(0);

        public final int value;

        private SourceFactor(int param0) {
            this.value = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class StencilFunc {
        public int func = 519;
        public int ref;
        public int mask = -1;
    }

    @OnlyIn(Dist.CLIENT)
    static class StencilState {
        public final GlStateManager.StencilFunc func = new GlStateManager.StencilFunc();
        public int mask = -1;
        public int fail = 7680;
        public int zfail = 7680;
        public int zpass = 7680;
    }

    @OnlyIn(Dist.CLIENT)
    static class TextureState {
        public boolean enable;
        public int binding;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Viewport {
        INSTANCE;

        protected int x;
        protected int y;
        protected int width;
        protected int height;

        public static int x() {
            return INSTANCE.x;
        }

        public static int y() {
            return INSTANCE.y;
        }

        public static int width() {
            return INSTANCE.width;
        }

        public static int height() {
            return INSTANCE.height;
        }
    }
}
