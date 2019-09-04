package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightningBoltRenderer extends EntityRenderer<LightningBolt> {
    public LightningBoltRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(LightningBolt param0, double param1, double param2, double param3, float param4, float param5) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.disableTexture();
        RenderSystem.disableLighting();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        double[] var2 = new double[8];
        double[] var3 = new double[8];
        double var4 = 0.0;
        double var5 = 0.0;
        Random var6 = new Random(param0.seed);

        for(int var7 = 7; var7 >= 0; --var7) {
            var2[var7] = var4;
            var3[var7] = var5;
            var4 += (double)(var6.nextInt(11) - 5);
            var5 += (double)(var6.nextInt(11) - 5);
        }

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

                double var13 = var2[var11] - var4;
                double var14 = var3[var11] - var5;

                for(int var15 = var11; var15 >= var12; --var15) {
                    double var16 = var13;
                    double var17 = var14;
                    if (var10 == 0) {
                        var13 += (double)(var9.nextInt(11) - 5);
                        var14 += (double)(var9.nextInt(11) - 5);
                    } else {
                        var13 += (double)(var9.nextInt(31) - 15);
                        var14 += (double)(var9.nextInt(31) - 15);
                    }

                    var1.begin(5, DefaultVertexFormat.POSITION_COLOR);
                    float var18 = 0.5F;
                    float var19 = 0.45F;
                    float var20 = 0.45F;
                    float var21 = 0.5F;
                    double var22 = 0.1 + (double)var8 * 0.2;
                    if (var10 == 0) {
                        var22 *= (double)var15 * 0.1 + 1.0;
                    }

                    double var23 = 0.1 + (double)var8 * 0.2;
                    if (var10 == 0) {
                        var23 *= (double)(var15 - 1) * 0.1 + 1.0;
                    }

                    for(int var24 = 0; var24 < 5; ++var24) {
                        double var25 = param1 - var22;
                        double var26 = param3 - var22;
                        if (var24 == 1 || var24 == 2) {
                            var25 += var22 * 2.0;
                        }

                        if (var24 == 2 || var24 == 3) {
                            var26 += var22 * 2.0;
                        }

                        double var27 = param1 - var23;
                        double var28 = param3 - var23;
                        if (var24 == 1 || var24 == 2) {
                            var27 += var23 * 2.0;
                        }

                        if (var24 == 2 || var24 == 3) {
                            var28 += var23 * 2.0;
                        }

                        var1.vertex(var27 + var13, param2 + (double)(var15 * 16), var28 + var14).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                        var1.vertex(var25 + var16, param2 + (double)((var15 + 1) * 16), var26 + var17).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                    }

                    var0.end();
                }
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableLighting();
        RenderSystem.enableTexture();
    }

    @Nullable
    protected ResourceLocation getTextureLocation(LightningBolt param0) {
        return null;
    }
}
