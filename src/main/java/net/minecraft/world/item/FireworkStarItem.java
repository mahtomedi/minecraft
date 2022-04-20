package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

public class FireworkStarItem extends Item {
    public FireworkStarItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        CompoundTag var0 = param0.getTagElement("Explosion");
        if (var0 != null) {
            appendHoverText(var0, param2);
        }

    }

    public static void appendHoverText(CompoundTag param0, List<Component> param1) {
        FireworkRocketItem.Shape var0 = FireworkRocketItem.Shape.byId(param0.getByte("Type"));
        param1.add(Component.translatable("item.minecraft.firework_star.shape." + var0.getName()).withStyle(ChatFormatting.GRAY));
        int[] var1 = param0.getIntArray("Colors");
        if (var1.length > 0) {
            param1.add(appendColors(Component.empty().withStyle(ChatFormatting.GRAY), var1));
        }

        int[] var2 = param0.getIntArray("FadeColors");
        if (var2.length > 0) {
            param1.add(appendColors(Component.translatable("item.minecraft.firework_star.fade_to").append(" ").withStyle(ChatFormatting.GRAY), var2));
        }

        if (param0.getBoolean("Trail")) {
            param1.add(Component.translatable("item.minecraft.firework_star.trail").withStyle(ChatFormatting.GRAY));
        }

        if (param0.getBoolean("Flicker")) {
            param1.add(Component.translatable("item.minecraft.firework_star.flicker").withStyle(ChatFormatting.GRAY));
        }

    }

    private static Component appendColors(MutableComponent param0, int[] param1) {
        for(int var0 = 0; var0 < param1.length; ++var0) {
            if (var0 > 0) {
                param0.append(", ");
            }

            param0.append(getColorName(param1[var0]));
        }

        return param0;
    }

    private static Component getColorName(int param0) {
        DyeColor var0 = DyeColor.byFireworkColor(param0);
        return var0 == null
            ? Component.translatable("item.minecraft.firework_star.custom_color")
            : Component.translatable("item.minecraft.firework_star." + var0.getName());
    }
}
