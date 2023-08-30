package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;

    public ShapelessRecipe(String param0, CraftingBookCategory param1, ItemStack param2, NonNullList<Ingredient> param3) {
        this.group = param0;
        this.category = param1;
        this.result = param2;
        this.ingredients = param3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess param0) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        StackedContents var0 = new StackedContents();
        int var1 = 0;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                ++var1;
                var0.accountStack(var3, 1);
            }
        }

        return var1 == this.ingredients.size() && var0.canCraft(this, null);
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= this.ingredients.size();
    }

    public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
        private static final Codec<ShapelessRecipe> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(param0x -> param0x.group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(param0x -> param0x.category),
                        CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(param0x -> param0x.result),
                        Ingredient.CODEC_NONEMPTY
                            .listOf()
                            .fieldOf("ingredients")
                            .flatXmap(
                                param0x -> {
                                    Ingredient[] var0x = param0x.stream().filter(param0xx -> !param0xx.isEmpty()).toArray(param0xx -> new Ingredient[param0xx]);
                                    if (var0x.length == 0) {
                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                    } else {
                                        return var0x.length > 9
                                            ? DataResult.error(() -> "Too many ingredients for shapeless recipe")
                                            : DataResult.success(NonNullList.of(Ingredient.EMPTY, var0x));
                                    }
                                },
                                DataResult::success
                            )
                            .forGetter(param0x -> param0x.ingredients)
                    )
                    .apply(param0, ShapelessRecipe::new)
        );

        @Override
        public Codec<ShapelessRecipe> codec() {
            return CODEC;
        }

        public ShapelessRecipe fromNetwork(FriendlyByteBuf param0) {
            String var0 = param0.readUtf();
            CraftingBookCategory var1 = param0.readEnum(CraftingBookCategory.class);
            int var2 = param0.readVarInt();
            NonNullList<Ingredient> var3 = NonNullList.withSize(var2, Ingredient.EMPTY);

            for(int var4 = 0; var4 < var3.size(); ++var4) {
                var3.set(var4, Ingredient.fromNetwork(param0));
            }

            ItemStack var5 = param0.readItem();
            return new ShapelessRecipe(var0, var1, var5, var3);
        }

        public void toNetwork(FriendlyByteBuf param0, ShapelessRecipe param1) {
            param0.writeUtf(param1.group);
            param0.writeEnum(param1.category);
            param0.writeVarInt(param1.ingredients.size());

            for(Ingredient var0 : param1.ingredients) {
                var0.toNetwork(param0);
            }

            param0.writeItem(param1.result);
        }
    }
}
