package net.minecraft.world.level.dimension.special;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G24 extends SpecialDimensionBase {
    public G24(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G24.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.5F;
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
        private final List<Block> blocks = Registry.BLOCK
            .stream()
            .filter(param0x -> !param0x.isUnstable() && !param0x.isEntityBlock())
            .collect(ImmutableList.toImmutableList());

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
            WorldgenRandom var0 = new WorldgenRandom();
            ChunkPos var1 = param1.getPos();
            var0.setBaseChunkSeed(var1.x, var1.z);
            float var2 = var0.nextFloat() / 10.0F;
            float var3 = var0.nextFloat() / 10.0F;
            BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

            for(int var5 = 0; var5 < 16; ++var5) {
                for(int var6 = 0; var6 < 16; ++var6) {
                    int var7 = var5 - 8;
                    int var8 = var6 - 8;
                    float var9 = Mth.sqrt((float)(var7 * var7 + var8 * var8));
                    if (var9 <= 9.0F && var9 >= 6.0F) {
                        if ((int)var9 == 7) {
                            param1.setBlockState(var4.set(var5, 128, var6), Blocks.SMOOTH_QUARTZ.defaultBlockState(), false);
                        } else {
                            param1.setBlockState(var4.set(var5, 128, var6), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), false);
                        }
                    }
                }
            }

            int var10 = 10 + var0.nextInt(75);

            for(int var11 = -var10; var11 < var10; ++var11) {
                Block var12 = Util.randomObject(var0, this.blocks);
                BlockState var13 = Util.randomObject(var0, var12.getStateDefinition().getPossibleStates());
                int var14 = 8 + (int)(5.0F * Mth.sin(var2 * (float)var11));
                int var15 = 8 + (int)(5.0F * Mth.sin(var3 * (float)var11));
                param1.setBlockState(var4.set(var14, 128 + var11, var15), var13, false);
                param1.setBlockState(var4.set(var14, 128 - var11, var15), var13, false);
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
            return ChunkGeneratorType.T19;
        }
    }
}
