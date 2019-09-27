package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemFlowerLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
    public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        IronGolem param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        if (param3.getOfferFlowerTick() != 0) {
            param0.pushPose();
            param0.scale(-1.0F, -1.0F, 1.0F);
            param0.mulPose(Vector3f.XP.rotation(5.0F + 180.0F * this.getParentModel().getFlowerHoldingArm().xRot / (float) Math.PI, true));
            param0.mulPose(Vector3f.XP.rotation(90.0F, true));
            param0.translate(0.6875, -0.3125, 1.0625);
            float var0 = 0.5F;
            param0.scale(0.5F, 0.5F, 0.5F);
            param0.mulPose(Vector3f.XP.rotation(180.0F, true));
            param0.translate(-0.5, -0.5, 0.5);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.POPPY.defaultBlockState(), param0, param1, param2, 0, 10);
            param0.popPose();
        }
    }
}
