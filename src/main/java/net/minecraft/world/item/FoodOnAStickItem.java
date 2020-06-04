package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity & ItemSteerable> extends Item {
    private final EntityType<T> canInteractWith;
    private final int consumeItemDamage;

    public FoodOnAStickItem(Item.Properties param0, EntityType<T> param1, int param2) {
        super(param0);
        this.canInteractWith = param1;
        this.consumeItemDamage = param2;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (param0.isClientSide) {
            return InteractionResultHolder.pass(var0);
        } else {
            Entity var1 = param1.getVehicle();
            if (param1.isPassenger() && var1 instanceof ItemSteerable && var1.getType() == this.canInteractWith) {
                ItemSteerable var2 = (ItemSteerable)var1;
                if (var2.boost()) {
                    var0.hurtAndBreak(this.consumeItemDamage, param1, param1x -> param1x.broadcastBreakEvent(param2));
                    if (var0.isEmpty()) {
                        ItemStack var3 = new ItemStack(Items.FISHING_ROD);
                        var3.setTag(var0.getTag());
                        return InteractionResultHolder.success(var3);
                    }

                    return InteractionResultHolder.success(var0);
                }
            }

            param1.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.pass(var0);
        }
    }
}
