package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemConditionalFunction {
    final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;
    final boolean append;

    SetBannerPatternFunction(LootItemCondition[] param0, List<Pair<Holder<BannerPattern>, DyeColor>> param1, boolean param2) {
        super(param0);
        this.patterns = param1;
        this.append = param2;
    }

    @Override
    protected ItemStack run(ItemStack param0, LootContext param1) {
        CompoundTag var0 = BlockItem.getBlockEntityData(param0);
        if (var0 == null) {
            var0 = new CompoundTag();
        }

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
        BlockItem.setBlockEntityData(param0, BlockEntityType.BANNER, var0);
        return param0;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static SetBannerPatternFunction.Builder setBannerPattern(boolean param0) {
        return new SetBannerPatternFunction.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetBannerPatternFunction.Builder> {
        private final ImmutableList.Builder<Pair<Holder<BannerPattern>, DyeColor>> patterns = ImmutableList.builder();
        private final boolean append;

        Builder(boolean param0) {
            this.append = param0;
        }

        protected SetBannerPatternFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
        }

        public SetBannerPatternFunction.Builder addPattern(ResourceKey<BannerPattern> param0, DyeColor param1) {
            return this.addPattern(Registry.BANNER_PATTERN.getHolderOrThrow(param0), param1);
        }

        public SetBannerPatternFunction.Builder addPattern(Holder<BannerPattern> param0, DyeColor param1) {
            this.patterns.add(Pair.of(param0, param1));
            return this;
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetBannerPatternFunction> {
        public void serialize(JsonObject param0, SetBannerPatternFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            JsonArray var0 = new JsonArray();
            param1.patterns
                .forEach(
                    param1x -> {
                        JsonObject var0x = new JsonObject();
                        var0x.addProperty(
                            "pattern",
                            param1x.getFirst()
                                .unwrapKey()
                                .orElseThrow(() -> new JsonSyntaxException("Unknown pattern: " + param1x.getFirst()))
                                .location()
                                .toString()
                        );
                        var0x.addProperty("color", param1x.getSecond().getName());
                        var0.add(var0x);
                    }
                );
            param0.add("patterns", var0);
            param0.addProperty("append", param1.append);
        }

        public SetBannerPatternFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            ImmutableList.Builder<Pair<Holder<BannerPattern>, DyeColor>> var0 = ImmutableList.builder();
            JsonArray var1 = GsonHelper.getAsJsonArray(param0, "patterns");

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                JsonObject var3 = GsonHelper.convertToJsonObject(var1.get(var2), "pattern[" + var2 + "]");
                String var4 = GsonHelper.getAsString(var3, "pattern");
                Optional<Holder<BannerPattern>> var5 = Registry.BANNER_PATTERN
                    .getHolder(ResourceKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(var4)));
                if (var5.isEmpty()) {
                    throw new JsonSyntaxException("Unknown pattern: " + var4);
                }

                String var6 = GsonHelper.getAsString(var3, "color");
                DyeColor var7 = DyeColor.byName(var6, null);
                if (var7 == null) {
                    throw new JsonSyntaxException("Unknown color: " + var6);
                }

                var0.add(Pair.of(var5.get(), var7));
            }

            boolean var8 = GsonHelper.getAsBoolean(param0, "append");
            return new SetBannerPatternFunction(param2, var0.build(), var8);
        }
    }
}
