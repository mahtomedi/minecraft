package net.minecraft.core;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class GeneratingRegistry<T> extends MappedRegistry<T> {
    private final IntFunction<T> generator;

    public GeneratingRegistry(IntFunction<T> param0) {
        this.generator = param0;
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceLocation param0) {
        T var0 = super.get(param0);
        if (var0 != null) {
            return var0;
        } else if (param0.getNamespace().equals("_generated")) {
            int var1 = Integer.parseInt(param0.getPath());
            if (var1 < 0) {
                return null;
            } else {
                T var2 = this.generator.apply(var1);
                this.registerMapping(var1, param0, var2);
                return var2;
            }
        } else {
            return null;
        }
    }

    @Override
    public T byId(int param0) {
        T var0 = super.byId(param0);
        if (var0 != null) {
            return var0;
        } else if (param0 < 0) {
            return null;
        } else {
            T var1 = this.generator.apply(param0);
            this.registerMapping(param0, new ResourceLocation("_generated", Integer.toString(param0)), var1);
            return var1;
        }
    }
}
