package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl implements Control {
    public static final float MIN_SPEED = 5.0E-4F;
    public static final float MIN_SPEED_SQR = 2.5000003E-7F;
    protected static final int MAX_TURN = 90;
    protected final Mob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected double speedModifier;
    protected float strafeForwards;
    protected float strafeRight;
    protected MoveControl.Operation operation = MoveControl.Operation.WAIT;

    public MoveControl(Mob param0) {
        this.mob = param0;
    }

    public boolean hasWanted() {
        return this.operation == MoveControl.Operation.MOVE_TO;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public void setWantedPosition(double param0, double param1, double param2, double param3) {
        this.wantedX = param0;
        this.wantedY = param1;
        this.wantedZ = param2;
        this.speedModifier = param3;
        if (this.operation != MoveControl.Operation.JUMPING) {
            this.operation = MoveControl.Operation.MOVE_TO;
        }

    }

    public void strafe(float param0, float param1) {
        this.operation = MoveControl.Operation.STRAFE;
        this.strafeForwards = param0;
        this.strafeRight = param1;
        this.speedModifier = 0.25;
    }

    public void tick() {
        if (this.operation == MoveControl.Operation.STRAFE) {
            float var0 = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float var1 = (float)this.speedModifier * var0;
            float var2 = this.strafeForwards;
            float var3 = this.strafeRight;
            float var4 = Mth.sqrt(var2 * var2 + var3 * var3);
            if (var4 < 1.0F) {
                var4 = 1.0F;
            }

            var4 = var1 / var4;
            var2 *= var4;
            var3 *= var4;
            float var5 = Mth.sin(this.mob.getYRot() * (float) (Math.PI / 180.0));
            float var6 = Mth.cos(this.mob.getYRot() * (float) (Math.PI / 180.0));
            float var7 = var2 * var6 - var3 * var5;
            float var8 = var3 * var6 + var2 * var5;
            if (!this.isWalkable(var7, var8)) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
            }

            this.mob.setSpeed(var1);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = MoveControl.Operation.WAIT;
        } else if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            double var9 = this.wantedX - this.mob.getX();
            double var10 = this.wantedZ - this.mob.getZ();
            double var11 = this.wantedY - this.mob.getY();
            double var12 = var9 * var9 + var11 * var11 + var10 * var10;
            if (var12 < 2.5000003E-7F) {
                this.mob.setZza(0.0F);
                return;
            }

            float var13 = (float)(Mth.atan2(var10, var9) * 180.0F / (float)Math.PI) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), var13, 90.0F));
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos var14 = this.mob.blockPosition();
            BlockState var15 = this.mob.level().getBlockState(var14);
            VoxelShape var16 = var15.getCollisionShape(this.mob.level(), var14);
            if (var11 > (double)this.mob.maxUpStep() && var9 * var9 + var10 * var10 < (double)Math.max(1.0F, this.mob.getBbWidth())
                || !var16.isEmpty()
                    && this.mob.getY() < var16.max(Direction.Axis.Y) + (double)var14.getY()
                    && !var15.is(BlockTags.DOORS)
                    && !var15.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = MoveControl.Operation.JUMPING;
            }
        } else if (this.operation == MoveControl.Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround()) {
                this.operation = MoveControl.Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0F);
        }

    }

    private boolean isWalkable(float param0, float param1) {
        PathNavigation var0 = this.mob.getNavigation();
        if (var0 != null) {
            NodeEvaluator var1 = var0.getNodeEvaluator();
            if (var1 != null
                && var1.getBlockPathType(
                        this.mob.level(), Mth.floor(this.mob.getX() + (double)param0), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + (double)param1)
                    )
                    != BlockPathTypes.WALKABLE) {
                return false;
            }
        }

        return true;
    }

    protected float rotlerp(float param0, float param1, float param2) {
        float var0 = Mth.wrapDegrees(param1 - param0);
        if (var0 > param2) {
            var0 = param2;
        }

        if (var0 < -param2) {
            var0 = -param2;
        }

        float var1 = param0 + var0;
        if (var1 < 0.0F) {
            var1 += 360.0F;
        } else if (var1 > 360.0F) {
            var1 -= 360.0F;
        }

        return var1;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    protected static enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;
    }
}
