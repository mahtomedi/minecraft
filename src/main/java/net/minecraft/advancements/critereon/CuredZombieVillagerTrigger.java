package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ContextAwarePredicate> var0 = EntityPredicate.fromJson(param0, "zombie", param2);
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "villager", param2);
        return new CuredZombieVillagerTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Zombie param1, Villager param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        LootContext var1 = EntityPredicate.createContext(param0, param2);
        this.trigger(param0, param2x -> param2x.matches(var0, var1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> zombie;
        private final Optional<ContextAwarePredicate> villager;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ContextAwarePredicate> param1, Optional<ContextAwarePredicate> param2) {
            super(CuredZombieVillagerTrigger.ID, param0);
            this.zombie = param1;
            this.villager = param2;
        }

        public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
            return new CuredZombieVillagerTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public boolean matches(LootContext param0, LootContext param1) {
            if (this.zombie.isPresent() && !this.zombie.get().matches(param0)) {
                return false;
            } else {
                return !this.villager.isPresent() || this.villager.get().matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.zombie.ifPresent(param1 -> var0.add("zombie", param1.toJson()));
            this.villager.ifPresent(param1 -> var0.add("villager", param1.toJson()));
            return var0;
        }
    }
}
