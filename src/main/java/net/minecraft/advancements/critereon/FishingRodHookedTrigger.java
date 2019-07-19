package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class FishingRodHookedTrigger implements CriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");
    private final Map<PlayerAdvancements, FishingRodHookedTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> param1) {
        FishingRodHookedTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new FishingRodHookedTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> param1) {
        FishingRodHookedTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 != null) {
            var0.removeListener(param1);
            if (var0.isEmpty()) {
                this.players.remove(param0);
            }
        }

    }

    @Override
    public void removePlayerListeners(PlayerAdvancements param0) {
        this.players.remove(param0);
    }

    public FishingRodHookedTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("rod"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("entity"));
        ItemPredicate var2 = ItemPredicate.fromJson(param0.get("item"));
        return new FishingRodHookedTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, FishingHook param2, Collection<ItemStack> param3) {
        FishingRodHookedTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2, param3);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, ItemStack param1, FishingHook param2, Collection<ItemStack> param3) {
            List<CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2, param3)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
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
