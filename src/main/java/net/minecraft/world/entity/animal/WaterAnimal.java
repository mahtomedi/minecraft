package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class WaterAnimal extends PathfinderMob {
    protected WaterAnimal(EntityType<? extends WaterAnimal> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
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
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return false;
    }

    public static boolean checkUndergroundWaterCreatureSpawnRules(
        EntityType<? extends LivingEntity> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4
    ) {
        return param3.getY() < param1.getSeaLevel() && param3.getY() < param1.getHeight(Heightmap.Types.OCEAN_FLOOR, param3.getX(), param3.getZ());
    }
}
