package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FishingRodHookedTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("rod"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("entity"));
        ItemPredicate var2 = ItemPredicate.fromJson(param0.get("item"));
        return new FishingRodHookedTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, FishingHook param2, Collection<ItemStack> param3) {
        this.trigger(param0.getAdvancements(), param4 -> param4.matches(param0, param1, param2, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate rod;
        private final EntityPredicate entity;
        private final ItemPredicate item;

        public TriggerInstance(ItemPredicate param0, EntityPredicate param1, ItemPredicate param2) {
            super(FishingRodHookedTrigger.ID);
            this.rod = param0;
            this.entity = param1;
            this.item = param2;
        }

        public static FishingRodHookedTrigger.TriggerInstance fishedItem(ItemPredicate param0, EntityPredicate param1, ItemPredicate param2) {
            return new FishingRodHookedTrigger.TriggerInstance(param0, param1, param2);
        }

        public boolean matches(ServerPlayer param0, ItemStack param1, FishingHook param2, Collection<ItemStack> param3) {
            if (!this.rod.matches(param1)) {
                return false;
            } else if (!this.entity.matches(param0, param2.hookedIn)) {
                return false;
            } else {
                if (this.item != ItemPredicate.ANY) {
                    boolean var0 = false;
                    if (param2.hookedIn instanceof ItemEntity) {
                        ItemEntity var1 = (ItemEntity)param2.hookedIn;
                        if (this.item.matches(var1.getItem())) {
                            var0 = true;
                        }
                    }

                    for(ItemStack var2 : param3) {
                        if (this.item.matches(var2)) {
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
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("rod", this.rod.serializeToJson());
            var0.add("entity", this.entity.serializeToJson());
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}
