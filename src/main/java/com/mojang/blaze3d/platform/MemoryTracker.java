package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MemoryTracker {
    public static synchronized ByteBuffer createByteBuffer(int param0) {
        return ByteBuffer.allocateDirect(param0).order(ByteOrder.nativeOrder());
    }
}
