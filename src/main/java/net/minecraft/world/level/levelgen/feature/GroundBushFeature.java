package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GroundBushFeature extends AbstractTreeFeature<TreeConfiguration> {
    public GroundBushFeature(Function<Dynamic<?>, ? extends TreeConfiguration> param0, Function<Random, ? extends TreeConfiguration> param1) {
        super(param0, param1);
    }

    @Override
    public boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        param2 = param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, param2).below();
        if (isGrassOrDirt(param0, param2)) {
            param2 = param2.above();
            this.placeLog(param0, param1, param2, param3, param5, param6);

            for(int var0 = 0; var0 <= 2; ++var0) {
                int var1 = 2 - var0;

                for(int var2 = -var1; var2 <= var1; ++var2) {
                    for(int var3 = -var1; var3 <= var1; ++var3) {
                        if (Math.abs(var2) != var1 || Math.abs(var3) != var1 || param1.nextInt(2) != 0) {
                            this.placeLeaf(
                                param0, param1, new BlockPos(var2 + param2.getX(), var0 + param2.getY(), var3 + param2.getZ()), param4, param5, param6
                            );
                        }
                    }
                }
            }
        }

        return true;
    }
}
