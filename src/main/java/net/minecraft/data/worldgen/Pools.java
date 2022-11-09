package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools {
    public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

    public static ResourceKey<StructureTemplatePool> createKey(String param0) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(param0));
    }

    public static void register(BootstapContext<StructureTemplatePool> param0, String param1, StructureTemplatePool param2) {
        param0.register(createKey(param1), param2);
    }

    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        HolderGetter<StructureTemplatePool> var0 = param0.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> var1 = var0.getOrThrow(EMPTY);
        param0.register(EMPTY, new StructureTemplatePool(var1, ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
        BastionPieces.bootstrap(param0);
        PillagerOutpostPools.bootstrap(param0);
        VillagePools.bootstrap(param0);
        AncientCityStructurePieces.bootstrap(param0);
    }
}
