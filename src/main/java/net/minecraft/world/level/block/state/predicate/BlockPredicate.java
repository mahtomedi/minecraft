package net.minecraft.world.level.block.state.predicate;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicate implements Predicate<BlockState> {
    private final Block block;

    public BlockPredicate(Block param0) {
        this.block = param0;
    }

    public static BlockPredicate forBlock(Block param0) {
        return new BlockPredicate(param0);
    }

    public boolean test(@Nullable BlockState param0) {
        return param0 != null && param0.is(this.block);
    }
}
