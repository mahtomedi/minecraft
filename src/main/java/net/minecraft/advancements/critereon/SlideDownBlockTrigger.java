package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("slide_down_block");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public SlideDownBlockTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        Block var0 = deserializeBlock(param0);
        StatePropertiesPredicate var1 = StatePropertiesPredicate.fromJson(param0.get("state"));
        if (var0 != null) {
            var1.checkState(var0.getStateDefinition(), param1x -> {
                throw new JsonSyntaxException("Block " + var0 + " has no property " + param1x);
            });
        }

        return new SlideDownBlockTrigger.TriggerInstance(param1, var0, var1);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject param0) {
        if (param0.has("block")) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            return Registry.BLOCK.getOptional(var0).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + var0 + "'"));
        } else {
            return null;
        }
    }

    public void trigger(ServerPlayer param0, BlockState param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Block block;
        private final StatePropertiesPredicate state;

        public TriggerInstance(EntityPredicate.Composite param0, @Nullable Block param1, StatePropertiesPredicate param2) {
            super(SlideDownBlockTrigger.ID, param0);
            this.block = param1;
            this.state = param2;
        }

        public static SlideDownBlockTrigger.TriggerInstance slidesDownBlock(Block param0) {
            return new SlideDownBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, StatePropertiesPredicate.ANY);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            if (this.block != null) {
                var0.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }

            var0.add("state", this.state.serializeToJson());
            return var0;
        }

        public boolean matches(BlockState param0) {
            if (this.block != null && !param0.is(this.block)) {
                return false;
            } else {
                return this.state.matches(param0);
            }
        }
    }
}
