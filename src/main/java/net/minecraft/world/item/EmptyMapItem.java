package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EmptyMapItem extends ComplexItem {
    public EmptyMapItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = MapItem.create(param0, Mth.floor(param1.getX()), Mth.floor(param1.getZ()), (byte)0, true, false);
        ItemStack var1 = param1.getItemInHand(param2);
        if (!param1.abilities.instabuild) {
            var1.shrink(1);
        }

        if (var1.isEmpty()) {
            return InteractionResultHolder.success(var0);
        } else {
            if (!param1.inventory.add(var0.copy())) {
                param1.drop(var0, false);
            }

            param1.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(var1);
        }
    }
}
