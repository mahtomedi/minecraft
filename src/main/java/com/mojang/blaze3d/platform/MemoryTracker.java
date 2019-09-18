package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MemoryTracker {
    public static synchronized ByteBuffer createByteBuffer(int param0) {
        return ByteBuffer.allocateDirect(param0).order(ByteOrder.nativeOrder());
    }

    public static FloatBuffer createFloatBuffer(int param0) {
        return createByteBuffer(param0 << 2).asFloatBuffer();
    }
}
