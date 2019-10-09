package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
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
        int var0 = param6.baseHeight + param1.nextInt(param6.heightRandA + 1) + param1.nextInt(param6.heightRandB + 1);
        int var1 = param6.trunkHeight >= 0
            ? param6.trunkHeight + param1.nextInt(param6.trunkHeightRandom + 1)
            : var0 - (param6.foliageHeight + param1.nextInt(param6.foliageHeightRandom + 1));
        int var2 = param6.foliagePlacer.foliageRadius(param1, var1, var0, param6);
        Optional<BlockPos> var3 = this.getProjectedOrigin(param0, var0, var1, var2, param2, param6);
        if (!var3.isPresent()) {
            return false;
        } else {
            BlockPos var4 = var3.get();
            this.setDirtAt(param0, var4.below());
            param6.foliagePlacer.createFoliage(param0, param1, param6, var0, var1, var2, var4, param4);
            this.placeTrunk(param0, param1, var0, var4, param6.trunkTopOffset + param1.nextInt(param6.trunkTopOffsetRandom + 1), param3, param5, param6);
            return true;
        }
    }
}
