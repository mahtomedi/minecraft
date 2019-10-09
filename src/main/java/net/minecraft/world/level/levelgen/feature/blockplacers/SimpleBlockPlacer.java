package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockPlacer extends BlockPlacer {
    public SimpleBlockPlacer() {
        super(BlockPlacerType.SIMPLE_BLOCK_PLACER);
    }

    public <T> SimpleBlockPlacer(Dynamic<T> param0) {
        this();
    }

    @Override
    public void place(LevelAccessor param0, BlockPos param1, BlockState param2, Random param3) {
        param0.setBlock(param1, param2, 2);
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
                param0,
                param0.createMap(ImmutableMap.of(param0.createString("type"), param0.createString(Registry.BLOCK_PLACER_TYPES.getKey(this.type).toString())))
            )
            .getValue();
    }
}
