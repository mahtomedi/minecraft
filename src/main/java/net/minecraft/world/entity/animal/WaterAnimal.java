package net.minecraft.world.entity.animal;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public abstract class WaterAnimal extends PathfinderMob {
    protected WaterAnimal(EntityType<? extends WaterAnimal> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected int getExperienceReward(Player param0) {
        return 1 + this.level.random.nextInt(3);
    }

    protected void handleAirSupply(int param0) {
        if (this.isAlive() && !this.isInWaterOrBubble()) {
            this.setAirSupply(param0 - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAirSupply(300);
        }

    }

    @Override
    public void baseTick() {
        int var0 = this.getAirSupply();
        super.baseTick();
        this.handleAirSupply(var0);
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return false;
    }
}
