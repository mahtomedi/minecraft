package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

public interface CollisionContext {
    static CollisionContext empty() {
        return EntityCollisionContext.EMPTY;
    }

    static CollisionContext of(Entity param0) {
        return new EntityCollisionContext(param0);
    }

    boolean isDescending();

    boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3);

    boolean hasItemOnFeet(Item var1);

    boolean isHoldingItem(Item var1);

    boolean canStandOnFluid(FluidState var1, FlowingFluid var2);
}
