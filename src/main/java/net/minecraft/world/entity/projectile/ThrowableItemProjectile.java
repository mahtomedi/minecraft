package net.minecraft.world.entity.projectile;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
    value = Dist.CLIENT,
    _interface = ItemSupplier.class
)
public abstract class ThrowableItemProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(
        ThrowableItemProjectile.class, EntityDataSerializers.ITEM_STACK
    );

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> param0, Level param1) {
        super(param0, param1);
    }

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> param0, double param1, double param2, double param3, Level param4) {
        super(param0, param1, param2, param3, param4);
    }

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> param0, LivingEntity param1, Level param2) {
        super(param0, param1, param2);
    }

    public void setItem(ItemStack param0) {
        if (!param0.is(this.getDefaultItem()) || param0.hasTag()) {
            this.getEntityData().set(DATA_ITEM_STACK, Util.make(param0.copy(), param0x -> param0x.setCount(1)));
        }

    }

    protected abstract Item getDefaultItem();

    protected ItemStack getItemRaw() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    public ItemStack getItem() {
        ItemStack var0 = this.getItemRaw();
        return var0.isEmpty() ? new ItemStack(this.getDefaultItem()) : var0;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        ItemStack var0 = this.getItemRaw();
        if (!var0.isEmpty()) {
            param0.put("Item", var0.save(new CompoundTag()));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        ItemStack var0 = ItemStack.of(param0.getCompound("Item"));
        this.setItem(var0);
    }
}
