package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class PoolAliasBindings {
    public static Codec<? extends PoolAliasBinding> bootstrap(Registry<Codec<? extends PoolAliasBinding>> param0) {
        Registry.register(param0, "random", Random.CODEC);
        Registry.register(param0, "random_group", RandomGroup.CODEC);
        return Registry.register(param0, "direct", Direct.CODEC);
    }

    public static void registerTargetsAsPools(
        BootstapContext<StructureTemplatePool> param0, Holder<StructureTemplatePool> param1, List<PoolAliasBinding> param2
    ) {
        param2.stream()
            .flatMap(PoolAliasBinding::allTargets)
            .map(param0x -> param0x.location().getPath())
            .forEach(
                param2x -> Pools.register(
                        param0,
                        param2x,
                        new StructureTemplatePool(param1, List.of(Pair.of(StructurePoolElement.single(param2x), 1)), StructureTemplatePool.Projection.RIGID)
                    )
            );
    }
}
