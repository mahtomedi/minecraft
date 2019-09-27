package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
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

    public boolean shouldRender(T param0, Frustum param1, double param2, double param3, double param4) {
        if (super.shouldRender(param0, param1, param2, param3, param4)) {
            return true;
        } else {
            Entity var0 = param0.getLeashHolder();
            return var0 != null ? param1.isVisible(var0.getBoundingBoxForCulling()) : false;
        }
    }

    public void render(T param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
        Entity var0 = param0.getLeashHolder();
        if (var0 != null) {
            renderLeash(param0, param5, param6, param7, var0);
        }
    }

    public static void renderLeash(Mob param0, float param1, PoseStack param2, MultiBufferSource param3, Entity param4) {
        param2.pushPose();
        double var0 = (double)(Mth.lerp(param1 * 0.5F, param4.yRot, param4.yRotO) * (float) (Math.PI / 180.0));
        double var1 = (double)(Mth.lerp(param1 * 0.5F, param4.xRot, param4.xRotO) * (float) (Math.PI / 180.0));
        double var2 = Math.cos(var0);
        double var3 = Math.sin(var0);
        double var4 = Math.sin(var1);
        if (param4 instanceof HangingEntity) {
            var2 = 0.0;
            var3 = 0.0;
            var4 = -1.0;
        }

        double var5 = Math.cos(var1);
        double var6 = Mth.lerp((double)param1, param4.xo, param4.x) - var2 * 0.7 - var3 * 0.5 * var5;
        double var7 = Mth.lerp((double)param1, param4.yo + (double)param4.getEyeHeight() * 0.7, param4.y + (double)param4.getEyeHeight() * 0.7)
            - var4 * 0.5
            - 0.25;
        double var8 = Mth.lerp((double)param1, param4.zo, param4.z) - var3 * 0.7 + var2 * 0.5 * var5;
        double var9 = (double)(Mth.lerp(param1, param0.yBodyRot, param0.yBodyRotO) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
        var2 = Math.cos(var9) * (double)param0.getBbWidth() * 0.4;
        var3 = Math.sin(var9) * (double)param0.getBbWidth() * 0.4;
        double var10 = Mth.lerp((double)param1, param0.xo, param0.x) + var2;
        double var11 = Mth.lerp((double)param1, param0.yo, param0.y);
        double var12 = Mth.lerp((double)param1, param0.zo, param0.z) + var3;
        param2.translate(var2, -(1.6 - (double)param0.getBbHeight()) * 0.5, var3);
        float var13 = (float)(var6 - var10);
        float var14 = (float)(var7 - var11);
        float var15 = (float)(var8 - var12);
        float var16 = 0.025F;
        VertexConsumer var17 = param3.getBuffer(RenderType.LEASH);
        Matrix4f var18 = param2.getPose();
        float var19 = Mth.fastInvSqrt(var13 * var13 + var15 * var15) * 0.025F / 2.0F;
        float var20 = var15 * var19;
        float var21 = var13 * var19;
        renderSide(var17, var18, var13, var14, var15, 0.025F, 0.025F, var20, var21);
        renderSide(var17, var18, var13, var14, var15, 0.025F, 0.0F, var20, var21);
        param2.popPose();
    }

    public static void renderSide(
        VertexConsumer param0, Matrix4f param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8
    ) {
        int var0 = 24;

        for(int var1 = 0; var1 < 24; ++var1) {
            addVertexPair(param0, param1, param2, param3, param4, param5, param6, 24, var1, false, param7, param8);
            addVertexPair(param0, param1, param2, param3, param4, param5, param6, 24, var1 + 1, true, param7, param8);
        }

    }

    public static void addVertexPair(
        VertexConsumer param0,
        Matrix4f param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        int param7,
        int param8,
        boolean param9,
        float param10,
        float param11
    ) {
        float var0 = 0.5F;
        float var1 = 0.4F;
        float var2 = 0.3F;
        if (param8 % 2 == 0) {
            var0 *= 0.7F;
            var1 *= 0.7F;
            var2 *= 0.7F;
        }

        float var3 = (float)param8 / (float)param7;
        float var4 = param2 * var3;
        float var5 = param3 * (var3 * var3 + var3) * 0.5F + ((float)param7 - (float)param8) / ((float)param7 * 0.75F) + 0.125F;
        float var6 = param4 * var3;
        if (!param9) {
            param0.vertex(param1, var4 + param10, var5 + param5 - param6, var6 - param11).color(var0, var1, var2, 1.0F).endVertex();
        }

        param0.vertex(param1, var4 - param10, var5 + param6, var6 + param11).color(var0, var1, var2, 1.0F).endVertex();
        if (param9) {
            param0.vertex(param1, var4 + param10, var5 + param5 - param6, var6 - param11).color(var0, var1, var2, 1.0F).endVertex();
        }

    }
}
