package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe {
    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipe(String param0, CraftingBookCategory param1, ShapedRecipePattern param2, ItemStack param3, boolean param4) {
        this.group = param0;
        this.category = param1;
        this.pattern = param2;
        this.result = param3;
        this.showNotification = param4;
    }

    public ShapedRecipe(String param0, CraftingBookCategory param1, ShapedRecipePattern param2, ItemStack param3) {
        this(param0, param1, param2, param3, true);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
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
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 >= this.pattern.width() && param1 >= this.pattern.height();
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        return this.pattern.matches(param0);
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        return this.getResultItem(param1).copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> var0 = this.getIngredients();
        return var0.isEmpty() || var0.stream().filter(param0 -> !param0.isEmpty()).anyMatch(param0 -> param0.getItems().length == 0);
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipe> {
        public static final Codec<ShapedRecipe> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(param0x -> param0x.group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(param0x -> param0x.category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(param0x -> param0x.pattern),
                        ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(param0x -> param0x.result),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(param0x -> param0x.showNotification)
                    )
                    .apply(param0, ShapedRecipe::new)
        );

        @Override
        public Codec<ShapedRecipe> codec() {
            return CODEC;
        }

        public ShapedRecipe fromNetwork(FriendlyByteBuf param0) {
            String var0 = param0.readUtf();
            CraftingBookCategory var1 = param0.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern var2 = ShapedRecipePattern.fromNetwork(param0);
            ItemStack var3 = param0.readItem();
            boolean var4 = param0.readBoolean();
            return new ShapedRecipe(var0, var1, var2, var3, var4);
        }

        public void toNetwork(FriendlyByteBuf param0, ShapedRecipe param1) {
            param0.writeUtf(param1.group);
            param0.writeEnum(param1.category);
            param1.pattern.toNetwork(param0);
            param0.writeItem(param1.result);
            param0.writeBoolean(param1.showNotification);
        }
    }
}
