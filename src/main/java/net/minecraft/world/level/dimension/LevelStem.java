package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class LevelStem {
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    DimensionType.CODEC.fieldOf("type").forGetter(LevelStem::typeHolder),
                    ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)
                )
                .apply(param0, param0.stable(LevelStem::new))
    );
    public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(OVERWORLD, NETHER, END);
    private final Holder<DimensionType> type;
    private final ChunkGenerator generator;

    public LevelStem(Holder<DimensionType> param0, ChunkGenerator param1) {
        this.type = param0;
        this.generator = param1;
    }

    public Holder<DimensionType> typeHolder() {
        return this.type;
    }

    public ChunkGenerator generator() {
        return this.generator;
    }

    public static Stream<ResourceKey<LevelStem>> keysInOrder(Stream<ResourceKey<LevelStem>> param0) {
        return Stream.concat(BUILTIN_ORDER.stream(), param0.filter(param0x -> !BUILTIN_ORDER.contains(param0x)));
    }

    public static Registry<LevelStem> sortMap(Registry<LevelStem> param0) {
        WritableRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        keysInOrder(param0.registryKeySet().stream()).forEach(param2 -> {
            LevelStem var0x = param0.get(param2);
            if (var0x != null) {
                var0.register(param2, var0x, param0.lifecycle(var0x));
            }

        });
        return var0;
    }

    public static boolean stable(Registry<LevelStem> param0) {
        if (param0.size() != BUILTIN_ORDER.size()) {
            return false;
        } else {
            Optional<LevelStem> var0 = param0.getOptional(OVERWORLD);
            Optional<LevelStem> var1 = param0.getOptional(NETHER);
            Optional<LevelStem> var2 = param0.getOptional(END);
            if (!var0.isEmpty() && !var1.isEmpty() && !var2.isEmpty()) {
                if (!var0.get().typeHolder().is(BuiltinDimensionTypes.OVERWORLD) && !var0.get().typeHolder().is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
                    return false;
                } else if (!var1.get().typeHolder().is(BuiltinDimensionTypes.NETHER)) {
                    return false;
                } else if (!var2.get().typeHolder().is(BuiltinDimensionTypes.END)) {
                    return false;
                } else if (var1.get().generator() instanceof NoiseBasedChunkGenerator var3 && var2.get().generator() instanceof NoiseBasedChunkGenerator var4) {
                    if (!var3.stable(NoiseGeneratorSettings.NETHER)) {
                        return false;
                    } else if (!var4.stable(NoiseGeneratorSettings.END)) {
                        return false;
                    } else if (!(var3.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
                        return false;
                    } else {
                        MultiNoiseBiomeSource var5 = (MultiNoiseBiomeSource)var3.getBiomeSource();
                        if (!var5.stable(MultiNoiseBiomeSource.Preset.NETHER)) {
                            return false;
                        } else {
                            BiomeSource var6 = var0.get().generator().getBiomeSource();
                            if (var6 instanceof MultiNoiseBiomeSource && !((MultiNoiseBiomeSource)var6).stable(MultiNoiseBiomeSource.Preset.OVERWORLD)) {
                                return false;
                            } else {
                                return var4.getBiomeSource() instanceof TheEndBiomeSource;
                            }
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
}
