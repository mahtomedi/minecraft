package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
    private final Optional<RegistryLoader.Bound> loader;
    private final RegistryAccess registryAccess;
    private final DynamicOps<JsonElement> asJson;

    public static <T> RegistryOps<T> create(DynamicOps<T> param0, RegistryAccess param1) {
        return new RegistryOps<>(param0, param1, Optional.empty());
    }

    public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> param0, RegistryAccess.Writable param1, ResourceManager param2) {
        return createAndLoad(param0, param1, RegistryResourceAccess.forResourceManager(param2));
    }

    public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> param0, RegistryAccess.Writable param1, RegistryResourceAccess param2) {
        RegistryLoader var0 = new RegistryLoader(param2);
        RegistryOps<T> var1 = new RegistryOps<>(param0, param1, Optional.of(var0.bind(param1)));
        RegistryAccess.load(param1, var1.getAsJson(), var0);
        return var1;
    }

    private RegistryOps(DynamicOps<T> param0, RegistryAccess param1, Optional<RegistryLoader.Bound> param2) {
        super(param0);
        this.loader = param2;
        this.registryAccess = param1;
        this.asJson = param0 == JsonOps.INSTANCE ? this : new RegistryOps<>(JsonOps.INSTANCE, param1, param2);
    }

    public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.registryAccess.registry(param0);
    }

    public Optional<RegistryLoader.Bound> registryLoader() {
        return this.loader;
    }

    public DynamicOps<JsonElement> getAsJson() {
        return this.asJson;
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
