package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
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

        if (var0 > -1 && var0 / 5 % 2 == 0) {
            renderWhiteSolidBlock(param2, param3, param4, param5);
        } else {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(param2, param3, param4, param5, 0, 10);
        }

    }

    public static void renderWhiteSolidBlock(BlockState param0, PoseStack param1, MultiBufferSource param2, int param3) {
        VertexConsumer var0 = param2.getBuffer(RenderType.NEW_ENTITY(TextureAtlas.LOCATION_BLOCKS));
        var0.defaultOverlayCoords(OverlayTexture.u(1.0F), 10);
        Minecraft.getInstance()
            .getBlockRenderer()
            .renderSingleBlock(param0, param1, param2x -> param2x == RenderType.SOLID ? var0 : param2.getBuffer(param2x), param3, 0, 10);
        var0.unsetDefaultOverlayCoords();
    }
}
