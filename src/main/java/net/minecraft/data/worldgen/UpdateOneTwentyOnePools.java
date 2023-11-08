package net.minecraft.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class UpdateOneTwentyOnePools {
    public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

    public static ResourceKey<StructureTemplatePool> createKey(String param0) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(param0));
    }

    public static void register(BootstapContext<StructureTemplatePool> param0, String param1, StructureTemplatePool param2) {
        Pools.register(param0, param1, param2);
    }

    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        TrialChambersStructurePools.bootstrap(param0);
    }
}
