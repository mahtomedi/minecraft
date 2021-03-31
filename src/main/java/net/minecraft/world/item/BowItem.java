package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class BowItem extends ProjectileWeaponItem implements Vanishable {
    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public BowItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public void releaseUsing(ItemStack param0, Level param1, LivingEntity param2, int param3) {
        if (param2 instanceof Player) {
            Player var0 = (Player)param2;
            boolean var1 = var0.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, param0) > 0;
            ItemStack var2 = var0.getProjectile(param0);
            if (!var2.isEmpty() || var1) {
                if (var2.isEmpty()) {
                    var2 = new ItemStack(Items.ARROW);
                }

                int var3 = this.getUseDuration(param0) - param3;
                float var4 = getPowerForTime(var3);
                if (!((double)var4 < 0.1)) {
                    boolean var5 = var1 && var2.is(Items.ARROW);
                    if (!param1.isClientSide) {
                        ArrowItem var6 = (ArrowItem)(var2.getItem() instanceof ArrowItem ? var2.getItem() : Items.ARROW);
                        AbstractArrow var7 = var6.createArrow(param1, var2, var0);
                        var7.shootFromRotation(var0, var0.xRot, var0.yRot, 0.0F, var4 * 3.0F, 1.0F);
                        if (var4 == 1.0F) {
                            var7.setCritArrow(true);
                        }

                        int var8 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, param0);
                        if (var8 > 0) {
                            var7.setBaseDamage(var7.getBaseDamage() + (double)var8 * 0.5 + 0.5);
                        }

                        int var9 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, param0);
                        if (var9 > 0) {
                            var7.setKnockback(var9);
                        }

                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, param0) > 0) {
                            var7.setSecondsOnFire(100);
                        }

                        param0.hurtAndBreak(1, var0, param1x -> param1x.broadcastBreakEvent(var0.getUsedItemHand()));
                        if (var5 || var0.getAbilities().instabuild && (var2.is(Items.SPECTRAL_ARROW) || var2.is(Items.TIPPED_ARROW))) {
                            var7.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        param1.addFreshEntity(var7);
                    }

                    param1.playSound(
                        null,
                        var0.getX(),
                        var0.getY(),
                        var0.getZ(),
                        SoundEvents.ARROW_SHOOT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F / (param1.getRandom().nextFloat() * 0.4F + 1.2F) + var4 * 0.5F
                    );
                    if (!var5 && !var0.getAbilities().instabuild) {
                        var2.shrink(1);
                        if (var2.isEmpty()) {
                            var0.getInventory().removeItem(var2);
                        }
                    }

                    var0.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public static float getPowerForTime(int param0) {
        float var0 = (float)param0 / 20.0F;
        var0 = (var0 * var0 + var0 * 2.0F) / 3.0F;
        if (var0 > 1.0F) {
            var0 = 1.0F;
        }

        return var0;
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        boolean var1 = !param1.getProjectile(var0).isEmpty();
        if (!param1.getAbilities().instabuild && !var1) {
            return InteractionResultHolder.fail(var0);
        } else {
            param1.startUsingItem(param2);
            return InteractionResultHolder.consume(var0);
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }
}
