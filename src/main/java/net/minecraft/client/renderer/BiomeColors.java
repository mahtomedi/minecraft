package net.minecraft.client.renderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeColors {
    public static final ColorResolver GRASS_COLOR_RESOLVER = Biome::getGrassColor;
    public static final ColorResolver FOLIAGE_COLOR_RESOLVER = (param0, param1, param2) -> param0.getFoliageColor();
    public static final ColorResolver WATER_COLOR_RESOLVER = (param0, param1, param2) -> param0.getWaterColor();

    private static int getAverageColor(BlockAndTintGetter param0, BlockPos param1, ColorResolver param2) {
        return param0.getBlockTint(param1, param2);
    }

    public static int getAverageGrassColor(BlockAndTintGetter param0, BlockPos param1) {
        return getAverageColor(param0, param1, GRASS_COLOR_RESOLVER);
    }

    public static int getAverageFoliageColor(BlockAndTintGetter param0, BlockPos param1) {
        return getAverageColor(param0, param1, FOLIAGE_COLOR_RESOLVER);
    }

    public static int getAverageWaterColor(BlockAndTintGetter param0, BlockPos param1) {
        return getAverageColor(param0, param1, WATER_COLOR_RESOLVER);
    }
}
