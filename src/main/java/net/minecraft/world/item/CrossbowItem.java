package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CrossbowItem extends ProjectileWeaponItem implements Vanishable {
    private static final String TAG_CHARGED = "Charged";
    private static final String TAG_CHARGED_PROJECTILES = "ChargedProjectiles";
    private static final int MAX_CHARGE_DURATION = 25;
    public static final int DEFAULT_RANGE = 8;
    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;
    private static final float START_SOUND_PERCENT = 0.2F;
    private static final float MID_SOUND_PERCENT = 0.5F;
    private static final float ARROW_POWER = 3.15F;
    private static final float FIREWORK_POWER = 1.6F;

    public CrossbowItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return ARROW_OR_FIREWORK;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (isCharged(var0)) {
            performShooting(param0, param1, param2, var0, getShootingPower(var0), 1.0F);
            setCharged(var0, false);
            return InteractionResultHolder.consume(var0);
        } else if (!param1.getProjectile(var0).isEmpty()) {
            if (!isCharged(var0)) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                param1.startUsingItem(param2);
            }

            return InteractionResultHolder.consume(var0);
        } else {
            return InteractionResultHolder.fail(var0);
        }
    }

    private static float getShootingPower(ItemStack param0) {
        return containsChargedProjectile(param0, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    @Override
    public void releaseUsing(ItemStack param0, Level param1, LivingEntity param2, int param3) {
        int var0 = this.getUseDuration(param0) - param3;
        float var1 = getPowerForTime(var0, param0);
        if (var1 >= 1.0F && !isCharged(param0) && tryLoadProjectiles(param2, param0)) {
            setCharged(param0, true);
            SoundSource var2 = param2 instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            param1.playSound(
                null,
                param2.getX(),
                param2.getY(),
                param2.getZ(),
                SoundEvents.CROSSBOW_LOADING_END,
                var2,
                1.0F,
                1.0F / (param1.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F
            );
        }

    }

    private static boolean tryLoadProjectiles(LivingEntity param0, ItemStack param1) {
        int var0 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, param1);
        int var1 = var0 == 0 ? 1 : 3;
        boolean var2 = param0 instanceof Player && ((Player)param0).getAbilities().instabuild;
        ItemStack var3 = param0.getProjectile(param1);
        ItemStack var4 = var3.copy();

        for(int var5 = 0; var5 < var1; ++var5) {
            if (var5 > 0) {
                var3 = var4.copy();
            }

            if (var3.isEmpty() && var2) {
                var3 = new ItemStack(Items.ARROW);
                var4 = var3.copy();
            }

            if (!loadProjectile(param0, param1, var3, var5 > 0, var2)) {
                return false;
            }
        }

        return true;
    }

    private static boolean loadProjectile(LivingEntity param0, ItemStack param1, ItemStack param2, boolean param3, boolean param4) {
        if (param2.isEmpty()) {
            return false;
        } else {
            boolean var0 = param4 && param2.getItem() instanceof ArrowItem;
            ItemStack var1;
            if (!var0 && !param4 && !param3) {
                var1 = param2.split(1);
                if (param2.isEmpty() && param0 instanceof Player) {
                    ((Player)param0).getInventory().removeItem(param2);
                }
            } else {
                var1 = param2.copy();
            }

            addChargedProjectile(param1, var1);
            return true;
        }
    }

    public static boolean isCharged(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null && var0.getBoolean("Charged");
    }

    public static void setCharged(ItemStack param0, boolean param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        var0.putBoolean("Charged", param1);
    }

    private static void addChargedProjectile(ItemStack param0, ItemStack param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        ListTag var1;
        if (var0.contains("ChargedProjectiles", 9)) {
            var1 = var0.getList("ChargedProjectiles", 10);
        } else {
            var1 = new ListTag();
        }

        CompoundTag var3 = new CompoundTag();
        param1.save(var3);
        var1.add(var3);
        var0.put("ChargedProjectiles", var1);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack param0) {
        List<ItemStack> var0 = Lists.newArrayList();
        CompoundTag var1 = param0.getTag();
        if (var1 != null && var1.contains("ChargedProjectiles", 9)) {
            ListTag var2 = var1.getList("ChargedProjectiles", 10);
            if (var2 != null) {
                for(int var3 = 0; var3 < var2.size(); ++var3) {
                    CompoundTag var4 = var2.getCompound(var3);
                    var0.add(ItemStack.of(var4));
                }
            }
        }

        return var0;
    }

    private static void clearChargedProjectiles(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null) {
            ListTag var1 = var0.getList("ChargedProjectiles", 9);
            var1.clear();
            var0.put("ChargedProjectiles", var1);
        }

    }

    public static boolean containsChargedProjectile(ItemStack param0, Item param1) {
        return getChargedProjectiles(param0).stream().anyMatch(param1x -> param1x.is(param1));
    }

    private static void shootProjectile(
        Level param0,
        LivingEntity param1,
        InteractionHand param2,
        ItemStack param3,
        ItemStack param4,
        float param5,
        boolean param6,
        float param7,
        float param8,
        float param9
    ) {
        if (!param0.isClientSide) {
            boolean var0 = param4.is(Items.FIREWORK_ROCKET);
            Projectile var1;
            if (var0) {
                var1 = new FireworkRocketEntity(param0, param4, param1, param1.getX(), param1.getEyeY() - 0.15F, param1.getZ(), true);
            } else {
                var1 = getArrow(param0, param1, param3, param4);
                if (param6 || param9 != 0.0F) {
                    ((AbstractArrow)var1).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            if (param1 instanceof CrossbowAttackMob var3) {
                var3.shootCrossbowProjectile(var3.getTarget(), param3, var1, param9);
            } else {
                Vec3 var4 = param1.getUpVector(1.0F);
                Quaternionf var5 = new Quaternionf().setAngleAxis((double)(param9 * (float) (Math.PI / 180.0)), var4.x, var4.y, var4.z);
                Vec3 var6 = param1.getViewVector(1.0F);
                Vector3f var7 = var6.toVector3f().rotate(var5);
                var1.shoot((double)var7.x(), (double)var7.y(), (double)var7.z(), param7, param8);
            }

            param3.hurtAndBreak(var0 ? 3 : 1, param1, param1x -> param1x.broadcastBreakEvent(param2));
            param0.addFreshEntity(var1);
            param0.playSound(null, param1.getX(), param1.getY(), param1.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, param5);
        }
    }

    private static AbstractArrow getArrow(Level param0, LivingEntity param1, ItemStack param2, ItemStack param3) {
        ArrowItem var0 = (ArrowItem)(param3.getItem() instanceof ArrowItem ? param3.getItem() : Items.ARROW);
        AbstractArrow var1 = var0.createArrow(param0, param3, param1);
        if (param1 instanceof Player) {
            var1.setCritArrow(true);
        }

        var1.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        var1.setShotFromCrossbow(true);
        int var2 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, param2);
        if (var2 > 0) {
            var1.setPierceLevel((byte)var2);
        }

        return var1;
    }

    public static void performShooting(Level param0, LivingEntity param1, InteractionHand param2, ItemStack param3, float param4, float param5) {
        List<ItemStack> var0 = getChargedProjectiles(param3);
        float[] var1 = getShotPitches(param1.getRandom());

        for(int var2 = 0; var2 < var0.size(); ++var2) {
            ItemStack var3 = var0.get(var2);
            boolean var4 = param1 instanceof Player && ((Player)param1).getAbilities().instabuild;
            if (!var3.isEmpty()) {
                if (var2 == 0) {
                    shootProjectile(param0, param1, param2, param3, var3, var1[var2], var4, param4, param5, 0.0F);
                } else if (var2 == 1) {
                    shootProjectile(param0, param1, param2, param3, var3, var1[var2], var4, param4, param5, -10.0F);
                } else if (var2 == 2) {
                    shootProjectile(param0, param1, param2, param3, var3, var1[var2], var4, param4, param5, 10.0F);
                }
            }
        }

        onCrossbowShot(param0, param1, param3);
    }

    private static float[] getShotPitches(RandomSource param0) {
        boolean var0 = param0.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(var0, param0), getRandomShotPitch(!var0, param0)};
    }

    private static float getRandomShotPitch(boolean param0, RandomSource param1) {
        float var0 = param0 ? 0.63F : 0.43F;
        return 1.0F / (param1.nextFloat() * 0.5F + 1.8F) + var0;
    }

    private static void onCrossbowShot(Level param0, LivingEntity param1, ItemStack param2) {
        if (param1 instanceof ServerPlayer var0) {
            if (!param0.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(var0, param2);
            }

            var0.awardStat(Stats.ITEM_USED.get(param2.getItem()));
        }

        clearChargedProjectiles(param2);
    }

    @Override
    public void onUseTick(Level param0, LivingEntity param1, ItemStack param2, int param3) {
        if (!param0.isClientSide) {
            int var0 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, param2);
            SoundEvent var1 = this.getStartSound(var0);
            SoundEvent var2 = var0 == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
            float var3 = (float)(param2.getUseDuration() - param3) / (float)getChargeDuration(param2);
            if (var3 < 0.2F) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (var3 >= 0.2F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                param0.playSound(null, param1.getX(), param1.getY(), param1.getZ(), var1, SoundSource.PLAYERS, 0.5F, 1.0F);
            }

            if (var3 >= 0.5F && var2 != null && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                param0.playSound(null, param1.getX(), param1.getY(), param1.getZ(), var2, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }

    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return getChargeDuration(param0) + 3;
    }

    public static int getChargeDuration(ItemStack param0) {
        int var0 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, param0);
        return var0 == 0 ? 25 : 25 - 5 * var0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.CROSSBOW;
    }

    private SoundEvent getStartSound(int param0) {
        switch(param0) {
            case 1:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_1;
            case 2:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_2;
            case 3:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_3;
            default:
                return SoundEvents.CROSSBOW_LOADING_START;
        }
    }

    private static float getPowerForTime(int param0, ItemStack param1) {
        float var0 = (float)param0 / (float)getChargeDuration(param1);
        if (var0 > 1.0F) {
            var0 = 1.0F;
        }

        return var0;
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        List<ItemStack> var0 = getChargedProjectiles(param0);
        if (isCharged(param0) && !var0.isEmpty()) {
            ItemStack var1 = var0.get(0);
            param2.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(var1.getDisplayName()));
            if (param3.isAdvanced() && var1.is(Items.FIREWORK_ROCKET)) {
                List<Component> var2 = Lists.newArrayList();
                Items.FIREWORK_ROCKET.appendHoverText(var1, param1, var2, param3);
                if (!var2.isEmpty()) {
                    for(int var3 = 0; var3 < var2.size(); ++var3) {
                        var2.set(var3, Component.literal("  ").append(var2.get(var3)).withStyle(ChatFormatting.GRAY));
                    }

                    param2.addAll(var2);
                }
            }

        }
    }

    @Override
    public boolean useOnRelease(ItemStack param0) {
        return param0.is(this);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }
}
