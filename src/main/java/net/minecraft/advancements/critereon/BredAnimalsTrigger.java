package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("bred_animals");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ContextAwarePredicate> var0 = EntityPredicate.fromJson(param0, "parent", param2);
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "partner", param2);
        Optional<ContextAwarePredicate> var2 = EntityPredicate.fromJson(param0, "child", param2);
        return new BredAnimalsTrigger.TriggerInstance(param1, var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, Animal param1, Animal param2, @Nullable AgeableMob param3) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        LootContext var1 = EntityPredicate.createContext(param0, param2);
        LootContext var2 = param3 != null ? EntityPredicate.createContext(param0, param3) : null;
        this.trigger(param0, param3x -> param3x.matches(var0, var1, var2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> parent;
        private final Optional<ContextAwarePredicate> partner;
        private final Optional<ContextAwarePredicate> child;

        public TriggerInstance(
            Optional<ContextAwarePredicate> param0,
            Optional<ContextAwarePredicate> param1,
            Optional<ContextAwarePredicate> param2,
            Optional<ContextAwarePredicate> param3
        ) {
            super(BredAnimalsTrigger.ID, param0);
            this.parent = param1;
            this.partner = param2;
            this.child = param3;
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
            return new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder param0) {
            return new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), EntityPredicate.wrap(param0));
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals(
            Optional<EntityPredicate> param0, Optional<EntityPredicate> param1, Optional<EntityPredicate> param2
        ) {
            return new BredAnimalsTrigger.TriggerInstance(
                Optional.empty(), EntityPredicate.wrap(param0), EntityPredicate.wrap(param1), EntityPredicate.wrap(param2)
            );
        }

        public boolean matches(LootContext param0, LootContext param1, @Nullable LootContext param2) {
            if (!this.child.isPresent() || param2 != null && this.child.get().matches(param2)) {
                return matches(this.parent, param0) && matches(this.partner, param1) || matches(this.parent, param1) && matches(this.partner, param0);
            } else {
                return false;
            }
        }

        private static boolean matches(Optional<ContextAwarePredicate> param0, LootContext param1) {
            return param0.isEmpty() || param0.get().matches(param1);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.parent.ifPresent(param1 -> var0.add("parent", param1.toJson()));
            this.partner.ifPresent(param1 -> var0.add("partner", param1.toJson()));
            this.child.ifPresent(param1 -> var0.add("child", param1.toJson()));
            return var0;
        }
    }
}
