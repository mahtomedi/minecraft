package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

public class CheckerboardColumnBiomeSource extends BiomeSource {
    public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(param0x -> param0x.allowedBiomes),
                    Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter(param0x -> param0x.size)
                )
                .apply(param0, CheckerboardColumnBiomeSource::new)
    );
    private final HolderSet<Biome> allowedBiomes;
    private final int bitShift;
    private final int size;

    public CheckerboardColumnBiomeSource(HolderSet<Biome> param0, int param1) {
        super(param0.stream());
        this.allowedBiomes = param0;
        this.bitShift = param1 + 2;
        this.size = param1;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2, Climate.Sampler param3) {
        return this.allowedBiomes.get(Math.floorMod((param0 >> this.bitShift) + (param2 >> this.bitShift), this.allowedBiomes.size()));
    }
}
