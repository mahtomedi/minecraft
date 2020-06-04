package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightningBoltRenderer extends EntityRenderer<LightningBolt> {
    public LightningBoltRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(LightningBolt param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        float[] var0 = new float[8];
        float[] var1 = new float[8];
        float var2 = 0.0F;
        float var3 = 0.0F;
        Random var4 = new Random(param0.seed);

        for(int var5 = 7; var5 >= 0; --var5) {
            var0[var5] = var2;
            var1[var5] = var3;
            var2 += (float)(var4.nextInt(11) - 5);
            var3 += (float)(var4.nextInt(11) - 5);
        }

        VertexConsumer var6 = param4.getBuffer(RenderType.lightning());
        Matrix4f var7 = param3.last().pose();

        for(int var8 = 0; var8 < 4; ++var8) {
            Random var9 = new Random(param0.seed);

            for(int var10 = 0; var10 < 3; ++var10) {
                int var11 = 7;
                int var12 = 0;
                if (var10 > 0) {
                    var11 = 7 - var10;
                }

                if (var10 > 0) {
                    var12 = var11 - 2;
                }

                float var13 = var0[var11] - var2;
                float var14 = var1[var11] - var3;

                for(int var15 = var11; var15 >= var12; --var15) {
                    float var16 = var13;
                    float var17 = var14;
                    if (var10 == 0) {
                        var13 += (float)(var9.nextInt(11) - 5);
                        var14 += (float)(var9.nextInt(11) - 5);
                    } else {
                        var13 += (float)(var9.nextInt(31) - 15);
                        var14 += (float)(var9.nextInt(31) - 15);
                    }

                    float var18 = 0.5F;
                    float var19 = 0.45F;
                    float var20 = 0.45F;
                    float var21 = 0.5F;
                    float var22 = 0.1F + (float)var8 * 0.2F;
                    if (var10 == 0) {
                        var22 = (float)((double)var22 * ((double)var15 * 0.1 + 1.0));
                    }

                    float var23 = 0.1F + (float)var8 * 0.2F;
                    if (var10 == 0) {
                        var23 *= (float)(var15 - 1) * 0.1F + 1.0F;
                    }

                    quad(var7, var6, var13, var14, var15, var16, var17, 0.45F, 0.45F, 0.5F, var22, var23, false, false, true, false);
                    quad(var7, var6, var13, var14, var15, var16, var17, 0.45F, 0.45F, 0.5F, var22, var23, true, false, true, true);
                    quad(var7, var6, var13, var14, var15, var16, var17, 0.45F, 0.45F, 0.5F, var22, var23, true, true, false, true);
                    quad(var7, var6, var13, var14, var15, var16, var17, 0.45F, 0.45F, 0.5F, var22, var23, false, true, false, false);
                }
            }
        }

    }

    private static void quad(
        Matrix4f param0,
        VertexConsumer param1,
        float param2,
        float param3,
        int param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11,
        boolean param12,
        boolean param13,
        boolean param14,
        boolean param15
    ) {
        param1.vertex(param0, param2 + (param12 ? param11 : -param11), (float)(param4 * 16), param3 + (param13 ? param11 : -param11))
            .color(param7, param8, param9, 0.3F)
            .endVertex();
        param1.vertex(param0, param5 + (param12 ? param10 : -param10), (float)((param4 + 1) * 16), param6 + (param13 ? param10 : -param10))
            .color(param7, param8, param9, 0.3F)
            .endVertex();
        param1.vertex(param0, param5 + (param14 ? param10 : -param10), (float)((param4 + 1) * 16), param6 + (param15 ? param10 : -param10))
            .color(param7, param8, param9, 0.3F)
            .endVertex();
        param1.vertex(param0, param2 + (param14 ? param11 : -param11), (float)(param4 * 16), param3 + (param15 ? param11 : -param11))
            .color(param7, param8, param9, 0.3F)
            .endVertex();
    }

    public ResourceLocation getTextureLocation(LightningBolt param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
