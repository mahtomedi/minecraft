package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Deadmau5EarsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public Deadmau5EarsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        AbstractClientPlayer param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        if ("deadmau5".equals(param3.getName().getString()) && param3.isSkinLoaded() && !param3.isInvisible()) {
            VertexConsumer var0 = param1.getBuffer(RenderType.entitySolid(param3.getSkinTextureLocation()));
            int var1 = LivingEntityRenderer.getOverlayCoords(param3, 0.0F);

            for(int var2 = 0; var2 < 2; ++var2) {
                float var3 = Mth.lerp(param6, param3.yRotO, param3.getYRot()) - Mth.lerp(param6, param3.yBodyRotO, param3.yBodyRot);
                float var4 = Mth.lerp(param6, param3.xRotO, param3.getXRot());
                param0.pushPose();
                param0.mulPose(Vector3f.YP.rotationDegrees(var3));
                param0.mulPose(Vector3f.XP.rotationDegrees(var4));
                param0.translate((double)(0.375F * (float)(var2 * 2 - 1)), 0.0, 0.0);
                param0.translate(0.0, -0.375, 0.0);
                param0.mulPose(Vector3f.XP.rotationDegrees(-var4));
                param0.mulPose(Vector3f.YP.rotationDegrees(-var3));
                float var5 = 1.3333334F;
                param0.scale(1.3333334F, 1.3333334F, 1.3333334F);
                this.getParentModel().renderEars(param0, var0, param2, var1);
                param0.popPose();
            }

        }
    }
}
