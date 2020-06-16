package net.minecraft.world.entity.monster;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;

public class ZombifiedPiglin extends Zombie implements NeutralMob {
    private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(
        SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.05, AttributeModifier.Operation.ADDITION
    );
    private static final IntRange FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
    private int playFirstAngerSoundIn;
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    private static final IntRange ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private int ticksUntilNextAlert;

    public ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID param0) {
        this.persistentAngerTarget = param0;
    }

    @Override
    public double getMyRidingOffset() {
        return this.isBaby() ? -0.16 : -0.45;
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0)
            .add(Attributes.MOVEMENT_SPEED, 0.23F)
            .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        AttributeInstance var0 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.isAngry()) {
            if (!this.isBaby() && !var0.hasModifier(SPEED_MODIFIER_ATTACKING)) {
                var0.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }

            this.maybePlayFirstAngerSound();
        } else if (var0.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            var0.removeModifier(SPEED_MODIFIER_ATTACKING);
        }

        this.updatePersistentAnger((ServerLevel)this.level, true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }

        if (this.isAngry()) {
            this.lastHurtByPlayerTime = this.tickCount;
        }

        super.customServerAiStep();
    }

    private void maybePlayFirstAngerSound() {
        if (this.playFirstAngerSoundIn > 0) {
            --this.playFirstAngerSoundIn;
            if (this.playFirstAngerSoundIn == 0) {
                this.playAngerSound();
            }
        }

    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            --this.ticksUntilNextAlert;
        } else {
            if (this.getSensing().canSee(this.getTarget())) {
                this.alertOthers();
            }

            this.ticksUntilNextAlert = ALERT_INTERVAL.randomValue(this.random);
        }
    }

    private void alertOthers() {
        double var0 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB var1 = AABB.unitCubeFromLowerCorner(this.position()).inflate(var0, 10.0, var0);
        this.level
            .getLoadedEntitiesOfClass(ZombifiedPiglin.class, var1)
            .stream()
            .filter(param0 -> param0 != this)
            .filter(param0 -> param0.getTarget() == null)
            .filter(param0 -> !param0.isAlliedTo(this.getTarget()))
            .forEach(param0 -> param0.setTarget(this.getTarget()));
    }

    private void playAngerSound() {
        this.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, this.getVoicePitch() * 1.8F);
    }

    @Override
    public void setTarget(@Nullable LivingEntity param0) {
        if (this.getTarget() == null && param0 != null) {
            this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.randomValue(this.random);
            this.ticksUntilNextAlert = ALERT_INTERVAL.randomValue(this.random);
        }

        if (param0 instanceof Player) {
            this.setLastHurtByPlayer((Player)param0);
        }

        super.setTarget(param0);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
    }

    public static boolean checkZombifiedPiglinSpawnRules(
        EntityType<ZombifiedPiglin> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4
    ) {
        return param1.getDifficulty() != Difficulty.PEACEFUL && param1.getBlockState(param3.below()).getBlock() != Blocks.NETHER_WART_BLOCK;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this) && !param0.containsAnyLiquid(this.getBoundingBox());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        this.addPersistentAngerSaveData(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.readPersistentAngerSaveData((ServerLevel)this.level, param0);
    }

    @Override
    public void setRemainingPersistentAngerTime(int param0) {
        this.remainingPersistentAngerTime = param0;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        return this.isInvulnerableTo(param0) ? false : super.hurt(param0, param1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void randomizeReinforcementsChance() {
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
    }

    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public boolean isPreventingPlayerRest(Player param0) {
        return this.isAngryAt(param0);
    }
}
