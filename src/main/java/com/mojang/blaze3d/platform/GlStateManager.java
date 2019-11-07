package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class GlStateManager {
    private static final FloatBuffer MATRIX_BUFFER = GLX.make(
        MemoryUtil.memAllocFloat(16), param0 -> DebugMemoryUntracker.untrack(MemoryUtil.memAddress(param0))
    );
    private static final GlStateManager.AlphaState ALPHA_TEST = new GlStateManager.AlphaState();
    private static final GlStateManager.BooleanState LIGHTING = new GlStateManager.BooleanState(2896);
    private static final GlStateManager.BooleanState[] LIGHT_ENABLE = IntStream.range(0, 8)
        .mapToObj(param0 -> new GlStateManager.BooleanState(16384 + param0))
        .toArray(param0 -> new GlStateManager.BooleanState[param0]);
    private static final GlStateManager.ColorMaterialState COLOR_MATERIAL = new GlStateManager.ColorMaterialState();
    private static final GlStateManager.BlendState BLEND = new GlStateManager.BlendState();
    private static final GlStateManager.DepthState DEPTH = new GlStateManager.DepthState();
    private static final GlStateManager.FogState FOG = new GlStateManager.FogState();
    private static final GlStateManager.CullState CULL = new GlStateManager.CullState();
    private static final GlStateManager.PolygonOffsetState POLY_OFFSET = new GlStateManager.PolygonOffsetState();
    private static final GlStateManager.ColorLogicState COLOR_LOGIC = new GlStateManager.ColorLogicState();
    private static final GlStateManager.TexGenState TEX_GEN = new GlStateManager.TexGenState();
    private static final GlStateManager.ClearState CLEAR = new GlStateManager.ClearState();
    private static final GlStateManager.StencilState STENCIL = new GlStateManager.StencilState();
    private static final FloatBuffer FLOAT_ARG_BUFFER = MemoryTracker.createFloatBuffer(4);
    private static final Vector3f DIFFUSE_LIGHT_0 = Util.make(new Vector3f(0.2F, 1.0F, -0.7F), Vector3f::normalize);
    private static final Vector3f DIFFUSE_LIGHT_1 = Util.make(new Vector3f(-0.2F, 1.0F, 0.7F), Vector3f::normalize);
    private static int activeTexture;
    private static final GlStateManager.TextureState[] TEXTURES = IntStream.range(0, 8)
        .mapToObj(param0 -> new GlStateManager.TextureState())
        .toArray(param0 -> new GlStateManager.TextureState[param0]);
    private static int shadeModel = 7425;
    private static final GlStateManager.BooleanState RESCALE_NORMAL = new GlStateManager.BooleanState(32826);
    private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();
    private static final GlStateManager.Color COLOR = new GlStateManager.Color();
    private static GlStateManager.FboMode fboMode;

    @Deprecated
    public static void _pushLightingAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPushAttrib(8256);
    }

    @Deprecated
    public static void _pushTextureAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPushAttrib(270336);
    }

    @Deprecated
    public static void _popAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPopAttrib();
    }

    @Deprecated
    public static void _disableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ALPHA_TEST.mode.disable();
    }

    @Deprecated
    public static void _enableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        ALPHA_TEST.mode.enable();
    }

    @Deprecated
    public static void _alphaFunc(int param0, float param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (param0 != ALPHA_TEST.func || param1 != ALPHA_TEST.reference) {
            ALPHA_TEST.func = param0;
            ALPHA_TEST.reference = param1;
            GL11.glAlphaFunc(param0, param1);
        }

    }

    @Deprecated
    public static void _enableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        LIGHTING.enable();
    }

    @Deprecated
    public static void _disableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        LIGHTING.disable();
    }

    @Deprecated
    public static void _enableLight(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        LIGHT_ENABLE[param0].enable();
    }

    @Deprecated
    public static void _enableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        COLOR_MATERIAL.enable.enable();
    }

    @Deprecated
    public static void _disableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        COLOR_MATERIAL.enable.disable();
    }

    @Deprecated
    public static void _colorMaterial(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != COLOR_MATERIAL.face || param1 != COLOR_MATERIAL.mode) {
            COLOR_MATERIAL.face = param0;
            COLOR_MATERIAL.mode = param1;
            GL11.glColorMaterial(param0, param1);
        }

    }

    @Deprecated
    public static void _light(int param0, int param1, FloatBuffer param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glLightfv(param0, param1, param2);
    }

    @Deprecated
    public static void _lightModel(int param0, FloatBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glLightModelfv(param0, param1);
    }

    @Deprecated
    public static void _normal3f(float param0, float param1, float param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glNormal3f(param0, param1, param2);
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

    public static void _blendColor(float param0, float param1, float param2, float param3) {
        GL14.glBlendColor(param0, param1, param2, param3);
    }

    public static void _blendEquation(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL14.glBlendEquation(param0);
    }

    public static String _init_fbo(GLCapabilities param0) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        if (param0.OpenGL30) {
            fboMode = GlStateManager.FboMode.BASE;
            GlConst.GL_FRAMEBUFFER = 36160;
            GlConst.GL_RENDERBUFFER = 36161;
            GlConst.GL_COLOR_ATTACHMENT0 = 36064;
            GlConst.GL_DEPTH_ATTACHMENT = 36096;
            GlConst.GL_FRAMEBUFFER_COMPLETE = 36053;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
            return "OpenGL 3.0";
        } else if (param0.GL_ARB_framebuffer_object) {
            fboMode = GlStateManager.FboMode.ARB;
            GlConst.GL_FRAMEBUFFER = 36160;
            GlConst.GL_RENDERBUFFER = 36161;
            GlConst.GL_COLOR_ATTACHMENT0 = 36064;
            GlConst.GL_DEPTH_ATTACHMENT = 36096;
            GlConst.GL_FRAMEBUFFER_COMPLETE = 36053;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
            return "ARB_framebuffer_object extension";
        } else if (param0.GL_EXT_framebuffer_object) {
            fboMode = GlStateManager.FboMode.EXT;
            GlConst.GL_FRAMEBUFFER = 36160;
            GlConst.GL_RENDERBUFFER = 36161;
            GlConst.GL_COLOR_ATTACHMENT0 = 36064;
            GlConst.GL_DEPTH_ATTACHMENT = 36096;
            GlConst.GL_FRAMEBUFFER_COMPLETE = 36053;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
            return "EXT_framebuffer_object extension";
        } else {
            throw new IllegalStateException("Could not initialize framebuffer support.");
        }
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

    public static void glShaderSource(int param0, CharSequence param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glShaderSource(param0, param1);
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

    public static int _glGenBuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL15.glGenBuffers();
    }

    public static void _glBindBuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBindBuffer(param0, param1);
    }

    public static void _glBufferData(int param0, ByteBuffer param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBufferData(param0, param1, param2);
    }

    public static void _glDeleteBuffers(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL15.glDeleteBuffers(param0);
    }

    public static void _glBindFramebuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                GL30.glBindFramebuffer(param0, param1);
                break;
            case ARB:
                ARBFramebufferObject.glBindFramebuffer(param0, param1);
                break;
            case EXT:
                EXTFramebufferObject.glBindFramebufferEXT(param0, param1);
        }

    }

    public static void _glBindRenderbuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                GL30.glBindRenderbuffer(param0, param1);
                break;
            case ARB:
                ARBFramebufferObject.glBindRenderbuffer(param0, param1);
                break;
            case EXT:
                EXTFramebufferObject.glBindRenderbufferEXT(param0, param1);
        }

    }

    public static void _glDeleteRenderbuffers(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                GL30.glDeleteRenderbuffers(param0);
                break;
            case ARB:
                ARBFramebufferObject.glDeleteRenderbuffers(param0);
                break;
            case EXT:
                EXTFramebufferObject.glDeleteRenderbuffersEXT(param0);
        }

    }

    public static void _glDeleteFramebuffers(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                GL30.glDeleteFramebuffers(param0);
                break;
            case ARB:
                ARBFramebufferObject.glDeleteFramebuffers(param0);
                break;
            case EXT:
                EXTFramebufferObject.glDeleteFramebuffersEXT(param0);
        }

    }

    public static int glGenFramebuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                return GL30.glGenFramebuffers();
            case ARB:
                return ARBFramebufferObject.glGenFramebuffers();
            case EXT:
                return EXTFramebufferObject.glGenFramebuffersEXT();
            default:
                return -1;
        }
    }

    public static int glGenRenderbuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                return GL30.glGenRenderbuffers();
            case ARB:
                return ARBFramebufferObject.glGenRenderbuffers();
            case EXT:
                return EXTFramebufferObject.glGenRenderbuffersEXT();
            default:
                return -1;
        }
    }

    public static void _glRenderbufferStorage(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                GL30.glRenderbufferStorage(param0, param1, param2, param3);
                break;
            case ARB:
                ARBFramebufferObject.glRenderbufferStorage(param0, param1, param2, param3);
                break;
            case EXT:
                EXTFramebufferObject.glRenderbufferStorageEXT(param0, param1, param2, param3);
        }

    }

    public static void _glFramebufferRenderbuffer(int param0, int param1, int param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                GL30.glFramebufferRenderbuffer(param0, param1, param2, param3);
                break;
            case ARB:
                ARBFramebufferObject.glFramebufferRenderbuffer(param0, param1, param2, param3);
                break;
            case EXT:
                EXTFramebufferObject.glFramebufferRenderbufferEXT(param0, param1, param2, param3);
        }

    }

    public static int glCheckFramebufferStatus(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                return GL30.glCheckFramebufferStatus(param0);
            case ARB:
                return ARBFramebufferObject.glCheckFramebufferStatus(param0);
            case EXT:
                return EXTFramebufferObject.glCheckFramebufferStatusEXT(param0);
            default:
                return -1;
        }
    }

    public static void _glFramebufferTexture2D(int param0, int param1, int param2, int param3, int param4) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch(fboMode) {
            case BASE:
                GL30.glFramebufferTexture2D(param0, param1, param2, param3, param4);
                break;
            case ARB:
                ARBFramebufferObject.glFramebufferTexture2D(param0, param1, param2, param3, param4);
                break;
            case EXT:
                EXTFramebufferObject.glFramebufferTexture2DEXT(param0, param1, param2, param3, param4);
        }

    }

    public static void glActiveTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glActiveTexture(param0);
    }

    @Deprecated
    public static void _glClientActiveTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glClientActiveTexture(param0);
    }

    @Deprecated
    public static void _glMultiTexCoord2f(int param0, float param1, float param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glMultiTexCoord2f(param0, param1, param2);
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

    public static void setupOutline() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        _texEnv(8960, 8704, 34160);
        color1arg(7681, 34168);
    }

    public static void teardownOutline() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        _texEnv(8960, 8704, 8448);
        color3arg(8448, 5890, 34168, 34166);
    }

    public static void setupOverlayColor(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        _activeTexture(33985);
        _enableTexture();
        _matrixMode(5890);
        _loadIdentity();
        float var0 = 1.0F / (float)(param1 - 1);
        _scalef(var0, var0, var0);
        _matrixMode(5888);
        _bindTexture(param0);
        _texParameter(3553, 10241, 9728);
        _texParameter(3553, 10240, 9728);
        _texParameter(3553, 10242, 10496);
        _texParameter(3553, 10243, 10496);
        _texEnv(8960, 8704, 34160);
        color3arg(34165, 34168, 5890, 5890);
        alpha1arg(7681, 34168);
        _activeTexture(33984);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        _activeTexture(33985);
        _disableTexture();
        _activeTexture(33984);
    }

    private static void color1arg(int param0, int param1) {
        _texEnv(8960, 34161, param0);
        _texEnv(8960, 34176, param1);
        _texEnv(8960, 34192, 768);
    }

    private static void color3arg(int param0, int param1, int param2, int param3) {
        _texEnv(8960, 34161, param0);
        _texEnv(8960, 34176, param1);
        _texEnv(8960, 34192, 768);
        _texEnv(8960, 34177, param2);
        _texEnv(8960, 34193, 768);
        _texEnv(8960, 34178, param3);
        _texEnv(8960, 34194, 770);
    }

    private static void alpha1arg(int param0, int param1) {
        _texEnv(8960, 34162, param0);
        _texEnv(8960, 34184, param1);
        _texEnv(8960, 34200, 770);
    }

    public static void setupLevelDiffuseLighting(Matrix4f param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        _enableLight(0);
        _enableLight(1);
        Vector4f var0 = new Vector4f(DIFFUSE_LIGHT_0);
        var0.transform(param0);
        _light(16384, 4611, getBuffer(var0.x(), var0.y(), var0.z(), 0.0F));
        float var1 = 0.6F;
        _light(16384, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
        _light(16384, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        _light(16384, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        Vector4f var2 = new Vector4f(DIFFUSE_LIGHT_1);
        var2.transform(param0);
        _light(16385, 4611, getBuffer(var2.x(), var2.y(), var2.z(), 0.0F));
        _light(16385, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
        _light(16385, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        _light(16385, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        _shadeModel(7424);
        float var3 = 0.4F;
        _lightModel(2899, getBuffer(0.4F, 0.4F, 0.4F, 1.0F));
    }

    public static void setupGuiDiffuseLighting(Matrix4f param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Matrix4f var0 = param0.copy();
        var0.multiply(Vector3f.YP.rotationDegrees(-22.5F));
        var0.multiply(Vector3f.XP.rotationDegrees(135.0F));
        setupLevelDiffuseLighting(var0);
    }

    private static FloatBuffer getBuffer(float param0, float param1, float param2, float param3) {
        ((Buffer)FLOAT_ARG_BUFFER).clear();
        FLOAT_ARG_BUFFER.put(param0).put(param1).put(param2).put(param3);
        ((Buffer)FLOAT_ARG_BUFFER).flip();
        return FLOAT_ARG_BUFFER;
    }

    public static void setupEndPortalTexGen() {
        _texGenMode(GlStateManager.TexGen.S, 9216);
        _texGenMode(GlStateManager.TexGen.T, 9216);
        _texGenMode(GlStateManager.TexGen.R, 9216);
        _texGenParam(GlStateManager.TexGen.S, 9474, getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
        _texGenParam(GlStateManager.TexGen.T, 9474, getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
        _texGenParam(GlStateManager.TexGen.R, 9474, getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
        _enableTexGen(GlStateManager.TexGen.S);
        _enableTexGen(GlStateManager.TexGen.T);
        _enableTexGen(GlStateManager.TexGen.R);
    }

    public static void clearTexGen() {
        _disableTexGen(GlStateManager.TexGen.S);
        _disableTexGen(GlStateManager.TexGen.T);
        _disableTexGen(GlStateManager.TexGen.R);
    }

    public static void mulTextureByProjModelView() {
        _getMatrix(2983, MATRIX_BUFFER);
        _multMatrix(MATRIX_BUFFER);
        _getMatrix(2982, MATRIX_BUFFER);
        _multMatrix(MATRIX_BUFFER);
    }

    @Deprecated
    public static void _enableFog() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        FOG.enable.enable();
    }

    @Deprecated
    public static void _disableFog() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        FOG.enable.disable();
    }

    @Deprecated
    public static void _fogMode(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != FOG.mode) {
            FOG.mode = param0;
            _fogi(2917, param0);
        }

    }

    @Deprecated
    public static void _fogDensity(float param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != FOG.density) {
            FOG.density = param0;
            GL11.glFogf(2914, param0);
        }

    }

    @Deprecated
    public static void _fogStart(float param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != FOG.start) {
            FOG.start = param0;
            GL11.glFogf(2915, param0);
        }

    }

    @Deprecated
    public static void _fogEnd(float param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != FOG.end) {
            FOG.end = param0;
            GL11.glFogf(2916, param0);
        }

    }

    @Deprecated
    public static void _fog(int param0, float[] param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glFogfv(param0, param1);
    }

    @Deprecated
    public static void _fogi(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glFogi(param0, param1);
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

    public static void _enableLineOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        POLY_OFFSET.line.enable();
    }

    public static void _disableLineOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        POLY_OFFSET.line.disable();
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

    @Deprecated
    public static void _enableTexGen(GlStateManager.TexGen param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        getTexGen(param0).enable.enable();
    }

    @Deprecated
    public static void _disableTexGen(GlStateManager.TexGen param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        getTexGen(param0).enable.disable();
    }

    @Deprecated
    public static void _texGenMode(GlStateManager.TexGen param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.TexGenCoord var0 = getTexGen(param0);
        if (param1 != var0.mode) {
            var0.mode = param1;
            GL11.glTexGeni(var0.coord, 9472, param1);
        }

    }

    @Deprecated
    public static void _texGenParam(GlStateManager.TexGen param0, int param1, FloatBuffer param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexGenfv(getTexGen(param0).coord, param1, param2);
    }

    @Deprecated
    private static GlStateManager.TexGenCoord getTexGen(GlStateManager.TexGen param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        switch(param0) {
            case S:
                return TEX_GEN.s;
            case T:
                return TEX_GEN.t;
            case R:
                return TEX_GEN.r;
            case Q:
                return TEX_GEN.q;
            default:
                return TEX_GEN.s;
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
        TEXTURES[activeTexture].enable.enable();
    }

    public static void _disableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        TEXTURES[activeTexture].enable.disable();
    }

    @Deprecated
    public static void _texEnv(int param0, int param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexEnvi(param0, param1, param2);
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

    public static void _deleteTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glDeleteTextures(param0);

        for(GlStateManager.TextureState var0 : TEXTURES) {
            if (var0.binding == param0) {
                var0.binding = -1;
            }
        }

    }

    public static void _bindTexture(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (param0 != TEXTURES[activeTexture].binding) {
            TEXTURES[activeTexture].binding = param0;
            GL11.glBindTexture(3553, param0);
        }

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

    @Deprecated
    public static void _shadeModel(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (param0 != shadeModel) {
            shadeModel = param0;
            GL11.glShadeModel(param0);
        }

    }

    @Deprecated
    public static void _enableRescaleNormal() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        RESCALE_NORMAL.enable();
    }

    @Deprecated
    public static void _disableRescaleNormal() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        RESCALE_NORMAL.disable();
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
        if (param0 != CLEAR.depth) {
            CLEAR.depth = param0;
            GL11.glClearDepth(param0);
        }

    }

    public static void _clearColor(float param0, float param1, float param2, float param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (param0 != CLEAR.color.r || param1 != CLEAR.color.g || param2 != CLEAR.color.b || param3 != CLEAR.color.a) {
            CLEAR.color.r = param0;
            CLEAR.color.g = param1;
            CLEAR.color.b = param2;
            CLEAR.color.a = param3;
            GL11.glClearColor(param0, param1, param2, param3);
        }

    }

    public static void _clearStencil(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != CLEAR.stencil) {
            CLEAR.stencil = param0;
            GL11.glClearStencil(param0);
        }

    }

    public static void _clear(int param0, boolean param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClear(param0);
        if (param1) {
            _getError();
        }

    }

    @Deprecated
    public static void _matrixMode(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glMatrixMode(param0);
    }

    @Deprecated
    public static void _loadIdentity() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glLoadIdentity();
    }

    @Deprecated
    public static void _pushMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPushMatrix();
    }

    @Deprecated
    public static void _popMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPopMatrix();
    }

    @Deprecated
    public static void _getMatrix(int param0, FloatBuffer param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glGetFloatv(param0, param1);
    }

    @Deprecated
    public static void _ortho(double param0, double param1, double param2, double param3, double param4, double param5) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glOrtho(param0, param1, param2, param3, param4, param5);
    }

    @Deprecated
    public static void _rotatef(float param0, float param1, float param2, float param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glRotatef(param0, param1, param2, param3);
    }

    @Deprecated
    public static void _scalef(float param0, float param1, float param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glScalef(param0, param1, param2);
    }

    @Deprecated
    public static void _scaled(double param0, double param1, double param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glScaled(param0, param1, param2);
    }

    @Deprecated
    public static void _translatef(float param0, float param1, float param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTranslatef(param0, param1, param2);
    }

    @Deprecated
    public static void _translated(double param0, double param1, double param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTranslated(param0, param1, param2);
    }

    @Deprecated
    public static void _multMatrix(FloatBuffer param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glMultMatrixf(param0);
    }

    @Deprecated
    public static void _multMatrix(Matrix4f param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        param0.store(MATRIX_BUFFER);
        ((Buffer)MATRIX_BUFFER).rewind();
        _multMatrix(MATRIX_BUFFER);
    }

    @Deprecated
    public static void _color4f(float param0, float param1, float param2, float param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (param0 != COLOR.r || param1 != COLOR.g || param2 != COLOR.b || param3 != COLOR.a) {
            COLOR.r = param0;
            COLOR.g = param1;
            COLOR.b = param2;
            COLOR.a = param3;
            GL11.glColor4f(param0, param1, param2, param3);
        }

    }

    @Deprecated
    public static void _clearCurrentColor() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        COLOR.r = -1.0F;
        COLOR.g = -1.0F;
        COLOR.b = -1.0F;
        COLOR.a = -1.0F;
    }

    @Deprecated
    public static void _normalPointer(int param0, int param1, long param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glNormalPointer(param0, param1, param2);
    }

    @Deprecated
    public static void _texCoordPointer(int param0, int param1, int param2, long param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexCoordPointer(param0, param1, param2, param3);
    }

    @Deprecated
    public static void _vertexPointer(int param0, int param1, int param2, long param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glVertexPointer(param0, param1, param2, param3);
    }

    @Deprecated
    public static void _colorPointer(int param0, int param1, int param2, long param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glColorPointer(param0, param1, param2, param3);
    }

    public static void _vertexAttribPointer(int param0, int param1, int param2, boolean param3, int param4, long param5) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glVertexAttribPointer(param0, param1, param2, param3, param4, param5);
    }

    @Deprecated
    public static void _enableClientState(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glEnableClientState(param0);
    }

    @Deprecated
    public static void _disableClientState(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDisableClientState(param0);
    }

    public static void _enableVertexAttribArray(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glEnableVertexAttribArray(param0);
    }

    public static void _disableVertexAttribArray(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glEnableVertexAttribArray(param0);
    }

    public static void _drawArrays(int param0, int param1, int param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDrawArrays(param0, param1, param2);
    }

    public static void _lineWidth(float param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glLineWidth(param0);
    }

    public static void _pixelStore(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glPixelStorei(param0, param1);
    }

    public static void _pixelTransfer(int param0, float param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPixelTransferf(param0, param1);
    }

    public static void _readPixels(int param0, int param1, int param2, int param3, int param4, int param5, ByteBuffer param6) {
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

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    static class AlphaState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3008);
        public int func = 519;
        public float reference = -1.0F;

        private AlphaState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class BlendState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3042);
        public int srcRgb = 1;
        public int dstRgb = 0;
        public int srcAlpha = 1;
        public int dstAlpha = 0;

        private BlendState() {
        }
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
    static class ClearState {
        public double depth = 1.0;
        public final GlStateManager.Color color = new GlStateManager.Color(0.0F, 0.0F, 0.0F, 0.0F);
        public int stencil;

        private ClearState() {
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    static class Color {
        public float r = 1.0F;
        public float g = 1.0F;
        public float b = 1.0F;
        public float a = 1.0F;

        public Color() {
            this(1.0F, 1.0F, 1.0F, 1.0F);
        }

        public Color(float param0, float param1, float param2, float param3) {
            this.r = param0;
            this.g = param1;
            this.b = param2;
            this.a = param3;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ColorLogicState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3058);
        public int op = 5379;

        private ColorLogicState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ColorMask {
        public boolean red = true;
        public boolean green = true;
        public boolean blue = true;
        public boolean alpha = true;

        private ColorMask() {
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    static class ColorMaterialState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2903);
        public int face = 1032;
        public int mode = 5634;

        private ColorMaterialState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class CullState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2884);
        public int mode = 1029;

        private CullState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class DepthState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(2929);
        public boolean mask = true;
        public int func = 513;

        private DepthState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
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
    public static enum FboMode {
        BASE,
        ARB,
        EXT;
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public static enum FogMode {
        LINEAR(9729),
        EXP(2048),
        EXP2(2049);

        public final int value;

        private FogMode(int param0) {
            this.value = param0;
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    static class FogState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2912);
        public int mode = 2048;
        public float density = 1.0F;
        public float start;
        public float end = 1.0F;

        private FogState() {
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

        private PolygonOffsetState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
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

        private StencilFunc() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class StencilState {
        public final GlStateManager.StencilFunc func = new GlStateManager.StencilFunc();
        public int mask = -1;
        public int fail = 7680;
        public int zfail = 7680;
        public int zpass = 7680;

        private StencilState() {
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public static enum TexGen {
        S,
        T,
        R,
        Q;
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    static class TexGenCoord {
        public final GlStateManager.BooleanState enable;
        public final int coord;
        public int mode = -1;

        public TexGenCoord(int param0, int param1) {
            this.coord = param0;
            this.enable = new GlStateManager.BooleanState(param1);
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    static class TexGenState {
        public final GlStateManager.TexGenCoord s = new GlStateManager.TexGenCoord(8192, 3168);
        public final GlStateManager.TexGenCoord t = new GlStateManager.TexGenCoord(8193, 3169);
        public final GlStateManager.TexGenCoord r = new GlStateManager.TexGenCoord(8194, 3170);
        public final GlStateManager.TexGenCoord q = new GlStateManager.TexGenCoord(8195, 3171);

        private TexGenState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class TextureState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3553);
        public int binding;

        private TextureState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Viewport {
        INSTANCE;

        protected int x;
        protected int y;
        protected int width;
        protected int height;
    }
}
