package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot extends LootPoolSingletonContainer {
    public static final ResourceLocation TYPE = new ResourceLocation("dynamic");
    private final ResourceLocation name;

    private DynamicLoot(ResourceLocation param0, int param1, int param2, LootItemCondition[] param3, LootItemFunction[] param4) {
        super(param1, param2, param3, param4);
        this.name = param0;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
        param1.addDynamicDrops(this.name, param0);
    }

    public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new DynamicLoot(param0, param1, param2, param3, param4));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
        public Serializer() {
            super(new ResourceLocation("dynamic"), DynamicLoot.class);
        }

        public void serialize(JsonObject param0, DynamicLoot param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("name", param1.name.toString());
        }

        protected DynamicLoot deserialize(
            JsonObject param0, JsonDeserializationContext param1, int param2, int param3, LootItemCondition[] param4, LootItemFunction[] param5
        ) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "name"));
            return new DynamicLoot(var0, param2, param3, param4, param5);
        }
    }
}
