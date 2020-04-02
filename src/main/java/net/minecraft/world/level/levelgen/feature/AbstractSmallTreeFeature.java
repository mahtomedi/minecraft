package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public abstract class AbstractSmallTreeFeature<T extends SmallTreeConfiguration> extends AbstractTreeFeature<T> {
    public AbstractSmallTreeFeature(Function<Dynamic<?>, ? extends T> param0) {
        super(param0);
    }

    public Optional<BlockPos> getProjectedOrigin(LevelSimulatedRW param0, int param1, int param2, BlockPos param3, SmallTreeConfiguration param4) {
        BlockPos var2;
        if (!param4.fromSapling) {
            int var0 = param0.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, param3).getY();
            int var1 = param0.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, param3).getY();
            var2 = new BlockPos(param3.getX(), var0, param3.getZ());
            if (var1 - var0 > param4.maxWaterDepth) {
                return Optional.empty();
            }
        } else {
            var2 = param3;
        }

        if (var2.getY() >= 1 && var2.getY() + param1 + 1 <= 256) {
            for(int var4 = 0; var4 <= param1 + 1; ++var4) {
                int var5 = param4.foliagePlacer.getTreeRadiusForHeight(param1, param2, var4);
                BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

                for(int var7 = -var5; var7 <= var5; ++var7) {
                    int var8 = -var5;

                    while(var8 <= var5) {
                        if (var4 + var2.getY() >= 0 && var4 + var2.getY() < 256) {
                            var6.set(var7 + var2.getX(), var4 + var2.getY(), var8 + var2.getZ());
                            if (isFree(param0, var6) && (param4.ignoreVines || !isVine(param0, var6))) {
                                ++var8;
                                continue;
                            }

                            return Optional.empty();
                        }

                        return Optional.empty();
                    }
                }
            }

            return isGrassOrDirtOrFarmland(param0, var2.below()) && var2.getY() < 256 - param1 - 1 ? Optional.of(var2) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }
}
