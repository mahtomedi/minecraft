package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
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
    public void validate(
        LootTableProblemCollector param0, Function<ResourceLocation, LootTable> param1, Set<ResourceLocation> param2, LootContextParamSet param3
    ) {
        if (param2.contains(this.name)) {
            param0.reportProblem("Table " + this.name + " is recursively called");
        } else {
            super.validate(param0, param1, param2, param3);
            LootTable var0 = param1.apply(this.name);
            if (var0 == null) {
                param0.reportProblem("Unknown loot table called " + this.name);
            } else {
                Set<ResourceLocation> var1 = ImmutableSet.<ResourceLocation>builder().addAll(param2).add(this.name).build();
                var0.validate(param0.forChild("->{" + this.name + "}"), param1, var1, param3);
            }

        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
        protected Serializer() {
            super(new ResourceLocation("set_loot_table"), SetContainerLootTable.class);
        }

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
