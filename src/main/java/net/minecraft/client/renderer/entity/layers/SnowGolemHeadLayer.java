package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowGolemHeadLayer extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
    public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> param0) {
        super(param0);
    }

    public void render(SnowGolem param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (!param0.isInvisible() && param0.hasPumpkin()) {
            RenderSystem.pushMatrix();
            this.getParentModel().getHead().translateTo(0.0625F);
            float var0 = 0.625F;
            RenderSystem.translatef(0.0F, -0.34375F, 0.0F);
            RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
            RenderSystem.scalef(0.625F, -0.625F, -0.625F);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(param0, new ItemStack(Blocks.CARVED_PUMPKIN), ItemTransforms.TransformType.HEAD);
            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
