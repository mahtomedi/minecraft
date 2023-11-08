package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBindings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class TrialChambersStructurePools {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("trial_chambers/chamber/end");
    public static final ResourceKey<StructureTemplatePool> HALLWAY_FALLBACK = Pools.createKey("trial_chambers/hallway/fallback");
    public static final ResourceKey<StructureTemplatePool> CHAMBER_CAP_FALLBACK = Pools.createKey("trial_chambers/chamber/entrance_cap");
    public static final List<PoolAliasBinding> ALIAS_BINDINGS = ImmutableList.<PoolAliasBinding>builder()
        .add(
            PoolAliasBinding.randomGroup(
                SimpleWeightedRandomList.<List<PoolAliasBinding>>builder()
                    .add(
                        List.of(
                            PoolAliasBinding.direct(spawner("contents/ranged"), spawner("ranged/skeleton")),
                            PoolAliasBinding.direct(spawner("contents/slow_ranged"), spawner("slow_ranged/skeleton"))
                        )
                    )
                    .add(
                        List.of(
                            PoolAliasBinding.direct(spawner("contents/ranged"), spawner("ranged/stray")),
                            PoolAliasBinding.direct(spawner("contents/slow_ranged"), spawner("slow_ranged/stray"))
                        )
                    )
                    .add(
                        List.of(
                            PoolAliasBinding.direct(spawner("contents/ranged"), spawner("ranged/poison_skeleton")),
                            PoolAliasBinding.direct(spawner("contents/slow_ranged"), spawner("slow_ranged/poison_skeleton"))
                        )
                    )
                    .build()
            )
        )
        .add(
            PoolAliasBinding.random(
                spawner("contents/melee"),
                SimpleWeightedRandomList.<String>builder().add(spawner("melee/zombie")).add(spawner("melee/husk")).add(spawner("melee/slime")).build()
            )
        )
        .add(
            PoolAliasBinding.random(
                spawner("contents/small_melee"),
                SimpleWeightedRandomList.<String>builder()
                    .add(spawner("small_melee/spider"))
                    .add(spawner("small_melee/cave_spider"))
                    .add(spawner("small_melee/silverfish"))
                    .add(spawner("small_melee/baby_zombie"))
                    .build()
            )
        )
        .build();

    public static String spawner(String param0) {
        return "trial_chambers/spawner/" + param0;
    }

    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureTemplatePool> var0 = param0.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> var1 = var0.getOrThrow(Pools.EMPTY);
        Holder<StructureTemplatePool> var2 = var0.getOrThrow(HALLWAY_FALLBACK);
        Holder<StructureTemplatePool> var3 = var0.getOrThrow(CHAMBER_CAP_FALLBACK);
        HolderGetter<StructureProcessorList> var4 = param0.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> var5 = var4.getOrThrow(UpdateOneTwentyOneProcessorLists.TRIAL_CHAMBERS_COPPER_BULB_DEGRADATION);
        param0.register(
            START,
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/end_1", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/end_2", var5), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/chamber/entrance_cap",
            new StructureTemplatePool(
                var3, List.of(Pair.of(StructurePoolElement.single("trial_chambers/chamber/entrance_cap", var5), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/chambers/end",
            new StructureTemplatePool(
                var2,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_1", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_5", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_6", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_9", var5), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/chamber/addon",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_stacked_walkway"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_stacked_walkway_2"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_corner_column"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_column_ranged_spawner", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/middle_column_ranged_spawner", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/grate_bridge"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/hanging_platform"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/short_grate_platform"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/short_platform"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/lower_staircase_down"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/lower_walkway_platform"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/walkway_extension"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/side_walkway"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/closed_side_walkway"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/middle_walkway"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/10x15_rise"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/10x15_stacked_pathway"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/10x15_pathway_3"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/platform_with_space"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/stairs_with_space"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/stairs_with_space_2"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c3_side_walkway_1"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c3_side_walkway_2"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/walkway_with_bridge_1"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/corner_room_1"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_wide_platform"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c1_breeze"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/chamber/addon/c6",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.empty(), 3),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_cover_small_1"), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_cover_small_2"), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_cover_small_3"), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_column_full"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_column_tall"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_column_tall_wide"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_ranged_column_short"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_ranged_column_short_wide"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_chest"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_melee_spawner"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_cover"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_cover_long"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_cover_short"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_cover_small"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c6_stairs"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/corridor/second",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/second_plate"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/intersection/intersection_1", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/intersection/intersection_2", var5), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/corridor/first",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/first_plate"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/entrance_1", var5), 2)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/corridor/first/straight",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/corridor/first_plate"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/corridor",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_1", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_2", var5), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_3", var5), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_4", var5), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_5", var5), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_6", var5), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_7", var5), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_8", var5), 2)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        param0.register(
            HALLWAY_FALLBACK,
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_thin"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber_thin"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/hallway",
            new StructureTemplatePool(
                var2,
                List.of(
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/corridor_connector_1"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/upper_hallway_connector", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/lower_hallway_connector", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_1", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_2", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_3", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_4", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_5", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_6", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_8", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_9", var5), 150),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber", var5), 10),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber_thin", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/cache_1", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/left_corner", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/right_corner", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/corner_staircase", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/corner_staircase_down", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/long_straight_staircase", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/long_straight_staircase_down", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/straight", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/straight_staircase", var5), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/hallway/straight_staircase_down", var5), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/corridors/addon/lower",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.empty(), 8),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/staircase"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/wall"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/ladder_to_middle"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/arrow_dispenser"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/bridge_lower"), 2)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/corridors/addon/middle",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.empty(), 8),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/open_walkway"), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/walled_walkway"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/corridors/addon/middle_upper",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.empty(), 6),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/open_walkway_upper"), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/chandelier_upper"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/decoration_upper"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/head_upper"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/reward_upper"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/decor",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.empty(), 22),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/empty_pot"), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/dead_bush_pot"), 2),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/undecorated_pot"), 3),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_1"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_2"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_3"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_4"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/decor/barrel"), 2)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/decor_chamber",
            new StructureTemplatePool(
                var1,
                List.of(Pair.of(StructurePoolElement.empty(), 4), Pair.of(StructurePoolElement.single("trial_chambers/decor/undecorated_pot"), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/reward/all",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/reward/connectors/default"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/reward/contents/default",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/reward/default"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/chests/supply",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/chests/connectors/supply"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/chests/contents/supply",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/chests/supply"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/spawner/ranged",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/ranged"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/spawner/slow_ranged",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/slow_ranged"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/spawner/melee",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/melee"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/spawner/small_melee",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/small_melee"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/spawner/breeze",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/breeze"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/spawner/all",
            new StructureTemplatePool(
                var1,
                List.of(
                    Pair.of(StructurePoolElement.empty(), 10),
                    Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/ranged"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/melee"), 1),
                    Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/small_melee"), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/spawner/contents/breeze",
            new StructureTemplatePool(
                var1, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/breeze/breeze"), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "trial_chambers/dispensers/chamber",
            new StructureTemplatePool(
                var1,
                List.of(Pair.of(StructurePoolElement.empty(), 1), Pair.of(StructurePoolElement.single("trial_chambers/dispensers/chamber"), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        PoolAliasBindings.registerTargetsAsPools(param0, var1, ALIAS_BINDINGS);
    }
}
