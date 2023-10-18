package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryUtil.MemoryAllocator;

@OnlyIn(Dist.CLIENT)
public class MemoryTracker {
    private static final MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

    public static ByteBuffer create(int param0) {
        long var0 = ALLOCATOR.malloc((long)param0);
        if (var0 == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + param0 + " bytes");
        } else {
            return MemoryUtil.memByteBuffer(var0, param0);
        }
    }

    public static ByteBuffer resize(ByteBuffer param0, int param1) {
        long var0 = ALLOCATOR.realloc(MemoryUtil.memAddress0(param0), (long)param1);
        if (var0 == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + param0.capacity() + " bytes to " + param1 + " bytes");
        } else {
            return MemoryUtil.memByteBuffer(var0, param1);
        }
    }

    public static void free(ByteBuffer param0) {
        ALLOCATOR.free(MemoryUtil.memAddress0(param0));
    }
}
