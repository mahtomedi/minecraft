package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentPredicate {
    public static final EnchantmentPredicate ANY = new EnchantmentPredicate();
    private final Enchantment enchantment;
    private final MinMaxBounds.Ints level;

    public EnchantmentPredicate() {
        this.enchantment = null;
        this.level = MinMaxBounds.Ints.ANY;
    }

    public EnchantmentPredicate(@Nullable Enchantment param0, MinMaxBounds.Ints param1) {
        this.enchantment = param0;
        this.level = param1;
    }

    public boolean containedIn(Map<Enchantment, Integer> param0) {
        if (this.enchantment != null) {
            if (!param0.containsKey(this.enchantment)) {
                return false;
            }

            int var0 = param0.get(this.enchantment);
            if (this.level != null && !this.level.matches(var0)) {
                return false;
            }
        } else if (this.level != null) {
            for(Integer var1 : param0.values()) {
                if (this.level.matches(var1)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (this.enchantment != null) {
                var0.addProperty("enchantment", Registry.ENCHANTMENT.getKey(this.enchantment).toString());
            }

            var0.add("levels", this.level.serializeToJson());
            return var0;
        }
    }

    public static EnchantmentPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "enchantment");
            Enchantment var1 = null;
            if (var0.has("enchantment")) {
                ResourceLocation var2 = new ResourceLocation(GsonHelper.getAsString(var0, "enchantment"));
                var1 = Registry.ENCHANTMENT.getOptional(var2).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + var2 + "'"));
            }

            MinMaxBounds.Ints var3 = MinMaxBounds.Ints.fromJson(var0.get("levels"));
            return new EnchantmentPredicate(var1, var3);
        } else {
            return ANY;
        }
    }

    public static EnchantmentPredicate[] fromJsonArray(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonArray var0 = GsonHelper.convertToJsonArray(param0, "enchantments");
            EnchantmentPredicate[] var1 = new EnchantmentPredicate[var0.size()];

            for(int var2 = 0; var2 < var1.length; ++var2) {
                var1[var2] = fromJson(var0.get(var2));
            }

            return var1;
        } else {
            return new EnchantmentPredicate[0];
        }
    }
}
