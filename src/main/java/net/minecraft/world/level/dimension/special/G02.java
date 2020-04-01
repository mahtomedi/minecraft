package net.minecraft.world.level.dimension.special;

import java.util.Random;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G02 extends SpecialDimensionBase {
    private static final BlockState X = Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
    private static final BlockState O = Blocks.AIR.defaultBlockState();
    private static final BlockState[] LEFT = new BlockState[]{
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X
    };
    private static final BlockState[] RIGHT = new BlockState[]{
        O,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        X,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        X,
        X,
        O,
        O,
        O,
        O,
        O,
        O
    };

    public G02(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G02.Generator(this.level, fixedBiome(Biomes.JUNGLE), NoneGeneratorSettings.INSTANCE);
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
        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
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
            ChunkPos var0 = param1.getPos();
            BlockState var1 = Blocks.BLUE_CONCRETE.defaultBlockState();
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(int var3 = 0; var3 < 16; ++var3) {
                for(int var4 = 0; var4 < 16; ++var4) {
                    for(int var5 = 0; var5 < 16; ++var5) {
                        param1.setBlockState(var2.set(var3, var4, var5), var1, false);
                    }
                }
            }

            Random var6 = new Random((long)(var0.x << 16 + var0.z));
            this.placeSlash(param1, 0, 0, var6.nextBoolean() ? G02.LEFT : G02.RIGHT);
            this.placeSlash(param1, 0, 8, var6.nextBoolean() ? G02.LEFT : G02.RIGHT);
            this.placeSlash(param1, 8, 0, var6.nextBoolean() ? G02.LEFT : G02.RIGHT);
            this.placeSlash(param1, 8, 8, var6.nextBoolean() ? G02.LEFT : G02.RIGHT);
        }

        private void placeSlash(ChunkAccess param0, int param1, int param2, BlockState[] param3) {
            BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

            for(int var1 = 0; var1 < 8; ++var1) {
                for(int var2 = 0; var2 < 8; ++var2) {
                    BlockState var3 = param3[var1 * 8 + var2];

                    for(int var4 = 16; var4 < 32; ++var4) {
                        param0.setBlockState(var0.set(var1 + param1, var4, var2 + param2), var3, false);
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
            return ChunkGeneratorType.T02;
        }
    }
}
