package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
    private final RegistryAccess registryAccess;

    public static <T> RegistryOps<T> create(DynamicOps<T> param0, RegistryAccess param1) {
        return new RegistryOps<>(param0, param1);
    }

    private RegistryOps(DynamicOps<T> param0, RegistryAccess param1) {
        super(param0);
        this.registryAccess = param1;
    }

    public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.registryAccess.registry(param0);
    }

    public static <E> MapCodec<Registry<E>> retrieveRegistry(ResourceKey<? extends Registry<? extends E>> param0) {
        return ExtraCodecs.retrieveContext(
            param1 -> param1 instanceof RegistryOps var0x
                    ? var0x.registry(param0)
                        .map(param0x -> DataResult.success(param0x, param0x.elementsLifecycle()))
                        .orElseGet(() -> DataResult.error("Unknown registry: " + param0))
                    : DataResult.error("Not a registry ops")
        );
    }
}
