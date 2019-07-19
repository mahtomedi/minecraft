package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger implements CriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("brewed_potion");
    private final Map<PlayerAdvancements, BrewedPotionTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> param1) {
        BrewedPotionTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new BrewedPotionTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> param1) {
        BrewedPotionTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public BrewedPotionTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        Potion var0 = null;
        if (param0.has("potion")) {
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(param0, "potion"));
            var0 = Registry.POTION.getOptional(var1).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + var1 + "'"));
        }

        return new BrewedPotionTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, Potion param1) {
        BrewedPotionTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(Potion param0) {
            List<CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Potion potion;

        public TriggerInstance(@Nullable Potion param0) {
            super(BrewedPotionTrigger.ID);
            this.potion = param0;
        }

        public static BrewedPotionTrigger.TriggerInstance brewedPotion() {
            return new BrewedPotionTrigger.TriggerInstance(null);
        }

        public boolean matches(Potion param0) {
            return this.potion == null || this.potion == param0;
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            if (this.potion != null) {
                var0.addProperty("potion", Registry.POTION.getKey(this.potion).toString());
            }

            return var0;
        }
    }
}
