package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class NoSurfaceOreFeature extends Feature<OreConfiguration> {
    NoSurfaceOreFeature(Function<Dynamic<?>, ? extends OreConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        OreConfiguration param5
    ) {
        int var0 = param3.nextInt(param5.size + 1);
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < var0; ++var2) {
            this.offsetTargetPos(var1, param3, param4, Math.min(var2, 7));
            if (param5.target.getPredicate().test(param0.getBlockState(var1)) && !this.isFacingAir(param0, var1)) {
                param0.setBlock(var1, param5.state, 2);
            }
        }

        return true;
    }

    private void offsetTargetPos(BlockPos.MutableBlockPos param0, Random param1, BlockPos param2, int param3) {
        int var0 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        int var1 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        int var2 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        param0.setWithOffset(param2, var0, var1, var2);
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(Random param0, int param1) {
        return Math.round((param0.nextFloat() - param0.nextFloat()) * (float)param1);
    }

    private boolean isFacingAir(LevelAccessor param0, BlockPos param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(Direction var1 : Direction.values()) {
            var0.setWithOffset(param1, var1);
            if (param0.getBlockState(var0).isAir()) {
                return true;
            }
        }

        return false;
    }
}
