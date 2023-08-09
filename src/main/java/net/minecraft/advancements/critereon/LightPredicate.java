package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;

public record LightPredicate(MinMaxBounds.Ints composite) {
    public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "light", MinMaxBounds.Ints.ANY).forGetter(LightPredicate::composite))
                .apply(param0, LightPredicate::new)
    );

    static Optional<LightPredicate> of(MinMaxBounds.Ints param0) {
        return param0.isAny() ? Optional.empty() : Optional.of(new LightPredicate(param0));
    }

    public boolean matches(ServerLevel param0, BlockPos param1) {
        if (!param0.isLoaded(param1)) {
            return false;
        } else {
            return this.composite.matches(param0.getMaxLocalRawBrightness(param1));
        }
    }

    public static class Builder {
        private MinMaxBounds.Ints composite = MinMaxBounds.Ints.ANY;

        public static LightPredicate.Builder light() {
            return new LightPredicate.Builder();
        }

        public LightPredicate.Builder setComposite(MinMaxBounds.Ints param0) {
            this.composite = param0;
            return this;
        }

        public Optional<LightPredicate> build() {
            return LightPredicate.of(this.composite);
        }
    }
}
