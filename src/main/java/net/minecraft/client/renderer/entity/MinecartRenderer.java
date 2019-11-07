package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinecartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
    private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
    protected final EntityModel<T> model = new MinecartModel<>();

    public MinecartRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.7F;
    }

    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        param3.pushPose();
        long var0 = (long)param0.getId() * 493286711L;
        var0 = var0 * var0 * 4392167121L + var0 * 98761L;
        float var1 = (((float)(var0 >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float var2 = (((float)(var0 >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float var3 = (((float)(var0 >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        param3.translate((double)var1, (double)var2, (double)var3);
        double var4 = Mth.lerp((double)param2, param0.xOld, param0.getX());
        double var5 = Mth.lerp((double)param2, param0.yOld, param0.getY());
        double var6 = Mth.lerp((double)param2, param0.zOld, param0.getZ());
        double var7 = 0.3F;
        Vec3 var8 = param0.getPos(var4, var5, var6);
        float var9 = Mth.lerp(param2, param0.xRotO, param0.xRot);
        if (var8 != null) {
            Vec3 var10 = param0.getPosOffs(var4, var5, var6, 0.3F);
            Vec3 var11 = param0.getPosOffs(var4, var5, var6, -0.3F);
            if (var10 == null) {
                var10 = var8;
            }

            if (var11 == null) {
                var11 = var8;
            }

            param3.translate(var8.x - var4, (var10.y + var11.y) / 2.0 - var5, var8.z - var6);
            Vec3 var12 = var11.add(-var10.x, -var10.y, -var10.z);
            if (var12.length() != 0.0) {
                var12 = var12.normalize();
                param1 = (float)(Math.atan2(var12.z, var12.x) * 180.0 / Math.PI);
                var9 = (float)(Math.atan(var12.y) * 73.0);
            }
        }

        param3.translate(0.0, 0.375, 0.0);
        param3.mulPose(Vector3f.YP.rotationDegrees(180.0F - param1));
        param3.mulPose(Vector3f.ZP.rotationDegrees(-var9));
        float var13 = (float)param0.getHurtTime() - param2;
        float var14 = param0.getDamage() - param2;
        if (var14 < 0.0F) {
            var14 = 0.0F;
        }

        if (var13 > 0.0F) {
            param3.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(var13) * var13 * var14 / 10.0F * (float)param0.getHurtDir()));
        }

        int var15 = param0.getDisplayOffset();
        BlockState var16 = param0.getDisplayBlockState();
        if (var16.getRenderShape() != RenderShape.INVISIBLE) {
            param3.pushPose();
            float var17 = 0.75F;
            param3.scale(0.75F, 0.75F, 0.75F);
            param3.translate(-0.5, (double)((float)(var15 - 8) / 16.0F), 0.5);
            param3.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            this.renderMinecartContents(param0, param2, var16, param3, param4, param5);
            param3.popPose();
        }

        param3.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(param0, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer var18 = param4.getBuffer(this.model.renderType(this.getTextureLocation(param0)));
        this.model.renderToBuffer(param3, var18, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
        param3.popPose();
    }

    public ResourceLocation getTextureLocation(T param0) {
        return MINECART_LOCATION;
    }

    protected void renderMinecartContents(T param0, float param1, BlockState param2, PoseStack param3, MultiBufferSource param4, int param5) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(param2, param3, param4, param5, OverlayTexture.NO_OVERLAY);
    }
}
