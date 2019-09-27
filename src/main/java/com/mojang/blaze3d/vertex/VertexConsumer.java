package com.mojang.blaze3d.vertex;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public interface VertexConsumer {
    VertexConsumer vertex(double var1, double var3, double var5);

    VertexConsumer color(int var1, int var2, int var3, int var4);

    VertexConsumer uv(float var1, float var2);

    VertexConsumer overlayCoords(int var1, int var2);

    VertexConsumer uv2(int var1, int var2);

    VertexConsumer normal(float var1, float var2, float var3);

    void endVertex();

    void defaultOverlayCoords(int var1, int var2);

    void unsetDefaultOverlayCoords();

    default VertexConsumer color(float param0, float param1, float param2, float param3) {
        return this.color((int)(param0 * 255.0F), (int)(param1 * 255.0F), (int)(param2 * 255.0F), (int)(param3 * 255.0F));
    }

    default VertexConsumer uv2(int param0) {
        return this.uv2(param0 & 65535, param0 >> 16 & 65535);
    }

    default void putBulkData(Matrix4f param0, BakedQuad param1, float param2, float param3, float param4, int param5) {
        this.putBulkData(param0, param1, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, param2, param3, param4, new int[]{param5, param5, param5, param5}, false);
    }

    default void putBulkData(Matrix4f param0, BakedQuad param1, float[] param2, float param3, float param4, float param5, int[] param6, boolean param7) {
        int[] var0 = param1.getVertices();
        Vec3i var1 = param1.getDirection().getNormal();
        int var2 = 8;
        int var3 = var0.length / 8;

        try (MemoryStack var4 = MemoryStack.stackPush()) {
            ByteBuffer var5 = var4.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer var6 = var5.asIntBuffer();

            for(int var7 = 0; var7 < var3; ++var7) {
                ((Buffer)var6).clear();
                var6.put(var0, var7 * 8, 8);
                float var8 = var5.getFloat(0);
                float var9 = var5.getFloat(4);
                float var10 = var5.getFloat(8);
                byte var14;
                byte var15;
                byte var16;
                if (param7) {
                    int var11 = var5.get(12) & 255;
                    int var12 = var5.get(13) & 255;
                    int var13 = var5.get(14) & 255;
                    var14 = (byte)((int)((float)var11 * param2[var7] * param3));
                    var15 = (byte)((int)((float)var12 * param2[var7] * param4));
                    var16 = (byte)((int)((float)var13 * param2[var7] * param5));
                } else {
                    var14 = (byte)((int)(255.0F * param2[var7] * param3));
                    var15 = (byte)((int)(255.0F * param2[var7] * param4));
                    var16 = (byte)((int)(255.0F * param2[var7] * param5));
                }

                int var20 = param6[var7];
                float var21 = var5.getFloat(16);
                float var22 = var5.getFloat(20);
                this.vertex(param0, var8, var9, var10);
                this.color(var14, var15, var16, 255);
                this.uv(var21, var22);
                this.uv2(var20);
                this.normal((float)var1.getX(), (float)var1.getY(), (float)var1.getZ());
                this.endVertex();
            }
        }

    }

    default VertexConsumer vertex(Matrix4f param0, float param1, float param2, float param3) {
        Vector4f var0 = new Vector4f(param1, param2, param3, 1.0F);
        var0.transform(param0);
        return this.vertex((double)var0.x(), (double)var0.y(), (double)var0.z());
    }
}
