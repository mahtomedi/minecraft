package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DaylightDetectorBlockEntity extends BlockEntity {
    public DaylightDetectorBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.DAYLIGHT_DETECTOR, param0, param1);
    }
}
