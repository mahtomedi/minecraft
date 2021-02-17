package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TheEndGatewayRenderer extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

    public TheEndGatewayRenderer(BlockEntityRendererProvider.Context param0) {
        super(param0);
    }

    public void render(TheEndGatewayBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        if (param0.isSpawning() || param0.isCoolingDown()) {
            float var0 = param0.isSpawning() ? param0.getSpawnPercent(param1) : param0.getCooldownPercent(param1);
            double var1 = param0.isSpawning() ? (double)param0.getLevel().getMaxBuildHeight() : 50.0;
            var0 = Mth.sin(var0 * (float) Math.PI);
            int var2 = Mth.floor((double)var0 * var1);
            float[] var3 = param0.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
            long var4 = param0.getLevel().getGameTime();
            BeaconRenderer.renderBeaconBeam(param2, param3, BEAM_LOCATION, param1, var0, var4, -var2, var2 * 2, var3, 0.15F, 0.175F);
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    @Override
    protected int getPasses(double param0) {
        return super.getPasses(param0) + 1;
    }

    @Override
    protected float getOffset() {
        return 1.0F;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
