package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireworkStarRecipe extends CustomRecipe {
    private static final Ingredient SHAPE_INGREDIENT = Ingredient.of(
        Items.FIRE_CHARGE,
        Items.FEATHER,
        Items.GOLD_NUGGET,
        Items.SKELETON_SKULL,
        Items.WITHER_SKELETON_SKULL,
        Items.CREEPER_HEAD,
        Items.PLAYER_HEAD,
        Items.DRAGON_HEAD,
        Items.ZOMBIE_HEAD
    );
    private static final Ingredient TRAIL_INGREDIENT = Ingredient.of(Items.DIAMOND);
    private static final Ingredient FLICKER_INGREDIENT = Ingredient.of(Items.GLOWSTONE_DUST);
    private static final Map<Item, FireworkRocketItem.Shape> SHAPE_BY_ITEM = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(Items.FIRE_CHARGE, FireworkRocketItem.Shape.LARGE_BALL);
        param0.put(Items.FEATHER, FireworkRocketItem.Shape.BURST);
        param0.put(Items.GOLD_NUGGET, FireworkRocketItem.Shape.STAR);
        param0.put(Items.SKELETON_SKULL, FireworkRocketItem.Shape.CREEPER);
        param0.put(Items.WITHER_SKELETON_SKULL, FireworkRocketItem.Shape.CREEPER);
        param0.put(Items.CREEPER_HEAD, FireworkRocketItem.Shape.CREEPER);
        param0.put(Items.PLAYER_HEAD, FireworkRocketItem.Shape.CREEPER);
        param0.put(Items.DRAGON_HEAD, FireworkRocketItem.Shape.CREEPER);
        param0.put(Items.ZOMBIE_HEAD, FireworkRocketItem.Shape.CREEPER);
    });
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);

    public FireworkStarRecipe(ResourceLocation param0) {
        super(param0);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        boolean var0 = false;
        boolean var1 = false;
        boolean var2 = false;
        boolean var3 = false;
        boolean var4 = false;

        for(int var5 = 0; var5 < param0.getContainerSize(); ++var5) {
            ItemStack var6 = param0.getItem(var5);
            if (!var6.isEmpty()) {
                if (SHAPE_INGREDIENT.test(var6)) {
                    if (var2) {
                        return false;
                    }

                    var2 = true;
                } else if (FLICKER_INGREDIENT.test(var6)) {
                    if (var4) {
                        return false;
                    }

                    var4 = true;
                } else if (TRAIL_INGREDIENT.test(var6)) {
                    if (var3) {
                        return false;
                    }

                    var3 = true;
                } else if (GUNPOWDER_INGREDIENT.test(var6)) {
                    if (var0) {
                        return false;
                    }

                    var0 = true;
                } else {
                    if (!(var6.getItem() instanceof DyeItem)) {
                        return false;
                    }

                    var1 = true;
                }
            }
        }

        return var0 && var1;
    }

    public ItemStack assemble(CraftingContainer param0) {
        ItemStack var0 = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag var1 = var0.getOrCreateTagElement("Explosion");
        FireworkRocketItem.Shape var2 = FireworkRocketItem.Shape.SMALL_BALL;
        List<Integer> var3 = Lists.newArrayList();

        for(int var4 = 0; var4 < param0.getContainerSize(); ++var4) {
            ItemStack var5 = param0.getItem(var4);
            if (!var5.isEmpty()) {
                if (SHAPE_INGREDIENT.test(var5)) {
                    var2 = SHAPE_BY_ITEM.get(var5.getItem());
                } else if (FLICKER_INGREDIENT.test(var5)) {
                    var1.putBoolean("Flicker", true);
                } else if (TRAIL_INGREDIENT.test(var5)) {
                    var1.putBoolean("Trail", true);
                } else if (var5.getItem() instanceof DyeItem) {
                    var3.add(((DyeItem)var5.getItem()).getDyeColor().getFireworkColor());
                }
            }
        }

        var1.putIntArray("Colors", var3);
        var1.putByte("Type", (byte)var2.getId());
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(Items.FIREWORK_STAR);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR;
    }
}
