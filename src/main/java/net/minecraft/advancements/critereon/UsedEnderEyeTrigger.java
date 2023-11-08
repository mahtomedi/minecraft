package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
    @Override
    public Codec<UsedEnderEyeTrigger.TriggerInstance> codec() {
        return UsedEnderEyeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, BlockPos param1) {
        double var0 = param0.getX() - (double)param1.getX();
        double var1 = param0.getZ() - (double)param1.getZ();
        double var2 = var0 * var0 + var1 * var1;
        this.trigger(param0, param1x -> param1x.matches(var2));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Doubles distance)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UsedEnderEyeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(UsedEnderEyeTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "distance", MinMaxBounds.Doubles.ANY)
                            .forGetter(UsedEnderEyeTrigger.TriggerInstance::distance)
                    )
                    .apply(param0, UsedEnderEyeTrigger.TriggerInstance::new)
        );

        public boolean matches(double param0) {
            return this.distance.matchesSqr(param0);
        }
    }
}
