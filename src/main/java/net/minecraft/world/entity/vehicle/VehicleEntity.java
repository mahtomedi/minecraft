package net.minecraft.world.entity.vehicle;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class VehicleEntity extends Entity {
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

    public VehicleEntity(EntityType<?> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        } else if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + param1 * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, param0.getEntity());
            boolean var0 = param0.getEntity() instanceof Player && ((Player)param0.getEntity()).getAbilities().instabuild;
            if ((var0 || !(this.getDamage() > 40.0F)) && !this.shouldSourceDestroy(param0)) {
                if (var0) {
                    this.discard();
                }
            } else {
                this.destroy(param0);
            }

            return true;
        }
    }

    boolean shouldSourceDestroy(DamageSource param0) {
        return false;
    }

    public void destroy(Item param0) {
        this.kill();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack var0 = new ItemStack(param0);
            if (this.hasCustomName()) {
                var0.setHoverName(this.getCustomName());
            }

            this.spawnAtLocation(var0);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, 0.0F);
    }

    public void setHurtTime(int param0) {
        this.entityData.set(DATA_ID_HURT, param0);
    }

    public void setHurtDir(int param0) {
        this.entityData.set(DATA_ID_HURTDIR, param0);
    }

    public void setDamage(float param0) {
        this.entityData.set(DATA_ID_DAMAGE, param0);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    protected void destroy(DamageSource param0) {
        this.destroy(this.getDropItem());
    }

    abstract Item getDropItem();
}
