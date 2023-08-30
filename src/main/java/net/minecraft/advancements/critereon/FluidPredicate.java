package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public record FluidPredicate(Optional<TagKey<Fluid>> tag, Optional<Holder<Fluid>> fluid, Optional<StatePropertiesPredicate> properties) {
    public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(TagKey.codec(Registries.FLUID), "tag").forGetter(FluidPredicate::tag),
                    ExtraCodecs.strictOptionalField(BuiltInRegistries.FLUID.holderByNameCodec(), "fluid").forGetter(FluidPredicate::fluid),
                    ExtraCodecs.strictOptionalField(StatePropertiesPredicate.CODEC, "state").forGetter(FluidPredicate::properties)
                )
                .apply(param0, FluidPredicate::new)
    );

    public boolean matches(ServerLevel param0, BlockPos param1) {
        if (!param0.isLoaded(param1)) {
            return false;
        } else {
            FluidState var0 = param0.getFluidState(param1);
            if (this.tag.isPresent() && !var0.is(this.tag.get())) {
                return false;
            } else if (this.fluid.isPresent() && !var0.is(this.fluid.get().value())) {
                return false;
            } else {
                return !this.properties.isPresent() || this.properties.get().matches(var0);
            }
        }
    }

    public static class Builder {
        private Optional<Holder<Fluid>> fluid = Optional.empty();
        private Optional<TagKey<Fluid>> fluids = Optional.empty();
        private Optional<StatePropertiesPredicate> properties = Optional.empty();

        private Builder() {
        }

        public static FluidPredicate.Builder fluid() {
            return new FluidPredicate.Builder();
        }

        public FluidPredicate.Builder of(Fluid param0) {
            this.fluid = Optional.of(param0.builtInRegistryHolder());
            return this;
        }

        public FluidPredicate.Builder of(TagKey<Fluid> param0) {
            this.fluids = Optional.of(param0);
            return this;
        }

        public FluidPredicate.Builder setProperties(StatePropertiesPredicate param0) {
            this.properties = Optional.of(param0);
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.fluids, this.fluid, this.properties);
        }
    }
}
