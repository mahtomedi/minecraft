package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerHeadLayer extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
    public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Shulker param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        param0.pushPose();
        param0.translate(0.0, 1.0, 0.0);
        param0.scale(-1.0F, -1.0F, 1.0F);
        Quaternion var0 = param3.getAttachFace().getOpposite().getRotation();
        var0.conj();
        param0.mulPose(var0);
        param0.scale(-1.0F, -1.0F, 1.0F);
        param0.translate(0.0, -1.0, 0.0);
        ModelPart var1 = this.getParentModel().getHead();
        var1.yRot = param8 * (float) (Math.PI / 180.0);
        var1.xRot = param9 * (float) (Math.PI / 180.0);
        DyeColor var2 = param3.getColor();
        ResourceLocation var3;
        if (var2 == null) {
            var3 = ShulkerRenderer.DEFAULT_TEXTURE_LOCATION;
        } else {
            var3 = ShulkerRenderer.TEXTURE_LOCATION[var2.getId()];
        }

        VertexConsumer var5 = param1.getBuffer(RenderType.entitySolid(var3));
        var1.render(param0, var5, param2, LivingEntityRenderer.getOverlayCoords(param3, 0.0F));
        param0.popPose();
    }
}
