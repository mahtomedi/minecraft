package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SmithingTrimRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;

    public SmithingTrimRecipe(ResourceLocation param0, Ingredient param1, Ingredient param2, Ingredient param3) {
        this.id = param0;
        this.template = param1;
        this.base = param2;
        this.addition = param3;
    }

    @Override
    public boolean matches(Container param0, Level param1) {
        return this.template.test(param0.getItem(0)) && this.base.test(param0.getItem(1)) && this.addition.test(param0.getItem(2));
    }

    @Override
    public ItemStack assemble(Container param0, RegistryAccess param1) {
        ItemStack var0 = param0.getItem(1);
        if (this.base.test(var0)) {
            Optional<Holder.Reference<TrimMaterial>> var1 = TrimMaterials.getFromIngredient(param1, param0.getItem(2));
            Optional<Holder.Reference<TrimPattern>> var2 = TrimPatterns.getFromTemplate(param1, param0.getItem(0));
            if (var1.isPresent() && var2.isPresent()) {
                Optional<ArmorTrim> var3 = ArmorTrim.getTrim(param1, var0, false);
                if (var3.isPresent() && var3.get().hasPatternAndMaterial(var2.get(), var1.get())) {
                    return ItemStack.EMPTY;
                }

                ItemStack var4 = var0.copy();
                var4.setCount(1);
                ArmorTrim var5 = new ArmorTrim(var1.get(), var2.get());
                if (ArmorTrim.setTrim(param1, var4, var5)) {
                    return var4;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess param0) {
        ItemStack var0 = new ItemStack(Items.IRON_CHESTPLATE);
        Optional<Holder.Reference<TrimPattern>> var1 = param0.registryOrThrow(Registries.TRIM_PATTERN).holders().findFirst();
        if (var1.isPresent()) {
            Optional<Holder.Reference<TrimMaterial>> var2 = param0.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(TrimMaterials.REDSTONE);
            if (var2.isPresent()) {
                ArmorTrim var3 = new ArmorTrim(var2.get(), var1.get());
                ArmorTrim.setTrim(param0, var0, var3);
            }
        }

        return var0;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack param0) {
        return this.template.test(param0);
    }

    @Override
    public boolean isBaseIngredient(ItemStack param0) {
        return this.base.test(param0);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack param0) {
        return this.addition.test(param0);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
        public SmithingTrimRecipe fromJson(ResourceLocation param0, JsonObject param1) {
            Ingredient var0 = Ingredient.fromJson(GsonHelper.getNonNull(param1, "template"));
            Ingredient var1 = Ingredient.fromJson(GsonHelper.getNonNull(param1, "base"));
            Ingredient var2 = Ingredient.fromJson(GsonHelper.getNonNull(param1, "addition"));
            return new SmithingTrimRecipe(param0, var0, var1, var2);
        }

        public SmithingTrimRecipe fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
            Ingredient var0 = Ingredient.fromNetwork(param1);
            Ingredient var1 = Ingredient.fromNetwork(param1);
            Ingredient var2 = Ingredient.fromNetwork(param1);
            return new SmithingTrimRecipe(param0, var0, var1, var2);
        }

        public void toNetwork(FriendlyByteBuf param0, SmithingTrimRecipe param1) {
            param1.template.toNetwork(param0);
            param1.base.toNetwork(param0);
            param1.addition.toNetwork(param0);
        }
    }
}
