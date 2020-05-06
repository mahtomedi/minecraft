package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FishingRodHookedTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("rod"));
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "entity", param2);
        ItemPredicate var2 = ItemPredicate.fromJson(param0.get("item"));
        return new FishingRodHookedTrigger.TriggerInstance(param1, var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, FishingHook param2, Collection<ItemStack> param3) {
        LootContext var0 = EntityPredicate.createContext(param0, (Entity)(param2.getHookedIn() != null ? param2.getHookedIn() : param2));
        this.trigger(param0, param3x -> param3x.matches(param1, var0, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate rod;
        private final EntityPredicate.Composite entity;
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite param0, ItemPredicate param1, EntityPredicate.Composite param2, ItemPredicate param3) {
            super(FishingRodHookedTrigger.ID, param0);
            this.rod = param1;
            this.entity = param2;
            this.item = param3;
        }

        public static FishingRodHookedTrigger.TriggerInstance fishedItem(ItemPredicate param0, EntityPredicate param1, ItemPredicate param2) {
            return new FishingRodHookedTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, EntityPredicate.Composite.wrap(param1), param2);
        }

        public boolean matches(ItemStack param0, LootContext param1, Collection<ItemStack> param2) {
            if (!this.rod.matches(param0)) {
                return false;
            } else if (!this.entity.matches(param1)) {
                return false;
            } else {
                if (this.item != ItemPredicate.ANY) {
                    boolean var0 = false;
                    Entity var1 = param1.getParamOrNull(LootContextParams.THIS_ENTITY);
                    if (var1 instanceof ItemEntity) {
                        ItemEntity var2 = (ItemEntity)var1;
                        if (this.item.matches(var2.getItem())) {
                            var0 = true;
                        }
                    }

                    for(ItemStack var3 : param2) {
                        if (this.item.matches(var3)) {
                            var0 = true;
                            break;
                        }
                    }

                    if (!var0) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("rod", this.rod.serializeToJson());
            var0.add("entity", this.entity.toJson(param0));
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}
