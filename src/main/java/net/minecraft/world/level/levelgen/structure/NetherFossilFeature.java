package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
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

    public static class FeatureStart extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            NoneFeatureConfiguration param5,
            LevelHeightAccessor param6
        ) {
            int var0 = param3.getMinBlockX() + this.random.nextInt(16);
            int var1 = param3.getMinBlockZ() + this.random.nextInt(16);
            int var2 = param1.getSeaLevel();
            int var3 = var2 + this.random.nextInt(param1.getGenDepth() - 2 - var2);
            NoiseColumn var4 = param1.getBaseColumn(var0, var1, param6);

            for(BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos(var0, var3, var1); var3 > var2; --var3) {
                BlockState var6 = var4.getBlockState(var5);
                var5.move(Direction.DOWN);
                BlockState var7 = var4.getBlockState(var5);
                if (var6.isAir() && (var7.is(Blocks.SOUL_SAND) || var7.isFaceSturdy(EmptyBlockGetter.INSTANCE, var5, Direction.UP))) {
                    break;
                }
            }

            if (var3 > var2) {
                NetherFossilPieces.addPieces(param2, this, this.random, new BlockPos(var0, var3, var1));
            }
        }
    }
}
