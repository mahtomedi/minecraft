package net.minecraft.world.entity.npc;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WanderingTrader extends AbstractVillager {
    @Nullable
    private BlockPos wanderTarget;
    private int despawnDelay;

    public WanderingTrader(EntityType<? extends WanderingTrader> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector
            .addGoal(
                0,
                new UseItemGoal<>(
                    this,
                    PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY),
                    SoundEvents.WANDERING_TRADER_DISAPPEARED,
                    param0 -> this.level.isNight() && !param0.isInvisible()
                )
            );
        this.goalSelector
            .addGoal(
                0,
                new UseItemGoal<>(
                    this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, param0 -> this.level.isDay() && param0.isInvisible()
                )
            );
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderingTrader.WanderToPositionGoal(this, 2.0, 0.35));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (!var0.is(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (param1 == InteractionHand.MAIN_HAND) {
                param0.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (this.getOffers().isEmpty()) {
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            } else {
                if (!this.level.isClientSide) {
                    this.setTradingPlayer(param0);
                    this.openTradingScreen(param0, this.getDisplayName(), 1);
                }

                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    @Override
    protected void updateTrades() {
        VillagerTrades.ItemListing[] var0 = VillagerTrades.WANDERING_TRADER_TRADES.get(1);
        VillagerTrades.ItemListing[] var1 = VillagerTrades.WANDERING_TRADER_TRADES.get(2);
        if (var0 != null && var1 != null) {
            MerchantOffers var2 = this.getOffers();
            this.addOffersFromItemListings(var2, var0, 5);
            int var3 = this.random.nextInt(var1.length);
            VillagerTrades.ItemListing var4 = var1[var3];
            MerchantOffer var5 = var4.getOffer(this, this.random);
            if (var5 != null) {
                var2.add(var5);
            }

        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("DespawnDelay", this.despawnDelay);
        if (this.wanderTarget != null) {
            param0.put("WanderTarget", NbtUtils.writeBlockPos(this.wanderTarget));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("DespawnDelay", 99)) {
            this.despawnDelay = param0.getInt("DespawnDelay");
        }

        if (param0.contains("WanderTarget")) {
            this.wanderTarget = NbtUtils.readBlockPos(param0.getCompound("WanderTarget"));
        }

        this.setAge(Math.max(0, this.getAge()));
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer param0) {
        if (param0.shouldRewardExp()) {
            int var0 = 3 + this.random.nextInt(4);
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY() + 0.5, this.getZ(), var0));
        }

    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    protected SoundEvent getDrinkingSound(ItemStack param0) {
        return param0.is(Items.MILK_BUCKET) ? SoundEvents.WANDERING_TRADER_DRINK_MILK : SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean param0) {
        return param0 ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public void setDespawnDelay(int param0) {
        this.despawnDelay = param0;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.maybeDespawn();
        }

    }

    private void maybeDespawn() {
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
            this.discard();
        }

    }

    public void setWanderTarget(@Nullable BlockPos param0) {
        this.wanderTarget = param0;
    }

    @Nullable
    private BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    class WanderToPositionGoal extends Goal {
        final WanderingTrader trader;
        final double stopDistance;
        final double speedModifier;

        WanderToPositionGoal(WanderingTrader param0, double param1, double param2) {
            this.trader = param0;
            this.stopDistance = param1;
            this.speedModifier = param2;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void stop() {
            this.trader.setWanderTarget(null);
            WanderingTrader.this.navigation.stop();
        }

        @Override
        public boolean canUse() {
            BlockPos var0 = this.trader.getWanderTarget();
            return var0 != null && this.isTooFarAway(var0, this.stopDistance);
        }

        @Override
        public void tick() {
            BlockPos var0 = this.trader.getWanderTarget();
            if (var0 != null && WanderingTrader.this.navigation.isDone()) {
                if (this.isTooFarAway(var0, 10.0)) {
                    Vec3 var1 = new Vec3(
                            (double)var0.getX() - this.trader.getX(), (double)var0.getY() - this.trader.getY(), (double)var0.getZ() - this.trader.getZ()
                        )
                        .normalize();
                    Vec3 var2 = var1.scale(10.0).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    WanderingTrader.this.navigation.moveTo(var2.x, var2.y, var2.z, this.speedModifier);
                } else {
                    WanderingTrader.this.navigation.moveTo((double)var0.getX(), (double)var0.getY(), (double)var0.getZ(), this.speedModifier);
                }
            }

        }

        private boolean isTooFarAway(BlockPos param0, double param1) {
            return !param0.closerThan(this.trader.position(), param1);
        }
    }
}
