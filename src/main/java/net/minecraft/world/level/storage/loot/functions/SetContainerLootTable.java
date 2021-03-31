package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
    private final ResourceLocation name;
    private final long seed;

    private SetContainerLootTable(LootItemCondition[] param0, ResourceLocation param1, long param2) {
        super(param0);
        this.name = param1;
        this.seed = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.isEmpty()) {
            return param0;
        } else {
            CompoundTag var0 = new CompoundTag();
            var0.putString("LootTable", this.name.toString());
            if (this.seed != 0L) {
                var0.putLong("LootTableSeed", this.seed);
            }

            param0.getOrCreateTag().put("BlockEntityTag", var0);
            return param0;
        }
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

    public static LootItemConditionalFunction.Builder<?> withLootTable(ResourceLocation param0) {
        return simpleBuilder(param1 -> new SetContainerLootTable(param1, param0, 0L));
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(ResourceLocation param0, long param1) {
        return simpleBuilder(param2 -> new SetContainerLootTable(param2, param0, param1));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
        public void serialize(JsonObject param0, SetContainerLootTable param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("name", param1.name.toString());
            if (param1.seed != 0L) {
                param0.addProperty("seed", param1.seed);
            }

        }

        public SetContainerLootTable deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "name"));
            long var1 = GsonHelper.getAsLong(param0, "seed", 0L);
            return new SetContainerLootTable(param2, var0, var1);
        }
    }
}
