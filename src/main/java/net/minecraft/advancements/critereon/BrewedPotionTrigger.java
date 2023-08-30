package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
    public BrewedPotionTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Potion var0 = null;
        if (param0.has("potion")) {
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(param0, "potion"));
            var0 = BuiltInRegistries.POTION.getOptional(var1).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + var1 + "'"));
        }

        return new BrewedPotionTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Potion param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Potion potion;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, @Nullable Potion param1) {
            super(param0);
            this.potion = param1;
        }

        public static Criterion<BrewedPotionTrigger.TriggerInstance> brewedPotion() {
            return CriteriaTriggers.BREWED_POTION.createCriterion(new BrewedPotionTrigger.TriggerInstance(Optional.empty(), null));
        }

        public boolean matches(Potion param0) {
            return this.potion == null || this.potion == param0;
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            if (this.potion != null) {
                var0.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
            }

            return var0;
        }
    }
}
