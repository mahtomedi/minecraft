package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionHousingUnitsPools {
    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureProcessorList> var0 = param0.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> var1 = var0.getOrThrow(ProcessorLists.HOUSING);
        HolderGetter<StructureTemplatePool> var2 = param0.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> var3 = var2.getOrThrow(Pools.EMPTY);
        Pools.register(
            param0,
            "bastion/units/center_pieces",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_2", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/pathways",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/pathways/pathway_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/pathways/pathway_wall_0", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/walls/wall_bases",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/walls/wall_base", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/walls/connected_wall", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/stages/stage_0",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/stages/stage_1",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/stages/rot/stage_1",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/stages/rot/stage_1_0", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/stages/stage_2",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_2_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_2_1", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/stages/stage_3",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_2", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/fillers/stage_0",
            new StructureTemplatePool(
                var3, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/fillers/stage_0", var1), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/edges",
            new StructureTemplatePool(
                var3, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/edges/edge_0", var1), 1)), StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/wall_units",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/wall_units/unit_0", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/edge_wall_units",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/wall_units/edge_0_large", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/ramparts",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_0", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_1", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_2", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/large_ramparts",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_0", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/units/rampart_plates",
            new StructureTemplatePool(
                var3,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/rampart_plates/plate_0", var1), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
