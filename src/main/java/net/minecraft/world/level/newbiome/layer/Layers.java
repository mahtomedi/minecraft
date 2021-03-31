package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.function.LongFunction;
import net.minecraft.Util;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public class Layers implements LayerBiomes {
    protected static final int WARM_ID = 1;
    protected static final int MEDIUM_ID = 2;
    protected static final int COLD_ID = 3;
    protected static final int ICE_ID = 4;
    protected static final int SPECIAL_MASK = 3840;
    protected static final int SPECIAL_SHIFT = 8;
    private static final Int2IntMap CATEGORIES = Util.make(new Int2IntOpenHashMap(), param0 -> {
        register(param0, Layers.Category.BEACH, 16);
        register(param0, Layers.Category.BEACH, 26);
        register(param0, Layers.Category.DESERT, 2);
        register(param0, Layers.Category.DESERT, 17);
        register(param0, Layers.Category.DESERT, 130);
        register(param0, Layers.Category.EXTREME_HILLS, 131);
        register(param0, Layers.Category.EXTREME_HILLS, 162);
        register(param0, Layers.Category.EXTREME_HILLS, 20);
        register(param0, Layers.Category.EXTREME_HILLS, 3);
        register(param0, Layers.Category.EXTREME_HILLS, 34);
        register(param0, Layers.Category.FOREST, 27);
        register(param0, Layers.Category.FOREST, 28);
        register(param0, Layers.Category.FOREST, 29);
        register(param0, Layers.Category.FOREST, 157);
        register(param0, Layers.Category.FOREST, 132);
        register(param0, Layers.Category.FOREST, 4);
        register(param0, Layers.Category.FOREST, 155);
        register(param0, Layers.Category.FOREST, 156);
        register(param0, Layers.Category.FOREST, 18);
        register(param0, Layers.Category.ICY, 140);
        register(param0, Layers.Category.ICY, 13);
        register(param0, Layers.Category.ICY, 12);
        register(param0, Layers.Category.JUNGLE, 168);
        register(param0, Layers.Category.JUNGLE, 169);
        register(param0, Layers.Category.JUNGLE, 21);
        register(param0, Layers.Category.JUNGLE, 23);
        register(param0, Layers.Category.JUNGLE, 22);
        register(param0, Layers.Category.JUNGLE, 149);
        register(param0, Layers.Category.JUNGLE, 151);
        register(param0, Layers.Category.MESA, 37);
        register(param0, Layers.Category.MESA, 165);
        register(param0, Layers.Category.MESA, 167);
        register(param0, Layers.Category.MESA, 166);
        register(param0, Layers.Category.BADLANDS_PLATEAU, 39);
        register(param0, Layers.Category.BADLANDS_PLATEAU, 38);
        register(param0, Layers.Category.MUSHROOM, 14);
        register(param0, Layers.Category.MUSHROOM, 15);
        register(param0, Layers.Category.NONE, 25);
        register(param0, Layers.Category.OCEAN, 46);
        register(param0, Layers.Category.OCEAN, 49);
        register(param0, Layers.Category.OCEAN, 50);
        register(param0, Layers.Category.OCEAN, 48);
        register(param0, Layers.Category.OCEAN, 24);
        register(param0, Layers.Category.OCEAN, 47);
        register(param0, Layers.Category.OCEAN, 10);
        register(param0, Layers.Category.OCEAN, 45);
        register(param0, Layers.Category.OCEAN, 0);
        register(param0, Layers.Category.OCEAN, 44);
        register(param0, Layers.Category.PLAINS, 1);
        register(param0, Layers.Category.PLAINS, 129);
        register(param0, Layers.Category.RIVER, 11);
        register(param0, Layers.Category.RIVER, 7);
        register(param0, Layers.Category.SAVANNA, 35);
        register(param0, Layers.Category.SAVANNA, 36);
        register(param0, Layers.Category.SAVANNA, 163);
        register(param0, Layers.Category.SAVANNA, 164);
        register(param0, Layers.Category.SWAMP, 6);
        register(param0, Layers.Category.SWAMP, 134);
        register(param0, Layers.Category.TAIGA, 160);
        register(param0, Layers.Category.TAIGA, 161);
        register(param0, Layers.Category.TAIGA, 32);
        register(param0, Layers.Category.TAIGA, 33);
        register(param0, Layers.Category.TAIGA, 30);
        register(param0, Layers.Category.TAIGA, 31);
        register(param0, Layers.Category.TAIGA, 158);
        register(param0, Layers.Category.TAIGA, 5);
        register(param0, Layers.Category.TAIGA, 19);
        register(param0, Layers.Category.TAIGA, 133);
    });

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
            return CATEGORIES.get(param0) == CATEGORIES.get(param1);
        }
    }

    private static void register(Int2IntOpenHashMap param0, Layers.Category param1, int param2) {
        param0.put(param2, param1.ordinal());
    }

    protected static boolean isOcean(int param0) {
        return param0 == 44
            || param0 == 45
            || param0 == 0
            || param0 == 46
            || param0 == 10
            || param0 == 47
            || param0 == 48
            || param0 == 24
            || param0 == 49
            || param0 == 50;
    }

    protected static boolean isShallowOcean(int param0) {
        return param0 == 44 || param0 == 45 || param0 == 0 || param0 == 46 || param0 == 10;
    }

    static enum Category {
        NONE,
        TAIGA,
        EXTREME_HILLS,
        JUNGLE,
        MESA,
        BADLANDS_PLATEAU,
        PLAINS,
        SAVANNA,
        ICY,
        BEACH,
        FOREST,
        OCEAN,
        DESERT,
        RIVER,
        SWAMP,
        MUSHROOM;
    }
}
