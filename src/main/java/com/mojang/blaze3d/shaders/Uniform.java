package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Uniform extends AbstractUniform implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int UT_INT1 = 0;
    public static final int UT_INT2 = 1;
    public static final int UT_INT3 = 2;
    public static final int UT_INT4 = 3;
    public static final int UT_FLOAT1 = 4;
    public static final int UT_FLOAT2 = 5;
    public static final int UT_FLOAT3 = 6;
    public static final int UT_FLOAT4 = 7;
    public static final int UT_MAT2 = 8;
    public static final int UT_MAT3 = 9;
    public static final int UT_MAT4 = 10;
    private static final boolean TRANSPOSE_MATRICIES = false;
    private int location;
    private final int count;
    private final int type;
    private final IntBuffer intValues;
    private final FloatBuffer floatValues;
    private final String name;
    private boolean dirty;
    private final Shader parent;

    public Uniform(String param0, int param1, int param2, Shader param3) {
        this.name = param0;
        this.count = param2;
        this.type = param1;
        this.parent = param3;
        if (param1 <= 3) {
            this.intValues = MemoryUtil.memAllocInt(param2);
            this.floatValues = null;
        } else {
            this.intValues = null;
            this.floatValues = MemoryUtil.memAllocFloat(param2);
        }

        this.location = -1;
        this.markDirty();
    }

    public static int glGetUniformLocation(int param0, CharSequence param1) {
        return GlStateManager._glGetUniformLocation(param0, param1);
    }

    public static void uploadInteger(int param0, int param1) {
        RenderSystem.glUniform1i(param0, param1);
    }

    public static int glGetAttribLocation(int param0, CharSequence param1) {
        return GlStateManager._glGetAttribLocation(param0, param1);
    }

    public static void glBindAttribLocation(int param0, int param1, CharSequence param2) {
        GlStateManager._glBindAttribLocation(param0, param1, param2);
    }

    @Override
    public void close() {
        if (this.intValues != null) {
            MemoryUtil.memFree(this.intValues);
        }

        if (this.floatValues != null) {
            MemoryUtil.memFree(this.floatValues);
        }

    }

    private void markDirty() {
        this.dirty = true;
        if (this.parent != null) {
            this.parent.markDirty();
        }

    }

    public static int getTypeFromString(String param0) {
        int var0 = -1;
        if ("int".equals(param0)) {
            var0 = 0;
        } else if ("float".equals(param0)) {
            var0 = 4;
        } else if (param0.startsWith("matrix")) {
            if (param0.endsWith("2x2")) {
                var0 = 8;
            } else if (param0.endsWith("3x3")) {
                var0 = 9;
            } else if (param0.endsWith("4x4")) {
                var0 = 10;
            }
        }

        return var0;
    }

    public void setLocation(int param0) {
        this.location = param0;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public final void set(float param0) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.markDirty();
    }

    @Override
    public final void set(float param0, float param1) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.markDirty();
    }

    public final void set(int param0, float param1) {
        this.floatValues.position(0);
        this.floatValues.put(param0, param1);
        this.markDirty();
    }

    @Override
    public final void set(float param0, float param1, float param2) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.markDirty();
    }

    @Override
    public final void set(Vector3f param0) {
        this.floatValues.position(0);
        param0.get(this.floatValues);
        this.markDirty();
    }

    @Override
    public final void set(float param0, float param1, float param2, float param3) {
        this.floatValues.position(0);
        this.floatValues.put(param0);
        this.floatValues.put(param1);
        this.floatValues.put(param2);
        this.floatValues.put(param3);
        this.floatValues.flip();
        this.markDirty();
    }

    @Override
    public final void set(Vector4f param0) {
        this.floatValues.position(0);
        param0.get(this.floatValues);
        this.markDirty();
    }

    @Override
    public final void setSafe(float param0, float param1, float param2, float param3) {
        this.floatValues.position(0);
        if (this.type >= 4) {
            this.floatValues.put(0, param0);
        }

        if (this.type >= 5) {
            this.floatValues.put(1, param1);
        }

        if (this.type >= 6) {
            this.floatValues.put(2, param2);
        }

        if (this.type >= 7) {
            this.floatValues.put(3, param3);
        }

        this.markDirty();
    }

    @Override
    public final void setSafe(int param0, int param1, int param2, int param3) {
        this.intValues.position(0);
        if (this.type >= 0) {
            this.intValues.put(0, param0);
        }

        if (this.type >= 1) {
            this.intValues.put(1, param1);
        }

        if (this.type >= 2) {
            this.intValues.put(2, param2);
        }

        if (this.type >= 3) {
            this.intValues.put(3, param3);
        }

        this.markDirty();
    }

    @Override
    public final void set(int param0) {
        this.intValues.position(0);
        this.intValues.put(0, param0);
        this.markDirty();
    }

    @Override
    public final void set(int param0, int param1) {
        this.intValues.position(0);
        this.intValues.put(0, param0);
        this.intValues.put(1, param1);
        this.markDirty();
    }

    @Override
    public final void set(int param0, int param1, int param2) {
        this.intValues.position(0);
        this.intValues.put(0, param0);
        this.intValues.put(1, param1);
        this.intValues.put(2, param2);
        this.markDirty();
    }

    @Override
    public final void set(int param0, int param1, int param2, int param3) {
        this.intValues.position(0);
        this.intValues.put(0, param0);
        this.intValues.put(1, param1);
        this.intValues.put(2, param2);
        this.intValues.put(3, param3);
        this.markDirty();
    }

    @Override
    public final void set(float[] param0) {
        if (param0.length < this.count) {
            LOGGER.warn("Uniform.set called with a too-small value array (expected {}, got {}). Ignoring.", this.count, param0.length);
        } else {
            this.floatValues.position(0);
            this.floatValues.put(param0);
            this.floatValues.position(0);
            this.markDirty();
        }
    }

    @Override
    public final void setMat2x2(float param0, float param1, float param2, float param3) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.markDirty();
    }

    @Override
    public final void setMat2x3(float param0, float param1, float param2, float param3, float param4, float param5) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.markDirty();
    }

    @Override
    public final void setMat2x4(float param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.floatValues.put(6, param6);
        this.floatValues.put(7, param7);
        this.markDirty();
    }

    @Override
    public final void setMat3x2(float param0, float param1, float param2, float param3, float param4, float param5) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.markDirty();
    }

    @Override
    public final void setMat3x3(float param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.floatValues.put(6, param6);
        this.floatValues.put(7, param7);
        this.floatValues.put(8, param8);
        this.markDirty();
    }

    @Override
    public final void setMat3x4(
        float param0,
        float param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11
    ) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.floatValues.put(6, param6);
        this.floatValues.put(7, param7);
        this.floatValues.put(8, param8);
        this.floatValues.put(9, param9);
        this.floatValues.put(10, param10);
        this.floatValues.put(11, param11);
        this.markDirty();
    }

    @Override
    public final void setMat4x2(float param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.floatValues.put(6, param6);
        this.floatValues.put(7, param7);
        this.markDirty();
    }

    @Override
    public final void setMat4x3(
        float param0,
        float param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11
    ) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.floatValues.put(6, param6);
        this.floatValues.put(7, param7);
        this.floatValues.put(8, param8);
        this.floatValues.put(9, param9);
        this.floatValues.put(10, param10);
        this.floatValues.put(11, param11);
        this.markDirty();
    }

    @Override
    public final void setMat4x4(
        float param0,
        float param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11,
        float param12,
        float param13,
        float param14,
        float param15
    ) {
        this.floatValues.position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.floatValues.put(3, param3);
        this.floatValues.put(4, param4);
        this.floatValues.put(5, param5);
        this.floatValues.put(6, param6);
        this.floatValues.put(7, param7);
        this.floatValues.put(8, param8);
        this.floatValues.put(9, param9);
        this.floatValues.put(10, param10);
        this.floatValues.put(11, param11);
        this.floatValues.put(12, param12);
        this.floatValues.put(13, param13);
        this.floatValues.put(14, param14);
        this.floatValues.put(15, param15);
        this.markDirty();
    }

    @Override
    public final void set(Matrix4f param0) {
        this.floatValues.position(0);
        param0.get(this.floatValues);
        this.markDirty();
    }

    @Override
    public final void set(Matrix3f param0) {
        this.floatValues.position(0);
        param0.get(this.floatValues);
        this.markDirty();
    }

    public void upload() {
        if (!this.dirty) {
        }

        this.dirty = false;
        if (this.type <= 3) {
            this.uploadAsInteger();
        } else if (this.type <= 7) {
            this.uploadAsFloat();
        } else {
            if (this.type > 10) {
                LOGGER.warn("Uniform.upload called, but type value ({}) is not a valid type. Ignoring.", this.type);
                return;
            }

            this.uploadAsMatrix();
        }

    }

    private void uploadAsInteger() {
        this.intValues.rewind();
        switch(this.type) {
            case 0:
                RenderSystem.glUniform1(this.location, this.intValues);
                break;
            case 1:
                RenderSystem.glUniform2(this.location, this.intValues);
                break;
            case 2:
                RenderSystem.glUniform3(this.location, this.intValues);
                break;
            case 3:
                RenderSystem.glUniform4(this.location, this.intValues);
                break;
            default:
                LOGGER.warn("Uniform.upload called, but count value ({}) is  not in the range of 1 to 4. Ignoring.", this.count);
        }

    }

    private void uploadAsFloat() {
        this.floatValues.rewind();
        switch(this.type) {
            case 4:
                RenderSystem.glUniform1(this.location, this.floatValues);
                break;
            case 5:
                RenderSystem.glUniform2(this.location, this.floatValues);
                break;
            case 6:
                RenderSystem.glUniform3(this.location, this.floatValues);
                break;
            case 7:
                RenderSystem.glUniform4(this.location, this.floatValues);
                break;
            default:
                LOGGER.warn("Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.", this.count);
        }

    }

    private void uploadAsMatrix() {
        this.floatValues.clear();
        switch(this.type) {
            case 8:
                RenderSystem.glUniformMatrix2(this.location, false, this.floatValues);
                break;
            case 9:
                RenderSystem.glUniformMatrix3(this.location, false, this.floatValues);
                break;
            case 10:
                RenderSystem.glUniformMatrix4(this.location, false, this.floatValues);
        }

    }

    public int getLocation() {
        return this.location;
    }

    public int getCount() {
        return this.count;
    }

    public int getType() {
        return this.type;
    }

    public IntBuffer getIntBuffer() {
        return this.intValues;
    }

    public FloatBuffer getFloatBuffer() {
        return this.floatValues;
    }
}
