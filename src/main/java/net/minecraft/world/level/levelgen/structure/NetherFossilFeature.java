package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends StructureFeature<NoneFeatureConfiguration> {
    public NetherFossilFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return NetherFossilFeature.FeatureStart::new;
    }

    public static class FeatureStart extends BeardedStructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4, NoneFeatureConfiguration param5) {
            ChunkPos var0 = new ChunkPos(param2, param3);
            int var1 = var0.getMinBlockX() + this.random.nextInt(16);
            int var2 = var0.getMinBlockZ() + this.random.nextInt(16);
            int var3 = param0.getSeaLevel();
            int var4 = var3 + this.random.nextInt(param0.getGenDepth() - 2 - var3);
            BlockGetter var5 = param0.getBaseColumn(var1, var2);

            for(BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos(var1, var4, var2); var4 > var3; --var4) {
                BlockState var7 = var5.getBlockState(var6);
                var6.move(Direction.DOWN);
                BlockState var8 = var5.getBlockState(var6);
                if (var7.isAir() && (var8.is(Blocks.SOUL_SAND) || var8.isFaceSturdy(var5, var6, Direction.UP))) {
                    break;
                }
            }

            if (var4 > var3) {
                NetherFossilPieces.addPieces(param1, this.pieces, this.random, new BlockPos(var1, var4, var2));
                this.calculateBoundingBox();
            }
        }
    }
}
