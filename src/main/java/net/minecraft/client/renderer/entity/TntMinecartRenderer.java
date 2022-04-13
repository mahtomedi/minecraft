package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT> {
    private final BlockRenderDispatcher blockRenderer;

    public TntMinecartRenderer(EntityRendererProvider.Context param0) {
        super(param0, ModelLayers.TNT_MINECART);
        this.blockRenderer = param0.getBlockRenderDispatcher();
    }

    protected void renderMinecartContents(MinecartTNT param0, float param1, BlockState param2, PoseStack param3, MultiBufferSource param4, int param5) {
        int var0 = param0.getFuse();
        if (var0 > -1 && (float)var0 - param1 + 1.0F < 10.0F) {
            float var1 = 1.0F - ((float)var0 - param1 + 1.0F) / 10.0F;
            var1 = Mth.clamp(var1, 0.0F, 1.0F);
            var1 *= var1;
            var1 *= var1;
            float var2 = 1.0F + var1 * 0.3F;
            param3.scale(var2, var2, var2);
        }

        renderWhiteSolidBlock(this.blockRenderer, param2, param3, param4, param5, var0 > -1 && var0 / 5 % 2 == 0);
    }

    public static void renderWhiteSolidBlock(
        BlockRenderDispatcher param0, BlockState param1, PoseStack param2, MultiBufferSource param3, int param4, boolean param5
    ) {
        int var0;
        if (param5) {
            var0 = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        } else {
            var0 = OverlayTexture.NO_OVERLAY;
        }

        param0.renderSingleBlock(param1, param2, param3, param4, var0);
    }
}
