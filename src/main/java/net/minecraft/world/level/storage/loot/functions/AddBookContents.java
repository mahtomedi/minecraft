package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AddBookContents extends LootItemConditionalFunction {
    private final AddBookContents.ContentProvider provider;

    protected AddBookContents(LootItemCondition[] param0, AddBookContents.ContentProvider param1) {
        super(param0);
        this.provider = param1;
    }

    public static LootItemConditionalFunction.Builder<?> addContents(AddBookContents.ContentProvider param0) {
        return simpleBuilder(param1 -> new AddBookContents(param1, param0));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_POS);
    }

    @Override
    protected ItemStack run(ItemStack param0, LootContext param1) {
        String var0 = this.provider.apply(param1.getRandom(), param1.getParam(LootContextParams.BLOCK_POS));
        CompoundTag var1 = param0.getOrCreateTag();
        ListTag var2 = new ListTag();
        var2.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(var0))));
        var1.put("pages", var2);
        var1.putString("author", ChatFormatting.OBFUSCATED + "Deepest Lore");
        var1.putString("title", "Orders");
        return param0;
    }

    public static enum ContentProvider implements BiFunction<Random, BlockPos, String> {
        ORDERS("orders") {
            private final String[] verb = new String[]{
                "capture", "destroy", "cut", "find", "obliterate", "discover", "observe", "reinforce", "build", "deploy", "restore", "deliver"
            };
            private final String[] object = new String[]{
                "cheese",
                "footprints",
                "bananas",
                "toeshoes",
                "mah brewskis",
                "bicycle build for two",
                "my canoe",
                "Minecraft 3D: Lost Floppies",
                "content",
                "those pesky modders",
                "license-free mappings",
                "those VHS",
                "pre-mixed coctails",
                "quasi-connectivity"
            };

            public String apply(Random param0, BlockPos param1) {
                return this.verb[param0.nextInt(this.verb.length)] + " " + ChatFormatting.OBFUSCATED + this.object[param0.nextInt(this.object.length)];
            }
        };

        private final String name;

        private ContentProvider(String param0) {
            this.name = param0;
        }

        public static AddBookContents.ContentProvider getByName(String param0) {
            for(AddBookContents.ContentProvider var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            throw new IllegalArgumentException("Invalid content source " + param0);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AddBookContents> {
        public Serializer() {
            super(new ResourceLocation("add_book_contents"), AddBookContents.class);
        }

        public void serialize(JsonObject param0, AddBookContents param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("provider", param1.provider.name);
        }

        public AddBookContents deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            AddBookContents.ContentProvider var0 = AddBookContents.ContentProvider.getByName(GsonHelper.getAsString(param0, "provider"));
            return new AddBookContents(param2, var0);
        }
    }
}
