package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

record Direct(ResourceKey<StructureTemplatePool> alias, ResourceKey<StructureTemplatePool> target) implements PoolAliasBinding {
    static Codec<Direct> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("alias").forGetter(Direct::alias),
                    ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("target").forGetter(Direct::target)
                )
                .apply(param0, Direct::new)
    );

    @Override
    public void forEachResolved(RandomSource param0, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> param1) {
        param1.accept(this.alias, this.target);
    }

    @Override
    public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
        return Stream.of(this.target);
    }

    @Override
    public Codec<Direct> codec() {
        return CODEC;
    }
}
