package net.minecraft.world.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ItemEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final int LIFETIME = 6000;
    private static final int INFINITE_PICKUP_DELAY = 32767;
    private static final int INFINITE_LIFETIME = -32768;
    private int age;
    private int pickupDelay;
    private int health = 5;
    @Nullable
    private UUID thrower;
    @Nullable
    private UUID owner;
    public final float bobOffs;

    public ItemEntity(EntityType<? extends ItemEntity> param0, Level param1) {
        super(param0, param1);
        this.bobOffs = this.random.nextFloat() * (float) Math.PI * 2.0F;
        this.setYRot(this.random.nextFloat() * 360.0F);
    }

    public ItemEntity(Level param0, double param1, double param2, double param3, ItemStack param4) {
        this(param0, param1, param2, param3, param4, param0.random.nextDouble() * 0.2 - 0.1, 0.2, param0.random.nextDouble() * 0.2 - 0.1);
    }

    public ItemEntity(Level param0, double param1, double param2, double param3, ItemStack param4, double param5, double param6, double param7) {
        this(EntityType.ITEM, param0);
        this.setPos(param1, param2, param3);
        this.setDeltaMovement(param5, param6, param7);
        this.setItem(param4);
    }

    private ItemEntity(ItemEntity param0) {
        super(param0.getType(), param0.level);
        this.setItem(param0.getItem().copy());
        this.copyPosition(param0);
        this.age = param0.age;
        this.bobOffs = param0.bobOffs;
    }

    @Override
    public boolean occludesVibrations() {
        return this.getItem().is(ItemTags.OCCLUDES_VIBRATION_SIGNALS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        if (this.getItem().isEmpty()) {
            this.discard();
        } else {
            super.tick();
            if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
                --this.pickupDelay;
            }

            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Vec3 var0 = this.getDeltaMovement();
            float var1 = this.getEyeHeight() - 0.11111111F;
            if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)var1) {
                this.setUnderwaterMovement();
            } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)var1) {
                this.setUnderLavaMovement();
            } else if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            }

            if (this.level.isClientSide) {
                this.noPhysics = false;
            } else {
                this.noPhysics = !this.level.noCollision(this, this.getBoundingBox().deflate(1.0E-7));
                if (this.noPhysics) {
                    this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
                }
            }

            if (!this.onGround || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                float var2 = 0.98F;
                if (this.onGround) {
                    var2 = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.98F;
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply((double)var2, 0.98, (double)var2));
                if (this.onGround) {
                    Vec3 var3 = this.getDeltaMovement();
                    if (var3.y < 0.0) {
                        this.setDeltaMovement(var3.multiply(1.0, -0.5, 1.0));
                    }
                }
            }

            boolean var4 = Mth.floor(this.xo) != Mth.floor(this.getX())
                || Mth.floor(this.yo) != Mth.floor(this.getY())
                || Mth.floor(this.zo) != Mth.floor(this.getZ());
            int var5 = var4 ? 2 : 40;
            if (this.tickCount % var5 == 0 && !this.level.isClientSide && this.isMergable()) {
                this.mergeWithNeighbours();
            }

            if (this.age != -32768) {
                ++this.age;
            }

            this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
            if (!this.level.isClientSide) {
                double var6 = this.getDeltaMovement().subtract(var0).lengthSqr();
                if (var6 > 0.01) {
                    this.hasImpulse = true;
                }
            }

            if (!this.level.isClientSide && this.age >= 6000) {
                this.discard();
            }

        }
    }

    private void setUnderwaterMovement() {
        Vec3 var0 = this.getDeltaMovement();
        this.setDeltaMovement(var0.x * 0.99F, var0.y + (double)(var0.y < 0.06F ? 5.0E-4F : 0.0F), var0.z * 0.99F);
    }

    private void setUnderLavaMovement() {
        Vec3 var0 = this.getDeltaMovement();
        this.setDeltaMovement(var0.x * 0.95F, var0.y + (double)(var0.y < 0.06F ? 5.0E-4F : 0.0F), var0.z * 0.95F);
    }

    private void mergeWithNeighbours() {
        if (this.isMergable()) {
            for(ItemEntity var1 : this.level
                .getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), param0 -> param0 != this && param0.isMergable())) {
                if (var1.isMergable()) {
                    this.tryToMerge(var1);
                    if (this.isRemoved()) {
                        break;
                    }
                }
            }

        }
    }

    private boolean isMergable() {
        ItemStack var0 = this.getItem();
        return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && var0.getCount() < var0.getMaxStackSize();
    }

    private void tryToMerge(ItemEntity param0) {
        ItemStack var0 = this.getItem();
        ItemStack var1 = param0.getItem();
        if (Objects.equals(this.getOwner(), param0.getOwner()) && areMergable(var0, var1)) {
            if (var1.getCount() < var0.getCount()) {
                merge(this, var0, param0, var1);
            } else {
                merge(param0, var1, this, var0);
            }

        }
    }

    public static boolean areMergable(ItemStack param0, ItemStack param1) {
        if (!param1.is(param0.getItem())) {
            return false;
        } else if (param1.getCount() + param0.getCount() > param1.getMaxStackSize()) {
            return false;
        } else if (param1.hasTag() ^ param0.hasTag()) {
            return false;
        } else {
            return !param1.hasTag() || param1.getTag().equals(param0.getTag());
        }
    }

    public static ItemStack merge(ItemStack param0, ItemStack param1, int param2) {
        int var0 = Math.min(Math.min(param0.getMaxStackSize(), param2) - param0.getCount(), param1.getCount());
        ItemStack var1 = param0.copy();
        var1.grow(var0);
        param1.shrink(var0);
        return var1;
    }

    private static void merge(ItemEntity param0, ItemStack param1, ItemStack param2) {
        ItemStack var0 = merge(param1, param2, 64);
        param0.setItem(var0);
    }

    private static void merge(ItemEntity param0, ItemStack param1, ItemEntity param2, ItemStack param3) {
        merge(param0, param1, param3);
        param0.pickupDelay = Math.max(param0.pickupDelay, param2.pickupDelay);
        param0.age = Math.min(param0.age, param2.age);
        if (param3.isEmpty()) {
            param2.discard();
        }

    }

    @Override
    public boolean fireImmune() {
        return this.getItem().getItem().isFireResistant() || super.fireImmune();
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (!this.getItem().isEmpty() && this.getItem().is(Items.NETHER_STAR) && param0.isExplosion()) {
            return false;
        } else if (!this.getItem().getItem().canBeHurtBy(param0)) {
            return false;
        } else if (this.level.isClientSide) {
            return true;
        } else {
            this.markHurt();
            this.health = (int)((float)this.health - param1);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, param0.getEntity());
            if (this.health <= 0) {
                this.getItem().onDestroyed(this);
                this.discard();
            }

            return true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putShort("Health", (short)this.health);
        param0.putShort("Age", (short)this.age);
        param0.putShort("PickupDelay", (short)this.pickupDelay);
        if (this.getThrower() != null) {
            param0.putUUID("Thrower", this.getThrower());
        }

        if (this.getOwner() != null) {
            param0.putUUID("Owner", this.getOwner());
        }

        if (!this.getItem().isEmpty()) {
            param0.put("Item", this.getItem().save(new CompoundTag()));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.health = param0.getShort("Health");
        this.age = param0.getShort("Age");
        if (param0.contains("PickupDelay")) {
            this.pickupDelay = param0.getShort("PickupDelay");
        }

        if (param0.hasUUID("Owner")) {
            this.owner = param0.getUUID("Owner");
        }

        if (param0.hasUUID("Thrower")) {
            this.thrower = param0.getUUID("Thrower");
        }

        CompoundTag var0 = param0.getCompound("Item");
        this.setItem(ItemStack.of(var0));
        if (this.getItem().isEmpty()) {
            this.discard();
        }

    }

    @Override
    public void playerTouch(Player param0) {
        if (!this.level.isClientSide) {
            ItemStack var0 = this.getItem();
            Item var1 = var0.getItem();
            int var2 = var0.getCount();
            if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(param0.getUUID())) && param0.getInventory().add(var0)) {
                param0.take(this, var2);
                if (var0.isEmpty()) {
                    this.discard();
                    var0.setCount(var2);
                }

                param0.awardStat(Stats.ITEM_PICKED_UP.get(var1), var2);
                param0.onItemPickup(this);
            }

        }
    }

    @Override
    public Component getName() {
        Component var0 = this.getCustomName();
        return (Component)(var0 != null ? var0 : new TranslatableComponent(this.getItem().getDescriptionId()));
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel param0) {
        Entity var0 = super.changeDimension(param0);
        if (!this.level.isClientSide && var0 instanceof ItemEntity) {
            ((ItemEntity)var0).mergeWithNeighbours();
        }

        return var0;
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack param0) {
        this.getEntityData().set(DATA_ITEM, param0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_ITEM.equals(param0)) {
            this.getItem().setEntityRepresentation(this);
        }

    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID param0) {
        this.owner = param0;
    }

    @Nullable
    public UUID getThrower() {
        return this.thrower;
    }

    public void setThrower(@Nullable UUID param0) {
        this.thrower = param0;
    }

    public int getAge() {
        return this.age;
    }

    public void setDefaultPickUpDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay() {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp() {
        this.pickupDelay = 32767;
    }

    public void setPickUpDelay(int param0) {
        this.pickupDelay = param0;
    }

    public boolean hasPickUpDelay() {
        return this.pickupDelay > 0;
    }

    public void setUnlimitedLifetime() {
        this.age = -32768;
    }

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setNeverPickUp();
        this.age = 5999;
    }

    public float getSpin(float param0) {
        return ((float)this.getAge() + param0) / 20.0F + this.bobOffs;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public ItemEntity copy() {
        return new ItemEntity(this);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 180.0F - this.getSpin(0.5F) / (float) (Math.PI * 2) * 360.0F;
    }
}
