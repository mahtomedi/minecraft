package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class LevelStem {
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    DimensionType.CODEC
                        .fieldOf("type")
                        .flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck())
                        .forGetter(LevelStem::typeSupplier),
                    ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)
                )
                .apply(param0, param0.stable(LevelStem::new))
    );
    public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(OVERWORLD, NETHER, END));
    private final Supplier<DimensionType> type;
    private final ChunkGenerator generator;

    public LevelStem(Supplier<DimensionType> param0, ChunkGenerator param1) {
        this.type = param0;
        this.generator = param1;
    }

    public Supplier<DimensionType> typeSupplier() {
        return this.type;
    }

    public DimensionType type() {
        return this.type.get();
    }

    public ChunkGenerator generator() {
        return this.generator;
    }

    public static MappedRegistry<LevelStem> sortMap(MappedRegistry<LevelStem> param0) {
        MappedRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());

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

    public static boolean stable(long param0, MappedRegistry<LevelStem> param1) {
        List<Entry<ResourceKey<LevelStem>, LevelStem>> var0 = Lists.newArrayList(param1.entrySet());
        if (var0.size() != BUILTIN_ORDER.size()) {
            return false;
        } else {
            Entry<ResourceKey<LevelStem>, LevelStem> var1 = var0.get(0);
            Entry<ResourceKey<LevelStem>, LevelStem> var2 = var0.get(1);
            Entry<ResourceKey<LevelStem>, LevelStem> var3 = var0.get(2);
            if (var1.getKey() != OVERWORLD || var2.getKey() != NETHER || var3.getKey() != END) {
                return false;
            } else if (!var1.getValue().type().equalTo(DimensionType.DEFAULT_OVERWORLD) && var1.getValue().type() != DimensionType.DEFAULT_OVERWORLD_CAVES) {
                return false;
            } else if (!var2.getValue().type().equalTo(DimensionType.DEFAULT_NETHER)) {
                return false;
            } else if (!var3.getValue().type().equalTo(DimensionType.DEFAULT_END)) {
                return false;
            } else if (var2.getValue().generator() instanceof NoiseBasedChunkGenerator var4
                && var3.getValue().generator() instanceof NoiseBasedChunkGenerator var5) {
                if (!var4.stable(param0, NoiseGeneratorSettings.NETHER)) {
                    return false;
                } else if (!var5.stable(param0, NoiseGeneratorSettings.END)) {
                    return false;
                } else if (!(var4.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
                    return false;
                } else {
                    MultiNoiseBiomeSource var6 = (MultiNoiseBiomeSource)var4.getBiomeSource();
                    if (!var6.stable(param0)) {
                        return false;
                    } else if (!(var5.getBiomeSource() instanceof TheEndBiomeSource)) {
                        return false;
                    } else {
                        TheEndBiomeSource var7 = (TheEndBiomeSource)var5.getBiomeSource();
                        return var7.stable(param0);
                    }
                }
            } else {
                return false;
            }
        }
    }
}
