package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    public ItemInHandLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        T param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        boolean var0 = param3.getMainArm() == HumanoidArm.RIGHT;
        ItemStack var1 = var0 ? param3.getOffhandItem() : param3.getMainHandItem();
        ItemStack var2 = var0 ? param3.getMainHandItem() : param3.getOffhandItem();
        if (!var1.isEmpty() || !var2.isEmpty()) {
            param0.pushPose();
            if (this.getParentModel().young) {
                float var3 = 0.5F;
                param0.translate(0.0, 0.75, 0.0);
                param0.scale(0.5F, 0.5F, 0.5F);
            }

            this.renderArmWithItem(param3, var2, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, param0, param1);
            this.renderArmWithItem(param3, var1, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, param0, param1);
            param0.popPose();
        }
    }

    private void renderArmWithItem(
        LivingEntity param0, ItemStack param1, ItemTransforms.TransformType param2, HumanoidArm param3, PoseStack param4, MultiBufferSource param5
    ) {
        if (!param1.isEmpty()) {
            param4.pushPose();
            this.getParentModel().translateToHand(0.0625F, param3, param4);
            if (param0.isCrouching()) {
                param4.translate(0.0, 0.2F, 0.0);
            }

            param4.mulPose(Vector3f.XP.rotation(-90.0F, true));
            param4.mulPose(Vector3f.YP.rotation(180.0F, true));
            boolean var0 = param3 == HumanoidArm.LEFT;
            param4.translate((double)((float)(var0 ? -1 : 1) / 16.0F), 0.125, -0.625);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(param0, param1, param2, var0, param4, param5);
            param4.popPose();
        }
    }
}
