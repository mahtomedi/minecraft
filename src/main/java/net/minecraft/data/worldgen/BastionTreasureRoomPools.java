package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionTreasureRoomPools {
    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureProcessorList> var0 = param0.lookup(Registry.PROCESSOR_LIST_REGISTRY);
        Holder<StructureProcessorList> var1 = var0.getOrThrow(ProcessorLists.TREASURE_ROOMS);
        Holder<StructureProcessorList> var2 = var0.getOrThrow(ProcessorLists.HIGH_WALL);
        Holder<StructureProcessorList> var3 = var0.getOrThrow(ProcessorLists.BOTTOM_RAMPART);
        Holder<StructureProcessorList> var4 = var0.getOrThrow(ProcessorLists.HIGH_RAMPART);
        Holder<StructureProcessorList> var5 = var0.getOrThrow(ProcessorLists.ROOF);
        HolderGetter<StructureTemplatePool> var6 = param0.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> var7 = var6.getOrThrow(Pools.EMPTY);
        Pools.register(
            param0,
            "bastion/treasure/bases",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/treasure/bases/lava_basin", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/stairs",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/treasure/stairs/lower_stairs", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/bases/centers",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/bases/centers/center_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/bases/centers/center_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/bases/centers/center_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/bases/centers/center_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/brains",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/treasure/brains/center_brain", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/walls",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/lava_wall", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/entrance_wall", var2), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/walls/outer",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/outer/top_corner", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/outer/mid_corner", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/outer/bottom_corner", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/outer/outer_wall", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/outer/medium_outer_wall", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/outer/tall_outer_wall", var2), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/walls/bottom",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/bottom/wall_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/bottom/wall_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/bottom/wall_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/bottom/wall_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/walls/mid",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/mid/wall_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/mid/wall_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/mid/wall_2", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/walls/top",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/top/main_entrance", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/top/wall_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/walls/top/wall_1", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/connectors",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/connectors/center_to_wall_middle", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/connectors/center_to_wall_top", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/connectors/center_to_wall_top_entrance", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/entrances",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/treasure/entrances/entrance_0", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/ramparts",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/ramparts/mid_wall_main", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/ramparts/mid_wall_side", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/ramparts/bottom_wall_0", var3), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/ramparts/top_wall", var4), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/ramparts/lava_basin_side", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/ramparts/lava_basin_main", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/corners/bottom",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/bottom/corner_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/bottom/corner_1", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/corners/edges",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/edges/bottom", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/edges/middle", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/edges/top", var2), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/corners/middle",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/middle/corner_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/middle/corner_1", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/corners/top",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/top/corner_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/corners/top/corner_1", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/extensions/large_pool",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/empty", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/empty", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/fire_room", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/large_bridge_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/large_bridge_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/large_bridge_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/large_bridge_3", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/roofed_bridge", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/empty", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/extensions/small_pool",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/empty", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/fire_room", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/empty", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/small_bridge_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/small_bridge_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/small_bridge_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/small_bridge_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/extensions/houses",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/house_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/extensions/house_1", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/treasure/roofs",
            new StructureTemplatePool(
                var7,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/treasure/roofs/wall_roof", var5), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/roofs/corner_roof", var5), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/roofs/center_roof", var5), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
