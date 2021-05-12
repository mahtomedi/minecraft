package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentHelper {
    public static int getItemEnchantmentLevel(Enchantment param0, ItemStack param1) {
        if (param1.isEmpty()) {
            return 0;
        } else {
            ResourceLocation var0 = Registry.ENCHANTMENT.getKey(param0);
            ListTag var1 = param1.getEnchantmentTags();

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                CompoundTag var3 = var1.getCompound(var2);
                ResourceLocation var4 = ResourceLocation.tryParse(var3.getString("id"));
                if (var4 != null && var4.equals(var0)) {
                    return Mth.clamp(var3.getInt("lvl"), 0, 255);
                }
            }

            return 0;
        }
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack param0) {
        ListTag var0 = param0.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(param0) : param0.getEnchantmentTags();
        return deserializeEnchantments(var0);
    }

    public static Map<Enchantment, Integer> deserializeEnchantments(ListTag param0) {
        Map<Enchantment, Integer> var0 = Maps.newLinkedHashMap();

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            CompoundTag var2 = param0.getCompound(var1);
            Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(var2.getString("id"))).ifPresent(param2 -> var0.put(param2, var2.getInt("lvl")));
        }

        return var0;
    }

    public static void setEnchantments(Map<Enchantment, Integer> param0, ItemStack param1) {
        ListTag var0 = new ListTag();

        for(Entry<Enchantment, Integer> var1 : param0.entrySet()) {
            Enchantment var2 = var1.getKey();
            if (var2 != null) {
                int var3 = var1.getValue();
                CompoundTag var4 = new CompoundTag();
                var4.putString("id", String.valueOf(Registry.ENCHANTMENT.getKey(var2)));
                var4.putShort("lvl", (short)var3);
                var0.add(var4);
                if (param1.is(Items.ENCHANTED_BOOK)) {
                    EnchantedBookItem.addEnchantment(param1, new EnchantmentInstance(var2, var3));
                }
            }
        }

        if (var0.isEmpty()) {
            param1.removeTagKey("Enchantments");
        } else if (!param1.is(Items.ENCHANTED_BOOK)) {
            param1.addTagElement("Enchantments", var0);
        }

    }

    private static void runIterationOnItem(EnchantmentHelper.EnchantmentVisitor param0, ItemStack param1) {
        if (!param1.isEmpty()) {
            ListTag var0 = param1.getEnchantmentTags();

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                String var2 = var0.getCompound(var1).getString("id");
                int var3 = var0.getCompound(var1).getInt("lvl");
                Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(var2)).ifPresent(param2 -> param0.accept(param2, var3));
            }

        }
    }

    private static void runIterationOnInventory(EnchantmentHelper.EnchantmentVisitor param0, Iterable<ItemStack> param1) {
        for(ItemStack var0 : param1) {
            runIterationOnItem(param0, var0);
        }

    }

    public static int getDamageProtection(Iterable<ItemStack> param0, DamageSource param1) {
        MutableInt var0 = new MutableInt();
        runIterationOnInventory((param2, param3) -> var0.add(param2.getDamageProtection(param3, param1)), param0);
        return var0.intValue();
    }

    public static float getDamageBonus(ItemStack param0, MobType param1) {
        MutableFloat var0 = new MutableFloat();
        runIterationOnItem((param2, param3) -> var0.add(param2.getDamageBonus(param3, param1)), param0);
        return var0.floatValue();
    }

    public static float getSweepingDamageRatio(LivingEntity param0) {
        int var0 = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, param0);
        return var0 > 0 ? SweepingEdgeEnchantment.getSweepingDamageRatio(var0) : 0.0F;
    }

    public static void doPostHurtEffects(LivingEntity param0, Entity param1) {
        EnchantmentHelper.EnchantmentVisitor var0 = (param2, param3) -> param2.doPostHurt(param0, param1, param3);
        if (param0 != null) {
            runIterationOnInventory(var0, param0.getAllSlots());
        }

        if (param1 instanceof Player) {
            runIterationOnItem(var0, param0.getMainHandItem());
        }

    }

    public static void doPostDamageEffects(LivingEntity param0, Entity param1) {
        EnchantmentHelper.EnchantmentVisitor var0 = (param2, param3) -> param2.doPostAttack(param0, param1, param3);
        if (param0 != null) {
            runIterationOnInventory(var0, param0.getAllSlots());
        }

        if (param0 instanceof Player) {
            runIterationOnItem(var0, param0.getMainHandItem());
        }

    }

    public static int getEnchantmentLevel(Enchantment param0, LivingEntity param1) {
        Iterable<ItemStack> var0 = param0.getSlotItems(param1).values();
        if (var0 == null) {
            return 0;
        } else {
            int var1 = 0;

            for(ItemStack var2 : var0) {
                int var3 = getItemEnchantmentLevel(param0, var2);
                if (var3 > var1) {
                    var1 = var3;
                }
            }

            return var1;
        }
    }

    public static int getKnockbackBonus(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.KNOCKBACK, param0);
    }

    public static int getFireAspect(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.FIRE_ASPECT, param0);
    }

    public static int getRespiration(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.RESPIRATION, param0);
    }

    public static int getDepthStrider(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, param0);
    }

    public static int getBlockEfficiency(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, param0);
    }

    public static int getFishingLuckBonus(ItemStack param0) {
        return getItemEnchantmentLevel(Enchantments.FISHING_LUCK, param0);
    }

    public static int getFishingSpeedBonus(ItemStack param0) {
        return getItemEnchantmentLevel(Enchantments.FISHING_SPEED, param0);
    }

    public static int getMobLooting(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.MOB_LOOTING, param0);
    }

    public static boolean hasAquaAffinity(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, param0) > 0;
    }

    public static boolean hasFrostWalker(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.FROST_WALKER, param0) > 0;
    }

    public static boolean hasSoulSpeed(LivingEntity param0) {
        return getEnchantmentLevel(Enchantments.SOUL_SPEED, param0) > 0;
    }

    public static boolean hasBindingCurse(ItemStack param0) {
        return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, param0) > 0;
    }

    public static boolean hasVanishingCurse(ItemStack param0) {
        return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, param0) > 0;
    }

    public static int getLoyalty(ItemStack param0) {
        return getItemEnchantmentLevel(Enchantments.LOYALTY, param0);
    }

    public static int getRiptide(ItemStack param0) {
        return getItemEnchantmentLevel(Enchantments.RIPTIDE, param0);
    }

    public static boolean hasChanneling(ItemStack param0) {
        return getItemEnchantmentLevel(Enchantments.CHANNELING, param0) > 0;
    }

    @Nullable
    public static Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment param0, LivingEntity param1) {
        return getRandomItemWith(param0, param1, param0x -> true);
    }

    @Nullable
    public static Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment param0, LivingEntity param1, Predicate<ItemStack> param2) {
        Map<EquipmentSlot, ItemStack> var0 = param0.getSlotItems(param1);
        if (var0.isEmpty()) {
            return null;
        } else {
            List<Entry<EquipmentSlot, ItemStack>> var1 = Lists.newArrayList();

            for(Entry<EquipmentSlot, ItemStack> var2 : var0.entrySet()) {
                ItemStack var3 = var2.getValue();
                if (!var3.isEmpty() && getItemEnchantmentLevel(param0, var3) > 0 && param2.test(var3)) {
                    var1.add(var2);
                }
            }

            return var1.isEmpty() ? null : var1.get(param1.getRandom().nextInt(var1.size()));
        }
    }

    public static int getEnchantmentCost(Random param0, int param1, int param2, ItemStack param3) {
        Item var0 = param3.getItem();
        int var1 = var0.getEnchantmentValue();
        if (var1 <= 0) {
            return 0;
        } else {
            if (param2 > 15) {
                param2 = 15;
            }

            int var2 = param0.nextInt(8) + 1 + (param2 >> 1) + param0.nextInt(param2 + 1);
            if (param1 == 0) {
                return Math.max(var2 / 3, 1);
            } else {
                return param1 == 1 ? var2 * 2 / 3 + 1 : Math.max(var2, param2 * 2);
            }
        }
    }

    public static ItemStack enchantItem(Random param0, ItemStack param1, int param2, boolean param3) {
        List<EnchantmentInstance> var0 = selectEnchantment(param0, param1, param2, param3);
        boolean var1 = param1.is(Items.BOOK);
        if (var1) {
            param1 = new ItemStack(Items.ENCHANTED_BOOK);
        }

        for(EnchantmentInstance var2 : var0) {
            if (var1) {
                EnchantedBookItem.addEnchantment(param1, var2);
            } else {
                param1.enchant(var2.enchantment, var2.level);
            }
        }

        return param1;
    }

    public static List<EnchantmentInstance> selectEnchantment(Random param0, ItemStack param1, int param2, boolean param3) {
        List<EnchantmentInstance> var0 = Lists.newArrayList();
        Item var1 = param1.getItem();
        int var2 = var1.getEnchantmentValue();
        if (var2 <= 0) {
            return var0;
        } else {
            param2 += 1 + param0.nextInt(var2 / 4 + 1) + param0.nextInt(var2 / 4 + 1);
            float var3 = (param0.nextFloat() + param0.nextFloat() - 1.0F) * 0.15F;
            param2 = Mth.clamp(Math.round((float)param2 + (float)param2 * var3), 1, Integer.MAX_VALUE);
            List<EnchantmentInstance> var4 = getAvailableEnchantmentResults(param2, param1, param3);
            if (!var4.isEmpty()) {
                WeightedRandom.getRandomItem(param0, var4).ifPresent(var0::add);

                while(param0.nextInt(50) <= param2) {
                    if (!var0.isEmpty()) {
                        filterCompatibleEnchantments(var4, Util.lastOf(var0));
                    }

                    if (var4.isEmpty()) {
                        break;
                    }

                    WeightedRandom.getRandomItem(param0, var4).ifPresent(var0::add);
                    param2 /= 2;
                }
            }

            return var0;
        }
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> param0, EnchantmentInstance param1) {
        Iterator<EnchantmentInstance> var0 = param0.iterator();

        while(var0.hasNext()) {
            if (!param1.enchantment.isCompatibleWith(var0.next().enchantment)) {
                var0.remove();
            }
        }

    }

    public static boolean isEnchantmentCompatible(Collection<Enchantment> param0, Enchantment param1) {
        for(Enchantment var0 : param0) {
            if (!var0.isCompatibleWith(param1)) {
                return false;
            }
        }

        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int param0, ItemStack param1, boolean param2) {
        List<EnchantmentInstance> var0 = Lists.newArrayList();
        Item var1 = param1.getItem();
        boolean var2 = param1.is(Items.BOOK);

        for(Enchantment var3 : Registry.ENCHANTMENT) {
            if ((!var3.isTreasureOnly() || param2) && var3.isDiscoverable() && (var3.category.canEnchant(var1) || var2)) {
                for(int var4 = var3.getMaxLevel(); var4 > var3.getMinLevel() - 1; --var4) {
                    if (param0 >= var3.getMinCost(var4) && param0 <= var3.getMaxCost(var4)) {
                        var0.add(new EnchantmentInstance(var3, var4));
                        break;
                    }
                }
            }
        }

        return var0;
    }

    @FunctionalInterface
    interface EnchantmentVisitor {
        void accept(Enchantment var1, int var2);
    }
}
