package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemConditionalFunction {
    private final List<Pair<BannerPattern, DyeColor>> patterns;
    private final boolean append;

    private SetBannerPatternFunction(LootItemCondition[] param0, List<Pair<BannerPattern, DyeColor>> param1, boolean param2) {
        super(param0);
        this.patterns = param1;
        this.append = param2;
    }

    @Override
    protected ItemStack run(ItemStack param0, LootContext param1) {
        CompoundTag var0 = param0.getOrCreateTagElement("BlockEntityTag");
        BannerPattern.Builder var1 = new BannerPattern.Builder();
        this.patterns.forEach(var1::addPattern);
        ListTag var2 = var1.toListTag();
        ListTag var3;
        if (this.append) {
            var3 = var0.getList("Patterns", 10).copy();
            var3.addAll(var2);
        } else {
            var3 = var2;
        }

        var0.put("Patterns", var3);
        return param0;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetBannerPatternFunction> {
        public void serialize(JsonObject param0, SetBannerPatternFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            JsonArray var0 = new JsonArray();
            param1.patterns.forEach(param1x -> {
                JsonObject var0x = new JsonObject();
                var0x.addProperty("pattern", param1x.getFirst().getFilename());
                var0x.addProperty("color", param1x.getSecond().getName());
                var0.add(var0x);
            });
            param0.add("patterns", var0);
            param0.addProperty("append", param1.append);
        }

        public SetBannerPatternFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            ImmutableList.Builder<Pair<BannerPattern, DyeColor>> var0 = ImmutableList.builder();
            JsonArray var1 = GsonHelper.getAsJsonArray(param0, "patterns");

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                JsonObject var3 = GsonHelper.convertToJsonObject(var1.get(var2), "pattern[" + var2 + "]");
                String var4 = GsonHelper.getAsString(var3, "pattern");
                BannerPattern var5 = BannerPattern.byFilename(var4);
                if (var5 == null) {
                    throw new JsonSyntaxException("Unknown pattern: " + var4);
                }

                String var6 = GsonHelper.getAsString(var3, "color");
                DyeColor var7 = DyeColor.byName(var6, null);
                if (var7 == null) {
                    throw new JsonSyntaxException("Unknown color: " + var6);
                }

                var0.add(Pair.of(var5, var7));
            }

            boolean var8 = GsonHelper.getAsBoolean(param0, "append");
            return new SetBannerPatternFunction(param2, var0.build(), var8);
        }
    }
}