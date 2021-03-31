package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
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
    private final Tag<Item> tag;
    @Nullable
    private final Item item;
    private final MinMaxBounds.Ints count;
    private final MinMaxBounds.Ints durability;
    private final EnchantmentPredicate[] enchantments;
    private final EnchantmentPredicate[] storedEnchantments;
    @Nullable
    private final Potion potion;
    private final NbtPredicate nbt;

    public ItemPredicate() {
        this.tag = null;
        this.item = null;
        this.potion = null;
        this.count = MinMaxBounds.Ints.ANY;
        this.durability = MinMaxBounds.Ints.ANY;
        this.enchantments = EnchantmentPredicate.NONE;
        this.storedEnchantments = EnchantmentPredicate.NONE;
        this.nbt = NbtPredicate.ANY;
    }

    public ItemPredicate(
        @Nullable Tag<Item> param0,
        @Nullable Item param1,
        MinMaxBounds.Ints param2,
        MinMaxBounds.Ints param3,
        EnchantmentPredicate[] param4,
        EnchantmentPredicate[] param5,
        @Nullable Potion param6,
        NbtPredicate param7
    ) {
        this.tag = param0;
        this.item = param1;
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
        } else if (this.item != null && !param0.is(this.item)) {
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
                Item var4 = null;
                if (var0.has("item")) {
                    ResourceLocation var5 = new ResourceLocation(GsonHelper.getAsString(var0, "item"));
                    var4 = Registry.ITEM.getOptional(var5).orElseThrow(() -> new JsonSyntaxException("Unknown item id '" + var5 + "'"));
                }

                Tag<Item> var6 = null;
                if (var0.has("tag")) {
                    ResourceLocation var7 = new ResourceLocation(GsonHelper.getAsString(var0, "tag"));
                    var6 = SerializationTags.getInstance()
                        .getTagOrThrow(Registry.ITEM_REGISTRY, var7, param0x -> new JsonSyntaxException("Unknown item tag '" + param0x + "'"));
                }

                Potion var8 = null;
                if (var0.has("potion")) {
                    ResourceLocation var9 = new ResourceLocation(GsonHelper.getAsString(var0, "potion"));
                    var8 = Registry.POTION.getOptional(var9).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + var9 + "'"));
                }

                EnchantmentPredicate[] var10 = EnchantmentPredicate.fromJsonArray(var0.get("enchantments"));
                EnchantmentPredicate[] var11 = EnchantmentPredicate.fromJsonArray(var0.get("stored_enchantments"));
                return new ItemPredicate(var6, var4, var1, var2, var10, var11, var8, var3);
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
            if (this.item != null) {
                var0.addProperty("item", Registry.ITEM.getKey(this.item).toString());
            }

            if (this.tag != null) {
                var0.addProperty(
                    "tag",
                    SerializationTags.getInstance()
                        .getIdOrThrow(Registry.ITEM_REGISTRY, this.tag, () -> new IllegalStateException("Unknown item tag"))
                        .toString()
                );
            }

            var0.add("count", this.count.serializeToJson());
            var0.add("durability", this.durability.serializeToJson());
            var0.add("nbt", this.nbt.serializeToJson());
            if (this.enchantments.length > 0) {
                JsonArray var1 = new JsonArray();

                for(EnchantmentPredicate var2 : this.enchantments) {
                    var1.add(var2.serializeToJson());
                }

                var0.add("enchantments", var1);
            }

            if (this.storedEnchantments.length > 0) {
                JsonArray var3 = new JsonArray();

                for(EnchantmentPredicate var4 : this.storedEnchantments) {
                    var3.add(var4.serializeToJson());
                }

                var0.add("stored_enchantments", var3);
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
        private Item item;
        @Nullable
        private Tag<Item> tag;
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

        public ItemPredicate.Builder of(ItemLike param0) {
            this.item = param0.asItem();
            return this;
        }

        public ItemPredicate.Builder of(Tag<Item> param0) {
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
                this.item,
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
