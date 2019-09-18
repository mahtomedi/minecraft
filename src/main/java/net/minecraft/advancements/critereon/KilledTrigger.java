package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
    private final ResourceLocation id;

    public KilledTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public KilledTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        return new KilledTrigger.TriggerInstance(
            this.id, EntityPredicate.fromJson(param0.get("entity")), DamageSourcePredicate.fromJson(param0.get("killing_blow"))
        );
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2) {
        this.trigger(param0.getAdvancements(), param3 -> param3.matches(param0, param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate entityPredicate;
        private final DamageSourcePredicate killingBlow;

        public TriggerInstance(ResourceLocation param0, EntityPredicate param1, DamageSourcePredicate param2) {
            super(param0);
            this.entityPredicate = param1;
            this.killingBlow = param2;
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, param0.build(), DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity() {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, param0.build(), param1.build());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer() {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Entity param1, DamageSource param2) {
            return !this.killingBlow.matches(param0, param2) ? false : this.entityPredicate.matches(param0, param1);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("entity", this.entityPredicate.serializeToJson());
            var0.add("killing_blow", this.killingBlow.serializeToJson());
            return var0;
        }
    }
}
