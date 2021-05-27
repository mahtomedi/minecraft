package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("lightning_strike");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public LightningStrikeTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        EntityPredicate.Composite var0 = EntityPredicate.Composite.fromJson(param0, "lightning", param2);
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "bystander", param2);
        return new LightningStrikeTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, LightningBolt param1, List<Entity> param2) {
        List<LootContext> var0 = param2.stream().map(param1x -> EntityPredicate.createContext(param0, param1x)).collect(Collectors.toList());
        LootContext var1 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param2x -> param2x.matches(var1, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite lightning;
        private final EntityPredicate.Composite bystander;

        public TriggerInstance(EntityPredicate.Composite param0, EntityPredicate.Composite param1, EntityPredicate.Composite param2) {
            super(LightningStrikeTrigger.ID, param0);
            this.lightning = param1;
            this.bystander = param2;
        }

        public static LightningStrikeTrigger.TriggerInstance lighthingStrike(EntityPredicate param0, EntityPredicate param1) {
            return new LightningStrikeTrigger.TriggerInstance(
                EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0), EntityPredicate.Composite.wrap(param1)
            );
        }

        public boolean matches(LootContext param0, List<LootContext> param1) {
            if (!this.lightning.matches(param0)) {
                return false;
            } else {
                return this.bystander == EntityPredicate.Composite.ANY || !param1.stream().noneMatch(this.bystander::matches);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("lightning", this.lightning.toJson(param0));
            var0.add("bystander", this.bystander.toJson(param0));
            return var0;
        }
    }
}
