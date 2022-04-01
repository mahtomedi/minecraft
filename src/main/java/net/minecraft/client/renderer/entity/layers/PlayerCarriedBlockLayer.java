package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerCarriedBlockLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerCarriedBlockLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> param0) {
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
        BlockState var0 = param3.getCarriedBlock();
        if (var0 != null) {
            param0.pushPose();
            Item var1 = GenericItemBlock.itemFromGenericBlock(var0);
            if (var1 != null) {
                ItemStack var2 = var1.getDefaultInstance();
                Minecraft.getInstance()
                    .getItemInHandRenderer()
                    .renderItem(param3, var2, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, param0, param1, OverlayTexture.NO_OVERLAY);
            } else {
                param0.translate(0.0, 0.6875, -0.75);
                param0.mulPose(Vector3f.XP.rotationDegrees(20.0F));
                param0.mulPose(Vector3f.YP.rotationDegrees(45.0F));
                param0.translate(0.125, 0.25, 0.5);
                float var3 = 0.625F;
                param0.scale(-0.625F, -0.625F, 0.625F);
                param0.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(var0, param0, param1, param2, OverlayTexture.NO_OVERLAY);
            }

            param0.popPose();
        }
    }
}
