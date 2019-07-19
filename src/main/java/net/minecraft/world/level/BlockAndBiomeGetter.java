package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface BlockAndBiomeGetter extends BlockGetter {
    Biome getBiome(BlockPos var1);

    int getBrightness(LightLayer var1, BlockPos var2);

    default boolean canSeeSky(BlockPos param0) {
        return this.getBrightness(LightLayer.SKY, param0) >= this.getMaxLightLevel();
    }

    @OnlyIn(Dist.CLIENT)
    default int getLightColor(BlockPos param0, int param1) {
        int var0 = this.getBrightness(LightLayer.SKY, param0);
        int var1 = this.getBrightness(LightLayer.BLOCK, param0);
        if (var1 < param1) {
            var1 = param1;
        }

        return var0 << 20 | var1 << 4;
    }
}
