package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("channeled_lightning");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ChanneledLightningTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate[] var0 = EntityPredicate.fromJsonArray(param0.get("victims"));
        return new ChanneledLightningTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, Collection<? extends Entity> param1) {
        this.trigger(param0.getAdvancements(), param2 -> param2.matches(param0, param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate[] victims;

        public TriggerInstance(EntityPredicate[] param0) {
            super(ChanneledLightningTrigger.ID);
            this.victims = param0;
        }

        public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... param0) {
            return new ChanneledLightningTrigger.TriggerInstance(param0);
        }

        public boolean matches(ServerPlayer param0, Collection<? extends Entity> param1) {
            for(EntityPredicate var0 : this.victims) {
                boolean var1 = false;

                for(Entity var2 : param1) {
                    if (var0.matches(param0, var2)) {
                        var1 = true;
                        break;
                    }
                }

                if (!var1) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("victims", EntityPredicate.serializeArrayToJson(this.victims));
            return var0;
        }
    }
}
