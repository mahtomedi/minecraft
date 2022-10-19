package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

public class EnchantedBookItem extends Item {
    public static final String TAG_STORED_ENCHANTMENTS = "StoredEnchantments";

    public EnchantedBookItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack param0) {
        return false;
    }

    public static ListTag getEnchantments(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null ? var0.getList("StoredEnchantments", 10) : new ListTag();
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        ItemStack.appendEnchantmentNames(param2, getEnchantments(param0));
    }

    public static void addEnchantment(ItemStack param0, EnchantmentInstance param1) {
        ListTag var0 = getEnchantments(param0);
        boolean var1 = true;
        ResourceLocation var2 = EnchantmentHelper.getEnchantmentId(param1.enchantment);

        for(int var3 = 0; var3 < var0.size(); ++var3) {
            CompoundTag var4 = var0.getCompound(var3);
            ResourceLocation var5 = EnchantmentHelper.getEnchantmentId(var4);
            if (var5 != null && var5.equals(var2)) {
                if (EnchantmentHelper.getEnchantmentLevel(var4) < param1.level) {
                    EnchantmentHelper.setEnchantmentLevel(var4, param1.level);
                }

                var1 = false;
                break;
            }
        }

        if (var1) {
            var0.add(EnchantmentHelper.storeEnchantment(var2, param1.level));
        }

        param0.getOrCreateTag().put("StoredEnchantments", var0);
    }

    public static ItemStack createForEnchantment(EnchantmentInstance param0) {
        ItemStack var0 = new ItemStack(Items.ENCHANTED_BOOK);
        addEnchantment(var0, param0);
        return var0;
    }
}
