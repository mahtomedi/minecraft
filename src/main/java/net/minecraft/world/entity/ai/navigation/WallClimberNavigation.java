package net.minecraft.world.entity.ai.navigation;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class WallClimberNavigation extends GroundPathNavigation {
    @Nullable
    private BlockPos pathToPosition;

    public WallClimberNavigation(Mob param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public Path createPath(BlockPos param0, int param1) {
        this.pathToPosition = param0;
        return super.createPath(param0, param1);
    }

    @Override
    public Path createPath(Entity param0, int param1) {
        this.pathToPosition = param0.blockPosition();
        return super.createPath(param0, param1);
    }

    @Override
    public boolean moveTo(Entity param0, double param1) {
        Path var0 = this.createPath(param0, 0);
        if (var0 != null) {
            return this.moveTo(var0, param1);
        } else {
            this.pathToPosition = param0.blockPosition();
            this.speedModifier = param1;
            return true;
        }
    }

    @Override
    public void tick() {
        if (!this.isDone()) {
            super.tick();
        } else {
            if (this.pathToPosition != null) {
                if (!this.pathToPosition.closerThan(this.mob.position(), (double)this.mob.getBbWidth())
                    && (
                        !(this.mob.getY() > (double)this.pathToPosition.getY())
                            || !new BlockPos((double)this.pathToPosition.getX(), this.mob.getY(), (double)this.pathToPosition.getZ())
                                .closerThan(this.mob.position(), (double)this.mob.getBbWidth())
                    )) {
                    this.mob
                        .getMoveControl()
                        .setWantedPosition(
                            (double)this.pathToPosition.getX(), (double)this.pathToPosition.getY(), (double)this.pathToPosition.getZ(), this.speedModifier
                        );
                } else {
                    this.pathToPosition = null;
                }
            }

        }
    }
}
