package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.RandomScatteredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
    public NetherFossilFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 14357921;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return NetherFossilFeature.FeatureStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Nether_Fossil";
    }

    @Override
    protected int getSpacing(DimensionType param0, ChunkGeneratorSettings param1) {
        return 2;
    }

    @Override
    protected int getSeparation(DimensionType param0, ChunkGeneratorSettings param1) {
        return 1;
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    public static class FeatureStart extends StructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
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
                if (var7.isAir() && (var8.getBlock() == Blocks.SOUL_SAND || var8.isFaceSturdy(var5, var6, Direction.UP))) {
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
