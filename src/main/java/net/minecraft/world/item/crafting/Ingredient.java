package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class Ingredient implements Predicate<ItemStack> {
    public static final Ingredient EMPTY = new Ingredient(Stream.empty());
    private final Ingredient.Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    @Nullable
    private IntList stackingIds;

    private Ingredient(Stream<? extends Ingredient.Value> param0) {
        this.values = param0.toArray(param0x -> new Ingredient.Value[param0x]);
    }

    public ItemStack[] getItems() {
        if (this.itemStacks == null) {
            this.itemStacks = Arrays.stream(this.values).flatMap(param0 -> param0.getItems().stream()).distinct().toArray(param0 -> new ItemStack[param0]);
        }

        return this.itemStacks;
    }

    public boolean test(@Nullable ItemStack param0) {
        if (param0 == null) {
            return false;
        } else if (this.isEmpty()) {
            return param0.isEmpty();
        } else {
            for(ItemStack var0 : this.getItems()) {
                if (var0.is(param0.getItem())) {
                    return true;
                }
            }

            return false;
        }
    }

    public IntList getStackingIds() {
        if (this.stackingIds == null) {
            ItemStack[] var0 = this.getItems();
            this.stackingIds = new IntArrayList(var0.length);

            for(ItemStack var1 : var0) {
                this.stackingIds.add(StackedContents.getStackingIndex(var1));
            }

            this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return this.stackingIds;
    }

    public void toNetwork(FriendlyByteBuf param0) {
        param0.writeCollection(Arrays.asList(this.getItems()), FriendlyByteBuf::writeItem);
    }

    public JsonElement toJson() {
        if (this.values.length == 1) {
            return this.values[0].serialize();
        } else {
            JsonArray var0 = new JsonArray();

            for(Ingredient.Value var1 : this.values) {
                var0.add(var1.serialize());
            }

            return var0;
        }
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    private static Ingredient fromValues(Stream<? extends Ingredient.Value> param0) {
        Ingredient var0 = new Ingredient(param0);
        return var0.isEmpty() ? EMPTY : var0;
    }

    public static Ingredient of() {
        return EMPTY;
    }

    public static Ingredient of(ItemLike... param0) {
        return of(Arrays.stream(param0).map(ItemStack::new));
    }

    public static Ingredient of(ItemStack... param0) {
        return of(Arrays.stream(param0));
    }

    public static Ingredient of(Stream<ItemStack> param0) {
        return fromValues(param0.filter(param0x -> !param0x.isEmpty()).map(Ingredient.ItemValue::new));
    }

    public static Ingredient of(TagKey<Item> param0) {
        return fromValues(Stream.of(new Ingredient.TagValue(param0)));
    }

    public static Ingredient fromNetwork(FriendlyByteBuf param0) {
        return fromValues(param0.<ItemStack>readList(FriendlyByteBuf::readItem).stream().map(Ingredient.ItemValue::new));
    }

    public static Ingredient fromJson(@Nullable JsonElement param0) {
        if (param0 == null || param0.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        } else if (param0.isJsonObject()) {
            return fromValues(Stream.of(valueFromJson(param0.getAsJsonObject())));
        } else if (param0.isJsonArray()) {
            JsonArray var0 = param0.getAsJsonArray();
            if (var0.size() == 0) {
                throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
            } else {
                return fromValues(
                    StreamSupport.stream(var0.spliterator(), false).map(param0x -> valueFromJson(GsonHelper.convertToJsonObject(param0x, "item")))
                );
            }
        } else {
            throw new JsonSyntaxException("Expected item to be object or array of objects");
        }
    }

    private static Ingredient.Value valueFromJson(JsonObject param0) {
        if (param0.has("item") && param0.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (param0.has("item")) {
            Item var0 = ShapedRecipe.itemFromJson(param0);
            return new Ingredient.ItemValue(new ItemStack(var0));
        } else if (param0.has("tag")) {
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(param0, "tag"));
            TagKey<Item> var2 = TagKey.create(Registries.ITEM, var1);
            return new Ingredient.TagValue(var2);
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }

    static class ItemValue implements Ingredient.Value {
        private final ItemStack item;

        ItemValue(ItemStack param0) {
            this.item = param0;
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.item);
        }

        @Override
        public JsonObject serialize() {
            JsonObject var0 = new JsonObject();
            var0.addProperty("item", BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString());
            return var0;
        }
    }

    static class TagValue implements Ingredient.Value {
        private final TagKey<Item> tag;

        TagValue(TagKey<Item> param0) {
            this.tag = param0;
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> var0 = Lists.newArrayList();

            for(Holder<Item> var1 : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                var0.add(new ItemStack(var1));
            }

            return var0;
        }

        @Override
        public JsonObject serialize() {
            JsonObject var0 = new JsonObject();
            var0.addProperty("tag", this.tag.location().toString());
            return var0;
        }
    }

    interface Value {
        Collection<ItemStack> getItems();

        JsonObject serialize();
    }
}
