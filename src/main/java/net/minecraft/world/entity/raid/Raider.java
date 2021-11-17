package net.minecraft.world.entity.raid;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public abstract class Raider extends PatrollingMonster {
    protected static final EntityDataAccessor<Boolean> IS_CELEBRATING = SynchedEntityData.defineId(Raider.class, EntityDataSerializers.BOOLEAN);
    static final Predicate<ItemEntity> ALLOWED_ITEMS = param0 -> !param0.hasPickUpDelay()
            && param0.isAlive()
            && ItemStack.matches(param0.getItem(), Raid.getLeaderBannerInstance());
    @Nullable
    protected Raid raid;
    private int wave;
    private boolean canJoinRaid;
    private int ticksOutsideRaid;

    protected Raider(EntityType<? extends Raider> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new Raider.ObtainRaidLeaderBannerGoal<>(this));
        this.goalSelector.addGoal(3, new PathfindToRaidGoal<>(this));
        this.goalSelector.addGoal(4, new Raider.RaiderMoveThroughVillageGoal(this, 1.05F, 1));
        this.goalSelector.addGoal(5, new Raider.RaiderCelebration(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CELEBRATING, false);
    }

    public abstract void applyRaidBuffs(int var1, boolean var2);

    public boolean canJoinRaid() {
        return this.canJoinRaid;
    }

    public void setCanJoinRaid(boolean param0) {
        this.canJoinRaid = param0;
    }

    @Override
    public void aiStep() {
        if (this.level instanceof ServerLevel && this.isAlive()) {
            Raid var0 = this.getCurrentRaid();
            if (this.canJoinRaid()) {
                if (var0 == null) {
                    if (this.level.getGameTime() % 20L == 0L) {
                        Raid var1 = ((ServerLevel)this.level).getRaidAt(this.blockPosition());
                        if (var1 != null && Raids.canJoinRaid(this, var1)) {
                            var1.joinRaid(var1.getGroupsSpawned(), this, null, true);
                        }
                    }
                } else {
                    LivingEntity var2 = this.getTarget();
                    if (var2 != null && (var2.getType() == EntityType.PLAYER || var2.getType() == EntityType.IRON_GOLEM)) {
                        this.noActionTime = 0;
                    }
                }
            }
        }

        super.aiStep();
    }

    @Override
    protected void updateNoActionTime() {
        this.noActionTime += 2;
    }

    @Override
    public void die(DamageSource param0) {
        if (this.level instanceof ServerLevel) {
            Entity var0 = param0.getEntity();
            Raid var1 = this.getCurrentRaid();
            if (var1 != null) {
                if (this.isPatrolLeader()) {
                    var1.removeLeader(this.getWave());
                }

                if (var0 != null && var0.getType() == EntityType.PLAYER) {
                    var1.addHeroOfTheVillage(var0);
                }

                var1.removeFromRaid(this, false);
            }

            if (this.isPatrolLeader() && var1 == null && ((ServerLevel)this.level).getRaidAt(this.blockPosition()) == null) {
                ItemStack var2 = this.getItemBySlot(EquipmentSlot.HEAD);
                Player var3 = null;
                if (var0 instanceof Player) {
                    var3 = (Player)var0;
                } else if (var0 instanceof Wolf var5) {
                    LivingEntity var6 = var5.getOwner();
                    if (var5.isTame() && var6 instanceof Player) {
                        var3 = (Player)var6;
                    }
                }

                if (!var2.isEmpty() && ItemStack.matches(var2, Raid.getLeaderBannerInstance()) && var3 != null) {
                    MobEffectInstance var7 = var3.getEffect(MobEffects.BAD_OMEN);
                    int var8 = 1;
                    if (var7 != null) {
                        var8 += var7.getAmplifier();
                        var3.removeEffectNoUpdate(MobEffects.BAD_OMEN);
                    } else {
                        --var8;
                    }

                    var8 = Mth.clamp(var8, 0, 4);
                    MobEffectInstance var9 = new MobEffectInstance(MobEffects.BAD_OMEN, 120000, var8, false, false, true);
                    if (!this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                        var3.addEffect(var9);
                    }
                }
            }
        }

        super.die(param0);
    }

    @Override
    public boolean canJoinPatrol() {
        return !this.hasActiveRaid();
    }

    public void setCurrentRaid(@Nullable Raid param0) {
        this.raid = param0;
    }

    @Nullable
    public Raid getCurrentRaid() {
        return this.raid;
    }

    public boolean hasActiveRaid() {
        return this.getCurrentRaid() != null && this.getCurrentRaid().isActive();
    }

    public void setWave(int param0) {
        this.wave = param0;
    }

    public int getWave() {
        return this.wave;
    }

    public boolean isCelebrating() {
        return this.entityData.get(IS_CELEBRATING);
    }

    public void setCelebrating(boolean param0) {
        this.entityData.set(IS_CELEBRATING, param0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Wave", this.wave);
        param0.putBoolean("CanJoinRaid", this.canJoinRaid);
        if (this.raid != null) {
            param0.putInt("RaidId", this.raid.getId());
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.wave = param0.getInt("Wave");
        this.canJoinRaid = param0.getBoolean("CanJoinRaid");
        if (param0.contains("RaidId", 3)) {
            if (this.level instanceof ServerLevel) {
                this.raid = ((ServerLevel)this.level).getRaids().get(param0.getInt("RaidId"));
            }

            if (this.raid != null) {
                this.raid.addWaveMob(this.wave, this, false);
                if (this.isPatrolLeader()) {
                    this.raid.setLeader(this.wave, this);
                }
            }
        }

    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        ItemStack var0 = param0.getItem();
        boolean var1 = this.hasActiveRaid() && this.getCurrentRaid().getLeader(this.getWave()) != null;
        if (this.hasActiveRaid() && !var1 && ItemStack.matches(var0, Raid.getLeaderBannerInstance())) {
            EquipmentSlot var2 = EquipmentSlot.HEAD;
            ItemStack var3 = this.getItemBySlot(var2);
            double var4 = (double)this.getEquipmentDropChance(var2);
            if (!var3.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < var4) {
                this.spawnAtLocation(var3);
            }

            this.onItemPickup(param0);
            this.setItemSlot(var2, var0);
            this.take(param0, var0.getCount());
            param0.discard();
            this.getCurrentRaid().setLeader(this.getWave(), this);
            this.setPatrolLeader(true);
        } else {
            super.pickUpItem(param0);
        }

    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return this.getCurrentRaid() == null ? super.removeWhenFarAway(param0) : false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.getCurrentRaid() != null;
    }

    public int getTicksOutsideRaid() {
        return this.ticksOutsideRaid;
    }

    public void setTicksOutsideRaid(int param0) {
        this.ticksOutsideRaid = param0;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.hasActiveRaid()) {
            this.getCurrentRaid().updateBossbar();
        }

        return super.hurt(param0, param1);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setCanJoinRaid(this.getType() != EntityType.WITCH || param2 != MobSpawnType.NATURAL);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public abstract SoundEvent getCelebrateSound();

    protected class HoldGroundAttackGoal extends Goal {
        private final Raider mob;
        private final float hostileRadiusSqr;
        public final TargetingConditions shoutTargeting = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight().ignoreInvisibilityTesting();

        public HoldGroundAttackGoal(AbstractIllager param1, float param2) {
            this.mob = param1;
            this.hostileRadiusSqr = param2 * param2;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity var0 = this.mob.getLastHurtByMob();
            return this.mob.getCurrentRaid() == null
                && this.mob.isPatrolling()
                && this.mob.getTarget() != null
                && !this.mob.isAggressive()
                && (var0 == null || var0.getType() != EntityType.PLAYER);
        }

        @Override
        public void start() {
            super.start();
            this.mob.getNavigation().stop();

            for(Raider var1 : this.mob.level.getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0))) {
                var1.setTarget(this.mob.getTarget());
            }

        }

        @Override
        public void stop() {
            super.stop();
            LivingEntity var0 = this.mob.getTarget();
            if (var0 != null) {
                for(Raider var2 : this.mob
                    .level
                    .getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0))) {
                    var2.setTarget(var0);
                    var2.setAggressive(true);
                }

                this.mob.setAggressive(true);
            }

        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity var0 = this.mob.getTarget();
            if (var0 != null) {
                if (this.mob.distanceToSqr(var0) > (double)this.hostileRadiusSqr) {
                    this.mob.getLookControl().setLookAt(var0, 30.0F, 30.0F);
                    if (this.mob.random.nextInt(50) == 0) {
                        this.mob.playAmbientSound();
                    }
                } else {
                    this.mob.setAggressive(true);
                }

                super.tick();
            }
        }
    }

    public class ObtainRaidLeaderBannerGoal<T extends Raider> extends Goal {
        private final T mob;

        public ObtainRaidLeaderBannerGoal(T param1) {
            this.mob = param1;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Raid var0 = this.mob.getCurrentRaid();
            if (this.mob.hasActiveRaid()
                && !this.mob.getCurrentRaid().isOver()
                && this.mob.canBeLeader()
                && !ItemStack.matches(this.mob.getItemBySlot(EquipmentSlot.HEAD), Raid.getLeaderBannerInstance())) {
                Raider var1 = var0.getLeader(this.mob.getWave());
                if (var1 == null || !var1.isAlive()) {
                    List<ItemEntity> var2 = this.mob
                        .level
                        .getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(16.0, 8.0, 16.0), Raider.ALLOWED_ITEMS);
                    if (!var2.isEmpty()) {
                        return this.mob.getNavigation().moveTo(var2.get(0), 1.15F);
                    }
                }

                return false;
            } else {
                return false;
            }
        }

        @Override
        public void tick() {
            if (this.mob.getNavigation().getTargetPos().closerThan(this.mob.position(), 1.414)) {
                List<ItemEntity> var0 = this.mob
                    .level
                    .getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(4.0, 4.0, 4.0), Raider.ALLOWED_ITEMS);
                if (!var0.isEmpty()) {
                    this.mob.pickUpItem(var0.get(0));
                }
            }

        }
    }

    public class RaiderCelebration extends Goal {
        private final Raider mob;

        RaiderCelebration(Raider param1) {
            this.mob = param1;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Raid var0 = this.mob.getCurrentRaid();
            return this.mob.isAlive() && this.mob.getTarget() == null && var0 != null && var0.isLoss();
        }

        @Override
        public void start() {
            this.mob.setCelebrating(true);
            super.start();
        }

        @Override
        public void stop() {
            this.mob.setCelebrating(false);
            super.stop();
        }

        @Override
        public void tick() {
            if (!this.mob.isSilent() && this.mob.random.nextInt(this.adjustedTickDelay(100)) == 0) {
                Raider.this.playSound(Raider.this.getCelebrateSound(), Raider.this.getSoundVolume(), Raider.this.getVoicePitch());
            }

            if (!this.mob.isPassenger() && this.mob.random.nextInt(this.adjustedTickDelay(50)) == 0) {
                this.mob.getJumpControl().jump();
            }

            super.tick();
        }
    }

    static class RaiderMoveThroughVillageGoal extends Goal {
        private final Raider raider;
        private final double speedModifier;
        private BlockPos poiPos;
        private final List<BlockPos> visited = Lists.newArrayList();
        private final int distanceToPoi;
        private boolean stuck;

        public RaiderMoveThroughVillageGoal(Raider param0, double param1, int param2) {
            this.raider = param0;
            this.speedModifier = param1;
            this.distanceToPoi = param2;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.updateVisited();
            return this.isValidRaid() && this.hasSuitablePoi() && this.raider.getTarget() == null;
        }

        private boolean isValidRaid() {
            return this.raider.hasActiveRaid() && !this.raider.getCurrentRaid().isOver();
        }

        private boolean hasSuitablePoi() {
            ServerLevel var0 = (ServerLevel)this.raider.level;
            BlockPos var1 = this.raider.blockPosition();
            Optional<BlockPos> var2 = var0.getPoiManager()
                .getRandom(param0 -> param0 == PoiType.HOME, this::hasNotVisited, PoiManager.Occupancy.ANY, var1, 48, this.raider.random);
            if (!var2.isPresent()) {
                return false;
            } else {
                this.poiPos = var2.get().immutable();
                return true;
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (this.raider.getNavigation().isDone()) {
                return false;
            } else {
                return this.raider.getTarget() == null
                    && !this.poiPos.closerThan(this.raider.position(), (double)(this.raider.getBbWidth() + (float)this.distanceToPoi))
                    && !this.stuck;
            }
        }

        @Override
        public void stop() {
            if (this.poiPos.closerThan(this.raider.position(), (double)this.distanceToPoi)) {
                this.visited.add(this.poiPos);
            }

        }

        @Override
        public void start() {
            super.start();
            this.raider.setNoActionTime(0);
            this.raider.getNavigation().moveTo((double)this.poiPos.getX(), (double)this.poiPos.getY(), (double)this.poiPos.getZ(), this.speedModifier);
            this.stuck = false;
        }

        @Override
        public void tick() {
            if (this.raider.getNavigation().isDone()) {
                Vec3 var0 = Vec3.atBottomCenterOf(this.poiPos);
                Vec3 var1 = DefaultRandomPos.getPosTowards(this.raider, 16, 7, var0, (float) (Math.PI / 10));
                if (var1 == null) {
                    var1 = DefaultRandomPos.getPosTowards(this.raider, 8, 7, var0, (float) (Math.PI / 2));
                }

                if (var1 == null) {
                    this.stuck = true;
                    return;
                }

                this.raider.getNavigation().moveTo(var1.x, var1.y, var1.z, this.speedModifier);
            }

        }

        private boolean hasNotVisited(BlockPos param0) {
            for(BlockPos var0x : this.visited) {
                if (Objects.equals(param0, var0x)) {
                    return false;
                }
            }

            return true;
        }

        private void updateVisited() {
            if (this.visited.size() > 2) {
                this.visited.remove(0);
            }

        }
    }
}
