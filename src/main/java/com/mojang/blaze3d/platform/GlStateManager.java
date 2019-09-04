package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL;
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
    private static final FloatBuffer COLOR_BUFFER = GLX.make(MemoryUtil.memAllocFloat(4), param0 -> DebugMemoryUntracker.untrack(MemoryUtil.memAddress(param0)));
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
    private static final GlStateManager.BooleanState NORMALIZE = new GlStateManager.BooleanState(2977);
    private static int activeTexture;
    private static final GlStateManager.TextureState[] TEXTURES = IntStream.range(0, 8)
        .mapToObj(param0 -> new GlStateManager.TextureState())
        .toArray(param0 -> new GlStateManager.TextureState[param0]);
    private static int shadeModel = 7425;
    private static final GlStateManager.BooleanState RESCALE_NORMAL = new GlStateManager.BooleanState(32826);
    private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();
    private static final GlStateManager.Color COLOR = new GlStateManager.Color();
    private static GlStateManager.FboMode fboMode;

    public static void _pushLightingAttributes() {
        GL11.glPushAttrib(8256);
    }

    public static void _pushTextureAttributes() {
        GL11.glPushAttrib(270336);
    }

    public static void _popAttributes() {
        GL11.glPopAttrib();
    }

    public static void _disableAlphaTest() {
        ALPHA_TEST.mode.disable();
    }

    public static void _enableAlphaTest() {
        ALPHA_TEST.mode.enable();
    }

    public static void _alphaFunc(int param0, float param1) {
        if (param0 != ALPHA_TEST.func || param1 != ALPHA_TEST.reference) {
            ALPHA_TEST.func = param0;
            ALPHA_TEST.reference = param1;
            GL11.glAlphaFunc(param0, param1);
        }

    }

    public static void _enableLighting() {
        LIGHTING.enable();
    }

    public static void _disableLighting() {
        LIGHTING.disable();
    }

    public static void _enableLight(int param0) {
        LIGHT_ENABLE[param0].enable();
    }

    public static void _disableLight(int param0) {
        LIGHT_ENABLE[param0].disable();
    }

    public static void _enableColorMaterial() {
        COLOR_MATERIAL.enable.enable();
    }

    public static void _disableColorMaterial() {
        COLOR_MATERIAL.enable.disable();
    }

    public static void _colorMaterial(int param0, int param1) {
        if (param0 != COLOR_MATERIAL.face || param1 != COLOR_MATERIAL.mode) {
            COLOR_MATERIAL.face = param0;
            COLOR_MATERIAL.mode = param1;
            GL11.glColorMaterial(param0, param1);
        }

    }

    public static void _light(int param0, int param1, FloatBuffer param2) {
        GL11.glLightfv(param0, param1, param2);
    }

    public static void _lightModel(int param0, FloatBuffer param1) {
        GL11.glLightModelfv(param0, param1);
    }

    public static void _normal3f(float param0, float param1, float param2) {
        GL11.glNormal3f(param0, param1, param2);
    }

    public static void _disableDepthTest() {
        DEPTH.mode.disable();
    }

    public static void _enableDepthTest() {
        DEPTH.mode.enable();
    }

    public static void _depthFunc(int param0) {
        if (param0 != DEPTH.func) {
            DEPTH.func = param0;
            GL11.glDepthFunc(param0);
        }

    }

    public static void _depthMask(boolean param0) {
        if (param0 != DEPTH.mask) {
            DEPTH.mask = param0;
            GL11.glDepthMask(param0);
        }

    }

    public static void _disableBlend() {
        BLEND.mode.disable();
    }

    public static void _enableBlend() {
        BLEND.mode.enable();
    }

    public static void _blendFunc(int param0, int param1) {
        if (param0 != BLEND.srcRgb || param1 != BLEND.dstRgb) {
            BLEND.srcRgb = param0;
            BLEND.dstRgb = param1;
            GL11.glBlendFunc(param0, param1);
        }

    }

    public static void _blendFuncSeparate(int param0, int param1, int param2, int param3) {
        if (param0 != BLEND.srcRgb || param1 != BLEND.dstRgb || param2 != BLEND.srcAlpha || param3 != BLEND.dstAlpha) {
            BLEND.srcRgb = param0;
            BLEND.dstRgb = param1;
            BLEND.srcAlpha = param2;
            BLEND.dstAlpha = param3;
            glBlendFuncSeparate(param0, param1, param2, param3);
        }

    }

    public static void _blendEquation(int param0) {
        GL14.glBlendEquation(param0);
    }

    public static void _setupSolidRenderingTextureCombine(int param0) {
        COLOR_BUFFER.put(0, (float)(param0 >> 16 & 0xFF) / 255.0F);
        COLOR_BUFFER.put(1, (float)(param0 >> 8 & 0xFF) / 255.0F);
        COLOR_BUFFER.put(2, (float)(param0 >> 0 & 0xFF) / 255.0F);
        COLOR_BUFFER.put(3, (float)(param0 >> 24 & 0xFF) / 255.0F);
        RenderSystem.texEnv(8960, 8705, COLOR_BUFFER);
        RenderSystem.texEnv(8960, 8704, 34160);
        RenderSystem.texEnv(8960, 34161, 7681);
        RenderSystem.texEnv(8960, 34176, 34166);
        RenderSystem.texEnv(8960, 34192, 768);
        RenderSystem.texEnv(8960, 34162, 7681);
        RenderSystem.texEnv(8960, 34184, 5890);
        RenderSystem.texEnv(8960, 34200, 770);
    }

    public static void _tearDownSolidRenderingTextureCombine() {
        RenderSystem.texEnv(8960, 8704, 8448);
        RenderSystem.texEnv(8960, 34161, 8448);
        RenderSystem.texEnv(8960, 34162, 8448);
        RenderSystem.texEnv(8960, 34176, 5890);
        RenderSystem.texEnv(8960, 34184, 5890);
        RenderSystem.texEnv(8960, 34192, 768);
        RenderSystem.texEnv(8960, 34200, 770);
    }

    public static String _init_fbo(GLCapabilities param0) {
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
        return GL20.glGetProgrami(param0, param1);
    }

    public static void glAttachShader(int param0, int param1) {
        GL20.glAttachShader(param0, param1);
    }

    public static void glDeleteShader(int param0) {
        GL20.glDeleteShader(param0);
    }

    public static int glCreateShader(int param0) {
        return GL20.glCreateShader(param0);
    }

    public static void glShaderSource(int param0, CharSequence param1) {
        GL20.glShaderSource(param0, param1);
    }

    public static void glCompileShader(int param0) {
        GL20.glCompileShader(param0);
    }

    public static int glGetShaderi(int param0, int param1) {
        return GL20.glGetShaderi(param0, param1);
    }

    public static void _glUseProgram(int param0) {
        GL20.glUseProgram(param0);
    }

    public static int glCreateProgram() {
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int param0) {
        GL20.glDeleteProgram(param0);
    }

    public static void glLinkProgram(int param0) {
        GL20.glLinkProgram(param0);
    }

    public static int _glGetUniformLocation(int param0, CharSequence param1) {
        return GL20.glGetUniformLocation(param0, param1);
    }

    public static void glUniform1(int param0, IntBuffer param1) {
        GL20.glUniform1iv(param0, param1);
    }

    public static void _glUniform1i(int param0, int param1) {
        GL20.glUniform1i(param0, param1);
    }

    public static void glUniform1(int param0, FloatBuffer param1) {
        GL20.glUniform1fv(param0, param1);
    }

    public static void glUniform2(int param0, IntBuffer param1) {
        GL20.glUniform2iv(param0, param1);
    }

    public static void glUniform2(int param0, FloatBuffer param1) {
        GL20.glUniform2fv(param0, param1);
    }

    public static void glUniform3(int param0, IntBuffer param1) {
        GL20.glUniform3iv(param0, param1);
    }

    public static void glUniform3(int param0, FloatBuffer param1) {
        GL20.glUniform3fv(param0, param1);
    }

    public static void glUniform4(int param0, IntBuffer param1) {
        GL20.glUniform4iv(param0, param1);
    }

    public static void glUniform4(int param0, FloatBuffer param1) {
        GL20.glUniform4fv(param0, param1);
    }

    public static void glUniformMatrix2(int param0, boolean param1, FloatBuffer param2) {
        GL20.glUniformMatrix2fv(param0, param1, param2);
    }

    public static void glUniformMatrix3(int param0, boolean param1, FloatBuffer param2) {
        GL20.glUniformMatrix3fv(param0, param1, param2);
    }

    public static void glUniformMatrix4(int param0, boolean param1, FloatBuffer param2) {
        GL20.glUniformMatrix4fv(param0, param1, param2);
    }

    public static int _glGetAttribLocation(int param0, CharSequence param1) {
        return GL20.glGetAttribLocation(param0, param1);
    }

    public static int glGenBuffers() {
        return GL15.glGenBuffers();
    }

    public static void glBindBuffer(int param0, int param1) {
        GL15.glBindBuffer(param0, param1);
    }

    public static void glBufferData(int param0, ByteBuffer param1, int param2) {
        GL15.glBufferData(param0, param1, param2);
    }

    public static void glDeleteBuffers(int param0) {
        GL15.glDeleteBuffers(param0);
    }

    public static void glBindFramebuffer(int param0, int param1) {
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

    public static void glBindRenderbuffer(int param0, int param1) {
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

    public static void glDeleteRenderbuffers(int param0) {
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

    public static void glDeleteFramebuffers(int param0) {
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

    public static void glRenderbufferStorage(int param0, int param1, int param2, int param3) {
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

    public static void glFramebufferRenderbuffer(int param0, int param1, int param2, int param3) {
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

    public static void glFramebufferTexture2D(int param0, int param1, int param2, int param3, int param4) {
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
        GL13.glActiveTexture(param0);
    }

    public static void _glClientActiveTexture(int param0) {
        GL13.glClientActiveTexture(param0);
    }

    public static void _glMultiTexCoord2f(int param0, float param1, float param2) {
        GL13.glMultiTexCoord2f(param0, param1, param2);
    }

    public static void glBlendFuncSeparate(int param0, int param1, int param2, int param3) {
        GL14.glBlendFuncSeparate(param0, param1, param2, param3);
    }

    public static String glGetShaderInfoLog(int param0, int param1) {
        return GL20.glGetShaderInfoLog(param0, param1);
    }

    public static String glGetProgramInfoLog(int param0, int param1) {
        return GL20.glGetProgramInfoLog(param0, param1);
    }

    public static void _enableFog() {
        FOG.enable.enable();
    }

    public static void _disableFog() {
        FOG.enable.disable();
    }

    public static void _fogMode(int param0) {
        if (param0 != FOG.mode) {
            FOG.mode = param0;
            _fogi(2917, param0);
        }

    }

    public static void _fogDensity(float param0) {
        if (param0 != FOG.density) {
            FOG.density = param0;
            GL11.glFogf(2914, param0);
        }

    }

    public static void _fogStart(float param0) {
        if (param0 != FOG.start) {
            FOG.start = param0;
            GL11.glFogf(2915, param0);
        }

    }

    public static void _fogEnd(float param0) {
        if (param0 != FOG.end) {
            FOG.end = param0;
            GL11.glFogf(2916, param0);
        }

    }

    public static void _fog(int param0, FloatBuffer param1) {
        GL11.glFogfv(param0, param1);
    }

    public static void _fogi(int param0, int param1) {
        GL11.glFogi(param0, param1);
    }

    public static void _enableCull() {
        CULL.enable.enable();
    }

    public static void _disableCull() {
        CULL.enable.disable();
    }

    public static void _cullFace(int param0) {
        if (param0 != CULL.mode) {
            CULL.mode = param0;
            GL11.glCullFace(param0);
        }

    }

    public static void _polygonMode(int param0, int param1) {
        GL11.glPolygonMode(param0, param1);
    }

    public static void _enablePolygonOffset() {
        POLY_OFFSET.fill.enable();
    }

    public static void _disablePolygonOffset() {
        POLY_OFFSET.fill.disable();
    }

    public static void _enableLineOffset() {
        POLY_OFFSET.line.enable();
    }

    public static void _disableLineOffset() {
        POLY_OFFSET.line.disable();
    }

    public static void _polygonOffset(float param0, float param1) {
        if (param0 != POLY_OFFSET.factor || param1 != POLY_OFFSET.units) {
            POLY_OFFSET.factor = param0;
            POLY_OFFSET.units = param1;
            GL11.glPolygonOffset(param0, param1);
        }

    }

    public static void _enableColorLogicOp() {
        COLOR_LOGIC.enable.enable();
    }

    public static void _disableColorLogicOp() {
        COLOR_LOGIC.enable.disable();
    }

    public static void _logicOp(int param0) {
        if (param0 != COLOR_LOGIC.op) {
            COLOR_LOGIC.op = param0;
            GL11.glLogicOp(param0);
        }

    }

    public static void _enableTexGen(GlStateManager.TexGen param0) {
        getTexGen(param0).enable.enable();
    }

    public static void _disableTexGen(GlStateManager.TexGen param0) {
        getTexGen(param0).enable.disable();
    }

    public static void _texGenMode(GlStateManager.TexGen param0, int param1) {
        GlStateManager.TexGenCoord var0 = getTexGen(param0);
        if (param1 != var0.mode) {
            var0.mode = param1;
            GL11.glTexGeni(var0.coord, 9472, param1);
        }

    }

    public static void _texGenParam(GlStateManager.TexGen param0, int param1, FloatBuffer param2) {
        GL11.glTexGenfv(getTexGen(param0).coord, param1, param2);
    }

    private static GlStateManager.TexGenCoord getTexGen(GlStateManager.TexGen param0) {
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
        if (activeTexture != param0 - 33984) {
            activeTexture = param0 - 33984;
            glActiveTexture(param0);
        }

    }

    public static void _enableTexture() {
        TEXTURES[activeTexture].enable.enable();
    }

    public static void _disableTexture() {
        TEXTURES[activeTexture].enable.disable();
    }

    public static void _texEnv(int param0, int param1, FloatBuffer param2) {
        GL11.glTexEnvfv(param0, param1, param2);
    }

    public static void _texEnv(int param0, int param1, int param2) {
        GL11.glTexEnvi(param0, param1, param2);
    }

    public static void _texEnv(int param0, int param1, float param2) {
        GL11.glTexEnvf(param0, param1, param2);
    }

    public static void _texParameter(int param0, int param1, float param2) {
        GL11.glTexParameterf(param0, param1, param2);
    }

    public static void _texParameter(int param0, int param1, int param2) {
        GL11.glTexParameteri(param0, param1, param2);
    }

    public static int _getTexLevelParameter(int param0, int param1, int param2) {
        return GL11.glGetTexLevelParameteri(param0, param1, param2);
    }

    public static int _genTexture() {
        return GL11.glGenTextures();
    }

    public static void _deleteTexture(int param0) {
        GL11.glDeleteTextures(param0);

        for(GlStateManager.TextureState var0 : TEXTURES) {
            if (var0.binding == param0) {
                var0.binding = -1;
            }
        }

    }

    public static void _bindTexture(int param0) {
        if (param0 != TEXTURES[activeTexture].binding) {
            TEXTURES[activeTexture].binding = param0;
            GL11.glBindTexture(3553, param0);
        }

    }

    public static void _texImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, @Nullable IntBuffer param8) {
        GL11.glTexImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void _texSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, long param8) {
        GL11.glTexSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void _copyTexSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        GL11.glCopyTexSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void _getTexImage(int param0, int param1, int param2, int param3, long param4) {
        GL11.glGetTexImage(param0, param1, param2, param3, param4);
    }

    public static void _enableNormalize() {
        NORMALIZE.enable();
    }

    public static void _disableNormalize() {
        NORMALIZE.disable();
    }

    public static void _shadeModel(int param0) {
        if (param0 != shadeModel) {
            shadeModel = param0;
            GL11.glShadeModel(param0);
        }

    }

    public static void _enableRescaleNormal() {
        RESCALE_NORMAL.enable();
    }

    public static void _disableRescaleNormal() {
        RESCALE_NORMAL.disable();
    }

    public static void _viewport(int param0, int param1, int param2, int param3) {
        GlStateManager.Viewport.INSTANCE.x = param0;
        GlStateManager.Viewport.INSTANCE.y = param1;
        GlStateManager.Viewport.INSTANCE.width = param2;
        GlStateManager.Viewport.INSTANCE.height = param3;
        GL11.glViewport(param0, param1, param2, param3);
    }

    public static void _colorMask(boolean param0, boolean param1, boolean param2, boolean param3) {
        if (param0 != COLOR_MASK.red || param1 != COLOR_MASK.green || param2 != COLOR_MASK.blue || param3 != COLOR_MASK.alpha) {
            COLOR_MASK.red = param0;
            COLOR_MASK.green = param1;
            COLOR_MASK.blue = param2;
            COLOR_MASK.alpha = param3;
            GL11.glColorMask(param0, param1, param2, param3);
        }

    }

    public static void _stencilFunc(int param0, int param1, int param2) {
        if (param0 != STENCIL.func.func || param0 != STENCIL.func.ref || param0 != STENCIL.func.mask) {
            STENCIL.func.func = param0;
            STENCIL.func.ref = param1;
            STENCIL.func.mask = param2;
            GL11.glStencilFunc(param0, param1, param2);
        }

    }

    public static void _stencilMask(int param0) {
        if (param0 != STENCIL.mask) {
            STENCIL.mask = param0;
            GL11.glStencilMask(param0);
        }

    }

    public static void _stencilOp(int param0, int param1, int param2) {
        if (param0 != STENCIL.fail || param1 != STENCIL.zfail || param2 != STENCIL.zpass) {
            STENCIL.fail = param0;
            STENCIL.zfail = param1;
            STENCIL.zpass = param2;
            GL11.glStencilOp(param0, param1, param2);
        }

    }

    public static void _clearDepth(double param0) {
        if (param0 != CLEAR.depth) {
            CLEAR.depth = param0;
            GL11.glClearDepth(param0);
        }

    }

    public static void _clearColor(float param0, float param1, float param2, float param3) {
        if (param0 != CLEAR.color.r || param1 != CLEAR.color.g || param2 != CLEAR.color.b || param3 != CLEAR.color.a) {
            CLEAR.color.r = param0;
            CLEAR.color.g = param1;
            CLEAR.color.b = param2;
            CLEAR.color.a = param3;
            GL11.glClearColor(param0, param1, param2, param3);
        }

    }

    public static void _clearStencil(int param0) {
        if (param0 != CLEAR.stencil) {
            CLEAR.stencil = param0;
            GL11.glClearStencil(param0);
        }

    }

    public static void _clear(int param0, boolean param1) {
        GL11.glClear(param0);
        if (param1) {
            RenderSystem.getError();
        }

    }

    public static void _matrixMode(int param0) {
        GL11.glMatrixMode(param0);
    }

    public static void _loadIdentity() {
        GL11.glLoadIdentity();
    }

    public static void _pushMatrix() {
        GL11.glPushMatrix();
    }

    public static void _popMatrix() {
        GL11.glPopMatrix();
    }

    public static void _getMatrix(int param0, FloatBuffer param1) {
        GL11.glGetFloatv(param0, param1);
    }

    public static Matrix4f _getMatrix4f(int param0) {
        _getMatrix(param0, MATRIX_BUFFER);
        ((Buffer)MATRIX_BUFFER).rewind();
        Matrix4f var0 = new Matrix4f();
        var0.load(MATRIX_BUFFER);
        ((Buffer)MATRIX_BUFFER).rewind();
        return var0;
    }

    public static void _ortho(double param0, double param1, double param2, double param3, double param4, double param5) {
        GL11.glOrtho(param0, param1, param2, param3, param4, param5);
    }

    public static void _rotatef(float param0, float param1, float param2, float param3) {
        GL11.glRotatef(param0, param1, param2, param3);
    }

    public static void _rotated(double param0, double param1, double param2, double param3) {
        GL11.glRotated(param0, param1, param2, param3);
    }

    public static void _scalef(float param0, float param1, float param2) {
        GL11.glScalef(param0, param1, param2);
    }

    public static void _scaled(double param0, double param1, double param2) {
        GL11.glScaled(param0, param1, param2);
    }

    public static void _translatef(float param0, float param1, float param2) {
        GL11.glTranslatef(param0, param1, param2);
    }

    public static void _translated(double param0, double param1, double param2) {
        GL11.glTranslated(param0, param1, param2);
    }

    public static void _multMatrix(FloatBuffer param0) {
        GL11.glMultMatrixf(param0);
    }

    public static void _multMatrix(Matrix4f param0) {
        param0.store(MATRIX_BUFFER);
        ((Buffer)MATRIX_BUFFER).rewind();
        _multMatrix(MATRIX_BUFFER);
    }

    public static void _color4f(float param0, float param1, float param2, float param3) {
        if (param0 != COLOR.r || param1 != COLOR.g || param2 != COLOR.b || param3 != COLOR.a) {
            COLOR.r = param0;
            COLOR.g = param1;
            COLOR.b = param2;
            COLOR.a = param3;
            GL11.glColor4f(param0, param1, param2, param3);
        }

    }

    public static void _texCoord2f(float param0, float param1) {
        GL11.glTexCoord2f(param0, param1);
    }

    public static void _vertex3f(float param0, float param1, float param2) {
        GL11.glVertex3f(param0, param1, param2);
    }

    public static void _clearCurrentColor() {
        COLOR.r = -1.0F;
        COLOR.g = -1.0F;
        COLOR.b = -1.0F;
        COLOR.a = -1.0F;
    }

    public static void _normalPointer(int param0, int param1, int param2) {
        GL11.glNormalPointer(param0, param1, (long)param2);
    }

    public static void _normalPointer(int param0, int param1, ByteBuffer param2) {
        GL11.glNormalPointer(param0, param1, param2);
    }

    public static void _texCoordPointer(int param0, int param1, int param2, int param3) {
        GL11.glTexCoordPointer(param0, param1, param2, (long)param3);
    }

    public static void _texCoordPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GL11.glTexCoordPointer(param0, param1, param2, param3);
    }

    public static void _vertexPointer(int param0, int param1, int param2, int param3) {
        GL11.glVertexPointer(param0, param1, param2, (long)param3);
    }

    public static void _vertexPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GL11.glVertexPointer(param0, param1, param2, param3);
    }

    public static void _colorPointer(int param0, int param1, int param2, int param3) {
        GL11.glColorPointer(param0, param1, param2, (long)param3);
    }

    public static void _colorPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GL11.glColorPointer(param0, param1, param2, param3);
    }

    public static void _disableClientState(int param0) {
        GL11.glDisableClientState(param0);
    }

    public static void _enableClientState(int param0) {
        GL11.glEnableClientState(param0);
    }

    public static void _begin(int param0) {
        GL11.glBegin(param0);
    }

    public static void _end() {
        GL11.glEnd();
    }

    public static void _drawArrays(int param0, int param1, int param2) {
        GL11.glDrawArrays(param0, param1, param2);
    }

    public static void _lineWidth(float param0) {
        GL11.glLineWidth(param0);
    }

    public static void _callList(int param0) {
        GL11.glCallList(param0);
    }

    public static void _deleteLists(int param0, int param1) {
        GL11.glDeleteLists(param0, param1);
    }

    public static void _newList(int param0, int param1) {
        GL11.glNewList(param0, param1);
    }

    public static void _endList() {
        GL11.glEndList();
    }

    public static int _genLists(int param0) {
        return GL11.glGenLists(param0);
    }

    public static void _pixelStore(int param0, int param1) {
        GL11.glPixelStorei(param0, param1);
    }

    public static void _pixelTransfer(int param0, float param1) {
        GL11.glPixelTransferf(param0, param1);
    }

    public static void _readPixels(int param0, int param1, int param2, int param3, int param4, int param5, ByteBuffer param6) {
        GL11.glReadPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static void _readPixels(int param0, int param1, int param2, int param3, int param4, int param5, long param6) {
        GL11.glReadPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static int _getError() {
        return GL11.glGetError();
    }

    public static String _getString(int param0) {
        return GL11.glGetString(param0);
    }

    public static void _getInteger(int param0, IntBuffer param1) {
        GL11.glGetIntegerv(param0, param1);
    }

    public static int _getInteger(int param0) {
        return GL11.glGetInteger(param0);
    }

    public static void setProfile(GlStateManager.Profile param0) {
        param0.apply();
    }

    public static void unsetProfile(GlStateManager.Profile param0) {
        param0.clean();
    }

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

    @OnlyIn(Dist.CLIENT)
    static class ColorMaterialState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2903);
        public int face = 1032;
        public int mode = 5634;

        private ColorMaterialState() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum CullFace {
        FRONT(1028),
        BACK(1029),
        FRONT_AND_BACK(1032);

        public final int value;

        private CullFace(int param0) {
            this.value = param0;
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
    public static enum Profile {
        DEFAULT {
            @Override
            public void apply() {
                RenderSystem.disableAlphaTest();
                RenderSystem.alphaFunc(519, 0.0F);
                RenderSystem.disableLighting();
                RenderSystem.lightModel(2899, Lighting.getBuffer(0.2F, 0.2F, 0.2F, 1.0F));

                for(int var0 = 0; var0 < 8; ++var0) {
                    RenderSystem.disableLight(var0);
                    RenderSystem.light(16384 + var0, 4608, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                    RenderSystem.light(16384 + var0, 4611, Lighting.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
                    if (var0 == 0) {
                        RenderSystem.light(16384 + var0, 4609, Lighting.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                        RenderSystem.light(16384 + var0, 4610, Lighting.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                    } else {
                        RenderSystem.light(16384 + var0, 4609, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                        RenderSystem.light(16384 + var0, 4610, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                    }
                }

                RenderSystem.disableColorMaterial();
                RenderSystem.colorMaterial(1032, 5634);
                RenderSystem.disableDepthTest();
                RenderSystem.depthFunc(513);
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
                );
                RenderSystem.blendEquation(32774);
                RenderSystem.disableFog();
                RenderSystem.fogi(2917, 2048);
                RenderSystem.fogDensity(1.0F);
                RenderSystem.fogStart(0.0F);
                RenderSystem.fogEnd(1.0F);
                RenderSystem.fog(2918, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                if (GL.getCapabilities().GL_NV_fog_distance) {
                    RenderSystem.fogi(2917, 34140);
                }

                RenderSystem.polygonOffset(0.0F, 0.0F);
                RenderSystem.disableColorLogicOp();
                RenderSystem.logicOp(5379);
                RenderSystem.disableTexGen(GlStateManager.TexGen.S);
                RenderSystem.texGenMode(GlStateManager.TexGen.S, 9216);
                RenderSystem.texGenParam(GlStateManager.TexGen.S, 9474, Lighting.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
                RenderSystem.texGenParam(GlStateManager.TexGen.S, 9217, Lighting.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
                RenderSystem.disableTexGen(GlStateManager.TexGen.T);
                RenderSystem.texGenMode(GlStateManager.TexGen.T, 9216);
                RenderSystem.texGenParam(GlStateManager.TexGen.T, 9474, Lighting.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
                RenderSystem.texGenParam(GlStateManager.TexGen.T, 9217, Lighting.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
                RenderSystem.disableTexGen(GlStateManager.TexGen.R);
                RenderSystem.texGenMode(GlStateManager.TexGen.R, 9216);
                RenderSystem.texGenParam(GlStateManager.TexGen.R, 9474, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                RenderSystem.texGenParam(GlStateManager.TexGen.R, 9217, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                RenderSystem.disableTexGen(GlStateManager.TexGen.Q);
                RenderSystem.texGenMode(GlStateManager.TexGen.Q, 9216);
                RenderSystem.texGenParam(GlStateManager.TexGen.Q, 9474, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                RenderSystem.texGenParam(GlStateManager.TexGen.Q, 9217, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                RenderSystem.activeTexture(0);
                RenderSystem.texParameter(3553, 10240, 9729);
                RenderSystem.texParameter(3553, 10241, 9986);
                RenderSystem.texParameter(3553, 10242, 10497);
                RenderSystem.texParameter(3553, 10243, 10497);
                RenderSystem.texParameter(3553, 33085, 1000);
                RenderSystem.texParameter(3553, 33083, 1000);
                RenderSystem.texParameter(3553, 33082, -1000);
                RenderSystem.texParameter(3553, 34049, 0.0F);
                RenderSystem.texEnv(8960, 8704, 8448);
                RenderSystem.texEnv(8960, 8705, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                RenderSystem.texEnv(8960, 34161, 8448);
                RenderSystem.texEnv(8960, 34162, 8448);
                RenderSystem.texEnv(8960, 34176, 5890);
                RenderSystem.texEnv(8960, 34177, 34168);
                RenderSystem.texEnv(8960, 34178, 34166);
                RenderSystem.texEnv(8960, 34184, 5890);
                RenderSystem.texEnv(8960, 34185, 34168);
                RenderSystem.texEnv(8960, 34186, 34166);
                RenderSystem.texEnv(8960, 34192, 768);
                RenderSystem.texEnv(8960, 34193, 768);
                RenderSystem.texEnv(8960, 34194, 770);
                RenderSystem.texEnv(8960, 34200, 770);
                RenderSystem.texEnv(8960, 34201, 770);
                RenderSystem.texEnv(8960, 34202, 770);
                RenderSystem.texEnv(8960, 34163, 1.0F);
                RenderSystem.texEnv(8960, 3356, 1.0F);
                RenderSystem.disableNormalize();
                RenderSystem.shadeModel(7425);
                RenderSystem.disableRescaleNormal();
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.clearDepth(1.0);
                RenderSystem.lineWidth(1.0F);
                RenderSystem.normal3f(0.0F, 0.0F, 1.0F);
                RenderSystem.polygonMode(1028, 6914);
                RenderSystem.polygonMode(1029, 6914);
            }

            @Override
            public void clean() {
            }
        },
        PLAYER_SKIN {
            @Override
            public void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            }

            @Override
            public void clean() {
                RenderSystem.disableBlend();
            }
        },
        TRANSPARENT_MODEL {
            @Override
            public void apply() {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.15F);
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.alphaFunc(516, 0.003921569F);
            }

            @Override
            public void clean() {
                RenderSystem.disableBlend();
                RenderSystem.alphaFunc(516, 0.1F);
                RenderSystem.depthMask(true);
            }
        };

        private Profile() {
        }

        public abstract void apply();

        public abstract void clean();
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

    @OnlyIn(Dist.CLIENT)
    public static enum TexGen {
        S,
        T,
        R,
        Q;
    }

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
