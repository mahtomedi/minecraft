package net.minecraft.world.entity.monster;

import com.mojang.math.Vector3f;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
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
import net.minecraft.world.entity.ai.control.LookControl;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Shulker extends AbstractGolem implements Enemy {
    private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
    private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(
        COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0, AttributeModifier.Operation.ADDITION
    );
    protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
    protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    private static final int TELEPORT_STEPS = 6;
    private static final byte NO_COLOR = 16;
    private static final byte DEFAULT_COLOR = 16;
    private static final int MAX_TELEPORT_DISTANCE = 8;
    private static final int OTHER_SHULKER_SCAN_RADIUS = 8;
    private static final int OTHER_SHULKER_LIMIT = 5;
    private static final float PEEK_PER_TICK = 0.05F;
    static final Vector3f FORWARD = Util.make(() -> {
        Vec3i var0 = Direction.SOUTH.getNormal();
        return new Vector3f((float)var0.getX(), (float)var0.getY(), (float)var0.getZ());
    });
    private float currentPeekAmountO;
    private float currentPeekAmount;
    @Nullable
    private BlockPos clientOldAttachPosition;
    private int clientSideTeleportInterpolation;
    private static final float MAX_LID_OPEN = 1.0F;

    public Shulker(EntityType<? extends Shulker> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 5;
        this.lookControl = new Shulker.ShulkerLookControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.02F, true));
        this.goalSelector.addGoal(4, new Shulker.ShulkerAttackGoal());
        this.goalSelector.addGoal(7, new Shulker.ShulkerPeekGoal());
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, this.getClass()).setAlertOthers());
        this.targetSelector.addGoal(2, new Shulker.ShulkerNearestAttackGoal(this));
        this.targetSelector.addGoal(3, new Shulker.ShulkerDefenseAttackGoal(this));
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
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
        this.setAttachFace(Direction.from3DDataValue(param0.getByte("AttachFace")));
        this.entityData.set(DATA_PEEK_ID, param0.getByte("Peek"));
        if (param0.contains("Color", 99)) {
            this.entityData.set(DATA_COLOR_ID, param0.getByte("Color"));
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putByte("AttachFace", (byte)this.getAttachFace().get3DDataValue());
        param0.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
        param0.putByte("Color", this.entityData.get(DATA_COLOR_ID));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide && !this.isPassenger() && !this.canStayAt(this.blockPosition(), this.getAttachFace())) {
            this.findNewAttachment();
        }

        if (this.updatePeekAmount()) {
            this.onPeekAmountChange();
        }

        if (this.level.isClientSide) {
            if (this.clientSideTeleportInterpolation > 0) {
                --this.clientSideTeleportInterpolation;
            } else {
                this.clientOldAttachPosition = null;
            }
        }

    }

    private void findNewAttachment() {
        Direction var0 = this.findAttachableSurface(this.blockPosition());
        if (var0 != null) {
            this.setAttachFace(var0);
        } else {
            this.teleportSomewhere();
        }

    }

    @Override
    protected AABB makeBoundingBox() {
        float var0 = getPhysicalPeek(this.currentPeekAmount);
        Direction var1 = this.getAttachFace().getOpposite();
        float var2 = this.getType().getWidth() / 2.0F;
        return getProgressAabb(var1, var0).move(this.getX() - (double)var2, this.getY(), this.getZ() - (double)var2);
    }

    private static float getPhysicalPeek(float param0) {
        return 0.5F - Mth.sin((0.5F + param0) * (float) Math.PI) * 0.5F;
    }

    private boolean updatePeekAmount() {
        this.currentPeekAmountO = this.currentPeekAmount;
        float var0 = (float)this.getRawPeekAmount() * 0.01F;
        if (this.currentPeekAmount == var0) {
            return false;
        } else {
            if (this.currentPeekAmount > var0) {
                this.currentPeekAmount = Mth.clamp(this.currentPeekAmount - 0.05F, var0, 1.0F);
            } else {
                this.currentPeekAmount = Mth.clamp(this.currentPeekAmount + 0.05F, 0.0F, var0);
            }

            return true;
        }
    }

    private void onPeekAmountChange() {
        this.reapplyPosition();
        float var0 = getPhysicalPeek(this.currentPeekAmount);
        float var1 = getPhysicalPeek(this.currentPeekAmountO);
        Direction var2 = this.getAttachFace().getOpposite();
        float var3 = var0 - var1;
        if (!(var3 <= 0.0F)) {
            for(Entity var5 : this.level
                .getEntities(
                    this,
                    getProgressDeltaAabb(var2, var1, var0).move(this.getX() - 0.5, this.getY(), this.getZ() - 0.5),
                    EntitySelector.NO_SPECTATORS.and(param0 -> !param0.isPassengerOfSameVehicle(this))
                )) {
                if (!(var5 instanceof Shulker) && !var5.noPhysics) {
                    var5.move(
                        MoverType.SHULKER,
                        new Vec3((double)(var3 * (float)var2.getStepX()), (double)(var3 * (float)var2.getStepY()), (double)(var3 * (float)var2.getStepZ()))
                    );
                }
            }

        }
    }

    public static AABB getProgressAabb(Direction param0, float param1) {
        return getProgressDeltaAabb(param0, -1.0F, param1);
    }

    public static AABB getProgressDeltaAabb(Direction param0, float param1, float param2) {
        double var0 = (double)Math.max(param1, param2);
        double var1 = (double)Math.min(param1, param2);
        return new AABB(BlockPos.ZERO)
            .expandTowards((double)param0.getStepX() * var0, (double)param0.getStepY() * var0, (double)param0.getStepZ() * var0)
            .contract((double)(-param0.getStepX()) * (1.0 + var1), (double)(-param0.getStepY()) * (1.0 + var1), (double)(-param0.getStepZ()) * (1.0 + var1));
    }

    @Override
    public double getMyRidingOffset() {
        EntityType<?> var0 = this.getVehicle().getType();
        return var0 != EntityType.BOAT && var0 != EntityType.MINECART ? super.getMyRidingOffset() : 0.1875 - this.getVehicle().getPassengersRidingOffset();
    }

    @Override
    public boolean startRiding(Entity param0, boolean param1) {
        if (this.level.isClientSide()) {
            this.clientOldAttachPosition = null;
            this.clientSideTeleportInterpolation = 0;
        }

        this.setAttachFace(Direction.DOWN);
        return super.startRiding(param0, param1);
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (this.level.isClientSide) {
            this.clientOldAttachPosition = this.blockPosition();
        }

        this.yBodyRotO = 0.0F;
        this.yBodyRot = 0.0F;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setYRot(0.0F);
        this.yHeadRot = this.getYRot();
        this.setOldPosAndRot();
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
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
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void setDeltaMovement(Vec3 param0) {
    }

    @Override
    public void setPos(double param0, double param1, double param2) {
        BlockPos var0 = this.blockPosition();
        if (this.isPassenger()) {
            super.setPos(param0, param1, param2);
        } else {
            super.setPos((double)Mth.floor(param0) + 0.5, (double)Mth.floor(param1 + 0.5), (double)Mth.floor(param2) + 0.5);
        }

        if (this.tickCount != 0) {
            BlockPos var1 = this.blockPosition();
            if (!var1.equals(var0)) {
                this.entityData.set(DATA_PEEK_ID, (byte)0);
                this.hasImpulse = true;
                if (this.level.isClientSide && !this.isPassenger() && !var1.equals(this.clientOldAttachPosition)) {
                    this.clientOldAttachPosition = var0;
                    this.clientSideTeleportInterpolation = 6;
                    this.xOld = this.getX();
                    this.yOld = this.getY();
                    this.zOld = this.getZ();
                }
            }

        }
    }

    @Nullable
    protected Direction findAttachableSurface(BlockPos param0) {
        for(Direction var0 : Direction.values()) {
            if (this.canStayAt(param0, var0)) {
                return var0;
            }
        }

        return null;
    }

    boolean canStayAt(BlockPos param0, Direction param1) {
        if (this.isPositionBlocked(param0)) {
            return false;
        } else {
            Direction var0 = param1.getOpposite();
            if (!this.level.loadedAndEntityCanStandOnFace(param0.relative(param1), this, var0)) {
                return false;
            } else {
                AABB var1 = getProgressAabb(var0, 1.0F).move(param0).deflate(1.0E-6);
                return this.level.noCollision(this, var1);
            }
        }
    }

    private boolean isPositionBlocked(BlockPos param0) {
        BlockState var0 = this.level.getBlockState(param0);
        if (var0.isAir()) {
            return false;
        } else {
            boolean var1 = var0.is(Blocks.MOVING_PISTON) && param0.equals(this.blockPosition());
            return !var1;
        }
    }

    protected boolean teleportSomewhere() {
        if (!this.isNoAi() && this.isAlive()) {
            BlockPos var0 = this.blockPosition();

            for(int var1 = 0; var1 < 5; ++var1) {
                BlockPos var2 = var0.offset(
                    Mth.randomBetweenInclusive(this.random, -8, 8),
                    Mth.randomBetweenInclusive(this.random, -8, 8),
                    Mth.randomBetweenInclusive(this.random, -8, 8)
                );
                if (var2.getY() > this.level.getMinBuildHeight()
                    && this.level.isEmptyBlock(var2)
                    && this.level.getWorldBorder().isWithinBounds(var2)
                    && this.level.noCollision(this, new AABB(var2).deflate(1.0E-6))) {
                    Direction var3 = this.findAttachableSurface(var2);
                    if (var3 != null) {
                        this.unRide();
                        this.setAttachFace(var3);
                        this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
                        this.setPos((double)var2.getX() + 0.5, (double)var2.getY(), (double)var2.getZ() + 0.5);
                        this.entityData.set(DATA_PEEK_ID, (byte)0);
                        this.setTarget(null);
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        this.lerpSteps = 0;
        this.setPos(param0, param1, param2);
        this.setRot(param3, param4);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isClosed()) {
            Entity var0 = param0.getDirectEntity();
            if (var0 instanceof AbstractArrow) {
                return false;
            }
        }

        if (!super.hurt(param0, param1)) {
            return false;
        } else {
            if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
                this.teleportSomewhere();
            } else if (param0.isProjectile()) {
                Entity var1 = param0.getDirectEntity();
                if (var1 != null && var1.getType() == EntityType.SHULKER_BULLET) {
                    this.hitByShulkerBullet();
                }
            }

            return true;
        }
    }

    private boolean isClosed() {
        return this.getRawPeekAmount() == 0;
    }

    private void hitByShulkerBullet() {
        Vec3 var0 = this.position();
        AABB var1 = this.getBoundingBox();
        if (!this.isClosed() && this.teleportSomewhere()) {
            int var2 = this.level.getEntities(EntityType.SHULKER, var1.inflate(8.0), Entity::isAlive).size();
            float var3 = (float)(var2 - 1) / 5.0F;
            if (!(this.level.random.nextFloat() < var3)) {
                Shulker var4 = EntityType.SHULKER.create(this.level);
                DyeColor var5 = this.getColor();
                if (var5 != null) {
                    var4.setColor(var5);
                }

                var4.moveTo(var0);
                this.level.addFreshEntity(var4);
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }

    public Direction getAttachFace() {
        return this.entityData.get(DATA_ATTACH_FACE_ID);
    }

    private void setAttachFace(Direction param0) {
        this.entityData.set(DATA_ATTACH_FACE_ID, param0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_ATTACH_FACE_ID.equals(param0)) {
            this.setBoundingBox(this.makeBoundingBox());
        }

        super.onSyncedDataUpdated(param0);
    }

    private int getRawPeekAmount() {
        return this.entityData.get(DATA_PEEK_ID);
    }

    void setRawPeekAmount(int param0) {
        if (!this.level.isClientSide) {
            this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER);
            if (param0 == 0) {
                this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
                this.playSound(SoundEvents.SHULKER_CLOSE, 1.0F, 1.0F);
                this.gameEvent(GameEvent.SHULKER_CLOSE);
            } else {
                this.playSound(SoundEvents.SHULKER_OPEN, 1.0F, 1.0F);
                this.gameEvent(GameEvent.SHULKER_OPEN);
            }
        }

        this.entityData.set(DATA_PEEK_ID, (byte)param0);
    }

    public float getClientPeekAmount(float param0) {
        return Mth.lerp(param0, this.currentPeekAmountO, this.currentPeekAmount);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.5F;
    }

    @Override
    public void recreateFromPacket(ClientboundAddMobPacket param0) {
        super.recreateFromPacket(param0);
        this.yBodyRot = 0.0F;
        this.yBodyRotO = 0.0F;
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

    public Optional<Vec3> getRenderPosition(float param0) {
        if (this.clientOldAttachPosition != null && this.clientSideTeleportInterpolation > 0) {
            double var0 = (double)((float)this.clientSideTeleportInterpolation - param0) / 6.0;
            var0 *= var0;
            BlockPos var1 = this.blockPosition();
            double var2 = (double)(var1.getX() - this.clientOldAttachPosition.getX()) * var0;
            double var3 = (double)(var1.getY() - this.clientOldAttachPosition.getY()) * var0;
            double var4 = (double)(var1.getZ() - this.clientOldAttachPosition.getZ()) * var0;
            return Optional.of(new Vec3(-var2, -var3, -var4));
        } else {
            return Optional.empty();
        }
    }

    private void setColor(DyeColor param0) {
        this.entityData.set(DATA_COLOR_ID, (byte)param0.getId());
    }

    @Nullable
    public DyeColor getColor() {
        byte var0 = this.entityData.get(DATA_COLOR_ID);
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
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (Shulker.this.level.getDifficulty() != Difficulty.PEACEFUL) {
                --this.attackTime;
                LivingEntity var0 = Shulker.this.getTarget();
                if (var0 != null) {
                    Shulker.this.getLookControl().setLookAt(var0, 180.0F, 180.0F);
                    double var1 = Shulker.this.distanceToSqr(var0);
                    if (var1 < 400.0) {
                        if (this.attackTime <= 0) {
                            this.attackTime = 20 + Shulker.this.random.nextInt(10) * 20 / 2;
                            Shulker.this.level
                                .addFreshEntity(new ShulkerBullet(Shulker.this.level, Shulker.this, var0, Shulker.this.getAttachFace().getAxis()));
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
    }

    static class ShulkerBodyRotationControl extends BodyRotationControl {
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

    class ShulkerLookControl extends LookControl {
        public ShulkerLookControl(Mob param0) {
            super(param0);
        }

        @Override
        protected void clampHeadRotationToBody() {
        }

        @Override
        protected Optional<Float> getYRotD() {
            Direction var0 = Shulker.this.getAttachFace().getOpposite();
            Vector3f var1 = Shulker.FORWARD.copy();
            var1.transform(var0.getRotation());
            Vec3i var2 = var0.getNormal();
            Vector3f var3 = new Vector3f((float)var2.getX(), (float)var2.getY(), (float)var2.getZ());
            var3.cross(var1);
            double var4 = this.wantedX - this.mob.getX();
            double var5 = this.wantedY - this.mob.getEyeY();
            double var6 = this.wantedZ - this.mob.getZ();
            Vector3f var7 = new Vector3f((float)var4, (float)var5, (float)var6);
            float var8 = var3.dot(var7);
            float var9 = var1.dot(var7);
            return !(Math.abs(var8) > 1.0E-5F) && !(Math.abs(var9) > 1.0E-5F)
                ? Optional.empty()
                : Optional.of((float)(Mth.atan2((double)(-var8), (double)var9) * 180.0F / (float)Math.PI));
        }

        @Override
        protected Optional<Float> getXRotD() {
            return Optional.of(0.0F);
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

        @Override
        public boolean canUse() {
            return Shulker.this.getTarget() == null
                && Shulker.this.random.nextInt(reducedTickDelay(40)) == 0
                && Shulker.this.canStayAt(Shulker.this.blockPosition(), Shulker.this.getAttachFace());
        }

        @Override
        public boolean canContinueToUse() {
            return Shulker.this.getTarget() == null && this.peekTime > 0;
        }

        @Override
        public void start() {
            this.peekTime = this.adjustedTickDelay(20 * (1 + Shulker.this.random.nextInt(3)));
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
