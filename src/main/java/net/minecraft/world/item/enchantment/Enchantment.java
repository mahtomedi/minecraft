package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;

public abstract class Enchantment {
    private final EquipmentSlot[] slots;
    private final Enchantment.Rarity rarity;
    public final EnchantmentCategory category;
    @Nullable
    protected String descriptionId;

    @Nullable
    public static Enchantment byId(int param0) {
        return Registry.ENCHANTMENT.byId(param0);
    }

    protected Enchantment(Enchantment.Rarity param0, EnchantmentCategory param1, EquipmentSlot[] param2) {
        this.rarity = param0;
        this.category = param1;
        this.slots = param2;
    }

    public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity param0) {
        Map<EquipmentSlot, ItemStack> var0 = Maps.newEnumMap(EquipmentSlot.class);

        for(EquipmentSlot var1 : this.slots) {
            ItemStack var2 = param0.getItemBySlot(var1);
            if (!var2.isEmpty()) {
                var0.put(var1, var2);
            }
        }

        return var0;
    }

    public Enchantment.Rarity getRarity() {
        return this.rarity;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinCost(int param0) {
        return 1 + param0 * 10;
    }

    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 5;
    }

    public int getDamageProtection(int param0, DamageSource param1) {
        return 0;
    }

    public float getDamageBonus(int param0, MobType param1) {
        return 0.0F;
    }

    public final boolean isCompatibleWith(Enchantment param0) {
        return this.checkCompatibility(param0) && param0.checkCompatibility(this);
    }

    protected boolean checkCompatibility(Enchantment param0) {
        return this != param0;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("enchantment", Registry.ENCHANTMENT.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getFullname(int param0) {
        MutableComponent var0 = Component.translatable(this.getDescriptionId());
        if (this.isCurse()) {
            var0.withStyle(ChatFormatting.RED);
        } else {
            var0.withStyle(ChatFormatting.GRAY);
        }

        if (param0 != 1 || this.getMaxLevel() != 1) {
            var0.append(" ").append(Component.translatable("enchantment.level." + param0));
        }

        return var0;
    }

    public boolean canEnchant(ItemStack param0) {
        return this.category.canEnchant(param0.getItem());
    }

    public void doPostAttack(LivingEntity param0, Entity param1, int param2) {
    }

    public void doPostHurt(LivingEntity param0, Entity param1, int param2) {
    }

    public boolean isTreasureOnly() {
        return false;
    }

    public boolean isCurse() {
        return false;
    }

    public boolean isTradeable() {
        return true;
    }

    public boolean isDiscoverable() {
        return true;
    }

    public static enum Rarity {
        COMMON(10),
        UNCOMMON(5),
        RARE(2),
        VERY_RARE(1);

        private final int weight;

        private Rarity(int param0) {
            this.weight = param0;
        }

        public int getWeight() {
            return this.weight;
        }
    }
}
