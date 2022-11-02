package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionBridgePools {
    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureProcessorList> var0 = param0.lookup(Registry.PROCESSOR_LIST_REGISTRY);
        Holder<StructureProcessorList> var1 = var0.getOrThrow(ProcessorLists.ENTRANCE_REPLACEMENT);
        Holder<StructureProcessorList> var2 = var0.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        Holder<StructureProcessorList> var3 = var0.getOrThrow(ProcessorLists.BRIDGE);
        Holder<StructureProcessorList> var4 = var0.getOrThrow(ProcessorLists.RAMPART_DEGRADATION);
        HolderGetter<StructureTemplatePool> var5 = param0.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> var6 = var5.getOrThrow(Pools.EMPTY);
        Pools.register(
            param0,
            "bastion/bridge/starting_pieces",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_face", var2), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/bridge/bridge_pieces",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/bridge/bridge_pieces/bridge", var3), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/bridge/legs",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/bridge/legs/leg_0", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/bridge/legs/leg_1", var2), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/bridge/walls",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/bridge/walls/wall_base_0", var4), 1),
                    Pair.of(StructurePoolElement.single("bastion/bridge/walls/wall_base_1", var4), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/bridge/ramparts",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/bridge/ramparts/rampart_0", var4), 1),
                    Pair.of(StructurePoolElement.single("bastion/bridge/ramparts/rampart_1", var4), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/bridge/rampart_plates",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/bridge/rampart_plates/plate_0", var4), 1)),
                StructureTemplatePool.Projection.RIGID
            )
        );
        Pools.register(
            param0,
            "bastion/bridge/connectors",
            new StructureTemplatePool(
                var6,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/bridge/connectors/back_bridge_top", var2), 1),
                    Pair.of(StructurePoolElement.single("bastion/bridge/connectors/back_bridge_bottom", var2), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
    }
}
