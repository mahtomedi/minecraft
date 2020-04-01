package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class G39 extends NormalDimension {
    public G39(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return new G39.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
    }

    public static class Generator extends OverworldLevelSource {
        private final PerlinSimplexNoise noise;

        public Generator(LevelAccessor param0, BiomeSource param1, OverworldGeneratorSettings param2) {
            super(param0, param1, param2);
            WorldgenRandom var0 = new WorldgenRandom(param0.getSeed());
            this.noise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-4, 1));
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            super.applyBiomeDecoration(param0);
            BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
            int var1 = param0.getCenterX();
            int var2 = param0.getCenterZ();
            BlockState var3 = Blocks.ZONE.defaultBlockState();

            for(int var4 = 0; var4 < 16; ++var4) {
                for(int var5 = 0; var5 < 16; ++var5) {
                    int var6 = 16 * var1 + var4;
                    int var7 = 16 * var2 + var5;
                    int var8 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var6, var7);
                    int var9 = (int)(this.noise.getValue((double)((float)var6 / 16.0F), (double)((float)var7 / 16.0F), false) * (double)var8 / 3.0) - 1;
                    if (var9 > 0) {
                        for(int var10 = -var9; var10 < var9; ++var10) {
                            var0.set(var6, var8 + var10, var7);
                            if (param0.isEmptyBlock(var0)) {
                                param0.setBlock(var0, var3, 4);
                            }
                        }
                    }
                }
            }

        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T34;
        }
    }
}
