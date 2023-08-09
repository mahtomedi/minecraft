package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
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

    public SlideDownBlockTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Block var0 = deserializeBlock(param0);
        Optional<StatePropertiesPredicate> var1 = StatePropertiesPredicate.fromJson(param0.get("state"));
        if (var0 != null) {
            var1.ifPresent(param1x -> param1x.checkState(var0.getStateDefinition(), param1xx -> {
                    throw new JsonSyntaxException("Block " + var0 + " has no property " + param1xx);
                }));
        }

        return new SlideDownBlockTrigger.TriggerInstance(param1, var0, var1);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject param0) {
        if (param0.has("block")) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            return BuiltInRegistries.BLOCK.getOptional(var0).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + var0 + "'"));
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
        private final Optional<StatePropertiesPredicate> state;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, @Nullable Block param1, Optional<StatePropertiesPredicate> param2) {
            super(SlideDownBlockTrigger.ID, param0);
            this.block = param1;
            this.state = param2;
        }

        public static SlideDownBlockTrigger.TriggerInstance slidesDownBlock(Block param0) {
            return new SlideDownBlockTrigger.TriggerInstance(Optional.empty(), param0, Optional.empty());
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            if (this.block != null) {
                var0.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
            }

            this.state.ifPresent(param1 -> var0.add("state", param1.serializeToJson()));
            return var0;
        }

        public boolean matches(BlockState param0) {
            if (this.block != null && !param0.is(this.block)) {
                return false;
            } else {
                return !this.state.isPresent() || this.state.get().matches(param0);
            }
        }
    }
}
