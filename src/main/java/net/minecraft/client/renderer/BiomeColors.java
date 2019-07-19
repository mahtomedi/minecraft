package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeColors {
    private static final BiomeColors.ColorResolver GRASS_COLOR_RESOLVER = Biome::getGrassColor;
    private static final BiomeColors.ColorResolver FOLIAGE_COLOR_RESOLVER = Biome::getFoliageColor;
    private static final BiomeColors.ColorResolver WATER_COLOR_RESOLVER = (param0, param1) -> param0.getWaterColor();
    private static final BiomeColors.ColorResolver WATER_FOG_COLOR_RESOLVER = (param0, param1) -> param0.getWaterFogColor();

    private static int getAverageColor(BlockAndBiomeGetter param0, BlockPos param1, BiomeColors.ColorResolver param2) {
        int var0 = 0;
        int var1 = 0;
        int var2 = 0;
        int var3 = Minecraft.getInstance().options.biomeBlendRadius;
        if (var3 == 0) {
            return param2.getColor(param0.getBiome(param1), param1);
        } else {
            int var4 = (var3 * 2 + 1) * (var3 * 2 + 1);
            Cursor3D var5 = new Cursor3D(param1.getX() - var3, param1.getY(), param1.getZ() - var3, param1.getX() + var3, param1.getY(), param1.getZ() + var3);

            int var7;
            for(BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos(); var5.advance(); var2 += var7 & 0xFF) {
                var6.set(var5.nextX(), var5.nextY(), var5.nextZ());
                var7 = param2.getColor(param0.getBiome(var6), var6);
                var0 += (var7 & 0xFF0000) >> 16;
                var1 += (var7 & 0xFF00) >> 8;
            }

            return (var0 / var4 & 0xFF) << 16 | (var1 / var4 & 0xFF) << 8 | var2 / var4 & 0xFF;
        }
    }

    public static int getAverageGrassColor(BlockAndBiomeGetter param0, BlockPos param1) {
        return getAverageColor(param0, param1, GRASS_COLOR_RESOLVER);
    }

    public static int getAverageFoliageColor(BlockAndBiomeGetter param0, BlockPos param1) {
        return getAverageColor(param0, param1, FOLIAGE_COLOR_RESOLVER);
    }

    public static int getAverageWaterColor(BlockAndBiomeGetter param0, BlockPos param1) {
        return getAverageColor(param0, param1, WATER_COLOR_RESOLVER);
    }

    @OnlyIn(Dist.CLIENT)
    interface ColorResolver {
        int getColor(Biome var1, BlockPos var2);
    }
}
