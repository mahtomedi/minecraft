package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("player_generates_container_loot");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    protected LootTableTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "loot_table"));
        return new LootTableTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, ResourceLocation param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ResourceLocation lootTable;

        public TriggerInstance(EntityPredicate.Composite param0, ResourceLocation param1) {
            super(LootTableTrigger.ID, param0);
            this.lootTable = param1;
        }

        public static LootTableTrigger.TriggerInstance lootTableUsed(ResourceLocation param0) {
            return new LootTableTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0);
        }

        public boolean matches(ResourceLocation param0) {
            return this.lootTable.equals(param0);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.addProperty("loot_table", this.lootTable.toString());
            return var0;
        }
    }
}
