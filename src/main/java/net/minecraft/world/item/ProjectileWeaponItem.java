package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public abstract class ProjectileWeaponItem extends Item {
    public static final Predicate<ItemStack> ARROW_ONLY = param0 -> param0.is(ItemTags.ARROWS);
    public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or(param0 -> param0.is(Items.FIREWORK_ROCKET));

    public ProjectileWeaponItem(Item.Properties param0) {
        super(param0);
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return this.getAllSupportedProjectiles();
    }

    public abstract Predicate<ItemStack> getAllSupportedProjectiles();

    public static ItemStack getHeldProjectile(LivingEntity param0, Predicate<ItemStack> param1) {
        if (param1.test(param0.getItemInHand(InteractionHand.OFF_HAND))) {
            return param0.getItemInHand(InteractionHand.OFF_HAND);
        } else {
            return param1.test(param0.getItemInHand(InteractionHand.MAIN_HAND)) ? param0.getItemInHand(InteractionHand.MAIN_HAND) : ItemStack.EMPTY;
        }
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    public abstract int getDefaultProjectileRange();
}
