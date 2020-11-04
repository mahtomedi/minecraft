package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkBiomeContainer implements BiomeManager.NoiseBiomeSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int WIDTH_BITS = (int)Math.round(Math.log(16.0) / Math.log(2.0)) - 2;
    private static final int HEIGHT_BITS = (int)Math.round(Math.log(256.0) / Math.log(2.0)) - 2;
    public static final int BIOMES_SIZE = 1 << WIDTH_BITS + WIDTH_BITS + HEIGHT_BITS;
    public static final int HORIZONTAL_MASK = (1 << WIDTH_BITS) - 1;
    public static final int VERTICAL_MASK = (1 << HEIGHT_BITS) - 1;
    private final IdMap<Biome> biomeRegistry;
    private final Biome[] biomes;

    public ChunkBiomeContainer(IdMap<Biome> param0, Biome[] param1) {
        this.biomeRegistry = param0;
        this.biomes = param1;
    }

    private ChunkBiomeContainer(IdMap<Biome> param0) {
        this(param0, new Biome[BIOMES_SIZE]);
    }

    @OnlyIn(Dist.CLIENT)
    public ChunkBiomeContainer(IdMap<Biome> param0, int[] param1) {
        this(param0);

        for(int var0 = 0; var0 < this.biomes.length; ++var0) {
            int var1 = param1[var0];
            Biome var2 = param0.byId(var1);
            if (var2 == null) {
                LOGGER.warn("Received invalid biome id: {}", var1);
                this.biomes[var0] = param0.byId(0);
            } else {
                this.biomes[var0] = var2;
            }
        }

    }

    public ChunkBiomeContainer(IdMap<Biome> param0, ChunkPos param1, BiomeSource param2) {
        this(param0);
        int var0 = param1.getMinBlockX() >> 2;
        int var1 = param1.getMinBlockZ() >> 2;

        for(int var2 = 0; var2 < this.biomes.length; ++var2) {
            int var3 = var2 & HORIZONTAL_MASK;
            int var4 = var2 >> WIDTH_BITS + WIDTH_BITS & VERTICAL_MASK;
            int var5 = var2 >> WIDTH_BITS & HORIZONTAL_MASK;
            this.biomes[var2] = param2.getNoiseBiome(var0 + var3, var4, var1 + var5);
        }

    }

    public ChunkBiomeContainer(IdMap<Biome> param0, ChunkPos param1, BiomeSource param2, @Nullable int[] param3) {
        this(param0);
        int var0 = param1.getMinBlockX() >> 2;
        int var1 = param1.getMinBlockZ() >> 2;
        if (param3 != null) {
            for(int var2 = 0; var2 < param3.length; ++var2) {
                this.biomes[var2] = param0.byId(param3[var2]);
                if (this.biomes[var2] == null) {
                    int var3 = var2 & HORIZONTAL_MASK;
                    int var4 = var2 >> WIDTH_BITS + WIDTH_BITS & VERTICAL_MASK;
                    int var5 = var2 >> WIDTH_BITS & HORIZONTAL_MASK;
                    this.biomes[var2] = param2.getNoiseBiome(var0 + var3, var4, var1 + var5);
                }
            }
        } else {
            for(int var6 = 0; var6 < this.biomes.length; ++var6) {
                int var7 = var6 & HORIZONTAL_MASK;
                int var8 = var6 >> WIDTH_BITS + WIDTH_BITS & VERTICAL_MASK;
                int var9 = var6 >> WIDTH_BITS & HORIZONTAL_MASK;
                this.biomes[var6] = param2.getNoiseBiome(var0 + var7, var8, var1 + var9);
            }
        }

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
        int var1 = Mth.clamp(param1, 0, VERTICAL_MASK);
        int var2 = param2 & HORIZONTAL_MASK;
        return this.biomes[var1 << WIDTH_BITS + WIDTH_BITS | var2 << WIDTH_BITS | var0];
    }
}
