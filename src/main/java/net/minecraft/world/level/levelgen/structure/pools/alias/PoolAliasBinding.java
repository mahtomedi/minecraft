package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public interface PoolAliasBinding {
    Codec<PoolAliasBinding> CODEC = BuiltInRegistries.POOL_ALIAS_BINDING_TYPE.byNameCodec().dispatch(PoolAliasBinding::codec, Function.identity());

    void forEachResolved(RandomSource var1, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> var2);

    Stream<ResourceKey<StructureTemplatePool>> allTargets();

    static Direct direct(String param0, String param1) {
        return direct(Pools.createKey(param0), Pools.createKey(param1));
    }

    static Direct direct(ResourceKey<StructureTemplatePool> param0, ResourceKey<StructureTemplatePool> param1) {
        return new Direct(param0, param1);
    }

    static Random random(String param0, SimpleWeightedRandomList<String> param1) {
        SimpleWeightedRandomList.Builder<ResourceKey<StructureTemplatePool>> var0 = SimpleWeightedRandomList.builder();
        param1.unwrap().forEach(param1x -> var0.add(Pools.createKey(param1x.getData()), param1x.getWeight().asInt()));
        return random(Pools.createKey(param0), var0.build());
    }

    static Random random(ResourceKey<StructureTemplatePool> param0, SimpleWeightedRandomList<ResourceKey<StructureTemplatePool>> param1) {
        return new Random(param0, param1);
    }

    static RandomGroup randomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> param0) {
        return new RandomGroup(param0);
    }

    Codec<? extends PoolAliasBinding> codec();
}
