package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {
    static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> param0, Consumer<ItemStack> param1, LootContext param2) {
        return param3 -> param1.accept(param0.apply(param3, param2));
    }

    public interface Builder {
        LootItemFunction build();
    }

    public abstract static class Serializer<T extends LootItemFunction> {
        private final ResourceLocation name;
        private final Class<T> clazz;

        protected Serializer(ResourceLocation param0, Class<T> param1) {
            this.name = param0;
            this.clazz = param1;
        }

        public ResourceLocation getName() {
            return this.name;
        }

        public Class<T> getFunctionClass() {
            return this.clazz;
        }

        public abstract void serialize(JsonObject var1, T var2, JsonSerializationContext var3);

        public abstract T deserialize(JsonObject var1, JsonDeserializationContext var2);
    }
}
