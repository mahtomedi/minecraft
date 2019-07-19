package net.minecraft.world.item.enchantment;

import net.minecraft.util.WeighedRandom;

public class EnchantmentInstance extends WeighedRandom.WeighedRandomItem {
    public final Enchantment enchantment;
    public final int level;

    public EnchantmentInstance(Enchantment param0, int param1) {
        super(param0.getRarity().getWeight());
        this.enchantment = param0;
        this.level = param1;
    }
}
