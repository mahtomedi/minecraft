package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

record Random(ResourceKey<StructureTemplatePool> alias, SimpleWeightedRandomList<ResourceKey<StructureTemplatePool>> targets) implements PoolAliasBinding {
    static Codec<Random> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("alias").forGetter(Random::alias),
                    SimpleWeightedRandomList.wrappedCodec(ResourceKey.codec(Registries.TEMPLATE_POOL)).fieldOf("targets").forGetter(Random::targets)
                )
                .apply(param0, Random::new)
    );

    @Override
    public void forEachResolved(RandomSource param0, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> param1) {
        this.targets.getRandom(param0).ifPresent(param1x -> param1.accept(this.alias, param1x.getData()));
    }

    @Override
    public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
        return this.targets.unwrap().stream().map(WeightedEntry.Wrapper::getData);
    }

    @Override
    public Codec<Random> codec() {
        return CODEC;
    }
}
