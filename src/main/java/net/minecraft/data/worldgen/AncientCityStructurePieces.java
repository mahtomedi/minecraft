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

public class AncientCityStructurePieces {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("ancient_city/city_center");

    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureProcessorList> var0 = param0.lookup(Registry.PROCESSOR_LIST_REGISTRY);
        Holder<StructureProcessorList> var1 = var0.getOrThrow(ProcessorLists.ANCIENT_CITY_START_DEGRADATION);
        HolderGetter<StructureTemplatePool> var2 = param0.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> var3 = var2.getOrThrow(Pools.EMPTY);
        param0.register(
            START,
            new StructureTemplatePool(
                var3,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_1", var1), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_2", var1), 1),
                    Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_3", var1), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        AncientCityStructurePools.bootstrap(param0);
    }
}
