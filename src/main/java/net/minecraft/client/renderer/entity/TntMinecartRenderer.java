package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT> {
    public TntMinecartRenderer(EntityRenderDispatcher param0) {
        super(param0);
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

        renderWhiteSolidBlock(param2, param3, param4, param5, var0 > -1 && var0 / 5 % 2 == 0);
    }

    public static void renderWhiteSolidBlock(BlockState param0, PoseStack param1, MultiBufferSource param2, int param3, boolean param4) {
        int var0;
        if (param4) {
            var0 = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        } else {
            var0 = OverlayTexture.NO_OVERLAY;
        }

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(param0, param1, param2, param3, var0);
    }
}
