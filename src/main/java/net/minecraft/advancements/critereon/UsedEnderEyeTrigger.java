package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("used_ender_eye");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public UsedEnderEyeTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        MinMaxBounds.Floats var0 = MinMaxBounds.Floats.fromJson(param0.get("distance"));
        return new UsedEnderEyeTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, BlockPos param1) {
        double var0 = param0.getX() - (double)param1.getX();
        double var1 = param0.getZ() - (double)param1.getZ();
        double var2 = var0 * var0 + var1 * var1;
        this.trigger(param0, param1x -> param1x.matches(var2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Floats level;

        public TriggerInstance(EntityPredicate.Composite param0, MinMaxBounds.Floats param1) {
            super(UsedEnderEyeTrigger.ID, param0);
            this.level = param1;
        }

        public boolean matches(double param0) {
            return this.level.matchesSqr(param0);
        }
    }
}
