package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        EntityPredicate.Composite var0 = EntityPredicate.Composite.fromJson(param0, "zombie", param2);
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "villager", param2);
        return new CuredZombieVillagerTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Zombie param1, Villager param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        LootContext var1 = EntityPredicate.createContext(param0, param2);
        this.trigger(param0, param2x -> param2x.matches(var0, var1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite zombie;
        private final EntityPredicate.Composite villager;

        public TriggerInstance(EntityPredicate.Composite param0, EntityPredicate.Composite param1, EntityPredicate.Composite param2) {
            super(CuredZombieVillagerTrigger.ID, param0);
            this.zombie = param1;
            this.villager = param2;
        }

        public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
            return new CuredZombieVillagerTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY);
        }

        public boolean matches(LootContext param0, LootContext param1) {
            if (!this.zombie.matches(param0)) {
                return false;
            } else {
                return this.villager.matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("zombie", this.zombie.toJson(param0));
            var0.add("villager", this.villager.toJson(param0));
            return var0;
        }
    }
}
