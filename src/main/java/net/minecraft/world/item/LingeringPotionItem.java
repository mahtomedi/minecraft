package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class LingeringPotionItem extends ThrowablePotionItem {
    public LingeringPotionItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        PotionUtils.addPotionTooltip(param0, param2, 0.25F);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        param0.playSound(
            null,
            param1.getX(),
            param1.getY(),
            param1.getZ(),
            SoundEvents.LINGERING_POTION_THROW,
            SoundSource.NEUTRAL,
            0.5F,
            0.4F / (param0.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        return super.use(param0, param1, param2);
    }
}
