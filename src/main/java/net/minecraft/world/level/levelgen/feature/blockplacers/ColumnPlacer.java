package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ColumnPlacer extends BlockPlacer {
    private final int minSize;
    private final int extraSize;

    public ColumnPlacer(int param0, int param1) {
        super(BlockPlacerType.COLUMN_PLACER);
        this.minSize = param0;
        this.extraSize = param1;
    }

    public <T> ColumnPlacer(Dynamic<T> param0) {
        this(param0.get("min_size").asInt(1), param0.get("extra_size").asInt(2));
    }

    @Override
    public void place(LevelAccessor param0, BlockPos param1, BlockState param2, Random param3) {
        BlockPos.MutableBlockPos var0 = param1.mutable();
        int var1 = this.minSize + param3.nextInt(param3.nextInt(this.extraSize + 1) + 1);

        for(int var2 = 0; var2 < var1; ++var2) {
            param0.setBlock(var0, param2, 2);
            var0.move(Direction.UP);
        }

    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
                param0,
                param0.createMap(
                    ImmutableMap.of(
                        param0.createString("type"),
                        param0.createString(Registry.BLOCK_PLACER_TYPES.getKey(this.type).toString()),
                        param0.createString("min_size"),
                        param0.createInt(this.minSize),
                        param0.createString("extra_size"),
                        param0.createInt(this.extraSize)
                    )
                )
            )
            .getValue();
    }
}
