package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
    protected LootTableTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "loot_table"));
        return new LootTableTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, ResourceLocation param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ResourceLocation lootTable;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, ResourceLocation param1) {
            super(param0);
            this.lootTable = param1;
        }

        public static Criterion<LootTableTrigger.TriggerInstance> lootTableUsed(ResourceLocation param0) {
            return CriteriaTriggers.GENERATE_LOOT.createCriterion(new LootTableTrigger.TriggerInstance(Optional.empty(), param0));
        }

        public boolean matches(ResourceLocation param0) {
            return this.lootTable.equals(param0);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            var0.addProperty("loot_table", this.lootTable.toString());
            return var0;
        }
    }
}
