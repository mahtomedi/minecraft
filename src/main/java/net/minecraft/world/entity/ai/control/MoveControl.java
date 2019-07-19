package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl {
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
            float var0 = (float)this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
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
            float var5 = Mth.sin(this.mob.yRot * (float) (Math.PI / 180.0));
            float var6 = Mth.cos(this.mob.yRot * (float) (Math.PI / 180.0));
            float var7 = var2 * var6 - var3 * var5;
            float var8 = var3 * var6 + var2 * var5;
            PathNavigation var9 = this.mob.getNavigation();
            if (var9 != null) {
                NodeEvaluator var10 = var9.getNodeEvaluator();
                if (var10 != null
                    && var10.getBlockPathType(this.mob.level, Mth.floor(this.mob.x + (double)var7), Mth.floor(this.mob.y), Mth.floor(this.mob.z + (double)var8))
                        != BlockPathTypes.WALKABLE) {
                    this.strafeForwards = 1.0F;
                    this.strafeRight = 0.0F;
                    var1 = var0;
                }
            }

            this.mob.setSpeed(var1);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = MoveControl.Operation.WAIT;
        } else if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            double var11 = this.wantedX - this.mob.x;
            double var12 = this.wantedZ - this.mob.z;
            double var13 = this.wantedY - this.mob.y;
            double var14 = var11 * var11 + var13 * var13 + var12 * var12;
            if (var14 < 2.5000003E-7F) {
                this.mob.setZza(0.0F);
                return;
            }

            float var15 = (float)(Mth.atan2(var12, var11) * 180.0F / (float)Math.PI) - 90.0F;
            this.mob.yRot = this.rotlerp(this.mob.yRot, var15, 90.0F);
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
            BlockPos var16 = new BlockPos(this.mob);
            BlockState var17 = this.mob.level.getBlockState(var16);
            Block var18 = var17.getBlock();
            VoxelShape var19 = var17.getCollisionShape(this.mob.level, var16);
            if (var13 > (double)this.mob.maxUpStep && var11 * var11 + var12 * var12 < (double)Math.max(1.0F, this.mob.getBbWidth())
                || !var19.isEmpty()
                    && this.mob.y < var19.max(Direction.Axis.Y) + (double)var16.getY()
                    && !var18.is(BlockTags.DOORS)
                    && !var18.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = MoveControl.Operation.JUMPING;
            }
        } else if (this.operation == MoveControl.Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
            if (this.mob.onGround) {
                this.operation = MoveControl.Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0F);
        }

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

    public static enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;
    }
}
