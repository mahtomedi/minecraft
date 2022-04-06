package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class DarkOakTrunkPlacer extends TrunkPlacer {
    public static final Codec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.create(param0 -> trunkPlacerParts(param0).apply(param0, DarkOakTrunkPlacer::new));

    public DarkOakTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, int param3, BlockPos param4, TreeConfiguration param5
    ) {
        List<FoliagePlacer.FoliageAttachment> var0 = Lists.newArrayList();
        BlockPos var1 = param4.below();
        setDirtAt(param0, param1, param2, var1, param5);
        setDirtAt(param0, param1, param2, var1.east(), param5);
        setDirtAt(param0, param1, param2, var1.south(), param5);
        setDirtAt(param0, param1, param2, var1.south().east(), param5);
        Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
        int var3 = param3 - param2.nextInt(4);
        int var4 = 2 - param2.nextInt(3);
        int var5 = param4.getX();
        int var6 = param4.getY();
        int var7 = param4.getZ();
        int var8 = var5;
        int var9 = var7;
        int var10 = var6 + param3 - 1;

        for(int var11 = 0; var11 < param3; ++var11) {
            if (var11 >= var3 && var4 > 0) {
                var8 += var2.getStepX();
                var9 += var2.getStepZ();
                --var4;
            }

            int var12 = var6 + var11;
            BlockPos var13 = new BlockPos(var8, var12, var9);
            if (TreeFeature.isAirOrLeaves(param0, var13)) {
                this.placeLog(param0, param1, param2, var13, param5);
                this.placeLog(param0, param1, param2, var13.east(), param5);
                this.placeLog(param0, param1, param2, var13.south(), param5);
                this.placeLog(param0, param1, param2, var13.east().south(), param5);
            }
        }

        var0.add(new FoliagePlacer.FoliageAttachment(new BlockPos(var8, var10, var9), 0, true));

        for(int var14 = -1; var14 <= 2; ++var14) {
            for(int var15 = -1; var15 <= 2; ++var15) {
                if ((var14 < 0 || var14 > 1 || var15 < 0 || var15 > 1) && param2.nextInt(3) <= 0) {
                    int var16 = param2.nextInt(3) + 2;

                    for(int var17 = 0; var17 < var16; ++var17) {
                        this.placeLog(param0, param1, param2, new BlockPos(var5 + var14, var10 - var17 - 1, var7 + var15), param5);
                    }

                    var0.add(new FoliagePlacer.FoliageAttachment(new BlockPos(var8 + var14, var10, var9 + var15), 0, false));
                }
            }
        }

        return var0;
    }
}
