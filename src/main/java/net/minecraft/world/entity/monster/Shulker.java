package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.ShulkerSharedHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Shulker extends AbstractGolem implements Enemy {
    private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
    private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(
        COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0, AttributeModifier.Operation.ADDITION
    );
    protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
    protected static final EntityDataAccessor<Optional<BlockPos>> DATA_ATTACH_POS_ID = SynchedEntityData.defineId(
        Shulker.class, EntityDataSerializers.OPTIONAL_BLOCK_POS
    );
    protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    private float currentPeekAmountO;
    private float currentPeekAmount;
    private BlockPos oldAttachPosition;
    private int clientSideTeleportInterpolation;

    public Shulker(EntityType<? extends Shulker> param0, Level param1) {
        super(param0, param1);
        this.yBodyRotO = 180.0F;
        this.yBodyRot = 180.0F;
        this.oldAttachPosition = null;
        this.xpReward = 5;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.yBodyRot = 180.0F;
        this.yBodyRotO = 180.0F;
        this.yRot = 180.0F;
        this.yRotO = 180.0F;
        this.yHeadRot = 180.0F;
        this.yHeadRotO = 180.0F;
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new Shulker.ShulkerAttackGoal());
        this.goalSelector.addGoal(7, new Shulker.ShulkerPeekGoal());
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new Shulker.ShulkerNearestAttackGoal(this));
        this.targetSelector.addGoal(3, new Shulker.ShulkerDefenseAttackGoal(this));
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHULKER_AMBIENT;
    }

    @Override
    public void playAmbientSound() {
        if (!this.isClosed()) {
            super.playAmbientSound();
        }

    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHULKER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isClosed() ? SoundEvents.SHULKER_HURT_CLOSED : SoundEvents.SHULKER_HURT;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACH_FACE_ID, Direction.DOWN);
        this.entityData.define(DATA_ATTACH_POS_ID, Optional.empty());
        this.entityData.define(DATA_PEEK_ID, (byte)0);
        this.entityData.define(DATA_COLOR_ID, (byte)16);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new Shulker.ShulkerBodyRotationControl(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.entityData.set(DATA_ATTACH_FACE_ID, Direction.from3DDataValue(param0.getByte("AttachFace")));
        this.entityData.set(DATA_PEEK_ID, param0.getByte("Peek"));
        this.entityData.set(DATA_COLOR_ID, param0.getByte("Color"));
        if (param0.contains("APX")) {
            int var0 = param0.getInt("APX");
            int var1 = param0.getInt("APY");
            int var2 = param0.getInt("APZ");
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(new BlockPos(var0, var1, var2)));
        } else {
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.empty());
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putByte("AttachFace", (byte)this.entityData.get(DATA_ATTACH_FACE_ID).get3DDataValue());
        param0.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
        param0.putByte("Color", this.entityData.get(DATA_COLOR_ID));
        BlockPos var0 = this.getAttachPosition();
        if (var0 != null) {
            param0.putInt("APX", var0.getX());
            param0.putInt("APY", var0.getY());
            param0.putInt("APZ", var0.getZ());
        }

    }

    @Override
    public void tick() {
        super.tick();
        BlockPos var0 = this.entityData.get(DATA_ATTACH_POS_ID).orElse(null);
        if (var0 == null && !this.level.isClientSide) {
            var0 = this.blockPosition();
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(var0));
        }

        if (this.isPassenger()) {
            var0 = null;
            float var1 = this.getVehicle().yRot;
            this.yRot = var1;
            this.yBodyRot = var1;
            this.yBodyRotO = var1;
            this.clientSideTeleportInterpolation = 0;
        } else if (!this.level.isClientSide) {
            BlockState var2 = this.level.getBlockState(var0);
            if (!var2.isAir()) {
                if (var2.getBlock() == Blocks.MOVING_PISTON) {
                    Direction var3 = var2.getValue(PistonBaseBlock.FACING);
                    if (this.level.isEmptyBlock(var0.relative(var3))) {
                        var0 = var0.relative(var3);
                        this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(var0));
                    } else {
                        this.teleportSomewhere();
                    }
                } else if (var2.getBlock() == Blocks.PISTON_HEAD) {
                    Direction var4 = var2.getValue(PistonHeadBlock.FACING);
                    if (this.level.isEmptyBlock(var0.relative(var4))) {
                        var0 = var0.relative(var4);
                        this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(var0));
                    } else {
                        this.teleportSomewhere();
                    }
                } else {
                    this.teleportSomewhere();
                }
            }

            Direction var5 = this.getAttachFace();
            if (!this.canAttachOnBlockFace(var0, var5)) {
                Direction var6 = this.findAttachableFace(var0);
                if (var6 != null) {
                    this.entityData.set(DATA_ATTACH_FACE_ID, var6);
                } else {
                    this.teleportSomewhere();
                }
            }
        }

        float var7 = (float)this.getRawPeekAmount() * 0.01F;
        this.currentPeekAmountO = this.currentPeekAmount;
        if (this.currentPeekAmount > var7) {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount - 0.05F, var7, 1.0F);
        } else if (this.currentPeekAmount < var7) {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount + 0.05F, 0.0F, var7);
        }

        if (var0 != null) {
            if (this.level.isClientSide) {
                if (this.clientSideTeleportInterpolation > 0 && this.oldAttachPosition != null) {
                    --this.clientSideTeleportInterpolation;
                } else {
                    this.oldAttachPosition = var0;
                }
            }

            this.setPosAndOldPos((double)var0.getX() + 0.5, (double)var0.getY(), (double)var0.getZ() + 0.5);
            double var8 = 0.5 - (double)Mth.sin((0.5F + this.currentPeekAmount) * (float) Math.PI) * 0.5;
            double var9 = 0.5 - (double)Mth.sin((0.5F + this.currentPeekAmountO) * (float) Math.PI) * 0.5;
            Direction var10 = this.getAttachFace().getOpposite();
            this.setBoundingBox(
                new AABB(this.getX() - 0.5, this.getY(), this.getZ() - 0.5, this.getX() + 0.5, this.getY() + 1.0, this.getZ() + 0.5)
                    .expandTowards((double)var10.getStepX() * var8, (double)var10.getStepY() * var8, (double)var10.getStepZ() * var8)
            );
            double var11 = var8 - var9;
            if (var11 > 0.0) {
                List<Entity> var12 = this.level.getEntities(this, this.getBoundingBox());
                if (!var12.isEmpty()) {
                    for(Entity var13 : var12) {
                        if (!(var13 instanceof Shulker) && !var13.noPhysics) {
                            var13.move(
                                MoverType.SHULKER,
                                new Vec3(var11 * (double)var10.getStepX(), var11 * (double)var10.getStepY(), var11 * (double)var10.getStepZ())
                            );
                        }
                    }
                }
            }
        }

    }

    @Override
    public void move(MoverType param0, Vec3 param1) {
        if (param0 == MoverType.SHULKER_BOX) {
            this.teleportSomewhere();
        } else {
            super.move(param0, param1);
        }

    }

    @Override
    public void setPos(double param0, double param1, double param2) {
        super.setPos(param0, param1, param2);
        if (this.entityData != null && this.tickCount != 0) {
            Optional<BlockPos> var0 = this.entityData.get(DATA_ATTACH_POS_ID);
            Optional<BlockPos> var1 = Optional.of(new BlockPos(param0, param1, param2));
            if (!var1.equals(var0)) {
                this.entityData.set(DATA_ATTACH_POS_ID, var1);
                this.entityData.set(DATA_PEEK_ID, (byte)0);
                this.hasImpulse = true;
            }

        }
    }

    @Nullable
    protected Direction findAttachableFace(BlockPos param0) {
        for(Direction var0 : Direction.values()) {
            if (this.canAttachOnBlockFace(param0, var0)) {
                return var0;
            }
        }

        return null;
    }

    private boolean canAttachOnBlockFace(BlockPos param0, Direction param1) {
        return this.level.loadedAndEntityCanStandOnFace(param0.relative(param1), this, param1.getOpposite())
            && this.level.noCollision(this, ShulkerSharedHelper.openBoundingBox(param0, param1.getOpposite()));
    }

    protected boolean teleportSomewhere() {
        if (!this.isNoAi() && this.isAlive()) {
            BlockPos var0 = this.blockPosition();

            for(int var1 = 0; var1 < 5; ++var1) {
                BlockPos var2 = var0.offset(8 - this.random.nextInt(17), 8 - this.random.nextInt(17), 8 - this.random.nextInt(17));
                if (var2.getY() > 0
                    && this.level.isEmptyBlock(var2)
                    && this.level.getWorldBorder().isWithinBounds(var2)
                    && this.level.noCollision(this, new AABB(var2))) {
                    Direction var3 = this.findAttachableFace(var2);
                    if (var3 != null) {
                        this.entityData.set(DATA_ATTACH_FACE_ID, var3);
                        this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
                        this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(var2));
                        this.entityData.set(DATA_PEEK_ID, (byte)0);
                        this.setTarget(null);
                        return true;
                    }
                }
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setDeltaMovement(Vec3.ZERO);
        this.yBodyRotO = 180.0F;
        this.yBodyRot = 180.0F;
        this.yRot = 180.0F;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_ATTACH_POS_ID.equals(param0) && this.level.isClientSide && !this.isPassenger()) {
            BlockPos var0 = this.getAttachPosition();
            if (var0 != null) {
                if (this.oldAttachPosition == null) {
                    this.oldAttachPosition = var0;
                } else {
                    this.clientSideTeleportInterpolation = 6;
                }

                this.setPosAndOldPos((double)var0.getX() + 0.5, (double)var0.getY(), (double)var0.getZ() + 0.5);
            }
        }

        super.onSyncedDataUpdated(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        this.lerpSteps = 0;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isClosed()) {
            Entity var0 = param0.getDirectEntity();
            if (var0 instanceof AbstractArrow) {
                return false;
            }
        }

        if (super.hurt(param0, param1)) {
            if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
                this.teleportSomewhere();
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean isClosed() {
        return this.getRawPeekAmount() == 0;
    }

    @Nullable
    @Override
    public AABB getCollideBox() {
        return this.isAlive() ? this.getBoundingBox() : null;
    }

    public Direction getAttachFace() {
        return this.entityData.get(DATA_ATTACH_FACE_ID);
    }

    @Nullable
    public BlockPos getAttachPosition() {
        return this.entityData.get(DATA_ATTACH_POS_ID).orElse(null);
    }

    public void setAttachPosition(@Nullable BlockPos param0) {
        this.entityData.set(DATA_ATTACH_POS_ID, Optional.ofNullable(param0));
    }

    public int getRawPeekAmount() {
        return this.entityData.get(DATA_PEEK_ID);
    }

    public void setRawPeekAmount(int param0) {
        if (!this.level.isClientSide) {
            this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER);
            if (param0 == 0) {
                this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
                this.playSound(SoundEvents.SHULKER_CLOSE, 1.0F, 1.0F);
            } else {
                this.playSound(SoundEvents.SHULKER_OPEN, 1.0F, 1.0F);
            }
        }

        this.entityData.set(DATA_PEEK_ID, (byte)param0);
    }

    @OnlyIn(Dist.CLIENT)
    public float getClientPeekAmount(float param0) {
        return Mth.lerp(param0, this.currentPeekAmountO, this.currentPeekAmount);
    }

    @OnlyIn(Dist.CLIENT)
    public int getClientSideTeleportInterpolation() {
        return this.clientSideTeleportInterpolation;
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getOldAttachPosition() {
        return this.oldAttachPosition;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.5F;
    }

    @Override
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override
    public int getMaxHeadYRot() {
        return 180;
    }

    @Override
    public void push(Entity param0) {
    }

    @Override
    public float getPickRadius() {
        return 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasValidInterpolationPositions() {
        return this.oldAttachPosition != null && this.getAttachPosition() != null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public DyeColor getColor() {
        Byte var0 = this.entityData.get(DATA_COLOR_ID);
        return var0 != 16 && var0 <= 15 ? DyeColor.byId(var0) : null;
    }

    class ShulkerAttackGoal extends Goal {
        private int attackTime;

        public ShulkerAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity var0 = Shulker.this.getTarget();
            if (var0 != null && var0.isAlive()) {
                return Shulker.this.level.getDifficulty() != Difficulty.PEACEFUL;
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            this.attackTime = 20;
            Shulker.this.setRawPeekAmount(100);
        }

        @Override
        public void stop() {
            Shulker.this.setRawPeekAmount(0);
        }

        @Override
        public void tick() {
            if (Shulker.this.level.getDifficulty() != Difficulty.PEACEFUL) {
                --this.attackTime;
                LivingEntity var0 = Shulker.this.getTarget();
                Shulker.this.getLookControl().setLookAt(var0, 180.0F, 180.0F);
                double var1 = Shulker.this.distanceToSqr(var0);
                if (var1 < 400.0) {
                    if (this.attackTime <= 0) {
                        this.attackTime = 20 + Shulker.this.random.nextInt(10) * 20 / 2;
                        Shulker.this.level.addFreshEntity(new ShulkerBullet(Shulker.this.level, Shulker.this, var0, Shulker.this.getAttachFace().getAxis()));
                        Shulker.this.playSound(
                            SoundEvents.SHULKER_SHOOT, 2.0F, (Shulker.this.random.nextFloat() - Shulker.this.random.nextFloat()) * 0.2F + 1.0F
                        );
                    }
                } else {
                    Shulker.this.setTarget(null);
                }

                super.tick();
            }
        }
    }

    class ShulkerBodyRotationControl extends BodyRotationControl {
        public ShulkerBodyRotationControl(Mob param0) {
            super(param0);
        }

        @Override
        public void clientTick() {
        }
    }

    static class ShulkerDefenseAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
        public ShulkerDefenseAttackGoal(Shulker param0) {
            super(param0, LivingEntity.class, 10, true, false, param0x -> param0x instanceof Enemy);
        }

        @Override
        public boolean canUse() {
            return this.mob.getTeam() == null ? false : super.canUse();
        }

        @Override
        protected AABB getTargetSearchArea(double param0) {
            Direction var0 = ((Shulker)this.mob).getAttachFace();
            if (var0.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0, param0, param0);
            } else {
                return var0.getAxis() == Direction.Axis.Z
                    ? this.mob.getBoundingBox().inflate(param0, param0, 4.0)
                    : this.mob.getBoundingBox().inflate(param0, 4.0, param0);
            }
        }
    }

    class ShulkerNearestAttackGoal extends NearestAttackableTargetGoal<Player> {
        public ShulkerNearestAttackGoal(Shulker param0) {
            super(param0, Player.class, true);
        }

        @Override
        public boolean canUse() {
            return Shulker.this.level.getDifficulty() == Difficulty.PEACEFUL ? false : super.canUse();
        }

        @Override
        protected AABB getTargetSearchArea(double param0) {
            Direction var0 = ((Shulker)this.mob).getAttachFace();
            if (var0.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0, param0, param0);
            } else {
                return var0.getAxis() == Direction.Axis.Z
                    ? this.mob.getBoundingBox().inflate(param0, param0, 4.0)
                    : this.mob.getBoundingBox().inflate(param0, 4.0, param0);
            }
        }
    }

    class ShulkerPeekGoal extends Goal {
        private int peekTime;

        private ShulkerPeekGoal() {
        }

        @Override
        public boolean canUse() {
            return Shulker.this.getTarget() == null && Shulker.this.random.nextInt(40) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return Shulker.this.getTarget() == null && this.peekTime > 0;
        }

        @Override
        public void start() {
            this.peekTime = 20 * (1 + Shulker.this.random.nextInt(3));
            Shulker.this.setRawPeekAmount(30);
        }

        @Override
        public void stop() {
            if (Shulker.this.getTarget() == null) {
                Shulker.this.setRawPeekAmount(0);
            }

        }

        @Override
        public void tick() {
            --this.peekTime;
        }
    }
}
