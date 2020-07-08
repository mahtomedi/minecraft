package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaJungleFoliagePlacer extends FoliagePlacer {
    public static final Codec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(Codec.intRange(0, 16).fieldOf("height").forGetter(param0x -> param0x.height))
                .apply(param0, MegaJungleFoliagePlacer::new)
    );
    protected final int height;

    public MegaJungleFoliagePlacer(UniformInt param0, UniformInt param1, int param2) {
        super(param0, param1);
        this.height = param2;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
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
        int var0 = param4.doubleTrunk() ? param5 : 1 + param1.nextInt(2);

        for(int var1 = param8; var1 >= param8 - var0; --var1) {
            int var2 = param6 + param4.radiusOffset() + 1 - var1;
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var2, param7, var1, param4.doubleTrunk(), param9);
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.height;
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
