package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ArmedModel;
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

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        boolean var0 = param0.getMainArm() == HumanoidArm.RIGHT;
        ItemStack var1 = var0 ? param0.getOffhandItem() : param0.getMainHandItem();
        ItemStack var2 = var0 ? param0.getMainHandItem() : param0.getOffhandItem();
        if (!var1.isEmpty() || !var2.isEmpty()) {
            GlStateManager.pushMatrix();
            if (this.getParentModel().young) {
                float var3 = 0.5F;
                GlStateManager.translatef(0.0F, 0.75F, 0.0F);
                GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            }

            this.renderArmWithItem(param0, var2, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT);
            this.renderArmWithItem(param0, var1, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT);
            GlStateManager.popMatrix();
        }
    }

    private void renderArmWithItem(LivingEntity param0, ItemStack param1, ItemTransforms.TransformType param2, HumanoidArm param3) {
        if (!param1.isEmpty()) {
            GlStateManager.pushMatrix();
            this.translateToHand(param3);
            if (param0.isVisuallySneaking()) {
                GlStateManager.translatef(0.0F, 0.2F, 0.0F);
            }

            GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
            boolean var0 = param3 == HumanoidArm.LEFT;
            GlStateManager.translatef((float)(var0 ? -1 : 1) / 16.0F, 0.125F, -0.625F);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(param0, param1, param2, var0);
            GlStateManager.popMatrix();
        }
    }

    protected void translateToHand(HumanoidArm param0) {
        this.getParentModel().translateToHand(0.0625F, param0);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
