package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
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
        ItemStack var0 = param1.getItemInHand(param2);
        if (param0.isClientSide) {
            return InteractionResultHolder.success(var0);
        } else {
            if (!param1.getAbilities().instabuild) {
                var0.shrink(1);
            }

            param1.awardStat(Stats.ITEM_USED.get(this));
            param1.level().playSound(null, param1, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, param1.getSoundSource(), 1.0F, 1.0F);
            ItemStack var1 = MapItem.create(param0, param1.getBlockX(), param1.getBlockZ(), (byte)0, true, false);
            if (var0.isEmpty()) {
                return InteractionResultHolder.consume(var1);
            } else {
                if (!param1.getInventory().add(var1.copy())) {
                    param1.drop(var1, false);
                }

                return InteractionResultHolder.consume(var0);
            }
        }
    }
}
