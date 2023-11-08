package net.minecraft.commands;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CacheableFunction {
    public static final Codec<CacheableFunction> CODEC = ResourceLocation.CODEC.xmap(CacheableFunction::new, CacheableFunction::getId);
    private final ResourceLocation id;
    private boolean resolved;
    private Optional<CommandFunction<CommandSourceStack>> function = Optional.empty();

    public CacheableFunction(ResourceLocation param0) {
        this.id = param0;
    }

    public Optional<CommandFunction<CommandSourceStack>> get(ServerFunctionManager param0) {
        if (!this.resolved) {
            this.function = param0.get(this.id);
            this.resolved = true;
        }

        return this.function;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object param0) {
        if (param0 == this) {
            return true;
        } else {
            if (param0 instanceof CacheableFunction var0 && this.getId().equals(var0.getId())) {
                return true;
            }

            return false;
        }
    }
}
