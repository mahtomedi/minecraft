package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
    private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

    public BeeStingerLayer(LivingEntityRenderer<T, M> param0) {
        super(param0);
    }

    @Override
    protected int numStuck(T param0) {
        return param0.getStingerCount();
    }

    @Override
    protected void renderStuckItem(
        PoseStack param0, MultiBufferSource param1, int param2, Entity param3, float param4, float param5, float param6, float param7
    ) {
        float var0 = Mth.sqrt(param4 * param4 + param6 * param6);
        float var1 = (float)(Math.atan2((double)param4, (double)param6) * 180.0F / (float)Math.PI);
        float var2 = (float)(Math.atan2((double)param5, (double)var0) * 180.0F / (float)Math.PI);
        param0.translate(0.0, 0.0, 0.0);
        param0.mulPose(Vector3f.YP.rotationDegrees(var1 - 90.0F));
        param0.mulPose(Vector3f.ZP.rotationDegrees(var2));
        float var3 = 0.0F;
        float var4 = 0.125F;
        float var5 = 0.0F;
        float var6 = 0.0625F;
        float var7 = 0.03125F;
        param0.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        param0.scale(0.03125F, 0.03125F, 0.03125F);
        param0.translate(2.5, 0.0, 0.0);
        VertexConsumer var8 = param1.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));

        for(int var9 = 0; var9 < 4; ++var9) {
            param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            PoseStack.Pose var10 = param0.last();
            Matrix4f var11 = var10.pose();
            Matrix3f var12 = var10.normal();
            vertex(var8, var11, var12, -4.5F, -1, 0.0F, 0.0F, param2);
            vertex(var8, var11, var12, 4.5F, -1, 0.125F, 0.0F, param2);
            vertex(var8, var11, var12, 4.5F, 1, 0.125F, 0.0625F, param2);
            vertex(var8, var11, var12, -4.5F, 1, 0.0F, 0.0625F, param2);
        }

    }

    private static void vertex(VertexConsumer param0, Matrix4f param1, Matrix3f param2, float param3, int param4, float param5, float param6, int param7) {
        param0.vertex(param1, param3, (float)param4, 0.0F)
            .color(255, 255, 255, 255)
            .uv(param5, param6)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param7)
            .normal(param2, 0.0F, 1.0F, 0.0F)
            .endVertex();
    }
}
