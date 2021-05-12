package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownTrident extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BOOLEAN);
    private ItemStack tridentItem = new ItemStack(Items.TRIDENT);
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public ThrownTrident(EntityType<? extends ThrownTrident> param0, Level param1) {
        super(param0, param1);
    }

    public ThrownTrident(Level param0, LivingEntity param1, ItemStack param2) {
        super(EntityType.TRIDENT, param1, param0);
        this.tridentItem = param2.copy();
        this.entityData.set(ID_LOYALTY, (byte)EnchantmentHelper.getLoyalty(param2));
        this.entityData.set(ID_FOIL, param2.hasFoil());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte)0);
        this.entityData.define(ID_FOIL, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity var0 = this.getOwner();
        int var1 = this.entityData.get(ID_LOYALTY);
        if (var1 > 0 && (this.dealtDamage || this.isNoPhysics()) && var0 != null) {
            if (!this.isAcceptibleReturnOwner()) {
                if (!this.level.isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 var2 = var0.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + var2.y * 0.015 * (double)var1, this.getZ());
                if (this.level.isClientSide) {
                    this.yOld = this.getY();
                }

                double var3 = 0.05 * (double)var1;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(var2.normalize().scale(var3)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.clientSideReturnTridentTickCount;
            }
        }

        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity var0 = this.getOwner();
        if (var0 == null || !var0.isAlive()) {
            return false;
        } else {
            return !(var0 instanceof ServerPlayer) || !var0.isSpectator();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.tridentItem.copy();
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 param0, Vec3 param1) {
        return this.dealtDamage ? null : super.findHitEntity(param0, param1);
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        Entity var0 = param0.getEntity();
        float var1 = 8.0F;
        if (var0 instanceof LivingEntity var2) {
            var1 += EnchantmentHelper.getDamageBonus(this.tridentItem, var2.getMobType());
        }

        Entity var3 = this.getOwner();
        DamageSource var4 = DamageSource.trident(this, (Entity)(var3 == null ? this : var3));
        this.dealtDamage = true;
        SoundEvent var5 = SoundEvents.TRIDENT_HIT;
        if (var0.hurt(var4, var1)) {
            if (var0.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (var0 instanceof LivingEntity var6) {
                if (var3 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(var6, var3);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)var3, var6);
                }

                this.doPostHurtEffects(var6);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        float var7 = 1.0F;
        if (this.level instanceof ServerLevel && this.level.isThundering() && this.isChanneling()) {
            BlockPos var8 = var0.blockPosition();
            if (this.level.canSeeSky(var8)) {
                LightningBolt var9 = EntityType.LIGHTNING_BOLT.create(this.level);
                var9.moveTo(Vec3.atBottomCenterOf(var8));
                var9.setCause(var3 instanceof ServerPlayer ? (ServerPlayer)var3 : null);
                this.level.addFreshEntity(var9);
                var5 = SoundEvents.TRIDENT_THUNDER;
                var7 = 5.0F;
            }
        }

        this.playSound(var5, var7, 1.0F);
    }

    public boolean isChanneling() {
        return EnchantmentHelper.hasChanneling(this.tridentItem);
    }

    @Override
    protected boolean tryPickup(Player param0) {
        return super.tryPickup(param0) || this.isNoPhysics() && this.ownedBy(param0) && param0.getInventory().add(this.getPickupItem());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player param0) {
        if (this.ownedBy(param0) || this.getOwner() == null) {
            super.playerTouch(param0);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("Trident", 10)) {
            this.tridentItem = ItemStack.of(param0.getCompound("Trident"));
        }

        this.dealtDamage = param0.getBoolean("DealtDamage");
        this.entityData.set(ID_LOYALTY, (byte)EnchantmentHelper.getLoyalty(this.tridentItem));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.put("Trident", this.tridentItem.save(new CompoundTag()));
        param0.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public void tickDespawn() {
        int var0 = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || var0 <= 0) {
            super.tickDespawn();
        }

    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double param0, double param1, double param2) {
        return true;
    }
}
