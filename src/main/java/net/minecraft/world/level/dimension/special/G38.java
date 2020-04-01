package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class G38 extends NormalDimension {
    public G38(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return new G38.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
    }

    public static class Generator extends OverworldLevelSource {
        public Generator(LevelAccessor param0, BiomeSource param1, OverworldGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            super.applyBiomeDecoration(param0);
            int var0 = param0.getCenterX();
            int var1 = param0.getCenterZ();
            WorldgenRandom var2 = new WorldgenRandom();
            var2.setBaseChunkSeed(var0, var1);
            int var3 = var0 * var0 + var1 * var1;
            int var4 = Math.min(Mth.floor(Math.sqrt((double)var3) / 3.0 + 1.0), 16);
            int var5 = var2.nextInt(Math.min(var3 / 2 + 1, 32768));
            BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

            for(int var8 = 0; var8 < var5; ++var8) {
                int var9 = 16 * var0 + var2.nextInt(16);
                int var10 = 16 * var1 + var2.nextInt(16);
                int var11 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var9, var10);
                int var12 = var2.nextInt(var11 + 5);
                var6.set(var9, var12, var10);
                var7.setWithOffset(var6, var2.nextInt(var4), var2.nextInt(var4), var2.nextInt(var4));
                BlockState var13 = param0.getBlockState(var6);
                param0.setBlock(var6, param0.getBlockState(var7), 4);
                param0.setBlock(var7, var13, 4);
            }

        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T33;
        }
    }
}
