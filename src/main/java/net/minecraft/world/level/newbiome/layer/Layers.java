package net.minecraft.world.level.newbiome.layer;

import java.util.function.LongFunction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public class Layers {
    protected static final int WARM_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.WARM_OCEAN);
    protected static final int LUKEWARM_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.LUKEWARM_OCEAN);
    protected static final int OCEAN = BuiltinRegistries.BIOME.getId(Biomes.OCEAN);
    protected static final int COLD_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.COLD_OCEAN);
    protected static final int FROZEN_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.FROZEN_OCEAN);
    protected static final int DEEP_WARM_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.DEEP_WARM_OCEAN);
    protected static final int DEEP_LUKEWARM_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.DEEP_LUKEWARM_OCEAN);
    protected static final int DEEP_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.DEEP_OCEAN);
    protected static final int DEEP_COLD_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.DEEP_COLD_OCEAN);
    protected static final int DEEP_FROZEN_OCEAN = BuiltinRegistries.BIOME.getId(Biomes.DEEP_FROZEN_OCEAN);

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> zoom(
        long param0, AreaTransformer1 param1, AreaFactory<T> param2, int param3, LongFunction<C> param4
    ) {
        AreaFactory<T> var0 = param2;

        for(int var1 = 0; var1 < param3; ++var1) {
            var0 = param1.run(param4.apply(param0 + (long)var1), var0);
        }

        return var0;
    }

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> getDefaultLayer(boolean param0, int param1, int param2, LongFunction<C> param3) {
        AreaFactory<T> var0 = IslandLayer.INSTANCE.run(param3.apply(1L));
        var0 = ZoomLayer.FUZZY.run(param3.apply(2000L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param3.apply(1L), var0);
        var0 = ZoomLayer.NORMAL.run(param3.apply(2001L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param3.apply(2L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param3.apply(50L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param3.apply(70L), var0);
        var0 = RemoveTooMuchOceanLayer.INSTANCE.run(param3.apply(2L), var0);
        AreaFactory<T> var1 = OceanLayer.INSTANCE.run(param3.apply(2L));
        var1 = zoom(2001L, ZoomLayer.NORMAL, var1, 6, param3);
        var0 = AddSnowLayer.INSTANCE.run(param3.apply(2L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param3.apply(3L), var0);
        var0 = AddEdgeLayer.CoolWarm.INSTANCE.run(param3.apply(2L), var0);
        var0 = AddEdgeLayer.HeatIce.INSTANCE.run(param3.apply(2L), var0);
        var0 = AddEdgeLayer.IntroduceSpecial.INSTANCE.run(param3.apply(3L), var0);
        var0 = ZoomLayer.NORMAL.run(param3.apply(2002L), var0);
        var0 = ZoomLayer.NORMAL.run(param3.apply(2003L), var0);
        var0 = AddIslandLayer.INSTANCE.run(param3.apply(4L), var0);
        var0 = AddMushroomIslandLayer.INSTANCE.run(param3.apply(5L), var0);
        var0 = AddDeepOceanLayer.INSTANCE.run(param3.apply(4L), var0);
        var0 = zoom(1000L, ZoomLayer.NORMAL, var0, 0, param3);
        AreaFactory<T> var2 = zoom(1000L, ZoomLayer.NORMAL, var0, 0, param3);
        var2 = RiverInitLayer.INSTANCE.run(param3.apply(100L), var2);
        AreaFactory<T> var3 = new BiomeInitLayer(param0).run(param3.apply(200L), var0);
        var3 = RareBiomeLargeLayer.INSTANCE.run(param3.apply(1001L), var3);
        var3 = zoom(1000L, ZoomLayer.NORMAL, var3, 2, param3);
        var3 = BiomeEdgeLayer.INSTANCE.run(param3.apply(1000L), var3);
        AreaFactory<T> var4 = zoom(1000L, ZoomLayer.NORMAL, var2, 2, param3);
        var3 = RegionHillsLayer.INSTANCE.run(param3.apply(1000L), var3, var4);
        var2 = zoom(1000L, ZoomLayer.NORMAL, var2, 2, param3);
        var2 = zoom(1000L, ZoomLayer.NORMAL, var2, param2, param3);
        var2 = RiverLayer.INSTANCE.run(param3.apply(1L), var2);
        var2 = SmoothLayer.INSTANCE.run(param3.apply(1000L), var2);
        var3 = RareBiomeSpotLayer.INSTANCE.run(param3.apply(1001L), var3);

        for(int var5 = 0; var5 < param1; ++var5) {
            var3 = ZoomLayer.NORMAL.run(param3.apply((long)(1000 + var5)), var3);
            if (var5 == 0) {
                var3 = AddIslandLayer.INSTANCE.run(param3.apply(3L), var3);
            }

            if (var5 == 1 || param1 == 1) {
                var3 = ShoreLayer.INSTANCE.run(param3.apply(1000L), var3);
            }
        }

        var3 = SmoothLayer.INSTANCE.run(param3.apply(1000L), var3);
        var3 = RiverMixerLayer.INSTANCE.run(param3.apply(100L), var3, var2);
        return OceanMixerLayer.INSTANCE.run(param3.apply(100L), var3, var1);
    }

    public static Layer getDefaultLayer(long param0, boolean param1, int param2, int param3) {
        int var0 = 25;
        AreaFactory<LazyArea> var1 = getDefaultLayer(param1, param2, param3, param1x -> new LazyAreaContext(25, param0, param1x));
        return new Layer(var1);
    }

    public static boolean isSame(int param0, int param1) {
        if (param0 == param1) {
            return true;
        } else {
            Biome var0 = BuiltinRegistries.BIOME.byId(param0);
            Biome var1 = BuiltinRegistries.BIOME.byId(param1);
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
