package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
    protected static final CollisionContext EMPTY = new EntityCollisionContext(
        false, -Double.MAX_VALUE, ItemStack.EMPTY, ItemStack.EMPTY, param0 -> false, null
    ) {
        @Override
        public boolean isAbove(VoxelShape param0, BlockPos param1, boolean param2) {
            return param2;
        }
    };
    private final boolean descending;
    private final double entityBottom;
    private final ItemStack heldItem;
    private final ItemStack footItem;
    private final Predicate<Fluid> canStandOnFluid;
    @Nullable
    private final Entity entity;

    protected EntityCollisionContext(boolean param0, double param1, ItemStack param2, ItemStack param3, Predicate<Fluid> param4, @Nullable Entity param5) {
        this.descending = param0;
        this.entityBottom = param1;
        this.footItem = param2;
        this.heldItem = param3;
        this.canStandOnFluid = param4;
        this.entity = param5;
    }

    @Deprecated
    protected EntityCollisionContext(Entity param0) {
        this(
            param0.isDescending(),
            param0.getY(),
            param0 instanceof LivingEntity ? ((LivingEntity)param0).getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY,
            param0 instanceof LivingEntity ? ((LivingEntity)param0).getMainHandItem() : ItemStack.EMPTY,
            param0 instanceof LivingEntity ? ((LivingEntity)param0)::canStandOnFluid : param0x -> false,
            param0
        );
    }

    @Override
    public boolean hasItemOnFeet(Item param0) {
        return this.footItem.is(param0);
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

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }
}
