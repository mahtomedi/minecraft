package net.minecraft.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;

public class RegistryWriteOps<T> extends DelegatingOps<T> {
    private final RegistryAccess registryHolder;

    public static <T> RegistryWriteOps<T> create(DynamicOps<T> param0, RegistryAccess param1) {
        return new RegistryWriteOps<>(param0, param1);
    }

    private RegistryWriteOps(DynamicOps<T> param0, RegistryAccess param1) {
        super(param0);
        this.registryHolder = param1;
    }

    protected <E> DataResult<T> encode(E param0, T param1, ResourceKey<Registry<E>> param2, Codec<E> param3) {
        Optional<WritableRegistry<E>> var0 = this.registryHolder.registry(param2);
        if (var0.isPresent()) {
            Optional<ResourceKey<E>> var1 = var0.get().getResourceKey(param0);
            if (var1.isPresent()) {
                return ResourceLocation.CODEC.encode(var1.get().location(), this.delegate, param1);
            }
        }

        return param3.encode(param0, this.delegate, param1);
    }
}
