package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonTakeoffPhase extends AbstractDragonPhaseInstance {
    private boolean firstTick;
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;

    public DragonTakeoffPhase(EnderDragon param0) {
        super(param0);
    }

    @Override
    public void doServerTick() {
        if (!this.firstTick && this.currentPath != null) {
            BlockPos var0 = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
            if (!var0.closerToCenterThan(this.dragon.position(), 10.0)) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            }
        } else {
            this.firstTick = false;
            this.findNewTarget();
        }

    }

    @Override
    public void begin() {
        this.firstTick = true;
        this.currentPath = null;
        this.targetLocation = null;
    }

    private void findNewTarget() {
        int var0 = this.dragon.findClosestNode();
        Vec3 var1 = this.dragon.getHeadLookVector(1.0F);
        int var2 = this.dragon.findClosestNode(-var1.x * 40.0, 105.0, -var1.z * 40.0);
        if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
            var2 %= 12;
            if (var2 < 0) {
                var2 += 12;
            }
        } else {
            var2 -= 12;
            var2 &= 7;
            var2 += 12;
        }

        this.currentPath = this.dragon.findPath(var0, var2, null);
        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null) {
            this.currentPath.advance();
            if (!this.currentPath.isDone()) {
                Vec3i var0 = this.currentPath.getNextNodePos();
                this.currentPath.advance();

                double var1;
                do {
                    var1 = (double)((float)var0.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
                } while(var1 < (double)var0.getY());

                this.targetLocation = new Vec3((double)var0.getX(), var1, (double)var0.getZ());
            }
        }

    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public EnderDragonPhase<DragonTakeoffPhase> getPhase() {
        return EnderDragonPhase.TAKEOFF;
    }
}
