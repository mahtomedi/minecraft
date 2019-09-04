package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends RenderLayer<T, WitchModel<T>> {
    public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        ItemStack var0 = param0.getMainHandItem();
        if (!var0.isEmpty()) {
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            if (this.getParentModel().young) {
                RenderSystem.translatef(0.0F, 0.625F, 0.0F);
                RenderSystem.rotatef(-20.0F, -1.0F, 0.0F, 0.0F);
                float var1 = 0.5F;
                RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            }

            this.getParentModel().getNose().translateTo(0.0625F);
            RenderSystem.translatef(-0.0625F, 0.53125F, 0.21875F);
            Item var2 = var0.getItem();
            if (Block.byItem(var2).defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
                RenderSystem.translatef(0.0F, 0.0625F, -0.25F);
                RenderSystem.rotatef(30.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(-5.0F, 0.0F, 1.0F, 0.0F);
                float var3 = 0.375F;
                RenderSystem.scalef(0.375F, -0.375F, 0.375F);
            } else if (var2 == Items.BOW) {
                RenderSystem.translatef(0.0F, 0.125F, -0.125F);
                RenderSystem.rotatef(-45.0F, 0.0F, 1.0F, 0.0F);
                float var4 = 0.625F;
                RenderSystem.scalef(0.625F, -0.625F, 0.625F);
                RenderSystem.rotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(-20.0F, 0.0F, 1.0F, 0.0F);
            } else {
                RenderSystem.translatef(0.1875F, 0.1875F, 0.0F);
                float var5 = 0.875F;
                RenderSystem.scalef(0.875F, 0.875F, 0.875F);
                RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.rotatef(-60.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(-30.0F, 0.0F, 0.0F, 1.0F);
            }

            RenderSystem.rotatef(-15.0F, 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(40.0F, 0.0F, 0.0F, 1.0F);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(param0, var0, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
