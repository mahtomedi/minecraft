package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableReference extends LootPoolSingletonContainer {
    private final ResourceLocation name;

    private LootTableReference(ResourceLocation param0, int param1, int param2, LootItemCondition[] param3, LootItemFunction[] param4) {
        super(param1, param2, param3, param4);
        this.name = param0;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.REFERENCE;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
        LootTable var0 = param1.getLootTable(this.name);
        var0.getRandomItemsRaw(param1, param0);
    }

    @Override
    public void validate(ValidationContext param0) {
        if (param0.hasVisitedTable(this.name)) {
            param0.reportProblem("Table " + this.name + " is recursively called");
        } else {
            super.validate(param0);
            LootTable var0 = param0.resolveLootTable(this.name);
            if (var0 == null) {
                param0.reportProblem("Unknown loot table called " + this.name);
            } else {
                var0.validate(param0.enterTable("->{" + this.name + "}", this.name));
            }

        }
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new LootTableReference(param0, param1, param2, param3, param4));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference> {
        public void serializeCustom(JsonObject param0, LootTableReference param1, JsonSerializationContext param2) {
            super.serializeCustom(param0, param1, param2);
            param0.addProperty("name", param1.name.toString());
        }

        protected LootTableReference deserialize(
            JsonObject param0, JsonDeserializationContext param1, int param2, int param3, LootItemCondition[] param4, LootItemFunction[] param5
        ) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "name"));
            return new LootTableReference(var0, param2, param3, param4, param5);
        }
    }
}
