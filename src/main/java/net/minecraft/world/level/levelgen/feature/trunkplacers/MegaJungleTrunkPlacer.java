package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaJungleTrunkPlacer extends GiantTrunkPlacer {
    public static final Codec<MegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> trunkPlacerParts(param0).apply(param0, MegaJungleTrunkPlacer::new)
    );

    public MegaJungleTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedRW param0, Random param1, int param2, BlockPos param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        List<FoliagePlacer.FoliageAttachment> var0 = Lists.newArrayList();
        var0.addAll(super.placeTrunk(param0, param1, param2, param3, param4, param5, param6));

        for(int var1 = param2 - 2 - param1.nextInt(4); var1 > param2 / 2; var1 -= 2 + param1.nextInt(4)) {
            float var2 = param1.nextFloat() * (float) (Math.PI * 2);
            int var3 = 0;
            int var4 = 0;

            for(int var5 = 0; var5 < 5; ++var5) {
                var3 = (int)(1.5F + Mth.cos(var2) * (float)var5);
                var4 = (int)(1.5F + Mth.sin(var2) * (float)var5);
                BlockPos var6 = param3.offset(var3, var1 - 3 + var5 / 2, var4);
                placeLog(param0, param1, var6, param4, param5, param6);
            }

            var0.add(new FoliagePlacer.FoliageAttachment(param3.offset(var3, var1, var4), -2, false));
        }

        return var0;
    }
}
