package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class Uniform extends AbstractUniform implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
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
        ((Buffer)this.floatValues).position(0);
        this.floatValues.put(0, param0);
        this.markDirty();
    }

    @Override
    public final void set(float param0, float param1) {
        ((Buffer)this.floatValues).position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.markDirty();
    }

    @Override
    public final void set(float param0, float param1, float param2) {
        ((Buffer)this.floatValues).position(0);
        this.floatValues.put(0, param0);
        this.floatValues.put(1, param1);
        this.floatValues.put(2, param2);
        this.markDirty();
    }

    @Override
    public final void set(Vector3f param0) {
        ((Buffer)this.floatValues).position(0);
        this.floatValues.put(0, param0.x());
        this.floatValues.put(1, param0.y());
        this.floatValues.put(2, param0.z());
        this.markDirty();
    }

    @Override
    public final void set(float param0, float param1, float param2, float param3) {
        ((Buffer)this.floatValues).position(0);
        this.floatValues.put(param0);
        this.floatValues.put(param1);
        this.floatValues.put(param2);
        this.floatValues.put(param3);
        ((Buffer)this.floatValues).flip();
        this.markDirty();
    }

    @Override
    public final void setSafe(float param0, float param1, float param2, float param3) {
        ((Buffer)this.floatValues).position(0);
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
        ((Buffer)this.intValues).position(0);
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
    public final void set(float[] param0) {
        if (param0.length < this.count) {
            LOGGER.warn("Uniform.set called with a too-small value array (expected {}, got {}). Ignoring.", this.count, param0.length);
        } else {
            ((Buffer)this.floatValues).position(0);
            this.floatValues.put(param0);
            ((Buffer)this.floatValues).position(0);
            this.markDirty();
        }
    }

    @Override
    public final void set(Matrix4f param0) {
        ((Buffer)this.floatValues).position(0);
        param0.store(this.floatValues);
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
        ((Buffer)this.intValues).rewind();
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
        ((Buffer)this.floatValues).rewind();
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
        ((Buffer)this.floatValues).clear();
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
}
