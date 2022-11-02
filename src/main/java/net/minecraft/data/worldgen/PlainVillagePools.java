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

public class PlainVillagePools {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/plains/town_centers");
    private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/plains/terminators");

    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<PlacedFeature> var0 = param0.lookup(Registry.PLACED_FEATURE_REGISTRY);
        Holder<PlacedFeature> var1 = var0.getOrThrow(VillagePlacements.OAK_VILLAGE);
        Holder<PlacedFeature> var2 = var0.getOrThrow(VillagePlacements.FLOWER_PLAIN_VILLAGE);
        Holder<PlacedFeature> var3 = var0.getOrThrow(VillagePlacements.PILE_HAY_VILLAGE);
        HolderGetter<StructureProcessorList> var4 = param0.lookup(Registry.PROCESSOR_LIST_REGISTRY);
        Holder<StructureProcessorList> var5 = var4.getOrThrow(ProcessorLists.MOSSIFY_10_PERCENT);
        Holder<StructureProcessorList> var6 = var4.getOrThrow(ProcessorLists.MOSSIFY_20_PERCENT);
        Holder<StructureProcessorList> var7 = var4.getOrThrow(ProcessorLists.MOSSIFY_70_PERCENT);
        Holder<StructureProcessorList> var8 = var4.getOrThrow(ProcessorLists.ZOMBIE_PLAINS);
        Holder<StructureProcessorList> var9 = var4.getOrThrow(ProcessorLists.STREET_PLAINS);
        Holder<StructureProcessorList> var10 = var4.getOrThrow(ProcessorLists.FARM_PLAINS);
        HolderGetter<StructureTemplatePool> var11 = param0.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> var12 = var11.getOrThrow(Pools.EMPTY);
        Holder<StructureTemplatePool> var13 = var11.getOrThrow(TERMINATORS_KEY);
        param0.register(
            START,
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_fountain_01", var6), 50),
                    Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_meeting_point_1", var6), 50),
                    Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_meeting_point_2"), 50),
                    Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_meeting_point_3", var7), 50),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_fountain_01", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_meeting_point_1", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_meeting_point_2", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_meeting_point_3", var8), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/plains/streets",
            new StructureTemplatePool(
                var13,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/corner_01", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/corner_02", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/corner_03", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_01", var9), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_02", var9), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_03", var9), 7),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_04", var9), 7),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_05", var9), 3),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_06", var9), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_01", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_02", var9), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_03", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_04", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_05", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_06", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/streets/turn_01", var9), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            param0,
            "village/plains/zombie/streets",
            new StructureTemplatePool(
                var13,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/corner_01", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/corner_02", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/corner_03", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_01", var9), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_02", var9), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_03", var9), 7),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_04", var9), 7),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_05", var9), 3),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_06", var9), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_01", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_02", var9), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_03", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_04", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_05", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_06", var9), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/turn_01", var9), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            param0,
            "village/plains/houses",
            new StructureTemplatePool(
                var13,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_2", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_3", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_4", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_5", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_6", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_7", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_8", var5), 3),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_medium_house_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_medium_house_2", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_big_house_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_butcher_shop_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_butcher_shop_2", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tool_smith_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_fletcher_house_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_shepherds_house_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_armorer_house_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_fisher_cottage_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tannery_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_cartographer_1", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_1", var5), 5),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_2", var5), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_masons_house_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_weaponsmith_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_3", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_4", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_stable_1", var5), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_stable_2"), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_large_farm_1", var10), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_farm_1", var10), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_1"), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_2"), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_3"), 5),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_accessory_1"), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_meeting_point_4", var7), 3),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_meeting_point_5"), 1),
                    Pair.of(StructurePoolElement.empty(), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/plains/zombie/houses",
            new StructureTemplatePool(
                var13,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_2", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_3", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_4", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_5", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_6", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_7", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_8", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_medium_house_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_medium_house_2", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_big_house_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_butcher_shop_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_butcher_shop_2", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tool_smith_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_fletcher_house_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_shepherds_house_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_armorer_house_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_fisher_cottage_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tannery_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_cartographer_1", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_1", var8), 3),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_2", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_masons_house_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_weaponsmith_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_3", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_4", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_stable_1", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_stable_2", var8), 2),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_large_farm_1", var8), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_farm_1", var8), 4),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_1", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_2", var8), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_animal_pen_3", var8), 5),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_meeting_point_4", var8), 3),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_meeting_point_5", var8), 1),
                    Pair.of(StructurePoolElement.empty(), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        param0.register(
            TERMINATORS_KEY,
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", var9), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", var9), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", var9), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", var9), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            param0,
            "village/plains/trees",
            new StructureTemplatePool(var12, ImmutableList.of(Pair.of(StructurePoolElement.feature(var1), 1)), StructureTemplatePool.Projection.RIGID)
        );
        Pools.register(
            param0,
            "village/plains/decor",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/plains_lamp_1"), 2),
                    Pair.of(StructurePoolElement.feature(var1), 1),
                    Pair.of(StructurePoolElement.feature(var2), 1),
                    Pair.of(StructurePoolElement.feature(var3), 1),
                    Pair.of(StructurePoolElement.empty(), 2)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/plains/zombie/decor",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/plains_lamp_1", var8), 1),
                    Pair.of(StructurePoolElement.feature(var1), 1),
                    Pair.of(StructurePoolElement.feature(var2), 1),
                    Pair.of(StructurePoolElement.feature(var3), 1),
                    Pair.of(StructurePoolElement.empty(), 2)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/plains/villagers",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/villagers/baby"), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/plains/zombie/villagers",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/zombie/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/common/animals",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cows_1"), 7),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/pigs_1"), 7),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/horses_1"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/horses_2"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/horses_3"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/horses_4"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/horses_5"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_1"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_2"), 1),
                    Pair.of(StructurePoolElement.empty(), 5)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/common/sheep",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_1"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_2"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/common/cats",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_black"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_british"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_calico"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_persian"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_ragdoll"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_red"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_siamese"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_tabby"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_white"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cat_jellie"), 1),
                    Pair.of(StructurePoolElement.empty(), 3)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/common/butcher_animals",
            new StructureTemplatePool(
                var12,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/common/animals/cows_1"), 3),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/pigs_1"), 3),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_1"), 1),
                    Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_2"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/common/iron_golem",
            new StructureTemplatePool(
                var12, ImmutableList.of(Pair.of(StructurePoolElement.legacy("village/common/iron_golem"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "village/common/well_bottoms",
            new StructureTemplatePool(
                var12, ImmutableList.of(Pair.of(StructurePoolElement.legacy("village/common/well_bottom"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
