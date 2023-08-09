package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;

public record ItemPredicate(
    Optional<TagKey<Item>> tag,
    Optional<HolderSet<Item>> items,
    MinMaxBounds.Ints count,
    MinMaxBounds.Ints durability,
    List<EnchantmentPredicate> enchantments,
    List<EnchantmentPredicate> storedEnchantments,
    Optional<Holder<Potion>> potion,
    Optional<NbtPredicate> nbt
) {
    private static final Codec<HolderSet<Item>> ITEMS_CODEC = BuiltInRegistries.ITEM
        .holderByNameCodec()
        .listOf()
        .xmap(HolderSet::direct, param0 -> param0.stream().toList());
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(TagKey.codec(Registries.ITEM), "tag").forGetter(ItemPredicate::tag),
                    ExtraCodecs.strictOptionalField(ITEMS_CODEC, "items").forGetter(ItemPredicate::items),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "durability", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::durability),
                    ExtraCodecs.strictOptionalField(EnchantmentPredicate.CODEC.listOf(), "enchantments", List.of()).forGetter(ItemPredicate::enchantments),
                    ExtraCodecs.strictOptionalField(EnchantmentPredicate.CODEC.listOf(), "stored_enchantments", List.of())
                        .forGetter(ItemPredicate::storedEnchantments),
                    ExtraCodecs.strictOptionalField(BuiltInRegistries.POTION.holderByNameCodec(), "potion").forGetter(ItemPredicate::potion),
                    ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt").forGetter(ItemPredicate::nbt)
                )
                .apply(param0, ItemPredicate::new)
    );

    static Optional<ItemPredicate> of(
        Optional<TagKey<Item>> param0,
        Optional<HolderSet<Item>> param1,
        MinMaxBounds.Ints param2,
        MinMaxBounds.Ints param3,
        List<EnchantmentPredicate> param4,
        List<EnchantmentPredicate> param5,
        Optional<Holder<Potion>> param6,
        Optional<NbtPredicate> param7
    ) {
        return param0.isEmpty()
                && param1.isEmpty()
                && param2.isAny()
                && param3.isAny()
                && param4.isEmpty()
                && param5.isEmpty()
                && param6.isEmpty()
                && param7.isEmpty()
            ? Optional.empty()
            : Optional.of(new ItemPredicate(param0, param1, param2, param3, param4, param5, param6, param7));
    }

    public boolean matches(ItemStack param0) {
        if (this.tag.isPresent() && !param0.is(this.tag.get())) {
            return false;
        } else if (this.items.isPresent() && !param0.is(this.items.get())) {
            return false;
        } else if (!this.count.matches(param0.getCount())) {
            return false;
        } else if (!this.durability.isAny() && !param0.isDamageableItem()) {
            return false;
        } else if (!this.durability.matches(param0.getMaxDamage() - param0.getDamageValue())) {
            return false;
        } else if (this.nbt.isPresent() && !this.nbt.get().matches(param0)) {
            return false;
        } else {
            if (!this.enchantments.isEmpty()) {
                Map<Enchantment, Integer> var0 = EnchantmentHelper.deserializeEnchantments(param0.getEnchantmentTags());

                for(EnchantmentPredicate var1 : this.enchantments) {
                    if (!var1.containedIn(var0)) {
                        return false;
                    }
                }
            }

            if (!this.storedEnchantments.isEmpty()) {
                Map<Enchantment, Integer> var2 = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(param0));

                for(EnchantmentPredicate var3 : this.storedEnchantments) {
                    if (!var3.containedIn(var2)) {
                        return false;
                    }
                }
            }

            return !this.potion.isPresent() || this.potion.get().value() == PotionUtils.getPotion(param0);
        }
    }

    public static Optional<ItemPredicate> fromJson(@Nullable JsonElement param0) {
        return param0 != null && !param0.isJsonNull()
            ? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new))
            : Optional.empty();
    }

    public JsonElement serializeToJson() {
        return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    public static JsonElement serializeToJsonArray(List<ItemPredicate> param0) {
        return Util.getOrThrow(CODEC.listOf().encodeStart(JsonOps.INSTANCE, param0), IllegalStateException::new);
    }

    public static List<ItemPredicate> fromJsonArray(@Nullable JsonElement param0) {
        return param0 != null && !param0.isJsonNull() ? Util.getOrThrow(CODEC.listOf().parse(JsonOps.INSTANCE, param0), JsonParseException::new) : List.of();
    }

    public static class Builder {
        private final ImmutableList.Builder<EnchantmentPredicate> enchantments = ImmutableList.builder();
        private final ImmutableList.Builder<EnchantmentPredicate> storedEnchantments = ImmutableList.builder();
        private Optional<HolderSet<Item>> items = Optional.empty();
        private Optional<TagKey<Item>> tag = Optional.empty();
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Ints durability = MinMaxBounds.Ints.ANY;
        private Optional<Holder<Potion>> potion = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();

        private Builder() {
        }

        public static ItemPredicate.Builder item() {
            return new ItemPredicate.Builder();
        }

        public ItemPredicate.Builder of(ItemLike... param0) {
            this.items = Optional.of(HolderSet.direct(param0x -> param0x.asItem().builtInRegistryHolder(), param0));
            return this;
        }

        public ItemPredicate.Builder of(TagKey<Item> param0) {
            this.tag = Optional.of(param0);
            return this;
        }

        public ItemPredicate.Builder withCount(MinMaxBounds.Ints param0) {
            this.count = param0;
            return this;
        }

        public ItemPredicate.Builder hasDurability(MinMaxBounds.Ints param0) {
            this.durability = param0;
            return this;
        }

        public ItemPredicate.Builder isPotion(Potion param0) {
            this.potion = Optional.of(param0.builtInRegistryHolder());
            return this;
        }

        public ItemPredicate.Builder hasNbt(CompoundTag param0) {
            this.nbt = Optional.of(new NbtPredicate(param0));
            return this;
        }

        public ItemPredicate.Builder hasEnchantment(EnchantmentPredicate param0) {
            this.enchantments.add(param0);
            return this;
        }

        public ItemPredicate.Builder hasStoredEnchantment(EnchantmentPredicate param0) {
            this.storedEnchantments.add(param0);
            return this;
        }

        public Optional<ItemPredicate> build() {
            return ItemPredicate.of(
                this.tag, this.items, this.count, this.durability, this.enchantments.build(), this.storedEnchantments.build(), this.potion, this.nbt
            );
        }
    }
}
