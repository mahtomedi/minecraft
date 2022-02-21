package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
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

    public static Registry<LevelStem> sortMap(Registry<LevelStem> param0) {
        WritableRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);

        for(ResourceKey<LevelStem> var1 : BUILTIN_ORDER) {
            LevelStem var2 = param0.get(var1);
            if (var2 != null) {
                var0.register(var1, var2, param0.lifecycle(var2));
            }
        }

        for(Entry<ResourceKey<LevelStem>, LevelStem> var3 : param0.entrySet()) {
            ResourceKey<LevelStem> var4 = var3.getKey();
            if (!BUILTIN_ORDER.contains(var4)) {
                var0.register(var4, var3.getValue(), param0.lifecycle(var3.getValue()));
            }
        }

        return var0;
    }

    public static boolean stable(long param0, Registry<LevelStem> param1) {
        if (param1.size() != BUILTIN_ORDER.size()) {
            return false;
        } else {
            Optional<LevelStem> var0 = param1.getOptional(OVERWORLD);
            Optional<LevelStem> var1 = param1.getOptional(NETHER);
            Optional<LevelStem> var2 = param1.getOptional(END);
            if (!var0.isEmpty() && !var1.isEmpty() && !var2.isEmpty()) {
                if (!var0.get().typeHolder().is(DimensionType.OVERWORLD_LOCATION) && !var0.get().typeHolder().is(DimensionType.OVERWORLD_CAVES_LOCATION)) {
                    return false;
                } else if (!var1.get().typeHolder().is(DimensionType.NETHER_LOCATION)) {
                    return false;
                } else if (!var2.get().typeHolder().is(DimensionType.END_LOCATION)) {
                    return false;
                } else if (var1.get().generator() instanceof NoiseBasedChunkGenerator var3 && var2.get().generator() instanceof NoiseBasedChunkGenerator var4) {
                    if (!var3.stable(param0, NoiseGeneratorSettings.NETHER)) {
                        return false;
                    } else if (!var4.stable(param0, NoiseGeneratorSettings.END)) {
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
                            } else if (!(var4.getBiomeSource() instanceof TheEndBiomeSource)) {
                                return false;
                            } else {
                                TheEndBiomeSource var7 = (TheEndBiomeSource)var4.getBiomeSource();
                                return var7.stable(param0);
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
