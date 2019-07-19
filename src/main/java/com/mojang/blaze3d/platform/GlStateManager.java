package com.mojang.blaze3d.platform;

import com.mojang.math.Matrix4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class GlStateManager {
    private static final int LIGHT_COUNT = 8;
    private static final int TEXTURE_COUNT = 8;
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
    private static final float DEFAULTALPHACUTOFF = 0.1F;

    public static void pushLightingAttributes() {
        GL11.glPushAttrib(8256);
    }

    public static void pushTextureAttributes() {
        GL11.glPushAttrib(270336);
    }

    public static void popAttributes() {
        GL11.glPopAttrib();
    }

    public static void disableAlphaTest() {
        ALPHA_TEST.mode.disable();
    }

    public static void enableAlphaTest() {
        ALPHA_TEST.mode.enable();
    }

    public static void alphaFunc(int param0, float param1) {
        if (param0 != ALPHA_TEST.func || param1 != ALPHA_TEST.reference) {
            ALPHA_TEST.func = param0;
            ALPHA_TEST.reference = param1;
            GL11.glAlphaFunc(param0, param1);
        }

    }

    public static void enableLighting() {
        LIGHTING.enable();
    }

    public static void disableLighting() {
        LIGHTING.disable();
    }

    public static void enableLight(int param0) {
        LIGHT_ENABLE[param0].enable();
    }

    public static void disableLight(int param0) {
        LIGHT_ENABLE[param0].disable();
    }

    public static void enableColorMaterial() {
        COLOR_MATERIAL.enable.enable();
    }

    public static void disableColorMaterial() {
        COLOR_MATERIAL.enable.disable();
    }

    public static void colorMaterial(int param0, int param1) {
        if (param0 != COLOR_MATERIAL.face || param1 != COLOR_MATERIAL.mode) {
            COLOR_MATERIAL.face = param0;
            COLOR_MATERIAL.mode = param1;
            GL11.glColorMaterial(param0, param1);
        }

    }

    public static void light(int param0, int param1, FloatBuffer param2) {
        GL11.glLightfv(param0, param1, param2);
    }

    public static void lightModel(int param0, FloatBuffer param1) {
        GL11.glLightModelfv(param0, param1);
    }

    public static void normal3f(float param0, float param1, float param2) {
        GL11.glNormal3f(param0, param1, param2);
    }

    public static void disableDepthTest() {
        DEPTH.mode.disable();
    }

    public static void enableDepthTest() {
        DEPTH.mode.enable();
    }

    public static void depthFunc(int param0) {
        if (param0 != DEPTH.func) {
            DEPTH.func = param0;
            GL11.glDepthFunc(param0);
        }

    }

    public static void depthMask(boolean param0) {
        if (param0 != DEPTH.mask) {
            DEPTH.mask = param0;
            GL11.glDepthMask(param0);
        }

    }

    public static void disableBlend() {
        BLEND.mode.disable();
    }

    public static void enableBlend() {
        BLEND.mode.enable();
    }

    public static void blendFunc(GlStateManager.SourceFactor param0, GlStateManager.DestFactor param1) {
        blendFunc(param0.value, param1.value);
    }

    public static void blendFunc(int param0, int param1) {
        if (param0 != BLEND.srcRgb || param1 != BLEND.dstRgb) {
            BLEND.srcRgb = param0;
            BLEND.dstRgb = param1;
            GL11.glBlendFunc(param0, param1);
        }

    }

    public static void blendFuncSeparate(
        GlStateManager.SourceFactor param0, GlStateManager.DestFactor param1, GlStateManager.SourceFactor param2, GlStateManager.DestFactor param3
    ) {
        blendFuncSeparate(param0.value, param1.value, param2.value, param3.value);
    }

    public static void blendFuncSeparate(int param0, int param1, int param2, int param3) {
        if (param0 != BLEND.srcRgb || param1 != BLEND.dstRgb || param2 != BLEND.srcAlpha || param3 != BLEND.dstAlpha) {
            BLEND.srcRgb = param0;
            BLEND.dstRgb = param1;
            BLEND.srcAlpha = param2;
            BLEND.dstAlpha = param3;
            GLX.glBlendFuncSeparate(param0, param1, param2, param3);
        }

    }

    public static void blendEquation(int param0) {
        GL14.glBlendEquation(param0);
    }

    public static void setupSolidRenderingTextureCombine(int param0) {
        COLOR_BUFFER.put(0, (float)(param0 >> 16 & 0xFF) / 255.0F);
        COLOR_BUFFER.put(1, (float)(param0 >> 8 & 0xFF) / 255.0F);
        COLOR_BUFFER.put(2, (float)(param0 >> 0 & 0xFF) / 255.0F);
        COLOR_BUFFER.put(3, (float)(param0 >> 24 & 0xFF) / 255.0F);
        texEnv(8960, 8705, COLOR_BUFFER);
        texEnv(8960, 8704, 34160);
        texEnv(8960, 34161, 7681);
        texEnv(8960, 34176, 34166);
        texEnv(8960, 34192, 768);
        texEnv(8960, 34162, 7681);
        texEnv(8960, 34184, 5890);
        texEnv(8960, 34200, 770);
    }

    public static void tearDownSolidRenderingTextureCombine() {
        texEnv(8960, 8704, 8448);
        texEnv(8960, 34161, 8448);
        texEnv(8960, 34162, 8448);
        texEnv(8960, 34176, 5890);
        texEnv(8960, 34184, 5890);
        texEnv(8960, 34192, 768);
        texEnv(8960, 34200, 770);
    }

    public static void enableFog() {
        FOG.enable.enable();
    }

    public static void disableFog() {
        FOG.enable.disable();
    }

    public static void fogMode(GlStateManager.FogMode param0) {
        fogMode(param0.value);
    }

    private static void fogMode(int param0) {
        if (param0 != FOG.mode) {
            FOG.mode = param0;
            GL11.glFogi(2917, param0);
        }

    }

    public static void fogDensity(float param0) {
        if (param0 != FOG.density) {
            FOG.density = param0;
            GL11.glFogf(2914, param0);
        }

    }

    public static void fogStart(float param0) {
        if (param0 != FOG.start) {
            FOG.start = param0;
            GL11.glFogf(2915, param0);
        }

    }

    public static void fogEnd(float param0) {
        if (param0 != FOG.end) {
            FOG.end = param0;
            GL11.glFogf(2916, param0);
        }

    }

    public static void fog(int param0, FloatBuffer param1) {
        GL11.glFogfv(param0, param1);
    }

    public static void fogi(int param0, int param1) {
        GL11.glFogi(param0, param1);
    }

    public static void enableCull() {
        CULL.enable.enable();
    }

    public static void disableCull() {
        CULL.enable.disable();
    }

    public static void cullFace(GlStateManager.CullFace param0) {
        cullFace(param0.value);
    }

    private static void cullFace(int param0) {
        if (param0 != CULL.mode) {
            CULL.mode = param0;
            GL11.glCullFace(param0);
        }

    }

    public static void polygonMode(int param0, int param1) {
        GL11.glPolygonMode(param0, param1);
    }

    public static void enablePolygonOffset() {
        POLY_OFFSET.fill.enable();
    }

    public static void disablePolygonOffset() {
        POLY_OFFSET.fill.disable();
    }

    public static void enableLineOffset() {
        POLY_OFFSET.line.enable();
    }

    public static void disableLineOffset() {
        POLY_OFFSET.line.disable();
    }

    public static void polygonOffset(float param0, float param1) {
        if (param0 != POLY_OFFSET.factor || param1 != POLY_OFFSET.units) {
            POLY_OFFSET.factor = param0;
            POLY_OFFSET.units = param1;
            GL11.glPolygonOffset(param0, param1);
        }

    }

    public static void enableColorLogicOp() {
        COLOR_LOGIC.enable.enable();
    }

    public static void disableColorLogicOp() {
        COLOR_LOGIC.enable.disable();
    }

    public static void logicOp(GlStateManager.LogicOp param0) {
        logicOp(param0.value);
    }

    public static void logicOp(int param0) {
        if (param0 != COLOR_LOGIC.op) {
            COLOR_LOGIC.op = param0;
            GL11.glLogicOp(param0);
        }

    }

    public static void enableTexGen(GlStateManager.TexGen param0) {
        getTexGen(param0).enable.enable();
    }

    public static void disableTexGen(GlStateManager.TexGen param0) {
        getTexGen(param0).enable.disable();
    }

    public static void texGenMode(GlStateManager.TexGen param0, int param1) {
        GlStateManager.TexGenCoord var0 = getTexGen(param0);
        if (param1 != var0.mode) {
            var0.mode = param1;
            GL11.glTexGeni(var0.coord, 9472, param1);
        }

    }

    public static void texGenParam(GlStateManager.TexGen param0, int param1, FloatBuffer param2) {
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

    public static void activeTexture(int param0) {
        if (activeTexture != param0 - GLX.GL_TEXTURE0) {
            activeTexture = param0 - GLX.GL_TEXTURE0;
            GLX.glActiveTexture(param0);
        }

    }

    public static void enableTexture() {
        TEXTURES[activeTexture].enable.enable();
    }

    public static void disableTexture() {
        TEXTURES[activeTexture].enable.disable();
    }

    public static void texEnv(int param0, int param1, FloatBuffer param2) {
        GL11.glTexEnvfv(param0, param1, param2);
    }

    public static void texEnv(int param0, int param1, int param2) {
        GL11.glTexEnvi(param0, param1, param2);
    }

    public static void texEnv(int param0, int param1, float param2) {
        GL11.glTexEnvf(param0, param1, param2);
    }

    public static void texParameter(int param0, int param1, float param2) {
        GL11.glTexParameterf(param0, param1, param2);
    }

    public static void texParameter(int param0, int param1, int param2) {
        GL11.glTexParameteri(param0, param1, param2);
    }

    public static int getTexLevelParameter(int param0, int param1, int param2) {
        return GL11.glGetTexLevelParameteri(param0, param1, param2);
    }

    public static int genTexture() {
        return GL11.glGenTextures();
    }

    public static void deleteTexture(int param0) {
        GL11.glDeleteTextures(param0);

        for(GlStateManager.TextureState var0 : TEXTURES) {
            if (var0.binding == param0) {
                var0.binding = -1;
            }
        }

    }

    public static void bindTexture(int param0) {
        if (param0 != TEXTURES[activeTexture].binding) {
            TEXTURES[activeTexture].binding = param0;
            GL11.glBindTexture(3553, param0);
        }

    }

    public static void texImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, @Nullable IntBuffer param8) {
        GL11.glTexImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void texSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, long param8) {
        GL11.glTexSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void copyTexSubImage2D(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        GL11.glCopyTexSubImage2D(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void getTexImage(int param0, int param1, int param2, int param3, long param4) {
        GL11.glGetTexImage(param0, param1, param2, param3, param4);
    }

    public static void enableNormalize() {
        NORMALIZE.enable();
    }

    public static void disableNormalize() {
        NORMALIZE.disable();
    }

    public static void shadeModel(int param0) {
        if (param0 != shadeModel) {
            shadeModel = param0;
            GL11.glShadeModel(param0);
        }

    }

    public static void enableRescaleNormal() {
        RESCALE_NORMAL.enable();
    }

    public static void disableRescaleNormal() {
        RESCALE_NORMAL.disable();
    }

    public static void viewport(int param0, int param1, int param2, int param3) {
        GlStateManager.Viewport.INSTANCE.x = param0;
        GlStateManager.Viewport.INSTANCE.y = param1;
        GlStateManager.Viewport.INSTANCE.width = param2;
        GlStateManager.Viewport.INSTANCE.height = param3;
        GL11.glViewport(param0, param1, param2, param3);
    }

    public static void colorMask(boolean param0, boolean param1, boolean param2, boolean param3) {
        if (param0 != COLOR_MASK.red || param1 != COLOR_MASK.green || param2 != COLOR_MASK.blue || param3 != COLOR_MASK.alpha) {
            COLOR_MASK.red = param0;
            COLOR_MASK.green = param1;
            COLOR_MASK.blue = param2;
            COLOR_MASK.alpha = param3;
            GL11.glColorMask(param0, param1, param2, param3);
        }

    }

    public static void stencilFunc(int param0, int param1, int param2) {
        if (param0 != STENCIL.func.func || param0 != STENCIL.func.ref || param0 != STENCIL.func.mask) {
            STENCIL.func.func = param0;
            STENCIL.func.ref = param1;
            STENCIL.func.mask = param2;
            GL11.glStencilFunc(param0, param1, param2);
        }

    }

    public static void stencilMask(int param0) {
        if (param0 != STENCIL.mask) {
            STENCIL.mask = param0;
            GL11.glStencilMask(param0);
        }

    }

    public static void stencilOp(int param0, int param1, int param2) {
        if (param0 != STENCIL.fail || param1 != STENCIL.zfail || param2 != STENCIL.zpass) {
            STENCIL.fail = param0;
            STENCIL.zfail = param1;
            STENCIL.zpass = param2;
            GL11.glStencilOp(param0, param1, param2);
        }

    }

    public static void clearDepth(double param0) {
        if (param0 != CLEAR.depth) {
            CLEAR.depth = param0;
            GL11.glClearDepth(param0);
        }

    }

    public static void clearColor(float param0, float param1, float param2, float param3) {
        if (param0 != CLEAR.color.r || param1 != CLEAR.color.g || param2 != CLEAR.color.b || param3 != CLEAR.color.a) {
            CLEAR.color.r = param0;
            CLEAR.color.g = param1;
            CLEAR.color.b = param2;
            CLEAR.color.a = param3;
            GL11.glClearColor(param0, param1, param2, param3);
        }

    }

    public static void clearStencil(int param0) {
        if (param0 != CLEAR.stencil) {
            CLEAR.stencil = param0;
            GL11.glClearStencil(param0);
        }

    }

    public static void clear(int param0, boolean param1) {
        GL11.glClear(param0);
        if (param1) {
            getError();
        }

    }

    public static void matrixMode(int param0) {
        GL11.glMatrixMode(param0);
    }

    public static void loadIdentity() {
        GL11.glLoadIdentity();
    }

    public static void pushMatrix() {
        GL11.glPushMatrix();
    }

    public static void popMatrix() {
        GL11.glPopMatrix();
    }

    public static void getMatrix(int param0, FloatBuffer param1) {
        GL11.glGetFloatv(param0, param1);
    }

    public static Matrix4f getMatrix4f(int param0) {
        GL11.glGetFloatv(param0, MATRIX_BUFFER);
        ((Buffer)MATRIX_BUFFER).rewind();
        Matrix4f var0 = new Matrix4f();
        var0.load(MATRIX_BUFFER);
        ((Buffer)MATRIX_BUFFER).rewind();
        return var0;
    }

    public static void ortho(double param0, double param1, double param2, double param3, double param4, double param5) {
        GL11.glOrtho(param0, param1, param2, param3, param4, param5);
    }

    public static void rotatef(float param0, float param1, float param2, float param3) {
        GL11.glRotatef(param0, param1, param2, param3);
    }

    public static void rotated(double param0, double param1, double param2, double param3) {
        GL11.glRotated(param0, param1, param2, param3);
    }

    public static void scalef(float param0, float param1, float param2) {
        GL11.glScalef(param0, param1, param2);
    }

    public static void scaled(double param0, double param1, double param2) {
        GL11.glScaled(param0, param1, param2);
    }

    public static void translatef(float param0, float param1, float param2) {
        GL11.glTranslatef(param0, param1, param2);
    }

    public static void translated(double param0, double param1, double param2) {
        GL11.glTranslated(param0, param1, param2);
    }

    public static void multMatrix(FloatBuffer param0) {
        GL11.glMultMatrixf(param0);
    }

    public static void multMatrix(Matrix4f param0) {
        param0.store(MATRIX_BUFFER);
        ((Buffer)MATRIX_BUFFER).rewind();
        GL11.glMultMatrixf(MATRIX_BUFFER);
    }

    public static void color4f(float param0, float param1, float param2, float param3) {
        if (param0 != COLOR.r || param1 != COLOR.g || param2 != COLOR.b || param3 != COLOR.a) {
            COLOR.r = param0;
            COLOR.g = param1;
            COLOR.b = param2;
            COLOR.a = param3;
            GL11.glColor4f(param0, param1, param2, param3);
        }

    }

    public static void color3f(float param0, float param1, float param2) {
        color4f(param0, param1, param2, 1.0F);
    }

    public static void texCoord2f(float param0, float param1) {
        GL11.glTexCoord2f(param0, param1);
    }

    public static void vertex3f(float param0, float param1, float param2) {
        GL11.glVertex3f(param0, param1, param2);
    }

    public static void clearCurrentColor() {
        COLOR.r = -1.0F;
        COLOR.g = -1.0F;
        COLOR.b = -1.0F;
        COLOR.a = -1.0F;
    }

    public static void normalPointer(int param0, int param1, int param2) {
        GL11.glNormalPointer(param0, param1, (long)param2);
    }

    public static void normalPointer(int param0, int param1, ByteBuffer param2) {
        GL11.glNormalPointer(param0, param1, param2);
    }

    public static void texCoordPointer(int param0, int param1, int param2, int param3) {
        GL11.glTexCoordPointer(param0, param1, param2, (long)param3);
    }

    public static void texCoordPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GL11.glTexCoordPointer(param0, param1, param2, param3);
    }

    public static void vertexPointer(int param0, int param1, int param2, int param3) {
        GL11.glVertexPointer(param0, param1, param2, (long)param3);
    }

    public static void vertexPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GL11.glVertexPointer(param0, param1, param2, param3);
    }

    public static void colorPointer(int param0, int param1, int param2, int param3) {
        GL11.glColorPointer(param0, param1, param2, (long)param3);
    }

    public static void colorPointer(int param0, int param1, int param2, ByteBuffer param3) {
        GL11.glColorPointer(param0, param1, param2, param3);
    }

    public static void disableClientState(int param0) {
        GL11.glDisableClientState(param0);
    }

    public static void enableClientState(int param0) {
        GL11.glEnableClientState(param0);
    }

    public static void begin(int param0) {
        GL11.glBegin(param0);
    }

    public static void end() {
        GL11.glEnd();
    }

    public static void drawArrays(int param0, int param1, int param2) {
        GL11.glDrawArrays(param0, param1, param2);
    }

    public static void lineWidth(float param0) {
        GL11.glLineWidth(param0);
    }

    public static void callList(int param0) {
        GL11.glCallList(param0);
    }

    public static void deleteLists(int param0, int param1) {
        GL11.glDeleteLists(param0, param1);
    }

    public static void newList(int param0, int param1) {
        GL11.glNewList(param0, param1);
    }

    public static void endList() {
        GL11.glEndList();
    }

    public static int genLists(int param0) {
        return GL11.glGenLists(param0);
    }

    public static void pixelStore(int param0, int param1) {
        GL11.glPixelStorei(param0, param1);
    }

    public static void pixelTransfer(int param0, float param1) {
        GL11.glPixelTransferf(param0, param1);
    }

    public static void readPixels(int param0, int param1, int param2, int param3, int param4, int param5, ByteBuffer param6) {
        GL11.glReadPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static void readPixels(int param0, int param1, int param2, int param3, int param4, int param5, long param6) {
        GL11.glReadPixels(param0, param1, param2, param3, param4, param5, param6);
    }

    public static int getError() {
        return GL11.glGetError();
    }

    public static String getString(int param0) {
        return GL11.glGetString(param0);
    }

    public static void getInteger(int param0, IntBuffer param1) {
        GL11.glGetIntegerv(param0, param1);
    }

    public static int getInteger(int param0) {
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
                GlStateManager.disableAlphaTest();
                GlStateManager.alphaFunc(519, 0.0F);
                GlStateManager.disableLighting();
                GlStateManager.lightModel(2899, Lighting.getBuffer(0.2F, 0.2F, 0.2F, 1.0F));

                for(int var0 = 0; var0 < 8; ++var0) {
                    GlStateManager.disableLight(var0);
                    GlStateManager.light(16384 + var0, 4608, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                    GlStateManager.light(16384 + var0, 4611, Lighting.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
                    if (var0 == 0) {
                        GlStateManager.light(16384 + var0, 4609, Lighting.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                        GlStateManager.light(16384 + var0, 4610, Lighting.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                    } else {
                        GlStateManager.light(16384 + var0, 4609, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                        GlStateManager.light(16384 + var0, 4610, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                    }
                }

                GlStateManager.disableColorMaterial();
                GlStateManager.colorMaterial(1032, 5634);
                GlStateManager.disableDepthTest();
                GlStateManager.depthFunc(513);
                GlStateManager.depthMask(true);
                GlStateManager.disableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.blendFuncSeparate(
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
                );
                GlStateManager.blendEquation(32774);
                GlStateManager.disableFog();
                GlStateManager.fogi(2917, 2048);
                GlStateManager.fogDensity(1.0F);
                GlStateManager.fogStart(0.0F);
                GlStateManager.fogEnd(1.0F);
                GlStateManager.fog(2918, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                if (GL.getCapabilities().GL_NV_fog_distance) {
                    GlStateManager.fogi(2917, 34140);
                }

                GlStateManager.polygonOffset(0.0F, 0.0F);
                GlStateManager.disableColorLogicOp();
                GlStateManager.logicOp(5379);
                GlStateManager.disableTexGen(GlStateManager.TexGen.S);
                GlStateManager.texGenMode(GlStateManager.TexGen.S, 9216);
                GlStateManager.texGenParam(GlStateManager.TexGen.S, 9474, Lighting.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.texGenParam(GlStateManager.TexGen.S, 9217, Lighting.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.disableTexGen(GlStateManager.TexGen.T);
                GlStateManager.texGenMode(GlStateManager.TexGen.T, 9216);
                GlStateManager.texGenParam(GlStateManager.TexGen.T, 9474, Lighting.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
                GlStateManager.texGenParam(GlStateManager.TexGen.T, 9217, Lighting.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
                GlStateManager.disableTexGen(GlStateManager.TexGen.R);
                GlStateManager.texGenMode(GlStateManager.TexGen.R, 9216);
                GlStateManager.texGenParam(GlStateManager.TexGen.R, 9474, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.texGenParam(GlStateManager.TexGen.R, 9217, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.disableTexGen(GlStateManager.TexGen.Q);
                GlStateManager.texGenMode(GlStateManager.TexGen.Q, 9216);
                GlStateManager.texGenParam(GlStateManager.TexGen.Q, 9474, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.texGenParam(GlStateManager.TexGen.Q, 9217, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.activeTexture(0);
                GlStateManager.texParameter(3553, 10240, 9729);
                GlStateManager.texParameter(3553, 10241, 9986);
                GlStateManager.texParameter(3553, 10242, 10497);
                GlStateManager.texParameter(3553, 10243, 10497);
                GlStateManager.texParameter(3553, 33085, 1000);
                GlStateManager.texParameter(3553, 33083, 1000);
                GlStateManager.texParameter(3553, 33082, -1000);
                GlStateManager.texParameter(3553, 34049, 0.0F);
                GlStateManager.texEnv(8960, 8704, 8448);
                GlStateManager.texEnv(8960, 8705, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.texEnv(8960, 34161, 8448);
                GlStateManager.texEnv(8960, 34162, 8448);
                GlStateManager.texEnv(8960, 34176, 5890);
                GlStateManager.texEnv(8960, 34177, 34168);
                GlStateManager.texEnv(8960, 34178, 34166);
                GlStateManager.texEnv(8960, 34184, 5890);
                GlStateManager.texEnv(8960, 34185, 34168);
                GlStateManager.texEnv(8960, 34186, 34166);
                GlStateManager.texEnv(8960, 34192, 768);
                GlStateManager.texEnv(8960, 34193, 768);
                GlStateManager.texEnv(8960, 34194, 770);
                GlStateManager.texEnv(8960, 34200, 770);
                GlStateManager.texEnv(8960, 34201, 770);
                GlStateManager.texEnv(8960, 34202, 770);
                GlStateManager.texEnv(8960, 34163, 1.0F);
                GlStateManager.texEnv(8960, 3356, 1.0F);
                GlStateManager.disableNormalize();
                GlStateManager.shadeModel(7425);
                GlStateManager.disableRescaleNormal();
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.clearDepth(1.0);
                GlStateManager.lineWidth(1.0F);
                GlStateManager.normal3f(0.0F, 0.0F, 1.0F);
                GlStateManager.polygonMode(1028, 6914);
                GlStateManager.polygonMode(1029, 6914);
            }

            @Override
            public void clean() {
            }
        },
        PLAYER_SKIN {
            @Override
            public void apply() {
                GlStateManager.enableBlend();
                GlStateManager.blendFuncSeparate(770, 771, 1, 0);
            }

            @Override
            public void clean() {
                GlStateManager.disableBlend();
            }
        },
        TRANSPARENT_MODEL {
            @Override
            public void apply() {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.15F);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.alphaFunc(516, 0.003921569F);
            }

            @Override
            public void clean() {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.depthMask(true);
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
