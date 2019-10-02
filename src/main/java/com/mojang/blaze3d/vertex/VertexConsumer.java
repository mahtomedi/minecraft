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
import net.minecraft.util.Mth;
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
        Vector3f var2 = new Vector3f((float)var1.getX(), (float)var1.getY(), (float)var1.getZ());
        Matrix3f var3 = new Matrix3f(param0);
        var3.transpose();
        float var4 = var3.adjugateAndDet();
        if (var4 < 1.0E-5F) {
            LOGGER.warn("Could not invert matrix while baking vertex: " + param0);
        } else {
            float var5 = var3.determinant();
            var3.mul(Mth.fastInvCubeRoot(var5));
        }

        var2.transform(var3);
        int var6 = 8;
        int var7 = var0.length / 8;

        try (MemoryStack var8 = MemoryStack.stackPush()) {
            ByteBuffer var9 = var8.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer var10 = var9.asIntBuffer();

            for(int var11 = 0; var11 < var7; ++var11) {
                ((Buffer)var10).clear();
                var10.put(var0, var11 * 8, 8);
                float var12 = var9.getFloat(0);
                float var13 = var9.getFloat(4);
                float var14 = var9.getFloat(8);
                byte var18;
                byte var19;
                byte var20;
                if (param7) {
                    int var15 = var9.get(12) & 255;
                    int var16 = var9.get(13) & 255;
                    int var17 = var9.get(14) & 255;
                    var18 = (byte)((int)((float)var15 * param2[var11] * param3));
                    var19 = (byte)((int)((float)var16 * param2[var11] * param4));
                    var20 = (byte)((int)((float)var17 * param2[var11] * param5));
                } else {
                    var18 = (byte)((int)(255.0F * param2[var11] * param3));
                    var19 = (byte)((int)(255.0F * param2[var11] * param4));
                    var20 = (byte)((int)(255.0F * param2[var11] * param5));
                }

                int var24 = param6[var11];
                float var25 = var9.getFloat(16);
                float var26 = var9.getFloat(20);
                this.vertex(param0, var12, var13, var14);
                this.color(var18, var19, var20, 255);
                this.uv(var25, var26);
                this.uv2(var24);
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
