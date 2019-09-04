package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
    public MobRenderer(EntityRenderDispatcher param0, M param1, float param2) {
        super(param0, param1, param2);
    }

    protected boolean shouldShowName(T param0) {
        return super.shouldShowName(param0) && (param0.shouldShowName() || param0.hasCustomName() && param0 == this.entityRenderDispatcher.crosshairPickEntity);
    }

    public boolean shouldRender(T param0, Culler param1, double param2, double param3, double param4) {
        if (super.shouldRender(param0, param1, param2, param3, param4)) {
            return true;
        } else {
            Entity var0 = param0.getLeashHolder();
            return var0 != null ? param1.isVisible(var0.getBoundingBoxForCulling()) : false;
        }
    }

    public void render(T param0, double param1, double param2, double param3, float param4, float param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        if (!this.solidRender) {
            this.renderLeash(param0, param1, param2, param3, param4, param5);
        }

    }

    protected void renderLeash(T param0, double param1, double param2, double param3, float param4, float param5) {
        Entity var0 = param0.getLeashHolder();
        if (var0 != null) {
            param2 -= (1.6 - (double)param0.getBbHeight()) * 0.5;
            Tesselator var1 = Tesselator.getInstance();
            BufferBuilder var2 = var1.getBuilder();
            double var3 = (double)(Mth.lerp(param5 * 0.5F, var0.yRot, var0.yRotO) * (float) (Math.PI / 180.0));
            double var4 = (double)(Mth.lerp(param5 * 0.5F, var0.xRot, var0.xRotO) * (float) (Math.PI / 180.0));
            double var5 = Math.cos(var3);
            double var6 = Math.sin(var3);
            double var7 = Math.sin(var4);
            if (var0 instanceof HangingEntity) {
                var5 = 0.0;
                var6 = 0.0;
                var7 = -1.0;
            }

            double var8 = Math.cos(var4);
            double var9 = Mth.lerp((double)param5, var0.xo, var0.x) - var5 * 0.7 - var6 * 0.5 * var8;
            double var10 = Mth.lerp((double)param5, var0.yo + (double)var0.getEyeHeight() * 0.7, var0.y + (double)var0.getEyeHeight() * 0.7)
                - var7 * 0.5
                - 0.25;
            double var11 = Mth.lerp((double)param5, var0.zo, var0.z) - var6 * 0.7 + var5 * 0.5 * var8;
            double var12 = (double)(Mth.lerp(param5, param0.yBodyRot, param0.yBodyRotO) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
            var5 = Math.cos(var12) * (double)param0.getBbWidth() * 0.4;
            var6 = Math.sin(var12) * (double)param0.getBbWidth() * 0.4;
            double var13 = Mth.lerp((double)param5, param0.xo, param0.x) + var5;
            double var14 = Mth.lerp((double)param5, param0.yo, param0.y);
            double var15 = Mth.lerp((double)param5, param0.zo, param0.z) + var6;
            param1 += var5;
            param3 += var6;
            double var16 = (double)((float)(var9 - var13));
            double var17 = (double)((float)(var10 - var14));
            double var18 = (double)((float)(var11 - var15));
            RenderSystem.disableTexture();
            RenderSystem.disableLighting();
            RenderSystem.disableCull();
            int var19 = 24;
            double var20 = 0.025;
            var2.begin(5, DefaultVertexFormat.POSITION_COLOR);

            for(int var21 = 0; var21 <= 24; ++var21) {
                float var22 = 0.5F;
                float var23 = 0.4F;
                float var24 = 0.3F;
                if (var21 % 2 == 0) {
                    var22 *= 0.7F;
                    var23 *= 0.7F;
                    var24 *= 0.7F;
                }

                float var25 = (float)var21 / 24.0F;
                var2.vertex(
                        param1 + var16 * (double)var25 + 0.0,
                        param2 + var17 * (double)(var25 * var25 + var25) * 0.5 + (double)((24.0F - (float)var21) / 18.0F + 0.125F),
                        param3 + var18 * (double)var25
                    )
                    .color(var22, var23, var24, 1.0F)
                    .endVertex();
                var2.vertex(
                        param1 + var16 * (double)var25 + 0.025,
                        param2 + var17 * (double)(var25 * var25 + var25) * 0.5 + (double)((24.0F - (float)var21) / 18.0F + 0.125F) + 0.025,
                        param3 + var18 * (double)var25
                    )
                    .color(var22, var23, var24, 1.0F)
                    .endVertex();
            }

            var1.end();
            var2.begin(5, DefaultVertexFormat.POSITION_COLOR);

            for(int var26 = 0; var26 <= 24; ++var26) {
                float var27 = 0.5F;
                float var28 = 0.4F;
                float var29 = 0.3F;
                if (var26 % 2 == 0) {
                    var27 *= 0.7F;
                    var28 *= 0.7F;
                    var29 *= 0.7F;
                }

                float var30 = (float)var26 / 24.0F;
                var2.vertex(
                        param1 + var16 * (double)var30 + 0.0,
                        param2 + var17 * (double)(var30 * var30 + var30) * 0.5 + (double)((24.0F - (float)var26) / 18.0F + 0.125F) + 0.025,
                        param3 + var18 * (double)var30
                    )
                    .color(var27, var28, var29, 1.0F)
                    .endVertex();
                var2.vertex(
                        param1 + var16 * (double)var30 + 0.025,
                        param2 + var17 * (double)(var30 * var30 + var30) * 0.5 + (double)((24.0F - (float)var26) / 18.0F + 0.125F),
                        param3 + var18 * (double)var30 + 0.025
                    )
                    .color(var27, var28, var29, 1.0F)
                    .endVertex();
            }

            var1.end();
            RenderSystem.enableLighting();
            RenderSystem.enableTexture();
            RenderSystem.enableCull();
        }
    }
}
