package net.minecraft.world.entity.boss.enderdragon.phases;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;

public class DragonLandingPhase extends AbstractDragonPhaseInstance {
    private Vec3 targetLocation;

    public DragonLandingPhase(EnderDragon param0) {
        super(param0);
    }

    @Override
    public void doClientTick() {
        Vec3 var0 = this.dragon.getHeadLookVector(1.0F).normalize();
        var0.yRot((float) (-Math.PI / 4));
        double var1 = this.dragon.head.x;
        double var2 = this.dragon.head.y + (double)(this.dragon.head.getBbHeight() / 2.0F);
        double var3 = this.dragon.head.z;

        for(int var4 = 0; var4 < 8; ++var4) {
            Random var5 = this.dragon.getRandom();
            double var6 = var1 + var5.nextGaussian() / 2.0;
            double var7 = var2 + var5.nextGaussian() / 2.0;
            double var8 = var3 + var5.nextGaussian() / 2.0;
            Vec3 var9 = this.dragon.getDeltaMovement();
            this.dragon
                .level
                .addParticle(ParticleTypes.DRAGON_BREATH, var6, var7, var8, -var0.x * 0.08F + var9.x, -var0.y * 0.3F + var9.y, -var0.z * 0.08F + var9.z);
            var0.yRot((float) (Math.PI / 16));
        }

    }

    @Override
    public void doServerTick() {
        if (this.targetLocation == null) {
            this.targetLocation = new Vec3(this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));
        }

        if (this.targetLocation.distanceToSqr(this.dragon.x, this.dragon.y, this.dragon.z) < 1.0) {
            this.dragon.getPhaseManager().getPhase(EnderDragonPhase.SITTING_FLAMING).resetFlameCount();
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
        }

    }

    @Override
    public float getFlySpeed() {
        return 1.5F;
    }

    @Override
    public float getTurnSpeed() {
        float var0 = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.dragon.getDeltaMovement())) + 1.0F;
        float var1 = Math.min(var0, 40.0F);
        return var1 / var0;
    }

    @Override
    public void begin() {
        this.targetLocation = null;
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public EnderDragonPhase<DragonLandingPhase> getPhase() {
        return EnderDragonPhase.LANDING;
    }
}
