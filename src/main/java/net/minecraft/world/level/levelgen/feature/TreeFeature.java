package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TreeFeature extends AbstractSmallTreeFeature<SmallTreeConfiguration> {
    public TreeFeature(Function<Dynamic<?>, ? extends SmallTreeConfiguration> param0) {
        super(param0);
    }

    public boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, SmallTreeConfiguration param6
    ) {
        int var0 = param6.trunkPlacer.getTreeHeight(param1, param6);
        int var1 = param6.foliagePlacer.foliageHeight(param1, var0);
        int var2 = var0 - var1;
        int var3 = param6.foliagePlacer.foliageRadius(param1, var2, param6);
        Optional<BlockPos> var4 = this.getProjectedOrigin(param0, var0, var3, param2, param6);
        if (!var4.isPresent()) {
            return false;
        } else {
            BlockPos var5 = var4.get();
            this.setDirtAt(param0, var5.below());
            Map<BlockPos, Integer> var6 = param6.trunkPlacer.placeTrunk(param0, param1, var0, var5, var3, param3, param5, param6);
            var6.forEach((param6x, param7) -> param6.foliagePlacer.createFoliage(param0, param1, param6, var0, param6x, var1, param7, param4));
            return true;
        }
    }
}
