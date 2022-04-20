package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

public class DiscFragmentItem extends Item {
    public DiscFragmentItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        param2.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.getDescriptionId() + ".desc");
    }
}
