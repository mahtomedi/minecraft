package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
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
    public static final Codec<Ingredient> CODEC = codec(true);
    public static final Codec<Ingredient> CODEC_NONEMPTY = codec(false);

    private Ingredient(Stream<? extends Ingredient.Value> param0) {
        this.values = param0.toArray(param0x -> new Ingredient.Value[param0x]);
    }

    private Ingredient(Ingredient.Value[] param0) {
        this.values = param0;
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

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    @Override
    public boolean equals(Object param0) {
        return param0 instanceof Ingredient var0 ? Arrays.equals((Object[])this.values, (Object[])var0.values) : false;
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

    private static Codec<Ingredient> codec(boolean param0) {
        Codec<Ingredient.Value[]> var0 = Codec.list(Ingredient.Value.CODEC)
            .comapFlatMap(
                param1 -> !param0 && param1.size() < 1
                        ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                        : DataResult.success(param1.toArray(new Ingredient.Value[0])),
                List::of
            );
        return ExtraCodecs.either(var0, Ingredient.Value.CODEC)
            .flatComapMap(
                param0x -> param0x.map(Ingredient::new, param0xx -> new Ingredient(new Ingredient.Value[]{param0xx})),
                param1 -> {
                    if (param1.values.length == 1) {
                        return DataResult.success(Either.right(param1.values[0]));
                    } else {
                        return param1.values.length == 0 && !param0
                            ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                            : DataResult.success(Either.left(param1.values));
                    }
                }
            );
    }

    static record ItemValue(ItemStack item) implements Ingredient.Value {
        static final Codec<Ingredient.ItemValue> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(ItemStack.SINGLE_ITEM_CODEC.fieldOf("item").forGetter(param0x -> param0x.item)).apply(param0, Ingredient.ItemValue::new)
        );

        @Override
        public boolean equals(Object param0) {
            if (!(param0 instanceof Ingredient.ItemValue)) {
                return false;
            } else {
                Ingredient.ItemValue var0 = (Ingredient.ItemValue)param0;
                return var0.item.getItem().equals(this.item.getItem()) && var0.item.getCount() == this.item.getCount();
            }
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.item);
        }
    }

    static record TagValue(TagKey<Item> tag) implements Ingredient.Value {
        static final Codec<Ingredient.TagValue> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(param0x -> param0x.tag)).apply(param0, Ingredient.TagValue::new)
        );

        @Override
        public boolean equals(Object param0) {
            return param0 instanceof Ingredient.TagValue var0 ? var0.tag.location().equals(this.tag.location()) : false;
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> var0 = Lists.newArrayList();

            for(Holder<Item> var1 : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                var0.add(new ItemStack(var1));
            }

            return var0;
        }
    }

    interface Value {
        Codec<Ingredient.Value> CODEC = ExtraCodecs.xor(Ingredient.ItemValue.CODEC, Ingredient.TagValue.CODEC)
            .xmap(param0 -> param0.map(param0x -> param0x, param0x -> param0x), param0 -> {
                if (param0 instanceof Ingredient.TagValue var0) {
                    return Either.right(var0);
                } else if (param0 instanceof Ingredient.ItemValue var1) {
                    return Either.left(var1);
                } else {
                    throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
                }
            });

        Collection<ItemStack> getItems();
    }
}
