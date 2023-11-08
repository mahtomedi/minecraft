package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
    @Override
    public Codec<SlideDownBlockTrigger.TriggerInstance> codec() {
        return SlideDownBlockTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, BlockState param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SlideDownBlockTrigger.TriggerInstance> CODEC = ExtraCodecs.validate(
            RecordCodecBuilder.create(
                param0 -> param0.group(
                            ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player")
                                .forGetter(SlideDownBlockTrigger.TriggerInstance::player),
                            ExtraCodecs.strictOptionalField(BuiltInRegistries.BLOCK.holderByNameCodec(), "block")
                                .forGetter(SlideDownBlockTrigger.TriggerInstance::block),
                            ExtraCodecs.strictOptionalField(StatePropertiesPredicate.CODEC, "state").forGetter(SlideDownBlockTrigger.TriggerInstance::state)
                        )
                        .apply(param0, SlideDownBlockTrigger.TriggerInstance::new)
            ),
            SlideDownBlockTrigger.TriggerInstance::validate
        );

        private static DataResult<SlideDownBlockTrigger.TriggerInstance> validate(SlideDownBlockTrigger.TriggerInstance param0) {
            return param0.block
                .<DataResult<SlideDownBlockTrigger.TriggerInstance>>flatMap(
                    param1 -> param0.state
                            .<String>flatMap(param1x -> param1x.checkState(((Block)param1.value()).getStateDefinition()))
                            .map(param1x -> DataResult.error(() -> "Block" + param1 + " has no property " + param1x))
                )
                .orElseGet(() -> DataResult.success(param0));
        }

        public static Criterion<SlideDownBlockTrigger.TriggerInstance> slidesDownBlock(Block param0) {
            return CriteriaTriggers.HONEY_BLOCK_SLIDE
                .createCriterion(new SlideDownBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(param0.builtInRegistryHolder()), Optional.empty()));
        }

        public boolean matches(BlockState param0) {
            if (this.block.isPresent() && !param0.is(this.block.get())) {
                return false;
            } else {
                return !this.state.isPresent() || this.state.get().matches(param0);
            }
        }
    }
}
