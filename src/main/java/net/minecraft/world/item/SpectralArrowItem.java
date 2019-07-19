package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.level.Level;

public class SpectralArrowItem extends ArrowItem {
    public SpectralArrowItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public AbstractArrow createArrow(Level param0, ItemStack param1, LivingEntity param2) {
        return new SpectralArrow(param0, param2);
    }
}
