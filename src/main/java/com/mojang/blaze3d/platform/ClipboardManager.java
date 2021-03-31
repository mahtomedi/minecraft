package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class ClipboardManager {
    public static final int FORMAT_UNAVAILABLE = 65545;
    private final ByteBuffer clipboardScratchBuffer = BufferUtils.createByteBuffer(8192);

    public String getClipboard(long param0, GLFWErrorCallbackI param1) {
        GLFWErrorCallback var0 = GLFW.glfwSetErrorCallback(param1);
        String var1 = GLFW.glfwGetClipboardString(param0);
        var1 = var1 != null ? StringDecomposer.filterBrokenSurrogates(var1) : "";
        GLFWErrorCallback var2 = GLFW.glfwSetErrorCallback(var0);
        if (var2 != null) {
            var2.free();
        }

        return var1;
    }

    private static void pushClipboard(long param0, ByteBuffer param1, byte[] param2) {
        ((Buffer)param1).clear();
        param1.put(param2);
        param1.put((byte)0);
        ((Buffer)param1).flip();
        GLFW.glfwSetClipboardString(param0, param1);
    }

    public void setClipboard(long param0, String param1) {
        byte[] var0 = param1.getBytes(Charsets.UTF_8);
        int var1 = var0.length + 1;
        if (var1 < this.clipboardScratchBuffer.capacity()) {
            pushClipboard(param0, this.clipboardScratchBuffer, var0);
        } else {
            ByteBuffer var2 = MemoryUtil.memAlloc(var1);

            try {
                pushClipboard(param0, var2, var0);
            } finally {
                MemoryUtil.memFree(var2);
            }
        }

    }
}
