package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;

public interface StructureProcessorType<P extends StructureProcessor> {
    StructureProcessorType<BlockIgnoreProcessor> BLOCK_IGNORE = register("block_ignore", BlockIgnoreProcessor.CODEC);
    StructureProcessorType<BlockRotProcessor> BLOCK_ROT = register("block_rot", BlockRotProcessor.CODEC);
    StructureProcessorType<GravityProcessor> GRAVITY = register("gravity", GravityProcessor.CODEC);
    StructureProcessorType<JigsawReplacementProcessor> JIGSAW_REPLACEMENT = register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
    StructureProcessorType<RuleProcessor> RULE = register("rule", RuleProcessor.CODEC);
    StructureProcessorType<NopProcessor> NOP = register("nop", NopProcessor.CODEC);
    StructureProcessorType<BlockAgeProcessor> BLOCK_AGE = register("block_age", BlockAgeProcessor.CODEC);
    StructureProcessorType<BlackstoneReplaceProcessor> BLACKSTONE_REPLACE = register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
    StructureProcessorType<LavaSubmergedBlockProcessor> LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
    Codec<StructureProcessor> SINGLE_CODEC = Registry.STRUCTURE_PROCESSOR
        .dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
    MapCodec<ImmutableList<StructureProcessor>> DIRECT_CODEC = handleDefaultField(
        "processors", SINGLE_CODEC.listOf().xmap(ImmutableList::copyOf, Function.identity())
    );
    Codec<Supplier<ImmutableList<StructureProcessor>>> LIST_CODEC = RegistryFileCodec.create(Registry.PROCESSOR_LIST_REGISTRY, DIRECT_CODEC);

    Codec<P> codec();

    static <E> MapCodec<E> handleDefaultField(final String param0, Codec<E> param1) {
        final MapCodec<E> var0 = param1.fieldOf(param0);
        return new MapCodec<E>() {
            @Override
            public <O> Stream<O> keys(DynamicOps<O> param0x) {
                return var0.keys(param0);
            }

            @Override
            public <O> DataResult<E> decode(DynamicOps<O> param0x, MapLike<O> param1) {
                return var0.decode(param0, param1);
            }

            @Override
            public <O> RecordBuilder<O> encode(E param0x, DynamicOps<O> param1, RecordBuilder<O> param2) {
                return var0.encode(param0, param1, param2);
            }

            @Override
            public Codec<E> codec() {
                final Codec<E> var0 = super.codec();
                return new Codec<E>() {
                    @Override
                    public <O> DataResult<Pair<E, O>> decode(DynamicOps<O> param0x, O param1) {
                        if (param0.compressMaps()) {
                            return var0.decode(param0, param1);
                        } else {
                            DataResult<MapLike<O>> var0 = param0.getMap(param1);
                            MapLike<O> var1 = var0.get()
                                .map(
                                    (Function<? super MapLike<O>, ? extends MapLike<O>>)(param0xx -> param0xx),
                                    param3 -> MapLike.forMap(ImmutableMap.of(param0.createString(param0), param1), param0)
                                );
                            return var0.decode(param0, var1).map(param1x -> Pair.of(param1x, param1));
                        }
                    }

                    @Override
                    public <O> DataResult<O> encode(E param0x, DynamicOps<O> param1, O param2) {
                        return var0.encode(param0, param1, param2);
                    }
                };
            }
        };
    }

    static <P extends StructureProcessor> StructureProcessorType<P> register(String param0, Codec<P> param1) {
        return Registry.register(Registry.STRUCTURE_PROCESSOR, param0, () -> param1);
    }
}
