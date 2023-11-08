package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
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
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;

    public SmithingTrimRecipe(Ingredient param0, Ingredient param1, Ingredient param2) {
        this.template = param0;
        this.base = param1;
        this.addition = param2;
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
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
        private static final Codec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Ingredient.CODEC.fieldOf("template").forGetter(param0x -> param0x.template),
                        Ingredient.CODEC.fieldOf("base").forGetter(param0x -> param0x.base),
                        Ingredient.CODEC.fieldOf("addition").forGetter(param0x -> param0x.addition)
                    )
                    .apply(param0, SmithingTrimRecipe::new)
        );

        @Override
        public Codec<SmithingTrimRecipe> codec() {
            return CODEC;
        }

        public SmithingTrimRecipe fromNetwork(FriendlyByteBuf param0) {
            Ingredient var0 = Ingredient.fromNetwork(param0);
            Ingredient var1 = Ingredient.fromNetwork(param0);
            Ingredient var2 = Ingredient.fromNetwork(param0);
            return new SmithingTrimRecipe(var0, var1, var2);
        }

        public void toNetwork(FriendlyByteBuf param0, SmithingTrimRecipe param1) {
            param1.template.toNetwork(param0);
            param1.base.toNetwork(param0);
            param1.addition.toNetwork(param0);
        }
    }
}
