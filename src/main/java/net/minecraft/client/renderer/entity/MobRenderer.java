package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
    public MobRenderer(EntityRendererProvider.Context param0, M param1, float param2) {
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

    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        Entity var0 = param0.getLeashHolder();
        if (var0 != null) {
            this.renderLeash(param0, param2, param3, param4, var0);
        }
    }

    private <E extends Entity> void renderLeash(T param0, float param1, PoseStack param2, MultiBufferSource param3, E param4) {
        param2.pushPose();
        Vec3 var0 = param4.getRopeHoldPosition(param1);
        double var1 = (double)(Mth.lerp(param1, param0.yBodyRot, param0.yBodyRotO) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
        Vec3 var2 = param0.getLeashOffset();
        double var3 = Math.cos(var1) * var2.z + Math.sin(var1) * var2.x;
        double var4 = Math.sin(var1) * var2.z - Math.cos(var1) * var2.x;
        double var5 = Mth.lerp((double)param1, param0.xo, param0.getX()) + var3;
        double var6 = Mth.lerp((double)param1, param0.yo, param0.getY()) + var2.y;
        double var7 = Mth.lerp((double)param1, param0.zo, param0.getZ()) + var4;
        param2.translate(var3, var2.y, var4);
        float var8 = (float)(var0.x - var5);
        float var9 = (float)(var0.y - var6);
        float var10 = (float)(var0.z - var7);
        float var11 = 0.025F;
        VertexConsumer var12 = param3.getBuffer(RenderType.leash());
        Matrix4f var13 = param2.last().pose();
        float var14 = Mth.fastInvSqrt(var8 * var8 + var10 * var10) * 0.025F / 2.0F;
        float var15 = var10 * var14;
        float var16 = var8 * var14;
        BlockPos var17 = new BlockPos(param0.getEyePosition(param1));
        BlockPos var18 = new BlockPos(param4.getEyePosition(param1));
        int var19 = this.getBlockLightLevel(param0, var17);
        int var20 = this.entityRenderDispatcher.getRenderer(param4).getBlockLightLevel(param4, var18);
        int var21 = param0.level.getBrightness(LightLayer.SKY, var17);
        int var22 = param0.level.getBrightness(LightLayer.SKY, var18);

        for(int var23 = 0; var23 <= 24; ++var23) {
            addVertexPair(var12, var13, var8, var9, var10, var19, var20, var21, var22, 0.025F, 0.025F, var15, var16, var23, false);
        }

        for(int var24 = 24; var24 >= 0; --var24) {
            addVertexPair(var12, var13, var8, var9, var10, var19, var20, var21, var22, 0.025F, 0.0F, var15, var16, var24, true);
        }

        param2.popPose();
    }

    private static void addVertexPair(
        VertexConsumer param0,
        Matrix4f param1,
        float param2,
        float param3,
        float param4,
        int param5,
        int param6,
        int param7,
        int param8,
        float param9,
        float param10,
        float param11,
        float param12,
        int param13,
        boolean param14
    ) {
        float var0 = (float)param13 / 24.0F;
        int var1 = (int)Mth.lerp(var0, (float)param5, (float)param6);
        int var2 = (int)Mth.lerp(var0, (float)param7, (float)param8);
        int var3 = LightTexture.pack(var1, var2);
        float var4 = param13 % 2 == (param14 ? 1 : 0) ? 0.7F : 1.0F;
        float var5 = 0.5F * var4;
        float var6 = 0.4F * var4;
        float var7 = 0.3F * var4;
        float var8 = param2 * var0;
        float var9 = param3 > 0.0F ? param3 * var0 * var0 : param3 - param3 * (1.0F - var0) * (1.0F - var0);
        float var10 = param4 * var0;
        param0.vertex(param1, var8 - param11, var9 + param10, var10 + param12).color(var5, var6, var7, 1.0F).uv2(var3).endVertex();
        param0.vertex(param1, var8 + param11, var9 + param9 - param10, var10 - param12).color(var5, var6, var7, 1.0F).uv2(var3).endVertex();
    }
}
