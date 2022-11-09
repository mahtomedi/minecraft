package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class AncientCityStructurePools {
    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<PlacedFeature> var0 = param0.lookup(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> var1 = var0.getOrThrow(CavePlacements.SCULK_PATCH_ANCIENT_CITY);
        HolderGetter<StructureProcessorList> var2 = param0.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> var3 = var2.getOrThrow(ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION);
        Holder<StructureProcessorList> var4 = var2.getOrThrow(ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION);
        HolderGetter<StructureTemplatePool> var5 = param0.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> var6 = var5.getOrThrow(Pools.EMPTY);
        Pools.register(
            param0,
            "ancient_city/structures",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.empty(), 7),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/barracks", var3), 4),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_1", var3), 4),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_2", var3), 4),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_3", var3), 4),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/sauna_1", var3), 4),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/small_statue", var3), 4),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/large_ruin_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_2", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_3", var3), 2),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_4", var3), 2),
                    Pair.of(
                        StructurePoolElement.list(
                            ImmutableList.of(
                                StructurePoolElement.single("ancient_city/structures/camp_1", var3),
                                StructurePoolElement.single("ancient_city/structures/camp_2", var3),
                                StructurePoolElement.single("ancient_city/structures/camp_3", var3)
                            )
                        ),
                        1
                    ),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_2", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_2", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/large_pillar_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/structures/medium_pillar_1", var3), 1),
                    Pair.of(StructurePoolElement.list(ImmutableList.of(StructurePoolElement.single("ancient_city/structures/ice_box_1"))), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "ancient_city/sculk",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(Pair.of(StructurePoolElement.feature(var1), 6), Pair.of(StructurePoolElement.empty(), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "ancient_city/walls",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_corner_wall_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_intersection_wall_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_lshape_wall_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_2", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_2", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_4", var4), 4),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_passage_1", var4), 3),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_corner_wall_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_corner_wall_2", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_1", var4), 2),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_2", var4), 2),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_3", var4), 3),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_4", var4), 3)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "ancient_city/walls/no_corners",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_2", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_1", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_2", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_4", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_5", var4), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_bridge", var4), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "ancient_city/city_center/walls",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_2", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_left_corner", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_right_corner_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_right_corner_2", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/left", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/right", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top_right_corner", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top_left_corner", var3), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "ancient_city/city/entrance",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_connector", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_1", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_2", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_3", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_4", var3), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_5", var3), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
