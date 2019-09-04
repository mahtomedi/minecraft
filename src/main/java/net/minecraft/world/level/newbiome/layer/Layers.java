package net.minecraft.world.level.newbiome.layer;

import java.util.function.LongFunction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public class Layers {
    protected static final int WARM_OCEAN = Registry.BIOME.getId(Biomes.WARM_OCEAN);
    protected static final int LUKEWARM_OCEAN = Registry.BIOME.getId(Biomes.LUKEWARM_OCEAN);
    protected static final int OCEAN = Registry.BIOME.getId(Biomes.OCEAN);
    protected static final int COLD_OCEAN = Registry.BIOME.getId(Biomes.COLD_OCEAN);
    protected static final int FROZEN_OCEAN = Registry.BIOME.getId(Biomes.FROZEN_OCEAN);
    protected static final int DEEP_WARM_OCEAN = Registry.BIOME.getId(Biomes.DEEP_WARM_OCEAN);
    protected static final int DEEP_LUKEWARM_OCEAN = Registry.BIOME.getId(Biomes.DEEP_LUKEWARM_OCEAN);
    protected static final int DEEP_OCEAN = Registry.BIOME.getId(Biomes.DEEP_OCEAN);
    protected static final int DEEP_COLD_OCEAN = Registry.BIOME.getId(Biomes.DEEP_COLD_OCEAN);
    protected static final int DEEP_FROZEN_OCEAN = Registry.BIOME.getId(Biomes.DEEP_FROZEN_OCEAN);

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> zoom(
        long param0, AreaTransformer1 param1, AreaFactory<T> param2, int param3, LongFunction<C> param4
    ) {
        AreaFactory<T> var0 = param2;

        for(int var1 = 0; var1 < param3; ++var1) {
            var0 = param1.run(param4.apply(param0 + (long)var1), var0);
        }

        return var0;
    }

    public static <T extends Area, C extends BigContext<T>> AreaFactory<T> getDefaultLayer(
        LevelType param0, OverworldGeneratorSettings param1, LongFunction<C> param2
    ) {
        AreaFactory<T> var0 = IslandLayer.INSTANCE.run(param2.apply(1L));
        var0 = ZoomLayer.FUZZY.run(param2.apply(2000L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param2.apply(1L), var0);
        var0 = ZoomLayer.NORMAL.run(param2.apply(2001L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param2.apply(2L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param2.apply(50L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param2.apply(70L), var0);
        var0 = RemoveTooMuchOceanLayer.INSTANCE.run(param2.apply(2L), var0);
        AreaFactory<T> var1 = OceanLayer.INSTANCE.run(param2.apply(2L));
        var1 = zoom(2001L, ZoomLayer.NORMAL, var1, 6, param2);
        var0 = AddSnowLayer.INSTANCE.run(param2.apply(2L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param2.apply(3L), var0);
        var0 = AddEdgeLayer.CoolWarm.INSTANCE.run(param2.apply(2L), var0);
        var0 = AddEdgeLayer.HeatIce.INSTANCE.run(param2.apply(2L), var0);
        var0 = AddEdgeLayer.IntroduceSpecial.INSTANCE.run(param2.apply(3L), var0);
        var0 = ZoomLayer.NORMAL.run(param2.apply(2002L), var0);
        var0 = ZoomLayer.NORMAL.run(param2.apply(2003L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param2.apply(4L), var0);
        var0 = AddMushroomIslandLayer.INSTANCE.run(param2.apply(5L), var0);
        var0 = AddDeepOceanLayer.INSTANCE.run(param2.apply(4L), var0);
        var0 = zoom(1000L, ZoomLayer.NORMAL, var0, 0, param2);
        int var2 = param0 == LevelType.LARGE_BIOMES ? 6 : param1.getBiomeSize();
        int var3 = param1.getRiverSize();
        AreaFactory<T> var4 = zoom(1000L, ZoomLayer.NORMAL, var0, 0, param2);
        var4 = RiverInitLayer.INSTANCE.run(param2.apply(100L), var4);
        AreaFactory<T> var5 = new BiomeInitLayer(param0, param1.getFixedBiome()).run(param2.apply(200L), var0);
        var5 = RareBiomeLargeLayer.INSTANCE.run(param2.apply(1001L), var5);
        var5 = zoom(1000L, ZoomLayer.NORMAL, var5, 2, param2);
        var5 = BiomeEdgeLayer.INSTANCE.run(param2.apply(1000L), var5);
        AreaFactory<T> var6 = zoom(1000L, ZoomLayer.NORMAL, var4, 2, param2);
        var5 = RegionHillsLayer.INSTANCE.run(param2.apply(1000L), var5, var6);
        var4 = zoom(1000L, ZoomLayer.NORMAL, var4, 2, param2);
        var4 = zoom(1000L, ZoomLayer.NORMAL, var4, var3, param2);
        var4 = RiverLayer.INSTANCE.run(param2.apply(1L), var4);
        var4 = SmoothLayer.INSTANCE.run(param2.apply(1000L), var4);
        var5 = RareBiomeSpotLayer.INSTANCE.run(param2.apply(1001L), var5);

        for(int var7x = 0; var7x < var2; ++var7x) {
            var5 = ZoomLayer.NORMAL.run(param2.apply((long)(1000 + var7x)), var5);
            if (var7x == 0) {
                var5 = AddIslandLayer.INSTANCE.run(param2.apply(3L), var5);
            }

            if (var7x == 1 || var2 == 1) {
                var5 = ShoreLayer.INSTANCE.run(param2.apply(1000L), var5);
            }
        }

        var5 = SmoothLayer.INSTANCE.run(param2.apply(1000L), var5);
        var5 = RiverMixerLayer.INSTANCE.run(param2.apply(100L), var5, var4);
        return OceanMixerLayer.INSTANCE.run(param2.apply(100L), var5, var1);
    }

    public static Layer getDefaultLayer(long param0, LevelType param1, OverworldGeneratorSettings param2) {
        int var0 = 25;
        AreaFactory<LazyArea> var1 = getDefaultLayer(param1, param2, param1x -> new LazyAreaContext(25, param0, param1x));
        return new Layer(var1);
    }

    public static boolean isSame(int param0, int param1) {
        if (param0 == param1) {
            return true;
        } else {
            Biome var0 = Registry.BIOME.byId(param0);
            Biome var1 = Registry.BIOME.byId(param1);
            if (var0 == null || var1 == null) {
                return false;
            } else if (var0 != Biomes.WOODED_BADLANDS_PLATEAU && var0 != Biomes.BADLANDS_PLATEAU) {
                if (var0.getBiomeCategory() != Biome.BiomeCategory.NONE
                    && var1.getBiomeCategory() != Biome.BiomeCategory.NONE
                    && var0.getBiomeCategory() == var1.getBiomeCategory()) {
                    return true;
                } else {
                    return var0 == var1;
                }
            } else {
                return var1 == Biomes.WOODED_BADLANDS_PLATEAU || var1 == Biomes.BADLANDS_PLATEAU;
            }
        }
    }

    protected static boolean isOcean(int param0) {
        return param0 == WARM_OCEAN
            || param0 == LUKEWARM_OCEAN
            || param0 == OCEAN
            || param0 == COLD_OCEAN
            || param0 == FROZEN_OCEAN
            || param0 == DEEP_WARM_OCEAN
            || param0 == DEEP_LUKEWARM_OCEAN
            || param0 == DEEP_OCEAN
            || param0 == DEEP_COLD_OCEAN
            || param0 == DEEP_FROZEN_OCEAN;
    }

    protected static boolean isShallowOcean(int param0) {
        return param0 == WARM_OCEAN || param0 == LUKEWARM_OCEAN || param0 == OCEAN || param0 == COLD_OCEAN || param0 == FROZEN_OCEAN;
    }
}
