package net.minecraft.world.level.chunk;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkBiomeContainer implements BiomeManager.NoiseBiomeSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int WIDTH_BITS = Mth.ceillog2(16) - 2;
    private static final int HORIZONTAL_MASK = (1 << WIDTH_BITS) - 1;
    public static final int MAX_SIZE = 1 << WIDTH_BITS + WIDTH_BITS + DimensionType.BITS_FOR_Y - 2;
    private final IdMap<Biome> biomeRegistry;
    private final Biome[] biomes;
    private final int quartMinY;
    private final int quartHeight;

    protected ChunkBiomeContainer(IdMap<Biome> param0, LevelHeightAccessor param1, Biome[] param2) {
        this.biomeRegistry = param0;
        this.biomes = param2;
        this.quartMinY = QuartPos.fromBlock(param1.getMinBuildHeight());
        this.quartHeight = QuartPos.fromBlock(param1.getHeight()) - 1;
    }

    public ChunkBiomeContainer(IdMap<Biome> param0, LevelHeightAccessor param1, int[] param2) {
        this(param0, param1, new Biome[param2.length]);
        int var0 = -1;

        for(int var1 = 0; var1 < this.biomes.length; ++var1) {
            int var2 = param2[var1];
            Biome var3 = param0.byId(var2);
            if (var3 == null) {
                if (var0 == -1) {
                    var0 = var1;
                }

                this.biomes[var1] = param0.byId(0);
            } else {
                this.biomes[var1] = var3;
            }
        }

        if (var0 != -1) {
            LOGGER.warn("Invalid biome data received, starting from {}: {}", var0, Arrays.toString(param2));
        }

    }

    public ChunkBiomeContainer(IdMap<Biome> param0, LevelHeightAccessor param1, ChunkPos param2, BiomeSource param3) {
        this(param0, param1, param2, param3, null);
    }

    public ChunkBiomeContainer(IdMap<Biome> param0, LevelHeightAccessor param1, ChunkPos param2, BiomeSource param3, @Nullable int[] param4) {
        this(param0, param1, new Biome[(1 << WIDTH_BITS + WIDTH_BITS) * ceilDiv(param1.getHeight(), 4)]);
        int var0 = QuartPos.fromBlock(param2.getMinBlockX());
        int var1 = this.quartMinY;
        int var2 = QuartPos.fromBlock(param2.getMinBlockZ());
        if (param4 != null) {
            for(int var3 = 0; var3 < param4.length; ++var3) {
                this.biomes[var3] = param0.byId(param4[var3]);
                if (this.biomes[var3] == null) {
                    this.biomes[var3] = biomeForIndex(param3, var0, var1, var2, var3);
                }
            }
        } else {
            for(int var4 = 0; var4 < this.biomes.length; ++var4) {
                this.biomes[var4] = biomeForIndex(param3, var0, var1, var2, var4);
            }
        }

    }

    private static int ceilDiv(int param0, int param1) {
        return (param0 + param1 - 1) / param1;
    }

    private static Biome biomeForIndex(BiomeSource param0, int param1, int param2, int param3, int param4) {
        int var0 = param4 & HORIZONTAL_MASK;
        int var1 = param4 >> WIDTH_BITS + WIDTH_BITS;
        int var2 = param4 >> WIDTH_BITS & HORIZONTAL_MASK;
        return param0.getNoiseBiome(param1 + var0, param2 + var1, param3 + var2);
    }

    public int[] writeBiomes() {
        int[] var0 = new int[this.biomes.length];

        for(int var1 = 0; var1 < this.biomes.length; ++var1) {
            var0[var1] = this.biomeRegistry.getId(this.biomes[var1]);
        }

        return var0;
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        int var0 = param0 & HORIZONTAL_MASK;
        int var1 = Mth.clamp(param1 - this.quartMinY, 0, this.quartHeight);
        int var2 = param2 & HORIZONTAL_MASK;
        return this.biomes[var1 << WIDTH_BITS + WIDTH_BITS | var2 << WIDTH_BITS | var0];
    }
}
