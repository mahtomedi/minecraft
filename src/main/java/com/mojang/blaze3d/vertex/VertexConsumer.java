package com.mojang.blaze3d.vertex;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public interface VertexConsumer {
    Logger LOGGER = LogManager.getLogger();

    VertexConsumer vertex(double var1, double var3, double var5);

    VertexConsumer color(int var1, int var2, int var3, int var4);

    VertexConsumer uv(float var1, float var2);

    VertexConsumer overlayCoords(int var1, int var2);

    VertexConsumer uv2(int var1, int var2);

    VertexConsumer normal(float var1, float var2, float var3);

    void endVertex();

    default VertexConsumer color(float param0, float param1, float param2, float param3) {
        return this.color((int)(param0 * 255.0F), (int)(param1 * 255.0F), (int)(param2 * 255.0F), (int)(param3 * 255.0F));
    }

    default VertexConsumer uv2(int param0) {
        return this.uv2(param0 & 65535, param0 >> 16 & 65535);
    }

    default VertexConsumer overlayCoords(int param0) {
        return this.overlayCoords(param0 & 65535, param0 >> 16 & 65535);
    }

    default void putBulkData(Matrix4f param0, Matrix3f param1, BakedQuad param2, float param3, float param4, float param5, int param6, int param7) {
        this.putBulkData(
            param0, param1, param2, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, param3, param4, param5, new int[]{param6, param6, param6, param6}, param7, false
        );
    }

    default void putBulkData(
        Matrix4f param0, Matrix3f param1, BakedQuad param2, float[] param3, float param4, float param5, float param6, int[] param7, int param8, boolean param9
    ) {
        int[] var0 = param2.getVertices();
        Vec3i var1 = param2.getDirection().getNormal();
        Vector3f var2 = new Vector3f((float)var1.getX(), (float)var1.getY(), (float)var1.getZ());
        var2.transform(param1);
        int var3 = 8;
        int var4 = var0.length / 8;

        try (MemoryStack var5 = MemoryStack.stackPush()) {
            ByteBuffer var6 = var5.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer var7 = var6.asIntBuffer();

            for(int var8 = 0; var8 < var4; ++var8) {
                ((Buffer)var7).clear();
                var7.put(var0, var8 * 8, 8);
                float var9 = var6.getFloat(0);
                float var10 = var6.getFloat(4);
                float var11 = var6.getFloat(8);
                byte var15;
                byte var16;
                byte var17;
                if (param9) {
                    int var12 = var6.get(12) & 255;
                    int var13 = var6.get(13) & 255;
                    int var14 = var6.get(14) & 255;
                    var15 = (byte)((int)((float)var12 * param3[var8] * param4));
                    var16 = (byte)((int)((float)var13 * param3[var8] * param5));
                    var17 = (byte)((int)((float)var14 * param3[var8] * param6));
                } else {
                    var15 = (byte)((int)(255.0F * param3[var8] * param4));
                    var16 = (byte)((int)(255.0F * param3[var8] * param5));
                    var17 = (byte)((int)(255.0F * param3[var8] * param6));
                }

                int var21 = param7[var8];
                float var22 = var6.getFloat(16);
                float var23 = var6.getFloat(20);
                this.vertex(param0, var9, var10, var11);
                this.color(var15, var16, var17, 255);
                this.uv(var22, var23);
                this.overlayCoords(param8);
                this.uv2(var21);
                this.normal(var2.x(), var2.y(), var2.z());
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
