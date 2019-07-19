package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class NameTagItem extends Item {
    public NameTagItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean interactEnemy(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        if (param0.hasCustomHoverName() && !(param2 instanceof Player)) {
            if (param2.isAlive()) {
                param2.setCustomName(param0.getHoverName());
                if (param2 instanceof Mob) {
                    ((Mob)param2).setPersistenceRequired();
                }

                param0.shrink(1);
            }

            return true;
        } else {
            return false;
        }
    }
}
