package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MemoryTracker {
    public static synchronized ByteBuffer createByteBuffer(int param0) {
        return ByteBuffer.allocateDirect(param0).order(ByteOrder.nativeOrder());
    }

    public static ShortBuffer createShortBuffer(int param0) {
        return createByteBuffer(param0 << 1).asShortBuffer();
    }

    public static CharBuffer createCharBuffer(int param0) {
        return createByteBuffer(param0 << 1).asCharBuffer();
    }

    public static IntBuffer createIntBuffer(int param0) {
        return createByteBuffer(param0 << 2).asIntBuffer();
    }

    public static LongBuffer createLongBuffer(int param0) {
        return createByteBuffer(param0 << 3).asLongBuffer();
    }

    public static FloatBuffer createFloatBuffer(int param0) {
        return createByteBuffer(param0 << 2).asFloatBuffer();
    }

    public static DoubleBuffer createDoubleBuffer(int param0) {
        return createByteBuffer(param0 << 3).asDoubleBuffer();
    }
}
