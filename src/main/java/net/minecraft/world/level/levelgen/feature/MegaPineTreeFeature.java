package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaPineTreeFeature extends MegaTreeFeature<MegaTreeConfiguration> {
    public MegaPineTreeFeature(Function<Dynamic<?>, ? extends MegaTreeConfiguration> param0, Function<Random, ? extends MegaTreeConfiguration> param1) {
        super(param0, param1);
    }

    public boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, MegaTreeConfiguration param6
    ) {
        int var0 = this.calcTreeHeigth(param1, param6);
        if (!this.prepareTree(param0, param2, var0)) {
            return false;
        } else {
            this.createCrown(param0, param1, param2.getX(), param2.getZ(), param2.getY() + var0, 0, param4, param5, param6);
            this.placeTrunk(param0, param1, param2, var0, param3, param5, param6);
            return true;
        }
    }

    private void createCrown(
        LevelSimulatedRW param0,
        Random param1,
        int param2,
        int param3,
        int param4,
        int param5,
        Set<BlockPos> param6,
        BoundingBox param7,
        MegaTreeConfiguration param8
    ) {
        int var0 = param1.nextInt(5) + param8.crownHeight;
        int var1 = 0;

        for(int var2 = param4 - var0; var2 <= param4; ++var2) {
            int var3 = param4 - var2;
            int var4 = param5 + Mth.floor((float)var3 / (float)var0 * 3.5F);
            this.placeDoubleTrunkLeaves(
                param0, param1, new BlockPos(param2, var2, param3), var4 + (var3 > 0 && var4 == var1 && (var2 & 1) == 0 ? 1 : 0), param6, param7, param8
            );
            var1 = var4;
        }

    }
}
