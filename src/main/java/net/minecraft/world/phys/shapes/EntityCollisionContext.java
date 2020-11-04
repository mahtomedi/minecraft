package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
    protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, ItemStack.EMPTY, param0 -> false) {
        @Override
        public boolean isAbove(VoxelShape param0, BlockPos param1, boolean param2) {
            return param2;
        }
    };
    private final boolean descending;
    private final double entityBottom;
    private final ItemStack heldItem;
    private final Predicate<Fluid> canStandOnFluid;

    protected EntityCollisionContext(boolean param0, double param1, ItemStack param2, Predicate<Fluid> param3) {
        this.descending = param0;
        this.entityBottom = param1;
        this.heldItem = param2;
        this.canStandOnFluid = param3;
    }

    @Deprecated
    protected EntityCollisionContext(Entity param0) {
        this(
            param0.isDescending(),
            param0.getY(),
            param0 instanceof LivingEntity ? ((LivingEntity)param0).getMainHandItem() : ItemStack.EMPTY,
            param0 instanceof LivingEntity ? ((LivingEntity)param0)::canStandOnFluid : param0x -> false
        );
    }

    @Override
    public boolean isHoldingItem(Item param0) {
        return this.heldItem.is(param0);
    }

    @Override
    public boolean canStandOnFluid(FluidState param0, FlowingFluid param1) {
        return this.canStandOnFluid.test(param1) && !param0.getType().isSame(param1);
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
