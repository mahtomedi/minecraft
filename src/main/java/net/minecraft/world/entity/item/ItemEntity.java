package net.minecraft.world.entity.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private int age;
    private int pickupDelay;
    private int health = 5;
    private UUID thrower;
    private UUID owner;
    public final float bobOffs = (float)(Math.random() * Math.PI * 2.0);

    public ItemEntity(EntityType<? extends ItemEntity> param0, Level param1) {
        super(param0, param1);
    }

    public ItemEntity(Level param0, double param1, double param2, double param3) {
        this(EntityType.ITEM, param0);
        this.setPos(param1, param2, param3);
        this.yRot = this.random.nextFloat() * 360.0F;
        this.setDeltaMovement(this.random.nextDouble() * 0.2 - 0.1, 0.2, this.random.nextDouble() * 0.2 - 0.1);
    }

    public ItemEntity(Level param0, double param1, double param2, double param3, ItemStack param4) {
        this(param0, param1, param2, param3);
        this.setItem(param4);
    }

    @Override
    protected boolean makeStepSound() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        if (this.getItem().isEmpty()) {
            this.remove();
        } else {
            super.tick();
            if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
                --this.pickupDelay;
            }

            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            Vec3 var0 = this.getDeltaMovement();
            if (this.isUnderLiquid(FluidTags.WATER)) {
                this.setUnderwaterMovement();
            } else if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            }

            if (this.level.isClientSide) {
                this.noPhysics = false;
            } else {
                this.noPhysics = !this.level.noCollision(this);
                if (this.noPhysics) {
                    this.checkInBlock(this.x, (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.z);
                }
            }

            if (!this.onGround || getHorizontalDistanceSqr(this.getDeltaMovement()) > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                float var1 = 0.98F;
                if (this.onGround) {
                    var1 = this.level.getBlockState(new BlockPos(this.x, this.getBoundingBox().minY - 1.0, this.z)).getBlock().getFriction() * 0.98F;
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply((double)var1, 0.98, (double)var1));
                if (this.onGround) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.5, 1.0));
                }
            }

            boolean var2 = Mth.floor(this.xo) != Mth.floor(this.x) || Mth.floor(this.yo) != Mth.floor(this.y) || Mth.floor(this.zo) != Mth.floor(this.z);
            int var3 = var2 ? 2 : 40;
            if (this.tickCount % var3 == 0) {
                if (this.level.getFluidState(new BlockPos(this)).is(FluidTags.LAVA)) {
                    this.setDeltaMovement(
                        (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F),
                        0.2F,
                        (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F)
                    );
                    this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
                }

                if (!this.level.isClientSide && this.isMergable()) {
                    this.mergeWithNeighbours();
                }
            }

            if (this.age != -32768) {
                ++this.age;
            }

            this.hasImpulse |= this.updateInWaterState();
            if (!this.level.isClientSide) {
                double var4 = this.getDeltaMovement().subtract(var0).lengthSqr();
                if (var4 > 0.01) {
                    this.hasImpulse = true;
                }
            }

            if (!this.level.isClientSide && this.age >= 6000) {
                this.remove();
            }

        }
    }

    private void setUnderwaterMovement() {
        Vec3 var0 = this.getDeltaMovement();
        this.setDeltaMovement(var0.x * 0.99F, var0.y + (double)(var0.y < 0.06F ? 5.0E-4F : 0.0F), var0.z * 0.99F);
    }

    private void mergeWithNeighbours() {
        List<ItemEntity> var0 = this.level
            .getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), param0 -> param0 != this && param0.isMergable());
        if (!var0.isEmpty()) {
            for(ItemEntity var1 : var0) {
                if (!this.isMergable()) {
                    return;
                }

                this.merge(var1);
            }
        }

    }

    private boolean isMergable() {
        ItemStack var0 = this.getItem();
        return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && var0.getCount() < var0.getMaxStackSize();
    }

    private void merge(ItemEntity param0) {
        ItemStack var0 = this.getItem();
        ItemStack var1 = param0.getItem();
        if (var1.getItem() == var0.getItem()) {
            if (var1.getCount() + var0.getCount() <= var1.getMaxStackSize()) {
                if (!(var1.hasTag() ^ var0.hasTag())) {
                    if (!var1.hasTag() || var1.getTag().equals(var0.getTag())) {
                        if (var1.getCount() < var0.getCount()) {
                            merge(this, var0, param0, var1);
                        } else {
                            merge(param0, var1, this, var0);
                        }

                    }
                }
            }
        }
    }

    private static void merge(ItemEntity param0, ItemStack param1, ItemEntity param2, ItemStack param3) {
        int var0 = Math.min(param1.getMaxStackSize() - param1.getCount(), param3.getCount());
        ItemStack var1 = param1.copy();
        var1.grow(var0);
        param0.setItem(var1);
        param3.shrink(var0);
        param2.setItem(param3);
        param0.pickupDelay = Math.max(param0.pickupDelay, param2.pickupDelay);
        param0.age = Math.min(param0.age, param2.age);
        if (param3.isEmpty()) {
            param2.remove();
        }

    }

    public void setShortLifeTime() {
        this.age = 4800;
    }

    @Override
    protected void burn(int param0) {
        this.hurt(DamageSource.IN_FIRE, (float)param0);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (!this.getItem().isEmpty() && this.getItem().getItem() == Items.NETHER_STAR && param0.isExplosion()) {
            return false;
        } else {
            this.markHurt();
            this.health = (int)((float)this.health - param1);
            if (this.health <= 0) {
                this.remove();
            }

            return false;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putShort("Health", (short)this.health);
        param0.putShort("Age", (short)this.age);
        param0.putShort("PickupDelay", (short)this.pickupDelay);
        if (this.getThrower() != null) {
            param0.put("Thrower", NbtUtils.createUUIDTag(this.getThrower()));
        }

        if (this.getOwner() != null) {
            param0.put("Owner", NbtUtils.createUUIDTag(this.getOwner()));
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

        if (param0.contains("Owner", 10)) {
            this.owner = NbtUtils.loadUUIDTag(param0.getCompound("Owner"));
        }

        if (param0.contains("Thrower", 10)) {
            this.thrower = NbtUtils.loadUUIDTag(param0.getCompound("Thrower"));
        }

        CompoundTag var0 = param0.getCompound("Item");
        this.setItem(ItemStack.of(var0));
        if (this.getItem().isEmpty()) {
            this.remove();
        }

    }

    @Override
    public void playerTouch(Player param0) {
        if (!this.level.isClientSide) {
            ItemStack var0 = this.getItem();
            Item var1 = var0.getItem();
            int var2 = var0.getCount();
            if (this.pickupDelay == 0 && (this.owner == null || 6000 - this.age <= 200 || this.owner.equals(param0.getUUID())) && param0.inventory.add(var0)) {
                param0.take(this, var2);
                if (var0.isEmpty()) {
                    this.remove();
                    var0.setCount(var2);
                }

                param0.awardStat(Stats.ITEM_PICKED_UP.get(var1), var2);
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
    public Entity changeDimension(DimensionType param0) {
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

    @OnlyIn(Dist.CLIENT)
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

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setNeverPickUp();
        this.age = 5999;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
