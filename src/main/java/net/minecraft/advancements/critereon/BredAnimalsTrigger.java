package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Animal;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("bred_animals");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("parent"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("partner"));
        EntityPredicate var2 = EntityPredicate.fromJson(param0.get("child"));
        return new BredAnimalsTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, Animal param1, @Nullable Animal param2, @Nullable AgableMob param3) {
        this.trigger(param0.getAdvancements(), param4 -> param4.matches(param0, param1, param2, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate parent;
        private final EntityPredicate partner;
        private final EntityPredicate child;

        public TriggerInstance(EntityPredicate param0, EntityPredicate param1, EntityPredicate param2) {
            super(BredAnimalsTrigger.ID);
            this.parent = param0;
            this.partner = param1;
            this.child = param2;
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
            return new BredAnimalsTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder param0) {
            return new BredAnimalsTrigger.TriggerInstance(param0.build(), EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Animal param1, @Nullable Animal param2, @Nullable AgableMob param3) {
            if (!this.child.matches(param0, param3)) {
                return false;
            } else {
                return this.parent.matches(param0, param1) && this.partner.matches(param0, param2)
                    || this.parent.matches(param0, param2) && this.partner.matches(param0, param1);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("parent", this.parent.serializeToJson());
            var0.add("partner", this.partner.serializeToJson());
            var0.add("child", this.child.serializeToJson());
            return var0;
        }
    }
}
