package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;

public class ItemPredicate {
    public static final ItemPredicate ANY = new ItemPredicate();
    @Nullable
    private final TagKey<Item> tag;
    @Nullable
    private final Set<Item> items;
    private final MinMaxBounds.Ints count;
    private final MinMaxBounds.Ints durability;
    private final EnchantmentPredicate[] enchantments;
    private final EnchantmentPredicate[] storedEnchantments;
    @Nullable
    private final Potion potion;
    private final NbtPredicate nbt;

    public ItemPredicate() {
        this.tag = null;
        this.items = null;
        this.potion = null;
        this.count = MinMaxBounds.Ints.ANY;
        this.durability = MinMaxBounds.Ints.ANY;
        this.enchantments = EnchantmentPredicate.NONE;
        this.storedEnchantments = EnchantmentPredicate.NONE;
        this.nbt = NbtPredicate.ANY;
    }

    public ItemPredicate(
        @Nullable TagKey<Item> param0,
        @Nullable Set<Item> param1,
        MinMaxBounds.Ints param2,
        MinMaxBounds.Ints param3,
        EnchantmentPredicate[] param4,
        EnchantmentPredicate[] param5,
        @Nullable Potion param6,
        NbtPredicate param7
    ) {
        this.tag = param0;
        this.items = param1;
        this.count = param2;
        this.durability = param3;
        this.enchantments = param4;
        this.storedEnchantments = param5;
        this.potion = param6;
        this.nbt = param7;
    }

    public boolean matches(ItemStack param0) {
        if (this == ANY) {
            return true;
        } else if (this.tag != null && !param0.is(this.tag)) {
            return false;
        } else if (this.items != null && !this.items.contains(param0.getItem())) {
            return false;
        } else if (!this.count.matches(param0.getCount())) {
            return false;
        } else if (!this.durability.isAny() && !param0.isDamageableItem()) {
            return false;
        } else if (!this.durability.matches(param0.getMaxDamage() - param0.getDamageValue())) {
            return false;
        } else if (!this.nbt.matches(param0)) {
            return false;
        } else {
            if (this.enchantments.length > 0) {
                Map<Enchantment, Integer> var0 = EnchantmentHelper.deserializeEnchantments(param0.getEnchantmentTags());

                for(EnchantmentPredicate var1 : this.enchantments) {
                    if (!var1.containedIn(var0)) {
                        return false;
                    }
                }
            }

            if (this.storedEnchantments.length > 0) {
                Map<Enchantment, Integer> var2 = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(param0));

                for(EnchantmentPredicate var3 : this.storedEnchantments) {
                    if (!var3.containedIn(var2)) {
                        return false;
                    }
                }
            }

            Potion var4 = PotionUtils.getPotion(param0);
            return this.potion == null || this.potion == var4;
        }
    }

    public static ItemPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "item");
            MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(var0.get("count"));
            MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(var0.get("durability"));
            if (var0.has("data")) {
                throw new JsonParseException("Disallowed data tag found");
            } else {
                NbtPredicate var3 = NbtPredicate.fromJson(var0.get("nbt"));
                Set<Item> var4 = null;
                JsonArray var5 = GsonHelper.getAsJsonArray(var0, "items", null);
                if (var5 != null) {
                    ImmutableSet.Builder<Item> var6 = ImmutableSet.builder();

                    for(JsonElement var7 : var5) {
                        ResourceLocation var8 = new ResourceLocation(GsonHelper.convertToString(var7, "item"));
                        var6.add(Registry.ITEM.getOptional(var8).orElseThrow(() -> new JsonSyntaxException("Unknown item id '" + var8 + "'")));
                    }

                    var4 = var6.build();
                }

                TagKey<Item> var9 = null;
                if (var0.has("tag")) {
                    ResourceLocation var10 = new ResourceLocation(GsonHelper.getAsString(var0, "tag"));
                    var9 = TagKey.create(Registry.ITEM_REGISTRY, var10);
                }

                Potion var11 = null;
                if (var0.has("potion")) {
                    ResourceLocation var12 = new ResourceLocation(GsonHelper.getAsString(var0, "potion"));
                    var11 = Registry.POTION.getOptional(var12).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + var12 + "'"));
                }

                EnchantmentPredicate[] var13 = EnchantmentPredicate.fromJsonArray(var0.get("enchantments"));
                EnchantmentPredicate[] var14 = EnchantmentPredicate.fromJsonArray(var0.get("stored_enchantments"));
                return new ItemPredicate(var9, var4, var1, var2, var13, var14, var11, var3);
            }
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (this.items != null) {
                JsonArray var1 = new JsonArray();

                for(Item var2 : this.items) {
                    var1.add(Registry.ITEM.getKey(var2).toString());
                }

                var0.add("items", var1);
            }

            if (this.tag != null) {
                var0.addProperty("tag", this.tag.location().toString());
            }

            var0.add("count", this.count.serializeToJson());
            var0.add("durability", this.durability.serializeToJson());
            var0.add("nbt", this.nbt.serializeToJson());
            if (this.enchantments.length > 0) {
                JsonArray var3 = new JsonArray();

                for(EnchantmentPredicate var4 : this.enchantments) {
                    var3.add(var4.serializeToJson());
                }

                var0.add("enchantments", var3);
            }

            if (this.storedEnchantments.length > 0) {
                JsonArray var5 = new JsonArray();

                for(EnchantmentPredicate var6 : this.storedEnchantments) {
                    var5.add(var6.serializeToJson());
                }

                var0.add("stored_enchantments", var5);
            }

            if (this.potion != null) {
                var0.addProperty("potion", Registry.POTION.getKey(this.potion).toString());
            }

            return var0;
        }
    }

    public static ItemPredicate[] fromJsonArray(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonArray var0 = GsonHelper.convertToJsonArray(param0, "items");
            ItemPredicate[] var1 = new ItemPredicate[var0.size()];

            for(int var2 = 0; var2 < var1.length; ++var2) {
                var1[var2] = fromJson(var0.get(var2));
            }

            return var1;
        } else {
            return new ItemPredicate[0];
        }
    }

    public static class Builder {
        private final List<EnchantmentPredicate> enchantments = Lists.newArrayList();
        private final List<EnchantmentPredicate> storedEnchantments = Lists.newArrayList();
        @Nullable
        private Set<Item> items;
        @Nullable
        private TagKey<Item> tag;
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Ints durability = MinMaxBounds.Ints.ANY;
        @Nullable
        private Potion potion;
        private NbtPredicate nbt = NbtPredicate.ANY;

        private Builder() {
        }

        public static ItemPredicate.Builder item() {
            return new ItemPredicate.Builder();
        }

        public ItemPredicate.Builder of(ItemLike... param0) {
            this.items = Stream.of(param0).map(ItemLike::asItem).collect(ImmutableSet.toImmutableSet());
            return this;
        }

        public ItemPredicate.Builder of(TagKey<Item> param0) {
            this.tag = param0;
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
            this.potion = param0;
            return this;
        }

        public ItemPredicate.Builder hasNbt(CompoundTag param0) {
            this.nbt = new NbtPredicate(param0);
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

        public ItemPredicate build() {
            return new ItemPredicate(
                this.tag,
                this.items,
                this.count,
                this.durability,
                this.enchantments.toArray(EnchantmentPredicate.NONE),
                this.storedEnchantments.toArray(EnchantmentPredicate.NONE),
                this.potion,
                this.nbt
            );
        }
    }
}
