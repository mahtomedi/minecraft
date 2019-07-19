package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ArmorMaterial {
    int getDurabilityForSlot(EquipmentSlot var1);

    int getDefenseForSlot(EquipmentSlot var1);

    int getEnchantmentValue();

    SoundEvent getEquipSound();

    Ingredient getRepairIngredient();

    @OnlyIn(Dist.CLIENT)
    String getName();

    float getToughness();
}
