package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
    final ResourceLocation id;

    public KilledTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public KilledTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        return new KilledTrigger.TriggerInstance(
            this.id, param1, EntityPredicate.fromJson(param0, "entity", param2), DamageSourcePredicate.fromJson(param0.get("killing_blow"))
        );
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param3 -> param3.matches(param0, var0, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> entityPredicate;
        private final Optional<DamageSourcePredicate> killingBlow;

        public TriggerInstance(
            ResourceLocation param0, Optional<ContextAwarePredicate> param1, Optional<ContextAwarePredicate> param2, Optional<DamageSourcePredicate> param3
        ) {
            super(param0, param1);
            this.entityPredicate = param2;
            this.killingBlow = param3;
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(Optional<EntityPredicate> param0) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(param0), Optional.empty());
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(param0), Optional.empty());
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity() {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(Optional<EntityPredicate> param0, Optional<DamageSourcePredicate> param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(param0), param1);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0, Optional<DamageSourcePredicate> param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(param0), param1);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(Optional<EntityPredicate> param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(param0), param1.build());
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, Optional.empty(), EntityPredicate.wrap(param0), param1.build());
        }

        public static KilledTrigger.TriggerInstance playerKilledEntityNearSculkCatalyst() {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.id, Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(Optional<EntityPredicate> param0) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(param0), Optional.empty());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder param0) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(param0), Optional.empty());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer() {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(Optional<EntityPredicate> param0, Optional<DamageSourcePredicate> param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(param0), param1);
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder param0, Optional<DamageSourcePredicate> param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(param0), param1);
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(Optional<EntityPredicate> param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(param0), param1.build());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, Optional.empty(), EntityPredicate.wrap(param0), param1.build());
        }

        public boolean matches(ServerPlayer param0, LootContext param1, DamageSource param2) {
            if (this.killingBlow.isPresent() && !this.killingBlow.get().matches(param0, param2)) {
                return false;
            } else {
                return this.entityPredicate.isEmpty() || this.entityPredicate.get().matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.entityPredicate.ifPresent(param1 -> var0.add("entity", param1.toJson()));
            this.killingBlow.ifPresent(param1 -> var0.add("killing_blow", param1.serializeToJson()));
            return var0;
        }
    }
}
