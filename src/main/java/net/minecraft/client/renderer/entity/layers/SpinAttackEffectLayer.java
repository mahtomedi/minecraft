package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpinAttackEffectLayer<T extends LivingEntity> extends RenderLayer<T, PlayerModel<T>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
    private final ModelPart box = new ModelPart(64, 64, 0, 0);

    public SpinAttackEffectLayer(RenderLayerParent<T, PlayerModel<T>> param0) {
        super(param0);
        this.box.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F);
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (param3.isAutoSpinAttack()) {
            VertexConsumer var0 = param1.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

            for(int var1 = 0; var1 < 3; ++var1) {
                param0.pushPose();
                float var2 = param7 * (float)(-(45 + var1 * 5));
                param0.mulPose(Vector3f.YP.rotationDegrees(var2));
                float var3 = 0.75F * (float)var1;
                param0.scale(var3, var3, var3);
                param0.translate(0.0, (double)(-0.2F + 0.6F * (float)var1), 0.0);
                this.box.render(param0, var0, param2, OverlayTexture.NO_OVERLAY, null);
                param0.popPose();
            }

        }
    }
}
