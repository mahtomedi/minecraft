package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ArrowItem extends Item {
    public ArrowItem(Item.Properties param0) {
        super(param0);
    }

    public AbstractArrow createArrow(Level param0, ItemStack param1, LivingEntity param2) {
        Arrow var0 = new Arrow(param0, param2, param1.copyWithCount(1));
        var0.setEffectsFromItem(param1);
        return var0;
    }
}
