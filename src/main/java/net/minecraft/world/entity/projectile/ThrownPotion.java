package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(
    value = Dist.CLIENT,
    _interface = ItemSupplier.class
)
public class ThrownPotion extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrownPotion.class, EntityDataSerializers.ITEM_STACK);
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Predicate<LivingEntity> WATER_SENSITIVE = ThrownPotion::isWaterSensitiveEntity;

    public ThrownPotion(EntityType<? extends ThrownPotion> param0, Level param1) {
        super(param0, param1);
    }

    public ThrownPotion(Level param0, LivingEntity param1) {
        super(EntityType.POTION, param1, param0);
    }

    public ThrownPotion(Level param0, double param1, double param2, double param3) {
        super(EntityType.POTION, param1, param2, param3, param0);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItem() {
        ItemStack var0 = this.getEntityData().get(DATA_ITEM_STACK);
        if (var0.getItem() != Items.SPLASH_POTION && var0.getItem() != Items.LINGERING_POTION) {
            if (this.level != null) {
                LOGGER.error("ThrownPotion entity {} has no item?!", this.getId());
            }

            return new ItemStack(Items.SPLASH_POTION);
        } else {
            return var0;
        }
    }

    public void setItem(ItemStack param0) {
        this.getEntityData().set(DATA_ITEM_STACK, param0.copy());
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(HitResult param0) {
        if (!this.level.isClientSide) {
            ItemStack var0 = this.getItem();
            Potion var1 = PotionUtils.getPotion(var0);
            List<MobEffectInstance> var2 = PotionUtils.getMobEffects(var0);
            boolean var3 = var1 == Potions.WATER && var2.isEmpty();
            if (param0.getType() == HitResult.Type.BLOCK) {
                BlockHitResult var4 = (BlockHitResult)param0;
                Direction var5 = var4.getDirection();
                BlockPos var6 = var4.getBlockPos();
                BlockPos var7 = var6.relative(var5);
                BlockState var8 = this.level.getBlockState(var6);
                var8.onProjectileHit(this.level, var8, var4, this);
                if (var3) {
                    this.dowseFire(var7, var5);
                    this.dowseFire(var7.relative(var5.getOpposite()), var5);

                    for(Direction var9 : Direction.Plane.HORIZONTAL) {
                        this.dowseFire(var7.relative(var9), var9);
                    }
                }
            }

            if (var3) {
                this.applyWater();
            } else if (!var2.isEmpty()) {
                if (this.isLingering()) {
                    this.makeAreaOfEffectCloud(var0, var1);
                } else {
                    this.applySplash(var2, param0.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)param0).getEntity() : null);
                }
            }

            int var10 = var1.hasInstantEffects() ? 2007 : 2002;
            this.level.levelEvent(var10, new BlockPos(this), PotionUtils.getColor(var0));
            this.remove();
        }
    }

    private void applyWater() {
        AABB var0 = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> var1 = this.level.getEntitiesOfClass(LivingEntity.class, var0, WATER_SENSITIVE);
        if (!var1.isEmpty()) {
            for(LivingEntity var2 : var1) {
                double var3 = this.distanceToSqr(var2);
                if (var3 < 16.0 && isWaterSensitiveEntity(var2)) {
                    var2.hurt(DamageSource.indirectMagic(var2, this.getOwner()), 1.0F);
                }
            }
        }

    }

    private void applySplash(List<MobEffectInstance> param0, @Nullable Entity param1) {
        AABB var0 = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> var1 = this.level.getEntitiesOfClass(LivingEntity.class, var0);
        if (!var1.isEmpty()) {
            for(LivingEntity var2 : var1) {
                if (var2.isAffectedByPotions()) {
                    double var3 = this.distanceToSqr(var2);
                    if (var3 < 16.0) {
                        double var4 = 1.0 - Math.sqrt(var3) / 4.0;
                        if (var2 == param1) {
                            var4 = 1.0;
                        }

                        for(MobEffectInstance var5 : param0) {
                            MobEffect var6 = var5.getEffect();
                            if (var6.isInstantenous()) {
                                var6.applyInstantenousEffect(this, this.getOwner(), var2, var5.getAmplifier(), var4);
                            } else {
                                int var7 = (int)(var4 * (double)var5.getDuration() + 0.5);
                                if (var7 > 20) {
                                    var2.addEffect(new MobEffectInstance(var6, var7, var5.getAmplifier(), var5.isAmbient(), var5.isVisible()));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void makeAreaOfEffectCloud(ItemStack param0, Potion param1) {
        AreaEffectCloud var0 = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
        var0.setOwner(this.getOwner());
        var0.setRadius(3.0F);
        var0.setRadiusOnUse(-0.5F);
        var0.setWaitTime(10);
        var0.setRadiusPerTick(-var0.getRadius() / (float)var0.getDuration());
        var0.setPotion(param1);

        for(MobEffectInstance var1 : PotionUtils.getCustomEffects(param0)) {
            var0.addEffect(new MobEffectInstance(var1));
        }

        CompoundTag var2 = param0.getTag();
        if (var2 != null && var2.contains("CustomPotionColor", 99)) {
            var0.setFixedColor(var2.getInt("CustomPotionColor"));
        }

        this.level.addFreshEntity(var0);
    }

    private boolean isLingering() {
        return this.getItem().getItem() == Items.LINGERING_POTION;
    }

    private void dowseFire(BlockPos param0, Direction param1) {
        BlockState var0 = this.level.getBlockState(param0);
        Block var1 = var0.getBlock();
        if (var0.is(BlockTags.FIRE)) {
            this.level.extinguishFire(null, param0.relative(param1), param1.getOpposite());
        } else if (var1 == Blocks.CAMPFIRE && var0.getValue(CampfireBlock.LIT)) {
            this.level.levelEvent(null, 1009, param0, 0);
            this.level.setBlockAndUpdate(param0, var0.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        ItemStack var0 = ItemStack.of(param0.getCompound("Potion"));
        if (var0.isEmpty()) {
            this.remove();
        } else {
            this.setItem(var0);
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        ItemStack var0 = this.getItem();
        if (!var0.isEmpty()) {
            param0.put("Potion", var0.save(new CompoundTag()));
        }

    }

    private static boolean isWaterSensitiveEntity(LivingEntity param0) {
        return param0 instanceof EnderMan || param0 instanceof Blaze;
    }
}
