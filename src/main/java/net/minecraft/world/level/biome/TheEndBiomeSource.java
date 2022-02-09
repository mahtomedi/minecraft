package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(param0x -> null),
                    Codec.LONG.fieldOf("seed").stable().forGetter(param0x -> param0x.seed)
                )
                .apply(param0, param0.stable(TheEndBiomeSource::new))
    );
    private static final float ISLAND_THRESHOLD = -0.9F;
    public static final int ISLAND_CHUNK_DISTANCE = 64;
    private static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
    private final SimplexNoise islandNoise;
    private final long seed;
    private final Holder<Biome> end;
    private final Holder<Biome> highlands;
    private final Holder<Biome> midlands;
    private final Holder<Biome> islands;
    private final Holder<Biome> barrens;

    public TheEndBiomeSource(Registry<Biome> param0, long param1) {
        this(
            param1,
            param0.getOrCreateHolder(Biomes.THE_END),
            param0.getOrCreateHolder(Biomes.END_HIGHLANDS),
            param0.getOrCreateHolder(Biomes.END_MIDLANDS),
            param0.getOrCreateHolder(Biomes.SMALL_END_ISLANDS),
            param0.getOrCreateHolder(Biomes.END_BARRENS)
        );
    }

    private TheEndBiomeSource(long param0, Holder<Biome> param1, Holder<Biome> param2, Holder<Biome> param3, Holder<Biome> param4, Holder<Biome> param5) {
        super(ImmutableList.of(param1, param2, param3, param4, param5));
        this.seed = param0;
        this.end = param1;
        this.highlands = param2;
        this.midlands = param3;
        this.islands = param4;
        this.barrens = param5;
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(param0));
        var0.consumeCount(17292);
        this.islandNoise = new SimplexNoise(var0);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long param0) {
        return new TheEndBiomeSource(param0, this.end, this.highlands, this.midlands, this.islands, this.barrens);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2, Climate.Sampler param3) {
        int var0 = param0 >> 2;
        int var1 = param2 >> 2;
        if ((long)var0 * (long)var0 + (long)var1 * (long)var1 <= 4096L) {
            return this.end;
        } else {
            float var2 = getHeightValue(this.islandNoise, var0 * 2 + 1, var1 * 2 + 1);
            if (var2 > 40.0F) {
                return this.highlands;
            } else if (var2 >= 0.0F) {
                return this.midlands;
            } else {
                return var2 < -20.0F ? this.islands : this.barrens;
            }
        }
    }

    public boolean stable(long param0) {
        return this.seed == param0;
    }

    public static float getHeightValue(SimplexNoise param0, int param1, int param2) {
        int var0 = param1 / 2;
        int var1 = param2 / 2;
        int var2 = param1 % 2;
        int var3 = param2 % 2;
        float var4 = 100.0F - Mth.sqrt((float)(param1 * param1 + param2 * param2)) * 8.0F;
        var4 = Mth.clamp(var4, -100.0F, 80.0F);

        for(int var5 = -12; var5 <= 12; ++var5) {
            for(int var6 = -12; var6 <= 12; ++var6) {
                long var7 = (long)(var0 + var5);
                long var8 = (long)(var1 + var6);
                if (var7 * var7 + var8 * var8 > 4096L && param0.getValue((double)var7, (double)var8) < -0.9F) {
                    float var9 = (Mth.abs((float)var7) * 3439.0F + Mth.abs((float)var8) * 147.0F) % 13.0F + 9.0F;
                    float var10 = (float)(var2 - var5 * 2);
                    float var11 = (float)(var3 - var6 * 2);
                    float var12 = 100.0F - Mth.sqrt(var10 * var10 + var11 * var11) * var9;
                    var12 = Mth.clamp(var12, -100.0F, 80.0F);
                    var4 = Math.max(var4, var12);
                }
            }
        }

        return var4;
    }
}
