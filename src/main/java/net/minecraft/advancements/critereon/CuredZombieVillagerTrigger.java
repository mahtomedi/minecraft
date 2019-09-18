package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("zombie"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("villager"));
        return new CuredZombieVillagerTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, Zombie param1, Villager param2) {
        this.trigger(param0.getAdvancements(), param3 -> param3.matches(param0, param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate zombie;
        private final EntityPredicate villager;

        public TriggerInstance(EntityPredicate param0, EntityPredicate param1) {
            super(CuredZombieVillagerTrigger.ID);
            this.zombie = param0;
            this.villager = param1;
        }

        public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
            return new CuredZombieVillagerTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Zombie param1, Villager param2) {
            if (!this.zombie.matches(param0, param1)) {
                return false;
            } else {
                return this.villager.matches(param0, param2);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("zombie", this.zombie.serializeToJson());
            var0.add("villager", this.villager.serializeToJson());
            return var0;
        }
    }
}
