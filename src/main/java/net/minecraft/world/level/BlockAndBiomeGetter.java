package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface BlockAndBiomeGetter extends BlockGetter {
    BiomeManager getBiomeManager();

    LevelLightEngine getLightEngine();

    default Biome getBiome(BlockPos param0) {
        return this.getBiomeManager().getBiome(param0);
    }

    default int getBrightness(LightLayer param0, BlockPos param1) {
        return this.getLightEngine().getLayerListener(param0).getLightValue(param1);
    }

    default int getRawBrightness(BlockPos param0, int param1) {
        return this.getLightEngine().getRawBrightness(param0, param1);
    }

    default boolean canSeeSky(BlockPos param0) {
        return this.getBrightness(LightLayer.SKY, param0) >= this.getMaxLightLevel();
    }

    @OnlyIn(Dist.CLIENT)
    default int getLightColor(BlockPos param0) {
        return this.getLightColor(this.getBlockState(param0), param0);
    }

    @OnlyIn(Dist.CLIENT)
    default int getLightColor(BlockState param0, BlockPos param1) {
        if (param0.emissiveRendering()) {
            return 15728880;
        } else {
            int var0 = this.getBrightness(LightLayer.SKY, param1);
            int var1 = this.getBrightness(LightLayer.BLOCK, param1);
            int var2 = param0.getLightEmission();
            if (var1 < var2) {
                var1 = var2;
            }

            return var0 << 20 | var1 << 4;
        }
    }
}
