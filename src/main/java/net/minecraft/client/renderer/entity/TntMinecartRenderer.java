package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT> {
    public TntMinecartRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    protected void renderMinecartContents(MinecartTNT param0, float param1, BlockState param2) {
        int var0 = param0.getFuse();
        if (var0 > -1 && (float)var0 - param1 + 1.0F < 10.0F) {
            float var1 = 1.0F - ((float)var0 - param1 + 1.0F) / 10.0F;
            var1 = Mth.clamp(var1, 0.0F, 1.0F);
            var1 *= var1;
            var1 *= var1;
            float var2 = 1.0F + var1 * 0.3F;
            GlStateManager.scalef(var2, var2, var2);
        }

        super.renderMinecartContents(param0, param1, param2);
        if (var0 > -1 && var0 / 5 % 2 == 0) {
            BlockRenderDispatcher var3 = Minecraft.getInstance().getBlockRenderer();
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, (1.0F - ((float)var0 - param1 + 1.0F) / 100.0F) * 0.8F);
            GlStateManager.pushMatrix();
            var3.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
        }

    }
}
