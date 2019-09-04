package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TheEndGatewayRenderer extends TheEndPortalRenderer {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

    @Override
    public void render(TheEndPortalBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        RenderSystem.disableFog();
        TheEndGatewayBlockEntity var0 = (TheEndGatewayBlockEntity)param0;
        if (var0.isSpawning() || var0.isCoolingDown()) {
            RenderSystem.alphaFunc(516, 0.1F);
            this.bindTexture(BEAM_LOCATION);
            float var1 = var0.isSpawning() ? var0.getSpawnPercent(param4) : var0.getCooldownPercent(param4);
            double var2 = var0.isSpawning() ? 256.0 - param2 : 50.0;
            var1 = Mth.sin(var1 * (float) Math.PI);
            int var3 = Mth.floor((double)var1 * var2);
            float[] var4 = var0.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
            BeaconRenderer.renderBeaconBeam(param1, param2, param3, (double)param4, (double)var1, var0.getLevel().getGameTime(), 0, var3, var4, 0.15, 0.175);
            BeaconRenderer.renderBeaconBeam(param1, param2, param3, (double)param4, (double)var1, var0.getLevel().getGameTime(), 0, -var3, var4, 0.15, 0.175);
        }

        super.render(param0, param1, param2, param3, param4, param5);
        RenderSystem.enableFog();
    }

    @Override
    protected int getPasses(double param0) {
        return super.getPasses(param0) + 1;
    }

    @Override
    protected float getOffset() {
        return 1.0F;
    }
}
