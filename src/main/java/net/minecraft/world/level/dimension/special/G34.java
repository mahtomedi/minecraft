package net.minecraft.world.level.dimension.special;

import com.mojang.math.OctahedralGroup;
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
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.CharFeature;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G34 extends SpecialDimensionBase {
    public G34(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G34.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
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
        private static final BlockState STATE = Blocks.FIRE
            .defaultBlockState()
            .setValue(FireBlock.NORTH, Boolean.valueOf(true))
            .setValue(FireBlock.SOUTH, Boolean.valueOf(true))
            .setValue(FireBlock.EAST, Boolean.valueOf(true))
            .setValue(FireBlock.WEST, Boolean.valueOf(true))
            .setValue(FireBlock.UP, Boolean.valueOf(true));
        final SimpleStateProvider provider = new SimpleStateProvider(STATE);

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
        }

        @Override
        public int getSpawnHeight() {
            return 30;
        }

        @Override
        public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
            ChunkPos var0 = param1.getPos();
            if (var0.z == 0) {
                this.printChar(param1, var0.x);
            }
        }

        private void printChar(ChunkAccess param0, int param1) {
            if (param1 >= 0 && param1 < " We apologise for the inconvenience.".length()) {
                char var0 = " We apologise for the inconvenience.".charAt(param1);
                BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
                CharFeature.place(BlockPos.ZERO, new CharConfiguration(this.provider, var0, OctahedralGroup.IDENTITY), param2 -> {
                    param0.setBlockState(var1.set(2 * param2.getX() + 0, 2 * param2.getY() + 100, 2 * param2.getZ() + 0), STATE, false);
                    param0.setBlockState(var1.set(2 * param2.getX() + 1, 2 * param2.getY() + 100, 2 * param2.getZ() + 1), STATE, false);
                    param0.setBlockState(var1.set(2 * param2.getX() + 1, 2 * param2.getY() + 100, 2 * param2.getZ() + 0), STATE, false);
                    param0.setBlockState(var1.set(2 * param2.getX() + 0, 2 * param2.getY() + 100, 2 * param2.getZ() + 1), STATE, false);
                    param0.setBlockState(var1.set(2 * param2.getX() + 0, 2 * param2.getY() + 101, 2 * param2.getZ() + 0), STATE, false);
                    param0.setBlockState(var1.set(2 * param2.getX() + 1, 2 * param2.getY() + 101, 2 * param2.getZ() + 1), STATE, false);
                    param0.setBlockState(var1.set(2 * param2.getX() + 1, 2 * param2.getY() + 101, 2 * param2.getZ() + 0), STATE, false);
                    param0.setBlockState(var1.set(2 * param2.getX() + 0, 2 * param2.getY() + 101, 2 * param2.getZ() + 1), STATE, false);
                });
            }
        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
        }

        @Override
        public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
            return 0;
        }

        @Override
        public BlockGetter getBaseColumn(int param0, int param1) {
            return EmptyBlockGetter.INSTANCE;
        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T29;
        }
    }
}
