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

    default void vertex(
        float param0,
        float param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        int param9,
        int param10,
        float param11,
        float param12,
        float param13
    ) {
        this.vertex((double)param0, (double)param1, (double)param2);
        this.color(param3, param4, param5, param6);
        this.uv(param7, param8);
        this.overlayCoords(param9);
        this.uv2(param10);
        this.normal(param11, param12, param13);
        this.endVertex();
    }

    default VertexConsumer color(float param0, float param1, float param2, float param3) {
        return this.color((int)(param0 * 255.0F), (int)(param1 * 255.0F), (int)(param2 * 255.0F), (int)(param3 * 255.0F));
    }

    default VertexConsumer uv2(int param0) {
        return this.uv2(param0 & 65535, param0 >> 16 & 65535);
    }

    default VertexConsumer overlayCoords(int param0) {
        return this.overlayCoords(param0 & 65535, param0 >> 16 & 65535);
    }

    default void putBulkData(PoseStack.Pose param0, BakedQuad param1, float param2, float param3, float param4, int param5, int param6) {
        this.putBulkData(param0, param1, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, param2, param3, param4, new int[]{param5, param5, param5, param5}, param6, false);
    }

    default void putBulkData(
        PoseStack.Pose param0, BakedQuad param1, float[] param2, float param3, float param4, float param5, int[] param6, int param7, boolean param8
    ) {
        float[] var0 = new float[]{param2[0], param2[1], param2[2], param2[3]};
        int[] var1 = new int[]{param6[0], param6[1], param6[2], param6[3]};
        int[] var2 = param1.getVertices();
        Vec3i var3 = param1.getDirection().getNormal();
        Vector3f var4 = new Vector3f((float)var3.getX(), (float)var3.getY(), (float)var3.getZ());
        Matrix4f var5 = param0.pose();
        var4.transform(param0.normal());
        int var6 = 8;
        int var7 = var2.length / 8;

        try (MemoryStack var8 = MemoryStack.stackPush()) {
            ByteBuffer var9 = var8.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer var10 = var9.asIntBuffer();

            for(int var11 = 0; var11 < var7; ++var11) {
                ((Buffer)var10).clear();
                var10.put(var2, var11 * 8, 8);
                float var12 = var9.getFloat(0);
                float var13 = var9.getFloat(4);
                float var14 = var9.getFloat(8);
                float var18;
                float var19;
                float var20;
                if (param8) {
                    float var15 = (float)(var9.get(12) & 255) / 255.0F;
                    float var16 = (float)(var9.get(13) & 255) / 255.0F;
                    float var17 = (float)(var9.get(14) & 255) / 255.0F;
                    var18 = var15 * var0[var11] * param3;
                    var19 = var16 * var0[var11] * param4;
                    var20 = var17 * var0[var11] * param5;
                } else {
                    var18 = var0[var11] * param3;
                    var19 = var0[var11] * param4;
                    var20 = var0[var11] * param5;
                }

                int var24 = var1[var11];
                float var25 = var9.getFloat(16);
                float var26 = var9.getFloat(20);
                Vector4f var27 = new Vector4f(var12, var13, var14, 1.0F);
                var27.transform(var5);
                this.vertex(var27.x(), var27.y(), var27.z(), var18, var19, var20, 1.0F, var25, var26, param7, var24, var4.x(), var4.y(), var4.z());
            }
        }

    }

    default VertexConsumer vertex(Matrix4f param0, float param1, float param2, float param3) {
        Vector4f var0 = new Vector4f(param1, param2, param3, 1.0F);
        var0.transform(param0);
        return this.vertex((double)var0.x(), (double)var0.y(), (double)var0.z());
    }

    default VertexConsumer normal(Matrix3f param0, float param1, float param2, float param3) {
        Vector3f var0 = new Vector3f(param1, param2, param3);
        var0.transform(param0);
        return this.normal(var0.x(), var0.y(), var0.z());
    }
}
