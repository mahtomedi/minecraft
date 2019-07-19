package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

public interface CollisionContext {
    static CollisionContext empty() {
        return EntityCollisionContext.EMPTY;
    }

    static CollisionContext of(Entity param0) {
        return new EntityCollisionContext(param0);
    }

    boolean isSneaking();

    boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3);

    boolean isHoldingItem(Item var1);
}
