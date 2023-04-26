package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

public class DragonSittingScanningPhase extends AbstractDragonSittingPhase {
    private static final int SITTING_SCANNING_IDLE_TICKS = 100;
    private static final int SITTING_ATTACK_Y_VIEW_RANGE = 10;
    private static final int SITTING_ATTACK_VIEW_RANGE = 20;
    private static final int SITTING_CHARGE_VIEW_RANGE = 150;
    private static final TargetingConditions CHARGE_TARGETING = TargetingConditions.forCombat().range(150.0);
    private final TargetingConditions scanTargeting;
    private int scanningTime;

    public DragonSittingScanningPhase(EnderDragon param0) {
        super(param0);
        this.scanTargeting = TargetingConditions.forCombat().range(20.0).selector(param1 -> Math.abs(param1.getY() - param0.getY()) <= 10.0);
    }

    @Override
    public void doServerTick() {
        ++this.scanningTime;
        LivingEntity var0 = this.dragon.level().getNearestPlayer(this.scanTargeting, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (var0 != null) {
            if (this.scanningTime > 25) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_ATTACKING);
            } else {
                Vec3 var1 = new Vec3(var0.getX() - this.dragon.getX(), 0.0, var0.getZ() - this.dragon.getZ()).normalize();
                Vec3 var2 = new Vec3(
                        (double)Mth.sin(this.dragon.getYRot() * (float) (Math.PI / 180.0)),
                        0.0,
                        (double)(-Mth.cos(this.dragon.getYRot() * (float) (Math.PI / 180.0)))
                    )
                    .normalize();
                float var3 = (float)var2.dot(var1);
                float var4 = (float)(Math.acos((double)var3) * 180.0F / (float)Math.PI) + 0.5F;
                if (var4 < 0.0F || var4 > 10.0F) {
                    double var5 = var0.getX() - this.dragon.head.getX();
                    double var6 = var0.getZ() - this.dragon.head.getZ();
                    double var7 = Mth.clamp(
                        Mth.wrapDegrees(180.0 - Mth.atan2(var5, var6) * 180.0F / (float)Math.PI - (double)this.dragon.getYRot()), -100.0, 100.0
                    );
                    this.dragon.yRotA *= 0.8F;
                    float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6) + 1.0F;
                    float var9 = var8;
                    if (var8 > 40.0F) {
                        var8 = 40.0F;
                    }

                    this.dragon.yRotA += (float)var7 * (0.7F / var8 / var9);
                    this.dragon.setYRot(this.dragon.getYRot() + this.dragon.yRotA);
                }
            }
        } else if (this.scanningTime >= 100) {
            var0 = this.dragon.level().getNearestPlayer(CHARGE_TARGETING, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            if (var0 != null) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                this.dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(new Vec3(var0.getX(), var0.getY(), var0.getZ()));
            }
        }

    }

    @Override
    public void begin() {
        this.scanningTime = 0;
    }

    @Override
    public EnderDragonPhase<DragonSittingScanningPhase> getPhase() {
        return EnderDragonPhase.SITTING_SCANNING;
    }
}
