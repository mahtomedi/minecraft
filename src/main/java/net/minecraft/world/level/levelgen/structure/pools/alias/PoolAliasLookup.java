package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

@FunctionalInterface
public interface PoolAliasLookup {
    PoolAliasLookup EMPTY = param0 -> param0;

    ResourceKey<StructureTemplatePool> lookup(ResourceKey<StructureTemplatePool> var1);

    static PoolAliasLookup create(List<PoolAliasBinding> param0, BlockPos param1, long param2) {
        if (param0.isEmpty()) {
            return EMPTY;
        } else {
            RandomSource var0 = RandomSource.create(param2).forkPositional().at(param1);
            Builder<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> var1 = ImmutableMap.builder();
            param0.forEach(param2x -> param2x.forEachResolved(var0, var1::put));
            Map<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> var2 = var1.build();
            return param1x -> Objects.requireNonNull(var2.getOrDefault(param1x, param1x), () -> "alias " + param1x + " was mapped to null value");
        }
    }
}
