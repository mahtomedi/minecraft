package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public abstract class DoorInteractGoal extends Goal {
    protected Mob mob;
    protected BlockPos doorPos = BlockPos.ZERO;
    protected boolean hasDoor;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;

    public DoorInteractGoal(Mob param0) {
        this.mob = param0;
        if (!(param0.getNavigation() instanceof GroundPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    protected boolean isOpen() {
        if (!this.hasDoor) {
            return false;
        } else {
            BlockState var0 = this.mob.level.getBlockState(this.doorPos);
            if (!(var0.getBlock() instanceof DoorBlock)) {
                this.hasDoor = false;
                return false;
            } else {
                return var0.getValue(DoorBlock.OPEN);
            }
        }
    }

    protected void setOpen(boolean param0) {
        if (this.hasDoor) {
            BlockState var0 = this.mob.level.getBlockState(this.doorPos);
            if (var0.getBlock() instanceof DoorBlock) {
                ((DoorBlock)var0.getBlock()).setOpen(this.mob.level, this.doorPos, param0);
            }
        }

    }

    @Override
    public boolean canUse() {
        if (!this.mob.horizontalCollision) {
            return false;
        } else {
            GroundPathNavigation var0 = (GroundPathNavigation)this.mob.getNavigation();
            Path var1 = var0.getPath();
            if (var1 != null && !var1.isDone() && var0.canOpenDoors()) {
                for(int var2 = 0; var2 < Math.min(var1.getIndex() + 2, var1.getSize()); ++var2) {
                    Node var3 = var1.get(var2);
                    this.doorPos = new BlockPos(var3.x, var3.y + 1, var3.z);
                    if (!(this.mob.distanceToSqr((double)this.doorPos.getX(), this.mob.getY(), (double)this.doorPos.getZ()) > 2.25)) {
                        this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
                        if (this.hasDoor) {
                            return true;
                        }
                    }
                }

                this.doorPos = new BlockPos(this.mob).above();
                this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
                return this.hasDoor;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.passed;
    }

    @Override
    public void start() {
        this.passed = false;
        this.doorOpenDirX = (float)((double)((float)this.doorPos.getX() + 0.5F) - this.mob.getX());
        this.doorOpenDirZ = (float)((double)((float)this.doorPos.getZ() + 0.5F) - this.mob.getZ());
    }

    @Override
    public void tick() {
        float var0 = (float)((double)((float)this.doorPos.getX() + 0.5F) - this.mob.getX());
        float var1 = (float)((double)((float)this.doorPos.getZ() + 0.5F) - this.mob.getZ());
        float var2 = this.doorOpenDirX * var0 + this.doorOpenDirZ * var1;
        if (var2 < 0.0F) {
            this.passed = true;
        }

    }
}
