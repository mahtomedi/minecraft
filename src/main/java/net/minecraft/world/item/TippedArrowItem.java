package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TippedArrowItem extends ArrowItem {
    public TippedArrowItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.POISON);
    }

    @Override
    public void fillItemCategory(CreativeModeTab param0, NonNullList<ItemStack> param1) {
        if (this.allowdedIn(param0)) {
            for(Potion var0 : Registry.POTION) {
                if (!var0.getEffects().isEmpty()) {
                    param1.add(PotionUtils.setPotion(new ItemStack(this), var0));
                }
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        PotionUtils.addPotionTooltip(param0, param2, 0.125F);
    }

    @Override
    public String getDescriptionId(ItemStack param0) {
        return PotionUtils.getPotion(param0).getName(this.getDescriptionId() + ".effect.");
    }
}
