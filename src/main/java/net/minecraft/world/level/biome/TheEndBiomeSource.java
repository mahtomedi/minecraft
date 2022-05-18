package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.DensityFunction;

public class TheEndBiomeSource extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(param0x -> null))
                .apply(param0, param0.stable(TheEndBiomeSource::new))
    );
    private final Holder<Biome> end;
    private final Holder<Biome> highlands;
    private final Holder<Biome> midlands;
    private final Holder<Biome> islands;
    private final Holder<Biome> barrens;

    public TheEndBiomeSource(Registry<Biome> param0) {
        this(
            param0.getOrCreateHolderOrThrow(Biomes.THE_END),
            param0.getOrCreateHolderOrThrow(Biomes.END_HIGHLANDS),
            param0.getOrCreateHolderOrThrow(Biomes.END_MIDLANDS),
            param0.getOrCreateHolderOrThrow(Biomes.SMALL_END_ISLANDS),
            param0.getOrCreateHolderOrThrow(Biomes.END_BARRENS)
        );
    }

    private TheEndBiomeSource(Holder<Biome> param0, Holder<Biome> param1, Holder<Biome> param2, Holder<Biome> param3, Holder<Biome> param4) {
        super(ImmutableList.of(param0, param1, param2, param3, param4));
        this.end = param0;
        this.highlands = param1;
        this.midlands = param2;
        this.islands = param3;
        this.barrens = param4;
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
