package net.minecraft.world.entity.monster;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ZombieVillager extends Zombie implements VillagerDataHolder {
    private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(
        ZombieVillager.class, EntityDataSerializers.VILLAGER_DATA
    );
    private int villagerConversionTime;
    private UUID conversionStarter;
    private Tag gossips;
    private CompoundTag tradeOffers;
    private int villagerXp;

    public ZombieVillager(EntityType<? extends ZombieVillager> param0, Level param1) {
        super(param0, param1);
        this.setVillagerData(this.getVillagerData().setProfession(Registry.VILLAGER_PROFESSION.getRandom(this.random)));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CONVERTING_ID, false);
        this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        VillagerData.CODEC
            .encodeStart(NbtOps.INSTANCE, this.getVillagerData())
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("VillagerData", param1));
        if (this.tradeOffers != null) {
            param0.put("Offers", this.tradeOffers);
        }

        if (this.gossips != null) {
            param0.put("Gossips", this.gossips);
        }

        param0.putInt("ConversionTime", this.isConverting() ? this.villagerConversionTime : -1);
        if (this.conversionStarter != null) {
            param0.putUUID("ConversionPlayer", this.conversionStarter);
        }

        param0.putInt("Xp", this.villagerXp);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("VillagerData", 10)) {
            DataResult<VillagerData> var0 = VillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, param0.get("VillagerData")));
            var0.resultOrPartial(LOGGER::error).ifPresent(this::setVillagerData);
        }

        if (param0.contains("Offers", 10)) {
            this.tradeOffers = param0.getCompound("Offers");
        }

        if (param0.contains("Gossips", 10)) {
            this.gossips = param0.getList("Gossips", 10);
        }

        if (param0.contains("ConversionTime", 99) && param0.getInt("ConversionTime") > -1) {
            this.startConverting(param0.hasUUID("ConversionPlayer") ? param0.getUUID("ConversionPlayer") : null, param0.getInt("ConversionTime"));
        }

        if (param0.contains("Xp", 3)) {
            this.villagerXp = param0.getInt("Xp");
        }

    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && this.isAlive() && this.isConverting()) {
            int var0 = this.getConversionProgress();
            this.villagerConversionTime -= var0;
            if (this.villagerConversionTime <= 0) {
                this.finishConversion((ServerLevel)this.level);
            }
        }

        super.tick();
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.getItem() == Items.GOLDEN_APPLE) {
            if (this.hasEffect(MobEffects.WEAKNESS)) {
                if (!param0.abilities.instabuild) {
                    var0.shrink(1);
                }

                if (!this.level.isClientSide) {
                    this.startConverting(param0.getUUID(), this.random.nextInt(2401) + 3600);
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME;
            }
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return !this.isConverting() && this.villagerXp == 0;
    }

    public boolean isConverting() {
        return this.getEntityData().get(DATA_CONVERTING_ID);
    }

    private void startConverting(@Nullable UUID param0, int param1) {
        this.conversionStarter = param0;
        this.villagerConversionTime = param1;
        this.getEntityData().set(DATA_CONVERTING_ID, true);
        this.removeEffect(MobEffects.WEAKNESS);
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, param1, Math.min(this.level.getDifficulty().getId() - 1, 0)));
        this.level.broadcastEntityEvent(this, (byte)16);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 16) {
            if (!this.isSilent()) {
                this.level
                    .playLocalSound(
                        this.getX(),
                        this.getEyeY(),
                        this.getZ(),
                        SoundEvents.ZOMBIE_VILLAGER_CURE,
                        this.getSoundSource(),
                        1.0F + this.random.nextFloat(),
                        this.random.nextFloat() * 0.7F + 0.3F,
                        false
                    );
            }

        } else {
            super.handleEntityEvent(param0);
        }
    }

    private void finishConversion(ServerLevel param0) {
        Villager var0 = this.convertTo(EntityType.VILLAGER, false);

        for(EquipmentSlot var1 : EquipmentSlot.values()) {
            ItemStack var2 = this.getItemBySlot(var1);
            if (!var2.isEmpty()) {
                if (EnchantmentHelper.hasBindingCurse(var2)) {
                    var0.setSlot(var1.getIndex() + 300, var2);
                } else {
                    double var3 = (double)this.getEquipmentDropChance(var1);
                    if (var3 > 1.0) {
                        this.spawnAtLocation(var2);
                    }
                }
            }
        }

        var0.setVillagerData(this.getVillagerData());
        if (this.gossips != null) {
            var0.setGossips(this.gossips);
        }

        if (this.tradeOffers != null) {
            var0.setOffers(new MerchantOffers(this.tradeOffers));
        }

        var0.setVillagerXp(this.villagerXp);
        var0.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var0.blockPosition()), MobSpawnType.CONVERSION, null, null);
        if (this.conversionStarter != null) {
            Player var4 = param0.getPlayerByUUID(this.conversionStarter);
            if (var4 instanceof ServerPlayer) {
                CriteriaTriggers.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayer)var4, this, var0);
                param0.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, var4, var0);
            }
        }

        var0.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        if (!this.isSilent()) {
            param0.levelEvent(null, 1027, this.blockPosition(), 0);
        }

    }

    private int getConversionProgress() {
        int var0 = 1;
        if (this.random.nextFloat() < 0.01F) {
            int var1 = 0;
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(int var3 = (int)this.getX() - 4; var3 < (int)this.getX() + 4 && var1 < 14; ++var3) {
                for(int var4 = (int)this.getY() - 4; var4 < (int)this.getY() + 4 && var1 < 14; ++var4) {
                    for(int var5 = (int)this.getZ() - 4; var5 < (int)this.getZ() + 4 && var1 < 14; ++var5) {
                        Block var6 = this.level.getBlockState(var2.set(var3, var4, var5)).getBlock();
                        if (var6 == Blocks.IRON_BARS || var6 instanceof BedBlock) {
                            if (this.random.nextFloat() < 0.3F) {
                                ++var0;
                            }

                            ++var1;
                        }
                    }
                }
            }
        }

        return var0;
    }

    @Override
    protected float getVoicePitch() {
        return this.isBaby()
            ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.0F
            : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ZOMBIE_VILLAGER_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    public SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_VILLAGER_STEP;
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    public void setTradeOffers(CompoundTag param0) {
        this.tradeOffers = param0;
    }

    public void setGossips(Tag param0) {
        this.gossips = param0;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(param0.getBiomeName(this.blockPosition()))));
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public void setVillagerData(VillagerData param0x) {
        VillagerData var0x = this.getVillagerData();
        if (var0x.getProfession() != param0x.getProfession()) {
            this.tradeOffers = null;
        }

        this.entityData.set(DATA_VILLAGER_DATA, param0x);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.entityData.get(DATA_VILLAGER_DATA);
    }

    public void setVillagerXp(int param0) {
        this.villagerXp = param0;
    }
}
