package net.minecraft.world.entity.monster;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class ZombifiedPiglin extends Zombie {
    private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(
        SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.05, AttributeModifier.Operation.ADDITION
    );
    private int angerTime;
    private int playAngrySoundIn;
    private UUID lastHurtByUUID;

    public ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity param0) {
        super.setLastHurtByMob(param0);
        if (param0 != null) {
            this.lastHurtByUUID = param0.getUUID();
        }

    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new ZombifiedPiglin.ZombifiedPiglinHurtByOtherGoal(this));
        this.targetSelector.addGoal(2, new ZombifiedPiglin.ZombifiedPiglinAngerTargetGoal(this));
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
        LivingEntity var1 = this.getLastHurtByMob();
        if (this.isAngry()) {
            if (!this.isBaby() && !var0.hasModifier(SPEED_MODIFIER_ATTACKING)) {
                var0.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }

            --this.angerTime;
            LivingEntity var2 = var1 != null ? var1 : this.getTarget();
            if (!this.isAngry() && var2 != null) {
                if (!this.canSee(var2)) {
                    this.setLastHurtByMob(null);
                    this.setTarget(null);
                } else {
                    this.angerTime = this.getAngerTime();
                }
            }
        } else if (var0.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            var0.removeModifier(SPEED_MODIFIER_ATTACKING);
        }

        if (this.playAngrySoundIn > 0 && --this.playAngrySoundIn == 0) {
            this.playSound(
                SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 1.8F
            );
        }

        if (this.isAngry() && this.lastHurtByUUID != null && var1 == null) {
            Player var3 = this.level.getPlayerByUUID(this.lastHurtByUUID);
            this.setLastHurtByMob(var3);
            this.lastHurtByPlayer = var3;
            this.lastHurtByPlayerTime = this.getLastHurtByMobTimestamp();
        }

        super.customServerAiStep();
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
        param0.putShort("Anger", (short)this.angerTime);
        if (this.lastHurtByUUID != null) {
            param0.putUUID("HurtBy", this.lastHurtByUUID);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.angerTime = param0.getShort("Anger");
        if (param0.hasUUID("HurtBy")) {
            this.lastHurtByUUID = param0.getUUID("HurtBy");
            Player var0 = this.level.getPlayerByUUID(this.lastHurtByUUID);
            this.setLastHurtByMob(var0);
            if (var0 != null) {
                this.lastHurtByPlayer = var0;
                this.lastHurtByPlayerTime = this.getLastHurtByMobTimestamp();
            }
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            Entity var0 = param0.getEntity();
            if (var0 instanceof Player && !((Player)var0).isCreative() && this.canSee(var0)) {
                this.makeAngry((LivingEntity)var0);
            }

            return super.hurt(param0, param1);
        }
    }

    private boolean makeAngry(LivingEntity param0) {
        this.angerTime = this.getAngerTime();
        this.playAngrySoundIn = this.random.nextInt(40);
        this.setLastHurtByMob(param0);
        return true;
    }

    private int getAngerTime() {
        return 400 + this.random.nextInt(400);
    }

    private boolean isAngry() {
        return this.angerTime > 0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
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
    public boolean isPreventingPlayerRest(Player param0) {
        return this.isAngry();
    }

    static class ZombifiedPiglinAngerTargetGoal extends NearestAttackableTargetGoal<Player> {
        public ZombifiedPiglinAngerTargetGoal(ZombifiedPiglin param0) {
            super(param0, Player.class, true);
        }

        @Override
        public boolean canUse() {
            return ((ZombifiedPiglin)this.mob).isAngry() && super.canUse();
        }
    }

    static class ZombifiedPiglinHurtByOtherGoal extends HurtByTargetGoal {
        public ZombifiedPiglinHurtByOtherGoal(ZombifiedPiglin param0) {
            super(param0);
            this.setAlertOthers(new Class[]{Zombie.class});
        }

        @Override
        protected void alertOther(Mob param0, LivingEntity param1) {
            if (param0 instanceof ZombifiedPiglin && this.mob.canSee(param1) && ((ZombifiedPiglin)param0).makeAngry(param1)) {
                param0.setTarget(param1);
            }

        }
    }
}
