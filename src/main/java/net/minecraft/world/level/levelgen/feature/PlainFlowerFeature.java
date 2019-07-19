package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PlainFlowerFeature extends FlowerFeature {
    public PlainFlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public BlockState getRandomFlower(Random param0, BlockPos param1) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param1.getX() / 200.0, (double)param1.getZ() / 200.0);
        if (var0 < -0.8) {
            int var1 = param0.nextInt(4);
            switch(var1) {
                case 0:
                    return Blocks.ORANGE_TULIP.defaultBlockState();
                case 1:
                    return Blocks.RED_TULIP.defaultBlockState();
                case 2:
                    return Blocks.PINK_TULIP.defaultBlockState();
                case 3:
                default:
                    return Blocks.WHITE_TULIP.defaultBlockState();
            }
        } else if (param0.nextInt(3) > 0) {
            int var2 = param0.nextInt(4);
            switch(var2) {
                case 0:
                    return Blocks.POPPY.defaultBlockState();
                case 1:
                    return Blocks.AZURE_BLUET.defaultBlockState();
                case 2:
                    return Blocks.OXEYE_DAISY.defaultBlockState();
                case 3:
                default:
                    return Blocks.CORNFLOWER.defaultBlockState();
            }
        } else {
            return Blocks.DANDELION.defaultBlockState();
        }
    }
}
