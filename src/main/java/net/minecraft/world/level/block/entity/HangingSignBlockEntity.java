package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignBlockEntity extends SignBlockEntity {
    private static final int MAX_TEXT_LINE_WIDTH = 50;
    private static final int TEXT_LINE_HEIGHT = 8;

    public HangingSignBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.HANGING_SIGN, param0, param1);
    }

    @Override
    public int getTextLineHeight() {
        return 8;
    }

    @Override
    public int getMaxTextLineWidth() {
        return 50;
    }
}
