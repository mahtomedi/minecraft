package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class EntityCollisionContext implements CollisionContext {
    protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, Items.AIR) {
        @Override
        public boolean isAbove(VoxelShape param0, BlockPos param1, boolean param2) {
            return param2;
        }
    };
    private final boolean descending;
    private final double entityBottom;
    private final Item heldItem;

    protected EntityCollisionContext(boolean param0, double param1, Item param2) {
        this.descending = param0;
        this.entityBottom = param1;
        this.heldItem = param2;
    }

    @Deprecated
    protected EntityCollisionContext(Entity param0) {
        this(param0.isDescending(), param0.getY(), param0 instanceof LivingEntity ? ((LivingEntity)param0).getMainHandItem().getItem() : Items.AIR);
    }

    @Override
    public boolean isHoldingItem(Item param0) {
        return this.heldItem == param0;
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape param0, BlockPos param1, boolean param2) {
        return this.entityBottom > (double)param1.getY() + param0.max(Direction.Axis.Y) - 1.0E-5F;
    }
}
