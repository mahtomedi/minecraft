package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ForestFlowerFeature extends FlowerFeature {
    private static final Block[] flowers = new Block[]{
        Blocks.DANDELION,
        Blocks.POPPY,
        Blocks.BLUE_ORCHID,
        Blocks.ALLIUM,
        Blocks.AZURE_BLUET,
        Blocks.RED_TULIP,
        Blocks.ORANGE_TULIP,
        Blocks.WHITE_TULIP,
        Blocks.PINK_TULIP,
        Blocks.OXEYE_DAISY,
        Blocks.CORNFLOWER,
        Blocks.LILY_OF_THE_VALLEY
    };

    public ForestFlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public BlockState getRandomFlower(Random param0, BlockPos param1) {
        double var0 = Mth.clamp((1.0 + Biome.BIOME_INFO_NOISE.getValue((double)param1.getX() / 48.0, (double)param1.getZ() / 48.0, false)) / 2.0, 0.0, 0.9999);
        Block var1 = flowers[(int)(var0 * (double)flowers.length)];
        return var1 == Blocks.BLUE_ORCHID ? Blocks.POPPY.defaultBlockState() : var1.defaultBlockState();
    }
}
