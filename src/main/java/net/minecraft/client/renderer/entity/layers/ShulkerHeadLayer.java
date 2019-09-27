package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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
        float param9,
        float param10
    ) {
        param0.pushPose();
        switch(param3.getAttachFace()) {
            case DOWN:
            default:
                break;
            case EAST:
                param0.mulPose(Vector3f.ZP.rotation(90.0F, true));
                param0.mulPose(Vector3f.XP.rotation(90.0F, true));
                param0.translate(1.0, -1.0, 0.0);
                param0.mulPose(Vector3f.YP.rotation(180.0F, true));
                break;
            case WEST:
                param0.mulPose(Vector3f.ZP.rotation(-90.0F, true));
                param0.mulPose(Vector3f.XP.rotation(90.0F, true));
                param0.translate(-1.0, -1.0, 0.0);
                param0.mulPose(Vector3f.YP.rotation(180.0F, true));
                break;
            case NORTH:
                param0.mulPose(Vector3f.XP.rotation(90.0F, true));
                param0.translate(0.0, -1.0, -1.0);
                break;
            case SOUTH:
                param0.mulPose(Vector3f.ZP.rotation(180.0F, true));
                param0.mulPose(Vector3f.XP.rotation(90.0F, true));
                param0.translate(0.0, -1.0, 1.0);
                break;
            case UP:
                param0.mulPose(Vector3f.XP.rotation(180.0F, true));
                param0.translate(0.0, -2.0, 0.0);
        }

        ModelPart var0 = this.getParentModel().getHead();
        var0.yRot = param8 * (float) (Math.PI / 180.0);
        var0.xRot = param9 * (float) (Math.PI / 180.0);
        DyeColor var1 = param3.getColor();
        ResourceLocation var2;
        if (var1 == null) {
            var2 = ShulkerRenderer.DEFAULT_TEXTURE_LOCATION;
        } else {
            var2 = ShulkerRenderer.TEXTURE_LOCATION[var1.getId()];
        }

        VertexConsumer var4 = param1.getBuffer(RenderType.NEW_ENTITY(var2));
        LivingEntityRenderer.setOverlayCoords(param3, var4, 0.0F);
        var0.render(param0, var4, param10, param2, null);
        var4.unsetDefaultOverlayCoords();
        param0.popPose();
    }
}
