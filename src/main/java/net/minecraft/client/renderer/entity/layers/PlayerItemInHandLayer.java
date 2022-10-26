package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerItemInHandLayer<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;
    private static final float X_ROT_MIN = (float) (-Math.PI / 6);
    private static final float X_ROT_MAX = (float) (Math.PI / 2);

    public PlayerItemInHandLayer(RenderLayerParent<T, M> param0, ItemInHandRenderer param1) {
        super(param0, param1);
        this.itemInHandRenderer = param1;
    }

    @Override
    protected void renderArmWithItem(
        LivingEntity param0, ItemStack param1, ItemTransforms.TransformType param2, HumanoidArm param3, PoseStack param4, MultiBufferSource param5, int param6
    ) {
        if (param1.is(Items.SPYGLASS) && param0.getUseItem() == param1 && param0.swingTime == 0) {
            this.renderArmWithSpyglass(param0, param1, param3, param4, param5, param6);
        } else {
            super.renderArmWithItem(param0, param1, param2, param3, param4, param5, param6);
        }

    }

    private void renderArmWithSpyglass(LivingEntity param0, ItemStack param1, HumanoidArm param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        ModelPart var0 = this.getParentModel().getHead();
        float var1 = var0.xRot;
        var0.xRot = Mth.clamp(var0.xRot, (float) (-Math.PI / 6), (float) (Math.PI / 2));
        var0.translateAndRotate(param3);
        var0.xRot = var1;
        CustomHeadLayer.translateToHead(param3, false);
        boolean var2 = param2 == HumanoidArm.LEFT;
        param3.translate((var2 ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        this.itemInHandRenderer.renderItem(param0, param1, ItemTransforms.TransformType.HEAD, false, param3, param4, param5);
        param3.popPose();
    }
}
