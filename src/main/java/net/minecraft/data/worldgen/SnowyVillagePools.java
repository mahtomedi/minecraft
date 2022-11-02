package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.VillagePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class SnowyVillagePools {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/snowy/town_centers");
    private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/snowy/terminators");

    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<PlacedFeature> var0 = param0.lookup(Registry.PLACED_FEATURE_REGISTRY);
        Holder<PlacedFeature> var1 = var0.getOrThrow(VillagePlacements.SPRUCE_VILLAGE);
        Holder<PlacedFeature> var2 = var0.getOrThrow(VillagePlacements.PILE_SNOW_VILLAGE);
        Holder<PlacedFeature> var3 = var0.getOrThrow(VillagePlacements.PILE_ICE_VILLAGE);
        HolderGetter<StructureProcessorList> var4 = param0.lookup(Registry.PROCESSOR_LIST_REGISTRY);
        Holder<StructureProcessorList> var5 = var4.getOrThrow(ProcessorLists.STREET_SNOWY_OR_TAIGA);
        Holder<StructureProcessorList> var6 = var4.getOrThrow(ProcessorLists.FARM_SNOWY);
        Holder<StructureProcessorList> var7 = var4.getOrThrow(ProcessorLists.ZOMBIE_SNOWY);
        HolderGetter<StructureTemplatePool> var8 = param0.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> var9 = var8.getOrThrow(Pools.EMPTY);
        Holder<StructureTemplatePool> var10 = var8.getOrThrow(TERMINATORS_KEY);
        param0.register(
            START,
            new StructureTemplatePool(
                var9,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/town_centers/snowy_meeting_point_1"), 100),
                    Pair.of(StructurePoolElement.legacy("village/snowy/town_centers/snowy_meeting_point_2"), 50),
                    Pair.of(StructurePoolElement.legacy("village/snowy/town_centers/snowy_meeting_point_3"), 150),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/town_centers/snowy_meeting_point_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/town_centers/snowy_meeting_point_2"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/town_centers/snowy_meeting_point_3"), 3)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/snowy/streets",
            new StructureTemplatePool(
                var10,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_01", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_02", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_03", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/square_01", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_01", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_02", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_03", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_04", var5), 7),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_06", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_08", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_02", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_03", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_04", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_05", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_06", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/turn_01", var5), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            param0,
            "village/snowy/zombie/streets",
            new StructureTemplatePool(
                var10,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_01", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_02", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_03", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/square_01", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_01", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_02", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_03", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_04", var5), 7),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_06", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_08", var5), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_02", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_03", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_04", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_05", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_06", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/turn_01", var5), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            param0,
            "village/snowy/houses",
            new StructureTemplatePool(
                var10,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_2"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_3"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_4"), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_5"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_6"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_7"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_8"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_medium_house_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_medium_house_2"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_medium_house_3"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_2"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tool_smith_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fletcher_house_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_shepherds_house_1"), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_1"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_2"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fisher_cottage"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tannery_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_cartographer_house_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_library_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_2"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_weapon_smith_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_temple_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_1", var6), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_2", var6), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_2"), 2),
                    Pair.of(StructurePoolElement.empty(), 6)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/snowy/zombie/houses",
            new StructureTemplatePool(
                var10,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_2", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_3", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_4", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_5", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_6", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_7", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_8", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_2", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_3", var7), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_2", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tool_smith_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fletcher_house_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_shepherds_house_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_1", var7), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_2", var7), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fisher_cottage", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tannery_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_cartographer_house_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_library_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_2", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_weapon_smith_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_temple_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_1", var7), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_2", var7), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_1", var7), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_2", var7), 2),
                    Pair.of(StructurePoolElement.empty(), 6)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        param0.register(
            TERMINATORS_KEY,
            new StructureTemplatePool(
                var9,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", var5), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            param0,
            "village/snowy/trees",
            new StructureTemplatePool(var9, ImmutableList.of(Pair.of(StructurePoolElement.feature(var1), 1)), StructureTemplatePool.Projection.RIGID)
        );
        Pools.register(
            param0,
            "village/snowy/decor",
            new StructureTemplatePool(
                var9,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_01"), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_02"), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_03"), 1),
                    Pair.of(StructurePoolElement.feature(var1), 4),
                    Pair.of(StructurePoolElement.feature(var2), 4),
                    Pair.of(StructurePoolElement.feature(var3), 1),
                    Pair.of(StructurePoolElement.empty(), 9)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/snowy/zombie/decor",
            new StructureTemplatePool(
                var9,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_01", var7), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_02", var7), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_03", var7), 1),
                    Pair.of(StructurePoolElement.feature(var1), 4),
                    Pair.of(StructurePoolElement.feature(var2), 4),
                    Pair.of(StructurePoolElement.feature(var3), 4),
                    Pair.of(StructurePoolElement.empty(), 7)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/snowy/villagers",
            new StructureTemplatePool(
                var9,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/villagers/baby"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/snowy/zombie/villagers",
            new StructureTemplatePool(
                var9,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
