package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ColumnPlacer extends BlockPlacer {
    public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("min_size").forGetter(param0x -> param0x.minSize),
                    Codec.INT.fieldOf("extra_size").forGetter(param0x -> param0x.extraSize)
                )
                .apply(param0, ColumnPlacer::new)
    );
    private final int minSize;
    private final int extraSize;

    public ColumnPlacer(int param0, int param1) {
        this.minSize = param0;
        this.extraSize = param1;
    }

    @Override
    protected BlockPlacerType<?> type() {
        return BlockPlacerType.COLUMN_PLACER;
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
}
