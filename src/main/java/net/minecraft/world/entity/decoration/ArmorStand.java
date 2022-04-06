package net.minecraft.world.entity.decoration;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmorStand extends LivingEntity {
    public static final int WOBBLE_TIME = 5;
    private static final boolean ENABLE_ARMS = true;
    private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0F, 0.0F, -10.0F);
    private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0F, 0.0F, 10.0F);
    private static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0F, 0.0F, -1.0F);
    private static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0F, 0.0F, 1.0F);
    private static final EntityDimensions MARKER_DIMENSIONS = new EntityDimensions(0.0F, 0.0F, true);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scale(0.5F);
    private static final double FEET_OFFSET = 0.1;
    private static final double CHEST_OFFSET = 0.9;
    private static final double LEGS_OFFSET = 0.4;
    private static final double HEAD_OFFSET = 1.6;
    public static final int DISABLE_TAKING_OFFSET = 8;
    public static final int DISABLE_PUTTING_OFFSET = 16;
    public static final int CLIENT_FLAG_SMALL = 1;
    public static final int CLIENT_FLAG_SHOW_ARMS = 4;
    public static final int CLIENT_FLAG_NO_BASEPLATE = 8;
    public static final int CLIENT_FLAG_MARKER = 16;
    public static final EntityDataAccessor<Byte> DATA_CLIENT_FLAGS = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    private static final Predicate<Entity> RIDABLE_MINECARTS = param0 -> param0 instanceof AbstractMinecart
            && ((AbstractMinecart)param0).getMinecartType() == AbstractMinecart.Type.RIDEABLE;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    private boolean invisible;
    public long lastHit;
    private int disabledSlots;
    private Rotations headPose = DEFAULT_HEAD_POSE;
    private Rotations bodyPose = DEFAULT_BODY_POSE;
    private Rotations leftArmPose = DEFAULT_LEFT_ARM_POSE;
    private Rotations rightArmPose = DEFAULT_RIGHT_ARM_POSE;
    private Rotations leftLegPose = DEFAULT_LEFT_LEG_POSE;
    private Rotations rightLegPose = DEFAULT_RIGHT_LEG_POSE;

    public ArmorStand(EntityType<? extends ArmorStand> param0, Level param1) {
        super(param0, param1);
        this.maxUpStep = 0.0F;
    }

    public ArmorStand(Level param0, double param1, double param2, double param3) {
        this(EntityType.ARMOR_STAND, param0);
        this.setPos(param1, param2, param3);
    }

    @Override
    public void refreshDimensions() {
        double var0 = this.getX();
        double var1 = this.getY();
        double var2 = this.getZ();
        super.refreshDimensions();
        this.setPos(var0, var1, var2);
    }

    private boolean hasPhysics() {
        return !this.isMarker() && !this.isNoGravity();
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && this.hasPhysics();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CLIENT_FLAGS, (byte)0);
        this.entityData.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
        this.entityData.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
        this.entityData.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
        this.entityData.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
        this.entityData.define(DATA_LEFT_LEG_POSE, DEFAULT_LEFT_LEG_POSE);
        this.entityData.define(DATA_RIGHT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot param0) {
        switch(param0.getType()) {
            case HAND:
                return this.handItems.get(param0.getIndex());
            case ARMOR:
                return this.armorItems.get(param0.getIndex());
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot param0, ItemStack param1) {
        this.verifyEquippedItem(param1);
        this.equipEventAndSound(param1, true);
        switch(param0.getType()) {
            case HAND:
                this.handItems.set(param0.getIndex(), param1);
                break;
            case ARMOR:
                this.armorItems.set(param0.getIndex(), param1);
        }

    }

    @Override
    public boolean canTakeItem(ItemStack param0) {
        EquipmentSlot var0 = Mob.getEquipmentSlotForItem(param0);
        return this.getItemBySlot(var0).isEmpty() && !this.isDisabled(var0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        ListTag var0 = new ListTag();

        for(ItemStack var1 : this.armorItems) {
            CompoundTag var2 = new CompoundTag();
            if (!var1.isEmpty()) {
                var1.save(var2);
            }

            var0.add(var2);
        }

        param0.put("ArmorItems", var0);
        ListTag var3 = new ListTag();

        for(ItemStack var4 : this.handItems) {
            CompoundTag var5 = new CompoundTag();
            if (!var4.isEmpty()) {
                var4.save(var5);
            }

            var3.add(var5);
        }

        param0.put("HandItems", var3);
        param0.putBoolean("Invisible", this.isInvisible());
        param0.putBoolean("Small", this.isSmall());
        param0.putBoolean("ShowArms", this.isShowArms());
        param0.putInt("DisabledSlots", this.disabledSlots);
        param0.putBoolean("NoBasePlate", this.isNoBasePlate());
        if (this.isMarker()) {
            param0.putBoolean("Marker", this.isMarker());
        }

        param0.put("Pose", this.writePose());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("ArmorItems", 9)) {
            ListTag var0 = param0.getList("ArmorItems", 10);

            for(int var1 = 0; var1 < this.armorItems.size(); ++var1) {
                this.armorItems.set(var1, ItemStack.of(var0.getCompound(var1)));
            }
        }

        if (param0.contains("HandItems", 9)) {
            ListTag var2 = param0.getList("HandItems", 10);

            for(int var3 = 0; var3 < this.handItems.size(); ++var3) {
                this.handItems.set(var3, ItemStack.of(var2.getCompound(var3)));
            }
        }

        this.setInvisible(param0.getBoolean("Invisible"));
        this.setSmall(param0.getBoolean("Small"));
        this.setShowArms(param0.getBoolean("ShowArms"));
        this.disabledSlots = param0.getInt("DisabledSlots");
        this.setNoBasePlate(param0.getBoolean("NoBasePlate"));
        this.setMarker(param0.getBoolean("Marker"));
        this.noPhysics = !this.hasPhysics();
        CompoundTag var4 = param0.getCompound("Pose");
        this.readPose(var4);
    }

    private void readPose(CompoundTag param0) {
        ListTag var0 = param0.getList("Head", 5);
        this.setHeadPose(var0.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(var0));
        ListTag var1 = param0.getList("Body", 5);
        this.setBodyPose(var1.isEmpty() ? DEFAULT_BODY_POSE : new Rotations(var1));
        ListTag var2 = param0.getList("LeftArm", 5);
        this.setLeftArmPose(var2.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(var2));
        ListTag var3 = param0.getList("RightArm", 5);
        this.setRightArmPose(var3.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(var3));
        ListTag var4 = param0.getList("LeftLeg", 5);
        this.setLeftLegPose(var4.isEmpty() ? DEFAULT_LEFT_LEG_POSE : new Rotations(var4));
        ListTag var5 = param0.getList("RightLeg", 5);
        this.setRightLegPose(var5.isEmpty() ? DEFAULT_RIGHT_LEG_POSE : new Rotations(var5));
    }

    private CompoundTag writePose() {
        CompoundTag var0 = new CompoundTag();
        if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
            var0.put("Head", this.headPose.save());
        }

        if (!DEFAULT_BODY_POSE.equals(this.bodyPose)) {
            var0.put("Body", this.bodyPose.save());
        }

        if (!DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
            var0.put("LeftArm", this.leftArmPose.save());
        }

        if (!DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
            var0.put("RightArm", this.rightArmPose.save());
        }

        if (!DEFAULT_LEFT_LEG_POSE.equals(this.leftLegPose)) {
            var0.put("LeftLeg", this.leftLegPose.save());
        }

        if (!DEFAULT_RIGHT_LEG_POSE.equals(this.rightLegPose)) {
            var0.put("RightLeg", this.rightLegPose.save());
        }

        return var0;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity param0) {
    }

    @Override
    protected void pushEntities() {
        List<Entity> var0 = this.level.getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            Entity var2 = var0.get(var1);
            if (this.distanceToSqr(var2) <= 0.2) {
                var2.push(this);
            }
        }

    }

    @Override
    public InteractionResult interactAt(Player param0, Vec3 param1, InteractionHand param2) {
        ItemStack var0 = param0.getItemInHand(param2);
        if (this.isMarker() || var0.is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        } else if (param0.isSpectator()) {
            return InteractionResult.SUCCESS;
        } else if (param0.level.isClientSide) {
            return InteractionResult.CONSUME;
        } else {
            EquipmentSlot var1 = Mob.getEquipmentSlotForItem(var0);
            if (var0.isEmpty()) {
                EquipmentSlot var2 = this.getClickedSlot(param1);
                EquipmentSlot var3 = this.isDisabled(var2) ? var1 : var2;
                if (this.hasItemInSlot(var3) && this.swapItem(param0, var3, var0, param2)) {
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (this.isDisabled(var1)) {
                    return InteractionResult.FAIL;
                }

                if (var1.getType() == EquipmentSlot.Type.HAND && !this.isShowArms()) {
                    return InteractionResult.FAIL;
                }

                if (this.swapItem(param0, var1, var0, param2)) {
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    private EquipmentSlot getClickedSlot(Vec3 param0) {
        EquipmentSlot var0 = EquipmentSlot.MAINHAND;
        boolean var1 = this.isSmall();
        double var2 = var1 ? param0.y * 2.0 : param0.y;
        EquipmentSlot var3 = EquipmentSlot.FEET;
        if (var2 >= 0.1 && var2 < 0.1 + (var1 ? 0.8 : 0.45) && this.hasItemInSlot(var3)) {
            var0 = EquipmentSlot.FEET;
        } else if (var2 >= 0.9 + (var1 ? 0.3 : 0.0) && var2 < 0.9 + (var1 ? 1.0 : 0.7) && this.hasItemInSlot(EquipmentSlot.CHEST)) {
            var0 = EquipmentSlot.CHEST;
        } else if (var2 >= 0.4 && var2 < 0.4 + (var1 ? 1.0 : 0.8) && this.hasItemInSlot(EquipmentSlot.LEGS)) {
            var0 = EquipmentSlot.LEGS;
        } else if (var2 >= 1.6 && this.hasItemInSlot(EquipmentSlot.HEAD)) {
            var0 = EquipmentSlot.HEAD;
        } else if (!this.hasItemInSlot(EquipmentSlot.MAINHAND) && this.hasItemInSlot(EquipmentSlot.OFFHAND)) {
            var0 = EquipmentSlot.OFFHAND;
        }

        return var0;
    }

    private boolean isDisabled(EquipmentSlot param0) {
        return (this.disabledSlots & 1 << param0.getFilterFlag()) != 0 || param0.getType() == EquipmentSlot.Type.HAND && !this.isShowArms();
    }

    private boolean swapItem(Player param0, EquipmentSlot param1, ItemStack param2, InteractionHand param3) {
        ItemStack var0 = this.getItemBySlot(param1);
        if (!var0.isEmpty() && (this.disabledSlots & 1 << param1.getFilterFlag() + 8) != 0) {
            return false;
        } else if (var0.isEmpty() && (this.disabledSlots & 1 << param1.getFilterFlag() + 16) != 0) {
            return false;
        } else if (param0.getAbilities().instabuild && var0.isEmpty() && !param2.isEmpty()) {
            ItemStack var1 = param2.copy();
            var1.setCount(1);
            this.setItemSlot(param1, var1);
            return true;
        } else if (param2.isEmpty() || param2.getCount() <= 1) {
            this.setItemSlot(param1, param2);
            param0.setItemInHand(param3, var0);
            return true;
        } else if (!var0.isEmpty()) {
            return false;
        } else {
            ItemStack var2 = param2.copy();
            var2.setCount(1);
            this.setItemSlot(param1, var2);
            param2.shrink(1);
            return true;
        }
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.level.isClientSide || this.isRemoved()) {
            return false;
        } else if (DamageSource.OUT_OF_WORLD.equals(param0)) {
            this.kill();
            return false;
        } else if (this.isInvulnerableTo(param0) || this.invisible || this.isMarker()) {
            return false;
        } else if (param0.isExplosion()) {
            this.brokenByAnything(param0);
            this.kill();
            return false;
        } else if (DamageSource.IN_FIRE.equals(param0)) {
            if (this.isOnFire()) {
                this.causeDamage(param0, 0.15F);
            } else {
                this.setSecondsOnFire(5);
            }

            return false;
        } else if (DamageSource.ON_FIRE.equals(param0) && this.getHealth() > 0.5F) {
            this.causeDamage(param0, 4.0F);
            return false;
        } else {
            boolean var0 = param0.getDirectEntity() instanceof AbstractArrow;
            boolean var1 = var0 && ((AbstractArrow)param0.getDirectEntity()).getPierceLevel() > 0;
            boolean var2 = "player".equals(param0.getMsgId());
            if (!var2 && !var0) {
                return false;
            } else if (param0.getEntity() instanceof Player && !((Player)param0.getEntity()).getAbilities().mayBuild) {
                return false;
            } else if (param0.isCreativePlayer()) {
                this.playBrokenSound();
                this.showBreakingParticles();
                this.kill();
                return var1;
            } else {
                long var3 = this.level.getGameTime();
                if (var3 - this.lastHit > 5L && !var0) {
                    this.level.broadcastEntityEvent(this, (byte)32);
                    this.gameEvent(GameEvent.ENTITY_DAMAGE, param0.getEntity());
                    this.lastHit = var3;
                } else {
                    this.brokenByPlayer(param0);
                    this.showBreakingParticles();
                    this.kill();
                }

                return true;
            }
        }
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 32) {
            if (this.level.isClientSide) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
                this.lastHit = this.level.getGameTime();
            }
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(var0) || var0 == 0.0) {
            var0 = 4.0;
        }

        var0 *= 64.0;
        return param0 < var0 * var0;
    }

    private void showBreakingParticles() {
        if (this.level instanceof ServerLevel) {
            ((ServerLevel)this.level)
                .sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
                    this.getX(),
                    this.getY(0.6666666666666666),
                    this.getZ(),
                    10,
                    (double)(this.getBbWidth() / 4.0F),
                    (double)(this.getBbHeight() / 4.0F),
                    (double)(this.getBbWidth() / 4.0F),
                    0.05
                );
        }

    }

    private void causeDamage(DamageSource param0, float param1) {
        float var0 = this.getHealth();
        var0 -= param1;
        if (var0 <= 0.5F) {
            this.brokenByAnything(param0);
            this.kill();
        } else {
            this.setHealth(var0);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, param0.getEntity());
        }

    }

    private void brokenByPlayer(DamageSource param0) {
        Block.popResource(this.level, this.blockPosition(), new ItemStack(Items.ARMOR_STAND));
        this.brokenByAnything(param0);
    }

    private void brokenByAnything(DamageSource param0) {
        this.playBrokenSound();
        this.dropAllDeathLoot(param0);

        for(int var0 = 0; var0 < this.handItems.size(); ++var0) {
            ItemStack var1 = this.handItems.get(var0);
            if (!var1.isEmpty()) {
                Block.popResource(this.level, this.blockPosition().above(), var1);
                this.handItems.set(var0, ItemStack.EMPTY);
            }
        }

        for(int var2 = 0; var2 < this.armorItems.size(); ++var2) {
            ItemStack var3 = this.armorItems.get(var2);
            if (!var3.isEmpty()) {
                Block.popResource(this.level, this.blockPosition().above(), var3);
                this.armorItems.set(var2, ItemStack.EMPTY);
            }
        }

    }

    private void playBrokenSound() {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    protected float tickHeadTurn(float param0, float param1) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.getYRot();
        return 0.0F;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * (this.isBaby() ? 0.5F : 0.9F);
    }

    @Override
    public double getMyRidingOffset() {
        return this.isMarker() ? 0.0 : 0.1F;
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.hasPhysics()) {
            super.travel(param0);
        }
    }

    @Override
    public void setYBodyRot(float param0) {
        this.yBodyRotO = this.yRotO = param0;
        this.yHeadRotO = this.yHeadRot = param0;
    }

    @Override
    public void setYHeadRot(float param0) {
        this.yBodyRotO = this.yRotO = param0;
        this.yHeadRotO = this.yHeadRot = param0;
    }

    @Override
    public void tick() {
        super.tick();
        Rotations var0 = this.entityData.get(DATA_HEAD_POSE);
        if (!this.headPose.equals(var0)) {
            this.setHeadPose(var0);
        }

        Rotations var1 = this.entityData.get(DATA_BODY_POSE);
        if (!this.bodyPose.equals(var1)) {
            this.setBodyPose(var1);
        }

        Rotations var2 = this.entityData.get(DATA_LEFT_ARM_POSE);
        if (!this.leftArmPose.equals(var2)) {
            this.setLeftArmPose(var2);
        }

        Rotations var3 = this.entityData.get(DATA_RIGHT_ARM_POSE);
        if (!this.rightArmPose.equals(var3)) {
            this.setRightArmPose(var3);
        }

        Rotations var4 = this.entityData.get(DATA_LEFT_LEG_POSE);
        if (!this.leftLegPose.equals(var4)) {
            this.setLeftLegPose(var4);
        }

        Rotations var5 = this.entityData.get(DATA_RIGHT_LEG_POSE);
        if (!this.rightLegPose.equals(var5)) {
            this.setRightLegPose(var5);
        }

    }

    @Override
    protected void updateInvisibilityStatus() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean param0) {
        this.invisible = param0;
        super.setInvisible(param0);
    }

    @Override
    public boolean isBaby() {
        return this.isSmall();
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean ignoreExplosion() {
        return this.isInvisible();
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return this.isMarker() ? PushReaction.IGNORE : super.getPistonPushReaction();
    }

    private void setSmall(boolean param0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 1, param0));
    }

    public boolean isSmall() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 1) != 0;
    }

    private void setShowArms(boolean param0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 4, param0));
    }

    public boolean isShowArms() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 4) != 0;
    }

    private void setNoBasePlate(boolean param0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 8, param0));
    }

    public boolean isNoBasePlate() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 8) != 0;
    }

    private void setMarker(boolean param0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 16, param0));
    }

    public boolean isMarker() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 16) != 0;
    }

    private byte setBit(byte param0, int param1, boolean param2) {
        if (param2) {
            param0 = (byte)(param0 | param1);
        } else {
            param0 = (byte)(param0 & ~param1);
        }

        return param0;
    }

    public void setHeadPose(Rotations param0) {
        this.headPose = param0;
        this.entityData.set(DATA_HEAD_POSE, param0);
    }

    public void setBodyPose(Rotations param0) {
        this.bodyPose = param0;
        this.entityData.set(DATA_BODY_POSE, param0);
    }

    public void setLeftArmPose(Rotations param0) {
        this.leftArmPose = param0;
        this.entityData.set(DATA_LEFT_ARM_POSE, param0);
    }

    public void setRightArmPose(Rotations param0) {
        this.rightArmPose = param0;
        this.entityData.set(DATA_RIGHT_ARM_POSE, param0);
    }

    public void setLeftLegPose(Rotations param0) {
        this.leftLegPose = param0;
        this.entityData.set(DATA_LEFT_LEG_POSE, param0);
    }

    public void setRightLegPose(Rotations param0) {
        this.rightLegPose = param0;
        this.entityData.set(DATA_RIGHT_LEG_POSE, param0);
    }

    public Rotations getHeadPose() {
        return this.headPose;
    }

    public Rotations getBodyPose() {
        return this.bodyPose;
    }

    public Rotations getLeftArmPose() {
        return this.leftArmPose;
    }

    public Rotations getRightArmPose() {
        return this.rightArmPose;
    }

    public Rotations getLeftLegPose() {
        return this.leftLegPose;
    }

    public Rotations getRightLegPose() {
        return this.rightLegPose;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.isMarker();
    }

    @Override
    public boolean skipAttackInteraction(Entity param0) {
        return param0 instanceof Player && !this.level.mayInteract((Player)param0, this.blockPosition());
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public void thunderHit(ServerLevel param0, LightningBolt param1) {
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_CLIENT_FLAGS.equals(param0)) {
            this.refreshDimensions();
            this.blocksBuilding = !this.isMarker();
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return this.getDimensionsMarker(this.isMarker());
    }

    private EntityDimensions getDimensionsMarker(boolean param0) {
        if (param0) {
            return MARKER_DIMENSIONS;
        } else {
            return this.isBaby() ? BABY_DIMENSIONS : this.getType().getDimensions();
        }
    }

    @Override
    public Vec3 getLightProbePosition(float param0) {
        if (this.isMarker()) {
            AABB var0 = this.getDimensionsMarker(false).makeBoundingBox(this.position());
            BlockPos var1 = this.blockPosition();
            int var2 = Integer.MIN_VALUE;

            for(BlockPos var3 : BlockPos.betweenClosed(new BlockPos(var0.minX, var0.minY, var0.minZ), new BlockPos(var0.maxX, var0.maxY, var0.maxZ))) {
                int var4 = Math.max(this.level.getBrightness(LightLayer.BLOCK, var3), this.level.getBrightness(LightLayer.SKY, var3));
                if (var4 == 15) {
                    return Vec3.atCenterOf(var3);
                }

                if (var4 > var2) {
                    var2 = var4;
                    var1 = var3.immutable();
                }
            }

            return Vec3.atCenterOf(var1);
        } else {
            return super.getLightProbePosition(param0);
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.ARMOR_STAND);
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return !this.isInvisible() && !this.isMarker();
    }
}
