package net.minecraft.world.level.dimension.special;

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

public class G30 extends NormalDimension {
    public G30(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return new G30.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
    }

    public static class Generator extends OverworldLevelSource {
        public Generator(LevelAccessor param0, BiomeSource param1, OverworldGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            super.applyBiomeDecoration(param0);
            BlockState var0 = Blocks.SLIME_BLOCK.defaultBlockState();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

            for(int var2 = 0; var2 < 16; ++var2) {
                for(int var3 = 0; var3 < 16; ++var3) {
                    int var4 = param0.getCenterX() * 16 + var2;
                    int var5 = param0.getCenterZ() * 16 + var3;
                    int var6 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var4, var5);

                    for(int var7 = 0; var7 < 10; ++var7) {
                        param0.setBlock(var1.set(var4, var6 + var7, var5), var0, 4);
                    }
                }
            }

        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T25;
        }
    }
}
