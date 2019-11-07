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

    default void putBulkData(PoseStack.Pose param0, BakedQuad param1, float param2, float param3, float param4, int param5, int param6) {
        this.putBulkData(param0, param1, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, param2, param3, param4, new int[]{param5, param5, param5, param5}, param6, false);
    }

    default void putBulkData(
        PoseStack.Pose param0, BakedQuad param1, float[] param2, float param3, float param4, float param5, int[] param6, int param7, boolean param8
    ) {
        int[] var0 = param1.getVertices();
        Vec3i var1 = param1.getDirection().getNormal();
        Vector3f var2 = new Vector3f((float)var1.getX(), (float)var1.getY(), (float)var1.getZ());
        Matrix4f var3 = param0.pose();
        var2.transform(param0.normal());
        int var4 = 8;
        int var5 = var0.length / 8;

        try (MemoryStack var6 = MemoryStack.stackPush()) {
            ByteBuffer var7 = var6.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer var8 = var7.asIntBuffer();

            for(int var9 = 0; var9 < var5; ++var9) {
                ((Buffer)var8).clear();
                var8.put(var0, var9 * 8, 8);
                float var10 = var7.getFloat(0);
                float var11 = var7.getFloat(4);
                float var12 = var7.getFloat(8);
                byte var16;
                byte var17;
                byte var18;
                if (param8) {
                    int var13 = var7.get(12) & 255;
                    int var14 = var7.get(13) & 255;
                    int var15 = var7.get(14) & 255;
                    var16 = (byte)((int)((float)var13 * param2[var9] * param3));
                    var17 = (byte)((int)((float)var14 * param2[var9] * param4));
                    var18 = (byte)((int)((float)var15 * param2[var9] * param5));
                } else {
                    var16 = (byte)((int)(255.0F * param2[var9] * param3));
                    var17 = (byte)((int)(255.0F * param2[var9] * param4));
                    var18 = (byte)((int)(255.0F * param2[var9] * param5));
                }

                int var22 = param6[var9];
                float var23 = var7.getFloat(16);
                float var24 = var7.getFloat(20);
                this.vertex(var3, var10, var11, var12);
                this.color(var16, var17, var18, 255);
                this.uv(var23, var24);
                this.overlayCoords(param7);
                this.uv2(var22);
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

    default VertexConsumer normal(Matrix3f param0, float param1, float param2, float param3) {
        Vector3f var0 = new Vector3f(param1, param2, param3);
        var0.transform(param0);
        return this.normal(var0.x(), var0.y(), var0.z());
    }
}
