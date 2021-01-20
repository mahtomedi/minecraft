package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DragonStrafePlayerPhase extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogManager.getLogger();
    private int fireballCharge;
    private Path currentPath;
    private Vec3 targetLocation;
    private LivingEntity attackTarget;
    private boolean holdingPatternClockwise;

    public DragonStrafePlayerPhase(EnderDragon param0) {
        super(param0);
    }

    @Override
    public void doServerTick() {
        if (this.attackTarget == null) {
            LOGGER.warn("Skipping player strafe phase because no player was found");
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        } else {
            if (this.currentPath != null && this.currentPath.isDone()) {
                double var0 = this.attackTarget.getX();
                double var1 = this.attackTarget.getZ();
                double var2 = var0 - this.dragon.getX();
                double var3 = var1 - this.dragon.getZ();
                double var4 = (double)Mth.sqrt(var2 * var2 + var3 * var3);
                double var5 = Math.min(0.4F + var4 / 80.0 - 1.0, 10.0);
                this.targetLocation = new Vec3(var0, this.attackTarget.getY() + var5, var1);
            }

            double var6 = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            if (var6 < 100.0 || var6 > 22500.0) {
                this.findNewTarget();
            }

            double var7 = 64.0;
            if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0) {
                if (this.dragon.canSee(this.attackTarget)) {
                    ++this.fireballCharge;
                    Vec3 var8 = new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0, this.attackTarget.getZ() - this.dragon.getZ()).normalize();
                    Vec3 var9 = new Vec3(
                            (double)Mth.sin(this.dragon.yRot * (float) (Math.PI / 180.0)),
                            0.0,
                            (double)(-Mth.cos(this.dragon.yRot * (float) (Math.PI / 180.0)))
                        )
                        .normalize();
                    float var10 = (float)var9.dot(var8);
                    float var11 = (float)(Math.acos((double)var10) * 180.0F / (float)Math.PI);
                    var11 += 0.5F;
                    if (this.fireballCharge >= 5 && var11 >= 0.0F && var11 < 10.0F) {
                        double var12 = 1.0;
                        Vec3 var13 = this.dragon.getViewVector(1.0F);
                        double var14 = this.dragon.head.getX() - var13.x * 1.0;
                        double var15 = this.dragon.head.getY(0.5) + 0.5;
                        double var16 = this.dragon.head.getZ() - var13.z * 1.0;
                        double var17 = this.attackTarget.getX() - var14;
                        double var18 = this.attackTarget.getY(0.5) - var15;
                        double var19 = this.attackTarget.getZ() - var16;
                        if (!this.dragon.isSilent()) {
                            this.dragon.level.levelEvent(null, 1017, this.dragon.blockPosition(), 0);
                        }

                        DragonFireball var20 = new DragonFireball(this.dragon.level, this.dragon, var17, var18, var19);
                        var20.moveTo(var14, var15, var16, 0.0F, 0.0F);
                        this.dragon.level.addFreshEntity(var20);
                        this.fireballCharge = 0;
                        if (this.currentPath != null) {
                            while(!this.currentPath.isDone()) {
                                this.currentPath.advance();
                            }
                        }

                        this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
                    }
                } else if (this.fireballCharge > 0) {
                    --this.fireballCharge;
                }
            } else if (this.fireballCharge > 0) {
                --this.fireballCharge;
            }

        }
    }

    private void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int var0 = this.dragon.findClosestNode();
            int var1 = var0;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.holdingPatternClockwise = !this.holdingPatternClockwise;
                var1 = var0 + 6;
            }

            if (this.holdingPatternClockwise) {
                ++var1;
            } else {
                --var1;
            }

            if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
                var1 %= 12;
                if (var1 < 0) {
                    var1 += 12;
                }
            } else {
                var1 -= 12;
                var1 &= 7;
                var1 += 12;
            }

            this.currentPath = this.dragon.findPath(var0, var1, null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
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
    public void begin() {
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;
    }

    public void setTarget(LivingEntity param0) {
        this.attackTarget = param0;
        int var0 = this.dragon.findClosestNode();
        int var1 = this.dragon.findClosestNode(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
        int var2 = this.attackTarget.getBlockX();
        int var3 = this.attackTarget.getBlockZ();
        double var4 = (double)var2 - this.dragon.getX();
        double var5 = (double)var3 - this.dragon.getZ();
        double var6 = (double)Mth.sqrt(var4 * var4 + var5 * var5);
        double var7 = Math.min(0.4F + var6 / 80.0 - 1.0, 10.0);
        int var8 = Mth.floor(this.attackTarget.getY() + var7);
        Node var9 = new Node(var2, var8, var3);
        this.currentPath = this.dragon.findPath(var0, var1, var9);
        if (this.currentPath != null) {
            this.currentPath.advance();
            this.navigateToNextPathNode();
        }

    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public EnderDragonPhase<DragonStrafePlayerPhase> getPhase() {
        return EnderDragonPhase.STRAFE_PLAYER;
    }
}
