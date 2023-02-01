package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Ingredient;

public enum ArmorMaterials implements StringRepresentable, ArmorMaterial {
    LEATHER("leather", 5, Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 1);
        param0.put(ArmorItem.Type.LEGGINGS, 2);
        param0.put(ArmorItem.Type.CHESTPLATE, 3);
        param0.put(ArmorItem.Type.HELMET, 1);
    }), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER)),
    CHAIN("chainmail", 15, Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 1);
        param0.put(ArmorItem.Type.LEGGINGS, 4);
        param0.put(ArmorItem.Type.CHESTPLATE, 5);
        param0.put(ArmorItem.Type.HELMET, 2);
    }), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)),
    IRON("iron", 15, Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 2);
        param0.put(ArmorItem.Type.LEGGINGS, 5);
        param0.put(ArmorItem.Type.CHESTPLATE, 6);
        param0.put(ArmorItem.Type.HELMET, 2);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)),
    GOLD("gold", 7, Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 1);
        param0.put(ArmorItem.Type.LEGGINGS, 3);
        param0.put(ArmorItem.Type.CHESTPLATE, 5);
        param0.put(ArmorItem.Type.HELMET, 2);
    }), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT)),
    DIAMOND("diamond", 33, Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 3);
        param0.put(ArmorItem.Type.LEGGINGS, 6);
        param0.put(ArmorItem.Type.CHESTPLATE, 8);
        param0.put(ArmorItem.Type.HELMET, 3);
    }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.of(Items.DIAMOND)),
    TURTLE("turtle", 25, Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 2);
        param0.put(ArmorItem.Type.LEGGINGS, 5);
        param0.put(ArmorItem.Type.CHESTPLATE, 6);
        param0.put(ArmorItem.Type.HELMET, 2);
    }), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.SCUTE)),
    NETHERITE("netherite", 37, Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 3);
        param0.put(ArmorItem.Type.LEGGINGS, 6);
        param0.put(ArmorItem.Type.CHESTPLATE, 8);
        param0.put(ArmorItem.Type.HELMET, 3);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT));

    public static final StringRepresentable.EnumCodec<ArmorMaterials> CODEC = StringRepresentable.fromEnum(ArmorMaterials::values);
    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), param0 -> {
        param0.put(ArmorItem.Type.BOOTS, 13);
        param0.put(ArmorItem.Type.LEGGINGS, 15);
        param0.put(ArmorItem.Type.CHESTPLATE, 16);
        param0.put(ArmorItem.Type.HELMET, 11);
    });
    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    private ArmorMaterials(
        String param0,
        int param1,
        EnumMap<ArmorItem.Type, Integer> param2,
        int param3,
        SoundEvent param4,
        float param5,
        float param6,
        Supplier<Ingredient> param7
    ) {
        this.name = param0;
        this.durabilityMultiplier = param1;
        this.protectionFunctionForType = param2;
        this.enchantmentValue = param3;
        this.sound = param4;
        this.toughness = param5;
        this.knockbackResistance = param6;
        this.repairIngredient = new LazyLoadedValue<>(param7);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type param0) {
        return HEALTH_FUNCTION_FOR_TYPE.get(param0) * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type param0) {
        return this.protectionFunctionForType.get(param0);
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.sound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
