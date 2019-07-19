package net.minecraft.world.entity.npc;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractVillager extends AgableMob implements Npc, Merchant {
    private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
    @Nullable
    private Player tradingPlayer;
    @Nullable
    protected MerchantOffers offers;
    private final SimpleContainer inventory = new SimpleContainer(8);

    public AbstractVillager(EntityType<? extends AbstractVillager> param0, Level param1) {
        super(param0, param1);
    }

    public int getUnhappyCounter() {
        return this.entityData.get(DATA_UNHAPPY_COUNTER);
    }

    public void setUnhappyCounter(int param0) {
        this.entityData.set(DATA_UNHAPPY_COUNTER, param0);
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return this.isBaby() ? 0.81F : 1.62F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
    }

    @Override
    public void setTradingPlayer(@Nullable Player param0) {
        this.tradingPlayer = param0;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.updateTrades();
        }

        return this.offers;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void overrideOffers(@Nullable MerchantOffers param0) {
    }

    @Override
    public void overrideXp(int param0) {
    }

    @Override
    public void notifyTrade(MerchantOffer param0) {
        param0.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.rewardTradeXp(param0);
        if (this.tradingPlayer instanceof ServerPlayer) {
            CriteriaTriggers.TRADE.trigger((ServerPlayer)this.tradingPlayer, this, param0.getResult());
        }

    }

    protected abstract void rewardTradeXp(MerchantOffer var1);

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public void notifyTradeUpdated(ItemStack param0) {
        if (!this.level.isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
            this.playSound(this.getTradeUpdatedSound(!param0.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
        }

    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    protected SoundEvent getTradeUpdatedSound(boolean param0) {
        return param0 ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    public void playCelebrateSound() {
        this.playSound(SoundEvents.VILLAGER_CELEBRATE, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        MerchantOffers var0 = this.getOffers();
        if (!var0.isEmpty()) {
            param0.put("Offers", var0.createTag());
        }

        ListTag var1 = new ListTag();

        for(int var2 = 0; var2 < this.inventory.getContainerSize(); ++var2) {
            ItemStack var3 = this.inventory.getItem(var2);
            if (!var3.isEmpty()) {
                var1.add(var3.save(new CompoundTag()));
            }
        }

        param0.put("Inventory", var1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("Offers", 10)) {
            this.offers = new MerchantOffers(param0.getCompound("Offers"));
        }

        ListTag var0 = param0.getList("Inventory", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            ItemStack var2 = ItemStack.of(var0.getCompound(var1));
            if (!var2.isEmpty()) {
                this.inventory.addItem(var2);
            }
        }

    }

    @Nullable
    @Override
    public Entity changeDimension(DimensionType param0) {
        this.stopTrading();
        return super.changeDimension(param0);
    }

    protected void stopTrading() {
        this.setTradingPlayer(null);
    }

    @Override
    public void die(DamageSource param0) {
        super.die(param0);
        this.stopTrading();
    }

    @OnlyIn(Dist.CLIENT)
    protected void addParticlesAroundSelf(ParticleOptions param0) {
        for(int var0 = 0; var0 < 5; ++var0) {
            double var1 = this.random.nextGaussian() * 0.02;
            double var2 = this.random.nextGaussian() * 0.02;
            double var3 = this.random.nextGaussian() * 0.02;
            this.level
                .addParticle(
                    param0,
                    this.x + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                    this.y + 1.0 + (double)(this.random.nextFloat() * this.getBbHeight()),
                    this.z + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                    var1,
                    var2,
                    var3
                );
        }

    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return false;
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    public boolean setSlot(int param0, ItemStack param1) {
        if (super.setSlot(param0, param1)) {
            return true;
        } else {
            int var0 = param0 - 300;
            if (var0 >= 0 && var0 < this.inventory.getContainerSize()) {
                this.inventory.setItem(var0, param1);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    protected abstract void updateTrades();

    protected void addOffersFromItemListings(MerchantOffers param0, VillagerTrades.ItemListing[] param1, int param2) {
        Set<Integer> var0 = Sets.newHashSet();
        if (param1.length > param2) {
            while(var0.size() < param2) {
                var0.add(this.random.nextInt(param1.length));
            }
        } else {
            for(int var1 = 0; var1 < param1.length; ++var1) {
                var0.add(var1);
            }
        }

        for(Integer var2 : var0) {
            VillagerTrades.ItemListing var3 = param1[var2];
            MerchantOffer var4 = var3.getOffer(this, this.random);
            if (var4 != null) {
                param0.add(var4);
            }
        }

    }
}
