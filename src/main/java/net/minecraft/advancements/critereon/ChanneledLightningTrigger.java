package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("channeled_lightning");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ChanneledLightningTrigger.TriggerInstance createInstance(JsonObject param0, ContextAwarePredicate param1, DeserializationContext param2) {
        ContextAwarePredicate[] var0 = EntityPredicate.fromJsonArray(param0, "victims", param2);
        return new ChanneledLightningTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Collection<? extends Entity> param1) {
        List<LootContext> var0 = param1.stream().map(param1x -> EntityPredicate.createContext(param0, param1x)).collect(Collectors.toList());
        this.trigger(param0, param1x -> param1x.matches(var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate[] victims;

        public TriggerInstance(ContextAwarePredicate param0, ContextAwarePredicate[] param1) {
            super(ChanneledLightningTrigger.ID, param0);
            this.victims = param1;
        }

        public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... param0) {
            return new ChanneledLightningTrigger.TriggerInstance(
                ContextAwarePredicate.ANY, Stream.of(param0).map(EntityPredicate::wrap).toArray(param0x -> new ContextAwarePredicate[param0x])
            );
        }

        public boolean matches(Collection<? extends LootContext> param0) {
            for(ContextAwarePredicate var0 : this.victims) {
                boolean var1 = false;

                for(LootContext var2 : param0) {
                    if (var0.matches(var2)) {
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
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("victims", ContextAwarePredicate.toJson(this.victims, param0));
            return var0;
        }
    }
}
