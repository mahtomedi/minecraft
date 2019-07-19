package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimatedEntityBlockRenderer {
    public void renderSingleBlock(Block param0, float param1) {
        GlStateManager.color4f(param1, param1, param1, 1.0F);
        GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
        EntityBlockRenderer.instance.renderByItem(new ItemStack(param0));
    }
}
