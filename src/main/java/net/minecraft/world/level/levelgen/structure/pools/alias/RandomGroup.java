package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

record RandomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> groups) implements PoolAliasBinding {
    static Codec<RandomGroup> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(SimpleWeightedRandomList.wrappedCodec(Codec.list(PoolAliasBinding.CODEC)).fieldOf("groups").forGetter(RandomGroup::groups))
                .apply(param0, RandomGroup::new)
    );

    @Override
    public void forEachResolved(RandomSource param0, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> param1) {
        this.groups.getRandom(param0).ifPresent(param2 -> param2.getData().forEach(param2x -> param2x.forEachResolved(param0, param1)));
    }

    @Override
    public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
        return this.groups.unwrap().stream().flatMap(param0 -> param0.getData().stream()).flatMap(PoolAliasBinding::allTargets);
    }

    @Override
    public Codec<RandomGroup> codec() {
        return CODEC;
    }
}
