package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MemoryTracker {
    public static synchronized int genLists(int param0) {
        int var0 = RenderSystem.genLists(param0);
        if (var0 == 0) {
            int var1 = RenderSystem.getError();
            String var2 = "No error code reported";
            if (var1 != 0) {
                var2 = GLX.getErrorString(var1);
            }

            throw new IllegalStateException("glGenLists returned an ID of 0 for a count of " + param0 + ", GL error (" + var1 + "): " + var2);
        } else {
            return var0;
        }
    }

    public static synchronized void releaseLists(int param0, int param1) {
        RenderSystem.deleteLists(param0, param1);
    }

    public static synchronized void releaseList(int param0) {
        releaseLists(param0, 1);
    }

    public static synchronized ByteBuffer createByteBuffer(int param0) {
        return ByteBuffer.allocateDirect(param0).order(ByteOrder.nativeOrder());
    }

    public static FloatBuffer createFloatBuffer(int param0) {
        return createByteBuffer(param0 << 2).asFloatBuffer();
    }
}
