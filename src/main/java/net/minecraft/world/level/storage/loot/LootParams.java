package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class LootParams {
    private final ServerLevel level;
    private final Map<LootContextParam<?>, Object> params;
    private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops;
    private final float luck;

    public LootParams(ServerLevel param0, Map<LootContextParam<?>, Object> param1, Map<ResourceLocation, LootParams.DynamicDrop> param2, float param3) {
        this.level = param0;
        this.params = param1;
        this.dynamicDrops = param2;
        this.luck = param3;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public boolean hasParam(LootContextParam<?> param0) {
        return this.params.containsKey(param0);
    }

    public <T> T getParameter(LootContextParam<T> param0) {
        T var0 = (T)this.params.get(param0);
        if (var0 == null) {
            throw new NoSuchElementException(param0.getName().toString());
        } else {
            return var0;
        }
    }

    @Nullable
    public <T> T getOptionalParameter(LootContextParam<T> param0) {
        return (T)this.params.get(param0);
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParam<T> param0) {
        return (T)this.params.get(param0);
    }

    public void addDynamicDrops(ResourceLocation param0, Consumer<ItemStack> param1) {
        LootParams.DynamicDrop var0 = this.dynamicDrops.get(param0);
        if (var0 != null) {
            var0.add(param1);
        }

    }

    public float getLuck() {
        return this.luck;
    }

    public static class Builder {
        private final ServerLevel level;
        private final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
        private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops = Maps.newHashMap();
        private float luck;

        public Builder(ServerLevel param0) {
            this.level = param0;
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public <T> LootParams.Builder withParameter(LootContextParam<T> param0, T param1) {
            this.params.put(param0, param1);
            return this;
        }

        public <T> LootParams.Builder withOptionalParameter(LootContextParam<T> param0, @Nullable T param1) {
            if (param1 == null) {
                this.params.remove(param0);
            } else {
                this.params.put(param0, param1);
            }

            return this;
        }

        public <T> T getParameter(LootContextParam<T> param0) {
            T var0 = (T)this.params.get(param0);
            if (var0 == null) {
                throw new NoSuchElementException(param0.getName().toString());
            } else {
                return var0;
            }
        }

        @Nullable
        public <T> T getOptionalParameter(LootContextParam<T> param0) {
            return (T)this.params.get(param0);
        }

        public LootParams.Builder withDynamicDrop(ResourceLocation param0, LootParams.DynamicDrop param1) {
            LootParams.DynamicDrop var0 = this.dynamicDrops.put(param0, param1);
            if (var0 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
            } else {
                return this;
            }
        }

        public LootParams.Builder withLuck(float param0) {
            this.luck = param0;
            return this;
        }

        public LootParams create(LootContextParamSet param0) {
            Set<LootContextParam<?>> var0 = Sets.difference(this.params.keySet(), param0.getAllowed());
            if (!var0.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + var0);
            } else {
                Set<LootContextParam<?>> var1 = Sets.difference(param0.getRequired(), this.params.keySet());
                if (!var1.isEmpty()) {
                    throw new IllegalArgumentException("Missing required parameters: " + var1);
                } else {
                    return new LootParams(this.level, this.params, this.dynamicDrops, this.luck);
                }
            }
        }
    }

    @FunctionalInterface
    public interface DynamicDrop {
        void add(Consumer<ItemStack> var1);
    }
}
