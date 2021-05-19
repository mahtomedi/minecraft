package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonHoldingPatternPhase extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
    private Path currentPath;
    private Vec3 targetLocation;
    private boolean clockwise;

    public DragonHoldingPatternPhase(EnderDragon param0) {
        super(param0);
    }

    @Override
    public EnderDragonPhase<DragonHoldingPatternPhase> getPhase() {
        return EnderDragonPhase.HOLDING_PATTERN;
    }

    @Override
    public void doServerTick() {
        double var0 = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (var0 < 100.0 || var0 > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.findNewTarget();
        }

    }

    @Override
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath != null && this.currentPath.isDone()) {
            BlockPos var0 = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION));
            int var1 = this.dragon.getDragonFight() == null ? 0 : this.dragon.getDragonFight().getCrystalsAlive();
            if (this.dragon.getRandom().nextInt(var1 + 3) == 0) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
                return;
            }

            double var2 = 64.0;
            Player var3 = this.dragon.level.getNearestPlayer(NEW_TARGET_TARGETING, this.dragon, (double)var0.getX(), (double)var0.getY(), (double)var0.getZ());
            if (var3 != null) {
                var2 = var0.distSqr(var3.position(), true) / 512.0;
            }

            if (var3 != null && (this.dragon.getRandom().nextInt(Mth.abs((int)var2) + 2) == 0 || this.dragon.getRandom().nextInt(var1 + 2) == 0)) {
                this.strafePlayer(var3);
                return;
            }
        }

        if (this.currentPath == null || this.currentPath.isDone()) {
            int var4 = this.dragon.findClosestNode();
            int var5 = var4;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.clockwise = !this.clockwise;
                var5 = var4 + 6;
            }

            if (this.clockwise) {
                ++var5;
            } else {
                --var5;
            }

            if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() >= 0) {
                var5 %= 12;
                if (var5 < 0) {
                    var5 += 12;
                }
            } else {
                var5 -= 12;
                var5 &= 7;
                var5 += 12;
            }

            this.currentPath = this.dragon.findPath(var4, var5, null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
    }

    private void strafePlayer(Player param0) {
        this.dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
        this.dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER).setTarget(param0);
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

    @Override
    public void onCrystalDestroyed(EndCrystal param0, BlockPos param1, DamageSource param2, @Nullable Player param3) {
        if (param3 != null && this.dragon.canAttack(param3)) {
            this.strafePlayer(param3);
        }

    }
}
