package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BowlFoodItem extends Item {
    public BowlFoodItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        super.finishUsingItem(param0, param1, param2);
        return new ItemStack(Items.BOWL);
    }
}
