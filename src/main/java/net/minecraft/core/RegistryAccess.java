package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface RegistryAccess {
    @OnlyIn(Dist.CLIENT)
    Registry<DimensionType> dimensionTypes();

    @OnlyIn(Dist.CLIENT)
    static RegistryAccess.RegistryHolder builtin() {
        return DimensionType.registerBuiltin(new RegistryAccess.RegistryHolder());
    }

    public static final class RegistryHolder implements RegistryAccess {
        public static final Codec<RegistryAccess.RegistryHolder> CODEC = MappedRegistry.codec(
                Registry.DIMENSION_TYPE_REGISTRY, Lifecycle.experimental(), DimensionType.CODEC
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

        @OnlyIn(Dist.CLIENT)
        @Override
        public Registry<DimensionType> dimensionTypes() {
            return this.dimensionTypes;
        }
    }
}
