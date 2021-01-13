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
        renderSide(var12, var13, var8, var9, var10, var19, var20, var21, var22, 0.025F, 0.025F, var15, var16);
        renderSide(var12, var13, var8, var9, var10, var19, var20, var21, var22, 0.025F, 0.0F, var15, var16);
        param2.popPose();
    }

    public static void renderSide(
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
        float param12
    ) {
        int var0 = 24;

        for(int var1 = 0; var1 < 24; ++var1) {
            float var2 = (float)var1 / 23.0F;
            int var3 = (int)Mth.lerp(var2, (float)param5, (float)param6);
            int var4 = (int)Mth.lerp(var2, (float)param7, (float)param8);
            int var5 = LightTexture.pack(var3, var4);
            addVertexPair(param0, param1, var5, param2, param3, param4, param9, param10, 24, var1, false, param11, param12);
            addVertexPair(param0, param1, var5, param2, param3, param4, param9, param10, 24, var1 + 1, true, param11, param12);
        }

    }

    public static void addVertexPair(
        VertexConsumer param0,
        Matrix4f param1,
        int param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        int param8,
        int param9,
        boolean param10,
        float param11,
        float param12
    ) {
        float var0 = 0.5F;
        float var1 = 0.4F;
        float var2 = 0.3F;
        if (param9 % 2 == 0) {
            var0 *= 0.7F;
            var1 *= 0.7F;
            var2 *= 0.7F;
        }

        float var3 = (float)param9 / (float)param8;
        float var4 = param3 * var3;
        float var5 = param4 > 0.0F ? param4 * var3 * var3 : param4 - param4 * (1.0F - var3) * (1.0F - var3);
        float var6 = param5 * var3;
        if (!param10) {
            param0.vertex(param1, var4 + param11, var5 + param6 - param7, var6 - param12).color(var0, var1, var2, 1.0F).uv2(param2).endVertex();
        }

        param0.vertex(param1, var4 - param11, var5 + param7, var6 + param12).color(var0, var1, var2, 1.0F).uv2(param2).endVertex();
        if (param10) {
            param0.vertex(param1, var4 + param11, var5 + param6 - param7, var6 - param12).color(var0, var1, var2, 1.0F).uv2(param2).endVertex();
        }

    }
}
