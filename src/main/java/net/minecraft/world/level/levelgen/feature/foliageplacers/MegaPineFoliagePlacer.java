package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaPineFoliagePlacer extends FoliagePlacer {
    public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(UniformInt.codec(0, 16, 8).fieldOf("crown_height").forGetter(param0x -> param0x.crownHeight))
                .apply(param0, MegaPineFoliagePlacer::new)
    );
    private final UniformInt crownHeight;

    public MegaPineFoliagePlacer(UniformInt param0, UniformInt param1, UniformInt param2) {
        super(param0, param1);
        this.crownHeight = param2;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(
        LevelSimulatedRW param0,
        Random param1,
        TreeConfiguration param2,
        int param3,
        FoliagePlacer.FoliageAttachment param4,
        int param5,
        int param6,
        Set<BlockPos> param7,
        int param8,
        BoundingBox param9
    ) {
        BlockPos var0 = param4.foliagePos();
        int var1 = 0;

        for(int var2 = var0.getY() - param5 + param8; var2 <= var0.getY() + param8; ++var2) {
            int var3 = var0.getY() - var2;
            int var4 = param6 + param4.radiusOffset() + Mth.floor((float)var3 / (float)param5 * 3.5F);
            int var5;
            if (var3 > 0 && var4 == var1 && (var2 & 1) == 0) {
                var5 = var4 + 1;
            } else {
                var5 = var4;
            }

            this.placeLeavesRow(param0, param1, param2, new BlockPos(var0.getX(), var2, var0.getZ()), var5, param7, 0, param4.doubleTrunk(), param9);
            var1 = var4;
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.crownHeight.sample(param0);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param1 + param3 >= 7) {
            return true;
        } else {
            return param1 * param1 + param3 * param3 > param4 * param4;
        }
    }
}
