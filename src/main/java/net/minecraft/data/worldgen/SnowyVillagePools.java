package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class SnowyVillagePools {
    public static final StructureTemplatePool START = Pools.register(
        new StructureTemplatePool(
            new ResourceLocation("village/snowy/town_centers"),
            new ResourceLocation("empty"),
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

    public static void bootstrap() {
    }

    static {
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/streets"),
                new ResourceLocation("village/snowy/terminators"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_02", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_03", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/square_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_02", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_03", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_04", ProcessorLists.STREET_SNOVY_OR_TAIGA), 7),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_06", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_08", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_02", ProcessorLists.STREET_SNOVY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_03", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_04", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_05", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_06", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/streets/turn_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/zombie/streets"),
                new ResourceLocation("village/snowy/terminators"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_02", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_03", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/square_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_02", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_03", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_04", ProcessorLists.STREET_SNOVY_OR_TAIGA), 7),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_06", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_08", ProcessorLists.STREET_SNOVY_OR_TAIGA), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_02", ProcessorLists.STREET_SNOVY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_03", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_04", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_05", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_06", ProcessorLists.STREET_SNOVY_OR_TAIGA), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/turn_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 3)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/houses"),
                new ResourceLocation("village/snowy/terminators"),
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
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_1", ProcessorLists.FARM_SNOVY), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_2", ProcessorLists.FARM_SNOVY), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_1"), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_2"), 2),
                    Pair.of(StructurePoolElement.empty(), 6)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/zombie/houses"),
                new ResourceLocation("village/snowy/terminators"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_2", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_3", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_4", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_5", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_6", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_7", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_8", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_2", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_3", ProcessorLists.ZOMBIE_SNOVY), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_2", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tool_smith_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fletcher_house_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_shepherds_house_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_1", ProcessorLists.ZOMBIE_SNOVY), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_2", ProcessorLists.ZOMBIE_SNOVY), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fisher_cottage", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tannery_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_cartographer_house_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_library_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_2", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_weapon_smith_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_temple_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_1", ProcessorLists.ZOMBIE_SNOVY), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_2", ProcessorLists.ZOMBIE_SNOVY), 3),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_1", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_2", ProcessorLists.ZOMBIE_SNOVY), 2),
                    Pair.of(StructurePoolElement.empty(), 6)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/terminators"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", ProcessorLists.STREET_SNOVY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", ProcessorLists.STREET_SNOVY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", ProcessorLists.STREET_SNOVY_OR_TAIGA), 1),
                    Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", ProcessorLists.STREET_SNOVY_OR_TAIGA), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/trees"),
                new ResourceLocation("empty"),
                ImmutableList.of(Pair.of(StructurePoolElement.feature(Features.SPRUCE), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/decor"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_01"), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_02"), 4),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_03"), 1),
                    Pair.of(StructurePoolElement.feature(Features.SPRUCE), 4),
                    Pair.of(StructurePoolElement.feature(Features.PILE_SNOW), 4),
                    Pair.of(StructurePoolElement.feature(Features.PILE_ICE), 1),
                    Pair.of(StructurePoolElement.empty(), 9)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/zombie/decor"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_01", ProcessorLists.ZOMBIE_SNOVY), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_02", ProcessorLists.ZOMBIE_SNOVY), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_03", ProcessorLists.ZOMBIE_SNOVY), 1),
                    Pair.of(StructurePoolElement.feature(Features.SPRUCE), 4),
                    Pair.of(StructurePoolElement.feature(Features.PILE_SNOW), 4),
                    Pair.of(StructurePoolElement.feature(Features.PILE_ICE), 4),
                    Pair.of(StructurePoolElement.empty(), 7)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/villagers"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/villagers/baby"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            new StructureTemplatePool(
                new ResourceLocation("village/snowy/zombie/villagers"),
                new ResourceLocation("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/villagers/nitwit"), 1),
                    Pair.of(StructurePoolElement.legacy("village/snowy/zombie/villagers/unemployed"), 10)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
