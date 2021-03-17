package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoublePlantPlacer extends BlockPlacer {
    public static final Codec<DoublePlantPlacer> CODEC = Codec.unit(() -> DoublePlantPlacer.INSTANCE);
    public static final DoublePlantPlacer INSTANCE = new DoublePlantPlacer();

    @Override
    protected BlockPlacerType<?> type() {
        return BlockPlacerType.DOUBLE_PLANT_PLACER;
    }

    @Override
    public void place(LevelAccessor param0, BlockPos param1, BlockState param2, Random param3) {
        ((DoublePlantBlock)param2.getBlock()).placeAt(param0, param2, param1, 2);
    }
}
