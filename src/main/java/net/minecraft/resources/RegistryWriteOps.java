package net.minecraft.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;

public class RegistryWriteOps<T> extends DelegatingOps<T> {
    private final RegistryAccess registryAccess;

    public static <T> RegistryWriteOps<T> create(DynamicOps<T> param0, RegistryAccess param1) {
        return new RegistryWriteOps<>(param0, param1);
    }

    private RegistryWriteOps(DynamicOps<T> param0, RegistryAccess param1) {
        super(param0);
        this.registryAccess = param1;
    }

    protected <E> DataResult<T> encode(E param0, T param1, ResourceKey<? extends Registry<E>> param2, Codec<E> param3) {
        Optional<WritableRegistry<E>> var0 = this.registryAccess.ownedRegistry(param2);
        if (var0.isPresent()) {
            WritableRegistry<E> var1 = var0.get();
            Optional<ResourceKey<E>> var2 = var1.getResourceKey(param0);
            if (var2.isPresent()) {
                ResourceKey<E> var3 = var2.get();
                return ResourceLocation.CODEC.encode(var3.location(), this.delegate, param1);
            }
        }

        return param3.encode(param0, this, param1);
    }
}
