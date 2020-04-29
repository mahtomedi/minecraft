package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("brewed_potion");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public BrewedPotionTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        Potion var0 = null;
        if (param0.has("potion")) {
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(param0, "potion"));
            var0 = Registry.POTION.getOptional(var1).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + var1 + "'"));
        }

        return new BrewedPotionTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Potion param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Potion potion;

        public TriggerInstance(EntityPredicate.Composite param0, @Nullable Potion param1) {
            super(BrewedPotionTrigger.ID, param0);
            this.potion = param1;
        }

        public static BrewedPotionTrigger.TriggerInstance brewedPotion() {
            return new BrewedPotionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, null);
        }

        public boolean matches(Potion param0) {
            return this.potion == null || this.potion == param0;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            if (this.potion != null) {
                var0.addProperty("potion", Registry.POTION.getKey(this.potion).toString());
            }

            return var0;
        }
    }
}
