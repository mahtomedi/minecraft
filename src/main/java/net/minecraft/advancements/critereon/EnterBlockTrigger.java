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
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class EnterBlockTrigger implements CriterionTrigger<EnterBlockTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("enter_block");
    private final Map<PlayerAdvancements, EnterBlockTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance> param1) {
        EnterBlockTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new EnterBlockTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance> param1) {
        EnterBlockTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public EnterBlockTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        Block var0 = null;
        if (param0.has("block")) {
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            var0 = Registry.BLOCK.getOptional(var1).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + var1 + "'"));
        }

        Map<Property<?>, Object> var2 = null;
        if (param0.has("state")) {
            if (var0 == null) {
                throw new JsonSyntaxException("Can't define block state without a specific block type");
            }

            StateDefinition<Block, BlockState> var3 = var0.getStateDefinition();

            for(Entry<String, JsonElement> var4 : GsonHelper.getAsJsonObject(param0, "state").entrySet()) {
                Property<?> var5 = var3.getProperty(var4.getKey());
                if (var5 == null) {
                    throw new JsonSyntaxException(
                        "Unknown block state property '" + (String)var4.getKey() + "' for block '" + Registry.BLOCK.getKey(var0) + "'"
                    );
                }

                String var6 = GsonHelper.convertToString(var4.getValue(), var4.getKey());
                Optional<?> var7 = var5.getValue(var6);
                if (!var7.isPresent()) {
                    throw new JsonSyntaxException(
                        "Invalid block state value '" + var6 + "' for property '" + (String)var4.getKey() + "' on block '" + Registry.BLOCK.getKey(var0) + "'"
                    );
                }

                if (var2 == null) {
                    var2 = Maps.newHashMap();
                }

                var2.put(var5, var7.get());
            }
        }

        return new EnterBlockTrigger.TriggerInstance(var0, var2);
    }

    public void trigger(ServerPlayer param0, BlockState param1) {
        EnterBlockTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(BlockState param0) {
            List<CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<EnterBlockTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Block block;
        private final Map<Property<?>, Object> state;

        public TriggerInstance(@Nullable Block param0, @Nullable Map<Property<?>, Object> param1) {
            super(EnterBlockTrigger.ID);
            this.block = param0;
            this.state = param1;
        }

        public static EnterBlockTrigger.TriggerInstance entersBlock(Block param0) {
            return new EnterBlockTrigger.TriggerInstance(param0, null);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            if (this.block != null) {
                var0.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
                if (this.state != null && !this.state.isEmpty()) {
                    JsonObject var1 = new JsonObject();

                    for(Entry<Property<?>, ?> var2 : this.state.entrySet()) {
                        var1.addProperty(var2.getKey().getName(), Util.getPropertyName(var2.getKey(), var2.getValue()));
                    }

                    var0.add("state", var1);
                }
            }

            return var0;
        }

        public boolean matches(BlockState param0) {
            if (this.block != null && param0.getBlock() != this.block) {
                return false;
            } else {
                if (this.state != null) {
                    for(Entry<Property<?>, Object> var0 : this.state.entrySet()) {
                        if (param0.getValue(var0.getKey()) != var0.getValue()) {
                            return false;
                        }
                    }
                }

                return true;
            }
        }
    }
}
