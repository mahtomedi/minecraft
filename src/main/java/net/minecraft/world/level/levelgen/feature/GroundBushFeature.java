package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GroundBushFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private final BlockState leaf;
    private final BlockState trunk;

    public GroundBushFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, BlockState param1, BlockState param2) {
        super(param0, false);
        this.trunk = param1;
        this.leaf = param2;
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        param3 = param1.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, param3).below();
        if (isGrassOrDirt(param1, param3)) {
            param3 = param3.above();
            this.setBlock(param0, param1, param3, this.trunk, param4);

            for(int var0 = param3.getY(); var0 <= param3.getY() + 2; ++var0) {
                int var1 = var0 - param3.getY();
                int var2 = 2 - var1;

                for(int var3 = param3.getX() - var2; var3 <= param3.getX() + var2; ++var3) {
                    int var4 = var3 - param3.getX();

                    for(int var5 = param3.getZ() - var2; var5 <= param3.getZ() + var2; ++var5) {
                        int var6 = var5 - param3.getZ();
                        if (Math.abs(var4) != var2 || Math.abs(var6) != var2 || param2.nextInt(2) != 0) {
                            BlockPos var7 = new BlockPos(var3, var0, var5);
                            if (isAirOrLeaves(param1, var7)) {
                                this.setBlock(param0, param1, var7, this.leaf, param4);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
