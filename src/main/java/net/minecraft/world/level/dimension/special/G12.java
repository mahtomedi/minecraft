package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G12 extends SpecialDimensionBase {
    private static final boolean[] FACE = new boolean[]{
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        false,
        true,
        true,
        false,
        false,
        true,
        true,
        false,
        false,
        true,
        true,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        true,
        true,
        false,
        false,
        false,
        false,
        true,
        false,
        false,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false
    };

    public G12(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G12.Generator(this.level, fixedBiome(Biomes.JUNGLE), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return false;
    }

    public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
        private final PerlinSimplexNoise face;
        private final PerlinSimplexNoise background;
        private final WorldgenRandom random = new WorldgenRandom();

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
            this.face = new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-5, 0));
            this.background = new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-5, 0));
        }

        @Override
        public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public int getSpawnHeight() {
            return 100;
        }

        @Override
        public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
            this.placeFace(param1, 0, 0);
            this.placeFace(param1, 0, 8);
            this.placeFace(param1, 8, 0);
            this.placeFace(param1, 8, 8);
        }

        private void placeFace(ChunkAccess param0, int param1, int param2) {
            ChunkPos var0 = param0.getPos();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
            double var2 = (1.0 + this.face.getValue((double)var0.x, (double)var0.z, false)) * 16.0;
            double var3 = (1.0 + this.background.getValue((double)var0.x, (double)var0.z, false)) * 16.0;
            this.random.setBaseChunkSeed(2 * var0.x + param1, 2 * var0.z + param2);
            Block[] var4 = Util.randomObject(this.random, ColoredBlocks.COLORED_BLOCKS);
            BlockState var5 = Util.randomObject(this.random, var4).defaultBlockState();
            BlockState var6 = Util.randomObject(this.random, var4).defaultBlockState();

            for(int var7 = 0; var7 < 8; ++var7) {
                for(int var8 = 0; var8 < 8; ++var8) {
                    boolean var9 = G12.FACE[var7 * 8 + var8];
                    BlockState var10 = var9 ? var5 : var6;
                    double var11 = var9 ? var2 : var3;

                    for(int var12 = 1; (double)var12 < var11; ++var12) {
                        param0.setBlockState(var1.set(var7 + param1, var12, var8 + param2), var10, false);
                    }
                }
            }

        }

        @Override
        public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
            return 100;
        }

        @Override
        public BlockGetter getBaseColumn(int param0, int param1) {
            return EmptyBlockGetter.INSTANCE;
        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T08;
        }
    }
}
