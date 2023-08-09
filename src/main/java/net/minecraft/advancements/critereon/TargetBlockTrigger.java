package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("target_hit");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TargetBlockTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("signal_strength"));
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "projectile", param2);
        return new TargetBlockTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Entity param1, Vec3 param2, int param3) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param3x -> param3x.matches(var0, param2, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints signalStrength;
        private final Optional<ContextAwarePredicate> projectile;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, MinMaxBounds.Ints param1, Optional<ContextAwarePredicate> param2) {
            super(TargetBlockTrigger.ID, param0);
            this.signalStrength = param1;
            this.projectile = param2;
        }

        public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints param0, Optional<ContextAwarePredicate> param1) {
            return new TargetBlockTrigger.TriggerInstance(Optional.empty(), param0, param1);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            var0.add("signal_strength", this.signalStrength.serializeToJson());
            this.projectile.ifPresent(param1 -> var0.add("projectile", param1.toJson()));
            return var0;
        }

        public boolean matches(LootContext param0, Vec3 param1, int param2) {
            if (!this.signalStrength.matches(param2)) {
                return false;
            } else {
                return !this.projectile.isPresent() || this.projectile.get().matches(param0);
            }
        }
    }
}
