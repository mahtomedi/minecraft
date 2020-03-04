package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("target_hit");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TargetBlockTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("signal_strength"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("projectile"));
        EntityPredicate var2 = EntityPredicate.fromJson(param0.get("shooter"));
        return new TargetBlockTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, Entity param1, Vec3 param2, int param3) {
        this.trigger(param0.getAdvancements(), param4 -> param4.matches(param0, param1, param2, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints signalStrength;
        private final EntityPredicate projectile;
        private final EntityPredicate shooter;

        public TriggerInstance(MinMaxBounds.Ints param0, EntityPredicate param1, EntityPredicate param2) {
            super(TargetBlockTrigger.ID);
            this.signalStrength = param0;
            this.projectile = param1;
            this.shooter = param2;
        }

        public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints param0) {
            return new TargetBlockTrigger.TriggerInstance(param0, EntityPredicate.ANY, EntityPredicate.ANY);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("signal_strength", this.signalStrength.serializeToJson());
            var0.add("projectile", this.projectile.serializeToJson());
            var0.add("shooter", this.shooter.serializeToJson());
            return var0;
        }

        public boolean matches(ServerPlayer param0, Entity param1, Vec3 param2, int param3) {
            if (!this.signalStrength.matches(param3)) {
                return false;
            } else if (!this.projectile.matches(param0, param1)) {
                return false;
            } else {
                return this.shooter.matches(param0.getLevel(), param2, param0);
            }
        }
    }
}
