package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CarrotOnAStickItem extends Item {
    public CarrotOnAStickItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (param0.isClientSide) {
            return InteractionResultHolder.pass(var0);
        } else {
            if (param1.isPassenger() && param1.getVehicle() instanceof Pig) {
                Pig var1 = (Pig)param1.getVehicle();
                if (var0.getMaxDamage() - var0.getDamageValue() >= 7 && var1.boost()) {
                    var0.hurtAndBreak(7, param1, param1x -> param1x.broadcastBreakEvent(param2));
                    if (var0.isEmpty()) {
                        ItemStack var2 = new ItemStack(Items.FISHING_ROD);
                        var2.setTag(var0.getTag());
                        return InteractionResultHolder.success(var2);
                    }

                    return InteractionResultHolder.success(var0);
                }
            }

            param1.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.pass(var0);
        }
    }
}
