package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.VillagePlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class TaigaVillagePools {
    public static final Holder<StructureTemplatePool> START = Pools.register(
        new StructureTemplatePool(
            new ResourceLocation("village/taiga/town_centers"),
            new ResourceLocation("empty"),
            ImmutableList.of(
                Pair.of(StructurePoolElement.legacy("village/taiga/town_centers/taiga_meeting_point_1", ProcessorLists.MOSSIFY_10_PERCENT), 49),
                Pair.of(StructurePoolElement.legacy("village/taiga/town_centers/taiga_meeting_point_2", ProcessorLists.MOSSIFY_10_PERCENT), 49),
                Pair.of(StructurePoolElement.legacy("village/taiga/zombie/town_centers/taiga_meeting_point_1", ProcessorLists.ZOMBIE_TAIGA), 1),
                Pair.of(StructurePoolElement.legacy("village/taiga/zombie/town_centers/taiga_meeting_point_2", ProcessorLists.ZOMBIE_TAIGA), 1)
            ),
            StructureTemplatePool.Projection.RIGID
        )
    );

    public static void bootstrap() {
    }

    static {
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/streets"),
                new ResourceLocation("village/taiga/terminators"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/corner_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/corner_02", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/corner_03", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_02", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_03", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_04", ProcessorLists.STREET_SNOWY_OR_TAIGA), 7),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_05", ProcessorLists.STREET_SNOWY_OR_TAIGA), 7),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_06", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_02", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_03", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_04", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_05", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_06", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/streets/turn_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/zombie/streets"),
                new ResourceLocation("village/taiga/terminators"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/corner_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/corner_02", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/corner_03", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_02", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_03", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_04", ProcessorLists.STREET_SNOWY_OR_TAIGA), 7),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_05", ProcessorLists.STREET_SNOWY_OR_TAIGA), 7),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_06", ProcessorLists.STREET_SNOWY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_02", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_03", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_04", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_05", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_06", ProcessorLists.STREET_SNOWY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/turn_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/houses"),
                new ResourceLocation("village/taiga/terminators"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_1", ProcessorLists.MOSSIFY_10_PERCENT), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_2", ProcessorLists.MOSSIFY_10_PERCENT), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_3", ProcessorLists.MOSSIFY_10_PERCENT), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_4", ProcessorLists.MOSSIFY_10_PERCENT), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_5", ProcessorLists.MOSSIFY_10_PERCENT), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_2", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_3", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_4", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_butcher_shop_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_tool_smith_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_fletcher_house_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_shepherds_house_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_armorer_house_1", ProcessorLists.MOSSIFY_10_PERCENT), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_armorer_2", ProcessorLists.MOSSIFY_10_PERCENT), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_fisher_cottage_1", ProcessorLists.MOSSIFY_10_PERCENT), 3),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_tannery_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_cartographer_house_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_library_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_masons_house_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_weaponsmith_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_weaponsmith_2", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_temple_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_large_farm_1", ProcessorLists.FARM_TAIGA), 6),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_large_farm_2", ProcessorLists.FARM_TAIGA), 6),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_farm_1", ProcessorLists.MOSSIFY_10_PERCENT), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_animal_pen_1", ProcessorLists.MOSSIFY_10_PERCENT), 2),
                    Pair.of(StructurePoolElement.empty(), 6)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/zombie/houses"),
                new ResourceLocation("village/taiga/terminators"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_1", ProcessorLists.ZOMBIE_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_2", ProcessorLists.ZOMBIE_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_3", ProcessorLists.ZOMBIE_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_4", ProcessorLists.ZOMBIE_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_5", ProcessorLists.ZOMBIE_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_2", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_3", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_4", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_butcher_shop_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_tool_smith_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_fletcher_house_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_shepherds_house_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_armorer_house_1", ProcessorLists.ZOMBIE_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_fisher_cottage_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_tannery_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_cartographer_house_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_library_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_masons_house_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_weaponsmith_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_weaponsmith_2", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_temple_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_large_farm_1", ProcessorLists.ZOMBIE_TAIGA), 6),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_large_farm_2", ProcessorLists.ZOMBIE_TAIGA), 6),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_farm_1", ProcessorLists.ZOMBIE_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_animal_pen_1", ProcessorLists.ZOMBIE_TAIGA), 2),
                    Pair.of(StructurePoolElement.empty(), 6)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/terminators"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", ProcessorLists.STREET_SNOWY_OR_TAIGA), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/decor"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_lamp_post_1"), 10),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_1"), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_2"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_3"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_4"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_5"), 2),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_6"), 1),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.SPRUCE_VILLAGE), 4),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PINE_VILLAGE), 4),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PILE_PUMPKIN_VILLAGE), 2),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PATCH_TAIGA_GRASS_VILLAGE), 4),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PATCH_BERRY_BUSH_VILLAGE), 1),
                    Pair.of(StructurePoolElement.empty(), 4)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/zombie/decor"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_1"), 4),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_2"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_3"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_4"), 1),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.SPRUCE_VILLAGE), 4),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PINE_VILLAGE), 4),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PILE_PUMPKIN_VILLAGE), 2),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PATCH_TAIGA_GRASS_VILLAGE), 4),
                    Pair.of(StructurePoolElement.feature(VillagePlacements.PATCH_BERRY_BUSH_VILLAGE), 1),
                    Pair.of(StructurePoolElement.empty(), 4)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/villagers"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/villagers/baby"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/taiga/zombie/villagers"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/taiga/zombie/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
