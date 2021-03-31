package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;

public class PotionBrewing {
    public static final int BREWING_TIME_SECONDS = 20;
    private static final List<PotionBrewing.Mix<Potion>> POTION_MIXES = Lists.newArrayList();
    private static final List<PotionBrewing.Mix<Item>> CONTAINER_MIXES = Lists.newArrayList();
    private static final List<Ingredient> ALLOWED_CONTAINERS = Lists.newArrayList();
    private static final Predicate<ItemStack> ALLOWED_CONTAINER = param0 -> {
        for(Ingredient var0 : ALLOWED_CONTAINERS) {
            if (var0.test(param0)) {
                return true;
            }
        }

        return false;
    };

    public static boolean isIngredient(ItemStack param0) {
        return isContainerIngredient(param0) || isPotionIngredient(param0);
    }

    protected static boolean isContainerIngredient(ItemStack param0) {
        int var0 = 0;

        for(int var1 = CONTAINER_MIXES.size(); var0 < var1; ++var0) {
            if (CONTAINER_MIXES.get(var0).ingredient.test(param0)) {
                return true;
            }
        }

        return false;
    }

    protected static boolean isPotionIngredient(ItemStack param0) {
        int var0 = 0;

        for(int var1 = POTION_MIXES.size(); var0 < var1; ++var0) {
            if (POTION_MIXES.get(var0).ingredient.test(param0)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBrewablePotion(Potion param0) {
        int var0 = 0;

        for(int var1 = POTION_MIXES.size(); var0 < var1; ++var0) {
            if (POTION_MIXES.get(var0).to == param0) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasMix(ItemStack param0, ItemStack param1) {
        if (!ALLOWED_CONTAINER.test(param0)) {
            return false;
        } else {
            return hasContainerMix(param0, param1) || hasPotionMix(param0, param1);
        }
    }

    protected static boolean hasContainerMix(ItemStack param0, ItemStack param1) {
        Item var0 = param0.getItem();
        int var1 = 0;

        for(int var2 = CONTAINER_MIXES.size(); var1 < var2; ++var1) {
            PotionBrewing.Mix<Item> var3 = CONTAINER_MIXES.get(var1);
            if (var3.from == var0 && var3.ingredient.test(param1)) {
                return true;
            }
        }

        return false;
    }

    protected static boolean hasPotionMix(ItemStack param0, ItemStack param1) {
        Potion var0 = PotionUtils.getPotion(param0);
        int var1 = 0;

        for(int var2 = POTION_MIXES.size(); var1 < var2; ++var1) {
            PotionBrewing.Mix<Potion> var3 = POTION_MIXES.get(var1);
            if (var3.from == var0 && var3.ingredient.test(param1)) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack mix(ItemStack param0, ItemStack param1) {
        if (!param1.isEmpty()) {
            Potion var0 = PotionUtils.getPotion(param1);
            Item var1 = param1.getItem();
            int var2 = 0;

            for(int var3 = CONTAINER_MIXES.size(); var2 < var3; ++var2) {
                PotionBrewing.Mix<Item> var4 = CONTAINER_MIXES.get(var2);
                if (var4.from == var1 && var4.ingredient.test(param0)) {
                    return PotionUtils.setPotion(new ItemStack(var4.to), var0);
                }
            }

            var2 = 0;

            for(int var6 = POTION_MIXES.size(); var2 < var6; ++var2) {
                PotionBrewing.Mix<Potion> var7 = POTION_MIXES.get(var2);
                if (var7.from == var0 && var7.ingredient.test(param0)) {
                    return PotionUtils.setPotion(new ItemStack(var1), var7.to);
                }
            }
        }

        return param1;
    }

    public static void bootStrap() {
        addContainer(Items.POTION);
        addContainer(Items.SPLASH_POTION);
        addContainer(Items.LINGERING_POTION);
        addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
        addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
        addMix(Potions.WATER, Items.GLISTERING_MELON_SLICE, Potions.MUNDANE);
        addMix(Potions.WATER, Items.GHAST_TEAR, Potions.MUNDANE);
        addMix(Potions.WATER, Items.RABBIT_FOOT, Potions.MUNDANE);
        addMix(Potions.WATER, Items.BLAZE_POWDER, Potions.MUNDANE);
        addMix(Potions.WATER, Items.SPIDER_EYE, Potions.MUNDANE);
        addMix(Potions.WATER, Items.SUGAR, Potions.MUNDANE);
        addMix(Potions.WATER, Items.MAGMA_CREAM, Potions.MUNDANE);
        addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
        addMix(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
        addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
        addMix(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        addMix(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
        addMix(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
        addMix(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
        addMix(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
        addMix(Potions.AWKWARD, Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        addMix(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
        addMix(Potions.AWKWARD, Items.RABBIT_FOOT, Potions.LEAPING);
        addMix(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
        addMix(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
        addMix(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        addMix(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        addMix(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
        addMix(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
        addMix(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        addMix(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
        addMix(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
        addMix(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        addMix(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        addMix(Potions.AWKWARD, Items.SUGAR, Potions.SWIFTNESS);
        addMix(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
        addMix(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
        addMix(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
        addMix(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
        addMix(Potions.AWKWARD, Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        addMix(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
        addMix(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        addMix(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        addMix(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
        addMix(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        addMix(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        addMix(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        addMix(Potions.AWKWARD, Items.SPIDER_EYE, Potions.POISON);
        addMix(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
        addMix(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
        addMix(Potions.AWKWARD, Items.GHAST_TEAR, Potions.REGENERATION);
        addMix(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
        addMix(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
        addMix(Potions.AWKWARD, Items.BLAZE_POWDER, Potions.STRENGTH);
        addMix(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
        addMix(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
        addMix(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
        addMix(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
        addMix(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
        addMix(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
    }

    private static void addContainerRecipe(Item param0, Item param1, Item param2) {
        if (!(param0 instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + Registry.ITEM.getKey(param0));
        } else if (!(param2 instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + Registry.ITEM.getKey(param2));
        } else {
            CONTAINER_MIXES.add(new PotionBrewing.Mix<>(param0, Ingredient.of(param1), param2));
        }
    }

    private static void addContainer(Item param0) {
        if (!(param0 instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + Registry.ITEM.getKey(param0));
        } else {
            ALLOWED_CONTAINERS.add(Ingredient.of(param0));
        }
    }

    private static void addMix(Potion param0, Item param1, Potion param2) {
        POTION_MIXES.add(new PotionBrewing.Mix<>(param0, Ingredient.of(param1), param2));
    }

    static class Mix<T> {
        private final T from;
        private final Ingredient ingredient;
        private final T to;

        public Mix(T param0, Ingredient param1, T param2) {
            this.from = param0;
            this.ingredient = param1;
            this.to = param2;
        }
    }
}
