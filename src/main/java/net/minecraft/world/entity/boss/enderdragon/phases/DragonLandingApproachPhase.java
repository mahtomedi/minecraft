package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonLandingApproachPhase extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEAR_EGG_TARGETING = new TargetingConditions().range(128.0);
    private Path currentPath;
    private Vec3 targetLocation;

    public DragonLandingApproachPhase(EnderDragon param0) {
        super(param0);
    }

    @Override
    public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
        return EnderDragonPhase.LANDING_APPROACH;
    }

    @Override
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override
    public void doServerTick() {
        double var0 = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (var0 < 100.0 || var0 > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.findNewTarget();
        }

    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int var0 = this.dragon.findClosestNode();
            BlockPos var1 = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
            Player var2 = this.dragon.level.getNearestPlayer(NEAR_EGG_TARGETING, (double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
            int var4;
            if (var2 != null) {
                Vec3 var3 = new Vec3(var2.getX(), 0.0, var2.getZ()).normalize();
                var4 = this.dragon.findClosestNode(-var3.x * 40.0, 105.0, -var3.z * 40.0);
            } else {
                var4 = this.dragon.findClosestNode(40.0, (double)var1.getY(), 0.0);
            }

            Node var6 = new Node(var1.getX(), var1.getY(), var1.getZ());
            this.currentPath = this.dragon.findPath(var0, var4, var6);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
        if (this.currentPath != null && this.currentPath.isDone()) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        }

    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3i var0 = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double var1 = (double)var0.getX();
            double var2 = (double)var0.getZ();

            double var3;
            do {
                var3 = (double)((float)var0.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
            } while(var3 < (double)var0.getY());

            this.targetLocation = new Vec3(var1, var3, var2);
        }

    }
}
