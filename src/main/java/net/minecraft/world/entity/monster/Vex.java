package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class Vex extends Monster {
    public static final float FLAP_DEGREES_PER_TICK = 45.836624F;
    public static final int TICKS_PER_FLAP = Mth.ceil((float) (Math.PI * 5.0 / 4.0));
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Vex.class, EntityDataSerializers.BYTE);
    private static final int FLAG_IS_CHARGING = 1;
    private Mob owner;
    @Nullable
    private BlockPos boundOrigin;
    private boolean hasLimitedLife;
    private int limitedLifeTicks;

    public Vex(EntityType<? extends Vex> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new Vex.VexMoveControl(this);
        this.xpReward = 3;
    }

    @Override
    public boolean isFlapping() {
        return this.tickCount % TICKS_PER_FLAP == 0;
    }

    @Override
    public void move(MoverType param0, Vec3 param1) {
        super.move(param0, param1);
        this.checkInsideBlocks();
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
        if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
            this.limitedLifeTicks = 20;
            this.hurt(DamageSource.STARVE, 1.0F);
        }

    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new Vex.VexChargeAttackGoal());
        this.goalSelector.addGoal(8, new Vex.VexRandomMoveGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new Vex.VexCopyOwnerTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("BoundX")) {
            this.boundOrigin = new BlockPos(param0.getInt("BoundX"), param0.getInt("BoundY"), param0.getInt("BoundZ"));
        }

        if (param0.contains("LifeTicks")) {
            this.setLimitedLife(param0.getInt("LifeTicks"));
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.boundOrigin != null) {
            param0.putInt("BoundX", this.boundOrigin.getX());
            param0.putInt("BoundY", this.boundOrigin.getY());
            param0.putInt("BoundZ", this.boundOrigin.getZ());
        }

        if (this.hasLimitedLife) {
            param0.putInt("LifeTicks", this.limitedLifeTicks);
        }

    }

    public Mob getOwner() {
        return this.owner;
    }

    @Nullable
    public BlockPos getBoundOrigin() {
        return this.boundOrigin;
    }

    public void setBoundOrigin(@Nullable BlockPos param0) {
        this.boundOrigin = param0;
    }

    private boolean getVexFlag(int param0) {
        int var0 = this.entityData.get(DATA_FLAGS_ID);
        return (var0 & param0) != 0;
    }

    private void setVexFlag(int param0, boolean param1) {
        int var0 = this.entityData.get(DATA_FLAGS_ID);
        if (param1) {
            var0 |= param0;
        } else {
            var0 &= ~param0;
        }

        this.entityData.set(DATA_FLAGS_ID, (byte)(var0 & 0xFF));
    }

    public boolean isCharging() {
        return this.getVexFlag(1);
    }

    public void setIsCharging(boolean param0) {
        this.setVexFlag(1, param0);
    }

    public void setOwner(Mob param0) {
        this.owner = param0;
    }

    public void setLimitedLife(int param0) {
        this.hasLimitedLife = true;
        this.limitedLifeTicks = param0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.populateDefaultEquipmentSlots(param1);
        this.populateDefaultEquipmentEnchantments(param1);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    class VexChargeAttackGoal extends Goal {
        public VexChargeAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (Vex.this.getTarget() != null && !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(7) == 0) {
                return Vex.this.distanceToSqr(Vex.this.getTarget()) > 4.0;
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return Vex.this.getMoveControl().hasWanted() && Vex.this.isCharging() && Vex.this.getTarget() != null && Vex.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity var0 = Vex.this.getTarget();
            Vec3 var1 = var0.getEyePosition();
            Vex.this.moveControl.setWantedPosition(var1.x, var1.y, var1.z, 1.0);
            Vex.this.setIsCharging(true);
            Vex.this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
        }

        @Override
        public void stop() {
            Vex.this.setIsCharging(false);
        }

        @Override
        public void tick() {
            LivingEntity var0 = Vex.this.getTarget();
            if (Vex.this.getBoundingBox().intersects(var0.getBoundingBox())) {
                Vex.this.doHurtTarget(var0);
                Vex.this.setIsCharging(false);
            } else {
                double var1 = Vex.this.distanceToSqr(var0);
                if (var1 < 9.0) {
                    Vec3 var2 = var0.getEyePosition();
                    Vex.this.moveControl.setWantedPosition(var2.x, var2.y, var2.z, 1.0);
                }
            }

        }
    }

    class VexCopyOwnerTargetGoal extends TargetGoal {
        private final TargetingConditions copyOwnerTargeting = new TargetingConditions().allowUnseeable().ignoreInvisibilityTesting();

        public VexCopyOwnerTargetGoal(PathfinderMob param0) {
            super(param0, false);
        }

        @Override
        public boolean canUse() {
            return Vex.this.owner != null && Vex.this.owner.getTarget() != null && this.canAttack(Vex.this.owner.getTarget(), this.copyOwnerTargeting);
        }

        @Override
        public void start() {
            Vex.this.setTarget(Vex.this.owner.getTarget());
            super.start();
        }
    }

    class VexMoveControl extends MoveControl {
        public VexMoveControl(Vex param0) {
            super(param0);
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                Vec3 var0 = new Vec3(this.wantedX - Vex.this.getX(), this.wantedY - Vex.this.getY(), this.wantedZ - Vex.this.getZ());
                double var1 = var0.length();
                if (var1 < Vex.this.getBoundingBox().getSize()) {
                    this.operation = MoveControl.Operation.WAIT;
                    Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().scale(0.5));
                } else {
                    Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().add(var0.scale(this.speedModifier * 0.05 / var1)));
                    if (Vex.this.getTarget() == null) {
                        Vec3 var2 = Vex.this.getDeltaMovement();
                        Vex.this.yRot = -((float)Mth.atan2(var2.x, var2.z)) * (180.0F / (float)Math.PI);
                        Vex.this.yBodyRot = Vex.this.yRot;
                    } else {
                        double var3 = Vex.this.getTarget().getX() - Vex.this.getX();
                        double var4 = Vex.this.getTarget().getZ() - Vex.this.getZ();
                        Vex.this.yRot = -((float)Mth.atan2(var3, var4)) * (180.0F / (float)Math.PI);
                        Vex.this.yBodyRot = Vex.this.yRot;
                    }
                }

            }
        }
    }

    class VexRandomMoveGoal extends Goal {
        public VexRandomMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(7) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            BlockPos var0 = Vex.this.getBoundOrigin();
            if (var0 == null) {
                var0 = Vex.this.blockPosition();
            }

            for(int var1 = 0; var1 < 3; ++var1) {
                BlockPos var2 = var0.offset(Vex.this.random.nextInt(15) - 7, Vex.this.random.nextInt(11) - 5, Vex.this.random.nextInt(15) - 7);
                if (Vex.this.level.isEmptyBlock(var2)) {
                    Vex.this.moveControl.setWantedPosition((double)var2.getX() + 0.5, (double)var2.getY() + 0.5, (double)var2.getZ() + 0.5, 0.25);
                    if (Vex.this.getTarget() == null) {
                        Vex.this.getLookControl().setLookAt((double)var2.getX() + 0.5, (double)var2.getY() + 0.5, (double)var2.getZ() + 0.5, 180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }
}
