package net.minecraft.world.level.levelgen;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OverworldLevelSource extends NoiseBasedChunkGenerator<OverworldGeneratorSettings> {
    private static final float[] BIOME_WEIGHTS = Util.make(new float[25], param0 -> {
        for(int var0 = -2; var0 <= 2; ++var0) {
            for(int var1 = -2; var1 <= 2; ++var1) {
                float var2 = 10.0F / Mth.sqrt((float)(var0 * var0 + var1 * var1) + 0.2F);
                param0[var0 + 2 + (var1 + 2) * 5] = var2;
            }
        }

    });
    private final PerlinNoise depthNoise;
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final PatrolSpawner patrolSpawner = new PatrolSpawner();
    private final CatSpawner catSpawner = new CatSpawner();
    private final VillageSiege villageSiege = new VillageSiege();
    private final OverworldGeneratorSettings settings;

    public OverworldLevelSource(BiomeSource param0, long param1, OverworldGeneratorSettings param2) {
        super(param0, param1, param2, 4, 8, 256, true);
        this.settings = param2;
        this.random.consumeCount(2620);
        this.depthNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ChunkGenerator withSeed(long param0) {
        return new OverworldLevelSource(this.biomeSource.withSeed(param0), param0, this.settings);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion param0) {
        int var0 = param0.getCenterX();
        int var1 = param0.getCenterZ();
        Biome var2 = param0.getBiome(new ChunkPos(var0, var1).getWorldPosition());
        WorldgenRandom var3 = new WorldgenRandom();
        var3.setDecorationSeed(param0.getSeed(), var0 << 4, var1 << 4);
        NaturalSpawner.spawnMobsForChunkGeneration(param0, var2, var0, var1, var3);
    }

    @Override
    protected void fillNoiseColumn(double[] param0, int param1, int param2) {
        double var0 = 684.412F;
        double var1 = 684.412F;
        double var2 = 8.555149841308594;
        double var3 = 4.277574920654297;
        int var4 = -10;
        int var5 = 3;
        this.fillNoiseColumn(param0, param1, param2, 684.412F, 684.412F, 8.555149841308594, 4.277574920654297, 3, -10);
    }

    @Override
    protected double getYOffset(double param0, double param1, int param2) {
        double var0 = 8.5;
        double var1 = ((double)param2 - (8.5 + param0 * 8.5 / 8.0 * 4.0)) * 12.0 * 128.0 / 256.0 / param1;
        if (var1 < 0.0) {
            var1 *= 4.0;
        }

        return var1;
    }

    @Override
    protected double[] getDepthAndScale(int param0, int param1) {
        double[] var0 = new double[2];
        float var1 = 0.0F;
        float var2 = 0.0F;
        float var3 = 0.0F;
        int var4 = 2;
        int var5 = this.getSeaLevel();
        float var6 = this.biomeSource.getNoiseBiome(param0, var5, param1).getDepth();

        for(int var7 = -2; var7 <= 2; ++var7) {
            for(int var8 = -2; var8 <= 2; ++var8) {
                Biome var9 = this.biomeSource.getNoiseBiome(param0 + var7, var5, param1 + var8);
                float var10 = var9.getDepth();
                float var11 = var9.getScale();
                if (this.settings.isAmplified() && var10 > 0.0F) {
                    var10 = 1.0F + var10 * 2.0F;
                    var11 = 1.0F + var11 * 4.0F;
                }

                float var12 = BIOME_WEIGHTS[var7 + 2 + (var8 + 2) * 5] / (var10 + 2.0F);
                if (var9.getDepth() > var6) {
                    var12 /= 2.0F;
                }

                var1 += var11 * var12;
                var2 += var10 * var12;
                var3 += var12;
            }
        }

        var1 /= var3;
        var2 /= var3;
        var1 = var1 * 0.9F + 0.1F;
        var2 = (var2 * 4.0F - 1.0F) / 8.0F;
        var0[0] = (double)var2 + this.getRdepth(param0, param1);
        var0[1] = (double)var1;
        return var0;
    }

    private double getRdepth(int param0, int param1) {
        double var0 = this.depthNoise.getValue((double)(param0 * 200), 10.0, (double)(param1 * 200), 1.0, 0.0, true) * 65535.0 / 8000.0;
        if (var0 < 0.0) {
            var0 = -var0 * 0.3;
        }

        var0 = var0 * 3.0 - 2.0;
        if (var0 < 0.0) {
            var0 /= 28.0;
        } else {
            if (var0 > 1.0) {
                var0 = 1.0;
            }

            var0 /= 40.0;
        }

        return var0;
    }

    @Override
    public List<Biome.SpawnerData> getMobsAt(Biome param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        if (Feature.SWAMP_HUT.isSwamphut(param1, param3)) {
            if (param2 == MobCategory.MONSTER) {
                return Feature.SWAMP_HUT.getSpecialEnemies();
            }

            if (param2 == MobCategory.CREATURE) {
                return Feature.SWAMP_HUT.getSpecialAnimals();
            }
        } else if (param2 == MobCategory.MONSTER) {
            if (Feature.PILLAGER_OUTPOST.isInsideBoundingFeature(param1, param3)) {
                return Feature.PILLAGER_OUTPOST.getSpecialEnemies();
            }

            if (Feature.OCEAN_MONUMENT.isInsideBoundingFeature(param1, param3)) {
                return Feature.OCEAN_MONUMENT.getSpecialEnemies();
            }
        }

        return super.getMobsAt(param0, param1, param2, param3);
    }

    @Override
    public void tickCustomSpawners(ServerLevel param0, boolean param1, boolean param2) {
        this.phantomSpawner.tick(param0, param1, param2);
        this.patrolSpawner.tick(param0, param1, param2);
        this.catSpawner.tick(param0, param1, param2);
        this.villageSiege.tick(param0, param1, param2);
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }
}
