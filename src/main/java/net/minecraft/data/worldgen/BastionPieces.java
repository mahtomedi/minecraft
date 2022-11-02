package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionPieces {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("bastion/starts");

    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureProcessorList> var0 = param0.lookup(Registry.PROCESSOR_LIST_REGISTRY);
        Holder<StructureProcessorList> var1 = var0.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        HolderGetter<StructureTemplatePool> var2 = param0.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> var3 = var2.getOrThrow(Pools.EMPTY);
        param0.register(
            START,
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/air_base", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/air_base", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/big_air_full", var1), 1),
                    Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_base", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        BastionHousingUnitsPools.bootstrap(param0);
        BastionHoglinStablePools.bootstrap(param0);
        BastionTreasureRoomPools.bootstrap(param0);
        BastionBridgePools.bootstrap(param0);
        BastionSharedPools.bootstrap(param0);
    }
}
