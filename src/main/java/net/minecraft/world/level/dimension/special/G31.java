package net.minecraft.world.level.dimension.special;

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
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G31 extends SpecialDimensionBase {
    public G31(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G31.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return NormalDimension.getTimeOfDayI(param0, 24000.0);
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
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
            BlockState var0 = Blocks.GRASS_BLOCK.defaultBlockState();
            BlockState var1 = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            ChunkPos var2 = param1.getPos();
            int var3 = var2.getMinBlockX();
            int var4 = var2.getMinBlockZ();
            int var5 = 0;
            int var6;
            if (var2.x >= 0) {
                var6 = 0;
            } else if (var2.z >= 0) {
                var6 = -1;
            } else {
                var6 = 1;
            }

            BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

            for(int var10 = 0; var10 < 16; ++var10) {
                for(int var11 = 0; var11 < 16; ++var11) {
                    int var12 = Math.max(Math.abs(var3 + var10 - var6), Math.abs(var4 + var11 - 0));
                    if (var12 % 2 == 0) {
                        param1.setBlockState(var9.set(var10, 50, var11), var1, false);
                        param1.setBlockState(var9.move(0, 1, 0), var1, false);
                        param1.setBlockState(var9.move(0, 1, 0), var1, false);
                    } else {
                        param1.setBlockState(var9.set(var10, 50, var11), var0, false);
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
            return ChunkGeneratorType.T26;
        }
    }
}
