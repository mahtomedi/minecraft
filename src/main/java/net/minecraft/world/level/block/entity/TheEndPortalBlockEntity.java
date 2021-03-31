package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TheEndPortalBlockEntity extends BlockEntity {
    protected TheEndPortalBlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
    }

    public TheEndPortalBlockEntity(BlockPos param0, BlockState param1) {
        this(BlockEntityType.END_PORTAL, param0, param1);
    }

    public boolean shouldRenderFace(Direction param0) {
        return param0.getAxis() == Direction.Axis.Y;
    }
}
