package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionHoglinStablePools {
    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureProcessorList> var0 = param0.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> var1 = var0.getOrThrow(ProcessorLists.STABLE_DEGRADATION);
        Holder<StructureProcessorList> var2 = var0.getOrThrow(ProcessorLists.SIDE_WALL_DEGRADATION);
        HolderGetter<StructureTemplatePool> var3 = param0.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> var4 = var3.getOrThrow(Pools.EMPTY);
        Pools.register(
            param0,
            "bastion/hoglin_stable/starting_pieces",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_3", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_4", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/mirrored_starting_pieces",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_0_mirrored", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_1_mirrored", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_2_mirrored", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_3_mirrored", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_4_mirrored", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/wall_bases",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/hoglin_stable/walls/wall_base", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/walls",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/walls/side_wall_0", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/walls/side_wall_1", var2), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/stairs",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_3", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_4", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_3", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_4", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_3", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_4", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/small_stables/inner",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/small_stables/outer",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/large_stables/inner",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_3", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_4", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/large_stables/outer",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_3", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_4", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/posts",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/posts/stair_post", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/posts/end_post", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/ramparts",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/ramparts/ramparts_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/ramparts/ramparts_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/ramparts/ramparts_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/rampart_plates",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/hoglin_stable/rampart_plates/rampart_plate_1", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/hoglin_stable/connectors",
            new StructureTemplatePool(
                var4,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/hoglin_stable/connectors/end_post_connector", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
