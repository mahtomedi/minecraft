package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShieldItem extends Item {
    public ShieldItem(Item.Properties param0) {
        super(param0);
        this.addProperty(
            new ResourceLocation("blocking"),
            (param0x, param1, param2) -> param2 != null && param2.isUsingItem() && param2.getUseItem() == param0x ? 1.0F : 0.0F
        );
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public String getDescriptionId(ItemStack param0) {
        return param0.getTagElement("BlockEntityTag") != null ? this.getDescriptionId() + '.' + getColor(param0).getName() : super.getDescriptionId(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(param0, param2);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        param1.startUsingItem(param2);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, var0);
    }

    @Override
    public boolean isValidRepairItem(ItemStack param0, ItemStack param1) {
        return ItemTags.PLANKS.contains(param1.getItem()) || super.isValidRepairItem(param0, param1);
    }

    public static DyeColor getColor(ItemStack param0) {
        return DyeColor.byId(param0.getOrCreateTagElement("BlockEntityTag").getInt("Base"));
    }
}
