package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface BlockAndTintGetter extends BlockGetter {
    LevelLightEngine getLightEngine();

    @OnlyIn(Dist.CLIENT)
    int getBlockTint(BlockPos var1, ColorResolver var2);

    default int getBrightness(LightLayer param0, BlockPos param1) {
        return this.getLightEngine().getLayerListener(param0).getLightValue(param1);
    }

    default int getRawBrightness(BlockPos param0, int param1) {
        return this.getLightEngine().getRawBrightness(param0, param1);
    }

    default boolean canSeeSky(BlockPos param0) {
        return this.getBrightness(LightLayer.SKY, param0) >= this.getMaxLightLevel();
    }
}
