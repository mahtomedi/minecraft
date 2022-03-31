package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
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

    public KilledTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        return new KilledTrigger.TriggerInstance(
            this.id, param1, EntityPredicate.Composite.fromJson(param0, "entity", param2), DamageSourcePredicate.fromJson(param0.get("killing_blow"))
        );
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param3 -> param3.matches(param0, var0, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite entityPredicate;
        private final DamageSourcePredicate killingBlow;

        public TriggerInstance(ResourceLocation param0, EntityPredicate.Composite param1, EntityPredicate.Composite param2, DamageSourcePredicate param3) {
            super(param0, param1);
            this.entityPredicate = param2;
            this.killingBlow = param3;
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate param0) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0), DamageSourcePredicate.ANY
            );
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.PLAYER_KILLED_ENTITY.id,
                EntityPredicate.Composite.ANY,
                EntityPredicate.Composite.wrap(param0.build()),
                DamageSourcePredicate.ANY
            );
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity() {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY
            );
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate param0, DamageSourcePredicate param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0), param1
            );
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0, DamageSourcePredicate param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0.build()), param1
            );
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0), param1.build()
            );
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0.build()), param1.build()
            );
        }

        public static KilledTrigger.TriggerInstance playerKilledEntityNearSculkCatalyst() {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY
            );
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate param0) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0), DamageSourcePredicate.ANY
            );
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder param0) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.ENTITY_KILLED_PLAYER.id,
                EntityPredicate.Composite.ANY,
                EntityPredicate.Composite.wrap(param0.build()),
                DamageSourcePredicate.ANY
            );
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer() {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY
            );
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate param0, DamageSourcePredicate param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0), param1
            );
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder param0, DamageSourcePredicate param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0.build()), param1
            );
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0), param1.build()
            );
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(
                CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0.build()), param1.build()
            );
        }

        public boolean matches(ServerPlayer param0, LootContext param1, DamageSource param2) {
            return !this.killingBlow.matches(param0, param2) ? false : this.entityPredicate.matches(param1);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("entity", this.entityPredicate.toJson(param0));
            var0.add("killing_blow", this.killingBlow.serializeToJson());
            return var0;
        }
    }
}
