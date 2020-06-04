package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("bred_animals");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        EntityPredicate.Composite var0 = EntityPredicate.Composite.fromJson(param0, "parent", param2);
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "partner", param2);
        EntityPredicate.Composite var2 = EntityPredicate.Composite.fromJson(param0, "child", param2);
        return new BredAnimalsTrigger.TriggerInstance(param1, var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, Animal param1, Animal param2, @Nullable AgableMob param3) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        LootContext var1 = EntityPredicate.createContext(param0, param2);
        LootContext var2 = param3 != null ? EntityPredicate.createContext(param0, param3) : null;
        this.trigger(param0, param3x -> param3x.matches(var0, var1, var2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite parent;
        private final EntityPredicate.Composite partner;
        private final EntityPredicate.Composite child;

        public TriggerInstance(
            EntityPredicate.Composite param0, EntityPredicate.Composite param1, EntityPredicate.Composite param2, EntityPredicate.Composite param3
        ) {
            super(BredAnimalsTrigger.ID, param0);
            this.parent = param1;
            this.partner = param2;
            this.child = param3;
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
            return new BredAnimalsTrigger.TriggerInstance(
                EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY
            );
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder param0) {
            return new BredAnimalsTrigger.TriggerInstance(
                EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0.build())
            );
        }

        public boolean matches(LootContext param0, LootContext param1, @Nullable LootContext param2) {
            if (param2 != null && !this.child.matches(param2)) {
                return false;
            } else {
                return this.parent.matches(param0) && this.partner.matches(param1) || this.parent.matches(param1) && this.partner.matches(param0);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("parent", this.parent.toJson(param0));
            var0.add("partner", this.partner.toJson(param0));
            var0.add("child", this.child.toJson(param0));
            return var0;
        }
    }
}
