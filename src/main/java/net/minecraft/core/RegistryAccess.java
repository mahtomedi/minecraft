package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface RegistryAccess {
    <E> Optional<WritableRegistry<E>> registry(ResourceKey<Registry<E>> var1);

    @OnlyIn(Dist.CLIENT)
    Registry<DimensionType> dimensionTypes();

    static RegistryAccess.RegistryHolder builtin() {
        return DimensionType.registerBuiltin(new RegistryAccess.RegistryHolder());
    }

    public static final class RegistryHolder implements RegistryAccess {
        public static final Codec<RegistryAccess.RegistryHolder> CODEC = MappedRegistry.networkCodec(
                Registry.DIMENSION_TYPE_REGISTRY, Lifecycle.experimental(), DimensionType.DIRECT_CODEC
            )
            .xmap(RegistryAccess.RegistryHolder::new, param0 -> param0.dimensionTypes)
            .fieldOf("dimension")
            .codec();
        private final MappedRegistry<DimensionType> dimensionTypes;

        public RegistryHolder() {
            this(new MappedRegistry<>(Registry.DIMENSION_TYPE_REGISTRY, Lifecycle.experimental()));
        }

        private RegistryHolder(MappedRegistry<DimensionType> param0) {
            this.dimensionTypes = param0;
        }

        public void registerDimension(ResourceKey<DimensionType> param0, DimensionType param1) {
            this.dimensionTypes.register(param0, param1);
        }

        @Override
        public <E> Optional<WritableRegistry<E>> registry(ResourceKey<Registry<E>> param0) {
            return Objects.equals(param0, Registry.DIMENSION_TYPE_REGISTRY) ? Optional.of(this.dimensionTypes) : Optional.empty();
        }

        @Override
        public Registry<DimensionType> dimensionTypes() {
            return this.dimensionTypes;
        }
    }
}
