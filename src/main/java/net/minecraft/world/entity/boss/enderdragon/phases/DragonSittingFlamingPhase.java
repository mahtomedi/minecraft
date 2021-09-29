package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

public class DragonSittingFlamingPhase extends AbstractDragonSittingPhase {
    private static final int FLAME_DURATION = 200;
    private static final int SITTING_FLAME_ATTACKS_COUNT = 4;
    private static final int WARMUP_TIME = 10;
    private int flameTicks;
    private int flameCount;
    @Nullable
    private AreaEffectCloud flame;

    public DragonSittingFlamingPhase(EnderDragon param0) {
        super(param0);
    }

    @Override
    public void doClientTick() {
        ++this.flameTicks;
        if (this.flameTicks % 2 == 0 && this.flameTicks < 10) {
            Vec3 var0 = this.dragon.getHeadLookVector(1.0F).normalize();
            var0.yRot((float) (-Math.PI / 4));
            double var1 = this.dragon.head.getX();
            double var2 = this.dragon.head.getY(0.5);
            double var3 = this.dragon.head.getZ();

            for(int var4 = 0; var4 < 8; ++var4) {
                double var5 = var1 + this.dragon.getRandom().nextGaussian() / 2.0;
                double var6 = var2 + this.dragon.getRandom().nextGaussian() / 2.0;
                double var7 = var3 + this.dragon.getRandom().nextGaussian() / 2.0;

                for(int var8 = 0; var8 < 6; ++var8) {
                    this.dragon
                        .level
                        .addParticle(
                            ParticleTypes.DRAGON_BREATH, var5, var6, var7, -var0.x * 0.08F * (double)var8, -var0.y * 0.6F, -var0.z * 0.08F * (double)var8
                        );
                }

                var0.yRot((float) (Math.PI / 16));
            }
        }

    }

    @Override
    public void doServerTick() {
        ++this.flameTicks;
        if (this.flameTicks >= 200) {
            if (this.flameCount >= 4) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            } else {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
            }
        } else if (this.flameTicks == 10) {
            Vec3 var0 = new Vec3(this.dragon.head.getX() - this.dragon.getX(), 0.0, this.dragon.head.getZ() - this.dragon.getZ()).normalize();
            float var1 = 5.0F;
            double var2 = this.dragon.head.getX() + var0.x * 5.0 / 2.0;
            double var3 = this.dragon.head.getZ() + var0.z * 5.0 / 2.0;
            double var4 = this.dragon.head.getY(0.5);
            double var5 = var4;
            BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos(var2, var4, var3);

            while(this.dragon.level.isEmptyBlock(var6)) {
                if (--var5 < 0.0) {
                    var5 = var4;
                    break;
                }

                var6.set(var2, var5, var3);
            }

            var5 = (double)(Mth.floor(var5) + 1);
            this.flame = new AreaEffectCloud(this.dragon.level, var2, var5, var3);
            this.flame.setOwner(this.dragon);
            this.flame.setRadius(5.0F);
            this.flame.setDuration(200);
            this.flame.setParticle(ParticleTypes.DRAGON_BREATH);
            this.flame.addEffect(new MobEffectInstance(MobEffects.HARM));
            this.dragon.level.addFreshEntity(this.flame);
        }

    }

    @Override
    public void begin() {
        this.flameTicks = 0;
        ++this.flameCount;
    }

    @Override
    public void end() {
        if (this.flame != null) {
            this.flame.discard();
            this.flame = null;
        }

    }

    @Override
    public EnderDragonPhase<DragonSittingFlamingPhase> getPhase() {
        return EnderDragonPhase.SITTING_FLAMING;
    }

    public void resetFlameCount() {
        this.flameCount = 0;
    }
}
