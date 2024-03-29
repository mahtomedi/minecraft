package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.DensityFunction;

public class TheEndBiomeSource extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    RegistryOps.retrieveElement(Biomes.THE_END),
                    RegistryOps.retrieveElement(Biomes.END_HIGHLANDS),
                    RegistryOps.retrieveElement(Biomes.END_MIDLANDS),
                    RegistryOps.retrieveElement(Biomes.SMALL_END_ISLANDS),
                    RegistryOps.retrieveElement(Biomes.END_BARRENS)
                )
                .apply(param0, param0.stable(TheEndBiomeSource::new))
    );
    private final Holder<Biome> end;
    private final Holder<Biome> highlands;
    private final Holder<Biome> midlands;
    private final Holder<Biome> islands;
    private final Holder<Biome> barrens;

    public static TheEndBiomeSource create(HolderGetter<Biome> param0) {
        return new TheEndBiomeSource(
            param0.getOrThrow(Biomes.THE_END),
            param0.getOrThrow(Biomes.END_HIGHLANDS),
            param0.getOrThrow(Biomes.END_MIDLANDS),
            param0.getOrThrow(Biomes.SMALL_END_ISLANDS),
            param0.getOrThrow(Biomes.END_BARRENS)
        );
    }

    private TheEndBiomeSource(Holder<Biome> param0, Holder<Biome> param1, Holder<Biome> param2, Holder<Biome> param3, Holder<Biome> param4) {
        this.end = param0;
        this.highlands = param1;
        this.midlands = param2;
        this.islands = param3;
        this.barrens = param4;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(this.end, this.highlands, this.midlands, this.islands, this.barrens);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2, Climate.Sampler param3) {
        int var0 = QuartPos.toBlock(param0);
        int var1 = QuartPos.toBlock(param1);
        int var2 = QuartPos.toBlock(param2);
        int var3 = SectionPos.blockToSectionCoord(var0);
        int var4 = SectionPos.blockToSectionCoord(var2);
        if ((long)var3 * (long)var3 + (long)var4 * (long)var4 <= 4096L) {
            return this.end;
        } else {
            int var5 = (SectionPos.blockToSectionCoord(var0) * 2 + 1) * 8;
            int var6 = (SectionPos.blockToSectionCoord(var2) * 2 + 1) * 8;
            double var7 = param3.erosion().compute(new DensityFunction.SinglePointContext(var5, var1, var6));
            if (var7 > 0.25) {
                return this.highlands;
            } else if (var7 >= -0.0625) {
                return this.midlands;
            } else {
                return var7 < -0.21875 ? this.islands : this.barrens;
            }
        }
    }
}
